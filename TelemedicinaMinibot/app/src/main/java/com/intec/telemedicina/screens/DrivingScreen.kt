package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.ui.color.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun DrivingScreen(navController: NavController, mqttViewModel: MqttViewModel){
    val closeDrivingScreenFace by mqttViewModel.closeDrivingScreenFace.collectAsState()
    
    if(closeDrivingScreenFace) {
        Log.d("CLOSEDRIVING","Closing drivingscreenface")
        //navController.popBackStack()
        //mqttViewModel.deactivateCloseDrivingScreenFace()
    }
    Box(modifier = Modifier.fillMaxSize()){
        FloatingActionButton(
            onClick = { navController.navigate(AppScreens.HomeScreen.route) },
            modifier = Modifier.size(36.dp).align(Alignment.TopStart),
            containerColor = md_theme_light_tertiary
        ) { // You can set the house icon here using painterResource
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null
            )
        }
        Box{

            Row (modifier = Modifier, horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically){
                Button(onClick = { mqttViewModel.robotMan.returnToPosition() }) {
                    Text("Regresar")
                }
                Spacer(modifier = Modifier.padding(end = 15.dp, bottom = 12.dp, top = 5.dp))
                Button(onClick = { mqttViewModel.robotMan.stopNavigation(0) }) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.padding(end = 15.dp, bottom = 12.dp, top = 5.dp))
                Button(onClick = {}) {
                    Text("Llamada de emergencia")
                }
                Spacer(modifier = Modifier.padding(end = 15.dp, bottom = 12.dp, top = 5.dp))
                Button(onClick = { mqttViewModel.robotMan.resumeNavigation(0) }) {
                    Text(text = "Continuar")
                }
                Spacer(modifier = Modifier.padding(end = 15.dp, bottom = 12.dp, top = 5.dp))
                Button(
                    onClick = { mqttViewModel.robotMan.moveForward() }) {
                    Text(text = "Avanzar 10 cm")
                }
                Spacer(modifier = Modifier.padding(end = 15.dp, bottom = 12.dp, top = 5.dp))
                Button(onClick = { mqttViewModel.robotMan.goCharge() }) {
                    Text(text = "Base de carga")
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(36.dp).align(Alignment.BottomStart),
            containerColor = md_theme_light_tertiary
        ) { // You can set the house icon here using painterResource
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )
        }
    }



}