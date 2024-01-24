package com.intec.t2o

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
import com.intec.t2o.di.MqttViewModelFactory
import com.intec.t2o.di.NumericPanelViewModelFactory
import com.intec.t2o.navigation.AppNavigation
import com.intec.t2o.robot.modulecallback.ModuleCallback
import com.intec.t2o.robotinterface.RobotManager
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
    lateinit var skillApi: SkillApi

    @Inject
    lateinit var robotMan: RobotManager

    // TODO: Pass robotManager to the viewmodel and try out the navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RobotApi.getInstance().connectServer(this, object : ApiListener {
            override fun handleApiDisabled() {
                // Implementar lógica en caso de que la API esté deshabilitada
            }

            override fun handleApiConnected() {

                // Establece el callback
                robotMan.setNavigationCallback(object : RobotManager.NavigationCallback {
                    override fun onNavigationCompleted() {
                        Log.d(
                            "MainActivity prev",
                            "onNavigationComplete: ${mqttViewModel.isNavigationComplete.value}"
                        )
                        mqttViewModel.onNavigationCompleted()
                        Log.d(
                            "MainActivity after",
                            "onNavigationComplete: ${mqttViewModel.isNavigationComplete.value}"
                        )
                    }

                    override fun onNavigationStarted() {
                        Log.d(
                            "MainActivity prev",
                            "onNavigationStart: ${mqttViewModel.isNavigationStart.value}"
                        )
                        mqttViewModel.onNavigationStarted()
                        Log.d(
                            "MainActivity prev",
                            "onNavigationStart: ${mqttViewModel.isNavigationStart.value}"
                        )
                    }

                    override fun onSpeakFinished() {
                        Log.d(
                            "MainActivity prev",
                            "onSpeakFinish: ${mqttViewModel.isSpeakFinish.value}"
                        )
                        mqttViewModel.onSpeakFinished()
                        Log.d(
                            "MainActivity prev",
                            "onSpeakFinish: ${mqttViewModel.isSpeakFinish.value}"
                        )
                    }
                })

                RobotApi.getInstance().setCallback(object : ModuleCallback() {
                    override fun onSendRequest(
                        reqId: Int, reqType: String, reqText: String, reqParam: String
                    ): Boolean {
                        Log.d(
                            "REQUEST MainActivity",
                            "reqId: $reqId, reqType: $reqType, reqText: $reqText, reqParam: $reqParam"
                        )
                        return robotMan.onSendRequest(reqId, reqType, reqText, reqParam) ?: false
                    }
                })

                skillApi.connectApi(applicationContext, object : ApiListener {
                    override fun handleApiDisabled() {
                        // Implementar lógica en caso de que la API esté deshabilitada
                    }

                    override fun handleApiConnected() {
                        Log.d("SKILLAPI", "Skill api connected! Creating robot Manager")
                        robotMan = RobotManager(skillApi, applicationContext)
                        robotMan.disableChargingInterface()
                        robotMan.registerCallback()
                    }

                    override fun handleApiDisconnected() {
                        // Implementar lógica en caso de desconexión de la API
                    }
                })
            }

            override fun handleApiDisconnected() {
                // Implementar lógica en caso de desconexión
            }
        })

        setContent {
            PlantillaJetpackTheme {
                Surface(
                    modifier = Modifier.fillMaxHeight(1f),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        mqttViewModel = mqttViewModel,
                        numericPanelViewModel = numericPanelViewModel,
                        robotManager = robotMan
                    )
                }
                // A surface container using the 'background' color from the theme
            }
        }
    }
}
