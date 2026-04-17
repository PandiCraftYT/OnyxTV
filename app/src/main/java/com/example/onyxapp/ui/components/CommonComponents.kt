@file:OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.onyxapp.*
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

    // Animación suave de fondo para móvil
    val mobileBgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        label = "mobile_bg_color"
    )

    LaunchedEffect(isFocused) { if (isFocused && !isMobile) onFocus() }

    if (isMobile) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(mobileBgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChannelItemContent(number, channel, isSelected, isFavorite, true, false, isPromo)
        }
    } else {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && isFocused) {
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
                number, channel, isSelected, isFavorite, false, isFocused, isPromo,
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
    // Animación suave de color de texto
    val titleColor by animateColorAsState(
        targetValue = if (isPromo && isFocused) Color.Black else Color.White,
        label = "title_color"
    )

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
                    style = if (isMobile) androidx.compose.material3.MaterialTheme.typography.bodyMedium else androidx.compose.material3.MaterialTheme.typography.titleLarge,
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
                    style = if (isMobile) androidx.compose.material3.MaterialTheme.typography.bodyLarge else androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = titleColor,
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
                style = if (isMobile) androidx.compose.material3.MaterialTheme.typography.labelSmall else androidx.compose.material3.MaterialTheme.typography.bodySmall,
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
                        startY = 200f
                    )
                )
        )

        // Animación suave de opacidad para el icono de play
        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF00B4D8).copy(0.2f)),
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
    // Transiciones de color suaves para las pestañas
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.08f),
        label = "tab_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color.White,
        label = "tab_text"
    )

    if (isMobile) {
        Box(
            modifier = modifier
                .height(36.dp)
                .widthIn(min = 80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = textColor,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
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
                containerColor = bgColor,
                focusedContainerColor = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.20f)
            ),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White))),
            interactionSource = interactionSource
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = name,
                    color = if (isFocused) Color.Black else textColor,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
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
                if (!mediaPlayer.isReleased) {
                    mediaPlayer.detachViews() // Limpiar antes de asignar
                    mediaPlayer.attachViews(this, null, true, false)
                }
            }
        },
        update = { vlcLayout ->
            if (!mediaPlayer.isReleased) {
                // Re-vincular vistas para asegurar que el vout se cree correctamente
                mediaPlayer.detachViews()
                mediaPlayer.attachViews(vlcLayout, null, true, false)
            }
        },
        onRelease = {
            if (!mediaPlayer.isReleased) {
                mediaPlayer.detachViews()
            }
        },
        modifier = modifier
    )
}

@Composable
fun AccountInfoCard(viewModel: MainViewModel) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val now = remember { Date() }

    val expiryDateText = viewModel.userExpiryDate?.let { sdf.format(it) } ?: "Sin fecha"
    val daysLeft = viewModel.userExpiryDate?.let {
        val diff = it.time - now.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1
        if (days < 0) 0 else days
    } ?: 0

    // Para la barra visual (Asumiendo 30 días como el 100% estándar de una mensualidad)
    val progress = (daysLeft.toFloat() / 30f).coerceIn(0f, 1f)
    val statusColor = if(daysLeft <= 3) Color.Red else if(daysLeft <= 7) Color(0xFFFFA500) else Color(0xFF00FF00)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ESTADO DE SUSCRIPCIÓN", color = Color(0xFF00B4D8), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vence el:", color = Color.White.copy(0.6f), fontSize = 13.sp)
                Text(expiryDateText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Días restantes:", color = Color.White.copy(0.6f), fontSize = 13.sp)
                Text("$daysLeft días", color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }

            // MEJORA: Barra de progreso visual sutil para los días restantes
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = statusColor,
                trackColor = Color.White.copy(0.1f)
            )
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
            delay(300)
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    // MEJORA: Se asegura de que el diálogo se comporte bien en TV
    Dialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = !isDownloading)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                            .heightIn(max = 120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()) // Añadido scroll por si el log es largo
                    ) {
                        Text(
                            text = config.changeLog,
                            color = Color.White.copy(alpha = 0.8f),
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
                            // CORRECCIÓN AQUÍ: buttonColors y shape directo
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("MÁS TARDE", color = Color.White)
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .focusRequester(focusRequester),
                            // CORRECCIÓN AQUÍ
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            shape = RoundedCornerShape(12.dp)
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
            "marquee" -> MarqueeMessage(message.message)
            "popup" -> PopupMessage(message.message, onDismiss)
        }
    }
}

@Composable
fun MarqueeMessage(text: String) {
    // MEJORA: Eliminado todo el código complejo de Layout. basicMarquee es nativo y súper optimizado.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Black.copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF00B4D8).copy(0.5f))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.CenterStart // Alineado a la izquierda para el inicio del marquee
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            maxLines = 1,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                velocity = 80.dp // Velocidad amigable para la vista
            )
        )
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
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 600.dp)
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
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp).focusRequester(focusRequester),
                    // CORRECCIÓN AQUÍ
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}