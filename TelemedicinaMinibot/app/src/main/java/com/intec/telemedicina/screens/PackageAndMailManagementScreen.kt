package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

@Composable
fun PackageAndMailManagementScreen(navController : NavController, mqttViewModel : MqttViewModel, numericPanelViewModel : NumericPanelViewModel, robotManager : RobotManager) {
    Log.d("Current Screen", "PackageAndMailManagementScreen")
    Text(text = "Package and Mail Management Screen")
}
