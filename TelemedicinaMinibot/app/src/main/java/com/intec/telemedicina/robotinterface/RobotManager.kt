package com.intec.telemedicina.robotinterface

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
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
import com.intec.telemedicina.data.Face
import com.intec.telemedicina.data.InteractionState
import com.intec.telemedicina.mqtt.MQTTConfig
import com.intec.telemedicina.mqtt.MqttManager
import com.intec.telemedicina.mqtt.MqttManagerCallback
import com.intec.telemedicina.mqtt.MqttMessageListener
import com.intec.telemedicina.repositories.dto.Place
import com.intec.telemedicina.robot.modulecallback.ModuleCallback
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject

class RobotManager @Inject constructor(skillApi: SkillApi, @ApplicationContext applicationContext: Context) :
    MqttMessageListener{

    var skillApi = skillApi

    var context: Context = applicationContext

    var actionListener : ActionListener = ActionListener()
    lateinit var robotInterface: RobotInterface

    //TODO: Add poseslist methods reading from the shared prefs to get the pose from name --> Use this to expand the robot manager
    //TODO: Create speech methods inside here
    //TODO: Add navController here for opening and closing screens
    val placesList: MutableList<Place> = mutableListOf()
    val posesList: MutableList<Pose> = mutableListOf()
    private val _destinationsList = MutableLiveData(listOf<String>())
    val destinationsList: LiveData<List<String>> get() = _destinationsList

    private val _navigationStatus = MutableLiveData<String>()
    val navigationStatus: LiveData<String> get() = _navigationStatus

    val savePosesSharedPreferences = SavePosesSharedPreferences(applicationContext)

    var pausedLocation : String = ""

    var currentDestination = ""
    var lastDestination = ""

    private val _connectionState = mutableStateOf("Disconnected")
    val connectionState get()= _connectionState
    var navigation : Boolean = false
    val listeningTopics : String = "robot/nav_cmds/stop_navigation"
    private var _mqttQuestion = MutableLiveData<String>()
    val mqttQuestion : MutableLiveData<String> get() = _mqttQuestion

    val showQuestionsDialog = MutableStateFlow(false)
    val showWelcomeDialog = MutableStateFlow(false)

    var initiated_status = false

    var fraseInteraccion = ""

    var hideEyesScreen = false

    private val _incomingMessages = mutableStateOf(emptyList<String>())
    val incomingMessages: State<List<String>> get() = _incomingMessages

    private val mqttCallback = MqttManagerCallback(_connectionState, {
        val updatedMessages = _incomingMessages.value.toMutableList()
        updatedMessages.add(it)
        _incomingMessages.value = updatedMessages
    }, this)
    private val mqttManager : MqttManager
    private var mqttConfigInstance = MQTTConfig(
        SERVER_URI ="tcp://192.168.47.116:1883",
        client_id = "Robot",
        qos = 0,
        user = "telegraf",
        password = "metricsmetricsmetricsmetrics",
        apiUser = "",
        apiPassword = "",
        waitingIdleTime = 0,
        meetingTimeThreshold = 0,
    )

    lateinit var textListener : TextListener

    var personListener: PersonListener

    var questionIsSiNo : Boolean = false

    val isFollowing = MutableStateFlow(false)

    var onPersonDetected: ((List<Person>?) -> Unit)? = null
    var onSpeechResultReceived : ((String) -> Unit)? = null
    init {
        mqttManager = MqttManager(getApplication(applicationContext), mqttCallback, mqttConfigInstance, getApplication(applicationContext))
        mqttManager.connect()
        setupActionListener()
        getPlaceList()

        personListener = object : PersonListener() {
            override fun personChanged() {
                val personList = PersonApi.getInstance().allPersons
                onPersonDetected?.invoke(personList)
            }
        }

        /*PersonApi.getInstance().registerPersonListener(listener)*/

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

        //getPlaceList()
        //robotInterface = RobotNavigationManager(robotApi)
    }

    fun startFocusFollow(faceId: Int) {
        // Define los parámetros necesarios para el método startFocusFollow
        val reqId = 1 // Define o calcula un ID de solicitud adecuado
        val lostTimeout = 10 // Define el tiempo en segundos antes de reportar la pérdida del objetivo
        val maxDistance = 2.5F //Define la distancia máxima en metros para el seguimiento

        // Inicia el seguimiento de la persona con el ID de cara dado
        RobotApi.getInstance().startFocusFollow(reqId, faceId,
            lostTimeout.toLong(), maxDistance, object : ActionListener() {
            override fun onStatusUpdate(status: Int, data: String?) {
                when (status) {
                    Definition.STATUS_TRACK_TARGET_SUCCEED -> {
                        // El seguimiento del objetivo ha tenido éxito
                        isFollowing.value = true
                        //Log.d("TAG", "Seguimiento del objetivo exitoso ${isFollowing.value}")

                    }
                    Definition.STATUS_GUEST_LOST -> {
                        // El objetivo se ha perdido
                        isFollowing.value = false
                        //Log.d("TAG", "Objetivo perdido : ${isFollowing.value}")
                    }
                    Definition.STATUS_GUEST_FARAWAY -> {
                        // El objetivo está fuera de rango
                        //Log.d("TAG", "Objetivo fuera de rango")
                    }
                    Definition.STATUS_GUEST_APPEAR -> {
                        // El objetivo está en rango nuevamente

                        isFollowing.value = true
                        //Log.d("TAG", "Objetivo detectado nuevamente: ${isFollowing.value}")
                    }
                }
            }

            override fun onError(errorCode: Int, errorString: String?) {
                // Maneja los errores aquí
                //Log.e("TAG", "Error en el seguimiento: $errorString")
            }

            override fun onResult(status: Int, responseString: String?) {
                // Maneja el resultado aquíPerson
                Log.d("TAG", "Respuesta del seguimiento: $responseString")
            }
        })
    }

    fun stopFocusFollow() {
        // Código para detener el enfoque
        // ...
        RobotApi.getInstance().stopFocusFollow(1);
        isFollowing.value = false
    }

    private val personApi = PersonApi.getInstance()

    fun unregisterPersonListener(){
        Log.d("RobotMan PersonListener", "Unregistering Person")
        personApi.unregisterPersonListener(personListener)
    }

    fun registerPersonListener(){
        Log.d("RobotMan PersonListener", "Registering Person")
        personApi.registerPersonListener(personListener)
        detectPerson(0)
    }

    fun detectPerson(faceId: Int): List<Person>? {
        startFocusFollow(faceId)
        //Log.d("RobotMan detectPerson", "${personApi.allPersons}")
        return personApi?.allPersons
    }


    fun callback_speech_to_speech(speechResult: String){
        Log.d("STT",speechResult)
        onSpeechResultReceived?.invoke(speechResult)
        /*if(speechResult == "reunión") {
            fraseInteraccion = "Vale, acompáñame por favor."
            startNavigation(0, "reunión", 0.1234, 100000)
        }

        if(speechResult == "sí"){
            fraseInteraccion = "Vale, acompáñame por favor."
            startNavigation(0, "reunión", 0.1234, 100000)

        }else if(speechResult == "no"){
            startNavigation(0, "entrada", 0.1234, 100000)
            speak("Está bien, vuelvo a mi puesto. Buenos días.", false)
        }*/


        addIncomingMessage("Publishing message: $speechResult to topic: test/speech")
        mqttManager.publishMessage("robot/voice_cmds/question_answer",speechResult.toString())
        questionIsSiNo = false
    }

    fun sendSiNoResponse(isSiNo: Boolean){
        Log.d("STT","response is Si: $isSiNo")
        addIncomingMessage("Publishing message: $isSiNo to topic: test/speech")
        if (isSiNo){
            mqttManager.publishMessage("robot/voice_cmds/question_si_no_answer","YES")

        }
        else {
            mqttManager.publishMessage("robot/voice_cmds/question_si_no_answer","NO")
        }
        questionIsSiNo = false
    }

    private fun addIncomingMessage(message: String) {
        Log.d("RobotManager", "Nuevo mensaje recibido: $message")
        val currentMessages = _incomingMessages.value ?: emptyList()
        _incomingMessages.value = currentMessages + message
    }

    override fun onMessageReceived(topic: String, message: String){

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

    data class RobotStatus(val destName : String, val status : String)

    fun startNavigation(
        reqId: Int,
        destName: String,
        coordinateDeviation: Double,
        time: Long
    ){
        Log.d("START NAVIGATION", "Comenzando navegación")
        unregisterPersonListener()
        stopCharging()

        RobotApi.getInstance().goPosition(0, RobotApi.getInstance().getSpecialPose(destName).toJson(), object : CommandListener(){
            override fun onError(errorCode: Int, errorString: String?, extraData: String?) {
                super.onError(errorCode, errorString, extraData)
                var status_ : String = ""
                status_ = "ERROR"
                var gson = Gson()

                val robotStatus = RobotStatus(destName, status_)

                val json = gson.toJson(robotStatus)
                mqttManager.publishMessage("robot/nav_cmds/status",json.toString())
                mqttManager.publishMessage("test/log_error","$errorCode, $errorString, $extraData")
            }

            override fun onStatusUpdate(status: Int, data: String?, extraData: String?) {
                super.onStatusUpdate(status, data, extraData)
                mqttManager.publishMessage("test/log_status","$status, $data, $extraData")
                var status_ : String = ""
                var gson = Gson()
                Log.d("INFO UPDATE", extraData.toString())
                when (status) {
                    Definition.STATUS_INFO_UPDATE -> {

                        when(data) {
                            "navigation_started" -> {
                                speak("Estoy yendo a $destName",false)
                                mqttManager.publishMessage("robot/faceType", Face.HAPPY.toString())
                                status_ = "START"
                                val robotStatus = RobotStatus(destName, status_)

                                val json = gson.toJson(robotStatus)
                                mqttManager.publishMessage("robot/nav_cmds/status",json.toString())
                                updateCurrentDestination(destName)
                            }
                            "Avoid" -> {
                                status_ = "AVOID"
                                val robotStatus = RobotStatus(destName, status_)

                                val json = gson.toJson(robotStatus)
                                mqttManager.publishMessage("robot/nav_cmds/status",json.toString())
                                speak("No puedo pasar, ¿podrías dejarme paso?",false)
                            }
                            "Avoid end" -> {
                                status_ = "END_AVOID"
                                val robotStatus = RobotStatus(destName, status_)

                                val json = gson.toJson(robotStatus)
                                mqttManager.publishMessage("robot/nav_cmds/status",json.toString())
                                speak("Gracias por dejarme paso, vamos",false)
                            }
                        }

                    }
                }
            }

            override fun onResult(result: Int, message: String?, extraData: String?) {
                super.onResult(result, message, extraData)
                mqttManager.publishMessage("test/log_result","$result, $message, $extraData")
                if(message.toBoolean()){
                    var status_ : String = ""
                    status_ = "END"
                    var gson = Gson()
                    val robotStatus = RobotStatus(destName, status_)
                    Log.d("ROBOT STATUS NUEVO", robotStatus.toString())

                    if (robotStatus.destName == "reunión" && robotStatus.status == "END"){
                        registerPersonListener()
                        fraseInteraccion = "Ya hemos llegado. ¿Deseas alguna otra cosa?"
                        speak(fraseInteraccion, true)
                    }
                    if (robotStatus.destName == "entrada" && robotStatus.status == "END"){
                        registerPersonListener()
                        setRecognizable(true)
                    }
                    if (robotStatus.destName == "reunion" && robotStatus.status == "END"){
                        speak("Hemos llegado. Póngase cómodo y Gloria llegará enseguida. Yo vuelvo a mi puesto. Muchas gracias.", false)
                        scheduleWithCoroutine()

                    }
                    val json = gson.toJson(robotStatus)
                    mqttManager.publishMessage("robot/nav_cmds/status",json.toString())
                    mqttManager.publishMessage("robot/faceType", Face.NEUTRAL.toString())
                    mqttManager.publishMessage("robot/nav_cmds/driving_finished", "0")
                }
            }
        })

        //robotApi.startNavigation(1, destName, coordinateDeviation, time, navigationListener)
        pausedLocation = destName
    }

    fun resumeNavigation(reqId: Int){
        if(pausedLocation.isNotEmpty()){
            Log.d("RESUME NAVIGATION","Continuing navigation: $pausedLocation")
            startNavigation(0, pausedLocation, 0.1,1000000)
        }
        else{
            Log.d("RESUME NAVIGATION","No last navigated location available")
        }
    }

    fun stopNavigation(reqId: Int) {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        //robotApi.stopNavigation(reqId)
        RobotApi.getInstance().stopGoPosition(0)
        pausedLocation = ""
    }

    fun pauseNavigation(reqId: Int) {
        Log.d("STOP NAVIGATION", "Deteniendo navegación")
        //robotApi.stopNavigation(reqId)
        RobotApi.getInstance().stopGoPosition(0)
    }

    fun stopCharging(){
        while(RobotApi.getInstance().chargeStatus){
            RobotApi.getInstance().stopChargingByApp()
            //Log.d("IS CHARGING", RobotApi.getInstance().chargeStatus.toString())
        }
    }

    fun returnToPosition(){
        //TODO: Save last known coordinates when starting a navigation
        if(lastDestination != ""){
            startNavigation(0,lastDestination,0.1,1000000)
        }
        else{
            speak("Actualmente no existe un destino al que haya ido previamente",false)
        }
    }


    fun speak(text : String, listen: Boolean){
        skillApi.playText(TTSEntity("sid-012345",text), object : TextListener() {
            override fun onStart() {
                mqttManager.publishMessage("robot/interactionState", InteractionState.SPEAKING.toString())
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
                skillApi.setRecognizeMode(listen)
                skillApi.setRecognizable(listen)
                if(listen){
                    mqttManager.publishMessage("robot/interactionState", InteractionState.LISTENING.toString())
                }
                else{
                    mqttManager.publishMessage("robot/interactionState", InteractionState.NONE.toString())
                }
            }
        })
    }

    fun questionPrueba(){
        RobotApi.getInstance().setCallback(object : ModuleCallback() {
            override fun onSendRequest(
                reqId: Int,
                reqType: String,
                reqText: String,
                reqParam: String
            ): Boolean {
                Log.d("onSendRequest Prueba","$reqId, $reqType, $reqText, $reqParam")
                callback_speech_to_speech(reqText)
                mqttManager.publishMessage("test/log","$reqId, $reqType, $reqText, $reqParam")
                return false
            }
        })
    }

    fun question_si_no(question: String, isSiNo: Boolean) {
        questionIsSiNo = isSiNo
        Log.d("QUESTION","Is SiNo: $questionIsSiNo")
        speak(question,true)

        RobotApi.getInstance().setCallback(object : ModuleCallback() {
            override fun onSendRequest(
                reqId: Int,
                reqType: String,
                reqText: String,
                reqParam: String
            ): Boolean {
                Log.d("QUESTION onSendRequestN","Is SiNo: $questionIsSiNo")
                Log.d("onSendRequestN","$reqText")
                if(questionIsSiNo) {
                    if(reqText.contains("si") || reqText.contains("no") || reqText.contains("sí")) {
                        if (reqText.contains("no")) {
                            sendSiNoResponse(false)
                        } else {
                            sendSiNoResponse(true)
                        }
                        mqttManager.publishMessage("robot/interactionState", InteractionState.NONE.toString())
                        mqttManager.publishMessage("robot/voice_cmds/remove_question", "")
                        //mqttManager.publishMessage("robot/notUnderstood",false.toString())
                        speak("Gracias por tu respuesta",false)
                        skillApi.setRecognizable(false)
                    }
                    else{
                        mqttManager.publishMessage("robot/interactionState", InteractionState.SPEAKING.toString())
                        speak("Por favor responde con si o no?",true)
                        mqttManager.publishMessage("robot/notUnderstood",true.toString())

                        //TODO: Screen to confirm si o no
                    }
                }
                else {
                    callback_speech_to_speech(reqText)
                    Log.d("BOOLEAN","$questionIsSiNo")
                    mqttManager.publishMessage("robot/interactionState", InteractionState.NONE.toString())
                    mqttManager.publishMessage("robot/voice_cmds/remove_question", "")
                    speak("Gracias por tu respuesta",false)
                    skillApi.setRecognizable(false)
                }
                return false
            }
        })
    }

    fun question_move() {
        questionIsSiNo = true
        Log.d("QUESTION","Is SiNo: $questionIsSiNo")
        speak("Estoy lo suficiente cerca para tocar mi pantalla?",true)

        RobotApi.getInstance().setCallback(object : ModuleCallback() {
            override fun onSendRequest(
                reqId: Int,
                reqType: String,
                reqText: String,
                reqParam: String
            ): Boolean {
                Log.d("QUESTION onSendRequest","Is SiNo: $questionIsSiNo")
                Log.d("onSendRequestN","$reqText")
                if(questionIsSiNo) {
                    if(reqText.contains("si") || reqText.contains("no") || reqText.contains("sí")) {
                        if (reqText.contains("no")) {
                            //sendSiNoResponse(false)
                            speak("me acerco 10 centimetros",false)
                            question_move()
                            moveForward()
                        } else {
                            //sendSiNoResponse(true)
                            speak("Vale",false)
                            mqttManager.publishMessage("robot/at_chair", "")
                        }
                        mqttManager.publishMessage("robot/interactionState", InteractionState.NONE.toString())
                        mqttManager.publishMessage("robot/voice_cmds/remove_question", "")
                        //mqttManager.publishMessage("robot/notUnderstood",false.toString())
                        //speak("Gracias por tu respuesta",false)
                        skillApi.setRecognizable(false)
                    }
                    else{
                        mqttManager.publishMessage("robot/interactionState", InteractionState.SPEAKING.toString())
                        speak("Por favor responde con si o no?",true)
                        mqttManager.publishMessage("robot/notUnderstood",true.toString())
                    }
                }
                else {
                    callback_speech_to_speech(reqText)
                    Log.d("BOOLEAN","$questionIsSiNo")
                    mqttManager.publishMessage("robot/interactionState", InteractionState.NONE.toString())
                    mqttManager.publishMessage("robot/voice_cmds/remove_question", "")
                    speak("Gracias por tu respuesta",false)
                    skillApi.setRecognizable(false)
                }
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
        Log.d("onSendRequest robotMan","$reqId, $reqType, $reqText, $reqParam")
        callback_speech_to_speech(reqText)
        mqttManager.publishMessage("test/log","$reqId, $reqType, $reqText, $reqParam")
        Log.d("QUESTION onSendRequest","Is SiNo: $questionIsSiNo")
        if(questionIsSiNo) {
            if(reqText.contains("si") || reqText.contains("no") || reqText.contains("sí")) {
                if (reqText.contains("no")) {
                    sendSiNoResponse(false)
                } else {
                    sendSiNoResponse(true)
                }
            }
        }
        else {
            callback_speech_to_speech(reqText)
            Log.d("BOOLEAN","$questionIsSiNo")
        }
        skillApi.setRecognizable(false)
        return false
    }

    fun setRecognizable(listen: Boolean){
        Log.d("SET RECOGNIZABLE","$listen")
        skillApi.setRecognizeMode(listen)
        skillApi.setRecognizable(listen)
    }

    fun registerCallback(){
        skillApi.registerCallBack(object : SkillCallback(){
            override fun onSpeechParResult(s: String) {
                // Resultado temporal del reconocimiento de voz
                //Log.d("RobotViewModel ASR", s)
                //robotMan.callback_speech_to_speech(s)
            }

            override fun onStart() {
                // Inicio del reconocimiento
                //Log.d("RobotViewModel ASR", "onStart")
            }

            override fun onStop() {
                // Fin del reconocimiento
                //Log.d("RobotViewModel ASR", "onStop")
            }

            override fun onVolumeChange(volume: Int) {
                // Cambio en el volumen de la voz reconocida
                //Log.d("RobotViewModel ASR", "onVolumeChange")
            }

            override fun onQueryEnded(status: Int) {
                // Manejar el fin de la consulta basado en el estado
                //Log.d("RobotViewModel ASR", "onQueryEnded")
            }

            override fun onQueryAsrResult(asrResult: String) {
                // asrResult: resultado final del reconocimiento
                //Log.d("RobotViewModel ASR", asrResult)
                //callback_speech_to_speech(asrResult)
            }
        })
        skillApi.setRecognizable(false)
    }
    


    fun getRobotInterfaceMethod(): RobotInterface {
        return robotInterface
    }

    fun moveForward() {
        RobotApi.getInstance().goForward(0, 0.1F,0.1F,false, CommandListener())
    }

    fun goCharge() {
        speak("He esperado demasiado tiempo. Vuelvo a mi puesto.", false)
        unregisterPersonListener()
        RobotApi.getInstance().goCharging(0)
    }
    
    fun wakeUp() {
        RobotApi.getInstance().stopCharge(0)
    }

    fun updateCurrentDestination(_currentDestination : String){
        lastDestination = currentDestination
        currentDestination = _currentDestination
    }

    fun scheduleWithCoroutine() = runBlocking {

        launch {
            //stopCharging()
            delay(6000L)
            startNavigation(0,"entrada",0.1234,0)
        }
    }

}