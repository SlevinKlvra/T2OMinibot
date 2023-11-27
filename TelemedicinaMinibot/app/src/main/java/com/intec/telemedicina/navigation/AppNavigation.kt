package com.intec.telemedicina.icariascreen

import IcariaScreen
import VideoCallScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.screens.DrivingScreen
import com.intec.telemedicina.screens.EyesScreen
import com.intec.telemedicina.screens.GamesScreen
import com.intec.telemedicina.screens.HomeControlScreen
import com.intec.telemedicina.screens.HomeScreen
import com.intec.telemedicina.screens.MqttScreen
import com.intec.telemedicina.screens.SettingsScreen
import com.intec.telemedicina.screens.SplashScreen
import com.intec.telemedicina.screens.TestScreen
import com.intec.telemedicina.screens.TourScreen
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun AppNavigation(viewModel: SplashScreenViewModel, mqttViewModel: MqttViewModel, robotManager: RobotManager){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.EyesScreen.route
    ){
        composable(AppScreens.SplashScreen.route){
            SplashScreen(navController = navController, viewModel = viewModel)
        }
        composable(AppScreens.HomeScreen.route){
            HomeScreen(navController = navController, splashScreenViewModel = viewModel, mqttViewModel = mqttViewModel, robotManager = robotManager)
        }
        composable(AppScreens.SettingsScreen.route){
            SettingsScreen(navController = navController, "Settings Screen")
        }
        composable(AppScreens.MqttScreen.route){
            MqttScreen(navController = navController, mqttViewModel = mqttViewModel)
        }
        composable(AppScreens.TestScreen.route){
            TestScreen(navController = navController)
        }
        composable(AppScreens.GamesScreen.route){
            GamesScreen(navController = navController)
        }
        composable(AppScreens.TourScreen.route){
            TourScreen(navController = navController, splashScreenViewModel = viewModel)
        }
        composable(AppScreens.HomeControlScreen.route){
            HomeControlScreen(navController = navController)
        }
        composable(AppScreens.VideoCallScreen.route){
            VideoCallScreen(navController = navController)
        }
        composable(AppScreens.IcariaScreen.route){
            IcariaScreen(navController = navController)
        }
        /*composable(AppScreens.DrivingFaceScreen.route){
            DrivingFaceScreen(navController = navController, mqttViewModel = mqttViewModel)
        }*/


        composable(AppScreens.DrivingScreen.route) {
            DrivingScreen(navController = navController, mqttViewModel = mqttViewModel)
        }
        composable(AppScreens.EyesScreen.route) {
            EyesScreen(navController = navController, mqttViewModel = mqttViewModel)
        }

    }
}