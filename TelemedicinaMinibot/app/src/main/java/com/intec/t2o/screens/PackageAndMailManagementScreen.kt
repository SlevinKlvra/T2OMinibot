package com.intec.t2o.screens

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.intec.t2o.components.ButtonCard
import com.intec.t2o.components.DrivingComposable
import com.intec.t2o.components.GoBackButton
import com.intec.t2o.components.NumericPad
import com.intec.t2o.components.PressableEyes
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.ui.theme.textColor
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel

@Composable
fun PackageAndMailManagementScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel
) {
    Log.d("Current Screen", "PackageAndMailManagementScreen")

    val shouldCheckCode = remember { mutableStateOf(false) }
    val isCodeCorrect by numericPanelViewModel.isCodeCorrect.collectAsState()

    var hasCode by remember { mutableStateOf(false) }

    // Usar estados del ViewModel
    val messageIndex by mqttViewModel.messageIndexState
    val currentPage by mqttViewModel.currentPageState

    val totalPages = 3

    var showDrivingComposable by remember { mutableStateOf(false) }


    val meetingInfo = numericPanelViewModel.collectedMeetingInfo.value

    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    LaunchedEffect(messageIndex) {
        when (messageIndex) {
            1 -> {
                Log.d("SECUENCIA", "$messageIndex: ¿Dispone de código de entrega?")
                mqttViewModel.speak(
                    "¿Dispone de código de entrega?",
                    false){
                    // Acciones a realizar antes de hablar
                    Log.d("NumericPanelScreen Speak Finished", "Speak finished")
                }
            }

            2 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Por favor, introduzca el código que se le ha proporcionado"
                )
                mqttViewModel.speak(
                    "Por favor, introduzca el código que se le ha proporcionado",
                    false){
                    // Acciones a realizar antes de hablar
                }
            }

            3 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Acompáñeme a la sección de mensajería para depositar el paquete"
                )
                mqttViewModel.speak(
                    "Acompáñeme a la sección de mensajería para depositar el paquete",
                    false)
                {
                    Log.d("PackageAndMailManagement Speak Finished", "Acompáñeme a la sección de mensajería para depositar el paquete")
                    mqttViewModel.startNavigation("correo"){
                        Log.d("PackageAndMailManagement Navigation Finished", "navigation ended")
                    }
                }
            }

            4 -> {
                Log.d("SECUENCIA", "$messageIndex: VACIO")
            }

            5 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería"
                )
                mqttViewModel.speak(
                    "Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería",
                    false){
                    Log.d("PackageAndMailManagement Speak Finished", "Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería")
                    mqttViewModel.setMessageIndex(3)
                }
            }

            6 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado. Espere por favor"
                )
                mqttViewModel.speak(
                    "Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado. Espere por favor",
                    false){
                    mqttViewModel.speak(
                        "Notificación enviada.",
                        false){
                        Log.d("PackageAndMailManagement Speak Finished", "Notificación enviada.")
                        mqttViewModel.setMessageIndex(3)
                    }
                }
            }

            7 -> {
                Log.d(
                    "SECUENCIA",
                    "$messageIndex: acasa"
                )
                mqttViewModel.speak(
                    "Hemos llegado. Puede depositar el paquete aquí. Yo vuelvo a mi puesto. Muchas gracias.",
                    false)
                {
                    Log.d("PackageAndMailManagement Speak Finished", "Hemos llegado. Puede depositar el paquete aquí. Yo vuelvo a mi puesto. Muchas gracias.")
                    mqttViewModel.setReturningHome(true)
                    mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                }
            }
        }
    }

    LaunchedEffect(shouldCheckCode.value) {
        if (shouldCheckCode.value) {
            Log.d(
                "NumericPad",
                "Before API Call - enteredCode: ${numericPanelViewModel.enteredCode.value}"
            )
            numericPanelViewModel.checkForTaskExecution()
            shouldCheckCode.value = false
        }
    }

    LaunchedEffect(isCodeCorrect) {
        if (isCodeCorrect) {
            mqttViewModel.setMessageIndex(5)
        }
    }

    LaunchedEffect(true) {
        mqttViewModel.setMessageIndex(1)
        mqttViewModel.setCurrentPage(1)
    }

    mqttViewModel.setReturnDestinationDefaultValue()

    FuturisticGradientBackground {
        if (!(currentPage == 2 && !hasCode)) {
            if (currentPage != totalPages) {
                GoBackButton(onClick = {
                    if (currentPage > 1) {
                        mqttViewModel.setCurrentPage(currentPage - 1)
                    } else {
                        Log.d(
                            "return",
                            "to home screen and to default return pos: ${mqttViewModel.returnDestination.value}"
                        )
                        mqttViewModel.setReturningHome(true)
                        mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                    }
                })
            }
            when (currentPage) {
                1 -> {

                    Log.d("SECUENCIA T", "$messageIndex: ¿Dispone de código de entrega?")
                    Row(modifier = Modifier.padding(top = 50.dp)) {
                        ButtonsStep(
                            icon1 = Icons.Outlined.Check,
                            icon2 = Icons.Outlined.Clear,
                            title = "¿Dispone de código de entrega?",
                            subtitle = "Seleccione Sí o No para proceder con la confirmación de su código",
                            onButton1Click = {
                                mqttViewModel.setCurrentPage(2); hasCode =
                                true; mqttViewModel.setMessageIndex(2)
                            },
                            onButton2Click = {
                                mqttViewModel.setCurrentPage(2); hasCode =
                                false; mqttViewModel.setMessageIndex(3)
                            })
                    }
                }

                2 -> {
                    Log.d(
                        "SECUENCIA T",
                        "$messageIndex: Por favor, introduzca el código que se le ha proporcionado"
                    )
                    if (hasCode) {
                        NumericPad(
                            numericPanelViewModel = numericPanelViewModel,
                            onClick = { shouldCheckCode.value = true },
                            titleText = "Por favor, introduce el código de entrega"
                        )
                    }
                }

                3 -> {
                    Log.d(
                        "SECUENCIA T",
                        "$currentPage: Acompáñeme a la sección de mensajería para depositar el paquete"
                    )
                }

                4 -> {
                    Log.d("SECUECIA T", "$currentPage: VACIO")
                }

                5 -> {
                    Log.d(
                        "SECUENCIA T",
                        "$currentPage: Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería"
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        NotificationStep()
                    }
                }

                6 -> {
                    Log.d(
                        "SECUENCIA T",
                        "$currentPage: Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería"
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
                                    .width(100.dp)
                                    .height(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    Log.d(
        "SECUENCIA DRIVING",
        "CURRENT PAGE: $currentPage, HAS CODE: $hasCode, MESSAGE INDEX: $messageIndex"
    )
    if ((currentPage == 2 && !hasCode) || messageIndex == 3) {
        Log.d("SECUENCIA DRIVING", "PRESSABLE EYES")
        PressableEyes(
            modifier = Modifier.fillMaxSize(),
            onClick = {
                mqttViewModel.detenerNavegacion()
                showDrivingComposable = true
            }
        )
    }
    if (showDrivingComposable) {
        DrivingComposable(
            navController = navController,
            mqttViewModel = mqttViewModel,
            onCancel = {
                mqttViewModel.setReturningHome(true)
                showDrivingComposable = false
            },
            onContinue = {
                mqttViewModel.currentNavigationContext.value = MqttViewModel.NavigationState.PackageAndMailManagementScreen
                mqttViewModel.reanudarNavegacion()
                showDrivingComposable = false
            }
        )
    }
}


@Composable
fun ButtonsStep(
    icon1: ImageVector,
    icon2: ImageVector,
    title: String,
    subtitle: String,
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = subtitle,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ButtonCard(
                text = "Sí tengo código", icon = icon1, onClick = onButton1Click
            )
            ButtonCard(
                text = "No tengo código", icon = icon2, onClick = onButton2Click
            )
        }
    }
}

@Composable
fun NotificationStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Código introducido correctamente",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Por favor, acompáñeme a la sección de mensajería.",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            imageVector = Icons.Filled.MailOutline,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
