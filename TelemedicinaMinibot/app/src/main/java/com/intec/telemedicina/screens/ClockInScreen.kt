package com.intec.telemedicina.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun ClockInScreen(
    mqttViewModel: MqttViewModel,
    navController: NavController,
    numericPanelViewModel: NumericPanelViewModel
) {
    FuturisticGradientBackground {
        Text(text = "ClockInScreen")
    }
}