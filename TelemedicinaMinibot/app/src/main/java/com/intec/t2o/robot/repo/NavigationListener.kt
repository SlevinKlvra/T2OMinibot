package com.intec.t2o.robot.repo

import android.util.Log
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.listener.ActionListener
import javax.inject.Inject

class NavigationListener @Inject constructor(): ActionListener(){
    override fun onError(errorCode: Int, errorString: String?, extraData: String?) {
        super.onError(errorCode, errorString, extraData)
        when (errorCode) {
            Definition.ERROR_NOT_ESTIMATE -> {
                Log.d("ONERROR", "ERROR NO ESTIMADO. ERRORCODE: $errorCode, ERRORSTRING: $errorString, EXTRADATA: extraData")
            }
            // Manejar otros códigos de error aquí...
        }
    }

    override fun onResult(status: Int, responseString: String?, extraData: String?) {
        super.onResult(status, responseString, extraData)
        println("STATUS RECIBIDO: $status")
        when(status){
            Definition.RESULT_OK -> {
                if("true" == responseString){
                    Log.d("ONRESULT_OK", "Navegación exitosa")
                }
                else
                {
                    Log.d("ONRESULT_FAIL", "La navegación ha fallado")
                }
            }
        }
    }

    override fun onStatusUpdate(status: Int, data: String?, extraData: String?) {
        super.onStatusUpdate(status, data, extraData)

        when (status) {
            Definition.STATUS_NAVI_AVOID -> {
                // La ruta actual ha sido bloqueada por obstáculos
                Log.d("ONSTATUS UPDATE", "La ruta actual ha sido bloqueada por obstáculos")

            }
            Definition.STATUS_NAVI_AVOID_END -> {

                Log.d("ONSTATUS UPDATE", "NAVI AVOID END")
            }
            Definition.STATUS_START_NAVIGATION -> {

                Log.d("ONSTATUS UPDATE", "START NAV")
            }
            Definition.STATUS_START_CRUISE -> {

                Log.d("ONSTATUS UPDATE", "START CRUISE")
            }
            Definition.STATUS_NAVI_OUT_MAP -> {

                Log.d("ONSTATUS UPDATE", "NAVI OUT MAP")
            }
            Definition.STATUS_NAVI_MULTI_ROBOT_WAITING -> {

                Log.d("ONSTATUS UPDATE", "ROBOT WAITING")
            }
            Definition.STATUS_NAVI_MULTI_ROBOT_WAITING_END -> {

                Log.d("ONSTATUS UPDATE", "WAITING END")
            }
            Definition.STATUS_NAVI_GO_STRAIGHT -> {

                Log.d("ONSTATUS UPDATE", "GO STRAIGHT")
            }
            Definition.STATUS_NAVI_TURN_LEFT -> {

                Log.d("ONSTATUS UPDATE", "LEFT")
            }
            Definition.STATUS_NAVI_TURN_RIGHT -> {

                Log.d("ONSTATUS UPDATE", "RIGHT")
            }
            // Manejar otros estados aquí...
            Definition.STATUS_DISTANCE_WITH_DESTINATION -> {

            }
        }
    }
}