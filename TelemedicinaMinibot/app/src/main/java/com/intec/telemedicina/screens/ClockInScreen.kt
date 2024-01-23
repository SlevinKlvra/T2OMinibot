package com.intec.telemedicina.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.LoadingSpinner
import com.intec.telemedicina.components.NumericPad
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun ClockInScreen(
    mqttViewModel: MqttViewModel,
    navController: NavController,
    numericPanelViewModel: NumericPanelViewModel
) {

    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    FuturisticGradientBackground {
        if (isLoading) LoadingSpinner()
        GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
        NumericPad(
            numericPanelViewModel = numericPanelViewModel,
            onClick = { numericPanelViewModel.checkForTaskExecution() },
            titleText = "Introduce tu código de empleado"
        )
    }
}