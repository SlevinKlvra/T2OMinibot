package com.intec.telemedicina.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.intec.telemedicina.ui.theme.md_theme_dark_primary
import com.intec.telemedicina.ui.theme.md_theme_light_inversePrimary
import com.intec.telemedicina.ui.theme.md_theme_light_primary
import com.intec.telemedicina.ui.theme.md_theme_light_primaryContainer
import com.intec.telemedicina.ui.theme.md_theme_light_secondary
import com.intec.telemedicina.ui.theme.md_theme_light_tertiary
import com.intec.telemedicina.ui.theme.seed

@Composable
fun FuturisticGradientBackground(content: @Composable () -> Unit) {
    // Definimos los colores del gradiente
    val gradientColors = listOf(
        seed,  // Azul principal
        md_theme_dark_primary,

        //md_theme_light_primaryContainer  // Terciario para añadir variedad
    )

    // Crear el gradiente
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),  // Comienza en la esquina superior izquierda
        end = Offset(1000f, 1000f)  // Termina en la esquina inferior derecha
    )

    // Aplicamos el gradiente al fondo y pasamos el contenido
    Box(modifier = Modifier
        .fillMaxSize()
        .background(brush = brush)) {
        content()
    }
}

@Preview
@Composable
fun FuturisticGradientBackgroundPreview() {
    FuturisticGradientBackground {}
}