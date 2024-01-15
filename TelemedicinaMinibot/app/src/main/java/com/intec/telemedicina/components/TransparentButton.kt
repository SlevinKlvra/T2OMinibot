package com.intec.telemedicina.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransparentButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick) // Acción al hacer clic
            .padding(horizontal = 38.dp, vertical = 10.dp) // Espaciado alrededor del botón
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Alinea el icono y el texto verticalmente
            verticalArrangement = Arrangement.Center // Alinea el contenido horizontalmente
        ) {

            Text(
                text = text,
                color = Color.White, // Color del texto
                fontSize = 25.sp, // Aumenta el tamaño de la fuente en un 50%
                fontWeight = FontWeight.Bold // Establece el texto en negrita

            )
        }
    }
}
