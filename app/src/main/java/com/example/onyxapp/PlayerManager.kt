package com.example.onyxapp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.net.toUri
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import kotlin.concurrent.thread

class PlayerManager(
    private val context: Context,
    private val onEvent: (MediaPlayer.Event) -> Unit
) {
    private var _libVlc: LibVLC? = null
    val libVlc get() = _libVlc

    private var _mediaPlayer: MediaPlayer? = null
    val mediaPlayer get() = _mediaPlayer

    fun initLibVLC() {
        if (_libVlc != null && _mediaPlayer != null && !_libVlc!!.isReleased && !_mediaPlayer!!.isReleased) {
            return
        }

        try {
            val args = arrayListOf(
                "-vv",
                "--network-caching=1500",
                "--http-reconnect",
                "--rtsp-tcp",
                "--no-stats",
                "--no-osd",
                "--ipv4"
            )

            _libVlc = LibVLC(context, args)
            _mediaPlayer = MediaPlayer(_libVlc)
            _mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
            _mediaPlayer?.setEventListener { event -> onEvent(event) }

        } catch (e: Exception) {
            Log.e("Onyx", "Fatal error creating VLC", e)
        }
    }

    fun play(url: String, userAgent: String? = null) {
        initLibVLC()
        val safePlayer = _mediaPlayer ?: return
        val safeLibVlc = _libVlc ?: return

        // Usamos un hilo dedicado para no trabar la interfaz de la app (ANRs)
        thread(start = true) {
            try {
                if (safePlayer.isPlaying) {
                    safePlayer.stop()
                }

                val media = Media(safeLibVlc, url.toUri())

                val isHttp = url.startsWith("http://")
                val isProblematic = url.contains("201.217.246.42")
                
                // Identidad optimizada para IPTV
                val finalUA = when {
                    isHttp || isProblematic -> "IPTVSmartersPlayer"
                    userAgent != null -> userAgent
                    else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                }

                media.addOption(":http-user-agent=$finalUA")

                // Opciones de red optimizadas
                media.addOption(":network-caching=2000") // 2 segundos para estabilidad en HTTP
                media.addOption(":http-reconnect=true")
                media.addOption(":no-ssl-verify")
                
                // SOLUCIÓN PARA CANALES HTTP QUE SE TRABAN (Error 206)
                if (isHttp) {
                    media.addOption(":no-http-range") // Evita peticiones por trozos que rompen el servidor
                    media.addOption(":http-forward-cookies=1")
                    media.addOption(":adaptive-logic=lowest") // Inicio ultra-rápido
                }

                if (isProblematic) {
                    media.addOption(":clock-jitter=0")
                    media.addOption(":clock-synchro=0")
                }

                safePlayer.media = media
                media.release()
                safePlayer.play()
                
                Log.d("OnyxPlayer", "Canal iniciado en hilo asíncrono: $url")
            } catch (e: Exception) {
                Log.e("Onyx", "Error en hilo de reproducción", e)
            }
        }
    }

    fun stop() {
        _mediaPlayer?.stop()
    }

    fun pause() {
        _mediaPlayer?.pause()
    }

    fun releasePlayer() {
        try {
            _mediaPlayer?.let {
                if (!it.isReleased) {
                    it.stop()
                    it.release()
                }
            }
            _mediaPlayer = null

            _libVlc?.let {
                if (!it.isReleased) {
                    it.release()
                }
            }
            _libVlc = null
        } catch (e: Exception) {
            Log.e("Onyx", "Error liberando VLC", e)
        }
    }
}