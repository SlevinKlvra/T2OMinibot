package com.intec.telemedicina.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.screens.AdminPanelScreen
import com.intec.telemedicina.screens.ClockInScreen
import com.intec.telemedicina.screens.DrivingScreen
import com.intec.telemedicina.screens.EyesScreen
import com.intec.telemedicina.screens.HomeScreen
import com.intec.telemedicina.screens.MQTTScreen
import com.intec.telemedicina.screens.MainScreen
import com.intec.telemedicina.screens.MeetingScreen
import com.intec.telemedicina.screens.NumericPanelScreen
import com.intec.telemedicina.screens.PackageAndMailManagementScreen
import com.intec.telemedicina.screens.UnknownVisitScreen
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun AppNavigation(
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel,
    robotManager: RobotManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.MainScreen.route
    ) {
        composable(AppScreens.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.MQTTScreen.route) {
            MQTTScreen(navController = navController, mqttViewModel = mqttViewModel)
        }
        composable(AppScreens.DrivingScreen.route) {
            DrivingScreen(navController = navController, mqttViewModel = mqttViewModel)
        }
        composable(AppScreens.EyesScreen.route) {
            EyesScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.NumericPanelScreen.route) {
            NumericPanelScreen(
                navController = navController,
                numericPanelViewModel = numericPanelViewModel,
                mqttViewModel = mqttViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.MainScreen.route) {
            MainScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.AdminPanelScreen.route) {
            AdminPanelScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.MeetingScreen.route) {
            MeetingScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.UnknownVisitScreen.route) {
            UnknownVisitScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.PackageAndMailManagementScreen.route) {
            PackageAndMailManagementScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
                robotManager = robotManager
            )
        }
        composable(AppScreens.ClockInScreen.route) {
            ClockInScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel,
            )
        }
    }
}