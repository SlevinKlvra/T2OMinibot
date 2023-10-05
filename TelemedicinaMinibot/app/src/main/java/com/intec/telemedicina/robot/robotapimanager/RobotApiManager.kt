package com.intec.telemedicina.robot.robotapimanager

import android.content.Context
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.intec.telemedicina.robot.modulecallback.ModuleCallback

class RobotApiManager {

    fun connectServer(context: Context, apiListener: ApiListener) {
        RobotApi.getInstance().connectServer(context, object : ApiListener {
            override fun handleApiDisabled() {
                // Implementa la lógica necesaria cuando el API está deshabilitado
            }

            override fun handleApiConnected() {
                println("Handle API Connected")
                RobotApi.getInstance().setCallback(ModuleCallback())

            }

            override fun handleApiDisconnected() {
                // Implementa la lógica necesaria cuando el API está desconectado
            }
        })
    }
}