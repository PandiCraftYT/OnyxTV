package com.example.onyxapp

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // 1. Estados separados para coreografía de animaciones
    var startLogoAnimation by remember { mutableStateOf(false) }
    var startSubtitleAnimation by remember { mutableStateOf(false) }
    var startLoaderAnimation by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 600

    // Animación exclusiva del Logo
    val logoAlpha by animateFloatAsState(
        targetValue = if (startLogoAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logo_alpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnimation) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = Easing { fraction -> OvershootInterpolator(1.2f).getInterpolation(fraction) } // Rebote sutil
        ),
        label = "logo_scale"
    )

    // El Orquestador
    LaunchedEffect(Unit) {
        startLogoAnimation = true
        delay(400) // Espera un poco...
        startSubtitleAnimation = true // ...Aparece el subtítulo
        delay(400) // Espera un poco más...
        startLoaderAnimation = true // ...Aparece el spinner

        delay(1500) // Tiempo para admirar la pantalla antes de entrar a la app
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF000508), Color(0xFF00141A)) // Fondo sutilmente más oscuro arriba
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // CONTENEDOR CENTRAL: Logo y Subtítulo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(logoAlpha)
                .scale(logoScale)
        ) {
            // Título con Gradiente Metálico/Cyan
            Text(
                text = "ONYX TV",
                fontSize = if (isMobile) 56.sp else 90.sp, // Ajuste de tamaño para más impacto
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFE0F7FA))
                    ),
                    shadow = Shadow(
                        color = Color(0xFF00B4D8).copy(alpha = 0.6f),
                        blurRadius = 30f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo con Fade Independiente
            AnimatedVisibility(
                visible = startSubtitleAnimation,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "STREAMING PREMIUM",
                    fontSize = if (isMobile) 11.sp else 16.sp,
                    color = Color(0xFF00B4D8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isMobile) 6.sp else 12.sp, // Estilo cinematográfico
                    textAlign = TextAlign.Center
                )
            }
        }

        // CONTENEDOR INFERIOR: Spinner (Estilo Netflix/Smart TV)
        AnimatedVisibility(
            visible = startLoaderAnimation,
            enter = fadeIn(animationSpec = tween(800)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isMobile) 60.dp else 100.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (isMobile) 36.dp else 48.dp),
                color = Color(0xFF00B4D8),
                strokeWidth = 3.dp
            )
        }
    }
}