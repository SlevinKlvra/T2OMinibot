package com.intec.telemedicina.viewmodels

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.repositories.dto.Place
import com.intec.telemedicina.robotinterface.RobotManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    application: Application,
    private val robotApi: RobotApi,
    private val skillApi: SkillApi,
    private val robotManager: RobotManager,
    private var actionListener: ActionListener
) : AndroidViewModel(application) {

    companion object {
        private const val TIMEOUT: Long = 1000000
        private const val AVOIDANCE_RADIUS = 0.12345
    }
    //NAVIGATION DIALOG
    val showNavigationDialog = MutableStateFlow(false)

    private val _navigationStatus = MutableLiveData<String>()
    val navigationStatus: LiveData<String> get() = _navigationStatus

    private val _destinationsList = MutableLiveData(listOf<String>())
    val destinationsList: LiveData<List<String>> get() = _destinationsList
    private var _mapName : String = ""

    var mapName: String get() = _mapName
        set(value) {
            _mapName = value
        }

    val actionListenerStatus = mutableStateOf("")
    val isNavigating = mutableStateOf(false)
    var currentAction: String = ""

    private val placesList: MutableList<Place> = mutableListOf()
    private val posesList: MutableList<Pose> = mutableListOf()

    var lastDestiny: String = ""
    var currentDestiny: String = ""
    /*
    init {

        setupActionListener()

        val appContext = application.applicationContext
        robotApi.connectServer(appContext, object : ApiListener {
            override fun handleApiDisabled() {
                // Handle disconnection or other relevant action
            }

            override fun handleApiConnected() {
                Log.d("SERVER CONNECTION", "Conectado al servidor")
                robotApi.setCallback(ModuleCallback())
                currentAction = "Conectado con éxito"
            }

            override fun handleApiDisconnected() {
                TODO("Not yet implemented")
            }
        })

    }
    */

    fun playTextViaTTS(text: String) {

            skillApi.playText(text, object : TextListener() {
                override fun onStart() {
                    // Iniciar reproducción
                    Log.d("TTS", "Reproduciendo texto: $text")
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

    /*private fun getCurrentMap(){
        RobotApi.getInstance().getMapName(1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                if (!TextUtils.isEmpty(message)) {
                    //"message" means map name
                    Log.d("MAP", "CURRENT MAP IS $message")
                    println(message)
                }
            }
        })
    }*/

    /*private fun getMapName(){
        robotApi.getMapName(1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                if (!TextUtils.isEmpty(message)) {
                    //"message" means map name
                    _mapName = message
                    mapName = _mapName
                }
            }
        })
        Log.d("MAP", "Mapa actual: $mapName")
    }*/

    fun showNavigationDialog() {
        //stopNavigation()
        showNavigationDialog.value = true
    }

    fun hideNavigationDialog() {
        showNavigationDialog.value = false
    }
/*
    fun getPlaceList() {
        currentAction = "Cargando ubicaciones del mapa"
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
*/

    /*
    fun navigateToDestiny(destiny: String) {
        Log.d("NAVIGATION", "Iniciando navegación a $destiny")
        updateDestiny(destiny)
        robotApi.startNavigation(1, destiny, AVOIDANCE_RADIUS, TIMEOUT, actionListener)
    }
*/
    //Actualiza el destino actual y guarda el anterior
    fun updateDestiny(destiny: String){
        Log.d("SWITCH_DESTINY", "Switching currentDestiny: $currentDestiny to lastDestiny: $lastDestiny")
        lastDestiny = currentDestiny
        currentDestiny = destiny
    }

    /*
    //Navega al último destino
    fun navigateToLastDestiny(){
        Log.d("NAVIGATION_RETURN", "Return to location: $lastDestiny")
        if(lastDestiny == ""){
            lastDestiny = currentDestiny
        }
        navigateToDestiny(lastDestiny)
    }
*/

    /*
    fun stopNavigation(){
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        robotApi.stopNavigation(2)
    }

    fun startTour(){
        robotApi.startCruise(1,posesList,1 , mutableListOf<Int>(1,2,3),actionListener)
    }

     */
}
