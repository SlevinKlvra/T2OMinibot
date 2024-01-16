package com.intec.telemedicina

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.di.MqttViewModelFactory
import com.intec.telemedicina.di.NumericPanelViewModelFactory
import com.intec.telemedicina.di.SplashScreenViewModelFactory
import com.intec.telemedicina.icariascreen.AppNavigation
import com.intec.telemedicina.robot.modulecallback.ModuleCallback
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.ui.theme.PlantillaJetpackTheme
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: SplashScreenViewModelFactory

    @Inject
    lateinit var mqttViewModelFactory: MqttViewModelFactory

    @Inject
    lateinit var numericPanelViewModelFactory: NumericPanelViewModelFactory

    private val viewModel by viewModels<SplashScreenViewModel> { viewModelFactory }

    private val mqttViewModel by viewModels<MqttViewModel> { mqttViewModelFactory }

    private val numericPanelViewModel by viewModels<NumericPanelViewModel> { numericPanelViewModelFactory }

    var skillApi = SkillApi()

    @Inject
    lateinit var robotMan: RobotManager

    // TODO: Pass robotManager to the viewmodel and try out the navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the DisplayMetrics object
        RobotApi.getInstance().connectServer(this, object :
            ApiListener {
            override fun handleApiDisabled() {}
            override fun handleApiConnected() {
                //robotMan.setRecognizable(true)
                // Server is connected, set the callback for receiving requests, including voice commands, system events, etc.
                skillApi.connectApi(applicationContext, object : ApiListener {
                    override fun handleApiDisabled() {
                        TODO("Not yet implemented")
                    }

                    override fun handleApiConnected() {
                        robotMan.registerCallback()
                        Log.d("SKILLAPI","Skill api connected! Creating robot Manager")
                        robotMan = RobotManager(skillApi,applicationContext)
                    }

                    override fun handleApiDisconnected() {
                        TODO("Not yet implemented")
                    }
                })

            }

            override fun handleApiDisconnected() {
                //Disconnect
            }
        })



        setContent {
            PlantillaJetpackTheme {
                Surface(
                    modifier = Modifier.fillMaxHeight(1f),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel, mqttViewModel = mqttViewModel, numericPanelViewModel = numericPanelViewModel, robotManager = robotMan)
                }
                // A surface container using the 'background' color from the theme
            }
        }
    }


}
