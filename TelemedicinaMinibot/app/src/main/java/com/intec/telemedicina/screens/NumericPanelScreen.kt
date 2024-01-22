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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.TransparentButton
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun NumericPanelScreen(
    navController: NavController,
    numericPanelViewModel: NumericPanelViewModel,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager
) {
    Log.d("Current Screen", "NumericPanelScreen")
    val shouldCheckCode = remember { mutableStateOf(false) }
    val isCodeCorrect by numericPanelViewModel.isCodeCorrect.collectAsState()
    val isLoading by numericPanelViewModel.isLoading.collectAsState()


    LaunchedEffect(shouldCheckCode.value) {
        if (shouldCheckCode.value) {
            numericPanelViewModel.checkForTaskExecutionHardcoded()
            shouldCheckCode.value = false
        }
    }

    LaunchedEffect(isCodeCorrect) {
        if (isCodeCorrect) {
            navController.navigate(AppScreens.MeetingScreen.route)
        }
    }

    // Estilos generales
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(40.dp)
        .width(100.dp)

    val textStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = Color.Black
    )

    val showErrorAnimation by numericPanelViewModel.showErrorAnimation
    // Crear un Modifier para la animación
    val shakeModifier = if (showErrorAnimation) {
        // Crear y recordar la animación
        val anim = rememberInfiniteTransition().animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Modifier.graphicsLayer { translationX = anim.value }
    } else Modifier

    // Usa este Modifier en el elemento que quieres animar
    FuturisticGradientBackground {
        Box(modifier = shakeModifier) {
            // Contenido de pantalla
            if (isLoading) LoadingSpinner()
            else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Texto explicativo
                    Text(
                        text = "Por favor, introduce el código que se te ha proporcionado: ",
                        style = textStyle.copy(fontSize = 15.sp),
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = Color.White
                    )
                    // Muestra el código ingresado
                    Text(
                        text = numericPanelViewModel.enteredCode.value,
                        style = textStyle.copy(fontSize = 8.sp),
                        modifier = Modifier.padding(6.dp),
                        color = Color.Black
                    )

                    // Teclado numérico
                    val buttons =
                        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Borrar", "0", "Enviar")
                    buttons.chunked(3).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { number ->
                                if (number.isNotBlank()) {
                                    TransparentButton(
                                        text = number,
                                        onClick = {
                                            when (number) {
                                                "Borrar" -> numericPanelViewModel.removeLastDigit()
                                                "Enviar" -> {
                                                    shouldCheckCode.value = true
                                                }
                                                else -> numericPanelViewModel.addDigit(number.first())
                                            }
                                        },
                                    )
                                } else {
                                    Spacer(modifier = buttonModifier)
                                }
                            }
                        }
                    }
                }
            }
        }
        // Botón de retorno
        GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
    }
}

@Preview
@Composable
fun LoadingSpinner() {
    // Column para centrar el CircularProgressIndicator
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(120.dp),
            strokeWidth = 3.dp,
            color = Color.White
        ) // Muestra el indicador de carga
    }
}