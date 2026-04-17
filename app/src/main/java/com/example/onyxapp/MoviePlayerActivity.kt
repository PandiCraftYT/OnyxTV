package com.example.onyxapp

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.ui.components.VideoPlayer
import com.example.onyxapp.ui.theme.OnyxAppTheme
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoviePlayerActivity : ComponentActivity() {

    private lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pantalla completa y encendida (Ideal para Móvil y TV)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val movieUrl = intent.getStringExtra("MOVIE_URL") ?: ""
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: "Película"

        playerManager = PlayerManager(this) { _ -> }
        playerManager.initLibVLC()

        setContent {
            OnyxAppTheme {
                MoviePlayerScreen(
                    playerManager = playerManager,
                    url = movieUrl,
                    title = movieTitle
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.releasePlayer()
    }
}

@Composable
fun MoviePlayerScreen(
    playerManager: PlayerManager,
    url: String,
    title: String
) {
    val context = LocalContext.current as Activity
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var totalTime by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    val mediaPlayer = remember { playerManager.mediaPlayer }
    val playButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        playerManager.play(url, ChannelsConfig.PC_USER_AGENT)
    }

    // Actualización de progreso y estados cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            mediaPlayer?.let {
                isPlaying = it.isPlaying
                currentTime = it.time
                totalTime = it.length
                if (it.isPlaying) isLoading = false
            }
            delay(1000)
        }
    }

    // Auto-ocultar controles si está reproduciendo (y re-enfocar para TV cuando aparecen)
    LaunchedEffect(showControls, isPlaying) {
        if (showControls) {
            try {
                // Intenta dar foco al botón Play para los usuarios de TV
                playButtonFocusRequester.requestFocus()
            } catch (e: Exception) {}

            if (isPlaying) {
                delay(5000) // Se oculta a los 5 segundos (Buen tiempo para touch y control)
                showControls = false
            }
        }
    }

    // Manejar el botón "Atrás" (Back de Android Móvil o Back del Control de TV)
    BackHandler {
        playerManager.releasePlayer()
        context.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // 1. SOPORTE PARA MÓVIL: Detectar toques con los dedos en la pantalla para mostrar/ocultar
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
            // 2. SOPORTE PARA TV: Escuchar botones físicos del control remoto
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    // Si los controles están ocultos, presionar cualquier botón los despierta
                    if (!showControls && event.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_BACK) {
                        showControls = true
                        return@onKeyEvent true
                    }

                    // Accesos directos físicos del control remoto
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            if (isPlaying) playerManager.pause() else playerManager.resume()
                            showControls = true
                            return@onKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            playerManager.seekTo(currentTime + 10000)
                            showControls = true
                            return@onKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            playerManager.seekTo(currentTime - 10000)
                            showControls = true
                            return@onKeyEvent true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (!showControls) {
                                showControls = true
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
            .focusable() // Hacer que el Box principal pueda recibir eventos de TV
    ) {
        // EL REPRODUCTOR
        mediaPlayer?.let { VideoPlayer(it, Modifier.fillMaxSize()) }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF00B4D8)
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Título de la película arriba
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp, // Tamaño balanceado para Móvil y TV
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Panel de controles abajo
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.85f))
                        .padding(horizontal = 30.dp, vertical = 24.dp)
                ) {
                    // El Slider funciona nativamente con toques en Móvil y se le agrega focusable para TV
                    Slider(
                        value = if (totalTime > 0) currentTime.toFloat() / totalTime.toFloat() else 0f,
                        onValueChange = {
                            val seekTo = (it * totalTime).toLong()
                            playerManager.seekTo(seekTo)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00B4D8),
                            activeTrackColor = Color(0xFF00B4D8),
                            inactiveTrackColor = Color.White.copy(0.3f)
                        ),
                        modifier = Modifier.focusable()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatTime(currentTime), color = Color.White, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Botón Universal Retroceder
                            UniversalIconButton(
                                icon = Icons.Default.Replay10,
                                onClick = { playerManager.seekTo(currentTime - 10000) }
                            )

                            Spacer(Modifier.width(20.dp))

                            // Botón Universal Play/Pausa
                            UniversalIconButton(
                                icon = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                iconSize = 64.dp,
                                modifier = Modifier.focusRequester(playButtonFocusRequester),
                                onClick = { if (isPlaying) playerManager.pause() else playerManager.resume() }
                            )

                            Spacer(Modifier.width(20.dp))

                            // Botón Universal Adelantar
                            UniversalIconButton(
                                icon = Icons.Default.Forward10,
                                onClick = { playerManager.seekTo(currentTime + 10000) }
                            )
                        }

                        Text(formatTime(totalTime), color = Color.White.copy(0.7f))
                    }
                }
            }
        }
    }
}

// Componente Híbrido: Reacciona al dedo (tap) y al control de TV (foco)
@Composable
fun UniversalIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 48.dp, // Tamaño amigable para dedos
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            // Color de fondo solo se activa si hay un control de TV enfocándolo
            .background(if (isFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // IconButton nativo maneja perfectamente los toques con los dedos
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(iconSize)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                // Cambia el color para que el usuario de TV sepa dónde está
                tint = if (isFocused) Color(0xFF00B4D8) else Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (h > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}