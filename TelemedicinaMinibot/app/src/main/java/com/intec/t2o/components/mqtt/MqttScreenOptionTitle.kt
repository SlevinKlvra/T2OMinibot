package com.intec.t2o.components.mqtt

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MqttScreenOptionTitle(title: String, subtitle: String){
    Text(
        title,
        color = Color.White,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        subtitle,
        color = Color.White,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(20.dp))
}