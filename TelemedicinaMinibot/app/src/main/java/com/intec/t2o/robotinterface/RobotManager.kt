package com.intec.t2o.robotinterface

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.listener.Person
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.person.PersonListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.SkillCallback
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.google.gson.Gson
import com.intec.t2o.mqtt.MqttMessageListener
import com.intec.t2o.repositories.dto.Place
import com.intec.t2o.robot.modulecallback.ModuleCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject

class RobotManager @Inject constructor(
    private val skillApi: SkillApi,
    @ApplicationContext applicationContext: Context
) :
    MqttMessageListener {

    var context: Context = applicationContext

    var actionListener: ActionListener = ActionListener()

    //TODO: Add poseslist methods reading from the shared prefs to get the pose from name --> Use this to expand the robot manager
    //TODO: Create speech methods inside here
    //TODO: Add navController here for opening and closing screens
    val placesList: MutableList<Place> = mutableListOf()
    val posesList: MutableList<Pose> = mutableListOf()
    private val _destinationsList = MutableLiveData(listOf<String>())

    private val _navigationStatus = MutableLiveData<String>()

    val savePosesSharedPreferences = SavePosesSharedPreferences(applicationContext)

    var pausedLocation: String = ""

    var lastDestination = mutableStateOf("")

    var navigation: Boolean = false

    var textListener: TextListener

    var personListener: PersonListener

    val isFollowing = MutableStateFlow(false)

    var onPersonDetected: ((List<Person>?) -> Unit)? = null
    var onSpeechResultReceived: ((String) -> Unit)? = null
    var onNavigationStarted: ((Boolean) -> Unit)? = null

    init {
        setupActionListener()
        getPlaceList()

        personListener = object : PersonListener() {
            override fun personChanged() {
                val personList = PersonApi.getInstance().allPersons
                Log.d("RobotMan PersonListener", "Person changed: $personList")
                onPersonDetected?.invoke(personList)
            }
        }

        textListener = object : TextListener() {
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
            }
        }
    }

    fun startFocusFollow(faceId: Int) {
        // Define los parámetros necesarios para el método startFocusFollow
        val reqId = 1 // Define o calcula un ID de solicitud adecuado
        val lostTimeout =
            10 // Define el tiempo en segundos antes de reportar la pérdida del objetivo
        val maxDistance = 2.5F //Define la distancia máxima en metros para el seguimiento

        // Inicia el seguimiento de la persona con el ID de cara dado
        RobotApi.getInstance().startFocusFollow(reqId, faceId,
            lostTimeout.toLong(), maxDistance, object : ActionListener() {
                @Deprecated("Deprecated in Java")
                override fun onStatusUpdate(status: Int, data: String?) {
                    when (status) {
                        Definition.STATUS_TRACK_TARGET_SUCCEED -> {
                            // El seguimiento del objetivo ha tenido éxito
                            isFollowing.value = true
                            Log.d(
                                "startFocusFolow",
                                "Seguimiento del objetivo exitoso ${isFollowing.value}"
                            )
                        }

                        Definition.STATUS_GUEST_LOST -> {
                            // El objetivo se ha perdido
                            isFollowing.value = false
                            Log.d("startFocusfollow", "Objetivo perdido : ${isFollowing.value}")
                        }

                        Definition.STATUS_GUEST_FARAWAY -> {
                            // El objetivo está fuera de rango
                            Log.d("startFocusFollow", "Objetivo fuera de rango")
                        }

                        Definition.STATUS_GUEST_APPEAR -> {
                            // El objetivo está en rango nuevamente

                            isFollowing.value = true
                            Log.d(
                                "startFocusFollow",
                                "Objetivo detectado nuevamente: ${isFollowing.value}"
                            )
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(errorCode: Int, errorString: String?) {
                    // Maneja los errores aquí
                    Log.e("startFocusFollow", "Error en el seguimiento: $errorString")
                }

                @Deprecated("Deprecated in Java")
                override fun onResult(status: Int, responseString: String?) {
                    // Maneja el resultado aquíPerson
                    Log.d("startFocusFollow", "Respuesta del seguimiento: $responseString")
                }
            })
    }

    fun stopFocusFollow() {
        // Código para detener el enfoque
        Log.d("stopFocusFollow", "Deteniendo seguimiento")
        unregisterPersonListener()
        RobotApi.getInstance().stopFocusFollow(1)
        isFollowing.value = false
    }

    private val personApi = PersonApi.getInstance()

    fun unregisterPersonListener() {
        Log.d("RobotMan PersonListener", "Unregistering Person")
        personApi.unregisterPersonListener(personListener)
    }

    fun registerPersonListener() {
        Log.d("RobotMan PersonListener", "Registering Person")
        personApi.registerPersonListener(personListener)
        detectPerson(0)
    }

    fun detectPerson(faceId: Int): List<Person>? {
        startFocusFollow(faceId)
        return personApi?.allPersons
    }

    fun callback_speech_to_speech(speechResult: String) {
        Log.d("STT", speechResult)
        onSpeechResultReceived?.invoke(speechResult)
    }

    fun callbackNavigationStarted(navigationStarted: Boolean) {
        Log.d("NAVIGATION STARTED", navigationStarted.toString())
        onNavigationStarted?.invoke(navigationStarted)
    }

    override fun onMessageReceived(topic: String, message: String) {

    }

    private fun setupActionListener() {
        actionListener = object : ActionListener() {
            @Deprecated("Deprecated in Java")
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
            @Deprecated("Deprecated in Java")
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
                        val ignoreDistance = false
                        val safedistance = 1
                        val pose = Pose(
                            x.toFloat(),
                            y.toFloat(),
                            theta.toFloat(),
                            name,
                            ignoreDistance,
                            safedistance
                        )
                        val place = Place(x, y, theta, name)
                        Log.d("POSE", pose.toString())
                        Log.d("PLACE", place.toString())

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

    fun getPoses(): List<Pose> {
        return savePosesSharedPreferences.getDataList()
    }

    private fun setLastPosition(lastPosition: String) {
        Log.d("setLastPosition", "Setting last position to $lastPosition")
        lastDestination.value = lastPosition
    }

    data class RobotStatus(val destName: String, val status: String)

    // Definición del callback
    interface NavigationCallback {
        fun onNavigationCompleted()
        fun onNavigationStarted()
        fun onSpeakFinished()
    }

    private var navigationCallback: NavigationCallback? = null

    fun setNavigationCallback(callback: NavigationCallback?) {
        this.navigationCallback = callback
    }

    fun startNavigation(
        reqId: Int,
        destName: String,
        coordinateDeviation: Double,
        time: Long,
        navigationCompleteListener: NavigationCompleteListener
    ){
        Log.d("START NAVIGATION", "Comenzando navegación a $destName")
        setLastPosition(destName)

        stopFocusFollow()
        callbackNavigationStarted(true)
        RobotApi.getInstance().goPosition(
            0,
            RobotApi.getInstance().getSpecialPose(destName).toJson(),
            object : CommandListener() {
                override fun onError(errorCode: Int, errorString: String?, extraData: String?) {
                    super.onError(errorCode, errorString, extraData)
                    var status_ = ""
                    status_ = "ERROR"
                    val gson = Gson()

                    val robotStatus = RobotStatus(destName, status_)

                    val json = gson.toJson(robotStatus)
                }

                override fun onStatusUpdate(status: Int, data: String?, extraData: String?) {
                    super.onStatusUpdate(status, data, extraData)

                    var status_: String
                    var gson = Gson()
                    Log.d("INFO UPDATE", extraData.toString())
                    when (status) {
                        Definition.STATUS_INFO_UPDATE -> {

                            when (data) {
                                "navigation_started" -> {
                                    //speak("Estoy yendo a $destName",false)
                                    pausedLocation = destName
                                    Log.d(
                                        "START NAVIGATION",
                                        "Asignado destino $destName a pausedLocation"
                                    )
                                    status_ = "START"
                                    navigationCallback?.onNavigationStarted()
                                    val robotStatus = RobotStatus(destName, status_)

                                    val json = gson.toJson(robotStatus)
                                }

                                "Avoid" -> {
                                    status_ = "AVOID"
                                    val robotStatus = RobotStatus(destName, status_)

                                    val json = gson.toJson(robotStatus)
                                    speak(
                                        "No puedo pasar, ¿podrías dejarme paso?",
                                        false,
                                        object : SpeakCompleteListener {
                                            override fun onSpeakComplete() {
                                                // Acciones a realizar después de hablar
                                            }
                                        })
                                }

                                "Avoid end" -> {
                                    status_ = "END_AVOID"
                                    val robotStatus = RobotStatus(destName, status_)

                                    val json = gson.toJson(robotStatus)
                                    speak(
                                        "Gracias por dejarme paso, vamos",
                                        false,
                                        object : RobotManager.SpeakCompleteListener {
                                            override fun onSpeakComplete() {
                                                // Acciones a realizar después de hablar
                                            }
                                        })
                                }
                            }

                        }
                    }
                }

            override fun onResult(result: Int, message: String?, extraData: String?) {
                super.onResult(result, message, extraData)
                if(message.toBoolean()){
                    var status_ : String = ""
                    status_ = "END"
                    var gson = Gson()
                    val robotStatus = RobotStatus(destName, status_)
                    Log.d("ROBOT STATUS NUEVO", robotStatus.toString())

                    if(robotStatus.status == "END"){
                        navigationCompleteListener.onNavigationComplete()
                        callbackNavigationStarted(false)
                        startFocusFollow(0)
                        registerPersonListener()
                        Log.d("ROBOT STATUS END", robotStatus.toString())
                        navigationCallback?.onNavigationCompleted()
                    }
                        val json = gson.toJson(robotStatus)
                    }
                }
            })
    }

    fun resumeNavigation() {
        if (!lastDestination.value.isNullOrEmpty()) {
            Log.d("RESUME NAVIGATION", "Continuing navigation: ${lastDestination.value}")
            startNavigation(0, lastDestination.value, 0.1, 1000000, navigationCompleteListener = object : NavigationCompleteListener {
                override fun onNavigationComplete() {
                    // Acciones a realizar después de hablar
                    speak("Continuando navegación",false, object : SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            // Acciones a realizar después de hablar
                        }
                    })
                }
            })
        } else {
            Log.d("RESUME NAVIGATION", "No last navigated location available")
        }
    }

    fun stopNavigation() {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        RobotApi.getInstance().stopGoPosition(0)
        pausedLocation = ""
    }

    fun pauseNavigation() {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        RobotApi.getInstance().stopGoPosition(0)
    }

    fun returnToPosition(positionToReturn: String) {
        //TODO: Save last known coordinates when starting a navigation
        if(positionToReturn != ""){
            startNavigation(0,positionToReturn,0.1,1000000, navigationCompleteListener = object : NavigationCompleteListener {
                override fun onNavigationComplete() {
                    // Acciones a realizar después de hablar
                }
            })
        }
        else{
            speak("Actualmente no existe un destino al que haya ido previamente",false, object : RobotManager.SpeakCompleteListener {
                override fun onSpeakComplete() {
                    // Acciones a realizar después de hablar
                }
            })
        }
    }

    interface SpeakCompleteListener {
        fun onSpeakComplete()
    }

    interface NavigationCompleteListener {
        fun onNavigationComplete()
    }

    fun speak(text : String, listen: Boolean, speakCompleteListener: SpeakCompleteListener){
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
                Log.d("SPEAK", "Speak finished")
                navigationCallback?.onSpeakFinished()
                skillApi.setRecognizeMode(listen)
                skillApi.setRecognizable(listen)
                speakCompleteListener.onSpeakComplete()
            }
        })
    }

    fun questionPrueba() {
        RobotApi.getInstance().setCallback(object : ModuleCallback() {
            override fun onSendRequest(
                reqId: Int,
                reqType: String,
                reqText: String,
                reqParam: String
            ): Boolean {
                Log.d("onSendRequest Prueba", "$reqId, $reqType, $reqText, $reqParam")
                callback_speech_to_speech(reqText)
                return false
            }
        })
    }

    fun onSendRequest(
        reqId: Int,
        reqType: String,
        reqText: String,
        reqParam: String
    ): Boolean {
        Log.d("onSendRequest robotMan", "$reqId, $reqType, $reqText, $reqParam")
        callback_speech_to_speech(reqText)
        return false
    }

    fun registerCallback() {
        try {
            skillApi.registerCallBack(object : SkillCallback() {
                // Implementar los métodos override como antes
                override fun onSpeechParResult(s: String) {
                    // Resultado temporal del reconocimiento de voz
                }

                override fun onStart() {
                    // Inicio del reconocimiento
                }

                override fun onStop() {
                    // Fin del reconocimiento
                }

                override fun onVolumeChange(volume: Int) {
                    // Cambio en el volumen de la voz reconocida
                }

                override fun onQueryEnded(status: Int) {
                    // Manejar el fin de la consulta basado en el estado
                }

                override fun onQueryAsrResult(asrResult: String) {
                    // asrResult: resultado final del reconocimiento
                }
            })
            skillApi.setRecognizable(false)
        } catch (e: Exception) {
            Log.e("RobotManager", "Error al registrar callback: ${e.message}")
        }
    }

    fun moveForward() {
        RobotApi.getInstance().goForward(0, 0.1F, 0.1F, false, CommandListener())
    }

    fun disableChargingInterface() {
        RobotApi.getInstance().disableBattery()
    }

    fun goCharge() {
        speak("Voy a recargar.", false, object : SpeakCompleteListener {
            override fun onSpeakComplete() {
                // Acciones a realizar después de hablar
            }
        })
        unregisterPersonListener()
        RobotApi.getInstance().goCharging(0)
    }

    fun wakeUp() {
        RobotApi.getInstance().stopCharge(0)
    }

    fun scheduleWithCoroutine() = runBlocking {
        launch {
            delay(6000L)
            startNavigation(0,"entrada",0.1234,0, navigationCompleteListener = object : NavigationCompleteListener {
                override fun onNavigationComplete() {
                    // Acciones a realizar después de hablar
                }
            })
        }
    }
}