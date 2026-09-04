package com.example.babyshop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PinkDark,
    secondary = Mint,
    background = Cream,
    surface = Cream,
    onPrimary = Cream,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun BabyShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
