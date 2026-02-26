@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.example.onyxapp

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.*
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val imageLoader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .build()

        setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                androidx.compose.material3.MaterialTheme {
                    val isAuthenticated = viewModel.isUserAuthenticated
                    val isAuthorized = viewModel.isUserAuthorized

                    if (!isAuthenticated || !isAuthorized) {
                        LoginScreen(viewModel)
                    } else {
                        MainAppContent(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF002B36), Color.Black), radius = 2000f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(if (isPortrait) 0.95f else 0.4f)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            androidx.compose.material3.Text(
                text = "ONYX TV",
                fontSize = if (isPortrait) 42.sp else 64.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(color = Color(0xFF00B4D8), blurRadius = 25f))
            )
            
            androidx.compose.material3.Text(
                text = "STREAMING PRIVADO",
                fontSize = 12.sp,
                color = Color(0xFF00B4D8),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(28.dp))
                    .padding(32.dp)
            ) {
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { androidx.compose.material3.Text("Usuario") },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { androidx.compose.material3.Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color.Gray
                    )
                )
                
                val error = viewModel.authError ?: viewModel.accountStatusMessage
                if (error != null) {
                    androidx.compose.material3.Text(
                        text = error, color = Color(0xFFFF4D6D),
                        fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF00B4D8))
                } else {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.signIn(username, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        androidx.compose.material3.Text("ENTRAR", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    
    var showMenu by remember { mutableStateOf(true) }
    var lastInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val categories = listOf("LIVE", "AJUSTES")
    var selectedCategory by remember { mutableStateOf("LIVE") }
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showMenu) {
        if (showMenu) {
            delay(200)
            initialFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(showMenu, lastInteractionTrigger) {
        if (showMenu && !isPortrait) {
            delay(15000)
            showMenu = false 
        }
    }

    BackHandler { if (showMenu) showMenu = false else (context as? Activity)?.finishAffinity() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. FONDO INMERSIVO
        if (showMenu) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black, Color(0xFF001A1F), Color.Black))
            ))
        }

        // 2. VIDEO DINÁMICO ADAPTABLE
        viewModel.mediaPlayer?.let { player ->
            val videoModifier = if (showMenu && !isPortrait) {
                Modifier
                    .padding(top = 160.dp, end = 40.dp) 
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight(0.45f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable { showMenu = false } // CLIC EN VIDEO = FULLSCREEN
            } else {
                Modifier.fillMaxSize()
            }
            VideoPlayer(player, videoModifier)
        }

        if (!showMenu) {
            Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                showMenu = true
                lastInteractionTrigger = System.currentTimeMillis()
            })
        }

        // 3. CAPA DE INTERFAZ
        AnimatedVisibility(visible = showMenu, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo para cerrar el menú
                Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showMenu = false })

                Column(modifier = Modifier.fillMaxSize()) {
                    // TOP BAR
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            androidx.tv.material3.Text("ONYX TV", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black)
                            androidx.tv.material3.Text("v1.2 - Premium Access", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00B4D8))
                        }
                        androidx.tv.material3.Text(viewModel.currentTime, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    // CATEGORÍAS (CON GLOW AL ENFOCAR)
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        itemsIndexed(categories) { _, cat ->
                            CategoryTab(name = cat, isSelected = selectedCategory == cat, modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier) {
                                selectedCategory = cat
                                lastInteractionTrigger = System.currentTimeMillis()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxSize()) {
                        // COLUMNA IZQUIERDA: CANALES
                        Column(modifier = Modifier.weight(0.42f).fillMaxHeight().padding(start = 30.dp, bottom = 20.dp)) {
                            androidx.tv.material3.Text("Canales disponibles", color = Color.Gray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (selectedCategory == "AJUSTES") {
                                    SettingsPanel(viewModel) { lastInteractionTrigger = System.currentTimeMillis() }
                                } else {
                                    val listState = rememberLazyListState()
                                    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                            val isFav = viewModel.favorites.any { it.url == channel.url }
                                            ChannelListItem(
                                                number = index + 1, channel = channel,
                                                isSelected = viewModel.currentChannelUrl == channel.url,
                                                isFavorite = isFav,
                                                onClick = {
                                                    if (viewModel.currentChannelUrl == channel.url) showMenu = false else viewModel.playVideo(channel.url)
                                                    lastInteractionTrigger = System.currentTimeMillis()
                                                },
                                                onLongClick = { viewModel.toggleFavorite(channel) },
                                                onFocus = { lastInteractionTrigger = System.currentTimeMillis() }
                                            )
                                        }
                                    }
                                }
                            }
                            if (selectedCategory != "AJUSTES") {
                                Spacer(modifier = Modifier.height(10.dp))
                                AccountInfoCard(viewModel)
                            }
                        }

                        // COLUMNA DERECHA: INFO CANAL
                        Column(modifier = Modifier.weight(0.58f).fillMaxHeight().padding(bottom = 40.dp, end = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            val currentChannel = viewModel.allChannels.find { it.url == viewModel.currentChannelUrl }
                            if (currentChannel != null) {
                                androidx.tv.material3.Text(
                                    text = currentChannel.name.uppercase(), 
                                    style = MaterialTheme.typography.headlineSmall.copy(shadow = Shadow(color = Color.Black, blurRadius = 10f)),
                                    color = Color.White, textAlign = TextAlign.Center, maxLines = 1, fontWeight = FontWeight.Black
                                )
                                androidx.tv.material3.Text(text = currentChannel.group ?: "GENERAL", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Red.copy(alpha = 0.1f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.tv.material3.Text("EN VIVO", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00B4D8))
    }
}

@Composable
fun AccountInfoCard(viewModel: MainViewModel) {
    val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(it) } ?: "..."
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.06f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.tv.material3.Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            androidx.tv.material3.Text(viewModel.currentUsername, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            androidx.tv.material3.Text("Suscripción hasta: $expiryStr", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CategoryTab(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Surface(
        onClick = onClick, modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isFocused) Color.White else if (isSelected) Color(0xFF00B4D8) else Color.Transparent,
            focusedContainerColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            androidx.tv.material3.Text(text = name, color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ChannelListItem(number: Int, channel: Channel, isSelected: Boolean, isFavorite: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, onFocus: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val fallbackPainter = rememberVectorPainter(Icons.Default.Tv)
    
    LaunchedEffect(isFocused) { if (isFocused) onFocus() }

    Surface(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth().height(60.dp)
            .clickable(onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }, onTap = { onClick() }) },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.15f) else Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color(0xFF00B4D8)))),
        interactionSource = interactionSource
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = channel.logo, contentDescription = null, placeholder = fallbackPainter, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(35.dp), contentAlignment = Alignment.Center) {
                if (isSelected) {
                    androidx.tv.material3.Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(24.dp))
                } else {
                    androidx.tv.material3.Text(text = number.toString(), color = if (isFocused) Color.White else Color.Gray, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            androidx.tv.material3.Text(text = channel.name, color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontSize = 16.sp, fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            if (isFavorite) {
                androidx.tv.material3.Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4D6D), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        AccountInfoCard(viewModel)
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(
            onClick = { viewModel.logout() }, 
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D6D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            androidx.tv.material3.Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            androidx.tv.material3.Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun VideoPlayer(mediaPlayer: MediaPlayer, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> VLCVideoLayout(context).apply { mediaPlayer.detachViews(); mediaPlayer.attachViews(this, null, false, false); isFocusable = false } },
        update = { view -> if (!mediaPlayer.vlcVout.areViewsAttached()) { mediaPlayer.attachViews(view, null, false, false) } },
        modifier = modifier
    )
}
