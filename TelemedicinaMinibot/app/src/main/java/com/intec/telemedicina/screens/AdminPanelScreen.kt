package com.intec.telemedicina.screens

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.components.TransparentButton
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel


@Composable
fun AdminPanelScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel,
    robotManager: RobotManager
) {

    var enteredCode by remember { mutableStateOf("") }

    // Estilos generales
    val textStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = Color.Black
    )

    FuturisticGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Texto explicativo
            Text(
                text = "CODIGO DE ACCESO",
                style = textStyle.copy(fontSize = 15.sp),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            // Muestra el código ingresado
            Text(
                text = enteredCode,
                style = textStyle.copy(fontSize = 12.sp),
                modifier = Modifier.padding(6.dp)
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
                                        "Borrar" -> {
                                            if (enteredCode.isNotEmpty()) {
                                                enteredCode = enteredCode.dropLast(1)
                                            }
                                        }

                                        "Enviar" -> {
                                            if (numericPanelViewModel.checkForAdvancedSettingsAccess(
                                                    enteredCode
                                                )
                                            ) {
                                                mqttViewModel.navigateToMqttScreen()
                                            } else {
                                                robotManager.speak("Código incorrecto", false)
                                            }
                                        }

                                        else -> enteredCode += number.first()
                                    }
                                },
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(30.dp)
                                    .width(100.dp)
                            )
                        }
                    }
                }
            }
        }

        // Botón de retorno
        FloatingActionButton(
            onClick = { mqttViewModel.navigateToHomeScreen() },
            modifier = Modifier
                .size(56.dp)
                .padding(16.dp),
            contentColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
    }


}
