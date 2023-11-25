package com.intec.telemedicina.robotinterface

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.intec.telemedicina.repositories.dto.Place
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject


class RobotManager @Inject constructor(skillApi: SkillApi, @ApplicationContext applicationContext: Context){
    var skillApi = skillApi

    var actionListener : ActionListener = ActionListener()
    lateinit var robotInterface: RobotInterface

    //TODO: Add poseslist methods reading from the shared prefs to get the pose from name --> Use this to expand the robot manager
    //TODO: Create speech methods inside here
    val placesList: MutableList<Place> = mutableListOf()
    val posesList: MutableList<Pose> = mutableListOf()
    private val _destinationsList = MutableLiveData(listOf<String>())
    val destinationsList: LiveData<List<String>> get() = _destinationsList

    private val _navigationStatus = MutableLiveData<String>()
    val navigationStatus: LiveData<String> get() = _navigationStatus

    val savePosesSharedPreferences = SavePosesSharedPreferences(applicationContext)

    init {
        setupActionListener()
        getPlaceList()

        //getPlaceList()
        //robotInterface = RobotNavigationManager(robotApi)
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

    fun getPlaceList() {
        RobotApi.getInstance().getPlaceList(1, object : CommandListener() {
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
                    savePosesSharedPreferences.saveDataList(posesList)

                } catch (e: JSONException) {
                    Log.e("ERROR", "Error parsing JSON", e)
                } catch (e: NullPointerException) {
                    Log.e("ERROR", "Null pointer exception", e)
                }
            }
        })
    }

    fun printPoses(){
        Log.d("Get Places Called",placesList.toString())
    }

    fun getPoses() : List<Pose>{
        return savePosesSharedPreferences.getDataList()
    }

    fun startNavigation(
        reqId: Int,
        destName: String,
        coordinateDeviation: Double,
        time: Long
    ) {
        Log.d("START NAVIGATION", "Comenzando navegación")
        RobotApi.getInstance().goPosition(0, RobotApi.getInstance().getSpecialPose(destName).toJson(), CommandListener())
        //robotApi.startNavigation(1, destName, coordinateDeviation, time, navigationListener)
    }

    fun stopNavigation(reqId: Int) {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        //robotApi.stopNavigation(reqId)
        RobotApi.getInstance().stopGoPosition(0)
    }


    fun speak(text : String){
        skillApi.playText(TTSEntity("sid-012345",text), object : TextListener() {
            override fun onStart() {
                // Iniciar reproducción
            }

            override fun onStop() {
                // Detener reproducción
            }

            override fun onError() {
                // Manejar error
            }

            override fun onComplete() {
                // Reproducción completada
                skillApi.setRecognizeMode(true);
                skillApi.setRecognizable(true);
            }
        })
    }
    
    


    fun getRobotInterfaceMethod(): RobotInterface {
        return robotInterface
    }
}