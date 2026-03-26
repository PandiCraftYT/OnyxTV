package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var channelsListener: ListenerRegistration? = null

    var libVlc: LibVLC? = null
        private set
    var mediaPlayer: MediaPlayer? = null
        private set

    var allChannels by mutableStateOf<List<Channel>>(emptyList())
        private set

    var filteredChannels by mutableStateOf<List<Channel>>(emptyList())
        private set

    var favorites = mutableStateListOf<Channel>()
        private set

    var currentChannelUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
    var isPlaying by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    var searchQuery by mutableStateOf("")
        private set

    var currentTime by mutableStateOf("")
        private set

    var currentDate by mutableStateOf("")
        private set

    private var retryCount = 0

    var isUserAuthenticated by mutableStateOf(auth.currentUser != null)
        private set
    var isUserAuthorized by mutableStateOf(false)
        private set
    var isAdmin by mutableStateOf(false)
    var isExpired by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)
        private set
    var accountStatusMessage by mutableStateOf<String?>(null)
        private set

    var userExpiryDate by mutableStateOf<Date?>(null)
        private set

    var selectedGroup by mutableStateOf("TODOS")
        private set

    var videoAspectRatio by mutableFloatStateOf(16f / 9f)
        private set

    var isFromPromoChannel by mutableStateOf(false)
    var onShowLoginRequested: (() -> Unit)? = null

    val currentUsername: String
        get() = auth.currentUser?.email?.replace("@onyxtv.app", "") ?: "Invitado"

    var allUsers = mutableStateListOf<Map<String, Any>>()
        private set

    private var networkOffset: Long = 0
    private var isTimeSynced by mutableStateOf(false)

    private val deviceId: String by lazy {
        val savedId = prefs.getString("device_unique_id", null)
        if (savedId != null) savedId else {
            val newId = UUID.randomUUID().toString()
            prefs.edit { putString("device_unique_id", newId) }
            newId
        }
    }

    init {
        initLibVLC()
        syncTimeWithNetwork()
        startClock()
        if (isUserAuthenticated) {
            loadUserFromCache()
            checkAuthorization()
        } else {
            observeChannels()
            isUserAuthorized = true 
        }
    }

    private fun syncTimeWithNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection = URL("https://www.google.com").openConnection()
                connection.connectTimeout = 5000
                val dateHeader = connection.getHeaderField("Date")
                if (dateHeader != null) {
                    val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                    val networkDate = sdf.parse(dateHeader)
                    if (networkDate != null) {
                        networkOffset = networkDate.time - SystemClock.elapsedRealtime()
                        isTimeSynced = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getRealTime(): Date {
        return if (isTimeSynced) Date(SystemClock.elapsedRealtime() + networkOffset) else Date()
    }

    private fun isAccountExpired(expiry: Date?, now: Date): Boolean {
        if (expiry == null) return true
        val calExpiry = Calendar.getInstance().apply {
            time = expiry
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }
        return now.after(calExpiry.time)
    }

    private fun initLibVLC() {
        try {
            val args = arrayListOf(
                "--http-user-agent=${ChannelsConfig.PC_USER_AGENT}",
                "--network-caching=5000",
                "--codec=mediacodec_ndk,mediacodec_jni,all",
                "--no-stats", "--no-osd", "--avcodec-hw=any", "--drop-late-frames", "--skip-frames",
                "--clock-jitter=1000", "--clock-synchro=0"
            )
            libVlc = LibVLC(getApplication(), args)
            mediaPlayer = MediaPlayer(libVlc)
            mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
            mediaPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Buffering -> { isLoading = event.buffering < 100f }
                    MediaPlayer.Event.Playing -> { isLoading = false; isPlaying = true; errorMessage = null; retryCount = 0 }
                    MediaPlayer.Event.Paused -> { isPlaying = false }
                    MediaPlayer.Event.Stopped -> { isPlaying = false }
                    MediaPlayer.Event.EncounteredError -> { isPlaying = false; handlePlaybackError() }
                    MediaPlayer.Event.EndReached -> { isPlaying = false; handlePlaybackError() }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun handlePlaybackError() {
        if (currentChannelUrl.isEmpty() || isLoading) return
        isLoading = false
        if (retryCount < MAX_RETRIES) {
            retryCount++
            errorMessage = "Reconectando canal ($retryCount/$MAX_RETRIES)..."
            viewModelScope.launch { delay(3000 * retryCount.toLong()); playVideo(currentChannelUrl, resetRetry = false) }
        } else { errorMessage = "Canal no disponible temporalmente" }
    }

    fun togglePause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
            else if (currentChannelUrl.isNotEmpty()) playVideo(currentChannelUrl, resetRetry = false)
        }
    }

    fun playVideo(url: String, resetRetry: Boolean = true) {
        if (url.isEmpty()) return
        if (url == "onyx://login") { isFromPromoChannel = true; onShowLoginRequested?.invoke(); return }
        if (resetRetry) { retryCount = 0; errorMessage = null; if (isUserAuthenticated && !isAdmin) checkAuthorization() }
        currentChannelUrl = url
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.stop()
                val media = Media(libVlc, url.toUri())
                media.addOption(":network-caching=5000")
                media.addOption(":codec=mediacodec_ndk,mediacodec_jni,all")
                withContext(Dispatchers.Main) { mediaPlayer?.media = media; media.release(); mediaPlayer?.play() }
                if (resetRetry) prefs.edit { putString("last_channel_url", url) }
            } catch (ignored: Exception) { withContext(Dispatchers.Main) { isLoading = false } }
        }
    }

    fun toggleChannelStatus(url: String, currentStatus: Boolean) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", url).get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { doc -> db.collection("channels").document(doc.id).update("isActive", !currentStatus) }
            authError = if (!currentStatus) "Canal Habilitado" else "Canal Deshabilitado"
        }
    }

    fun clearAuthError() { authError = null }

    fun updateChannel(channel: Channel, name: String, url: String, group: String, logo: String) {
        if (!isAdmin || channel.id == null) return
        val updates = hashMapOf("name" to name, "url" to url, "group" to group, "logo" to logo)
        db.collection("channels").document(channel.id).update(updates as Map<String, Any>)
            .addOnSuccessListener { authError = "Canal actualizado" }
    }

    fun deleteChannel(url: String) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", url).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc -> batch.delete(db.collection("channels").document(doc.id)) }
            batch.commit().addOnSuccessListener { 
                authError = "Canal eliminado"
                val favs = prefs.getStringSet("favorites_urls", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                if (favs.remove(url)) { prefs.edit { putStringSet("favorites_urls", favs) }; loadFavorites() }
                if (currentChannelUrl == url) stopPlayback()
                filterChannels()
            }
        }
    }

    fun addChannel(name: String, url: String, group: String, logo: String) {
        if (!isAdmin) return
        val data = hashMapOf("name" to name, "url" to url, "group" to group, "logo" to logo, "isActive" to true, "order" to allChannels.size)
        db.collection("channels").add(data).addOnSuccessListener { authError = "Canal Agregado" }
    }

    fun fetchAllUsers(force: Boolean = false) {
        if (!isAdmin) return
        if (allUsers.isNotEmpty() && !force) return
        db.collection("users").get().addOnSuccessListener { snapshot ->
            allUsers.clear()
            allUsers.addAll(snapshot.documents.map { doc -> val d = doc.data?.toMutableMap() ?: mutableMapOf(); d["uid"] = doc.id; d })
        }
    }

    fun toggleUserStatus(uid: String, currentStatus: Boolean) {
        if (!isAdmin) return
        db.collection("users").document(uid).update("isActive", !currentStatus).addOnSuccessListener { authError = "Estado cambiado"; fetchAllUsers(force = true) }
    }

    fun updateUserDetails(uid: String, updates: Map<String, Any>) {
        if (!isAdmin) return
        db.collection("users").document(uid).update(updates).addOnSuccessListener { authError = "Usuario actualizado"; fetchAllUsers(force = true) }
    }

    fun deleteUser(uid: String) {
        if (!isAdmin) return
        db.collection("users").document(uid).delete().addOnSuccessListener { authError = "Usuario eliminado"; fetchAllUsers(force = true) }
    }

    fun updateOwnPassword(newPass: String) {
        auth.currentUser?.updatePassword(newPass)
            ?.addOnSuccessListener { authError = "Contraseña actualizada" }
            ?.addOnFailureListener { authError = "Error: Re-autentique su sesión" }
    }

    fun createManualUser(username: String, pass: String) {
        if (!isAdmin) return
        val email = if (username.contains("@")) username.trim().lowercase() else "${username.trim().lowercase()}@onyxtv.app"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val secondaryOptions = auth.app.options
                val secondaryAppName = "SecondaryApp_${System.currentTimeMillis()}"
                withContext(Dispatchers.Main) {
                    val secondaryApp = FirebaseApp.initializeApp(getApplication(), secondaryOptions, secondaryAppName)
                    val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
                    secondaryAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { task ->
                        val uid = task.user?.uid ?: return@addOnSuccessListener
                        val data = hashMapOf("username" to username.substringBefore("@"), "isActive" to true, "role" to "USER", "expiryDate" to com.google.firebase.Timestamp(Date(System.currentTimeMillis() + 2592000000L)), "uid" to uid)
                        db.collection("users").document(uid).set(data).addOnSuccessListener { authError = "Creado"; fetchAllUsers(force = true); secondaryAuth.signOut(); secondaryApp.delete() }
                    }.addOnFailureListener { authError = "Error: ${it.localizedMessage}"; secondaryApp.delete() }
                }
            } catch (e: Exception) { withContext(Dispatchers.Main) { authError = "Error: ${e.localizedMessage}" } }
        }
    }

    fun signIn(userInput: String, pass: String) {
        if (userInput.isEmpty() || pass.isEmpty()) { authError = "Campos vacíos"; return }
        isLoading = true
        val finalEmail = if (userInput.contains("@")) userInput.trim().lowercase() else "${userInput.trim().lowercase().removeSuffix("@onyxtv.app")}@onyxtv.app"
        auth.signInWithEmailAndPassword(finalEmail, pass)
            .addOnSuccessListener { 
                isUserAuthenticated = true
                isFromPromoChannel = false
                isExpired = false
                checkAuthorization() 
            }
            .addOnFailureListener { authError = "Error: ${it.localizedMessage}"; isLoading = false }
    }

    private fun saveUserToCache(isActive: Boolean, role: String, expiry: Date?) {
        prefs.edit { putBoolean("user_active", isActive); putString("user_role", role); putLong("user_expiry", expiry?.time ?: 0L) }
    }

    private fun loadUserFromCache() {
        val role = prefs.getString("user_role", "USER") ?: "USER"
        val expiryTime = prefs.getLong("user_expiry", 0L)
        isAdmin = role.uppercase() == "ADMIN"
        userExpiryDate = if (expiryTime > 0) Date(expiryTime) else null
    }

    private fun checkAuthorization() {
        val user = auth.currentUser ?: return
        isLoading = true
        viewModelScope.launch {
            var sync = 0; while (!isTimeSynced && sync < 40) { delay(100); sync++ }
            db.collection("users").document(user.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val role = snapshot.getString("role") ?: "USER"
                    val isActive = snapshot.get("isActive") as? Boolean ?: false
                    val expiry = snapshot.getTimestamp("expiryDate")?.toDate()
                    val sid = snapshot.getString("deviceId")
                    isAdmin = role.uppercase() == "ADMIN"
                    if (!isAdmin && sid != null && sid != deviceId) { 
                        isUserAuthorized = false
                        accountStatusMessage = "Vinculada a otra TV."
                        stopPlayback()
                    } else {
                        if (!isAdmin && sid == null) db.collection("users").document(user.uid).update("deviceId", deviceId)
                        if (!isActive) { isUserAuthorized = false; stopPlayback(); logout() }
                        else if (isAdmin) { isUserAuthorized = true; isExpired = false; observeChannels() }
                        else if (isAccountExpired(expiry, getRealTime())) { isExpired = true; isUserAuthorized = true; observeChannels() }
                        else { isExpired = false; saveUserToCache(true, role, expiry); userExpiryDate = expiry; isUserAuthorized = true; observeChannels() }
                    }
                }
                isLoading = false
            }
        }
    }

    fun relinkDevice() {
        val user = auth.currentUser ?: return
        isLoading = true
        db.collection("users").document(user.uid).update("deviceId", deviceId).addOnSuccessListener { checkAuthorization() }
    }

    fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) db.collection("users").document(uid).update("deviceId", null)
        auth.signOut(); isUserAuthenticated = false; isUserAuthorized = true; isAdmin = false; isExpired = false; userExpiryDate = null
        prefs.edit { remove("user_active"); remove("user_role"); remove("user_expiry") }
        filterChannels()
    }

    private fun saveChannelsToCache(channels: List<Channel>) {
        val array = JSONArray()
        channels.forEach { c -> val o = JSONObject(); o.put("name", c.name); o.put("url", c.url); o.put("isActive", c.isActive); array.put(o) }
        prefs.edit { putString("cached_channels", array.toString()) }
    }

    private fun loadChannelsFromCache(): List<Channel> {
        val json = prefs.getString("cached_channels", null) ?: return emptyList()
        val list = mutableListOf<Channel>()
        try { val a = JSONArray(json); for (i in 0 until a.length()) { val o = a.getJSONObject(i); list.add(Channel(o.getString("name"), o.getString("url"), null, null, o.getBoolean("isActive"))) } } catch (e: Exception) {}
        return list
    }

    private fun observeChannels() {
        channelsListener?.remove()
        val cached = loadChannelsFromCache()
        if (cached.isNotEmpty()) { allChannels = cached; filterChannels(); onChannelsLoaded() }
        channelsListener = db.collection("channels").orderBy("order", Query.Direction.ASCENDING).addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener
            allChannels = snapshot.documents.mapNotNull { d -> Channel(d.getString("name") ?: "", d.getString("url") ?: "", d.getString("logo"), d.getString("group"), d.getBoolean("isActive") ?: true, d.id) }
            saveChannelsToCache(allChannels); filterChannels(); onChannelsLoaded()
        }
    }

    private fun onChannelsLoaded() {
        loadFavorites()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            val active = allChannels.filter { it.isActive }
            if (active.isNotEmpty()) { val toPlay = if (savedUrl.isNotEmpty() && active.any { it.url == savedUrl }) savedUrl else active[0].url; playVideo(toPlay) }
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) { searchQuery = q; filterChannels() }

    private fun filterChannels() {
        val baseList = when {
            selectedGroup == "FAVORITOS" -> favorites.toList()
            else -> allChannels.filter { it.isActive && (selectedGroup == "TODOS" || it.group == selectedGroup) }
        }
        if (!isUserAuthenticated || isExpired) {
            val authNames = listOf("LAS ESTRELLAS", "CINECANAL", "COMEDY CENTRAL", "AZTECA UNO", "H2")
            val restricted = baseList.filter { c -> authNames.any { a -> c.name.contains(a, true) } }
            val promo = Channel("INICIA SESIÓN PARA DISFRUTAR +60 CANALES", "onyx://login", null, "PROMO", true)
            val combined = (listOf(promo) + restricted)
            filteredChannels = if (searchQuery.isEmpty()) combined else combined.filter { it.name.contains(searchQuery, true) || it.group?.contains(searchQuery, true) == true }
        } else {
            filteredChannels = if (searchQuery.isEmpty()) baseList else baseList.filter { it.name.contains(searchQuery, true) || it.group?.contains(searchQuery, true) == true }
        }
    }

    fun stopPlayback() { mediaPlayer?.stop() }

    private fun loadFavorites() {
        val favs = prefs.getStringSet("favorites_urls", emptySet()) ?: emptySet()
        favorites.clear(); favorites.addAll(allChannels.filter { it.url in favs && it.isActive })
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val now = getRealTime()
                currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(now)
                currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                if (isUserAuthorized && isUserAuthenticated && !isAdmin && isAccountExpired(userExpiryDate, now)) { isExpired = true; filterChannels() }
                delay(1000)
            }
        }
    }

    fun zapNext() {
        if (filteredChannels.isEmpty()) return
        val currentList = filteredChannels
        val i = currentList.indexOfFirst { it.url == currentChannelUrl }
        if (i != -1) {
            val nextIndex = (i + 1) % currentList.size
            if (currentList[nextIndex].url == "onyx://login") {
                playVideo(currentList[(nextIndex + 1) % currentList.size].url)
            } else {
                playVideo(currentList[nextIndex].url)
            }
        }
    }

    fun zapPrevious() {
        if (filteredChannels.isEmpty()) return
        val currentList = filteredChannels
        val i = currentList.indexOfFirst { it.url == currentChannelUrl }
        if (i != -1) {
            val prevIndex = if (i <= 0) currentList.size - 1 else i - 1
            if (currentList[prevIndex].url == "onyx://login") {
                val skipIndex = if (prevIndex <= 0) currentList.size - 1 else prevIndex - 1
                playVideo(currentList[skipIndex].url)
            } else {
                playVideo(currentList[prevIndex].url)
            }
        }
    }

    override fun onCleared() { super.onCleared(); channelsListener?.remove(); mediaPlayer?.release(); libVlc?.release() }

    companion object { private const val MAX_RETRIES = 3 }
}
