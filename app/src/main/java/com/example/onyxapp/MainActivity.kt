@file:OptIn(ExperimentalTvMaterial3Api::class)
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
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text as M3Text
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
import androidx.compose.ui.input.key.*
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
import androidx.compose.ui.zIndex
import androidx.tv.material3.*
import coil.compose.AsyncImage
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

        setContent {
            M3MaterialTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen { showSplash = false }
                } else {
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
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var showMenu by remember { mutableStateOf(true) }
    val lastInteractionTrigger = remember { mutableLongStateOf(System.currentTimeMillis()) }

    val categories = remember(viewModel.isAdmin) {
        if (viewModel.isAdmin) listOf("LIVE", "ADMIN", "AJUSTES") else listOf("LIVE", "AJUSTES")
    }

    var selectedCategory by remember { mutableStateOf("LIVE") }
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showMenu) {
        if (showMenu) {
            delay(200)
            initialFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(showMenu, lastInteractionTrigger.longValue) {
        if (showMenu && !isPortrait) {
            delay(15000)
            showMenu = false
        }
    }

    BackHandler { if (showMenu) showMenu = false else (context as? Activity)?.finishAffinity() }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        // 1. REPRODUCTOR DE VIDEO
        viewModel.mediaPlayer?.let { player ->
            val videoModifier = if (showMenu && !isPortrait) {
                Modifier
                    .padding(top = 100.dp, end = 40.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.55f)
                    .aspectRatio(viewModel.videoAspectRatio)
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                    .clickable { showMenu = false }
            } else {
                Modifier.fillMaxSize().clickable { showMenu = true }
            }
            VideoPlayer(player, videoModifier)
        }

        // 2. CAPA DE INTERFAZ (Layout de dos columnas para NO obstruir)
        AnimatedVisibility(visible = showMenu, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
            Row(modifier = Modifier.fillMaxSize()) {
                
                // COLUMNA IZQUIERDA: MENÚ (42% de la pantalla)
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)))
                        .padding(start = 40.dp, top = 30.dp, bottom = 30.dp, end = 20.dp)
                ) {
                    // TÍTULO Y RELOJ
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

                    // CATEGORÍAS
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        itemsIndexed(categories) { _, cat ->
                            CategoryTab(
                                name = cat,
                                isSelected = selectedCategory == cat,
                                modifier = if (cat == selectedCategory) Modifier.focusRequester(initialFocusRequester) else Modifier
                            ) {
                                selectedCategory = cat
                                lastInteractionTrigger.longValue = System.currentTimeMillis()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = if (selectedCategory == "LIVE") "CANALES EN VIVO" else selectedCategory, 
                        color = Color.White.copy(alpha = 0.4f), 
                        style = MaterialTheme.typography.labelLarge, 
                        modifier = Modifier.padding(bottom = 15.dp),
                        letterSpacing = 1.5.sp
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedCategory) {
                            "AJUSTES" -> SettingsPanel(viewModel) { lastInteractionTrigger.longValue = System.currentTimeMillis() }
                            "ADMIN" -> AdminPanel(viewModel) { lastInteractionTrigger.longValue = System.currentTimeMillis() }
                            else -> {
                                val listState = rememberLazyListState()
                                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(viewModel.filteredChannels) { index, channel ->
                                        val isFav = viewModel.favorites.any { it.url == channel.url }
                                        ChannelListItem(
                                            number = index + 1, channel = channel,
                                            isSelected = viewModel.currentChannelUrl == channel.url,
                                            isFavorite = isFav,
                                            onClick = {
                                                if (viewModel.currentChannelUrl == channel.url) showMenu = false else viewModel.playVideo(channel.url)
                                                lastInteractionTrigger.longValue = System.currentTimeMillis()
                                            },
                                            onFocus = { lastInteractionTrigger.longValue = System.currentTimeMillis() },
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

                // COLUMNA DERECHA: INFO CANAL (Bajada para no obstruir el video)
                Column(
                    modifier = Modifier
                        .weight(0.58f)
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
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF00B4D8),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EN VIVO", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelListItem(number: Int, channel: Channel, isSelected: Boolean, isFavorite: Boolean, onClick: () -> Unit, onFocus: () -> Unit, onRight: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) { if (isFocused) onFocus() }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionRight) {
                    onRight()
                    true
                } else false
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color(0xFF00B4D8)))),
        interactionSource = interactionSource
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = channel.logo, 
                    contentDescription = null, 
                    placeholder = rememberVectorPainter(Icons.Default.Tv), 
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.3f)), 
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (isSelected) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(24.dp))
                } else {
                    Text(text = number.toString(), color = if (isFocused) Color.White else Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = channel.name, 
                color = Color.White, 
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge, 
                fontSize = 19.sp, 
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )

            if (isFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4D6D), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AccountInfoCard(viewModel: MainViewModel) {
    val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(it) } ?: "..."
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(if (viewModel.isAdmin) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF00B4D8).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (viewModel.isAdmin) Icons.Default.Shield else Icons.Default.AccountCircle, 
                    contentDescription = null, 
                    tint = if (viewModel.isAdmin) Color(0xFFFFD700) else Color(0xFF00B4D8), 
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(viewModel.currentUsername.uppercase(), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                Text("Suscripción hasta: $expiryStr", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun CategoryTab(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        modifier = modifier.height(48.dp).widthIn(min = 120.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.1f),
            focusedContainerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.2f)
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White))),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = name, 
                color = Color.White,
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VideoPlayer(mediaPlayer: MediaPlayer, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            VLCVideoLayout(context).apply {
                mediaPlayer.detachViews()
                mediaPlayer.attachViews(this, null, true, false)
            }
        },
        modifier = modifier
    )
}

@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 20.dp)) {
        Text("CONFIGURACIÓN", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(20.dp))
        
        SettingToggle("Modo Administrador", viewModel.isAdmin) { 
            viewModel.isAdmin = it
            onInteraction()
        }
        
        Spacer(modifier = Modifier.height(15.dp))
        
        Button(
            onClick = { viewModel.logout(); onInteraction() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(12.dp))
            M3Text("CERRAR SESIÓN", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.05f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        M3Text(label, color = Color.White, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00B4D8), checkedTrackColor = Color(0xFF00B4D8).copy(alpha = 0.5f))
        )
    }
}

@Composable
fun AdminPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 20.dp)) {
        Text("PANEL DE CONTROL", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Aquí puedes gestionar canales y usuarios si tienes los permisos necesarios.", color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ONYX TV", style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.Black)
                Text("Bienvenido de nuevo", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 30.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 15.dp), textAlign = TextAlign.Center)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { M3Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00B4D8), focusedLabelColor = Color(0xFF00B4D8), cursorColor = Color(0xFF00B4D8))
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { M3Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00B4D8), focusedLabelColor = Color(0xFF00B4D8), cursorColor = Color(0xFF00B4D8))
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { viewModel.signIn(username, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else M3Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
