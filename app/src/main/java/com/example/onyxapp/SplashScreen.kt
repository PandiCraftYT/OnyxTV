package com.example.onyxapp

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
    var startAnimation by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 600
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(
            durationMillis = 1000, 
            easing = Easing { fraction -> OvershootInterpolator(1.5f).getInterpolation(fraction) }
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF00080B), Color(0xFF00141A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            Text(
                text = "ONYX TV",
                fontSize = if (isMobile) 48.sp else 80.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF00B4D8),
                        blurRadius = 25f
                    )
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "STREAMING PREMIUM",
                fontSize = if (isMobile) 10.sp else 14.sp,
                color = Color(0xFF00B4D8),
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isMobile) 4.sp else 8.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color(0xFF00B4D8),
                strokeWidth = 3.dp
            )
        }
    }
}
