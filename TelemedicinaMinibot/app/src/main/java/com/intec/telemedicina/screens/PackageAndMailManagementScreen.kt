package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.NumericPad
import com.intec.telemedicina.components.TransparentButton
import com.intec.telemedicina.components.TransparentButtonWithIcon
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

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
                        Row() {
                            ButtonsStep(
                                icon1 = Icons.Outlined.Check,
                                icon2 = Icons.Outlined.Clear,
                                text = "¿Dispone de código de entrega?",
                                onButton1Click = { currentPage++; hasCode = true },
                                onButton2Click = { currentPage++; hasCode = false })
                        }
                    }

                    2 -> {
                        if (hasCode)
                            NumericPad(
                                numericPanelViewModel = numericPanelViewModel,
                                onClick = { shouldCheckCode.value = true },
                                titleText = "Por favor, introduce el código que se te ha proporcionado"
                            )
                        else {
                            Row(modifier = Modifier.fillMaxSize()) {
                                NoCodeStep(robotManager, mqttViewModel)
                            }
                        }
                    }

                    3 -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            NotificationStep(navController = navController)
                        }
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


