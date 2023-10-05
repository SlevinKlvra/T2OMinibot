package com.intec.telemedicina.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun FuturisticGradientBackground(content: @Composable () -> Unit) {
    // Definimos los colores del gradiente
    val gradientColors = listOf(
        Color(0xFF1A1A1A),  // Gris oscuro
        Color(0xFF2E2E2E),  // Gris medio
        Color(0xFF4D4D4D)   // Gris claro
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