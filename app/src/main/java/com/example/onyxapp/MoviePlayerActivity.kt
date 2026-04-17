package com.example.onyxapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
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
    private var movieUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        movieUrl = intent.getStringExtra("MOVIE_URL") ?: ""
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

    override fun onPause() {
        super.onPause()
        // DETENER: Si el usuario sale (Home), liberamos recursos
        playerManager.releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        // RESTAURAR: Al volver a la app, reiniciamos el video si hay URL
        if (movieUrl.isNotEmpty()) {
            playerManager.initLibVLC()
            playerManager.play(movieUrl, ChannelsConfig.PC_USER_AGENT)
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

    val playButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        playerManager.play(url, ChannelsConfig.PC_USER_AGENT)
    }

    // Actualización de progreso y estados cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val mp = playerManager.mediaPlayer
                if (mp != null && !mp.isReleased) {
                    isPlaying = mp.isPlaying
                    currentTime = mp.time
                    totalTime = mp.length
                    if (isPlaying) isLoading = false
                } else {
                    isPlaying = false
                }
            } catch (e: Exception) {
                Log.e("MoviePlayer", "Error in progress loop: ${e.message}")
                isPlaying = false
            }
            delay(1000)
        }
    }

    // Auto-ocultar controles si está reproduciendo
    LaunchedEffect(showControls, isPlaying) {
        if (showControls) {
            try {
                playButtonFocusRequester.requestFocus()
            } catch (e: Exception) {}

            if (isPlaying) {
                delay(5000)
                showControls = false
            }
        }
    }

    // Manejar el botón "Atrás"
    BackHandler {
        playerManager.releasePlayer()
        context.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls = !showControls })
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (!showControls && event.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_BACK) {
                        showControls = true
                        return@onKeyEvent true
                    }
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
            .focusable()
    ) {
        playerManager.mediaPlayer?.let { VideoPlayer(it, Modifier.fillMaxSize()) }

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
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.85f))
                        .padding(horizontal = 30.dp, vertical = 24.dp)
                ) {
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
                            UniversalIconButton(
                                icon = Icons.Default.Replay10,
                                onClick = { playerManager.seekTo(currentTime - 10000) }
                            )
                            Spacer(Modifier.width(20.dp))
                            UniversalIconButton(
                                icon = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                iconSize = 64.dp,
                                modifier = Modifier.focusRequester(playButtonFocusRequester),
                                onClick = { if (isPlaying) playerManager.pause() else playerManager.resume() }
                            )
                            Spacer(Modifier.width(20.dp))
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

@Composable
fun UniversalIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 48.dp,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(iconSize)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
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
