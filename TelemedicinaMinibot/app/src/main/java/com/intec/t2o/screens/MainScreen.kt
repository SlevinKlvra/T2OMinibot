package com.intec.t2o.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.intec.t2o.navigation.AppScreens
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel
) {
    val navigationState by mqttViewModel.navigationState.collectAsState()
    Log.d("Current Screen", "MainScreen")
    when (navigationState) {

        MqttViewModel.NavigationState.EyesScreen -> {
            Log.d("HomeScreen enum", AppScreens.EyesScreen.route)
            EyesScreen(navController, mqttViewModel)
        }

        MqttViewModel.NavigationState.MqttScreen -> {
            Log.d("HomeScreen enum", AppScreens.MQTTScreen.route)
            MQTTScreen(navController, mqttViewModel)
        }

        MqttViewModel.NavigationState.HomeScreen -> {
            Log.d("HomeScreen enum", AppScreens.HomeScreen.route)
            HomeScreen(navController, mqttViewModel)
        }

        MqttViewModel.NavigationState.NumericPanelScreen -> {
            Log.d("HomeScreen enum", AppScreens.NumericPanelScreen.route)
            NumericPanelScreen(
                navController,
                numericPanelViewModel = numericPanelViewModel,
                mqttViewModel = mqttViewModel
            )
        }

        MqttViewModel.NavigationState.AdminPanelScreen -> {
            Log.d("HomeScreen enum", AppScreens.AdminPanelScreen.route)
            AdminPanelScreen(
                navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }

        MqttViewModel.NavigationState.MeetingScreen -> {
            Log.d("HomeScreen enum", AppScreens.MeetingScreen.route)
            MeetingScreen(
                navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }

        MqttViewModel.NavigationState.UnknownVisitsScreen -> {
            Log.d("HomeScreen enum", AppScreens.UnknownVisitScreen.route)
            UnknownVisitScreen(
                navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }

        MqttViewModel.NavigationState.PackageAndMailManagementScreen -> {
            Log.d("HomeScreen enum", AppScreens.PackageAndMailManagementScreen.route)
            PackageAndMailManagementScreen(
                navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }

        MqttViewModel.NavigationState.DrivingScreen -> {
            Log.d("HomeScreen enum", AppScreens.DrivingScreen.route)
            DrivingScreen(navController, mqttViewModel = mqttViewModel)
        }

        MqttViewModel.NavigationState.ClockInScreen -> {
            Log.d("HomeScreen enum", AppScreens.ClockInScreen.route)
            ClockInScreen(
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                navController = navController
            )
        }
    }
}