package com.intec.t2o.navigation

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    object SettingsScreen : AppScreens("settings_screen")
    object MQTTScreen : AppScreens("mqtt_screen")
    object DrivingScreen : AppScreens("driving_screen")
    object EyesScreen : AppScreens("eyes_screen")
    object NumericPanelScreen : AppScreens("numeric_panel_screen")
    object MainScreen : AppScreens("main_screen")
    object AdminPanelScreen : AppScreens("admin_panel_screen")
    object MeetingScreen : AppScreens("meeting_screen")
    object UnknownVisitScreen : AppScreens("unknown_visit_screen")
    object PackageAndMailManagementScreen : AppScreens("package_and_mail_management_screen")
    object ClockInScreen : AppScreens("clock_in_screen")
}
