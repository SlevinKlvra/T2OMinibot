package com.intec.telemedicina.navigation

sealed class AppScreens(val route:String){
    object SplashScreen: AppScreens("splash_screen")
    object FirstScreen: AppScreens("first_screen")
    object HomeScreen: AppScreens("home_screen")
    object GamesScreen: AppScreens("games_screen")
    object SettingsScreen: AppScreens("settings_screen")
    object VideoCallScreen: AppScreens("videocall_screen")
    object IcariaScreen: AppScreens("icaria_screen")
    object MqttScreen: AppScreens("mqtt_screen")
    object TourScreen: AppScreens("tour_screen")
    object HomeControlScreen: AppScreens("home_control_screen")
    object TestScreen: AppScreens("test_screen")
    object DrivingScreen: AppScreens("driving_screen")
    object EyesScreen: AppScreens("eyes_screen")
    object NumericPanelScreen: AppScreens("numeric_panel_screen")
    object MainScreen: AppScreens("main_screen")
    object AdminPanelScreen: AppScreens("admin_panel_screen")
    object MeetingScreen: AppScreens("meeting_screen")
    object UnknownVisitScreen: AppScreens("unknown_visit_screen")
}
