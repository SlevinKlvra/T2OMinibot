package com.intec.t2o.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intec.t2o.screens.AdminPanelScreen
import com.intec.t2o.screens.ClockInScreen
import com.intec.t2o.screens.DrivingScreen
import com.intec.t2o.screens.EyesScreen
import com.intec.t2o.screens.HomeScreen
import com.intec.t2o.screens.MQTTScreen
import com.intec.t2o.screens.MainScreen
import com.intec.t2o.screens.MeetingScreen
import com.intec.t2o.screens.NumericPanelScreen
import com.intec.t2o.screens.PackageAndMailManagementScreen
import com.intec.t2o.screens.UnknownVisitScreen
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNavigation(
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.MainScreen.route
    ) {
        composable(AppScreens.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                mqttViewModel = mqttViewModel
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
                mqttViewModel = mqttViewModel
            )
        }

        composable(AppScreens.NumericPanelScreen.route) {
            NumericPanelScreen(
                navController = navController,
                numericPanelViewModel = numericPanelViewModel,
                mqttViewModel = mqttViewModel
            )
        }
        composable(AppScreens.MainScreen.route) {
            MainScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }
        composable(AppScreens.AdminPanelScreen.route) {
            AdminPanelScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }
        composable(AppScreens.MeetingScreen.route) {
            MeetingScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }
        composable(AppScreens.UnknownVisitScreen.route) {
            UnknownVisitScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
            )
        }
        composable(AppScreens.PackageAndMailManagementScreen.route) {
            PackageAndMailManagementScreen(
                navController = navController,
                mqttViewModel = mqttViewModel,
                numericPanelViewModel = numericPanelViewModel
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