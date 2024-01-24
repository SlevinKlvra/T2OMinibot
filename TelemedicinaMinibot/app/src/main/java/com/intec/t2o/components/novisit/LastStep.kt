package com.intec.t2o.components.novisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.intec.t2o.components.TransparentButtonWithIcon
import com.intec.t2o.viewmodels.MqttViewModel

@Composable
fun LastStep(mqttViewModel: MqttViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Muchas gracias por su visita",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Su información ha sido enviada. Nos pondremos en contacto con usted lo más rápido posible.",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .padding(horizontal = 85.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            TransparentButtonWithIcon(
                text = "Volver",
                icon = Icons.Outlined.ArrowBack,
                onClick = { mqttViewModel.navigateToHomeScreen() }
            )
        }
    }
}