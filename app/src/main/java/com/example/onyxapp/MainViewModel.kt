package com.example.onyxapp

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE)

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

    init {
        initLibVLC()
        loadChannels()
        startClock()
    }

    private fun initLibVLC() {
        try {
            val args = arrayListOf(
                "-vvv",
                "--http-user-agent=${ChannelsConfig.PC_USER_AGENT}",
                "--http-referrer=https://www.google.com/",
                "--network-caching=3000",
                "--rtsp-tcp"
            )
            libVlc = LibVLC(getApplication(), args)
            mediaPlayer = MediaPlayer(libVlc)

            mediaPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Buffering -> {
                        isLoading = event.buffering < 100f
                    }
                    MediaPlayer.Event.Playing -> {
                        isLoading = false
                        errorMessage = null
                    }
                    MediaPlayer.Event.EncounteredError -> {
                        isLoading = false
                        errorMessage = "El canal no está disponible actualmente"
                    }
                    MediaPlayer.Event.Stopped -> {
                        isLoading = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error al inicializar el reproductor"
        }
    }

    fun loadChannels() {
        viewModelScope.launch {
            isLoading = true
            val channels = mutableListOf<Channel>()
            
            try {
                // 1. Cargar embebidos
                channels.addAll(ChannelsConfig.parseM3U(ChannelsConfig.M3U_SOURCE))
                
                // 2. Cargar externos si existen
                if (externalM3uUrl.isNotEmpty()) {
                    try {
                        val externalContent = withContext(Dispatchers.IO) {
                            val connection = URL(externalM3uUrl).openConnection() as HttpURLConnection
                            connection.setRequestProperty("User-Agent", ChannelsConfig.PC_USER_AGENT)
                            connection.connectTimeout = 10000
                            connection.readTimeout = 10000
                            connection.inputStream.bufferedReader().use { it.readText() }
                        }
                        if (externalContent.isNotEmpty()) {
                            channels.addAll(ChannelsConfig.parseM3U(externalContent))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al cargar la lista de canales"
            }
            
            allChannels = channels
            filterChannels()
            loadFavorites()
            
            // Auto-play con protección contra crashes
            if (currentChannelUrl.isEmpty() && allChannels.isNotEmpty()) {
                val savedUrl = prefs.getString("last_channel_url", "") ?: ""
                try {
                    if (savedUrl.isNotEmpty() && allChannels.any { it.url == savedUrl }) {
                        playVideo(savedUrl)
                    } else {
                        allChannels.firstOrNull()?.url?.let { playVideo(it) }
                    }
                } catch (e: Exception) {
                    // Si el auto-play falla, limpiamos la preferencia para evitar bucles de crash
                    prefs.edit().remove("last_channel_url").apply()
                    allChannels.firstOrNull()?.url?.let { playVideo(it) }
                }
            }
            isLoading = false
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterChannels()
    }

    private fun filterChannels() {
        filteredChannels = if (searchQuery.isEmpty()) {
            allChannels
        } else {
            allChannels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    fun playVideo(url: String) {
        if (url.isEmpty()) return
        
        currentChannelUrl = url
        errorMessage = null
        isLoading = true
        
        try {
            mediaPlayer?.stop()
            val vlc = libVlc ?: return
            
            val media = Media(vlc, Uri.parse(url))
            media.addOption(":http-user-agent=${ChannelsConfig.PC_USER_AGENT}")
            media.addOption(":http-referrer=https://www.google.com/")
            media.addOption(":network-caching=3000")
            
            mediaPlayer?.media = media
            media.release()
            mediaPlayer?.play()
            
            // Solo guardamos en prefs si logramos llegar al play sin excepciones
            prefs.edit().putString("last_channel_url", url).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error al reproducir este canal"
            isLoading = false
            // Si falla, borramos la marca de "último canal" para que no intente abrirlo al reiniciar
            prefs.edit().remove("last_channel_url").apply()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleFavorite(channel: Channel) {
        if (favorites.any { it.url == channel.url }) {
            favorites.removeAll { it.url == channel.url }
        } else {
            favorites.add(channel)
        }
        saveFavorites()
    }

    private fun saveFavorites() {
        val favoriteUrls = favorites.map { it.url }.toSet()
        prefs.edit().putStringSet("favorites_urls", favoriteUrls).apply()
    }

    private fun loadFavorites() {
        val favoriteUrls = prefs.getStringSet("favorites_urls", emptySet()) ?: emptySet()
        favorites.clear()
        favorites.addAll(allChannels.filter { it.url in favoriteUrls })
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                delay(1000)
            }
        }
    }

    fun zapNext() {
        val list = allChannels
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.url == currentChannelUrl }
        val nextIndex = if (currentIndex == -1 || currentIndex == list.size - 1) 0 else currentIndex + 1
        playVideo(list[nextIndex].url)
    }

    fun zapPrevious() {
        val list = allChannels
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.url == currentChannelUrl }
        val prevIndex = if (currentIndex == -1 || currentIndex == 0) list.size - 1 else currentIndex - 1
        playVideo(list[prevIndex].url)
    }

    fun updateExternalM3U(url: String) {
        externalM3uUrl = url
        prefs.edit().putString("external_m3u_url", url).apply()
        loadChannels()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            libVlc?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
