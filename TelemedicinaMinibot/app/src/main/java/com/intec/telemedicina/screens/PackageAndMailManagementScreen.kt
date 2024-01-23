package com.intec.telemedicina.screens

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.LoadingSpinner
import com.intec.telemedicina.components.NumericPad
import com.intec.telemedicina.components.TransparentButtonWithIcon
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.delay

@Composable
fun PackageAndMailManagementScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager,
    numericPanelViewModel: NumericPanelViewModel
) {
    Log.d("Current Screen", "PackageAndMailManagementScreen")
    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    val shouldCheckCode = remember { mutableStateOf(false) }
    var hasCode by remember { mutableStateOf(false) }

    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 3

    val isCodeCorrect by numericPanelViewModel.isCodeCorrect.collectAsState()

    var messageIndex by remember { mutableStateOf(1) }

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

    val isNavigationComplete = mqttViewModel.isNavigationComplete.observeAsState()

    LaunchedEffect(isNavigationComplete.value) {
        if (isNavigationComplete.value == true) {
            messageIndex = 4
        }
    }

    LaunchedEffect(messageIndex){
        when(messageIndex){
            1 -> {
                Log.d("SECUENCIA","$messageIndex: ¿Dispone de código de entrega?")
                robotManager.speak("¿Dispone de código de entrega?", false)
            }
            2 -> {

                Log.d("SECUENCIA","$messageIndex: Por favor, introduzca el código que se le ha proporcionado")
                robotManager.speak("Por favor, introduzca el código que se le ha proporcionado", false)
            }
            3 -> {
                Log.d("SECUENCIA","$messageIndex: Acompáñeme a la sección de mensajería para depositar el paquete")
                robotManager.speak("Acompáñeme a la sección de mensajería para depositar el paquete", false)
                robotManager.startNavigation(1, "correo", mqttViewModel.coordinateDeviation.value!!.toDouble(), mqttViewModel.navigationTimeout.value!!.toLong())
                messageIndex = 4
            }
            4 -> {
                Log.d("SECUENCIA","$messageIndex: VACIO")
            }
            5 -> {
                Log.d("SECUENCIA","$messageIndex: Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería")
                robotManager.speak("Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería", false)
            }
            6 -> {
                Log.d("SECUENCIA","$messageIndex: Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado. Espere por favor")
                robotManager.speak("Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado. Espere por favor", false)
                delay(5000L)
                robotManager.speak("Notificación enviada.", false)
                messageIndex = 3
            }
            7 -> {
                Log.d("SECUENCIA","$messageIndex: Hemos llegado. Puede depositar el paquete aquí. Gracias.")
                robotManager.speak("Hemos llegado. Puede depositar el paquete aquí. Gracias.", false)
                delay(8000L)
                messageIndex = 8
            }
            8 -> {
                Log.d("SECUENCIA","$messageIndex: REGRESANDO")
                robotManager.returnToPosition(mqttViewModel.returnDestination.value!!)
                // Considera agregar un delay o manejar cuando se debe cambiar a messageIndex 6 si es necesario
            }
        }
    }

    LaunchedEffect(shouldCheckCode.value) {
        if (shouldCheckCode.value) {
            numericPanelViewModel.checkForTaskExecution()
            shouldCheckCode.value = false
        }
    }

    LaunchedEffect(isCodeCorrect) {
        if (isCodeCorrect) {
            currentPage++
        }
    }

    LaunchedEffect(isNavigationComplete.value) {
        if (isNavigationComplete.value == true) {
            messageIndex = 7
        }
    }

    mqttViewModel.setReturnDestinationDefaultValue()

    FuturisticGradientBackground {
        if (isLoading) LoadingSpinner()
        else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
            ) {
                Row() {
                    if (currentPage != totalPages) {
                        GoBackButton(onClick = {
                            if (currentPage > 1) {
                                currentPage--
                            } else {
                                Log.d(
                                    "return",
                                    "to home screen and to default return pos: ${mqttViewModel.returnDestination.value}"
                                )
                                robotManager.returnToPosition(mqttViewModel.returnDestination.value.toString())
                                mqttViewModel.navigateToHomeScreen()
                            }
                        })
                    }
                }

                when (currentPage) {
                    1 -> {

                        Log.d("SECUENCIA T","$messageIndex: ¿Dispone de código de entrega?")
                        Row() {
                            ButtonsStep(
                                icon1 = Icons.Outlined.Check,
                                icon2 = Icons.Outlined.Clear,
                                text = "¿Dispone de código de entrega?",
                                onButton1Click = { currentPage++; hasCode = true; messageIndex = 2 },
                                onButton2Click = { currentPage++; hasCode = false; messageIndex = 3 })
                        }
                    }

                    2 -> {
                        Log.d("SECUENCIA T","$messageIndex: Por favor, introduzca el código que se le ha proporcionado")
                        if (hasCode){
                            NumericPad(
                                numericPanelViewModel = numericPanelViewModel,
                                onClick = { shouldCheckCode.value = true },
                                titleText = "Por favor, introduce el código de entrega"
                            )
                        }
                        else {
                            Row(modifier = Modifier.fillMaxSize()) {
                                NoCodeStep(robotManager, mqttViewModel)
                            }
                        }
                    }

                    3 -> {
                        Log.d("SECUENCIA T","$messageIndex: Acompáñeme a la sección de mensajería para depositar el paquete")
                        Row(modifier = Modifier.fillMaxSize()) {
                            NotificationStep(navController = navController)
                        }
                    }
                    4 -> {
                        Log.d("SECUECIA T","$messageIndex: VACIO")
                    }
                    5 -> {
                        Log.d("SECUENCIA T","$messageIndex: Código introducido correctamente. Por favor, acompáñeme a la sección de mensajería")
                        robotManager.speak("Código correcto.", false)
                    }
                    6 -> {
                        Log.d("SECUENCIA T","$messageIndex: Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado. Espere por favor")
                        robotManager.speak("Notificando a ${meetingInfo.anfitrion} de que su entrega ha llegado.", false)
                        //TO-DO: Implementar notificación
                        messageIndex = 3
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonsStep(
    icon1: ImageVector,
    icon2: ImageVector,
    text: String,
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransparentButtonWithIcon(
                text = "Sí", icon = icon1, onClick = onButton1Click
            )
            TransparentButtonWithIcon(
                text = "No", icon = icon2, onClick = onButton2Click
            )
        }
    }
}

@Composable
fun NotificationStep(navController: NavController) {
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

@Composable
fun NoCodeStep(robotManager: RobotManager, mqttViewModel: MqttViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Sin código",
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


