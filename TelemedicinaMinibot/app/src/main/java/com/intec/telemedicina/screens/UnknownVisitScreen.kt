package com.intec.telemedicina.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.intec.telemedicina.R
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun UnknownVisitScreen(navController: NavController, mqttViewModel: MqttViewModel, numericPanelViewModel: NumericPanelViewModel, robotManager: RobotManager){

    val text by mqttViewModel.capturedText.observeAsState("")

    LaunchedEffect(key1 = "UnknownVisitScreen"){
        mqttViewModel.robotMan.questionPrueba()
        mqttViewModel.listenToSpeechResult()
    }

    FuturisticGradientBackground {
        VoiceIcon(text)
    }

}

@Composable
fun VoiceIcon(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        /*if (text != "") {
            // Mostrar el texto que se está entendiendo
            Text(text)
        } else {
            // Mostrar el icono personalizado de voz
            Image(
                painter = painterResource(id = R.drawable.voice),
                contentDescription = "Voice Icon"
            )
        }*/
        Text(text)
    }

}

@Preview(showBackground = true, )
@Composable
fun UnknownVisitScreenPreview() {
    FuturisticGradientBackground {

    }
}