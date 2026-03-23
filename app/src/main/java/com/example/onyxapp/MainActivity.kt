package com.example.onyxapp

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

            // Vincular el callback del ViewModel con el estado de la interfaz
            LaunchedEffect(Unit) {
                viewModel.onShowLoginRequested = {
                    forceShowLogin = true
                }
            }

            // Redirección automática al menú tras login exitoso
            LaunchedEffect(viewModel.isUserAuthenticated) {
                if (viewModel.isUserAuthenticated) {
                    forceShowLogin = false
                    viewModel.isFromPromoChannel = false
                }
            }

            OnyxAppTheme {
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    // SISTEMA DE SEGURIDAD: Auth Guard + Modo Invitado
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
            }
        }
    }
}

@Composable
fun VideoStatusOverlay(isLoading: Boolean, errorMessage: String?) {
    if (isLoading || errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF00B4D8),
                    modifier = Modifier.size(40.dp)
                )
            }
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLoginRequest: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val isTV = remember { 
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) || 
        context.packageManager.hasSystemFeature("android.hardware.type.television") 
    }
    val isMobile = !isTV

    var isFullScreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(true) }
    val activity = context as? Activity

    var interactionKey by remember { mutableStateOf(Any()) }
    var isOpeningMenuByKey by remember { mutableStateOf(false) }
    val resetTimer = { interactionKey = Any() }

    LaunchedEffect(isFullScreen, isMobile) {
        if (isMobile) {
            if (isFullScreen) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    LaunchedEffect(viewModel.authError) {
        viewModel.authError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAuthError()
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

    val categories = remember(viewModel.isAdmin, viewModel.isUserAuthenticated) {
        val list = mutableListOf("LIVE")
        if (viewModel.isAdmin) list.add("ADMIN")
        list.add("AJUSTES")
        if (!viewModel.isUserAuthenticated) list.add("INICIAR SESIÓN")
        list
    }
    var selectedCategory by remember { mutableStateOf("LIVE") }
    val listState = rememberLazyListState()
    val initialFocusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchActiveMain by remember { mutableStateOf(false) }

    LaunchedEffect(showMenu) {
        if (isTV && showMenu) {
            delay(300)
            if (selectedCategory == "LIVE" && viewModel.currentChannelUrl.isNotEmpty()) {
                val index = viewModel.filteredChannels.indexOfFirst { it.url == viewModel.currentChannelUrl }
                if (index >= 0) listState.scrollToItem(index)
            }
            try { initialFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(isSearchActiveMain) {
        if (isSearchActiveMain) {
            delay(150)
            try { searchFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(showMenu, interactionKey) {
        if (isTV && showMenu) {
            delay(15000)
            showMenu = false
        }
    }

    BackHandler(enabled = isFullScreen || (isTV && showMenu)) { 
        if (isMobile) {
            isFullScreen = false
            showMenu = true 
        } else {
            showMenu = false
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .onPreviewKeyEvent { event ->
            if (!isTV) return@onPreviewKeyEvent false
            if (event.type == KeyEventType.KeyDown) resetTimer()

            val isCenterKey = event.nativeKeyEvent.keyCode in listOf(
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            )

            if (isCenterKey) {
                if (event.type == KeyEventType.KeyDown) {
                    if (!showMenu) {
                        showMenu = true
                        isOpeningMenuByKey = true
                        true
                    } else if (isOpeningMenuByKey) {
                        true
                    } else {
                        false
                    }
                } else { 
                    if (isOpeningMenuByKey) {
                        isOpeningMenuByKey = false
                        true
                    } else {
                        false
                    }
                }
            } else if (!showMenu && event.type == KeyEventType.KeyDown) {
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> { viewModel.zapNext(); true }
                    KeyEvent.KEYCODE_DPAD_DOWN -> { viewModel.zapPrevious(); true }
                    else -> false
                }
            } else false
        }
    ) {
        if (isTV) AnimatedBackground()
        
        val videoModifier = remember(isFullScreen, isMobile, viewModel.videoAspectRatio, showMenu, selectedCategory, isOpeningMenuByKey) {
            val base = when {
                isFullScreen -> Modifier.fillMaxSize()
                isMobile -> Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .align(Alignment.TopCenter)
                showMenu && isTV -> Modifier
                    .padding(top = 100.dp, end = 40.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(if (selectedCategory == "ADMIN") 0.25f else 0.55f)
                    .aspectRatio(viewModel.videoAspectRatio)
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                else -> Modifier.fillMaxSize()
            }
            
            if (isTV) {
                base.clickable { if (!isOpeningMenuByKey) showMenu = !showMenu }
            } else {
                base
            }
        }

        Box(modifier = videoModifier) {
            viewModel.mediaPlayer?.let { player ->
                VideoPlayer(player, Modifier.fillMaxSize())
            }
            VideoStatusOverlay(viewModel.isLoading, viewModel.errorMessage)
        }

        if (!isFullScreen) {
            if (isMobile) {
                // UI MÓVIL (HEADER DINÁMICO)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Transparent)) {
                        IconButton(
                            onClick = { isFullScreen = true; showMenu = false },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Fullscreen, null, tint = Color.White)
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF0A0E12)).padding(horizontal = 16.dp)) {
                        Text(
                            text = if (viewModel.isUserAuthenticated) "ONYX TV - PREMIUM" else "ONYX TV - GRATIS",
                            fontWeight = FontWeight.Black,
                            color = if (viewModel.isUserAuthenticated) Color(0xFFFFD700) else Color(0xFFC0C0C0),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 12.dp).align(Alignment.CenterHorizontally)
                        )
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Buscar canal...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(0.05f),
                                unfocusedContainerColor = Color.White.copy(0.05f),
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00B4D8)) }
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(categories) { _, cat ->
                                CategoryTab(name = cat, isSelected = selectedCategory == cat, isMobile = true) { 
                                    if (cat == "INICIAR SESIÓN") onLoginRequest() else selectedCategory = cat 
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedCategory) {
                                "AJUSTES" -> SettingsPanel(viewModel) {}
                                "ADMIN" -> AdminPanel(viewModel) {}
                                else -> {
                                    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                                        itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                            val displayNumber = if (viewModel.isUserAuthenticated) index + 1 else index
                                            ChannelListItem(number = displayNumber, channel = channel, isSelected = viewModel.currentChannelUrl == channel.url, isFavorite = viewModel.favorites.any { it.url == channel.url }, isMobile = true, onClick = { viewModel.playVideo(channel.url) }, onFocus = {}, onLeft = {}, onRight = {})
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isTV) {
                // UI TV (HEADER DINÁMICO)
                AnimatedVisibility(visible = showMenu, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val columnWeight = if (selectedCategory == "ADMIN") 0.70f else 0.42f
                        Column(modifier = Modifier.weight(columnWeight).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.9f), Color.Transparent))).padding(start = 40.dp, top = 25.dp, end = 20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = if (viewModel.isUserAuthenticated) "ONYX TV - PREMIUM" else "ONYX TV - GRATIS",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (viewModel.isUserAuthenticated) Color(0xFFFFD700) else Color(0xFFC0C0C0),
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(viewModel.currentTime, color = Color.White.copy(0.9f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(viewModel.currentDate, color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(Modifier.height(15.dp))
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (!isSearchActiveMain && viewModel.searchQuery.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.05f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).clickable { isSearchActiveMain = true; resetTimer() }.padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Search, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text("Buscar...", color = Color.White.copy(0.3f), fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    TextField(
                                        value = viewModel.searchQuery,
                                        onValueChange = { viewModel.updateSearchQuery(it); resetTimer() },
                                        placeholder = { Text("Buscar...", fontSize = 13.sp) },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).focusRequester(searchFocusRequester).onFocusChanged { 
                                            if (it.isFocused) resetTimer()
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
                                        name = cat, 
                                        isSelected = selectedCategory == cat,
                                        modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier
                                    ) { 
                                        if (cat == "INICIAR SESIÓN") onLoginRequest() else selectedCategory = cat
                                        resetTimer() 
                                    } 
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (selectedCategory == "AJUSTES") SettingsPanel(viewModel) { resetTimer() }
                                else if (selectedCategory == "ADMIN") AdminPanel(viewModel) { resetTimer() }
                                else {
                                    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                            val displayNumber = if (viewModel.isUserAuthenticated) index + 1 else index
                                            ChannelListItem(number = displayNumber, channel = channel, isSelected = viewModel.currentChannelUrl == channel.url, isFavorite = viewModel.favorites.any { it.url == channel.url }, isMobile = false, onClick = { viewModel.playVideo(channel.url); resetTimer() }, onFocus = { resetTimer() }, onLeft = { try { initialFocusRequester.requestFocus() } catch(e:Exception){} }, onRight = { showMenu = false })
                                        }
                                    }
                                }
                            }
                            if (selectedCategory == "LIVE" && viewModel.isUserAuthenticated) { Spacer(Modifier.height(15.dp)); AccountInfoCard(viewModel) }
                        }
                        Column(modifier = Modifier.weight(1f - columnWeight).fillMaxHeight().padding(bottom = 60.dp, end = 50.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            viewModel.allChannels.find { it.url == viewModel.currentChannelUrl }?.let {
                                Text(it.name.uppercase(), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                                Text(it.group ?: "GENERAL", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
