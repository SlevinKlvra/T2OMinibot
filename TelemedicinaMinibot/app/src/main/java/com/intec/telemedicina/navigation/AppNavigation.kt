package com.intec.telemedicina.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intec.telemedicina.di.SplashScreenViewModelFactory
import com.intec.telemedicina.screens.*

@Composable
fun AppNavigation(viewModelFactory : SplashScreenViewModelFactory){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.SplashScreen.route
    ){
        composable(AppScreens.SplashScreen.route){
            SplashScreen(navController = navController, viewModelFactory = viewModelFactory)
        }
        composable(AppScreens.HomeScreen.route){
            HomeScreen(navController = navController, viewModelFactory = viewModelFactory)
        }
        composable(AppScreens.SettingsScreen.route){
            SettingsScreen(navController = navController, "Settings Screen")
        }
        composable(AppScreens.MqttScreen.route){
            MqttScreen(navController = navController)
        }
        composable(AppScreens.AgendaScreen.route){
            AgendaScreen(navController = navController)
        }
        composable(AppScreens.GamesScreen.route){
            GamesScreen(navController = navController)
        }
        composable(AppScreens.ThirdScreen.route){
            ThirdScreen(navController = navController)
        }
        composable(AppScreens.FourthScreen.route){
            FourthScreen(navController = navController)
        }
        composable(AppScreens.VideoCallScreen.route){
            VideoCallScreen(navController = navController)
        }
        composable(AppScreens.MenuComidaScreen.route){
            MenuComidaScreen(navController = navController)
        }
    }
}