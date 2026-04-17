package com.example.onyxapp

import android.content.Context
import android.util.Log
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

    private var _mediaPlayer: MediaPlayer? = null
    val mediaPlayer get() = _mediaPlayer

    // MEJORA 1: Corrutinas en lugar de Threads.
    // Permite cancelar reproducciones si el usuario hace "zapping" muy rápido.
    private val playerJob = SupervisorJob()
    private val playerScope = CoroutineScope(Dispatchers.Main + playerJob)
    private var currentPlayJob: Job? = null

    fun initLibVLC() {
        if (_libVlc != null && _mediaPlayer != null && !_libVlc!!.isReleased && !_mediaPlayer!!.isReleased) {
            return
        }

        try {
            // MEJORA 2: Argumentos anti-congelamiento para TV Boxes de gama baja
            val args = arrayListOf(
                "-vv",
                "--network-caching=3000", // Aumentado para absorber micro-cortes de WiFi
                "--live-caching=3000",
                "--http-reconnect",
                "--rtsp-tcp",
                "--drop-late-frames", // Crucial: Si la TV se traba, tira el cuadro en lugar de congelar la app
                "--skip-frames",      // Crucial: Alivia el procesador
                "--no-stats",
                "--no-osd",
                "--ipv4",
                "--vout=android-display",
                "--codec=mediacodec,iomx,all", // Fuerza el uso del decodificador por hardware (GPU)
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
        // Cancelamos cualquier intento de reproducción anterior inmediatamente
        currentPlayJob?.cancel()

        currentPlayJob = playerScope.launch(Dispatchers.IO) {
            try {
                // Asegurarnos de que LibVLC se inicializa en el hilo principal
                withContext(Dispatchers.Main) {
                    initLibVLC()
                }

                val safePlayer = _mediaPlayer ?: return@launch
                val safeLibVlc = _libVlc ?: return@launch

                if (safePlayer.isPlaying) {
                    safePlayer.stop()
                }

                val media = Media(safeLibVlc, url.toUri())

                // MEJORA 3: Aceleración por Hardware Obligatoria
                // Esto pasa el trabajo del CPU a la Tarjeta Gráfica de la TV Box. Hace que el video vuele.
                media.setHWDecoderEnabled(true, false)

                val finalUA = userAgent ?: "IPTVSmartersPlayer"

                media.addOption(":http-user-agent=$finalUA")
                media.addOption(":network-caching=3000")
                media.addOption(":live-caching=3000")
                media.addOption(":http-reconnect=true")
                media.addOption(":no-ssl-verify")
                media.addOption(":clock-jitter=0")

                // Asegurar que la asignación al reproductor se hace en el hilo principal
                withContext(Dispatchers.Main) {
                    safePlayer.media = media
                    media.release() // Importante liberar el objeto Media de la memoria nativa
                    safePlayer.play()
                }

                Log.d("OnyxPlayer", "Contenido iniciado: $url")
            } catch (e: Exception) {
                Log.e("Onyx", "Error en corrutina de reproducción", e)
            }
        }
    }

    fun stop() {
        currentPlayJob?.cancel()
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
        // MEJORA 4: Destrucción total. Evita que la app se quede en segundo plano consumiendo RAM.
        try {
            currentPlayJob?.cancel()
            playerJob.cancelChildren()

            _mediaPlayer?.let {
                if (!it.isReleased) {
                    it.setEventListener(null) // Quitar el listener evita fugas de memoria (Memory Leaks)
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