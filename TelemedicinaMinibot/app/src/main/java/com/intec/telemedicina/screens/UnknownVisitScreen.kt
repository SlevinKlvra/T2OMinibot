package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

    Log.d("UnknownVisitScreen", "UnknownVisitScreen")
    val text by mqttViewModel.capturedText.collectAsState()
    Text("Captured Text: $text")

    val isListening by mqttViewModel.isListening.observeAsState(true)

    LaunchedEffect(isListening){
        mqttViewModel.robotMan.questionPrueba()
        mqttViewModel.listenToSpeechResult()
        if(text!=""){
            Log.d("UnknownVisitScreen", "Escuchando... $text")
            mqttViewModel.stopListening()
        }
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