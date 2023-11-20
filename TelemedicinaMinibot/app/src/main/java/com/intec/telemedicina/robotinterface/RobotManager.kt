package com.intec.telemedicina.robotinterface

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.intec.telemedicina.robot.modulecallback.ModuleCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RobotManager @Inject constructor(@ApplicationContext applicationContext: Context){
    val robotApi : RobotApi = RobotApiModule().provideRobotApi()
    var actionListener : ActionListener = ActionListener()

    private val _navigationStatus = MutableLiveData<String>()
    val navigationStatus: LiveData<String> get() = _navigationStatus
    init {

        setupActionListener()

        robotApi.connectServer(applicationContext, object : ApiListener {
            override fun handleApiDisabled() {
                // Handle disconnection or other relevant action
            }

            override fun handleApiConnected() {
                Log.d("SERVER CONNECTION", "Conectado al servidor")
                robotApi.setCallback(ModuleCallback())
            }

            override fun handleApiDisconnected() {
                TODO("Not yet implemented")
            }
        })

    }

    private fun setupActionListener() {
        actionListener = object : ActionListener() {
            override fun onStatusUpdate(status: Int, data: String) {
                when (status) {
                    Definition.STATUS_NAVI_AVOID -> _navigationStatus.postValue("Ruta bloqueada por obstáculos")
                    Definition.STATUS_NAVI_AVOID_END -> _navigationStatus.postValue("Obstáculo desaparecido")
                    Definition.STATUS_START_NAVIGATION -> _navigationStatus.postValue("Navegación iniciada")
                    // ... añade todos los estados que necesites
                }
            }

            // Si hay otros métodos en ActionListener, sobrescribirlos aquí también
        }
    }

    val robotInterface: RobotInterface = RobotNavigationManager(robotApi)

    fun getRobotInterfaceMethod(): RobotInterface {
        return robotInterface
    }
}