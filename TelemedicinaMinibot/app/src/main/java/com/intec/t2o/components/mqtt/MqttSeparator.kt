package com.intec.t2o.components.mqtt

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intec.t2o.ui.theme.iconColor

@Composable
fun MqttSeparator() {
    Spacer(modifier = Modifier.height(20.dp))
    Divider(color = iconColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(20.dp))
}