package com.intec.t2o.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.screens.FuturisticGradientBackground
import com.intec.t2o.viewmodels.MqttViewModel

@Composable
fun DrivingComposable(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    Log.d("Current Composable", "DrivingScreen")
    val closeDrivingScreenFace by mqttViewModel.closeDrivingScreenFace.collectAsState()
    val tiempoRestantePausa by mqttViewModel.countdownState.collectAsState()
    val countdownFlag by mqttViewModel.countdownFlag.collectAsState()

    LaunchedEffect(key1 = true) {
        mqttViewModel.startCountdown()
    }

    if (countdownFlag) {
        mqttViewModel.navigateToEyesScreen()
        mqttViewModel.setCountdownFlagState(false)
    }

    if (closeDrivingScreenFace) {
        Log.d("CLOSEDRIVING", "Closing drivingscreenface")
        //mqttViewModel.deactivateCloseDrivingScreenFace()
    }
    FuturisticGradientBackground {

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "La actividad del robot se reanudará en:",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = tiempoRestantePausa.toString() + "s",
                        modifier = Modifier.padding(bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonCard(
                        text = "Reanudar tarea",
                        icon = Icons.Outlined.PlayArrow,
                        onClick = {
                            mqttViewModel.coutndownJob?.cancel()
                            mqttViewModel.isNavigating.value = true
                            onContinue()
                        })
                    ButtonCard(
                        text = "Cancelar tarea",
                        icon = Icons.Outlined.Clear,
                        onClick = {
                            mqttViewModel.coutndownJob?.cancel()
                            mqttViewModel.isNavigating.value = true
                            mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                            onCancel()
                        })
                }
            }
        }
    }
}