package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.MediaPlayer
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
            try {
                supabase.realtime.connect()
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

                val moviesChannel = supabase.realtime.channel("public-movies")
                val moviesFlow = moviesChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "movies"
                }
                launch {
                    moviesFlow.collect { observeMovies() }
                }
                moviesChannel.subscribe()

                // --- ESCUCHAR CAMBIOS EN TABLA 'users' ---
                supabase.auth.sessionStatus.collect { status ->
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

            } catch (e: Exception) {
                Log.e("Onyx", "Error conectando Realtime", e)
            }
        }
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
                    }
                }
            } catch (e: Exception) {
                Log.e("Onyx", "Time sync failed: ${e.message}")
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
                    // LIMPIAR deviceId en Supabase al cerrar sesión para liberar el slot
                    // Se usa cast explícito a String? para evitar ambigüedad en set()
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
            try {
                val channels = supabase.postgrest["channels"]
                    .select {
                        order("order", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    }.decodeList<Channel>()

                allChannels = channels
                filterChannels()
                onChannelsLoaded()
            } catch (e: Exception) {
                Log.e("Onyx", "Error observing channels", e)
                if (e.message?.contains("JWT expired", ignoreCase = true) == true) {
                    try { supabase.auth.refreshCurrentSession() } catch(_: Exception) {}
                }
            } finally {
                isLoading = false
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

    override fun onCleared() {
        super.onCleared()
        playerManager.releasePlayer()
    }
}
