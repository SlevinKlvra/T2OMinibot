package com.intec.telemedicina.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.components.TransparentButtonWithIcon
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.viewmodels.MqttViewModel

fun test1() {}

@Composable
@Preview(showBackground = true, widthDp = 1000, heightDp = 500)
fun DrivingScreenPreview() {
    val navController = NavController(LocalContext.current)
    FuturisticGradientBackground {
        // DrivingScreen(navController = navController)
    }
}

@Composable
fun DrivingScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel
) {
    Log.d("Current Screen", "DrivingScreen")
    val closeDrivingScreenFace by mqttViewModel.closeDrivingScreenFace.collectAsState()
    val tiempoRestantePausa by mqttViewModel.countdownState.collectAsState()
    val countdownFlag by mqttViewModel.countdownFlag.collectAsState()

    LaunchedEffect(key1 = true) {
        mqttViewModel.startCountdown()
    }

    if (countdownFlag){
        navController.popBackStack()
        mqttViewModel.setCountdownFlagState(false)
    }

    if (closeDrivingScreenFace) {
        Log.d("CLOSEDRIVING", "Closing drivingscreenface")
        //navController.popBackStack()
        //mqttViewModel.deactivateCloseDrivingScreenFace()
    }
    FuturisticGradientBackground {

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "La actividad del robot se reanudará en",
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = tiempoRestantePausa.toString(),
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        fontSize = 104.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "segundos",
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransparentButtonWithIcon(
                        text = "Reanudar tarea",
                        icon = Icons.Outlined.PlayArrow,
                        onClick = {
                            mqttViewModel.coutndownJob?.cancel()
                            mqttViewModel.robotMan.resumeNavigation(0)
                            navController.popBackStack()
                        })
                    TransparentButtonWithIcon(
                        text = "Cancelar tarea",
                        icon = Icons.Outlined.Clear,
                        onClick = {
                            mqttViewModel.coutndownJob?.cancel()
                            mqttViewModel.robotMan.returnToPosition(mqttViewModel.selectedItem.toString())
                            navController.popBackStack()
                        })
                }
            }
        }
    }
}

