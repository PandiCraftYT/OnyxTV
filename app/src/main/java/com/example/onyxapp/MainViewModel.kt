package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
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
    private var authListener: ListenerRegistration? = null

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

    var errorMessage by mutableStateOf<String?>(null)

    var searchQuery by mutableStateOf("")
        private set

    var currentTime by mutableStateOf("")
        private set

    var currentDate by mutableStateOf("")
        private set

    private var retryCount = 0
    private val MAX_RETRIES = 3

    var isUserAuthenticated by mutableStateOf(auth.currentUser != null)
        private set
    var isUserAuthorized by mutableStateOf(false)
        private set
    var isAdmin by mutableStateOf(false)

    var authError by mutableStateOf<String?>(null)
        private set
    var accountStatusMessage by mutableStateOf<String?>(null)
        private set

    var userExpiryDate by mutableStateOf<Date?>(null)
        private set

    var selectedGroup by mutableStateOf("TODOS")
        private set

    var videoAspectRatio by mutableStateOf(16f / 9f)
        private set

    val availableGroups: List<String>
        get() = listOf("TODOS", "FAVORITOS") + allChannels.mapNotNull { it.group }.distinct().sorted()

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
            prefs.edit().putString("device_unique_id", newId).apply()
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
            isLoading = false
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
        return if (isTimeSynced) {
            Date(SystemClock.elapsedRealtime() + networkOffset)
        } else {
            Date()
        }
    }

    private fun isAccountExpired(expiry: Date?, now: Date): Boolean {
        if (expiry == null) return true
        val calExpiry = Calendar.getInstance().apply { 
            time = expiry 
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        return now.after(calExpiry.time)
    }

    private fun initLibVLC() {
        try {
            val args = arrayListOf(
                "-vvv",
                "--http-user-agent=${ChannelsConfig.PC_USER_AGENT}",
                "--network-caching=5000",
                "--clock-jitter=0",
                "--clock-synchro=0",
                "--rtsp-tcp",
                "--drop-late-frames",
                "--skip-frames"
            )
            libVlc = LibVLC(getApplication(), args)
            mediaPlayer = MediaPlayer(libVlc)
            mediaPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Buffering -> {
                        isLoading = event.buffering < 100f
                        if (event.buffering >= 100f) errorMessage = null
                    }
                    MediaPlayer.Event.Playing -> {
                        isLoading = false
                        errorMessage = null
                        retryCount = 0
                        updateVideoSize()
                    }
                    MediaPlayer.Event.EncounteredError -> { handlePlaybackError() }
                    MediaPlayer.Event.EndReached -> { handlePlaybackError() }
                    MediaPlayer.Event.Vout -> { updateVideoSize() }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun updateVideoSize() {
        viewModelScope.launch(Dispatchers.Main) {
            mediaPlayer?.let { player ->
                val track = player.currentVideoTrack
                if (track != null && track.width > 0 && track.height > 0) {
                    val newAspect = track.width.toFloat() / track.height.toFloat()
                    if (newAspect in 0.5f..2.5f) {
                        videoAspectRatio = newAspect
                    }
                }
            }
        }
    }

    private fun handlePlaybackError() {
        if (currentChannelUrl.isEmpty() || isLoading) return
        
        isLoading = false
        if (retryCount < MAX_RETRIES) {
            retryCount++
            errorMessage = "Reconectando canal ($retryCount/$MAX_RETRIES)..."
            viewModelScope.launch {
                delay(3000 * retryCount.toLong())
                playVideo(currentChannelUrl, resetRetry = false)
            }
        } else {
            errorMessage = "Canal no disponible temporalmente"
        }
    }

    fun clearAuthError() { authError = null }

    fun updateChannelsFromUrl(url: String) {
        if (!isAdmin) return
        isLoading = true
        authError = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val m3uContent = URL(url).readText()
                val newChannels = ChannelsConfig.parseM3U(m3uContent)

                if (newChannels.isNotEmpty()) {
                    uploadChannelsToFirestore(newChannels)
                    withContext(Dispatchers.Main) {
                        saveChannelsToCache(newChannels)
                        allChannels = newChannels
                        onChannelsLoaded()
                        authError = "EXITO: ${newChannels.size} canales actualizados."
                    }
                } else {
                    withContext(Dispatchers.Main) { authError = "ERROR: La lista está vacía." }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { authError = "ERROR: ${e.localizedMessage}" }
            } finally {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    private suspend fun uploadChannelsToFirestore(channels: List<Channel>) {
        val collectionRef = db.collection("channels")
        try {
            val snapshot = Tasks.await(collectionRef.get())
            if (!snapshot.isEmpty) {
                snapshot.documents.chunked(500).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    Tasks.await(batch.commit())
                }
            }

            channels.chunked(500).forEach { chunk ->
                val batchInsert = db.batch()
                chunk.forEachIndexed { index, channel ->
                    val doc = collectionRef.document()
                    batchInsert.set(doc, hashMapOf(
                        "name" to channel.name,
                        "url" to channel.url,
                        "logo" to channel.logo,
                        "group" to channel.group,
                        "order" to index
                    ))
                }
                Tasks.await(batchInsert.commit())
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateChannelUrl(oldUrl: String, newUrl: String) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", oldUrl).get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val docId = snapshot.documents[0].id
                db.collection("channels").document(docId).update("url", newUrl)
                    .addOnSuccessListener { 
                        allChannels = emptyList()
                        observeChannels()
                        authError = "URL Actualizada" 
                    }
            }
        }
    }

    fun deleteChannel(url: String) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", url).get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val docId = snapshot.documents[0].id
                db.collection("channels").document(docId).delete()
                    .addOnSuccessListener { 
                        allChannels = emptyList()
                        observeChannels()
                        authError = "Canal eliminado" 
                    }
            }
        }
    }

    fun addChannel(name: String, url: String, group: String, logo: String) {
        if (!isAdmin) return
        val newChannel = hashMapOf(
            "name" to name,
            "url" to url,
            "group" to group,
            "logo" to logo,
            "order" to allChannels.size
        )
        db.collection("channels").add(newChannel).addOnSuccessListener { 
            allChannels = emptyList()
            observeChannels()
            authError = "Canal Agregado" 
        }
    }

    fun fetchAllUsers() {
        if (!isAdmin) return
        db.collection("users").get().addOnSuccessListener { snapshot ->
            allUsers.clear()
            allUsers.addAll(snapshot.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["uid"] = doc.id
                data
            })
        }
    }

    fun toggleUserStatus(uid: String, currentStatus: Boolean) {
        if (!isAdmin) return
        db.collection("users").document(uid).update("isActive", !currentStatus)
            .addOnSuccessListener { fetchAllUsers(); authError = "Estado de usuario cambiado" }
    }

    fun updateUserDetails(uid: String, updates: Map<String, Any>) {
        if (!isAdmin) return
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { fetchAllUsers(); authError = "Usuario actualizado" }
    }

    fun updateOwnPassword(newPass: String) {
        auth.currentUser?.updatePassword(newPass)
            ?.addOnSuccessListener { authError = "Contraseña actualizada correctamente" }
            ?.addOnFailureListener { authError = "Error: Inicia sesión de nuevo para cambiar clave" }
    }

    fun createManualUser(username: String, pass: String) {
        if (!isAdmin) return
        val email = if (username.contains("@")) username.trim().lowercase() else "${username.trim().lowercase()}@onyxtv.app"
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {
            val uid = it.user?.uid ?: return@addOnSuccessListener
            val data = hashMapOf(
                "username" to username.substringBefore("@"),
                "isActive" to true,
                "role" to "USER",
                "expiryDate" to com.google.firebase.Timestamp(Date(System.currentTimeMillis() + 2592000000L)),
                "uid" to uid
            )
            db.collection("users").document(uid).set(data).addOnSuccessListener {
                authError = "Usuario Creado Exitosamente"
                fetchAllUsers()
            }
        }
    }

    fun signIn(userInput: String, pass: String) {
        if (userInput.isEmpty() || pass.isEmpty()) { authError = "Completa los campos"; return }
        isLoading = true
        authError = null
        val finalEmail = if (userInput.contains("@")) userInput.trim().lowercase() else "${userInput.trim().lowercase().removeSuffix("@onyxtv.app")}@onyxtv.app"
        auth.signInWithEmailAndPassword(finalEmail, pass)
            .addOnSuccessListener { isUserAuthenticated = true; checkAuthorization() }
            .addOnFailureListener { authError = "Error: ${it.localizedMessage}"; isLoading = false }
    }

    private fun saveUserToCache(isActive: Boolean, role: String, expiry: Date?) {
        prefs.edit().apply {
            putBoolean("user_active", isActive)
            putString("user_role", role)
            putLong("user_expiry", expiry?.time ?: 0L)
            apply()
        }
    }

    private fun loadUserFromCache() {
        val isActive = prefs.getBoolean("user_active", false)
        val role = prefs.getString("user_role", "USER") ?: "USER"
        val expiryTime = prefs.getLong("user_expiry", 0L)
        
        isAdmin = role.uppercase() == "ADMIN"
        userExpiryDate = if (expiryTime > 0) Date(expiryTime) else null
    }

    private fun checkAuthorization() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        isLoading = true
        authError = null

        viewModelScope.launch {
            var syncAttempts = 0
            while (!isTimeSynced && syncAttempts < 40) {
                delay(100)
                syncAttempts++
            }

            db.collection("users").document(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val role = snapshot.getString("role") ?: "USER"
                        val rawIsActive = snapshot.get("isActive")
                        val isActive = when(rawIsActive) {
                            is Boolean -> rawIsActive
                            is String -> rawIsActive.trim().lowercase() == "true"
                            else -> false
                        }
                        val expiryDate = snapshot.getTimestamp("expiryDate")?.toDate()
                        
                        val storedDeviceId = snapshot.getString("deviceId")
                        val currentDeviceId = deviceId
                        isAdmin = role.uppercase() == "ADMIN"

                        if (!isAdmin && storedDeviceId != null && storedDeviceId != currentDeviceId) {
                            isUserAuthorized = false
                            accountStatusMessage = "Cuenta vinculada a otra TV."
                            stopPlayback()
                        } else {
                            if (!isAdmin && storedDeviceId == null) {
                                db.collection("users").document(uid).update("deviceId", currentDeviceId)
                            }

                            if (!isActive) {
                                isUserAuthorized = false
                                prefs.edit().remove("user_active").apply()
                                stopPlayback()
                                logout()
                            } else if (isAdmin) {
                                isUserAuthorized = true
                                observeChannels()
                            } else if (isAccountExpired(expiryDate, getRealTime())) {
                                db.collection("users").document(uid).update("isActive", false)
                                logout()
                            } else {
                                saveUserToCache(isActive, role, expiryDate)
                                userExpiryDate = expiryDate
                                isUserAuthorized = true
                                observeChannels()
                            }
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        }
    }

    fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).update("deviceId", null)
        }
        auth.signOut()
        isUserAuthenticated = false
        isUserAuthorized = false
        isAdmin = false
        userExpiryDate = null
        prefs.edit().remove("user_active").remove("user_role").remove("user_expiry").apply()
        stopPlayback()
    }

    private fun saveChannelsToCache(channels: List<Channel>) {
        val array = JSONArray()
        channels.forEach { channel ->
            val obj = JSONObject()
            obj.put("name", channel.name)
            obj.put("url", channel.url)
            obj.put("logo", channel.logo ?: "")
            obj.put("group", channel.group ?: "")
            array.put(obj)
        }
        prefs.edit().putString("cached_channels", array.toString()).apply()
    }

    private fun loadChannelsFromCache(): List<Channel> {
        val json = prefs.getString("cached_channels", null) ?: return emptyList()
        val list = mutableListOf<Channel>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(Channel(obj.getString("name"), obj.getString("url"), obj.optString("logo").takeIf { it.isNotEmpty() }, obj.optString("group").takeIf { it.isNotEmpty() }))
            }
        } catch (e: Exception) { }
        return list
    }

    private fun observeChannels() {
        if (allChannels.isNotEmpty()) return
        val cached = loadChannelsFromCache()
        if (cached.isNotEmpty()) {
            allChannels = cached
            onChannelsLoaded()
        }
        if (allChannels.isEmpty()) {
            db.collection("channels").orderBy("order", Query.Direction.ASCENDING).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null) {
                        val remoteChannels = snapshot.documents.mapNotNull { doc -> Channel(doc.getString("name") ?: "", doc.getString("url") ?: "", doc.getString("logo"), doc.getString("group")) }
                        allChannels = remoteChannels
                        saveChannelsToCache(remoteChannels)
                        onChannelsLoaded()
                    }
                }
                .addOnFailureListener { isLoading = false }
        }
    }

    private fun onChannelsLoaded() {
        loadFavorites()
        filterChannels()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            val channelToPlay = if (savedUrl.isNotEmpty() && allChannels.any { it.url == savedUrl }) savedUrl else allChannels.find { it.name.contains("001") }?.url ?: allChannels[0].url
            playVideo(channelToPlay)
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) { searchQuery = q; filterChannels() }

    fun updateSelectedGroup(group: String) { selectedGroup = group; filterChannels() }

    private fun filterChannels() {
        val baseList = when (selectedGroup) {
            "TODOS" -> allChannels
            "FAVORITOS" -> favorites.toList()
            else -> allChannels.filter { it.group == selectedGroup }
        }
        filteredChannels = if (searchQuery.isEmpty()) baseList else baseList.filter { it.name.contains(searchQuery, true) }
    }

    fun playVideo(url: String, resetRetry: Boolean = true) {
        if (url.isEmpty()) return
        if (resetRetry) {
            retryCount = 0
            errorMessage = null
            videoAspectRatio = 16f / 9f
            // VERIFICACIÓN LAZY AL CAMBIAR CANAL (Gasto mínimo de tokens)
            if (isUserAuthenticated && !isAdmin) checkAuthorization()
        }
        currentChannelUrl = url
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.stop()
                val media = Media(libVlc, Uri.parse(url))
                media.addOption(":http-user-agent=${ChannelsConfig.PC_USER_AGENT}")
                media.addOption(":network-caching=5000")
                media.addOption(":clock-jitter=0")
                withContext(Dispatchers.Main) {
                    mediaPlayer?.media = media
                    media.release()
                    mediaPlayer?.play()
                }
                if (resetRetry) prefs.edit().putString("last_channel_url", url).apply()
            } catch (e: Exception) { withContext(Dispatchers.Main) { isLoading = false } }
        }
    }

    fun stopPlayback() { mediaPlayer?.stop() }

    fun toggleFavorite(c: Channel) {
        if (favorites.any { it.url == c.url }) favorites.removeAll { it.url == c.url } else favorites.add(c)
        prefs.edit().putStringSet("favorites_urls", favorites.map { it.url }.toSet()).apply()
        if (selectedGroup == "FAVORITOS") filterChannels()
    }

    private fun loadFavorites() {
        val favs = prefs.getStringSet("favorites_urls", emptySet()) ?: emptySet()
        favorites.clear()
        favorites.addAll(allChannels.filter { it.url in favs })
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val now = getRealTime()
                val locale = Locale.getDefault()
                val timeSdf = SimpleDateFormat("hh:mm:ss a", locale)
                currentTime = timeSdf.format(now)
                val dateSdf = SimpleDateFormat("dd/MM/yyyy", locale)
                currentDate = dateSdf.format(now)

                // Verificación constante de fecha (CONSUMO 0 TOKENS)
                if (isUserAuthorized && !isAdmin) {
                    userExpiryDate?.let { expiry ->
                        if (isAccountExpired(expiry, now)) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) db.collection("users").document(uid).update("isActive", false)
                            authError = "Suscripción expirada."
                            logout()
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    fun zapNext() {
        val list = allChannels
        if (list.isEmpty()) return
        val i = list.indexOfFirst { it.url == currentChannelUrl }
        playVideo(list[(i + 1) % list.size].url)
    }

    fun zapPrevious() {
        val list = allChannels
        if (list.isEmpty()) return
        val i = list.indexOfFirst { it.url == currentChannelUrl }
        playVideo(list[if (i <= 0) list.size - 1 else i - 1].url)
    }

    override fun onCleared() {
        super.onCleared()
        authListener?.remove()
        mediaPlayer?.release()
        libVlc?.release()
    }
}
