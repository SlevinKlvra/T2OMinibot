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
                Log.d("SECUENCIA", "$messageIndex: Hola, ${meetingInfo.visitante}")
                robotManager.speak("Hola, ${meetingInfo.visitante}", false, object : RobotManager.SpeakCompleteListener {
                    override fun onSpeakComplete() {
                        // Acciones a realizar después de hablar
                    }
                })
                delay(3000L)
                messageIndex = 1
            }

            1 -> {
                Log.d("SECUENCIA", "$messageIndex: Estoy notificando a ${meetingInfo.anfitrion} de tu llegada")
                robotManager.speak("Notificando a ${meetingInfo.anfitrion} de tu llegada", false, object : RobotManager.SpeakCompleteListener {
                    override fun onSpeakComplete() {
                        // Acciones a realizar después de hablar
                    }
                })
                delay(5000L)
                messageIndex = 2
            }

            2 -> {
                Log.d("SECUENCIA", "$messageIndex: He notificado a ${meetingInfo.anfitrion} de tu llegada.")
                robotManager.speak(
                    "He notificado a ${meetingInfo.anfitrion} de tu llegada. ",
                    false,
                    object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    }
                )
                delay(8000L)
                messageIndex = 3
            }

            3 -> {
                Log.d("SECUENCIA", "$messageIndex: TRUE: A 5, FALSE A 6")
                if(numericPanelViewModel.isMeetingTimeWithinThreshold()){
                    messageIndex = 5
                } else {
                    messageIndex = 6
                }
                // Nada aquí. La navegación se inicia cuando isNavigationComplete cambia
            }

            4 -> {
                Log.d("SECUENCIA", "$messageIndex: Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias")
                robotManager.speak(
                    "Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias",
                    false,
                    object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    }
                )
                delay(5000L)
                messageIndex = 7
            }

            5 -> {

                Log.d("SECUENCIA", "$messageIndex: Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.")
                robotManager.speak(
                    "Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.",
                    false,
                        object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    }
                )
                mqttViewModel.setDrivingState()
                robotManager.startNavigation(
                    1,
                    meetingInfo.puntomapa,
                    mqttViewModel.coordinateDeviation.value!!.toDouble(),
                    mqttViewModel.navigationTimeout.value!!.toLong(), navigationCompleteListener = object :
                        RobotManager.NavigationCompleteListener {
                        override fun onNavigationComplete() {
                            // Acciones a realizar después de hablar
                            robotManager.speak(
                                "Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Yo vuelvo a mi puesto. Muchas gracias",
                                false,
                                object : RobotManager.SpeakCompleteListener {
                                    override fun onSpeakComplete() {
                                        // Acciones a realizar después de hablar
                                        robotManager.returnToPosition(mqttViewModel.returnDestination.value!!)
                                    }
                                }
                            )
                        }
                    })
                messageIndex = 8
            }

            6 -> {
                Log.d("SECUENCIA", "$messageIndex: Todavía no es la hora establecida para la reunión. Por favor, tome asiento. En breves instantes vendrán a buscarle")
                robotManager.speak(
                    "Todavía no es la hora establecida para la reunión. Por favor, tome asiento. En breves instantes vendrán a buscarle",
                    false,
                    object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    }
                )
                delay(5000L)
                messageIndex = 7
            }

            7 -> {
                robotManager.returnToPosition(mqttViewModel.returnDestination.value!!)
                // Considera agregar un delay o manejar cuando se debe cambiar a messageIndex 6 si es necesario
            }

            8 -> {

            }
        }
    }

    LaunchedEffect(isNavigationComplete.value) {
        if (messageIndex == 5) {
            messageIndex = 4
        }
    }

    FuturisticGradientBackground {
        Box(modifier = Modifier
            .fillMaxSize()
            .height(65.dp), contentAlignment = Alignment.Center) {
            when (messageIndex) {
                0 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Hola, ${meetingInfo.visitante}")
                    Text(
                    color = Color.White,
                    fontSize = 65.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    text = "Hola, ${meetingInfo.visitante}"
                    )
                }

                1 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Estoy notificando a ${meetingInfo.anfitrion} de tu llegada")
                    Box {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                color = Color.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                text = "Estoy notificando a ${meetingInfo.anfitrion} de tu llegada"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(
                                    R.drawable.emailsend,
                                    imageEmotionsLoader
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(bottom = 1.dp) // Adjust the padding as needed
                                    .width(100.dp)
                                    .height(100.dp)
                            )
                        }
                    }
                }

                2 -> {

                    Log.d("SECUENCIA T", "$messageIndex: He notificado a ${meetingInfo.anfitrion} de tu llegada")
                    Box {
                        Column {
                            Text(
                                color = Color.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                text = "He notificado a ${meetingInfo.anfitrion} de tu llegada"
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Reunión programada a las ${meetingInfo.start_time}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }

                }

                3 -> {

                }
                4 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Yendo a ${meetingInfo.puntomapa}")
                    Text(
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        text = "Hemos llegado a ${meetingInfo.puntomapa}. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias"
                    )
                }

                5 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.")
                    Text(
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        text = "Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        text = "Yendo a ${meetingInfo.puntomapa}"
                    )
                }

                6 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Todavía no es la hora establecida para la reunión. Por favor, espere en la sala de espera. En breves instantes vendrán a buscarle. Gracias")
                    Text(
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        text = "Todavía no es la hora establecida para la reunión. Por favor, espere en la sala de espera. En breves instantes vendrán a buscarle. Gracias"
                    )
                }

                7 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Regresando a ${mqttViewModel.returnDestination.value}")
                    Text(
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
}
