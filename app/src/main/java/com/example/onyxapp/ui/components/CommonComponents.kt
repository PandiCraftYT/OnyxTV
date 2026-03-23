package com.example.onyxapp.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.example.onyxapp.Channel
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isPromo) 60.dp else 70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPromo) Color(0xFF00B4D8).copy(alpha = 0.3f)
                    else if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.03f)
                )
                .border(
                    width = if (isPromo || isSelected) 1.dp else 0.dp,
                    color = if (isPromo) Color(0xFF00B4D8) else if (isSelected) Color(0xFF00B4D8).copy(0.5f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            ChannelItemContent(number, channel, isSelected, isFavorite, true, false, isPromo)
        }
    } else {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isPromo) 65.dp else 75.dp)
                .onPreviewKeyEvent { event ->
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
            Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                ChannelItemContent(number, channel, isSelected, isFavorite, false, isFocused, isPromo)
            }
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
    isPromo: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!isPromo) {
            Text(
                text = String.format(Locale.getDefault(), "%03d", number),
                style = if (isMobile) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
                color = if (isSelected) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(if (isMobile) 12.dp else 20.dp))
            
            if (channel.logo != null) {
                Box(
                    modifier = Modifier
                        .size(if (isMobile) 44.dp else 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(if (isMobile) 12.dp else 20.dp))
            }
        } else {
            // ICONO DE ESTRELLA PARA EL PROMO
            Icon(
                Icons.Default.Star, 
                null, 
                tint = if (isFocused) Color.Black else Color(0xFF00B4D8),
                modifier = Modifier.size(if (isMobile) 24.dp else 30.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = if (isMobile) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                color = if (isPromo && isFocused) Color.Black else Color.White,
                fontWeight = if (isPromo) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!isPromo) {
                Text(
                    text = (channel.group ?: "General").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            } else {
                Text(
                    text = "HAZTE PREMIUM AHORA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFocused) Color.Black.copy(0.7f) else Color(0xFF00B4D8),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isFavorite && !isPromo) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(if (isMobile) 16.dp else 20.dp))
        }
        
        if (isFocused && !isMobile) {
            Icon(
                Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = if (isPromo) Color.Black else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryTab(name: String, isSelected: Boolean, isMobile: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
                mediaPlayer.attachViews(this, null, false, false)
            }
        },
        update = { vlcLayout ->
            if (!mediaPlayer.vlcVout.areViewsAttached()) {
                mediaPlayer.detachViews()
                mediaPlayer.attachViews(vlcLayout, null, false, false)
            }
        },
        onRelease = {
            mediaPlayer.detachViews()
        },
        modifier = modifier
    )
}
