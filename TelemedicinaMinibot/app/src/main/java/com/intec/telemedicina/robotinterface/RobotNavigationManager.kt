package com.intec.telemedicina.robotinterface

import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.intec.telemedicina.CoordinatePose
import com.intec.telemedicina.repositories.dto.Place
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class RobotNavigationManager @Inject constructor(
    private val robotApi: RobotApi // Your Robot API class
) : RobotInterface {
    var navigation : Boolean = false
    private val closeEnoughThreshold = 0.5f // Define your threshold, e.g., 0.5 meters

    private val placesList: MutableList<Place> = mutableListOf()
    private val posesList: MutableList<Pose> = mutableListOf()
    private val _destinationsList = MutableLiveData(listOf<String>())
    val destinationsList: LiveData<List<String>> get() = _destinationsList

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
        time: Long
    ) {
        Log.d("START NAVIGATION", "Comenzando navegación")
        RobotApi.getInstance().goPosition(0, Pose(0.16775802F,-1.8305563F,-2.7992465F).toJson(), CommandListener())
        //robotApi.startNavigation(1, destName, coordinateDeviation, time, navigationListener)
    }

    override fun stopNavigation(reqId: Int) {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        //robotApi.stopNavigation(reqId)
        RobotApi.getInstance().stopGoPosition(0)
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

    override fun getNavigationListener(): ActionListener {
        return navigationListener
    }

    private var navigationListener: ActionListener = object : ActionListener() {
        @Throws(RemoteException::class)
        override fun onResult(status: Int, response: String) {
            when (status) {
                Definition.RESULT_OK -> if ("true" == response) {
                    //navigation is successful
                    //publishMessage("robot/nav_pub/status",robotApi.currentPose.toString())

                } else {
                    //navigation is failed
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onError(errorCode: Int, errorString: String?) {
            when (errorCode) {
                Definition.ERROR_NOT_ESTIMATE -> {}
                Definition.ERROR_IN_DESTINATION -> {}
                Definition.ERROR_DESTINATION_NOT_EXIST -> {}
                Definition.ERROR_DESTINATION_CAN_NOT_ARRAIVE -> {}
                Definition.ACTION_RESPONSE_ALREADY_RUN -> {}
                Definition.ACTION_RESPONSE_REQUEST_RES_ERROR -> {}
                Definition.ERROR_MULTI_ROBOT_WAITING_TIMEOUT -> {}
                Definition.ERROR_NAVIGATION_FAILED -> {}
            }
        }

        @Throws(RemoteException::class)
        override fun onStatusUpdate(status: Int, data: String?) {
            when (status) {
                Definition.STATUS_NAVI_AVOID -> {}
                Definition.STATUS_NAVI_AVOID_END -> {}
                Definition.STATUS_START_NAVIGATION -> {
                    Log.d("NAVIGATION", "Starting navigation, $data")
                    navigation = true
                }
                Definition.STATUS_START_CRUISE -> {}
                Definition.STATUS_NAVI_OUT_MAP -> {}
                Definition.STATUS_NAVI_MULTI_ROBOT_WAITING -> {}
                Definition.STATUS_NAVI_MULTI_ROBOT_WAITING_END -> {}
                Definition.STATUS_NAVI_GO_STRAIGHT -> {}
                Definition.STATUS_NAVI_TURN_LEFT -> {}
                Definition.STATUS_NAVI_TURN_RIGHT -> {}
                Definition.ACTION_NAVI_GET_LOCATION -> {
                    Log.d("NAVIGATION", "Getting location, $data")
                }

                Definition.ACTION_NAVI_GET_PLACE_NAME -> {
                    Log.d("NAVIGATION", "Getting place name, $data")
                }
                Definition.ACTION_NAVI_GET_POSITION ->  {
                    Log.d("NAVIGATION", "Getting position, $data")
                }
                Definition.ACTION_NAVI_STOP_MOVE -> {
                    Log.d("NAVIGATION", "Stopping move, $data")
                }
                Definition.ACTION_NAVI_STOP_NAVIGATION -> {
                    Log.d("NAVIGATION", "Stopping navigation, $data")

                }
            }
        }
    }

    override fun getPlaceList() {
        robotApi.getPlaceList(1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val jsonArray = JSONArray(message)
                    val newPlaces = mutableListOf<Place>()
                    val newDestinations = mutableListOf<String>()
                    val newPoses = mutableListOf<Pose>()

                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val x = json.getDouble("x")
                        val y = json.getDouble("y")
                        val theta = json.getDouble("theta")
                        val name = json.getString("name")
                        //float x, float y, float theta, String name, boolean ignoreDistance, int safeDistance
                        val ignoreDistance = false//json.getBoolean("ignoreDistance")
                        val safedistance = 1//json.getInt("safeDistance")
                        val pose = Pose(x.toFloat(),y.toFloat(), theta.toFloat(), name, ignoreDistance, safedistance)
                        val place = Place(x, y, theta, name)
                        Log.d("POSE", "${pose.toString()}")
                        Log.d("PLACE", "${place.toString()}")

                        newPlaces.add(place)
                        newDestinations.add(name)
                        newPoses.add(pose)
                    }
                    posesList.addAll(newPoses)
                    placesList.addAll(newPlaces)
                    _destinationsList.value = placesList.map { it.name }
                    Log.d("MAP", "CURRENT LOCATIONS ARE $placesList")

                } catch (e: JSONException) {
                    Log.e("ERROR", "Error parsing JSON", e)
                } catch (e: NullPointerException) {
                    Log.e("ERROR", "Null pointer exception", e)
                }
            }
        })
    }

    override fun getPoses() : MutableList<Pose>{
        return posesList
    }

    // Other methods...
}
