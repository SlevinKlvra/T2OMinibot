package com.intec.t2o.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.t2o.R
import com.intec.t2o.components.DrivingComposable
import com.intec.t2o.components.PressableEyes
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.ui.theme.textColor
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.runBlocking

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MeetingScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel
) {
    Log.d("Current Screen", "MeetingScreen")

    val isWorking by remember { mutableStateOf(false) }

    var recompositionTrigger by remember { mutableStateOf(false) }

    val meetingInfo = numericPanelViewModel.collectedMeetingInfo.value

    // Usar estados del ViewModel
    val messageIndex by mqttViewModel.messageIndexState
    val currentPage by mqttViewModel.currentPageState

    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {

                add(GifDecoder.Factory())
            }
        }
        .build()

    var showDrivingComposable by remember { mutableStateOf(false) }

    mqttViewModel.setReturnDestinationDefaultValue()

    // Cambiar el mensaje después de un retraso
    LaunchedEffect(messageIndex, recompositionTrigger) {
        when (messageIndex) {
            0 -> {
                Log.d("SECUENCIA", "0: Hola, ${meetingInfo.visitante}")
                mqttViewModel.speak(
                    "Hola, ${meetingInfo.visitante}",
                    false)
                {
                    Log.d("SECUENCIA SPEAK FINISHED", "0: Hola, ${meetingInfo.visitante}")
                    mqttViewModel.setMessageIndex(1)
                    mqttViewModel.setCurrentPage(1)
                }
            }

            1 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Estoy notificando a ${meetingInfo.anfitrion} de tu llegada"
                )
                mqttViewModel.speak(
                    "Estoy notificando a ${meetingInfo.anfitrion} de tu llegada",
                    false
                )
                {
                    // Acciones a realizar después de hablar
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: Estoy notificando a ${meetingInfo.anfitrion} de tu llegada")
                    mqttViewModel.setMessageIndex(2)
                    mqttViewModel.setCurrentPage(2)
                }
            }

            2 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: He notificado a ${meetingInfo.anfitrion} de tu llegada."
                )
                mqttViewModel.speak(
                    "He notificado a ${meetingInfo.anfitrion} de tu llegada.",
                    false){
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: He notificado a ${meetingInfo.anfitrion} de tu llegada.")
                    mqttViewModel.setMessageIndex(5)
                    mqttViewModel.setCurrentPage(5)
                }
            }

            3 -> {
                Log.d("SECUENCIA", "$messageIndex: TRUE: A 5, FALSE A 6")
                // Nada aquí. La navegación se inicia cuando isNavigationComplete cambia
            }

            4 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias"
                )
                mqttViewModel.speak(
                    "Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias. Vuelvo a mi puesto.",
                    false
                )
                {
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias")
                    mqttViewModel.setReturningHome(true)
                    mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                }
            }

            5 -> {

                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado."
                )
                mqttViewModel.speak(
                    "La reunión está a punto de comenzar. Acompáñeme a la sala que se le ha asignado.",
                    false){
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: Veo que ha llegado puntual. Acompáñeme a la sala que se le ha asignado.")
                    mqttViewModel.setMessageIndex(7)
                    mqttViewModel.setCurrentPage(6)
                }
            }

            6 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Todavía no es la hora establecida para la reunión. Por favor, tome asiento. En breves instantes vendrán a buscarle"
                )
                mqttViewModel.speak(
                    "Actualmente la sala está ocupada. Por favor, tome asiento. En breves instantes vendrán a buscarle, muchas gracias.",
                    false
                )
                {
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: Todavía no es la hora establecida para la reunión. Por favor, tome asiento. En breves instantes vendrán a buscarle")
                    mqttViewModel.setReturningHome(true)
                    mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                }
            }

            7 -> {
                mqttViewModel.startNavigation(meetingInfo.puntomapa){
                    Log.d("SECUENCIA NAVIGATION FINISHED", "7: Navegación finalizada")
                    mqttViewModel.speak(
                        "Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias.",
                        false
                    )
                    {
                        Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: Hemos llegado. Tome asiento y en breves momentos comenzará la reunión. Muchas gracias.")
                        mqttViewModel.setReturningHome(true)
                        mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                    }
                }
            }

            8 -> {
                mqttViewModel.setReturningHome(true)
                // Acciones a realizar después de hablar
                mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
            }

            9 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: La sala está ocupada. Acompáñeme a la sala de espera."
                )
                mqttViewModel.speak(
                    "Actualmente la sala asignada para la reunión está ocupada. Acompáñeme a la sala de espera.",
                    false){
                    Log.d("SECUENCIA SPEAK FINISHED", "$messageIndex: La sala está ocupada. Acompáñeme a la sala de espera.")
                    mqttViewModel.setMessageIndex(10)
                    mqttViewModel.setCurrentPage(6)
                }
            }

            10 -> mqttViewModel.startNavigation("sala de espera"){
                Log.d("SECUENCIA NAVIGATION FINISHED", "10: Navegación a sala de espera finalizada")
            }
        }
    }

    FuturisticGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (currentPage) {
                0 -> {
                    Log.d("SECUENCIA T", "$messageIndex: Hola, ${meetingInfo.visitante}")
                    Text(
                        color = Color.White,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        text = "Hola, ${meetingInfo.visitante}"
                    )
                }

                1 -> {
                    Log.d(
                        "SECUENCIA T",
                        "$messageIndex: Estoy notificando a ${meetingInfo.anfitrion} de tu llegada"
                    )
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
                                    .width(150.dp)
                                    .height(150.dp)
                            )
                        }
                    }
                }

                2 -> {

                    Log.d(
                        "SECUENCIA T",
                        "$messageIndex: He notificado a ${meetingInfo.anfitrion} de tu llegada"
                    )
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                color = Color.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                text = "He notificado a ${meetingInfo.anfitrion} de tu llegada"
                            )
                            Spacer(modifier = Modifier.height(15.dp))
                            Text(
                                text = "Reunión programada a las ${meetingInfo.start_time}",
                                color = textColor,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }

    Log.d(
        "SECUENCIA DRIVING",
        "CURRENT PAGE: $currentPage, MESSAGE INDEX: $messageIndex, IS WORKING: $isWorking"
    )

    if (messageIndex == 4 || messageIndex == 5 || messageIndex == 6 || messageIndex == 7 || messageIndex == 8 || messageIndex == 9 || messageIndex == 10) {
        Log.d("SECUENCIA DRIVING", "$messageIndex: PressableEyes")
        PressableEyes(
            modifier = Modifier.fillMaxSize(),
            onClick = {
                mqttViewModel.detenerNavegacion()
                showDrivingComposable = true
            }
        )
    }

    if (showDrivingComposable) {
        Log.d("SECUENCIA DRIVING", "$messageIndex: DrivingComposable")
        DrivingComposable(
            navController = navController,
            mqttViewModel = mqttViewModel,
            onCancel = {
                mqttViewModel.setReturningHome(true)
                showDrivingComposable = false
            },
            onContinue = {
                mqttViewModel.currentNavigationContext.value = MqttViewModel.NavigationState.MeetingScreen
                mqttViewModel.reanudarNavegacion()
                recompositionTrigger = !recompositionTrigger
                showDrivingComposable = false
            }
        )
    }
}
