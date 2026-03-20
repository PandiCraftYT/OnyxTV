package com.example.onyxapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OnyxAppTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = OnyxBlue,
        secondary = OnyxDarkBlue,
        tertiary = OnyxGold,
        background = OnyxBlack,
        surface = OnyxSurface,
        onPrimary = OnyxBlack,
        onSurface = androidx.compose.ui.graphics.Color.White
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
