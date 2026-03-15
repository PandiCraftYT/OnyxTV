package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    // Nueva variable para el aspecto dinámico del video
    var videoAspectRatio by mutableStateOf(16f / 9f)
        private set

    val availableGroups: List<String>
        get() = listOf("TODOS", "FAVORITOS") + allChannels.mapNotNull { it.group }.distinct().sorted()

    val currentUsername: String
        get() = auth.currentUser?.email?.replace("@onyxtv.app", "") ?: "Invitado"

    // Lista de usuarios para el panel admin
    var allUsers = mutableStateListOf<Map<String, Any>>()
        private set

    init {
        initLibVLC()
        startClock()
        if (isUserAuthenticated) checkAuthorization() else isLoading = false
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
                    if (newAspect in 0.5f..2.5f) { // Validar rangos razonables
                        videoAspectRatio = newAspect
                    }
                }
            }
        }
    }

    private fun handlePlaybackError() {
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

    // --- FUNCIONES DE ADMINISTRADOR (MANUALES POR BOTÓN) ---

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
                        authError = "EXITO: ${newChannels.size} canales actualizados."
                        observeChannels()
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
            .addOnSuccessListener { fetchAllUsers() }
    }

    // --- FIN FUNCIONES ADMIN ---

    fun signIn(username: String, pass: String) {
        if (username.isEmpty() || pass.isEmpty()) { authError = "Completa los campos"; return }
        isLoading = true
        authError = null
        val finalEmail = "${username.trim().lowercase().removeSuffix("@onyxtv.app")}@onyxtv.app"
        auth.signInWithEmailAndPassword(finalEmail, pass)
            .addOnSuccessListener { isUserAuthenticated = true; checkAuthorization() }
            .addOnFailureListener { authError = "Error: ${it.localizedMessage}"; isLoading = false }
    }

    private fun checkAuthorization() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        isLoading = true

        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { accountStatusMessage = "Error DB: ${e.code}"; isLoading = false; return@addSnapshotListener }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data ?: emptyMap()

                    val role = snapshot.getString("role") ?: "USER"
                    isAdmin = role.uppercase() == "ADMIN"

                    val activeKey = data.keys.find { it.equals("isActive", true) }
                    val rawIsActive = if (activeKey != null) data[activeKey] else null

                    val isActive = when(rawIsActive) {
                        is Boolean -> rawIsActive
                        is String -> rawIsActive.trim().lowercase() == "true"
                        else -> false
                    }

                    val expiryDate = snapshot.getTimestamp("expiryDate")?.toDate()
                    userExpiryDate = expiryDate
                    val now = Date()

                    if (!isActive) {
                        isUserAuthorized = false
                        accountStatusMessage = "Cuenta desactivada. Contacta al administrador."
                        stopPlayback()
                    } else if (expiryDate != null && now.after(expiryDate)) {
                        isUserAuthorized = false
                        accountStatusMessage = "Suscripción expirada. Contacta con el administrador: ig carlosnvz__"
                        stopPlayback()
                    } else {
                        isUserAuthorized = true
                        accountStatusMessage = null
                        observeChannels()
                    }
                } else {
                    val newUserData = hashMapOf(
                        "username" to (user.email?.replace("@onyxtv.app", "") ?: "user"),
                        "isActive" to false,
                        "role" to "USER",
                        "expiryDate" to com.google.firebase.Timestamp(Date(System.currentTimeMillis() + 2592000000L)),
                        "uid" to uid
                    )
                    db.collection("users").document(uid).set(newUserData)
                    isUserAuthorized = false
                    accountStatusMessage = "REGISTRADO. Activa el ID:\n$uid"
                }
                isLoading = false
            }
    }

    fun logout() {
        auth.signOut()
        isUserAuthenticated = false
        isUserAuthorized = false
        isAdmin = false
        userExpiryDate = null
        stopPlayback()
    }

    private fun observeChannels() {
        db.collection("channels").orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    allChannels = snapshot.documents.mapNotNull { doc ->
                        Channel(doc.getString("name") ?: "", doc.getString("url") ?: "", doc.getString("logo"), doc.getString("group"))
                    }
                    onChannelsLoaded()
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    private fun onChannelsLoaded() {
        loadFavorites()
        filterChannels()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            playVideo(if (savedUrl.isNotEmpty() && allChannels.any { it.url == savedUrl }) savedUrl else allChannels[0].url)
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) { searchQuery = q; filterChannels() }

    fun updateSelectedGroup(group: String) {
        selectedGroup = group
        filterChannels()
    }

    private fun filterChannels() {
        val baseList = when (selectedGroup) {
            "TODOS" -> allChannels
            "FAVORITOS" -> favorites.toList()
            else -> allChannels.filter { it.group == selectedGroup }
        }

        filteredChannels = if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter { it.name.contains(searchQuery, true) }
        }
    }

    fun playVideo(url: String, resetRetry: Boolean = true) {
        if (url.isEmpty()) return
        if (resetRetry) {
            retryCount = 0
            errorMessage = null
            videoAspectRatio = 16f / 9f // Resetear aspecto al cambiar canal
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    fun stopPlayback() { mediaPlayer?.stop() }

    fun toggleFavorite(c: Channel) {
        if (favorites.any { it.url == c.url }) {
            favorites.removeAll { it.url == c.url }
        } else {
            favorites.add(c)
        }
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
                val now = Date()
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("America/Mazatlan")
                currentTime = sdf.format(now)

                if (isUserAuthorized) {
                    userExpiryDate?.let { expiry ->
                        if (now.after(expiry)) {
                            isUserAuthorized = false
                            accountStatusMessage = "Suscripción expirada. Contacta con el administrador: ig carlosnvz__"
                            stopPlayback()
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
        mediaPlayer?.release()
        libVlc?.release()
    }
}
