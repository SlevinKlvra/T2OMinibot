package com.intec.t2o

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.intec.t2o.di.MqttViewModelFactory
import com.intec.t2o.di.NumericPanelViewModelFactory
import com.intec.t2o.navigation.AppNavigation
import com.intec.t2o.robotinterface.RobotConnectionService
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.robotinterface.SkillApiService
import com.intec.t2o.ui.theme.PlantillaJetpackTheme
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mqttViewModelFactory: MqttViewModelFactory

    @Inject
    lateinit var numericPanelViewModelFactory: NumericPanelViewModelFactory

    private val mqttViewModel by viewModels<MqttViewModel> { mqttViewModelFactory }

    private val numericPanelViewModel by viewModels<NumericPanelViewModel> { numericPanelViewModelFactory }

    @Inject
    lateinit var robotConnectionService: RobotConnectionService

    @Inject
    lateinit var skillApiService: SkillApiService

    @Inject
    lateinit var robotMan: RobotManager

    // TODO: Pass robotManager to the viewmodel and try out the navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlantillaJetpackTheme {
                Surface(
                    modifier = Modifier.fillMaxHeight(1f),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        mqttViewModel = mqttViewModel,
                        numericPanelViewModel = numericPanelViewModel
                    )
                }
                // A surface container using the 'background' color from the theme
            }
        }
    }
}
