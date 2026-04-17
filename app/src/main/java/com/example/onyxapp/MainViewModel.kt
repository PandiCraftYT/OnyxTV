package com.example.onyxapp

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val supabase = SupabaseConfig.supabase
    private val prefs = application.getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE)

    val playerManager = PlayerManager(application) { event ->
        when (event.type) {
            MediaPlayer.Event.Buffering -> { isLoading = event.buffering < 100f }
            MediaPlayer.Event.Playing -> {
                isLoading = false
                isPlaying = true
                errorMessage = null
                retryCount = 0
            }
            MediaPlayer.Event.Paused -> { isPlaying = false }
            MediaPlayer.Event.Stopped -> { isPlaying = false }
            MediaPlayer.Event.EncounteredError -> {
                isPlaying = false
                handlePlaybackError()
            }
            MediaPlayer.Event.EndReached -> {
                isPlaying = false
                handlePlaybackError()
            }
        }
    }

    val libVlc get() = playerManager.libVlc
    val mediaPlayer get() = playerManager.mediaPlayer

    var allChannels by mutableStateOf<List<Channel>>(emptyList())
        private set

    var filteredChannels by mutableStateOf<List<Channel>>(emptyList())
        private set

    var allMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var filteredMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    val currentPlaybackTitle by derivedStateOf {
        allChannels.find { it.url == currentChannelUrl }?.name
            ?: allMovies.find { it.video_url == currentChannelUrl }?.title
            ?: ""
    }

    val currentPlaybackGroup by derivedStateOf {
        allChannels.find { it.url == currentChannelUrl }?.group
            ?: if (allMovies.any { it.video_url == currentChannelUrl }) "PELÍCULA" else "GENERAL"
    }

    var favorites = mutableStateListOf<Channel>()
        private set

    var currentChannelUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
    var isAuthLoading by mutableStateOf(false)
        private set
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
    private val MAX_RETRIES = 15

    var isUserAuthenticated by mutableStateOf(false)
        private set
    var isUserAuthorized by mutableStateOf(true)
    var isAdmin by mutableStateOf(false)
    var isExpired by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)
        private set
    var accountStatusMessage by mutableStateOf<String?>(null)
        private set

    var userExpiryDate by mutableStateOf<Date?>(null)
        private set

    var videoAspectRatio by mutableFloatStateOf(16f / 9f)
    var isFromPromoChannel = false
    var onShowLoginRequested: (() -> Unit)? = null

    // --- Mensajería Global ---
    var activeGlobalMessage by mutableStateOf<GlobalMessage?>(null)
        private set
    private var messageDismissJob: Job? = null

    // --- Lógica de Actualización ---
    var appUpdateConfig by mutableStateOf<AppConfig?>(null)
        private set
    var isDownloadingUpdate by mutableStateOf(false)
        private set
    var downloadProgress by mutableStateOf(0f)
        private set

    private var networkOffset = 0L
    private var isTimeSynced = false

    private val deviceId: String by lazy {
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit { putString("device_id", id) }
        }
        id
    }

    private var selectedGroup = "TODOS"

    init {
        syncTimeWithNetwork()
        startClock()
        loadUserFromCache()
        setupRealtime()
        checkForUpdates()

        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                Log.d("Onyx", "Auth Status: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        isUserAuthenticated = true
                        checkAuthorization()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        isUserAuthenticated = false
                        isUserAuthorized = true
                        isAdmin = false
                        observeChannels()
                        observeMovies()
                    }
                    else -> { /* Cargando o refrescando sesión */ }
                }
            }
        }
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            // Reintento infinito silencioso para conexión Realtime (Ideal para TVs con WiFi inestable)
            while (true) {
                try {
                    Log.d("OnyxRealtime", "Intentando conectar a Realtime...")
                    supabase.realtime.connect()
                    
                    // --- CANALES ---
                    val myChannel = supabase.realtime.channel("public-channels")
                    val changeFlow = myChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "channels"
                    }
                    launch {
                        changeFlow.collect { action ->
                            withContext(Dispatchers.Main) {
                                handleDatabaseAction(action)
                            }
                        }
                    }
                    myChannel.subscribe()

                    // --- PELÍCULAS ---
                    val moviesChannel = supabase.realtime.channel("public-movies")
                    val moviesFlow = moviesChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "movies"
                    }
                    launch {
                        moviesFlow.collect { observeMovies() }
                    }
                    moviesChannel.subscribe()

                    // --- MENSAJES GLOBALES ---
                    val messagesChannel = supabase.realtime.channel("global-messages")
                    val messagesFlow = messagesChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "global_messages"
                    }
                    launch {
                        messagesFlow.collect { action ->
                            withContext(Dispatchers.Main) {
                                handleGlobalMessageAction(action)
                            }
                        }
                    }
                    messagesChannel.subscribe()

                    // --- ESCUCHAR CAMBIOS EN TABLA 'users' ---
                    launch {
                        supabase.auth.sessionStatus.collectLatest { status ->
                            if (status is SessionStatus.Authenticated) {
                                val user = status.session.user
                                if (user != null) {
                                    val profileChannel = supabase.realtime.channel("user-data-sync")
                                    val profileFlow = profileChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                                        table = "users"
                                    }
                                    launch {
                                        profileFlow.collect { action ->
                                            val profileId = when (action) {
                                                is PostgresAction.Update -> action.record["id"]?.toString()?.trim('"')
                                                is PostgresAction.Insert -> action.record["id"]?.toString()?.trim('"')
                                                else -> null
                                            }
                                            if (profileId == user.id) {
                                                Log.d("Onyx", "Realtime: Cambio detectado para usuario: $profileId")
                                                checkAuthorization()
                                            }
                                        }
                                    }
                                    profileChannel.subscribe()
                                }
                            }
                        }
                    }

                    // Monitorear estado de conexión
                    supabase.realtime.status.collect { status ->
                        Log.d("OnyxRealtime", "Estado: $status")
                        if (status == Realtime.Status.DISCONNECTED) {
                           // El bucle while volverá a intentar la conexión
                        }
                    }
                    break // Salir del bucle si todo se configuró bien

                } catch (e: Exception) {
                    Log.e("OnyxRealtime", "Error en setupRealtime, reintentando en 5s...", e)
                    delay(5000)
                }
            }
        }
    }

    private fun handleGlobalMessageAction(action: PostgresAction) {
        try {
            when (action) {
                is PostgresAction.Insert -> {
                    val msg = action.decodeRecord<GlobalMessage>()
                    if (msg.isActive) showGlobalMessage(msg)
                }
                is PostgresAction.Update -> {
                    val msg = action.decodeRecord<GlobalMessage>()
                    if (msg.isActive) {
                        showGlobalMessage(msg)
                    } else if (activeGlobalMessage?.id == msg.id) {
                        dismissGlobalMessage()
                    }
                }
                is PostgresAction.Delete -> {
                    val deletedId = action.oldRecord["id"]?.toString()?.toLongOrNull()
                    if (activeGlobalMessage?.id == deletedId) {
                        dismissGlobalMessage()
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("Onyx", "Error handling global message action", e)
        }
    }

    private fun showGlobalMessage(msg: GlobalMessage) {
        messageDismissJob?.cancel()
        activeGlobalMessage = msg
        
        if (msg.durationSeconds > 0) {
            messageDismissJob = viewModelScope.launch {
                delay(msg.durationSeconds * 1000L)
                activeGlobalMessage = null
            }
        }
    }

    fun dismissGlobalMessage() {
        messageDismissJob?.cancel()
        activeGlobalMessage = null
    }

    private fun handleDatabaseAction(action: PostgresAction) {
        try {
            when (action) {
                is PostgresAction.Update -> {
                    val updated = action.decodeRecord<Channel>()
                    allChannels = allChannels.map {
                        if (it.id == updated.id || it.url == updated.url) updated else it
                    }
                    if (!updated.isActive && currentChannelUrl == updated.url) {
                        stopPlayback()
                        errorMessage = "Contenido no disponible temporalmente"
                    }
                }
                is PostgresAction.Insert -> {
                    val newChannel = action.decodeRecord<Channel>()
                    if (allChannels.none { it.id == newChannel.id }) {
                        allChannels = (allChannels + newChannel).sortedBy { it.order }
                    }
                }
                is PostgresAction.Delete -> {
                    val deletedId = action.oldRecord["id"]?.toString()?.replace("\"", "")
                    allChannels = allChannels.filter { it.id != deletedId }
                }
                else -> {}
            }
            filterChannels()
            loadFavorites()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun syncTimeWithNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            var synced = false
            var attempts = 0
            while (!synced && attempts < 5) {
                try {
                    val connection = URL("http://www.google.com").openConnection()
                    connection.connectTimeout = 5000
                    val dateHeader = connection.getHeaderField("Date")
                    if (dateHeader != null) {
                        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                        val networkDate = sdf.parse(dateHeader)
                        if (networkDate != null) {
                            networkOffset = networkDate.time - SystemClock.elapsedRealtime()
                            isTimeSynced = true
                            synced = true
                        }
                    }
                } catch (e: Exception) {
                    attempts++
                    Log.e("Onyx", "Time sync attempt $attempts failed: ${e.message}")
                    delay(3000)
                }
            }
        }
    }

    private fun getRealTime(): Date {
        return if (isTimeSynced) Date(SystemClock.elapsedRealtime() + networkOffset) else Date()
    }

    private fun handlePlaybackError() {
        if (currentChannelUrl.isEmpty() || isLoading) return
        isLoading = false
        if (retryCount < MAX_RETRIES) {
            retryCount++
            errorMessage = "Reconectando contenido ($retryCount/$MAX_RETRIES)..."
            viewModelScope.launch { delay(2000); playVideo(currentChannelUrl, resetRetry = false) }
        } else {
            errorMessage = "Contenido no disponible temporalmente"
        }
    }

    fun togglePause() {
        if (isPlaying) playerManager.pause()
        else if (currentChannelUrl.isNotEmpty()) playVideo(currentChannelUrl, resetRetry = false)
    }

    fun playVideo(url: String, resetRetry: Boolean = true) {
        if (url.isEmpty()) return
        // BLOQUEO: Si hay una alerta de actualización, no permitir reproducir
        if (appUpdateConfig != null) {
            Log.d("OnyxUpdate", "Reproducción bloqueada por actualización pendiente")
            return
        }

        if (url == "onyx://login") {
            isFromPromoChannel = true
            onShowLoginRequested?.invoke()
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            playerManager.initLibVLC()

            if (resetRetry) {
                retryCount = 0
                errorMessage = null
                if (isUserAuthenticated && !isAdmin) checkAuthorization()
            }

            currentChannelUrl = url
            isLoading = true
            playerManager.play(url, ChannelsConfig.PC_USER_AGENT)

            if (resetRetry) {
                prefs.edit { putString("last_channel_url", url) }
            }
        }
    }

    fun stopPlayback() {
        playerManager.stop()
    }

    fun clearAuthError() { authError = null }

    fun signIn(userInput: String, pass: String) {
        if (userInput.isEmpty() || pass.isEmpty()) { authError = "Campos vacíos"; return }
        isAuthLoading = true
        val finalEmail = if (userInput.contains("@")) userInput.trim().lowercase() else "${userInput.trim().lowercase().removeSuffix("@onyxtv.app")}@onyxtv.app"

        viewModelScope.launch {
            try {
                supabase.auth.signInWith(Email) {
                    email = finalEmail
                    password = pass
                }
            } catch (e: Exception) {
                authError = "Error: ${e.localizedMessage}"
            } finally {
                isAuthLoading = false
            }
        }
    }

    fun updateOwnPassword(newPassword: String) {
        viewModelScope.launch {
            try {
                supabase.auth.updateUser {
                    password = newPassword
                }
            } catch (e: Exception) {
                Log.e("Onyx", "Error updating password", e)
                authError = "Error al actualizar contraseña: ${e.localizedMessage}"
            }
        }
    }

    fun relinkDevice() {
        val user = supabase.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                // Forzar uso de cadena exacta "deviceId" para evitar errores PGRST204
                supabase.postgrest["users"].update({ set("deviceId", deviceId) }) {
                    filter { eq("id", user.id) }
                }
                checkAuthorization()
            } catch (e: Exception) {
                Log.e("Onyx", "Error relinking device", e)
            }
        }
    }

    fun refreshAccess() {
        isLoading = true
        checkAuthorization()
    }

    private fun saveUserToCache(role: String, expiry: String?) {
        prefs.edit { putBoolean("user_active", true); putString("user_role", role); putString("user_expiry", expiry) }
    }

    private fun loadUserFromCache() {
        val role = prefs.getString("user_role", "USER") ?: "USER"
        isAdmin = role.uppercase() == "ADMIN"
        val expiryStr = prefs.getString("user_expiry", null)
        if (expiryStr != null) {
            try {
                val cleanDate = if (expiryStr.contains("T")) expiryStr.substringBefore("T") else expiryStr
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                userExpiryDate = sdf.parse(cleanDate)
            } catch (_: Exception) {}
        }
    }

    private fun checkAuthorization() {
        val user = supabase.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                var sync = 0; while (!isTimeSynced && sync < 40) { delay(100); sync++ }
                
                val profile = try {
                    supabase.postgrest["users"].select {
                        filter { eq("id", user.id) }
                    }.decodeSingleOrNull<UserProfile>()
                } catch (e: Exception) {
                    Log.e("Onyx", "Error fetching from users table: ${e.message}")
                    if (e.message?.contains("JWT expired", ignoreCase = true) == true) {
                        try { supabase.auth.refreshCurrentSession() } catch(_: Exception) {}
                    }
                    null
                }

                if (profile != null) {
                    isAdmin = profile.role.uppercase() == "ADMIN"
                    if (profile.expiryDate != null) {
                        try {
                            val cleanDate = if (profile.expiryDate.contains("T")) profile.expiryDate.substringBefore("T") else profile.expiryDate
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            userExpiryDate = sdf.parse(cleanDate)
                        } catch (_: Exception) {
                            Log.e("Onyx", "Date parse error: ${profile.expiryDate}")
                        }
                    } else {
                        userExpiryDate = null
                    }

                    if (isAdmin) {
                        isUserAuthorized = true; isExpired = false
                        saveUserToCache(profile.role, profile.expiryDate)
                        observeChannels()
                        observeMovies()
                        isLoading = false
                        return@launch
                    }

                    if (!profile.isActive) {
                        isUserAuthorized = false
                        accountStatusMessage = "Cuenta desactivada. Contacta al Soporte."
                        stopPlayback()
                        isLoading = false
                        return@launch
                    }

                    if (profile.expiryDate == null || SubscriptionService.isAccountExpired(profile.expiryDate, getRealTime())) {
                        isExpired = true
                        isUserAuthorized = false 
                        accountStatusMessage = "Suscripción expirada o sin fecha. Contacta al dueño para renovar."
                        stopPlayback()
                        isLoading = false
                        return@launch
                    }

                    val authResult = SubscriptionService.checkDeviceAuthorization(deviceId, profile)
                    when (authResult) {
                        is SubscriptionService.DeviceAuthResult.Authorized -> {
                            isUserAuthorized = true
                            isExpired = false
                            saveUserToCache(profile.role, profile.expiryDate)
                            observeChannels()
                            observeMovies()
                            isLoading = false
                        }
                        is SubscriptionService.DeviceAuthResult.LinkToSlot1 -> {
                            supabase.postgrest["users"].update({ set("deviceId", deviceId) }) {
                                filter { eq("id", user.id) }
                            }
                            delay(600)
                            checkAuthorization()
                        }
                        is SubscriptionService.DeviceAuthResult.LinkToSlot2 -> {
                            supabase.postgrest["users"].update({ set("deviceId2", deviceId) }) {
                                filter { eq("id", user.id) }
                            }
                            delay(600)
                            checkAuthorization()
                        }
                        is SubscriptionService.DeviceAuthResult.NotAuthorized -> {
                            isUserAuthorized = false
                            accountStatusMessage = authResult.message
                            stopPlayback()
                            isLoading = false
                        }
                    }
                } else {
                    isUserAuthorized = false
                    accountStatusMessage = "Error: Usuario no encontrado en tabla 'users'."
                    stopPlayback()
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("Onyx", "Critical error in checkAuthorization", e)
                isUserAuthorized = false
                accountStatusMessage = "Error de conexión."
                isLoading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    try {
                        supabase.postgrest["users"].update({
                            set("deviceId", null as String?)
                        }) {
                            filter { eq("id", user.id) }
                        }
                    } catch (e: Exception) {
                        Log.e("Onyx", "Error clearing deviceId on logout", e)
                    }
                }
                supabase.auth.signOut()
            } catch (e: Exception) {
                Log.e("Onyx", "Logout error", e)
            } finally {
                isUserAuthenticated = false; isUserAuthorized = true; isAdmin = false; isExpired = false; userExpiryDate = null
                accountStatusMessage = null
                prefs.edit { remove("user_active"); remove("user_role"); remove("user_expiry") }
                observeChannels()
                observeMovies()
            }
        }
    }

    private fun observeChannels() {
        isLoading = true
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 3) {
                try {
                    val channels = supabase.postgrest["channels"]
                        .select {
                            order("order", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                        }.decodeList<Channel>()

                    allChannels = channels
                    filterChannels()
                    onChannelsLoaded()
                    break
                } catch (e: Exception) {
                    attempts++
                    Log.e("Onyx", "Error observing channels (attempt $attempts)", e)
                    if (e.message?.contains("JWT expired", ignoreCase = true) == true) {
                        try { supabase.auth.refreshCurrentSession() } catch(_: Exception) {}
                    }
                    if (attempts < 3) delay(2000)
                } finally {
                    if (attempts >= 3) isLoading = false
                }
            }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            try {
                val movies = supabase.postgrest["movies"]
                    .select {
                        filter { eq("is_active", true) }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }.decodeList<Movie>()
                allMovies = movies
                filterMovies()
            } catch (e: Exception) {
                Log.e("Onyx", "Error observing movies", e)
            }
        }
    }

    private fun onChannelsLoaded() {
        loadFavorites()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            val active = allChannels.filter { it.isActive }
            if (active.isNotEmpty()) {
                val toPlay = if (savedUrl.isNotEmpty() && active.any { it.url == savedUrl }) savedUrl else active[0].url
                playVideo(toPlay)
            }
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) {
        searchQuery = q
        filterChannels()
        filterMovies()
    }

    private fun filterChannels() {
        val baseList = when {
            selectedGroup == "FAVORITOS" -> favorites.toList()
            else -> allChannels.filter { it.isActive && (selectedGroup == "TODOS" || it.group == selectedGroup) }
        }

        val showRestrictedMode = !isAdmin && (!isUserAuthenticated || isExpired)

        if (showRestrictedMode) {
            val authNames = listOf("LAS ESTRELLAS HD", "CINECANAL", "COMEDY CENTRAL", "AZTECA UNO", "H2")
            val restricted = baseList.filter { c -> authNames.any { a -> c.name.trim().equals(a.trim(), ignoreCase = true) } }
            val promo = Channel("INICIA SESIÓN PARA DISFRUTAR +60 CANALES", "onyx://login", null, "PROMO", true)
            val combined = (listOf(promo) + restricted)
            filteredChannels = if (searchQuery.isEmpty()) combined else combined.filter { it.name.contains(searchQuery, true) || it.group?.contains(searchQuery, true) == true }
        } else {
            filteredChannels = if (searchQuery.isEmpty()) baseList else baseList.filter { it.name.contains(searchQuery, true) || it.group?.contains(searchQuery, true) == true }
        }
    }

    private fun filterMovies() {
        filteredMovies = if (searchQuery.isEmpty()) {
            allMovies
        } else {
            allMovies.filter { it.title.contains(searchQuery, true) || it.description?.contains(searchQuery, true) == true }
        }
    }

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

    // --- ACTUALIZACIONES OTA ---

    private fun checkForUpdates() {
        viewModelScope.launch {
            try {
                // Sincronización con la Tabla app_config: Obtenemos siempre la primera fila
                val config = supabase.postgrest["app_config"].select {
                    limit(1)
                    order("id", Order.ASCENDING)
                }.decodeSingleOrNull<AppConfig>()

                if (config != null) {
                    val currentVersionCode = try {
                        val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
                    } catch (e: Exception) { 0L }

                    if (config.versionCode > currentVersionCode) {
                        Log.d("OnyxUpdate", "Nueva versión detectada: ${config.versionName}")
                        appUpdateConfig = config
                        // DETENER REPRODUCCIÓN: Si hay una actualización, parar el video actual
                        stopPlayback()
                    }
                }
            } catch (e: Exception) {
                Log.e("OnyxUpdate", "Error checking for updates", e)
            }
        }
    }

    fun dismissUpdate() {
        appUpdateConfig = null
    }

    fun downloadAndInstallUpdate() {
        val config = appUpdateConfig ?: return
        if (config.downloadUrl.isBlank()) {
            Log.e("OnyxUpdate", "Error: URL de descarga vacía")
            errorMessage = "URL de descarga no disponible"
            return
        }

        Log.d("OnyxUpdate", "Iniciando proceso para: ${config.downloadUrl}")
        isDownloadingUpdate = true
        val context = getApplication<Application>()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        // Usamos el directorio público de descargas para evitar restricciones de MediaProvider con DownloadManager
        val updateFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "onyxtv_update.apk")
        if (updateFile.exists()) {
            Log.d("OnyxUpdate", "Borrando APK vieja")
            updateFile.delete()
        }

        try {
            val uri = Uri.parse(config.downloadUrl.trim())
            val request = DownloadManager.Request(uri)
                .setTitle("Onyx TV Update")
                .setDescription("Actualizando a ${config.versionName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "onyxtv_update.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadId = downloadManager.enqueue(request)
            Log.d("OnyxUpdate", "Download ID: $downloadId")

            viewModelScope.launch(Dispatchers.IO) {
                var downloaded = false
                while (!downloaded) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusCol != -1) {
                            val status = cursor.getInt(statusCol)
                            
                            // Progreso
                            val bytesCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            if (bytesCol != -1 && totalCol != -1) {
                                val bytesDownloaded = cursor.getLong(bytesCol)
                                val bytesTotal = cursor.getLong(totalCol)
                                if (bytesTotal > 0) {
                                    downloadProgress = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                                }
                            }

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloaded = true
                                withContext(Dispatchers.Main) {
                                    isDownloadingUpdate = false
                                    installApk(context)
                                }
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                downloaded = true
                                withContext(Dispatchers.Main) {
                                    isDownloadingUpdate = false
                                    errorMessage = "Error en la descarga. Verifica la URL."
                                }
                            }
                        }
                    } else {
                        downloaded = true
                        withContext(Dispatchers.Main) { isDownloadingUpdate = false }
                    }
                    cursor?.close()
                    delay(800)
                }
            }
        } catch (e: Exception) {
            Log.e("OnyxUpdate", "Error crítico", e)
            isDownloadingUpdate = false
            errorMessage = "No se pudo iniciar descarga."
        }
    }

    private fun installApk(context: Context) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "onyxtv_update.apk")
        if (file.exists()) {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("OnyxUpdate", "Error instalador", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.releasePlayer()
    }
}
