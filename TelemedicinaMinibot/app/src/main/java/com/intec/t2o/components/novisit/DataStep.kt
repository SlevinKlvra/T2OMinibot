package com.intec.t2o.components.novisit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.intec.t2o.screens.UserData
import com.intec.t2o.ui.theme.textColor

@Composable
fun DataStep(userData: UserData) {
    Text(
        text = "¿Son estos datos correctos?",
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium
    )
    Text(
        text = "Pulse en \"Enviar\" para concertar una cita con los datos mostrados",
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Row {
        Text(
            text = "Nombre: ",
            color = textColor,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 10.dp)
        )
        Text(
            text = userData.nombre,
            color = Color.White,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
    }
    Row {
        Text(
            text = "Email: ",
            color = textColor,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 10.dp)
        )
        Text(
            text = userData.email,
            color = Color.White,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
    }
    Row {
        Text(
            text = "Empresa: ",
            color = textColor,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 10.dp)
        )
        Text(
            text = userData.empresa,
            color = Color.White,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
    }
    Row {
        Text(
            text = "Mensaje: ",
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 10.dp)
        )
        Text(
            text = userData.asunto,
            color = Color.White,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(bottom = 25.dp)
        )
    }
}