package com.intec.telemedicina.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransparentButtonWithIconAndText(
    text: String,
    indicacion: String,
    icon: ImageVector,
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
            Icon(
                imageVector = icon,
                contentDescription = null, // Descripción del contenido para accesibilidad
                tint = Color.White, // Color del icono
                modifier = Modifier.size(100.dp) // Aumenta el tamaño del icono en un 50%
            )
            Spacer(Modifier.width(2.dp)) // Espacio entre el icono y el texto
            Text(
                text = text,
                color = Color.White, // Color del texto
                fontSize = 15.sp, // Aumenta el tamaño de la fuente en un 50%
                fontWeight = FontWeight.Bold // Establece el texto en negrita
            )
            Spacer(Modifier.width(4.dp)) // Espacio entre el icono y el texto
            Text(
                text = "Diga: ",
                color = Color.White, // Color del texto
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp, // Aumenta el tamaño de la fuente en un 50%
                textAlign = TextAlign.Center
            )
            Text(
                text = indicacion,
                color = Color.White, // Color del texto
                fontSize = 10.sp, // Aumenta el tamaño de la fuente en un 50%
                lineHeight = 14.sp,
                fontStyle = FontStyle.Italic, // Establece el texto en negrita
                textAlign = TextAlign.Center,
            )
        }
    }
}
