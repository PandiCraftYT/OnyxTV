package com.example.onyxapp

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.onyxapp.ui.components.*
import com.example.onyxapp.ui.theme.OnyxAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val viewModel: MainViewModel = viewModel()
            var showSplash by remember { mutableStateOf(true) }
            var forceShowLogin by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.onShowLoginRequested = { forceShowLogin = true }
            }

            LaunchedEffect(viewModel.isUserAuthenticated) {
                if (viewModel.isUserAuthenticated) {
                    forceShowLogin = false
                    viewModel.isFromPromoChannel = false
                }
            }

            OnyxAppTheme {
                viewModel.appUpdateConfig?.let { config ->
                    UpdateDialog(
                        config = config,
                        isDownloading = viewModel.isDownloadingUpdate,
                        progress = viewModel.downloadProgress,
                        onDismiss = { viewModel.dismissUpdate() },
                        onConfirm = { viewModel.downloadAndInstallUpdate() }
                    )
                }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    when {
                        forceShowLogin -> {
                            LoginScreen(viewModel) {
                                forceShowLogin = false
                                viewModel.isFromPromoChannel = false
                            }
                        }
                        viewModel.isLoading && viewModel.isUserAuthenticated && !viewModel.isUserAuthorized -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00B4D8))
                            }
                        }
                        viewModel.isUserAuthenticated && !viewModel.isUserAuthorized -> {
                            UnauthorizedScreen(viewModel)
                        }
                        else -> {
                            MainScreen(viewModel, onLoginRequest = {
                                viewModel.isFromPromoChannel = true
                                forceShowLogin = true
                            })
                        }
                    }
                }

                viewModel.activeGlobalMessage?.let { message ->
                    GlobalMessageOverlay(message = message, onDismiss = { viewModel.dismissGlobalMessage() })
                }
            }
        }
    }
}

@Composable
fun VideoControlsOverlay(
    isPlaying: Boolean,
    title: String,
    group: String = "LIVE",
    currentTimeMs: Long = 0,
    totalTimeMs: Long = 0,
    onTogglePause: () -> Unit,
    onNext: () -> Unit,
    onFullScreen: () -> Unit,
    onSeek: (Float) -> Unit = {},
    isFullScreen: Boolean,
    visible: Boolean
) {
    val isMovie = group.contains("PELÍCULA", true) || group.contains("MOVIE", true)
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f))) {
                if (isMovie && totalTimeMs > 0) {
                    Slider(
                        value = (currentTimeMs.toFloat() / totalTimeMs.toFloat()).coerceIn(0f, 1f),
                        onValueChange = onSeek,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00B4D8), activeTrackColor = Color(0xFF00B4D8))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentTimeMs), color = Color.White, fontSize = 10.sp)
                        Text(formatTime(totalTimeMs), color = Color.White, fontSize = 10.sp)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onTogglePause) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White) }
                        IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
                        Spacer(Modifier.width(8.dp))
                        Text(text = title.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                        Text(text = if (isMovie) "PELÍCULA" else "LIVE", color = if (isMovie) Color(0xFF00B4D8) else Color.Red, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        IconButton(onClick = onFullScreen) { Icon(if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, null, tint = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoStatusOverlay(isLoading: Boolean, errorMessage: String?) {
    if (isLoading || errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) CircularProgressIndicator(color = Color(0xFF00B4D8), modifier = Modifier.size(40.dp))
            errorMessage?.let {
                Text(it, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLoginRequest: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isTV = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) || context.packageManager.hasSystemFeature("android.hardware.type.television") }
    val isMobile = !isTV

    // Controladores de sistema para Mobile y TV
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isFullScreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(true) }
    var showVideoControls by remember { mutableStateOf(false) }
    val activity = context as? Activity

    var userActivityTrigger by remember { mutableLongStateOf(0L) }
    val resetTimer = { userActivityTrigger = System.currentTimeMillis() }

    // Estados de scroll independientes para no perder la posición al cambiar de pestaña
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // MEJORA 1: Gestores de Foco dedicados para guiar el control remoto
    val initialFocusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }
    val listFocusRequester = remember { FocusRequester() } // Foco para el primer elemento de la lista

    var isSearchActiveMain by remember { mutableStateOf(false) }

    val filteredChannels = viewModel.filteredChannels
    val filteredMovies = viewModel.filteredMovies
    val currentChannelUrl = viewModel.currentChannelUrl
    val isMoviePlaying = viewModel.currentPlaybackGroup.contains("PELÍCULA", true) || viewModel.currentPlaybackGroup.contains("MOVIE", true)

    LaunchedEffect(showVideoControls) {
        if (showVideoControls) { delay(5000); showVideoControls = false }
    }

    LaunchedEffect(isFullScreen, isMobile) {
        if (isMobile) {
            activity?.requestedOrientation = if (isFullScreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.stopPlayback()
            if (event == Lifecycle.Event.ON_RESUME && viewModel.currentChannelUrl.isNotEmpty()) {
                viewModel.viewModelScope.launch { delay(500); viewModel.playVideo(viewModel.currentChannelUrl, false) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val categories = remember(viewModel.isUserAuthenticated) {
        val list = mutableListOf("LIVE")
        if (viewModel.isUserAuthenticated) { list.add("PELÍCULAS"); list.add("AJUSTES") }
        if (!viewModel.isUserAuthenticated) { if (isTV) list.add("ACCEDER") else list.add("INICIAR SESIÓN") }
        list
    }

    var selectedCategory by remember { mutableStateOf("LIVE") }

    // MEJORA 2: Cuando se cambia de categoría en TV, automáticamente enfocar el contenido debajo
    LaunchedEffect(selectedCategory) {
        if (isTV && selectedCategory != "AJUSTES") {
            delay(100) // Pequeño retraso para que Compose renderice la nueva lista
            try { listFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(viewModel.isUserAuthenticated) {
        if (!viewModel.isUserAuthenticated && (selectedCategory == "AJUSTES" || selectedCategory == "PELÍCULAS")) {
            selectedCategory = "LIVE"
        }
    }

    LaunchedEffect(showMenu, userActivityTrigger) {
        if (isTV && showMenu) { delay(15000); showMenu = false }
    }

    LaunchedEffect(showMenu) {
        if (isTV && showMenu) {
            delay(300)
            if (selectedCategory == "LIVE" && currentChannelUrl.isNotEmpty()) {
                val index = filteredChannels.indexOfFirst { it.url == currentChannelUrl }
                if (index >= 0) { try { listState.scrollToItem(index) } catch(e: Exception) {} }
            }
            try { initialFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(isSearchActiveMain) {
        if (isSearchActiveMain) {
            delay(200)
            try { searchFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    BackHandler(enabled = isFullScreen || (isTV && showMenu)) {
        if (isMobile) { isFullScreen = false; showMenu = true } else { showMenu = false }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).onPreviewKeyEvent { event ->
        if (!isTV) return@onPreviewKeyEvent false
        resetTimer()
        if (event.type == KeyEventType.KeyDown) {
            val isCenterKey = event.nativeKeyEvent.keyCode in listOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            if (isCenterKey) {
                if (!showMenu) { showMenu = true; return@onPreviewKeyEvent true }
            } else if (!showMenu) {
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> { viewModel.zapNext(); return@onPreviewKeyEvent true }
                    KeyEvent.KEYCODE_DPAD_DOWN -> { viewModel.zapPrevious(); return@onPreviewKeyEvent true }
                    KeyEvent.KEYCODE_DPAD_LEFT -> { if (isMoviePlaying) { viewModel.seekTo(viewModel.currentTimeMs - 15000); return@onPreviewKeyEvent true } }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { if (isMoviePlaying) { viewModel.seekTo(viewModel.currentTimeMs + 30000); return@onPreviewKeyEvent true } }
                }
            }
        }
        false
    }) {
        if (isTV) AnimatedBackground()

        val videoModifier = remember(isFullScreen, isMobile, viewModel.videoAspectRatio, showMenu, selectedCategory) {
            when {
                isFullScreen -> Modifier.fillMaxSize()
                isMobile -> Modifier.fillMaxWidth().aspectRatio(16f / 9f).align(Alignment.TopCenter)
                showMenu && isTV -> Modifier.padding(top = 100.dp, end = 40.dp).align(Alignment.TopEnd).fillMaxWidth(0.55f).aspectRatio(viewModel.videoAspectRatio).clip(RoundedCornerShape(24.dp)).border(BorderStroke(2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                else -> Modifier.fillMaxSize()
            }
        }

        Box(modifier = videoModifier.clickable {
            resetTimer()
            if (isTV) showMenu = !showMenu else showVideoControls = !showVideoControls
        }) {
            viewModel.mediaPlayer?.let { player -> VideoPlayer(player, Modifier.fillMaxSize()) }
            VideoStatusOverlay(viewModel.isLoading, errorMessage = viewModel.errorMessage)

            if (isMobile || isFullScreen) {
                VideoControlsOverlay(
                    isPlaying = viewModel.isPlaying, title = viewModel.currentPlaybackTitle,
                    group = viewModel.currentPlaybackGroup,
                    currentTimeMs = viewModel.currentTimeMs, totalTimeMs = viewModel.totalTimeMs,
                    onTogglePause = { resetTimer(); viewModel.togglePause() }, onNext = { resetTimer(); viewModel.zapNext() },
                    onFullScreen = { resetTimer(); if (isFullScreen) { isFullScreen = false; showMenu = true } else { isFullScreen = true; showMenu = false }; showVideoControls = false },
                    onSeek = { viewModel.seekTo((it * viewModel.totalTimeMs).toLong()) },
                    isFullScreen = isFullScreen, visible = showVideoControls
                )
            }
        }

        if (!isFullScreen) {
            if (isMobile) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Transparent))
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF0A0E12)).padding(horizontal = 16.dp)) {
                        Text(
                            text = if (viewModel.isUserAuthenticated) "ONYX TV - PREMIUM" else "ONYX TV - GRATIS",
                            fontWeight = FontWeight.Black, color = if (viewModel.isUserAuthenticated) Color(0xFFFFD700) else Color(0xFFC0C0C0),
                            fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp).align(Alignment.CenterHorizontally)
                        )

                        // MEJORA 3: KeyboardActions en Móvil para ocultar el teclado limpiamente
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { resetTimer(); viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Buscar...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); keyboardController?.hide() }),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White.copy(0.05f), unfocusedContainerColor = Color.White.copy(0.05f), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00B4D8)) }
                        )

                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(categories) { _, cat ->
                                CategoryTab(name = cat, isSelected = selectedCategory == cat, isMobile = true) {
                                    resetTimer()
                                    if (cat == "INICIAR SESIÓN") onLoginRequest() else selectedCategory = cat
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedCategory) {
                                "AJUSTES" -> SettingsPanel(viewModel) { resetTimer() }
                                "PELÍCULAS" -> {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 32.dp)
                                    ) {
                                        items(filteredMovies) { movie ->
                                            MovieItem(movie = movie, isMobile = true, onClick = {
                                                resetTimer()
                                                viewModel.playVideo(movie.video_url)
                                            }, onFocus = { resetTimer() })
                                        }
                                    }
                                }
                                else -> {
                                    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                                        itemsIndexed(filteredChannels, key = { index, item -> item.id ?: "${item.url}_$index" }) { index, channel ->
                                            val displayNumber = if (viewModel.isUserAuthenticated) index + 1 else index
                                            ChannelListItem(number = displayNumber, channel = channel, isSelected = currentChannelUrl == channel.url, isFavorite = viewModel.favorites.any { it.url == channel.url }, isMobile = true, onClick = { resetTimer(); viewModel.playVideo(channel.url) }, onFocus = { resetTimer() }, onLeft = { resetTimer() }, onRight = { resetTimer() })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isTV) {
                AnimatedVisibility(visible = showMenu, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val columnWeight = 0.42f
                        Column(modifier = Modifier.weight(columnWeight).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.9f), Color.Transparent))).padding(start = 40.dp, top = 25.dp, end = 20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column { Text(text = if (viewModel.isUserAuthenticated) "ONYX TV - PREMIUM" else "ONYX TV - GRATIS", style = MaterialTheme.typography.headlineSmall, color = if (viewModel.isUserAuthenticated) Color(0xFFFFD700) else Color(0xFFC0C0C0), fontWeight = FontWeight.Black) }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(viewModel.currentTime, color = Color.White.copy(0.9f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(viewModel.currentDate, color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(Modifier.height(15.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (!isSearchActiveMain && viewModel.searchQuery.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.05f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).clickable { resetTimer(); isSearchActiveMain = true }.padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(12.dp)); Text("Buscar...", color = Color.White.copy(0.3f), fontSize = 13.sp) }
                                    }
                                } else {
                                    // MEJORA 4: Manejo de teclado robusto para TV
                                    TextField(
                                        value = viewModel.searchQuery,
                                        onValueChange = { resetTimer(); viewModel.updateSearchQuery(it) },
                                        placeholder = { Text("Buscar...", fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = {
                                            // Cuando se presiona la lupa o enter en el teclado de la TV
                                            keyboardController?.hide()
                                            try { listFocusRequester.requestFocus() } catch(e:Exception){}
                                        }),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).focusRequester(searchFocusRequester).onFocusChanged {
                                            resetTimer()
                                            if (!it.isFocused && viewModel.searchQuery.isEmpty()) isSearchActiveMain = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00B4D8)) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(15.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)) {
                                itemsIndexed(categories) { _, cat ->
                                    CategoryTab(
                                        name = cat, isSelected = selectedCategory == cat,
                                        modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier
                                    ) {
                                        resetTimer()
                                        if (cat == "INICIAR SESIÓN" || cat == "ACCEDER") onLoginRequest() else selectedCategory = cat
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))

                            // MEJORA 5: Agrupar el foco (focusGroup) de las listas para que el D-pad no escape hacia arriba fácilmente
                            Box(modifier = Modifier.weight(1f).focusGroup()) {
                                when (selectedCategory) {
                                    "AJUSTES" -> SettingsPanel(viewModel) { resetTimer() }
                                    "PELÍCULAS" -> {
                                        LazyVerticalGrid(
                                            state = gridState, // Mantiene la posición del scroll
                                            columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(15.dp), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(bottom = 50.dp)
                                        ) {
                                            items(filteredMovies) { movie ->
                                                MovieItem(
                                                    movie = movie, isMobile = false,
                                                    onClick = {
                                                        resetTimer()
                                                        viewModel.playVideo(movie.video_url)
                                                    },
                                                    onFocus = { resetTimer() }
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            itemsIndexed(filteredChannels, key = { index, item -> item.id ?: "${item.url}_$index" }) { index, channel ->
                                                val displayNumber = if (viewModel.isUserAuthenticated) index + 1 else index
                                                ChannelListItem(
                                                    number = displayNumber, channel = channel, isSelected = currentChannelUrl == channel.url, isFavorite = viewModel.favorites.any { it.url == channel.url }, isMobile = false,
                                                    onClick = { resetTimer(); viewModel.playVideo(channel.url) },
                                                    onFocus = { resetTimer() },
                                                    onLeft = { resetTimer(); try { initialFocusRequester.requestFocus() } catch(e:Exception){} },
                                                    onRight = { resetTimer(); showMenu = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (selectedCategory == "LIVE" && viewModel.isUserAuthenticated) { Spacer(Modifier.height(15.dp)); AccountInfoCard(viewModel) }
                        }
                        Column(modifier = Modifier.weight(1f - columnWeight).fillMaxHeight().padding(bottom = 60.dp, end = 50.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            if (viewModel.currentPlaybackTitle.isNotEmpty()) {
                                Text(viewModel.currentPlaybackTitle.uppercase(), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                                Text(viewModel.currentPlaybackGroup, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                                
                                if (isMoviePlaying && viewModel.totalTimeMs > 0) {
                                    Spacer(Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { (viewModel.currentTimeMs.toFloat() / viewModel.totalTimeMs.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth(0.8f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF00B4D8), trackColor = Color.White.copy(0.2f)
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(0.8f).padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(formatTime(viewModel.currentTimeMs), color = Color.White.copy(0.7f), fontSize = 12.sp)
                                        Text(formatTime(viewModel.totalTimeMs), color = Color.White.copy(0.7f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (h > 0) String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
