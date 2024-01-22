package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.ainirobot.base.analytics.utils.StringUtil.formatTime
import com.intec.telemedicina.R

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MeetingScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel,
    robotManager: RobotManager
) {
    Log.d("Current Screen", "MeetingScreen")

    val meetingInfo = numericPanelViewModel.collectedMeetingInfo.value
    var message = remember { mutableStateOf("") }

    var messageIndex by remember { mutableStateOf(0) }

    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    // Cambiar el mensaje después de un retraso
    LaunchedEffect(messageIndex) {
        Log.d("MeetingScreen", "Index: $messageIndex")
        when (messageIndex) {
            0 -> delay(3000L) // Esperar 3 segundos
            1 -> delay(5000L) // Esperar 5 segundos, ajusta este tiempo según sea necesario
            2 -> delay(8000L) // Esperar 5 segundos, ajusta este tiempo según sea necesario
            3 -> delay(8000L) // Esperar 5 segundos, ajusta este tiempo según sea necesario
        }
        if (messageIndex < 3) {
            messageIndex++
        } else if(messageIndex == 3){
            robotManager.startNavigation(1, meetingInfo.puntomapa, mqttViewModel.coordinateDeviation.value!!.toDouble(), mqttViewModel.navigationTimeout.value!!.toLong())
        }
    }

    // Observar el estado de navegación finalizada
    LaunchedEffect(true) {
        if (messageIndex == 3) {
            Log.d("MeetingScreen", "Navigation Finished and message 3")
            messageIndex++
        }
    }

    /*LaunchedEffect(meetingInfo) {
        if (meetingInfo != null && meetingInfo.id != 0) {
            Log.d("MeetingScreen", "MeetingInfo: Hola, ${meetingInfo.visitante}")
            robotManager.speak("Hola, ${meetingInfo.visitante}", false)

            // Espera unos segundos
            delay(3000)
            Log.d("DELAY", "DELAY")
            robotManager.speak("Notificando a ${meetingInfo.anfitrion} de tu llegada", false)
            Log.d("MeetingScreen", "MeetingInfo: Notificando a ${meetingInfo.anfitrion} de tu llegada")
            if (true) {
                Log.d("MeetingScreen", "He notificado a ${meetingInfo.anfitrion} de tu llegada, acompáñeme a la sala que se le ha asignado.")
                robotManager.speak("He notificado a ${meetingInfo.anfitrion} de tu llegada, acompáñeme a la sala que se le ha asignado.", false)
                robotManager.startNavigation(1, meetingInfo.puntomapa, mqttViewModel.coordinateDeviation.value!!.toDouble(), mqttViewModel.navigationTimeout.value!!.toLong())
                // Aquí podrías agregar lógica para cuando el robot llegue a la sala
            } else {

                message.value =  "Veo que no es el momento establecido para la reunión. He notificado a ${meetingInfo.anfitrion} de tu llegada, toma asiento y vendrán a buscarte pronto."
                Log.d("MeetingScreen", "MeetingInfo: Veo que no es el momento establecido para la reunión. He notificado a ${meetingInfo.anfitrion} de tu llegada, toma asiento y vendrán a buscarte pronto.")
            // Considera agregar un temporizador antes de reiniciar la secuencia
            }
        }
    }*/


    FuturisticGradientBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            when (messageIndex) {
                0 -> {
                    Text("Hola, ${meetingInfo.visitante}")
                    robotManager.speak("Hola, ${meetingInfo.visitante}", false)
                }
                1 -> {
                    Log.d("MeetingScreen", "MeetingInfo step 1: Notificando a ${meetingInfo.anfitrion} de tu llegada")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        robotManager.speak("Notificando a ${meetingInfo.anfitrion} de tu llegada", false)
                        Text("Notificando a ${meetingInfo.anfitrion} de tu llegada")
                        // Asegúrate de tener un gif adecuado y de implementar GifImage
                        Image(
                            painter = rememberAsyncImagePainter(R.drawable.emailsend,imageEmotionsLoader),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                2 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (true) {
                            Log.d("MeetingScreen", "MeetingInfo step 2 He notificado a ${meetingInfo.anfitrion} de tu llegada, acompáñeme a la sala que se le ha asignado.")
                            robotManager.speak("He notificado a ${meetingInfo.anfitrion} de tu llegada. Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.", false)

                            Text("He notificado a ${meetingInfo.anfitrion} de tu llegada, acompáñeme a la sala que se le ha asignado.")

                            // Aquí podrías agregar lógica para cuando el robot llegue a la sala
                        } else {
                            message.value =  "Veo que no es el momento establecido para la reunión. He notificado a ${meetingInfo.anfitrion} de tu llegada, toma asiento y vendrán a buscarte pronto."
                            Log.d("MeetingScreen", "MeetingInfo: Veo que no es el momento establecido para la reunión. He notificado a ${meetingInfo.anfitrion} de tu llegada, toma asiento y vendrán a buscarte pronto.")
                            // Considera agregar un temporizador antes de reiniciar la secuencia
                        }
                    }
                }
                3 -> {
                    Log.d("MeetingScreen", "MeetingInfo step 3: ${meetingInfo.puntomapa}")
                    Text("Navegando a ${meetingInfo.puntomapa}")
                }
                4 -> {
                    Log.d("MeetingScreen", "MeetingInfo step 4: Hemos llegado a ${meetingInfo.puntomapa}. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias")
                    Text("Hemos llegado a ${meetingInfo.puntomapa}. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias")
                }
            }
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