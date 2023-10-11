package com.intec.telemedicina.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.compose.md_theme_light_onBackground

@Composable
fun FuturisticGradientBackground(content: @Composable () -> Unit) {
    // Definimos los colores del gradiente
    val gradientColors = listOf(
        Color(0xFFFFFAF0),  // Crema más claro
        Color(0xFFFFF5E1),  // Crema base
        Color(0xFFFFEBCD)   // Crema ligeramente más oscuro
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
