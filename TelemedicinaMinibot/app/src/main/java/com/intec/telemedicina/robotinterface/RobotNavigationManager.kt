package com.intec.telemedicina.robotinterface

import android.util.Log
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.intec.telemedicina.CoordinatePose
import org.json.JSONObject
import javax.inject.Inject


class RobotNavigationManager @Inject constructor(
    private val robotApi: RobotApi // Your Robot API class
) : RobotInterface {
    private val closeEnoughThreshold = 0.5f // Define your threshold, e.g., 0.5 meters
    /*
    fun startNavigation(
        reqId: Int,
        destName: String,
        coordinateDeviation: Double,
        time: Long,
        navigationListener: ActionListener
    ) {
        robotApi.startNavigation(reqId, destName, coordinateDeviation, time, object :
            ActionListener() {
            override fun onResult(status: Int, response: String) {
                if (status == Definition.ERROR_DESTINATION_CAN_NOT_ARRAIVE) {
                    getCurrentPosition(reqId) { currentPosition -> (
                        getDestinationPosition(destName) { destinationPosition ->
                            if (isCloseEnough(currentPosition, destinationPosition)) {
                                handleBeingCloseEnough(destName)
                            } else {
                                // Handle other cases or reattempt navigation
                            }
                        })
                    }
                } else {
                    // Handle other statuses
                }
            }

            override fun onError(errorCode: Int, errorString: String) {
                // Handle error
            }
        })
    }*/

    private fun getCurrentPosition(reqId: Int, callback: (CoordinatePose) -> Unit) {
        robotApi.getPosition(reqId, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                val json = JSONObject(message)
                val x = json.getDouble("x")
                val y = json.getDouble("y")
                val theta = json.getDouble("theta")
                callback(CoordinatePose(x, y, theta))
            }

            override fun onError(errorCode: Int, errorString: String) {
                // Handle error
            }
        })
    }

    private fun getDestinationPosition(destName: String, callback: (CoordinatePose) -> Unit) {
        // Implement this method based on how you retrieve the destination's position
    }

    private fun isCloseEnough(currentPosition: CoordinatePose, destinationPosition: CoordinatePose): Boolean {
        val distance = calculateDistance(currentPosition, destinationPosition)
        return distance <= closeEnoughThreshold
    }

    private fun calculateDistance(pos1: CoordinatePose, pos2: CoordinatePose): Float {
        // Calculate the distance between two positions
        return Math.sqrt(
            Math.pow((pos2.x - pos1.x).toDouble(), 2.0) +
                    Math.pow((pos2.y - pos1.y).toDouble(), 2.0)
        ).toFloat()
    }

    private fun handleBeingCloseEnough(destination: String) {
        // Implement what should happen when the robot is close enough to the destination
    }

    override fun startNavigation(
        reqId: Int,
        destName: String,
        coordinateDeviation: Double,
        time: Long,
        navigationListener: ActionListener
    ) {
        Log.d("START NAVIGATION", "Comenzando navegación")
        robotApi.startNavigation(1, destName, coordinateDeviation, time, navigationListener)
    }

    override fun stopNavigation(reqId: Int) {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        robotApi.stopNavigation(reqId)
    }

    override fun goPosition(reqId: Int, position: String, goPositionListener: CommandListener) {
        TODO("Not yet implemented")
    }

    override fun stopGoPosition(reqId: Int) {
        TODO("Not yet implemented")
    }

    override fun getPosition(reqId: Int, commandListener: CommandListener) {
        TODO("Not yet implemented")
    }

    // Other methods...
}
