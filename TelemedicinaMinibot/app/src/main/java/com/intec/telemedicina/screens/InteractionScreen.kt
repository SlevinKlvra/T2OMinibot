package com.intec.telemedicina.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun InteractionScreen(navController: NavController, mqttViewModel : MqttViewModel){
    FuturisticGradientBackground {

        // Botón de retorno
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .size(56.dp)
                .padding(16.dp),
            contentColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
    }
}

@Composable
fun CenteredTextScreen(text: String) {
    Box(
        contentAlignment = Alignment.Center, // Centra el contenido en la caja
        modifier = Modifier.fillMaxSize() // Hace que la caja ocupe toda la pantalla
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center, // Centra el texto horizontalmente
            modifier = Modifier.padding(16.dp), // Añade un poco de padding
            fontSize = 20.sp, // Puedes ajustar el tamaño del texto según necesites
            maxLines = 2, // Ajusta el número de líneas máximas
            overflow = TextOverflow.Ellipsis // Añade puntos suspensivos si el texto es demasiado largo
        )
    }
}