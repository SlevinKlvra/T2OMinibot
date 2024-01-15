package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun MainScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel, mqttViewModel: MqttViewModel, numericPanelViewModel: NumericPanelViewModel,robotManager: RobotManager) {
    val navigationState by mqttViewModel.navigationState.collectAsState()

    when (navigationState) {

        MqttViewModel.NavigationState.EyesScreen -> {
            Log.d("EyesScreen enum", "${AppScreens.EyesScreen.route}")
            EyesScreen(navController, mqttViewModel, robotManager)
        }
        MqttViewModel.NavigationState.HomeScreen -> {
            Log.d("HomeScreen enum", "${AppScreens.HomeScreen.route}")
            HomeScreen(navController, splashScreenViewModel, mqttViewModel, robotManager)
        }
        MqttViewModel.NavigationState.NumericPanelScreen -> {
            Log.d("HomeScreen enum", "${AppScreens.NumericPanelScreen.route}")
            NumericPanelScreen(navController, numericPanelViewModel = numericPanelViewModel, robotManager = robotManager)
        }
        MqttViewModel.NavigationState.MeetingScreen -> {
            Log.d("HomeScreen enum", "${AppScreens.MeetingScreen.route}")
            MeetingScreen(navController, mqttViewModel = mqttViewModel,numericPanelViewModel = numericPanelViewModel, robotManager = robotManager)
        }
    }
}