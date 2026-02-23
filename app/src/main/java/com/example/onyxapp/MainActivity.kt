package com.example.onyxapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.tv.material3.*
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val imageLoader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .build()

        setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                MaterialTheme {
                    val context = LocalContext.current
                    val configuration = LocalConfiguration.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    val isWideScreen = configuration.screenWidthDp > 800
                    val isTv = (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
                    
                    val prefs = remember { context.getSharedPreferences("OnyxPrefs", Context.MODE_PRIVATE) }
                    var showTutorial by remember { mutableStateOf(prefs.getBoolean("is_first_launch", true)) }

                    var isAppActive by remember { mutableStateOf(true) }

                    // Gestión de ciclo de vida
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> isAppActive = true
                                Lifecycle.Event.ON_PAUSE -> isAppActive = false
                                Lifecycle.Event.ON_STOP -> { viewModel.stopPlayback() }
                                Lifecycle.Event.ON_START -> {
                                    if (viewModel.currentChannelUrl.isNotEmpty()) {
                                        viewModel.playVideo(viewModel.currentChannelUrl)
                                    }
                                }
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    val filteredChannels = viewModel.filteredChannels
                    val favorites = viewModel.favorites
                    
                    val groupedChannels = remember(filteredChannels, favorites.size, viewModel.searchQuery) {
                        val map = filteredChannels.groupBy { it.group ?: "OTROS" }.toMutableMap()
                        if (favorites.isNotEmpty()) {
                            val favsToShow = if (viewModel.searchQuery.isEmpty()) {
                                favorites.toList()
                            } else {
                                favorites.filter { it.name.contains(viewModel.searchQuery, ignoreCase = true) }
                            }
                            if (favsToShow.isNotEmpty()) map["FAVORITOS"] = favsToShow
                        }
                        map
                    }
                    
                    val categories = remember(groupedChannels) { 
                        val keys = groupedChannels.keys.toMutableList().apply { sort() }
                        if (keys.remove("MEXICO")) keys.add(0, "MEXICO")
                        if (keys.remove("FAVORITOS")) keys.add(0, "FAVORITOS")
                        keys.add("AJUSTES")
                        keys
                    }

                    var selectedCategory by remember { mutableStateOf("") }
                    LaunchedEffect(categories) {
                        if (selectedCategory !in categories) {
                            selectedCategory = categories.firstOrNull() ?: ""
                        }
                    }

                    var showMenu by remember { mutableStateOf(true) }
                    var zapInfoTrigger by remember { mutableLongStateOf(0L) }
                    var lastInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    var backPressCount by remember { mutableIntStateOf(0) }
                    
                    val mainFocusRequester = remember { FocusRequester() }
                    val categoryFocusRequester = remember { FocusRequester() }
                    val channelFocusRequester = remember { FocusRequester() }

                    // Temporizador de ocultación del menú
                    LaunchedEffect(showMenu, lastInteractionTrigger, isAppActive) {
                        if (showMenu && !isPortrait && isAppActive) {
                            delay(10000)
                            showMenu = false 
                        }
                    }

                    val showZapInfo = remember(zapInfoTrigger) { zapInfoTrigger > 0 }
                    // Temporizador del banner de información
                    LaunchedEffect(zapInfoTrigger, isAppActive) {
                        if (zapInfoTrigger > 0 && isAppActive) {
                            delay(5000)
                            zapInfoTrigger = 0
                        }
                    }

                    // Gestión de foco protegida contra crashes
                    LaunchedEffect(showMenu) {
                        if (!showMenu) {
                            try { mainFocusRequester.requestFocus() } catch (e: Exception) {}
                        } else if (isTv) {
                            delay(500) // Tiempo para que la animación termine y el elemento se monte
                            try { categoryFocusRequester.requestFocus() } catch (e: Exception) {}
                        }
                    }

                    LaunchedEffect(backPressCount) {
                        if (backPressCount > 0) {
                            delay(2000)
                            backPressCount = 0
                        }
                    }

                    BackHandler {
                        if (showMenu) showMenu = false
                        else {
                            if (backPressCount >= 1) finishAffinity() else backPressCount++
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .focusRequester(mainFocusRequester)
                            .focusable()
                            .onKeyEvent {
                                if (!showMenu && !showTutorial) {
                                    when (it.nativeKeyEvent.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> { 
                                            viewModel.zapPrevious()
                                            zapInfoTrigger = System.currentTimeMillis()
                                            true 
                                        }
                                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> { 
                                            viewModel.zapNext()
                                            zapInfoTrigger = System.currentTimeMillis()
                                            true 
                                        }
                                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, 
                                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                            showMenu = true
                                            lastInteractionTrigger = System.currentTimeMillis()
                                            true
                                        }
                                        KeyEvent.KEYCODE_BACK -> false
                                        else -> {
                                            showMenu = true
                                            lastInteractionTrigger = System.currentTimeMillis()
                                            true
                                        }
                                    }
                                } else false
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    if (!showTutorial) {
                                        showMenu = !showMenu
                                        lastInteractionTrigger = System.currentTimeMillis()
                                    }
                                })
                            }
                            .pointerInput(isTv) {
                                if (!isTv && !showTutorial) {
                                    var totalDrag = 0f
                                    detectVerticalDragGestures(
                                        onDragEnd = {
                                            if (totalDrag > 150) {
                                                viewModel.zapNext()
                                                zapInfoTrigger = System.currentTimeMillis()
                                            } else if (totalDrag < -150) {
                                                viewModel.zapPrevious()
                                                zapInfoTrigger = System.currentTimeMillis()
                                            }
                                            totalDrag = 0f
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDrag += dragAmount
                                        }
                                    )
                                }
                            }
                    ) {
                        viewModel.mediaPlayer?.let { player -> VideoPlayer(player) }

                        if (viewModel.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00B4D8), strokeWidth = 4.dp)
                            }
                        }

                        viewModel.errorMessage?.let { msg ->
                            Box(modifier = Modifier.fillMaxSize().padding(bottom = if(isPortrait) 100.dp else 50.dp), contentAlignment = Alignment.BottomCenter) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.7f)).padding(16.dp)) {
                                    Text(msg, color = Color.White)
                                }
                            }
                        }

                        // Banner de información
                        AnimatedVisibility(
                            visible = showZapInfo && !showMenu,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            val currentChannel = viewModel.allChannels.find { it.url == viewModel.currentChannelUrl }
                            currentChannel?.let { channel ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black)))
                                        .padding(horizontal = 30.dp, vertical = 25.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(if(isPortrait) 70.dp else 90.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(channel.logo).addHeader("User-Agent", ChannelsConfig.PC_USER_AGENT).build(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.padding(10.dp).fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(20.dp))
                                            Column {
                                                Text(text = channel.name, color = Color.White, fontSize = if(isPortrait) 22.sp else 28.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                                                Text(text = channel.group?.uppercase() ?: "GENERAL", color = Color(0xFF00B4D8), fontSize = if(isPortrait) 14.sp else 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                                            }
                                        }
                                        if (!isPortrait) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = viewModel.currentTime, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                                                Text(text = "EN VIVO", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Menú Principal
                        AnimatedVisibility(
                            visible = showMenu && !showTutorial,
                            enter = if (isPortrait) slideInVertically(initialOffsetY = { it }) + fadeIn() else slideInHorizontally() + fadeIn(),
                            exit = if (isPortrait) slideOutVertically(targetOffsetY = { it }) + fadeOut() else slideOutHorizontally() + fadeOut(),
                            modifier = Modifier.align(if (isPortrait) Alignment.BottomCenter else Alignment.CenterStart)
                        ) {
                            val menuBrush = if (isPortrait) {
                                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF121212).copy(alpha = 0.85f), Color(0xFF121212)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF121212).copy(alpha = 0.98f), Color(0xFF121212).copy(alpha = 0.85f), Color.Transparent))
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(if (isPortrait) 0.75f else 1f)
                                    .fillMaxWidth(if (isPortrait) 1f else if (isWideScreen) 0.45f else 0.65f)
                                    .background(menuBrush)
                                    .pointerInput(Unit) { detectTapGestures { /* Bloquea clics */ } }
                                    .padding(20.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        androidx.tv.material3.Text("ONYX TV", style = androidx.tv.material3.MaterialTheme.typography.displaySmall, color = Color(0xFF00B4D8))
                                        androidx.tv.material3.Text("ig: carlosnvz_", style = androidx.tv.material3.MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.tv.material3.Text(viewModel.currentTime, style = androidx.tv.material3.MaterialTheme.typography.titleLarge, color = Color.White)
                                        if (!isTv) {
                                            Spacer(modifier = Modifier.width(16.dp))
                                            IconButton(onClick = { showMenu = false }) {
                                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).onFocusChanged { if (it.isFocused) lastInteractionTrigger = System.currentTimeMillis() },
                                    placeholder = { Text("Buscar canal...", color = Color.Gray) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color.Gray),
                                    singleLine = true
                                )

                                Row(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(modifier = Modifier.weight(0.35f).fillMaxHeight()) {
                                        itemsIndexed(categories) { index, cat ->
                                            CategoryItem(
                                                name = cat, 
                                                isSelected = selectedCategory == cat,
                                                modifier = if (index == 0) Modifier.focusRequester(categoryFocusRequester) else Modifier
                                            ) { 
                                                selectedCategory = cat
                                                lastInteractionTrigger = System.currentTimeMillis()
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(15.dp))
                                    Column(modifier = Modifier.weight(0.65f).fillMaxHeight()) {
                                        androidx.tv.material3.Text(selectedCategory, style = androidx.tv.material3.MaterialTheme.typography.labelLarge, color = Color(0xFF00B4D8), modifier = Modifier.padding(bottom = 8.dp))
                                        
                                        if (selectedCategory == "AJUSTES") {
                                            SettingsPanel(viewModel) { lastInteractionTrigger = System.currentTimeMillis() }
                                        } else {
                                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                itemsIndexed(groupedChannels[selectedCategory] ?: emptyList()) { index, channel ->
                                                    ChannelItem(
                                                        channel = channel,
                                                        isSelected = viewModel.currentChannelUrl == channel.url,
                                                        isFav = favorites.any { it.url == channel.url },
                                                        modifier = if (index == 0) Modifier.focusRequester(channelFocusRequester) else Modifier,
                                                        onSelect = { viewModel.playVideo(channel.url); showMenu = false },
                                                        onToggleFav = { viewModel.toggleFavorite(channel); lastInteractionTrigger = System.currentTimeMillis() },
                                                        onFocus = { lastInteractionTrigger = System.currentTimeMillis() }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showTutorial) {
                            TutorialOverlay(isTv) {
                                showTutorial = false
                                prefs.edit().putBoolean("is_first_launch", false).apply()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialOverlay(isTv: Boolean, onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val steps = if (isTv) {
        listOf(
            TutorialStep(Icons.Default.UnfoldMore, "Control de Canales", "Usa las flechas ARRIBA/ABAJO de tu control para cambiar de canal rápidamente."),
            TutorialStep(Icons.Default.AdsClick, "Abrir el Menú", "Presiona el botón CENTRAL (OK) o cualquier flecha lateral para abrir la lista de canales."),
            TutorialStep(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", "Usa el botón ATRÁS de tu control para cerrar el menú o salir de la aplicación.")
        )
    } else {
        listOf(
            TutorialStep(Icons.Default.SwipeVertical, "Cambiar Canales", "Desliza hacia arriba o abajo en la pantalla para navegar por la lista de canales."),
            TutorialStep(Icons.Default.TouchApp, "Abrir el Menú", "Toca cualquier parte de la pantalla para mostrar u ocultar la lista de canales."),
            TutorialStep(Icons.Default.Favorite, "Favoritos", "Mantén presionado un canal en la lista para agregarlo o quitarlo de tus favoritos.")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(onClick = { if (step < steps.size - 1) step++ else onDismiss() }, interactionSource = remember { MutableInteractionSource() }, indication = null)
            .onKeyEvent { 
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (step < steps.size - 1) step++ else onDismiss()
                    true
                } else false
            }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut()) },
            label = "TutorialAnimation"
        ) { targetStep ->
            val current = steps[targetStep]
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                Box(modifier = Modifier.size(if(isTv) 120.dp else 100.dp).background(Color(0xFF00B4D8).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(current.icon, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(if(isTv) 60.dp else 48.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = current.title, color = Color.White, fontSize = if(isTv) 36.sp else 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = current.description, color = Color.LightGray, fontSize = if(isTv) 22.sp else 18.sp, textAlign = TextAlign.Center, lineHeight = if(isTv) 32.sp else 24.sp, modifier = Modifier.fillMaxWidth(if(isTv) 0.7f else 1f))
                Spacer(modifier = Modifier.height(48.dp))
                Text(text = if (targetStep == steps.size - 1) "¡ENTENDIDO!" else "SIGUIENTE", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold, fontSize = if(isTv) 20.sp else 16.sp)
            }
        }
    }
}

data class TutorialStep(val icon: ImageVector, val title: String, val description: String)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    var tempUrl by remember { mutableStateOf(viewModel.externalM3uUrl) }
    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        androidx.compose.material3.Text("Lista M3U Externa", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tempUrl,
            onValueChange = { tempUrl = it; onInteraction() },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://servidor.com/lista.m3u", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color.Gray),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material3.Button(onClick = { viewModel.updateExternalM3U(tempUrl) }, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar y Actualizar", color = Color.White)
        }
    }
}

@Composable
fun VideoPlayer(mediaPlayer: MediaPlayer) {
    AndroidView(factory = { context -> VLCVideoLayout(context).apply { mediaPlayer.detachViews(); mediaPlayer.attachViews(this, null, false, false); isFocusable = false } }, update = { view -> if (!mediaPlayer.vlcVout.areViewsAttached()) { mediaPlayer.attachViews(view, null, false, false) } }, modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryItem(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onFocus: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(onClick = { onFocus() }, modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).onFocusChanged { if (it.isFocused) onFocus() }.clickable(interactionSource = interactionSource, indication = null) { onFocus() }, interactionSource = interactionSource, colors = ClickableSurfaceDefaults.colors(containerColor = if (isSelected || isHovered) Color(0xFF00B4D8).copy(alpha = 0.1f) else Color.Transparent, focusedContainerColor = Color(0xFF00B4D8).copy(alpha = 0.2f)), shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.CenterStart) { androidx.compose.material3.Text(name, style = androidx.tv.material3.MaterialTheme.typography.labelMedium, color = if (isFocused || isSelected || isHovered) Color.White else Color.Gray, maxLines = 1) }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelItem(channel: Channel, isSelected: Boolean, isFav: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit, onToggleFav: () -> Unit, onFocus: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val fallbackPainter = rememberVectorPainter(Icons.Default.Tv)
    Surface(onClick = { onSelect() }, modifier = modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onFocus() }.pointerInput(Unit) { detectTapGestures(onTap = { onSelect() }, onLongPress = { onToggleFav() }) }, interactionSource = interactionSource, colors = ClickableSurfaceDefaults.colors(containerColor = if (isSelected || isHovered) Color(0xFF252525) else Color(0xFF121212).copy(alpha = 0.5f), focusedContainerColor = Color(0xFF333333)), border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00B4D8)))), shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp))) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(channel.logo).addHeader("User-Agent", ChannelsConfig.PC_USER_AGENT).addHeader("Referer", "https://www.google.com/").addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8").build(), contentDescription = null, placeholder = fallbackPainter, error = fallbackPainter, modifier = Modifier.size(48.dp), contentScale = ContentScale.Fit) }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(channel.name, style = androidx.tv.material3.MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
                if (isFav) androidx.compose.material3.Text("❤ FAVORITO", style = androidx.tv.material3.MaterialTheme.typography.labelSmall, color = Color(0xFFFF4D6D))
            }
        }
    }
}
