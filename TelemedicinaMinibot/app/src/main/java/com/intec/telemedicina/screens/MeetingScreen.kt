package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    var messageIndex by remember { mutableStateOf(0) }

    val isNavigationComplete = mqttViewModel.isNavigationComplete.observeAsState()

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
        when (messageIndex) {
            0 -> {
                robotManager.speak("Hola, ${meetingInfo.visitante}", false)
                delay(3000L)
                messageIndex = 1
            }
            1 -> {
                robotManager.speak("Notificando a ${meetingInfo.anfitrion} de tu llegada", false)
                delay(5000L)
                messageIndex = 2
            }
            2 -> {
                robotManager.speak("He notificado a ${meetingInfo.anfitrion} de tu llegada. ", false)
                delay(8000L)
                messageIndex = 3
            }
            3 -> {
                // Nada aquí. La navegación se inicia cuando isNavigationComplete cambia
            }
            4 -> {
                robotManager.speak("Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias", false)
                delay(5000L)
                messageIndex = 7
            }
            5 -> {
                robotManager.speak("Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.", false)
                robotManager.startNavigation(1, meetingInfo.puntomapa, mqttViewModel.coordinateDeviation.value!!.toDouble(), mqttViewModel.navigationTimeout.value!!.toLong())
                messageIndex = 3
            }
            6 -> {
                robotManager.speak("Todavía no es la hora establecida para la reunión. Por favor, tome asiento. En breves instantes vendrán a buscarle", false)
                delay(5000L)
                messageIndex = 7
            }
            7 -> {
                robotManager.returnToPosition(mqttViewModel.returnDestination.value!!)
                // Considera agregar un delay o manejar cuando se debe cambiar a messageIndex 6 si es necesario
            }
        }
    }

    LaunchedEffect(isNavigationComplete.value) {
        if (isNavigationComplete.value == true) {
            messageIndex = 4
        }
    }

    LaunchedEffect(numericPanelViewModel.isMeetingTimeWithinThreshold()){
        if (numericPanelViewModel.isMeetingTimeWithinThreshold()) {
            messageIndex = 5
        } else {
            messageIndex = 6
        }
    }

    FuturisticGradientBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (messageIndex) {
                0 -> Text(color = Color.White,
                    fontSize = 65.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Hola, ${meetingInfo.visitante}"
                )
                1 -> {

                    Box{
                        Column (
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                color = Color.White,
                                fontSize = 45.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                text = "Estoy notificando a ${meetingInfo.anfitrion} de tu llegada"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(R.drawable.emailsend, imageEmotionsLoader),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(bottom = 1.dp) // Adjust the padding as needed
                                    .width(100.dp)
                                    .height(100.dp)
                            )
                        }
                    }
                }

                2 ->
                    Text(
                    color = Color.White,
                    fontSize = 65.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Te acompaño a la sala asignada"
                )

                3 -> Text(
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Yendo a ${meetingInfo.puntomapa}"
                )

                4 -> Text(
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Hemos llegado a ${meetingInfo.puntomapa}. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias"
                )

                5 -> Text(
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado."
                )

                6 -> Text(
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Todavía no es la hora establecida para la reunión. Por favor, espere en la sala de espera. En breves instantes vendrán a buscarle. Gracias"
                )

                7 -> Text(
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Regresando a ${mqttViewModel.returnDestination.value}"
                )
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