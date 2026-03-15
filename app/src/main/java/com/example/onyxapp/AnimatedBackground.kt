package com.example.onyxapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    
    // Rotación lenta del gradiente - Período largo para que sea fluido
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val color1 = Color(0xFF00080B)
    val color2 = Color(0xFF00141A)
    val color3 = Color(0xFF00252E)
    val color4 = Color(0xFF00080B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color1)
    ) {
        // Lienzo para el gradiente rotativo - Escalado por 3x para evitar que los bordes se vean al rotar
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { 
                rotationZ = angle
                scaleX = 3.0f
                scaleY = 3.0f
            }
        ) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(color1, color2, color3, color4, color1),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                size = size
            )
        }
        
        // Pulso de "luces" de fondo con gradientes radiales más suaves y de mayor radio
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Círculo 1 (Arriba Izquierda) - Transición más suave con múltiples stops
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF00B4D8).copy(alpha = alpha),
                    0.6f to Color(0xFF00B4D8).copy(alpha = alpha * 0.4f),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 1.2f // Mucho más grande para evitar cortes bruscos
                )
            )
            // Círculo 2 (Abajo Derecha)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF0077B6).copy(alpha = alpha * 0.8f),
                    0.5f to Color(0xFF0077B6).copy(alpha = alpha * 0.3f),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                    radius = size.width * 1.5f
                )
            )
        }
    }
}
