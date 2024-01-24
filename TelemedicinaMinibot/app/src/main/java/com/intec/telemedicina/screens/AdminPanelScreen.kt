package com.intec.telemedicina.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.NumericPad
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel


@Composable
fun AdminPanelScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel,
    robotManager: RobotManager
) {

    FuturisticGradientBackground {
        GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
        NumericPad(numericPanelViewModel = numericPanelViewModel, onClick = {
            if (numericPanelViewModel.checkForAdvancedSettingsAccess()
            ) {
                mqttViewModel.navigateToMqttScreen()
            } else {
                robotManager.speak(
                    "Código incorrecto",
                    false,
                    object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    })
            }
        }, titleText = "Código de acceso administrador")

    }
}


