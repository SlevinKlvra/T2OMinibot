package com.intec.telemedicina.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun TourScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel){
    Button(onClick = {splashScreenViewModel.startTour()},
        content = {})
    Text("Third Screen")
}