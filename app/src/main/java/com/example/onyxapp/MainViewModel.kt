package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
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
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var currentTime by mutableStateOf("")
        private set

    var externalM3uUrl by mutableStateOf(prefs.getString("external_m3u_url", "") ?: "")
        private set

    var isUserAuthenticated by mutableStateOf(auth.currentUser != null)
        private set
    var isUserAuthorized by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var accountStatusMessage by mutableStateOf<String?>(null)
        private set
    
    var userExpiryDate by mutableStateOf<Date?>(null)
        private set

    val currentUsername: String
        get() = auth.currentUser?.email?.replace("@onyxtv.app", "") ?: "Invitado"

    init {
        initLibVLC()
        startClock()
        if (isUserAuthenticated) checkAuthorization() else isLoading = false
        uploadLocalChannelsToFirestore()
    }

    private fun initLibVLC() {
        try {
            val args = arrayListOf("-vvv", "--http-user-agent=${ChannelsConfig.PC_USER_AGENT}", "--http-referrer=https://www.google.com/", "--network-caching=3000", "--rtsp-tcp")
            libVlc = LibVLC(getApplication(), args)
            mediaPlayer = MediaPlayer(libVlc)
            mediaPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Buffering -> { isLoading = event.buffering < 100f }
                    MediaPlayer.Event.Playing -> { isLoading = false; errorMessage = null }
                    MediaPlayer.Event.EncounteredError -> { isLoading = false; errorMessage = "Error en el canal" }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

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
        userExpiryDate = null
        stopPlayback()
    }

    private fun observeChannels() {
        db.collection("channels").orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allChannels = snapshot.documents.mapNotNull { doc ->
                        Channel(doc.getString("name") ?: "", doc.getString("url") ?: "", doc.getString("logo"), doc.getString("group"))
                    }
                    onChannelsLoaded()
                }
            }
    }

    fun updateExternalM3U(url: String) {
        externalM3uUrl = url
        prefs.edit().putString("external_m3u_url", url).apply()
        loadExternalChannels(url)
    }

    private fun loadExternalChannels(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val channels = ChannelsConfig.parseM3U(url)
                withContext(Dispatchers.Main) { allChannels = channels; onChannelsLoaded() }
            } catch (e: Exception) {}
        }
    }

    fun uploadLocalChannelsToFirestore() {
        val channels = ChannelsConfig.parseM3U(ChannelsConfig.M3U_SOURCE)
        val collectionRef = db.collection("channels")
        viewModelScope.launch(Dispatchers.IO) {
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
                    val batch = db.batch()
                    chunk.forEachIndexed { i, c ->
                        val d = hashMapOf("name" to c.name, "url" to c.url, "logo" to c.logo, "group" to c.group, "order" to i)
                        batch.set(collectionRef.document(), d)
                    }
                    Tasks.await(batch.commit())
                }
            } catch (e: Exception) {}
        }
    }

    private fun loadLocalChannels() {
        viewModelScope.launch {
            val source = if (externalM3uUrl.isNotEmpty()) externalM3uUrl else ChannelsConfig.M3U_SOURCE
            allChannels = withContext(Dispatchers.IO) { ChannelsConfig.parseM3U(source) }
            onChannelsLoaded()
        }
    }

    private fun onChannelsLoaded() {
        filterChannels()
        loadFavorites()
        if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
            val savedUrl = prefs.getString("last_channel_url", "") ?: ""
            playVideo(if (savedUrl.isNotEmpty() && allChannels.any { it.url == savedUrl }) savedUrl else allChannels[0].url)
        }
        isLoading = false
    }

    fun updateSearchQuery(q: String) { searchQuery = q; filterChannels() }

    private fun filterChannels() {
        filteredChannels = if (searchQuery.isEmpty()) allChannels else allChannels.filter { it.name.contains(searchQuery, true) }
    }

    fun playVideo(url: String) {
        if (url.isEmpty()) return
        currentChannelUrl = url
        isLoading = true
        try {
            mediaPlayer?.stop()
            val media = Media(libVlc, Uri.parse(url))
            media.addOption(":http-user-agent=${ChannelsConfig.PC_USER_AGENT}")
            media.addOption(":network-caching=3000")
            mediaPlayer?.media = media
            media.release()
            mediaPlayer?.play()
            prefs.edit().putString("last_channel_url", url).apply()
        } catch (e: Exception) { isLoading = false }
    }

    fun stopPlayback() { mediaPlayer?.stop() }

    fun toggleFavorite(c: Channel) {
        if (favorites.any { it.url == c.url }) favorites.removeAll { it.url == c.url } else favorites.add(c)
        prefs.edit().putStringSet("favorites_urls", favorites.map { it.url }.toSet()).apply()
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
                // Formato 24h para Mazatlán (GMT-7)
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
