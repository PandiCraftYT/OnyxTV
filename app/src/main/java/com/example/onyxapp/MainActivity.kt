package com.example.onyxapp

import android.app.Activity
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.text.SimpleDateFormat
import java.util.*

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
    val isWideScreen = configuration.screenWidthDp > 800

    LaunchedEffect(Unit) {
        delay(300)
        try { focusRequester.requestFocus() } catch (e: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF002B36), Color(0xFF000000)),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(if (isWideScreen) 0.35f else 0.85f)
                .padding(24.dp)
        ) {
            androidx.compose.material3.Text(
                text = "ONYX TV",
                fontSize = 56.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                style = TextStyle(shadow = Shadow(color = Color(0xFF00B4D8), blurRadius = 20f))
            )

            androidx.compose.material3.Text(
                text = "STREAMING PRIVADO",
                fontSize = 12.sp,
                color = Color(0xFF00B4D8),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
                    .padding(32.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { androidx.compose.material3.Text("Nombre de Usuario", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00B4D8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = Color(0xFF00B4D8)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { androidx.compose.material3.Text("Contraseña", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00B4D8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = Color(0xFF00B4D8)
                    )
                )

                val error = viewModel.authError ?: viewModel.accountStatusMessage
                if (error != null) {
                    androidx.compose.material3.Text(
                        text = error,
                        color = Color(0xFFFF4D6D),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00B4D8), strokeWidth = 3.dp)
                    }
                } else {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.signIn(username, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B4D8),
                            contentColor = Color.Black
                        )
                    ) {
                        androidx.compose.material3.Text(
                            "ENTRAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.sp
                        )
                    }
                }
            }

            if (viewModel.isUserAuthenticated && !viewModel.isUserAuthorized) {
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.TextButton(onClick = { viewModel.logout() }) {
                    androidx.compose.material3.Text("Cerrar Sesión actual", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    
    var showMenu by remember { mutableStateOf(true) }
    var lastInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Categorías unificadas: Solo LIVE y AJUSTES
    val categories = listOf("LIVE", "AJUSTES")
    var selectedCategory by remember { mutableStateOf("LIVE") }

    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showMenu) {
        if (showMenu) {
            delay(200)
            try { initialFocusRequester.requestFocus() } catch (e: Exception) {}
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
        
        // FONDO DEGRADADO
        if (showMenu) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color(0xFF003B4A).copy(alpha = 0.6f), Color.Black))
            ))
        }

        // VIDEO DINÁMICO
        viewModel.mediaPlayer?.let { player ->
            val videoModifier = if (showMenu && !isPortrait) {
                Modifier
                    .padding(top = 160.dp, end = 40.dp)
                    .align(Alignment.TopEnd)
                    .width((configuration.screenWidthDp * 0.55).dp)
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    showMenu = false
                })

                Column(modifier = Modifier.fillMaxSize()) {
                    // TOP BAR
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            androidx.tv.material3.Text("ONYX TV", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black)
                            androidx.tv.material3.Text("v1.2 - Privado", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Text(viewModel.currentUsername, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(20.dp))
                            androidx.compose.material3.Text(viewModel.currentTime, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // PESTAÑAS LIVE / AJUSTES
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(categories) { _, cat ->
                            CategoryTab(
                                name = cat, isSelected = selectedCategory == cat,
                                modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier
                            ) {
                                selectedCategory = cat
                                lastInteractionTrigger = System.currentTimeMillis()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(modifier = Modifier.fillMaxSize()) {
                        // LISTA DE CANALES UNIFICADA
                        Column(modifier = Modifier.weight(0.42f).padding(start = 25.dp)) {
                            androidx.tv.material3.Text(
                                if (selectedCategory == "LIVE") "Todos los Canales" else "Configuración",
                                color = Color.Gray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (selectedCategory == "AJUSTES") {
                                SettingsPanel(viewModel) { lastInteractionTrigger = System.currentTimeMillis() }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxHeight()) {
                                    itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                        ChannelListItem(
                                            number = index + 1,
                                            channel = channel, isSelected = viewModel.currentChannelUrl == channel.url,
                                            onClick = {
                                                if (viewModel.currentChannelUrl == channel.url) {
                                                    showMenu = false
                                                } else {
                                                    viewModel.playVideo(channel.url)
                                                }
                                                lastInteractionTrigger = System.currentTimeMillis()
                                            },
                                            onFocus = { lastInteractionTrigger = System.currentTimeMillis() }
                                        )
                                    }
                                }
                            }
                        }

                        // INFO CANAL
                        Column(
                            modifier = Modifier.weight(0.58f).fillMaxHeight().padding(bottom = 30.dp, end = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            val currentChannel = viewModel.allChannels.find { it.url == viewModel.currentChannelUrl }
                            if (currentChannel != null) {
                                androidx.tv.material3.Text(
                                    text = currentChannel.name.uppercase(),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        shadow = Shadow(color = Color.Black, blurRadius = 6f)
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold
                                )
                                androidx.tv.material3.Text(
                                    text = currentChannel.group ?: "GENERAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00B4D8),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.tv.material3.Text("EN VIVO", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00B4D8))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryTab(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isFocused) Color.White else if (isSelected) Color(0xFF00B4D8) else Color.Transparent,
            focusedContainerColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
            androidx.tv.material3.Text(
                text = name,
                color = if (isFocused) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelListItem(number: Int, channel: Channel, isSelected: Boolean, onClick: () -> Unit, onFocus: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val fallbackPainter = rememberVectorPainter(Icons.Default.Tv)

    LaunchedEffect(isFocused) { if (isFocused) onFocus() }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp).clickable { onClick() },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color(0xFF00B4D8)))),
        interactionSource = interactionSource
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Text(
                text = "$number.",
                color = if (isFocused || isSelected) Color(0xFF00B4D8) else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(10.dp))
            AsyncImage(
                model = channel.logo, contentDescription = null, placeholder = fallbackPainter, error = fallbackPainter,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(5.dp)), contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            androidx.tv.material3.Text(
                text = channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp, fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) } ?: "Cargando..."
    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(15.dp)) {
            androidx.tv.material3.Text("Usuario: ${viewModel.currentUsername}", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            androidx.tv.material3.Text("Vence: $expiryStr", color = Color.LightGray, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material3.Button(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D6D)), shape = RoundedCornerShape(10.dp)) {
            androidx.tv.material3.Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            androidx.compose.material3.Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
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
