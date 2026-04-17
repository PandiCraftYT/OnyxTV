package com.example.onyxapp

import android.content.Context
import android.util.Log
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
                "--ipv4",
                "--video-title-show",
                "--vout=android-display",
                "--audio-resampler=soxr"
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

        thread(start = true) {
            try {
                if (safePlayer.isPlaying) {
                    safePlayer.stop()
                }

                val media = Media(safeLibVlc, url.toUri())
                val finalUA = userAgent ?: "IPTVSmartersPlayer"
                
                media.addOption(":http-user-agent=$finalUA")
                media.addOption(":network-caching=2000")
                media.addOption(":http-reconnect=true")
                media.addOption(":no-ssl-verify")
                media.addOption(":clock-jitter=0")

                safePlayer.media = media
                media.release()
                safePlayer.play()
                
                Log.d("OnyxPlayer", "Contenido iniciado: $url")
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

    fun resume() {
        _mediaPlayer?.play()
    }

    fun seekTo(time: Long) {
        _mediaPlayer?.time = time
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