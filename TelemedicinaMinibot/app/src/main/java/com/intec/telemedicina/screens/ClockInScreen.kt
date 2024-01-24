package com.intec.telemedicina.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.telemedicina.R
import com.intec.telemedicina.components.GoBackButton
import com.intec.telemedicina.components.NumericPad
import com.intec.telemedicina.components.TransparentButtonWithIcon
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun ClockInScreen(
    mqttViewModel: MqttViewModel,
    navController: NavController,
    numericPanelViewModel: NumericPanelViewModel
) {
    var showSecondPart by remember { mutableStateOf(false) }

    FuturisticGradientBackground {
        if (!showSecondPart) {
            // Mostrar la primera parte con el NumericPad
            GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
            NumericPad(
                numericPanelViewModel = numericPanelViewModel,
                onClick = { if (numericPanelViewModel.clockIn()) showSecondPart = true },
                titleText = "Introduce tu código de empleado"
            )
        } else {
            mqttViewModel.robotMan.speak("", false, object : RobotManager.SpeakCompleteListener {
                override fun onSpeakComplete() {
                    // Acciones a realizar después de hablar
                }
            })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .padding(top = 24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Entrada registrada",
                        color = Color.White,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Image(
                        painter = painterResource(id = R.drawable.clockicon),
                        contentDescription = "logo",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(start = 8.dp)
                            .clickable {
                                mqttViewModel.navigateToClockInScreen()
                            })
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransparentButtonWithIcon(
                        text = "Volver",
                        icon = Icons.Outlined.ArrowBack,
                        onClick = { mqttViewModel.navigateToHomeScreen() }
                    )
                }
            }
        }
    }
}