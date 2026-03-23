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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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

    // Control de flujo para el login premium
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
                "--http-user-agent=${ChannelsConfig.PC_USER_AGENT}",
                "--network-caching=5000",
                "--codec=mediacodec_ndk,mediacodec_jni,all",
                "--no-stats",
                "--no-osd",
                "--avcodec-hw=any",
                "--drop-late-frames",
                "--skip-frames",
                "--clock-jitter=1000",
                "--clock-synchro=0"
            )
            libVlc = LibVLC(getApplication(), args)
            mediaPlayer = MediaPlayer(libVlc)
            mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
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
                        mediaPlayer?.aspectRatio = null 
                    }
                    MediaPlayer.Event.EncounteredError -> { handlePlaybackError() }
                    MediaPlayer.Event.EndReached -> { handlePlaybackError() }
                    MediaPlayer.Event.Vout -> { 
                        mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
                    }
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
            viewModelScope.launch {
                delay(3000 * retryCount.toLong())
                playVideo(currentChannelUrl, resetRetry = false)
            }
        } else {
            errorMessage = "Canal no disponible temporalmente"
        }
    }

    fun playVideo(url: String, resetRetry: Boolean = true) {
        if (url.isEmpty()) return
        
        if (url == "onyx://login") {
            isFromPromoChannel = true
            onShowLoginRequested?.invoke()
            return
        }

        if (resetRetry) {
            retryCount = 0
            errorMessage = null
            if (isUserAuthenticated && !isAdmin) checkAuthorization()
        }
        currentChannelUrl = url
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.stop()
                val media = Media(libVlc, url.toUri())
                media.addOption(":network-caching=5000")
                media.addOption(":codec=mediacodec_ndk,mediacodec_jni,all")
                media.addOption(":no-stats")
                withContext(Dispatchers.Main) {
                    mediaPlayer?.media = media
                    media.release()
                    mediaPlayer?.play()
                    mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
                    mediaPlayer?.aspectRatio = null
                }
                if (resetRetry) prefs.edit { putString("last_channel_url", url) }
            } catch (ignored: Exception) { withContext(Dispatchers.Main) { isLoading = false } }
        }
    }

    fun toggleChannelStatus(url: String, currentStatus: Boolean) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", url).get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { doc ->
                db.collection("channels").document(doc.id).update("isActive", !currentStatus)
            }
            authError = if (!currentStatus) "Canal Habilitado" else "Canal Deshabilitado"
        }
    }

    fun clearAuthError() { authError = null }

    fun updateChannel(channel: Channel, name: String, url: String, group: String, logo: String) {
        if (!isAdmin || channel.id == null) return
        val updates = hashMapOf(
            "name" to name,
            "url" to url,
            "group" to group,
            "logo" to logo
        )
        db.collection("channels").document(channel.id).update(updates as Map<String, Any>)
            .addOnSuccessListener {
                authError = "Canal actualizado"
            }
            .addOnFailureListener {
                authError = "Error al actualizar: ${it.localizedMessage}"
            }
    }

    fun deleteChannel(url: String) {
        if (!isAdmin) return
        db.collection("channels").whereEqualTo("url", url).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(db.collection("channels").document(doc.id))
            }
            batch.commit().addOnSuccessListener { 
                authError = "Canal eliminado de la base de datos"
                
                // Limpiar de favoritos si existe
                val favs = prefs.getStringSet("favorites_urls", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                if (favs.remove(url)) {
                    prefs.edit { putStringSet("favorites_urls", favs) }
                    loadFavorites()
                }
                
                if (currentChannelUrl == url) stopPlayback()
                filterChannels() // Forzar refresco visual
            }
        }
    }

    fun addChannel(name: String, url: String, group: String, logo: String) {
        if (!isAdmin) return
        val newChannelData = hashMapOf(
            "name" to name,
            "url" to url,
            "group" to group,
            "logo" to logo,
            "isActive" to true,
            "order" to allChannels.size
        )
        db.collection("channels").add(newChannelData).addOnSuccessListener {
            authError = "Canal Agregado" 
        }
    }

    fun fetchAllUsers(force: Boolean = false) {
        if (!isAdmin) return
        if (allUsers.isNotEmpty() && !force) return
        
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
            .addOnSuccessListener {
                authError = "Estado de usuario cambiado"
                fetchAllUsers(force = true)
            }
    }

    fun updateUserDetails(uid: String, updates: Map<String, Any>) {
        if (!isAdmin) return
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { 
                authError = "Usuario actualizado"
                fetchAllUsers(force = true)
            }
    }

    fun deleteUser(uid: String) {
        if (!isAdmin) return
        db.collection("users").document(uid).delete().addOnSuccessListener {
            authError = "Usuario eliminado del registro"
            fetchAllUsers(force = true)
        }.addOnFailureListener {
            authError = "Error al eliminar: ${it.localizedMessage}"
        }
    }

    fun updateOwnPassword(newPass: String) {
        auth.currentUser?.updatePassword(newPass)
            ?.addOnSuccessListener { authError = "Contraseña actualizada correctamente" }
            ?.addOnFailureListener { authError = "Error: Inicia sesión de nuevo para cambiar clave" }
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
                        
                        val data = hashMapOf(
                            "username" to username.substringBefore("@"),
                            "isActive" to true,
                            "role" to "USER",
                            "expiryDate" to com.google.firebase.Timestamp(Date(System.currentTimeMillis() + 2592000000L)),
                            "uid" to uid
                        )
                        
                        db.collection("users").document(uid).set(data).addOnSuccessListener {
                            authError = "Usuario Creado: ${username.substringBefore("@")}"
                            fetchAllUsers(force = true)
                            secondaryAuth.signOut()
                            secondaryApp.delete()
                        }
                    }.addOnFailureListener {
                        authError = "Error al crear: ${it.localizedMessage}"
                        secondaryApp.delete()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { authError = "Error de sistema: ${e.localizedMessage}" }
            }
        }
    }

    fun signIn(userInput: String, pass: String) {
        if (userInput.isEmpty() || pass.isEmpty()) { authError = "Completa los campos"; return }
        isLoading = true
        authError = null
        val finalEmail = if (userInput.contains("@")) userInput.trim().lowercase() else "${userInput.trim().lowercase().removeSuffix("@onyxtv.app")}@onyxtv.app"
        auth.signInWithEmailAndPassword(finalEmail, pass)
            .addOnSuccessListener { 
                isUserAuthenticated = true
                isFromPromoChannel = false
                checkAuthorization() 
            }
            .addOnFailureListener { authError = "Error: ${it.localizedMessage}"; isLoading = false }
    }

    private fun saveUserToCache(isActive: Boolean, role: String, expiry: Date?) {
        prefs.edit {
            putBoolean("user_active", isActive)
            putString("user_role", role)
            putLong("user_expiry", expiry?.time ?: 0L)
        }
    }

    private fun loadUserFromCache() {
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
                        val isActive = when(val rawIsActive = snapshot.get("isActive")) {
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
                                prefs.edit { remove("user_active") }
                                stopPlayback()
                                logout()
                            } else if (isAdmin) {
                                isUserAuthorized = true
                                observeChannels()
                            } else if (isAccountExpired(expiryDate, getRealTime())) {
                                db.collection("users").document(uid).update("isActive", false)
                                logout()
                            } else {
                                saveUserToCache(true, role, expiryDate)
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

    fun relinkDevice() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        isLoading = true
        db.collection("users").document(uid).update("deviceId", deviceId)
            .addOnSuccessListener {
                checkAuthorization()
            }
            .addOnFailureListener {
                authError = "Error al vincular: ${it.localizedMessage}"
                isLoading = false
            }
    }

    fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).update("deviceId", null)
        }
        auth.signOut()
        isUserAuthenticated = false
        isUserAuthorized = true // Volvemos a modo invitado
        isAdmin = false
        userExpiryDate = null
        prefs.edit { 
            remove("user_active")
            remove("user_role")
            remove("user_expiry")
        }
        filterChannels()
    }

    private fun saveChannelsToCache(channels: List<Channel>) {
        val array = JSONArray()
        channels.forEach { channel ->
            val obj = JSONObject()
            obj.put("name", channel.name)
            obj.put("url", channel.url)
            obj.put("logo", channel.logo ?: "")
            obj.put("group", channel.group ?: "")
            obj.put("isActive", channel.isActive)
            array.put(obj)
        }
        prefs.edit { putString("cached_channels", array.toString()) }
    }

    private fun loadChannelsFromCache(): List<Channel> {
        val json = prefs.getString("cached_channels", null) ?: return emptyList()
        val list = mutableListOf<Channel>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(Channel(obj.getString("name"), obj.getString("url"), obj.optString("logo").takeIf { it.isNotEmpty() }, obj.optString("group").takeIf { it.isNotEmpty() }, obj.optBoolean("isActive", true)))
            }
        } catch (ignored: Exception) { }
        return list
    }

    private fun observeChannels() {
        channelsListener?.remove()
        
        val cached = loadChannelsFromCache()
        if (cached.isNotEmpty()) {
            allChannels = cached
            filterChannels()
            onChannelsLoaded()
        }
        
        channelsListener = db.collection("channels")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val remoteChannels = snapshot.documents.mapNotNull { doc -> 
                    Channel(
                        doc.getString("name") ?: "", 
                        doc.getString("url") ?: "", 
                        doc.getString("logo"), 
                        doc.getString("group"), 
                        doc.getBoolean("isActive") ?: true, 
                        doc.id
                    ) 
                }
                
                allChannels = remoteChannels
                saveChannelsToCache(remoteChannels)
                filterChannels()
                onChannelsLoaded()
            }
    }

    private fun onChannelsLoaded() {
        loadFavorites()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            val activeChannels = allChannels.filter { it.isActive }
            if (activeChannels.isNotEmpty()) {
                val channelToPlay = if (savedUrl.isNotEmpty() && activeChannels.any { it.url == savedUrl }) savedUrl else activeChannels.find { it.name.contains("001") }?.url ?: activeChannels[0].url
                playVideo(channelToPlay)
            }
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) { 
        searchQuery = q
        filterChannels() 
    }

    private fun filterChannels() {
        val baseList = when {
            selectedGroup == "FAVORITOS" -> favorites.toList()
            else -> allChannels.filter { it.isActive && (selectedGroup == "TODOS" || it.group == selectedGroup) }
        }
        
        val finalBaseList = if (!isUserAuthenticated) {
            val authorizedNames = listOf("LAS ESTRELLAS", "CINECANAL", "COMEDY CENTRAL", "AZTECA UNO", "H2")
            val restrictedList = baseList.filter { channel ->
                authorizedNames.any { authorized -> channel.name.contains(authorized, ignoreCase = true) }
            }.toMutableList()
            
            restrictedList.add(0, Channel(
                name = "INICIA SESIÓN PARA DISFRUTAR +60 CANALES",
                url = "onyx://login",
                logo = null,
                group = "PROMO",
                isActive = true
            ))
            restrictedList
        } else {
            baseList
        }

        filteredChannels = if (searchQuery.isEmpty()) {
            finalBaseList
        } else {
            finalBaseList.filter { 
                it.name.contains(searchQuery, true) || 
                it.group?.contains(searchQuery, true) == true
            }
        }
    }

    fun stopPlayback() { mediaPlayer?.stop() }

    private fun loadFavorites() {
        val favs = prefs.getStringSet("favorites_urls", emptySet()) ?: emptySet()
        favorites.clear()
        favorites.addAll(allChannels.filter { it.url in favs && it.isActive })
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

                if (isUserAuthorized && isUserAuthenticated && !isAdmin) {
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
        val list = filteredChannels
        if (list.isEmpty()) return
        val i = list.indexOfFirst { it.url == currentChannelUrl }
        playVideo(list[(i + 1) % list.size].url)
    }

    fun zapPrevious() {
        val list = filteredChannels
        if (list.isEmpty()) return
        val i = list.indexOfFirst { it.url == currentChannelUrl }
        playVideo(list[if (i <= 0) list.size - 1 else i - 1].url)
    }

    override fun onCleared() {
        super.onCleared()
        channelsListener?.remove()
        mediaPlayer?.release()
        libVlc?.release()
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
