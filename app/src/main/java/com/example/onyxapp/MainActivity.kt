package com.example.onyxapp

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.onyxapp.ui.components.*
import com.example.onyxapp.ui.theme.OnyxAppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val viewModel: MainViewModel = viewModel()
            var showSplash by remember { mutableStateOf(true) }

            OnyxAppTheme {
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    // Manejo del ciclo de vida para detener audio al salir (Home)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopPlayback()
            } else if (event == Lifecycle.Event.ON_START) {
                if (viewModel.currentChannelUrl.isNotEmpty()) {
                    viewModel.playVideo(viewModel.currentChannelUrl, resetRetry = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showMenu by remember { mutableStateOf(true) }
    var interactionKey by remember { mutableStateOf(Any()) }

    var isOpeningMenuByKey by remember { mutableStateOf(false) }

    val categories = remember(viewModel.isAdmin) {
        if (viewModel.isAdmin) listOf("LIVE", "ADMIN", "AJUSTES") else listOf("LIVE", "AJUSTES")
    }

    var selectedCategory by remember { mutableStateOf("LIVE") }
    val initialFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    val resetTimer = { interactionKey = Any() }

    LaunchedEffect(showMenu) {
        if (showMenu) {
            delay(200)
            if (selectedCategory == "LIVE" && viewModel.currentChannelUrl.isNotEmpty()) {
                val index = viewModel.filteredChannels.indexOfFirst { it.url == viewModel.currentChannelUrl }
                if (index >= 0) listState.scrollToItem(index)
            }
            initialFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(showMenu, interactionKey) {
        if (showMenu && !isPortrait) {
            delay(15000)
            showMenu = false
        }
    }

    BackHandler(enabled = showMenu) {
        showMenu = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
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
                        KeyEvent.KEYCODE_DPAD_UP -> { viewModel.zapPrevious(); true }
                        KeyEvent.KEYCODE_DPAD_DOWN -> { viewModel.zapNext(); true }
                        else -> false
                    }
                } else false
            }
    ) {
        AnimatedBackground()

        Box(modifier = Modifier.fillMaxSize()) {
            viewModel.mediaPlayer?.let { player ->
                val videoModifier = if (showMenu && !isPortrait) {
                    Modifier
                        .padding(top = 100.dp, end = 40.dp)
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(if (selectedCategory == "ADMIN") 0.25f else 0.55f)
                        .aspectRatio(viewModel.videoAspectRatio)
                        .clip(RoundedCornerShape(24.dp))
                        .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                        .clickable { if (!isOpeningMenuByKey) showMenu = false }
                } else {
                    Modifier.fillMaxSize().clickable { showMenu = true }
                }
                VideoPlayer(player, videoModifier)
            }

            if (viewModel.isLoading || viewModel.errorMessage != null) {
                Box(
                    modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(color = Color(0xFF00B4D8))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Cargando...", color = Color.White)
                        }
                        viewModel.errorMessage?.let {
                            Text(it, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val columnWeight = if (selectedCategory == "ADMIN") 0.70f else 0.42f
                Column(
                    modifier = Modifier
                        .weight(columnWeight)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)))
                        .padding(start = 40.dp, top = 30.dp, bottom = 30.dp, end = 20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("ONYX TV", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text(
                                text = if (viewModel.isAdmin) "MODO ADMINISTRADOR" else "Premium Access",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (viewModel.isAdmin) Color(0xFFFFD700) else Color(0xFF00B4D8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(viewModel.currentTime, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.headlineSmall)
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(categories) { _, cat ->
                            CategoryTab(
                                name = cat,
                                isSelected = selectedCategory == cat,
                                modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier
                            ) {
                                selectedCategory = cat
                                resetTimer()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = if (selectedCategory == "LIVE") "CANALES EN VIVO" else selectedCategory,
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedCategory) {
                            "AJUSTES" -> SettingsPanel(viewModel) { resetTimer() }
                            "ADMIN" -> AdminPanel(viewModel) { resetTimer() }
                            else -> {
                                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                        val isFav = viewModel.favorites.any { it.url == channel.url }
                                        ChannelListItem(
                                            number = index + 1, channel = channel,
                                            isSelected = viewModel.currentChannelUrl == channel.url,
                                            isFavorite = isFav,
                                            onClick = {
                                                if (viewModel.currentChannelUrl == channel.url) showMenu = false else viewModel.playVideo(channel.url)
                                                resetTimer()
                                            },
                                            onFocus = { resetTimer() },
                                            onLeft = { initialFocusRequester.requestFocus() },
                                            onRight = { showMenu = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (selectedCategory == "LIVE") {
                        Spacer(modifier = Modifier.height(20.dp))
                        AccountInfoCard(viewModel)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f - columnWeight)
                        .fillMaxHeight()
                        .padding(bottom = 60.dp, end = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (selectedCategory == "LIVE") {
                        val currentChannel = viewModel.allChannels.find { it.url == viewModel.currentChannelUrl }
                        if (currentChannel != null) {
                            Text(
                                text = currentChannel.name.uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    shadow = Shadow(color = Color.Black.copy(alpha = 0.8f), blurRadius = 20f)
                                ),
                                color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentChannel.group ?: "GENERAL",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
