package com.intec.telemedicina.navigation

sealed class AppScreens(val route:String){
    object SplashScreen: AppScreens("splash_screen")
    object FirstScreen: AppScreens("first_screen")
    object HomeScreen: AppScreens("home_screen")
    object GamesScreen: AppScreens("games_screen")
    object SettingsScreen: AppScreens("settings_screen")
    object VideoCallScreen: AppScreens("videocall_screen")
    object AgendaScreen: AppScreens("agenda_screen")
    object MqttScreen: AppScreens("mqtt_screen")
    object ThirdScreen: AppScreens("3rd_screen")
    object FourthScreen: AppScreens("4th_screen")
    object MenuComidaScreen: AppScreens("menucomida_screen")
}
