package com.intec.telemedicina

import androidx.compose.animation.core.animateDp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.RobotApi
import com.intec.telemedicina.navigation.AppScreens
import kotlinx.coroutines.delay

@Composable
fun AppBottomBar(navController: NavController) {

    val visible = remember { mutableStateOf(false)}

    LaunchedEffect(visible.value) {
        if (visible.value){
            delay(5000) // Espera 5 segundos
            visible.value = false // Oculta la AppBottomBar
        }
    }

    val modifier = Modifier.pointerInput(Unit){
        detectVerticalDragGestures { change, dragAmount ->
            change.consume()

            if(dragAmount < 0){
                visible.value = true
            }

            if (dragAmount > 0){
                visible.value = false
            }
        }
    }
    val transition = updateTransition(targetState = visible.value, label = "LABEL")
    val offset by transition.animateDp(label = "LABEL") {
        isVisible -> if(isVisible) 0.dp else 80.dp
    }

    BottomAppBar(
        modifier = modifier
            .offset(y = offset),
        containerColor = Color.Transparent,
        contentPadding = PaddingValues(
            start = 10.dp,
            end = 10.dp,
            top = 8.dp,
            bottom = 8.dp
        ),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    IconButton(
                        onClick = { onHomeClick() },
                        modifier = Modifier.size(40.dp)
                    ) { // Icono para volver a Home
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                        )
                    }
                    Text(text = "Home")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    IconButton(
                        onClick = { onChargeClick() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        // Icono para ir a la pila de carga
                        Icon(painterResource(id = R.drawable.battery_charging), "Cargar")
                    }
                    Text(text = "Cargar")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    IconButton(
                        onClick = { onSettingsClick(navController) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        // Icono para abrir ajustes del robot
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes"
                        )
                    }
                    Text("Settings")
                }
            }
        }
    )
}

fun onHomeClick(){}

fun onChargeClick(){RobotApi.getInstance().goCharging(1)}

fun onSettingsClick(navController:NavController){navController.navigate(route = AppScreens.SettingsScreen.route)}
