package com.intec.telemedicina.components

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.navigation.NavController

@Composable
fun BackButton(navController: NavController?) {
    Layout(
        content = {
            IconButton(onClick = {
                navController?.navigateUp()
            }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)

        layout(placeable.width, placeable.height) {
            // Posicionar el botón en la esquina superior izquierda
            placeable.place(0, 0)
        }
    }
}
