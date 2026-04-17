package com.example.onyxapp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import kotlinx.coroutines.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class PlayerManager(
    private val context: Context,
    private val onEvent: (MediaPlayer.Event) -> Unit
) {
    private var _libVlc: LibVLC? = null
    val libVlc get() = _libVlc

    var mediaPlayer by mutableStateOf<MediaPlayer?>(null)
        private set

    private val playerJob = SupervisorJob()
    private val playerScope = CoroutineScope(Dispatchers.Main + playerJob)
    private var currentPlayJob: Job? = null

    fun initLibVLC() {
        if (_libVlc != null && mediaPlayer != null && !_libVlc!!.isReleased && !mediaPlayer!!.isReleased) {
            return
        }

        // Release existing resources before re-initializing
        releasePlayerResources()

        try {
            val args = arrayListOf(
                "-vv",
                "--network-caching=3000",
                "--live-caching=3000",
                "--http-reconnect",
                "--rtsp-tcp",
                "--drop-late-frames",
                "--skip-frames",
                "--no-stats",
                "--no-osd",
                "--ipv4"
            )

            _libVlc = LibVLC(context, args)
            val mp = MediaPlayer(_libVlc)
            mp.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
            mp.setEventListener { event -> onEvent(event) }
            mediaPlayer = mp

        } catch (e: Exception) {
            Log.e("Onyx", "Fatal error creating VLC", e)
        }
    }

    private fun releasePlayerResources() {
        try {
            mediaPlayer?.let {
                if (!it.isReleased) {
                    it.stop()
                    it.detachViews()
                    it.release()
                }
            }
            _libVlc?.let { if (!it.isReleased) it.release() }
        } catch (e: Exception) {
            Log.e("Onyx", "Error releasing VLC resources", e)
        }
        mediaPlayer = null
        _libVlc = null
    }

    fun play(url: String, userAgent: String? = null, hwAcceleration: Boolean = true) {
        currentPlayJob?.cancel()

        currentPlayJob = playerScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    initLibVLC()
                }

                val safePlayer = mediaPlayer ?: return@launch
                val safeLibVlc = _libVlc ?: return@launch

                if (safePlayer.isPlaying) {
                    safePlayer.stop()
                }

                val media = Media(safeLibVlc, url.toUri())
                try {
                    // Si hwAcceleration es false, forzamos decodificación por CPU
                    media.setHWDecoderEnabled(hwAcceleration, false)
                    
                    if (!hwAcceleration) {
                        // Opciones adicionales para mejorar el rendimiento en modo CPU
                        media.addOption(":codec=avcodec,all")
                        media.addOption(":avcodec-hw=none")
                    }

                    val finalUA = userAgent ?: "IPTVSmartersPlayer"
                    media.addOption(":http-user-agent=$finalUA")
                    media.addOption(":network-caching=3000")
                    media.addOption(":live-caching=3000")
                    media.addOption(":http-reconnect=true")
                    media.addOption(":no-ssl-verify")
                    media.addOption(":clock-jitter=0")

                    withContext(Dispatchers.Main) {
                        if (!safePlayer.isReleased) {
                            safePlayer.media = media
                            safePlayer.play()
                        }
                    }
                } finally {
                    media.release()
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("Onyx", "Error en corrutina de reproducción", e)
                }
            }
        }
    }

    fun stop() {
        currentPlayJob?.cancel()
        val mp = mediaPlayer
        if (mp != null && !mp.isReleased) {
            mp.stop()
        }
    }

    fun pause() {
        val mp = mediaPlayer
        if (mp != null && !mp.isReleased) {
            mp.pause()
        }
    }

    fun resume() {
        val mp = mediaPlayer
        if (mp != null && !mp.isReleased) {
            mp.play()
        }
    }

    fun seekTo(time: Long) {
        val mp = mediaPlayer
        if (mp != null && !mp.isReleased) {
            mp.time = time
        }
    }

    fun releasePlayer() {
        currentPlayJob?.cancel()
        playerJob.cancelChildren()
        releasePlayerResources()
    }
}
