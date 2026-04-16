@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.example.onyxapp.ui.components

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.onyxapp.*
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChannelListItem(
    number: Int,
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean,
    isMobile: Boolean = false,
    onClick: () -> Unit,
    onFocus: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPromo = channel.url == "onyx://login"

    LaunchedEffect(isFocused) { if (isFocused && !isMobile) onFocus() }

    if (isMobile) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChannelItemContent(
                number = number,
                channel = channel,
                isSelected = isSelected,
                isFavorite = isFavorite,
                isMobile = true,
                isFocused = false,
                isPromo = isPromo
            )
        }
    } else {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.DirectionRight -> { onRight(); true }
                            Key.DirectionLeft -> { onLeft(); true }
                            else -> false
                        }
                    } else false
                },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isPromo) Color(0xFF00B4D8).copy(0.2f) else if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                focusedContainerColor = if (isPromo) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.15f)
            ),
            border = if (isPromo) {
                ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, Color.White)),
                    border = Border(BorderStroke(1.dp, Color(0xFF00B4D8).copy(0.5f)))
                )
            } else {
                ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, if (isSelected) Color(0xFF00B4D8) else Color.White))
                )
            },
            interactionSource = interactionSource
        ) {
            ChannelItemContent(
                number = number,
                channel = channel,
                isSelected = isSelected,
                isFavorite = isFavorite,
                isMobile = false,
                isFocused = isFocused,
                isPromo = isPromo,
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun ChannelItemContent(
    number: Int,
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean,
    isMobile: Boolean,
    isFocused: Boolean,
    isPromo: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isPromo) {
            Box(
                modifier = Modifier.width(if (isMobile) 40.dp else 50.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%03d", number),
                    style = if (isMobile) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
                    color = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(if (isMobile) 8.dp else 12.dp))
            
            Box(
                modifier = Modifier
                    .size(if (isMobile) 44.dp else 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logo != null) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.width(if (isMobile) 92.dp else 114.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star, 
                    null, 
                    tint = if (isFocused) Color.Black else Color(0xFF00B4D8),
                    modifier = Modifier.size(if (isMobile) 30.dp else 36.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(if (isMobile) 12.dp else 20.dp))
        
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    style = if (isMobile) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = if (isPromo && isFocused) Color.Black else Color.White,
                    fontWeight = if (isPromo) FontWeight.Black else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isFavorite && !isPromo) {
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Default.Favorite, 
                        null, 
                        tint = Color.Red, 
                        modifier = Modifier.size(if (isMobile) 18.dp else 22.dp)
                    )
                }
            }
            Text(
                text = if (isPromo) "HAZTE PREMIUM AHORA" else (channel.group ?: "General").uppercase(),
                style = if (isMobile) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                color = if (isPromo && isFocused) Color.Black.copy(0.7f) else if (isPromo) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.4f),
                fontWeight = if (isPromo) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isFocused && !isMobile) {
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = if (isPromo) Color.Black else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun MovieItem(
    movie: Movie,
    isMobile: Boolean = false,
    onClick: () -> Unit,
    onFocus: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) { if (isFocused && !isMobile) onFocus() }

    if (isMobile) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        ) {
            MovieItemContent(movie = movie, isFocused = false)
        }
    } else {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(2f / 3f),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(BorderStroke(3.dp, Color(0xFF00B4D8)))
            ),
            interactionSource = interactionSource
        ) {
            MovieItemContent(movie = movie, isFocused = isFocused)
        }
    }
}

@Composable
private fun MovieItemContent(movie: Movie, isFocused: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = movie.poster_url,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.8f)),
                        startY = 300f
                    )
                )
        )

        if (isFocused) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF00B4D8).copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }

        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryTab(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    isMobile: Boolean = false,
    onClick: () -> Unit
) {
    if (isMobile) {
        Box(
            modifier = modifier
                .height(36.dp)
                .widthIn(min = 80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.08f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name, 
                color = if (isSelected) Color.Black else Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    } else {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            modifier = modifier.height(44.dp).widthIn(min = 120.dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.08f),
                focusedContainerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.20f)
            ),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White))),
            interactionSource = interactionSource
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = name, 
                    color = if (isSelected || isFocused) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun VideoPlayer(mediaPlayer: MediaPlayer, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            VLCVideoLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mediaPlayer.detachViews()
                mediaPlayer.attachViews(this, null, true, false)
            }
        },
        update = { vlcLayout ->
            if (!mediaPlayer.vlcVout.areViewsAttached()) {
                mediaPlayer.detachViews()
                mediaPlayer.attachViews(vlcLayout, null, true, false)
            }
        },
        onRelease = {
            mediaPlayer.detachViews()
        },
        modifier = modifier
    )
}

@Composable
fun AccountInfoCard(viewModel: MainViewModel) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = Date()
    
    val expiryDateText = viewModel.userExpiryDate?.let { sdf.format(it) } ?: "Sin fecha"
    val daysLeft = viewModel.userExpiryDate?.let { 
        val diff = it.time - now.time
        val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS) + 1
        if (days < 0) 0 else days
    } ?: 0
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("ESTADO DE SUSCRIPCIÓN", color = Color(0xFF00B4D8), fontWeight = FontWeight.Black, fontSize = 10.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vence el:", color = Color.White.copy(0.6f), fontSize = 13.sp)
                Text(expiryDateText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Días restantes:", color = Color.White.copy(0.6f), fontSize = 13.sp)
                Text("$daysLeft días", color = if(daysLeft <= 3) Color.Red else Color(0xFF00FF00), fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun UpdateDialog(
    config: AppConfig,
    isDownloading: Boolean,
    progress: Float,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        if (!isDownloading) {
            delay(500)
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF0F1115))
                .border(2.dp, Color(0xFF00B4D8).copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = Color(0xFF00B4D8),
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "ACTUALIZACIÓN DISPONIBLE",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Versión ${config.versionName}",
                    color = Color(0xFF00B4D8),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!config.changeLog.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = config.changeLog,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                if (isDownloading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF00B4D8),
                            trackColor = Color.White.copy(0.1f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("DESCARGANDO... ${(progress * 100).toInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.colors(containerColor = Color.White.copy(0.1f)),
                            shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                        ) {
                            Text("MÁS TARDE", color = Color.White)
                        }
                        
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .focusRequester(focusRequester),
                            colors = ButtonDefaults.colors(containerColor = Color(0xFF00B4D8)),
                            shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                        ) {
                            Text("ACTUALIZAR", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalMessageOverlay(message: GlobalMessage, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (message.type) {
            "marquee" -> {
                MarqueeMessage(message.message)
            }
            "popup" -> {
                PopupMessage(message.message, onDismiss)
            }
        }
    }
}

@Composable
fun MarqueeMessage(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Black.copy(alpha = 0.75f))
            .border(1.dp, Color(0xFF00B4D8).copy(0.4f))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.layout.Layout(
            content = {
                Text(
                    text = text.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints.copy(minWidth = 0))
            layout(constraints.maxWidth, constraints.maxHeight) {
                val x = ((constraints.maxWidth + placeable.width) * xOffset / 2) + (constraints.maxWidth / 2) - (placeable.width / 2)
                placeable.placeRelative(x.toInt(), 0)
            }
        }
    }
}

@Composable
fun PopupMessage(text: String, onDismiss: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300)
        try { focusRequester.requestFocus() } catch(e: Exception) {}
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(550.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF16191E))
                .border(2.dp, Color(0xFF00B4D8), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "MENSAJE DEL SISTEMA",
                    color = Color(0xFF00B4D8),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = text,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp).focusRequester(focusRequester),
                    colors = ButtonDefaults.colors(containerColor = Color(0xFF00B4D8)),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
