package com.example.onyxapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.*
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*

private const val PC_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

class MainActivity : ComponentActivity() {

    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private val prefs by lazy { getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE) }

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Motor de imágenes con camuflaje total y soporte SVG
        val imageLoader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .build()

        val args = arrayListOf("-vvv", "--http-user-agent=$PC_USER_AGENT", "--http-referrer=https://www.google.com/", "--network-caching=3000", "--rtsp-tcp")
        libVlc = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVlc)

        setContent {
            // Inyectamos el motor de imágenes en toda la jerarquía de la app
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                MaterialTheme {
                    val configuration = LocalConfiguration.current
                    val isWideScreen = configuration.screenWidthDp > 800
                    
                    val allChannels = remember { ChannelsConfig.getChannels() }
                    val favorites = remember { mutableStateListOf<Channel>() }
                    
                    val groupedChannels = remember(allChannels, favorites.size) {
                        val map = allChannels.groupBy { it.group ?: "OTROS" }.toMutableMap()
                        if (favorites.isNotEmpty()) map["FAVORITOS"] = favorites.toList()
                        map
                    }
                    
                    val categories = remember(groupedChannels) { 
                        val keys = groupedChannels.keys.toMutableList().apply { sort() }
                        if (keys.remove("MEXICO")) keys.add(0, "MEXICO")
                        if (keys.remove("FAVORITOS")) keys.add(0, "FAVORITOS")
                        keys
                    }

                    val savedUrl = remember { prefs.getString("last_channel_url", "") ?: "" }
                    var currentChannelUrl by remember { mutableStateOf(if (savedUrl.isNotEmpty() && allChannels.any { it.url == savedUrl }) savedUrl else allChannels.firstOrNull()?.url ?: "") }
                    var selectedCategory by remember { mutableStateOf(allChannels.find { it.url == currentChannelUrl }?.group ?: categories.firstOrNull() ?: "") }
                    var showMenu by remember { mutableStateOf(true) }
                    var isLoading by remember { mutableStateOf(true) }
                    var lastInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    var backPressCount by remember { mutableIntStateOf(0) }
                    var currentTime by remember { mutableStateOf("") }
                    val mainFocusRequester = remember { FocusRequester() }

                    LaunchedEffect(Unit) {
                        while (true) {
                            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            delay(1000)
                        }
                    }

                    LaunchedEffect(showMenu, lastInteractionTrigger) {
                        if (showMenu) { delay(10000); showMenu = false }
                    }

                    LaunchedEffect(showMenu) {
                        if (!showMenu) {
                            mainFocusRequester.requestFocus()
                        }
                    }

                    LaunchedEffect(backPressCount) {
                        if (backPressCount > 0) {
                            delay(2000)
                            backPressCount = 0
                        }
                    }

                    BackHandler {
                        if (showMenu) {
                            showMenu = false
                        } else {
                            if (backPressCount >= 1) {
                                finishAffinity()
                            } else {
                                backPressCount++
                            }
                        }
                    }

                    DisposableEffect(mediaPlayer) {
                        val listener = MediaPlayer.EventListener { event ->
                            when (event.type) {
                                MediaPlayer.Event.Buffering -> isLoading = event.buffering < 100f
                                MediaPlayer.Event.Playing -> isLoading = false
                                else -> isLoading = false
                            }
                        }
                        mediaPlayer?.setEventListener(listener)
                        onDispose { mediaPlayer?.setEventListener(null) }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .focusRequester(mainFocusRequester)
                            .focusable()
                            .onKeyEvent {
                                if (!showMenu && it.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_BACK) {
                                    showMenu = true
                                    lastInteractionTrigger = System.currentTimeMillis()
                                    true
                                } else {
                                    false
                                }
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) { awaitPointerEvent(); showMenu = true; lastInteractionTrigger = System.currentTimeMillis() }
                                }
                            }
                    ) {
                        mediaPlayer?.let { player -> VideoPlayer(player, currentChannelUrl) }

                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00B4D8), strokeWidth = 4.dp)
                            }
                        }

                        AnimatedVisibility(visible = showMenu, enter = slideInHorizontally() + fadeIn(), exit = slideOutHorizontally() + fadeOut()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(if (isWideScreen) 0.45f else 0.65f)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF121212).copy(alpha = 0.98f), Color(0xFF121212).copy(alpha = 0.85f), Color.Transparent)))
                                    .padding(20.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("ONYX TV", style = MaterialTheme.typography.displaySmall, color = Color(0xFF00B4D8))
                                        Text("ig: carlosnvz_", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(currentTime, style = MaterialTheme.typography.titleLarge, color = Color.White)
                                }

                                Row(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(modifier = Modifier.weight(0.35f).fillMaxHeight()) {
                                        items(categories) { cat ->
                                            CategoryItem(cat, selectedCategory == cat) { 
                                                selectedCategory = cat
                                                lastInteractionTrigger = System.currentTimeMillis()
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(15.dp))
                                    LazyColumn(modifier = Modifier.weight(0.65f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        item { Text(selectedCategory, style = MaterialTheme.typography.labelLarge, color = Color(0xFF00B4D8), modifier = Modifier.padding(bottom = 8.dp)) }
                                        items(groupedChannels[selectedCategory] ?: emptyList()) { channel ->
                                            ChannelItem(
                                                channel = channel,
                                                isSelected = currentChannelUrl == channel.url,
                                                isFav = favorites.contains(channel),
                                                onSelect = {
                                                    isLoading = true; currentChannelUrl = channel.url
                                                    playVideo(channel.url); showMenu = false
                                                },
                                                onToggleFav = {
                                                    if (favorites.contains(channel)) favorites.remove(channel) else favorites.add(channel)
                                                    lastInteractionTrigger = System.currentTimeMillis()
                                                },
                                                onFocus = {
                                                    lastInteractionTrigger = System.currentTimeMillis()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        val initial = ChannelsConfig.getChannels()
        val savedUrl = prefs.getString("last_channel_url", "") ?: ""
        val initialUrl = if (savedUrl.isNotEmpty() && initial.any { it.url == savedUrl }) savedUrl else initial.firstOrNull()?.url
        initialUrl?.let { playVideo(it) }
    }

    private fun playVideo(url: String) {
        try {
            mediaPlayer?.stop()
            val media = Media(libVlc, Uri.parse(url))
            media.addOption(":http-user-agent=$PC_USER_AGENT")
            media.addOption(":http-referrer=https://www.google.com/")
            media.addOption(":network-caching=3000")
            mediaPlayer?.media = media
            media.release()
            mediaPlayer?.play()
            prefs.edit().putString("last_channel_url", url).apply()
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onStop() { super.onStop(); mediaPlayer?.stop(); mediaPlayer?.detachViews() }
    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release(); libVlc?.release() }
}

@Composable
fun VideoPlayer(mediaPlayer: MediaPlayer, url: String) {
    AndroidView(factory = { context -> 
        VLCVideoLayout(context).apply { 
            mediaPlayer.attachViews(this, null, false, false) 
            isFocusable = false
        } 
    }, update = { view ->
        // Forzamos que la vista se mantenga vinculada al reproductor
        if (!mediaPlayer.vlcVout.areViewsAttached()) {
            mediaPlayer.detachViews()
            mediaPlayer.attachViews(view, null, false, false)
        }
    }, modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryItem(name: String, isSelected: Boolean, onFocus: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick = { onFocus() },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onFocusChanged { 
            isFocused = it.isFocused
            if (it.isFocused) onFocus() 
        },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.1f) else Color.Transparent,
            focusedContainerColor = Color(0xFF00B4D8).copy(alpha = 0.2f)
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.CenterStart) {
            Text(name, style = MaterialTheme.typography.labelMedium, color = if (isFocused || isSelected) Color.White else Color.Gray, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelItem(channel: Channel, isSelected: Boolean, isFav: Boolean, onSelect: () -> Unit, onToggleFav: () -> Unit, onFocus: () -> Unit) {
    val fallbackPainter = rememberVectorPainter(Icons.Default.Tv)
    
    Surface(
        onClick = { onSelect() },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.isFocused) onFocus() }
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onToggleFav() }) },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF252525) else Color(0xFF121212).copy(alpha = 0.5f),
            focusedContainerColor = Color(0xFF333333)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00B4D8)))
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp))
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(channel.logo)
                        .addHeader("User-Agent", PC_USER_AGENT)
                        .addHeader("Referer", "https://www.google.com/")
                        .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                        .build(),
                    contentDescription = null, 
                    placeholder = fallbackPainter,
                    error = fallbackPainter,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
                if (isFav) Text("❤ FAVORITO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF4D6D))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DialogButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick, 
        modifier = modifier,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = color,
            focusedContainerColor = color.copy(alpha = 0.8f)
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
