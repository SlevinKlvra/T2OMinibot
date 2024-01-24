package com.intec.t2o.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.intec.t2o.ui.theme.md_theme_dark_primary
import com.intec.t2o.ui.theme.seed

@Composable
fun FuturisticGradientBackground(content: @Composable () -> Unit) {
    // Definimos los colores del gradiente
    val gradientColors = listOf(
        seed,  // Azul principal
        md_theme_dark_primary,
    )

    // Crear el gradiente
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),  // Comienza en la esquina superior izquierda
        end = Offset(1000f, 1000f)  // Termina en la esquina inferior derecha
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = brush)
    ) {
        content()
    }
}

@Preview
@Composable
fun FuturisticGradientBackgroundPreview() {
    FuturisticGradientBackground {}
}