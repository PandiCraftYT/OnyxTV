package com.example.onyxapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            OnyxAppTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

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
        // Usamos la implementación de AnimatedBackground.kt
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

                    // CATEGORÍAS - Centradas y con padding vertical para evitar cortes
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
                                lastInteractionTrigger.longValue = System.currentTimeMillis()
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelListItem(
    number: Int,
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit,
    onRight: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) { if (isFocused) onFocus() }

    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White.copy(alpha = 0.2f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, if (isSelected) Color(0xFF00B4D8) else Color.White))
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%03d", number),
                style = MaterialTheme.typography.titleLarge,
                color = if (isFocused || isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = channel.logo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.group ?: "General",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (isFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
            }
            
            if (isFocused) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            }
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
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        modifier = modifier.height(48.dp).widthIn(min = 100.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.1f),
            focusedContainerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.2f)
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White))),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = name, 
                color = Color.White,
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
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
fun AccountInfoCard(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (viewModel.isAdmin) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF00B4D8).copy(alpha = 0.1f))
            .border(1.dp, if (viewModel.isAdmin) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFF00B4D8).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imageVector = if (viewModel.isAdmin) Icons.Default.Shield else Icons.Default.AccountCircle
            Icon(
                imageVector = imageVector, 
                contentDescription = null, 
                tint = if (viewModel.isAdmin) Color(0xFFFFD700) else Color(0xFF00B4D8), 
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Text(viewModel.currentUsername.uppercase(), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Black)
                val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "N/A"
                Text("Expira: $expiryStr", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("CONFIGURACIÓN", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        
        SettingToggle("Modo Administrador", viewModel.isAdmin) { 
            viewModel.isAdmin = it
            onInteraction()
        }
        
        SettingButton("Cerrar Sesión", Icons.Default.ExitToApp, Color.Red) {
            viewModel.logout()
            onInteraction()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White)
            Switch(checked = checked, onCheckedChange = null, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00B4D8)))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(15.dp))
            Text(label, color = Color.White)
        }
    }
}

@Composable
fun AdminPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Text("PANEL DE CONTROL", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Gestionar usuarios y suscripciones (Simulado)", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        
        // Aquí iría la lista de usuarios para el admin
    }
}

@Composable
fun OnyxAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00B4D8),
            background = Color.Black,
            surface = Color(0xFF121212)
        ),
        typography = Typography(),
        content = content
    )
}
