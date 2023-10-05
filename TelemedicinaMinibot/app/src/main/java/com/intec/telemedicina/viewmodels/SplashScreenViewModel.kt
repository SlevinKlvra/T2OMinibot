package com.intec.telemedicina.viewmodels

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.SkillCallback
import com.intec.telemedicina.repositories.dto.Place
import com.intec.telemedicina.robot.modulecallback.ModuleCallback
import com.intec.telemedicina.robot.skillcallback.mSkillCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject


@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    application: Application,
    private val robotApi: RobotApi,
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

    init {

        val mSkillCallback = object : SkillCallback() {
            override fun onSpeechParResult(s: String) {
                // Handle the result of temporary speech recognition
            }

            override fun onStart() {
                // Handle start of recognition
            }

            override fun onStop() {
                //println("LA FRASE TERMINA (ONSTOP)")
            }

            override fun onVolumeChange(p0: Int) {}

            override fun onQueryEnded(p0: Int) {}
            // Implement other methods...
        }

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

            // Si hay otros métodos en ActionListener, deberías sobrescribirlos aquí también
        }
    }

    private fun getCurrentMap(){
        RobotApi.getInstance().getMapName(1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                if (!TextUtils.isEmpty(message)) {
                    //"message" means map name
                    Log.d("MAP", "CURRENT MAP IS $message")
                    println(message)
                }
            }
        })
    }

    private fun getMapName(){
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
    }

    fun showNavigationDialog() {
        showNavigationDialog.value = true
    }

    fun hideNavigationDialog() {
        showNavigationDialog.value = false
    }

    fun getPlaceList() {
        currentAction = "Cargando ubicaciones del mapa"
        robotApi.getPlaceList(1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val jsonArray = JSONArray(message)
                    val newPlaces = mutableListOf<Place>()
                    val newDestinations = mutableListOf<String>()

                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val x = json.getDouble("x")
                        val y = json.getDouble("y")
                        val theta = json.getDouble("theta")
                        val name = json.getString("name")

                        val place = Place(x, y, theta, name)
                        newPlaces.add(place)
                        newDestinations.add(name)
                    }

                    placesList.addAll(newPlaces)
                    _destinationsList.value = placesList.map { it.name }

                    Log.d("MAP", "CURRENT LOCATIONS ARE $placesList")
                    Log.d("MAP", "Lista ubicaciones: $destinationsList")

                } catch (e: JSONException) {
                    Log.e("ERROR", "Error parsing JSON", e)
                } catch (e: NullPointerException) {
                    Log.e("ERROR", "Null pointer exception", e)
                }
            }
        })
    }

    fun navigateToDestiny(destiny: String) {
        robotApi.startNavigation(1, destiny, AVOIDANCE_RADIUS, TIMEOUT, actionListener)
    }

    fun stopNavigation(){
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        robotApi.stopNavigation(2)
    }
}
