package com.intec.telemedicina.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun MeetingScreen(navController : NavController, mqttViewModel : MqttViewModel, numericPanelViewModel : NumericPanelViewModel, robotManager : RobotManager){
    val meetingInfo = numericPanelViewModel.collectedMeetingInfo.value // Directamente accede al valor

    // Preparar el mensaje para el robot y el texto en pantalla
    val message = if (meetingInfo.id != 0) {
        "Hola, ${meetingInfo.nombre}. Veo que tienes programada una reunión a las ${meetingInfo.start_time}."
    } else {
        "Bienvenido. No tengo información sobre reuniones próximas."
    }

    // Efecto para activar el speak del robot
    LaunchedEffect(meetingInfo.id) {
        robotManager.speak(message, false)
    }

    FuturisticGradientBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                text = message,
                color = Color.White
            )
        }
    }
}

/*@Preview(showBackground = true, name = "10-inch Tablet Landscape", widthDp = 960, heightDp = 600)
@Composable
fun MeetingScreenPreview() {
    MeetingScreen()
}*/

/*@Composable
fun MeetingScreen(navController: NavController, mqttViewModel: MqttViewModel, numericPanelViewModel: NumericPanelViewModel, robotManager: RobotManager) {

    val meetingInfo = numericPanelViewModel.collectedMeetingInfo.value // Directamente accede al valor

    FuturisticGradientBackground {
        if (meetingInfo.start_time.isNotEmpty()) {
            //if (numericPanelViewModel.isMeetingTimeWithinThreshold()) {
            if (true) {
                // Lógica para dirigir al cliente a la sala asignada
                Text(textAlign = TextAlign.Center,text = "Bienvenid@, Sergio")
                robotManager.speak("Su código ha sido verificado. He notificado a ${meetingInfo.nombre} de su llegada. Veo que ha sido puntual. ¿Quiere que le acompañe a la sala?", true)
            } else {
                // Lógica para dirigir al cliente a la sala de espera
                Text("Por favor, diríjase a la sala de espera")
            }
        } else {
            // Lógica para dirigir al cliente a la sala de espera
            Text("Por favor, diríjase a la sala de espera")
        }
    }
}*/