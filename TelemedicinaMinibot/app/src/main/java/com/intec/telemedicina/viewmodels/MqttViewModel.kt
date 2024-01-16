package com.intec.telemedicina.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.constraintlayout.motion.utils.ViewState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.Person
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.intec.telemedicina.data.Face
import com.intec.telemedicina.data.InteractionState
import com.intec.telemedicina.mqtt.MQTTConfig
import com.intec.telemedicina.mqtt.MqttManager
import com.intec.telemedicina.mqtt.MqttManagerCallback
import com.intec.telemedicina.mqtt.MqttMessageListener
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.preferences.PreferencesRepository
import com.intec.telemedicina.robotinterface.RobotManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MqttViewModel @Inject constructor(
    application: Application,
    robotMan : RobotManager,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application), MqttMessageListener {


    //COUNTDOWN DEL ESTADO DE PAUSA
    //TO DO: AÑADIR A SETPREFERENCES LA VARIABLE PARA PODER EDITARLA
    private val _countdownState = MutableStateFlow(5)
    val countdownState: StateFlow<Int> = _countdownState

    val countdownFlag = MutableStateFlow(false)

    //VARIABLES PARA LA ESCUCHA ACTIVA DE UNKNOWN VISITS
    private val _capturedText = MutableStateFlow("")
    val capturedText: StateFlow<String> = _capturedText

    private val _isListening = MutableLiveData<Boolean>()
    val isListening: LiveData<Boolean> = _isListening

    var robotMan = robotMan

    fun stopListening() {
        // Detener el reconocimiento de voz
        _isListening.value = false
    }

    fun setCountdownFlagState(newState: Boolean) {
        countdownFlag.value = newState
        Log.d("ADMIN STATE", adminState.value.toString())
    }

    var coutndownJob : Job? = null
    fun startCountdown() {
        coutndownJob = viewModelScope.launch {
            var currentCount = 5
            while (currentCount >= 0) {
                _countdownState.value = currentCount
                delay(1000)
                currentCount--
            }
            coutndownJob?.cancel()
            robotMan.resumeNavigation(0)
            setCountdownFlagState(true)
        }
    }

    private var detectionJob: Job? = null

    //Admin Mode
    val adminState = MutableStateFlow(false)

    private val _connectionState = mutableStateOf("Disconnected")
    val connectionState get()= _connectionState
    var navigation : Boolean = false
    private var _mqttQuestion = MutableLiveData<String>()
    val mqttQuestion : MutableLiveData<String> get() = _mqttQuestion

    val posesList = MutableStateFlow(emptyList<Pose>())

    var faceType = MutableStateFlow(Face.NEUTRAL)
    var interactionState = MutableStateFlow(InteractionState.NONE)
    var isDriving = MutableStateFlow(false)
    var isPaused = MutableStateFlow(false)
    var question = MutableStateFlow("")
    var notUnderstood = MutableStateFlow(false)

    val showQuestionsDialog = MutableStateFlow(false)
    val showWelcomeDialog = MutableStateFlow(false)

    val showDrivingScreenFace = MutableStateFlow(false)
    val closeDrivingScreenFace = MutableStateFlow(false)

    val openHomeScreen = MutableStateFlow(false)
    val openEyesScreen = MutableStateFlow(false)

    var initiated_status = false

    private val mqttCallback = MqttManagerCallback(_connectionState, {
        val updatedMessages = _incomingMessages.value.toMutableList()
        updatedMessages.add(it)
        _incomingMessages.value = updatedMessages
    }, this)

    // LiveData para cada configuración
    val brokerIp: MutableLiveData<String> = MutableLiveData()
    val brokerPort: MutableLiveData<String> = MutableLiveData()
    val brokerUser: MutableLiveData<String> = MutableLiveData()
    val brokerPassword: MutableLiveData<String> = MutableLiveData()
    val brokerQoS: MutableLiveData<String> = MutableLiveData()
    val brokerClient: MutableLiveData<String> = MutableLiveData()
    val idleWaitingTime: MutableLiveData<Int> = MutableLiveData()
    val meetingTimeThreshold: MutableLiveData<Int> = MutableLiveData()
    val apiUser: MutableLiveData<String> = MutableLiveData()
    val apiPassword: MutableLiveData<String> = MutableLiveData()

    private val mqttManager : MqttManager
    private var mqttConfigInstance = MQTTConfig(
        SERVER_URI = "tcp://192.168.2.243:1883",
        client_id = "Robot",
        qos = 0,
        user = "intecfull",
        password = "intecfullpassword",
        waitingIdleTime = 10,
        meetingTimeThreshold = 10,
        apiUser = "api_default_user",
        apiPassword = "api_default_pass"
    )

    private val _navigationState = MutableStateFlow(NavigationState.EyesScreen)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    enum class NavigationState {
        EyesScreen, HomeScreen, NumericPanelScreen, MeetingScreen, UnknownVisitsScreen
    }

    // Observador para cambios en brokerIp
    private val brokerIpObserver = Observer<String> { nuevaIp ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerPort
    private val brokerPortObserver = Observer<String> { nuevoPuerto ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerUser
    private val brokerUserMqttObserver = Observer<String> { nuevoUser ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerPassword
    private val brokerPasswordMqttObserver = Observer<String> { nuevoPassword ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerQoS
    private val brokerQoSMqttObserver = Observer<String> { nuevoQoS ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerClient
    private val brokerClientMqttObserver = Observer<String> { nuevoClient ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en waiting Idle Time
    private val waitingIdleTimeObserver = Observer<Int> { nuevoWaitingIdleTime ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en Meeting time
    private val meetingTimeThresholdObserver = Observer<Int> { nuevomeetingTimeThreshold ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en API User
    private val apiUserObserver = Observer<String> { nuevoApiUser ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en API Password
    private val apiPasswordObserver = Observer<String> { nuevoApiPassword ->
        actualizarConfiguracionMQTT()
    }

    fun getBrokerIpDefaultValue(): String {
        return preferencesRepository.getBrokerIp()
    }

    fun getBrokerPortDefaultValue(): String {
        return preferencesRepository.getBrokerPort()
    }

    fun getBrokerUserDefaultValue(): String {
        return preferencesRepository.getMqttUsuario()
    }

    fun getBrokerPasswordDefaultValue(): String {
        return preferencesRepository.getMqttPassword()
    }

    fun getBrokerQoSDefaultValue(): String {
        return preferencesRepository.getMqttQoS()
    }

    fun getBrokerClientDefaultValue(): String {
        return preferencesRepository.getMqttClient()
    }

    fun getWaitingTimeDefaultValue(): Int {
        return preferencesRepository.getIdleWaitingTime()
    }

    fun getApiUserDefaultValue(): String {
        return preferencesRepository.getApiUsuario()
    }

    fun getApiPasswordDefaultValue(): String {
        return preferencesRepository.getApiPassword()
    }

    fun getApiIdleWaitingTimeDefaultValue(): Int {
        return preferencesRepository.getIdleWaitingTime()
    }

    fun getMeetingTimeThresholdDefaultValue(): Int {
        return preferencesRepository.getMeetingTimeThreshold()
    }


    init {
        mqttManager = MqttManager(getApplication(), mqttCallback, mqttConfigInstance, application)

        Log.d("Init MqttViewModel", "MqttManager created: $mqttManager")
        brokerIp.value = preferencesRepository.getBrokerIp()
        brokerPort.value = preferencesRepository.getBrokerPort()
        brokerUser.value = preferencesRepository.getMqttUsuario()
        brokerPassword.value = preferencesRepository.getMqttPassword()
        brokerQoS.value = preferencesRepository.getMqttQoS()
        brokerClient.value = preferencesRepository.getMqttClient()
        idleWaitingTime.value = preferencesRepository.getIdleWaitingTime()
        meetingTimeThreshold.value = preferencesRepository.getMeetingTimeThreshold()
        apiUser.value = preferencesRepository.getApiUsuario()
        apiPassword.value = preferencesRepository.getApiPassword()

        // Configurar la observación de cambios
        brokerIp.observeForever(brokerIpObserver)
        brokerPort.observeForever(brokerPortObserver)
        brokerUser.observeForever(brokerUserMqttObserver)
        brokerPassword.observeForever(brokerPasswordMqttObserver)
        brokerQoS.observeForever(brokerQoSMqttObserver)
        brokerClient.observeForever(brokerClientMqttObserver)
        idleWaitingTime.observeForever(waitingIdleTimeObserver)
        meetingTimeThreshold.observeForever(meetingTimeThresholdObserver)
        apiUser.observeForever(apiUserObserver)
        apiPassword.observeForever(apiPasswordObserver)
        actualizarConfiguracionMQTT()
        //TODO Inicializar los LiveData para cada configuración

        robotMan.onPersonDetected = { personList ->
            if (!personList.isNullOrEmpty() && !hasHandledPersonDetection) {
                hasHandledPersonDetection = true
                detectionJob?.cancel()
                navigateToHomeScreen()
                robotMan.questionPrueba()
                listenToSpeechResult()
            } else {
                startPersonDetection(20)
            }
        }
    }

    private fun actualizarConfiguracionMQTT() {
        brokerIp.value?.let {serverUri ->

            val port = preferencesRepository.getBrokerPort()
            val clientId = preferencesRepository.getMqttClient()
            val user = preferencesRepository.getMqttUsuario()
            val password = preferencesRepository.getMqttPassword()
            // Obtener otros campos
            val apiUser = preferencesRepository.getApiUsuario()
            val apiPassword = preferencesRepository.getApiPassword()
            val idleWaitingTime = preferencesRepository.getIdleWaitingTime()
            val meetingTimeThreshold = preferencesRepository.getMeetingTimeThreshold()

            val fullServerUri = "tcp://$serverUri:$port"

            // Actualizar la configuración
            mqttConfigInstance = mqttConfigInstance.copy(
                SERVER_URI = fullServerUri,
                client_id = clientId,
                user = user,
                password = password,
                apiUser = apiUser,
                apiPassword = apiPassword,
                waitingIdleTime = idleWaitingTime,
                meetingTimeThreshold = meetingTimeThreshold
            )

            mqttManager.actualizarConfiguracion(mqttConfigInstance)  // Suponiendo que tienes un método para actualizar la configuración
        }
    }
    fun guardarConfiguracion(
        ip: String,
        port: String,
        mqttUser: String,
        mqttPassword: String,
        mqttQoS: String,
        mqttClient: String,
        waitingIdleTime: Int,
        receivedMeetingTimeThreshold: Int,
        receivedApiUser: String,
        receivedApiPassword: String
    ) {
        Log.d("MqttViewModel", "Guardando configuración: $ip")
        preferencesRepository.setBrokerIp(ip)
        preferencesRepository.setBrokerPort(port)
        preferencesRepository.setMqttUsuario(mqttUser)
        preferencesRepository.setMqttPassword(mqttPassword)
        preferencesRepository.setMqttQoS(mqttQoS)
        preferencesRepository.setMqttClient(mqttClient)
        preferencesRepository.setIdleWaitingTime(waitingIdleTime)
        preferencesRepository.setMeetingTimeThreshold(receivedMeetingTimeThreshold)
        preferencesRepository.setApiUsuario(receivedApiUser)
        preferencesRepository.setApiPassword(receivedApiPassword)
        // Guardar otros campos en SharedPreferences
        // No olvides actualizar los LiveData
        brokerIp.value = ip
        brokerPort.value = port
        brokerUser.value = mqttUser
        brokerPassword.value = mqttPassword
        brokerQoS.value = mqttQoS
        brokerClient.value = mqttClient
        idleWaitingTime.value = waitingIdleTime
        meetingTimeThreshold.value = receivedMeetingTimeThreshold
        apiUser.value = receivedApiUser
        apiPassword.value = receivedApiPassword
        // ...
    }
    override fun onCleared() {
        super.onCleared()
        brokerIp.removeObserver(brokerIpObserver)
        brokerPort.removeObserver(brokerPortObserver)
        brokerUser.removeObserver(brokerUserMqttObserver)
        brokerPassword.removeObserver(brokerPasswordMqttObserver)
        brokerQoS.removeObserver(brokerQoSMqttObserver)
        brokerClient.removeObserver(brokerClientMqttObserver)
        idleWaitingTime.removeObserver(waitingIdleTimeObserver)
        meetingTimeThreshold.removeObserver(meetingTimeThresholdObserver)
        apiUser.removeObserver(apiUserObserver)
        apiPassword.removeObserver(apiPasswordObserver)
    }

    fun listenToSpeechResult() {
        robotMan.onSpeechResultReceived = { speechResult ->
            if (speechResult.isNotEmpty()) {
                Log.d("listenToSpeechResult", "speechResult: $speechResult")
                onResultUnknownVisitor(speechResult)
                Log.d("listenToSpeechResult", "actualiza capturedText $_capturedText")
                processSpeechResult(speechResult)
            }
        }
    }

    fun onResultUnknownVisitor(text: String){
        // Actualizar el texto cuando se recibe un resultado
        Log.d("onResultUnknownVisitor", "text: $text")
        _capturedText.value = text
        Log.d("onResultUnknownVisitor", "actualiza capturedText $_capturedText")
    }

    private fun processSpeechResult(speechResult: String) {
        Log.d("processSpeechResult", "speechResult: $speechResult")
        if (containsMeetingKeyword(speechResult)) {
            // Lógica cuando se detectan palabras clave
            navigateToNumericPanelScreen()
        }
        else if(containsVisitKeyword(speechResult)){
            Log.d("speechResult", "Se ha detectado una visita")
            navigateToUnknownVisitsScreen()
        }
        else if(containsDealerKeyword(speechResult)){
            Log.d("speechResult", "Se ha detectado un repartidor")
        }
        else if(containSiWord(speechResult)){
            Log.d("speechResult", "Se ha detectado un si")
            robotMan.stopFocusFollow()
            robotMan.speak("Deacuerdo, por aquí por favor", false)
            robotMan.startNavigation(0,"reunion",0.1234,0)
        }
        else {
            Log.d("speechResult", "No se ha detectado nada")
            // Lógica cuando no se detectan palabras clave
            repeatCommand()
        }
    }

    // Variable de control
    private var hasHandledPersonDetection = false

    private fun navigateToHomeScreen() {
        detectionJob?.cancel()
        robotMan.speak("Bienvenidos a t, dos, o media. Dígame en qué puedo ayudarle", true)
        _navigationState.value = NavigationState.HomeScreen
    }

    private fun navigateToNumericPanelScreen() {
        detectionJob?.cancel()
        robotMan.speak("Deacuerdo, introduce el código que se te ha proporcionado", false)
        _navigationState.value = NavigationState.NumericPanelScreen
    }

    private fun navigateToUnknownVisitsScreen() {
        detectionJob?.cancel()
        //robotMan.speak("Deacuerdo, introduce el código que se te ha proporcionado", false)
        _navigationState.value = NavigationState.UnknownVisitsScreen
    }

    private fun repeatCommand() {
        robotMan.speak("Por favor repita el comando", true)
        _navigationState.value = NavigationState.HomeScreen // Solo navegar aquí si es necesario
    }

    fun startPersonDetection(waitTimeInSeconds: Int) {
        //Log.d("startPersonDetection", "$waitTimeInSeconds")
        //detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            val waitTimeInMillis = waitTimeInSeconds * 1000L
            var elapsedTime = 0L // Reinicia el temporizador
            var detectedPerson : List<Person>? = robotMan.detectPerson(0)

            while (detectedPerson.isNullOrEmpty() && elapsedTime < waitTimeInMillis) {

                delay(1000)
                elapsedTime += 1000
                //Log.d("startDetection", "CURRENT ELAPSED TIME: $elapsedTime is less than $waitTimeInMillis. current detection state: ${detectedPerson.isNullOrEmpty()}")

            }

            if(!detectedPerson.isNullOrEmpty()){
                //Log.d("startDetection", "PERSONS LIST IS NOT NULL NEITHER EMPTY. RESTARTING TIME")
                detectionJob?.cancel()
                elapsedTime = 0 // Reinicia el temporizador
                //detectionJob?.cancel()
            }

            if (detectedPerson.isNullOrEmpty() && elapsedTime >= waitTimeInMillis) {
                Log.d("startDetection", "ELAPSED TIME: $elapsedTime, detection state: ${detectedPerson.isNullOrEmpty()}")
                robotMan.unregisterPersonListener()
                _navigationState.value = NavigationState.EyesScreen
                //robotMan.goCharge()
            }
        }
    }

    private fun containSiWord(text: String): Boolean {
        val keywords = listOf(
            "si", "sí", "Si", "Sí"
        )
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun containsDealerKeyword(text: String): Boolean {
        val keywords = listOf(
            "mensajero", "mensajería",
            "paquete", "envío","repartidor","reparto",
            "entrega", "recogida",
            "courier", "paquetería",
            "envío express", "envío urgente",
            "correspondencia", "bulto",
            "caja", "envoltorio",
            "sobre", "remesa",
            "postal", "carta",
            "parcelservice", "delivery service",
            "shipping", "shipment",
            "mail", "package delivery",
            "freight", "cargo",
            "consignment", "dispatch",
            "postal service", "express delivery"
        )
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun containsVisitKeyword(text: String): Boolean {
        val keywords = listOf("visita") // Agrega más palabras clave según sea necesario
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun containsMeetingKeyword(text: String): Boolean {
        val keywords = listOf(
            "reunión", "meeting",
            "encuentro", "asamblea",
            "junta", "congregación",
            "convocatoria", "sesión",
            "conferencia", "simposio",
            "cónclave", "foro",
            "seminario", "taller",
            "charla", "coloquio",
            "conclave", "plenaria",
            "mesa redonda", "panel",
            "conversatorio", "debate",
            "simposium", "webinar",
            "videoconferencia", "teleconferencia",
            "meet", "gathering",
            "assembly", "symposium",
            "conference", "seminar",
            "workshop", "discussion",
            "talk", "forum",
            "roundtable", "panel discussion",
            "webinar", "video call",
            "teleconference"
        )
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    // State para mensajes entrantes
    private val _incomingMessages = mutableStateOf(emptyList<String>())
    val incomingMessages: State<List<String>> get() = _incomingMessages

    // Añadir un mensaje a la lista de mensajes recibidos
    private fun addIncomingMessage(message: String) {
        Log.d("MqttViewModel", "Nuevo mensaje recibido: $message")
        val currentMessages = _incomingMessages.value ?: emptyList()
        _incomingMessages.value = currentMessages + message
    }

    fun connect() {
        Log.d("MQTTViewModel", "Connecting to broker: ${mqttConfigInstance.SERVER_URI}")
        Log.d("MQTTViewModel", "Client id: ${mqttConfigInstance.client_id}")
        Log.d("MQTTViewModel", "User: ${mqttConfigInstance.user}")
        Log.d("MQTTViewModel", "Pwd: ${mqttConfigInstance.password}")
        addIncomingMessage("Connecting to broker: ${mqttConfigInstance.SERVER_URI}")
        mqttManager.connect()
        initiated_status = true
        //subscribeToAllTopics(resumeTopics())
    }

    fun disconnect() {
        addIncomingMessage("Disconnected")
        mqttManager.disconnect()
        initiated_status = false
    }

    fun getInitiatedStatus() : Boolean {
        return initiated_status
    }

    fun subscribeToTopic(topic: String) {
        Log.d("MQTTViewModel", "Subscribing to topic: $topic")
        addIncomingMessage("Subscribing to topic: $topic")
        mqttManager.subscribeToTopic(topic)
    }

    fun resumeTopics(): MutableList<String> {
        Log.d("MQTTViewModel", "Resuming topics")
        addIncomingMessage("Resuming topics")
        for (topic in mqttManager.getTopics()) {
            Log.d("MQTTViewModel", "Current subscribing topic: $topic")
            addIncomingMessage("Current subscribing topic: $topic")
        }
        return mqttManager.resumeTopics()
    }

    fun subscribeToAllTopics(topics: MutableList<String>) {
        Log.d("MQTTViewModel", "Subscribing to all topics")
        addIncomingMessage("Subscribing to all topics")
        //mqttManager.subscribeToAllTopics(topics)
    }

    fun publishMessage(topic: String, message: String) {
        Log.d("MQTTViewModel", "Publishing message: $message to topic: $topic")
        addIncomingMessage("Publishing message: $message to topic: $topic")
        mqttManager.publishMessage(topic, message)
    }
    override fun onMessageReceived(topic: String, message: String) {
        addIncomingMessage(message)
        Log.d("MQTT Message","$topic: $message")

        when(topic){
            //"robot/nav_pub/status" -> robotApi.currentPose
            "robot/nav_cmds/go_to" -> {
                isDriving.value = true
                //Log.d("MQTTViewModel", "Starting navigation to: $message")
                //robotApi.startNavigation(1, message.toString(), 0.01, 100000, navigationListener)
                //RobotManager(getApplication<Application>().applicationContext).getRobotInterfaceMethod().startNavigation(1, message.toString(), 0.01, 100000)

                //Log.d("Poses",robotMan.getPoses().toString())
                //robotMan.printPoses()
                //robotMan.startNavigation(0,"sillon",0.1234,0)

                Log.d("DISTANCE TO SILLON",RobotApi.getInstance().getPlaceDistance("sillon").toString())
                /*Log.d("DISTANCE IN FRONT",
                    //RobotApi.getInstance().queryRadarStatus(0, CommandListener()).toString()
                    ""
                )*/
                //RobotApi.getInstance().setObstaclesSafeDistance(0,1.0, CommandListener())
                Log.d("UPDATE SAFE DISTANCE","Update the safe distance")

                //RobotApi.getInstance().startInspection(0,100000, ActionListener())
                robotMan.startNavigation(0,message.toString(),0.1234,0)

                //RobotApi.getInstance().goForward(0, 0.2F,0.1F,false, CommandListener())


                //RobotApi.getInstance().goForward(0, 0.3F, 0.1F, CommandListener())
                //RobotManager(getApplication<Application>().applicationContext).getRobotInterfaceMethod().goPosition()
            }
            /*"robot/nav_cmds/go_to_coord" -> robotApi.startNavigation(1, message,0.01, 100000, navigationListener)*/
            "robot/nav_cmds/go_charger" -> RobotApi.getInstance().goCharging(0)
            "robot/nav_cmds/stop_navigation" -> {
                //robotApi.stopNavigation(1)
                robotMan.stopNavigation(0)
                isDriving.value = false
            }

            "robot/nav_cmds/pause_navigation" -> {
                robotMan.pauseNavigation(0)
                isDriving.value = false
            }

            "robot/nav_cmds/resume_navigation" -> {
                robotMan.resumeNavigation(0)
                isDriving.value = true
                isPaused.value = false
            }

            //navigationListener.onStatusUpdate(Definition.ACTION_NAVI_STOP_MOVE,"YESSSSS")
            "robot/voice_cmds/text_to_speech" -> {
                Log.d("TextToSpeech", message)
                //playTextViaTTS(message)
                robotMan.speak(message,false)
            }

            "robot/voice_cmds/question_si_no" -> {
                question.value = message
            }

            "robot/voice_cmds/question" -> {
                //Log.d("Question",message)
                //_mqttQuestion.value = message
                question.value = message
                //showQuestionsDialog()
                //Open window with question --> Yes/No
                //Send response back
            }
            "robot/voice_cmds/remove_question" -> {
                question.value = ""
                notUnderstood.value = false
            }
            "robot/welcome_cmd" -> { //return answer on --> "robot/welcome_pub"
                //robotApi.startNavigation(1, "Punto de recepción", 0.01, 100000, navigationListener)
                showWelcomeDialog()
            }
            "robot/focus" -> {
                val personList: List<Person> = PersonApi.getInstance().getAllPersons()
                Log.d("PERSON LIST",personList.toString())
                Log.d("FOCUS","We will try to focus on the user!")
                /*RobotApi.getInstance().startFocusFollow(
                    0,
                    personList.get(0).remoteFaceId.toInt(),
                    1000000,
                    5F,
                    object : ActionListener() {
                        override fun onStatusUpdate(status: Int, data: String?) {
                            when (status) {
                                Definition.STATUS_TRACK_TARGET_SUCCEED -> {Log.d("Focus", "Focus on person")}
                                Definition.STATUS_GUEST_LOST -> {Log.d("Focus", "Focus on person lost")}
                                Definition.STATUS_GUEST_FARAWAY -> {Log.d("Focus", "Focus on person faraway")}
                                Definition.STATUS_GUEST_APPEAR -> {Log.d("Focus", "Focus on person appear")}
                            }
                        }

                        override fun onError(errorCode: Int, errorString: String?) {
                            when (errorCode) {
                                Definition.ERROR_SET_TRACK_FAILED, Definition.ERROR_TARGET_NOT_FOUND -> {Log.d("Focus", "Focus on person failed")}
                                Definition.ACTION_RESPONSE_ALREADY_RUN -> {Log.d("Focus", "Focus on person already running")}
                                Definition.ACTION_RESPONSE_REQUEST_RES_ERROR -> {Log.d("Focus", "Focus on person request res error")}
                            }
                        }

                        override fun onResult(status: Int, responseString: String?) {
                            //Log.d(TAG, "startTrackPerson onResult status: $status")
                            when (status) {
                                Definition.ACTION_RESPONSE_STOP_SUCCESS -> {}
                            }
                        }
                    })*/
            }

            "robot/unfocus" -> {
                Log.d("UNFOCUS","We will not focus on the user anymore!")
                RobotApi.getInstance().stopFocusFollow(0)
            }
            "robot/move_forward" -> {
                robotMan.moveForward()
            }
            "robot/stop_stt" -> {
                Log.d("STT","Listening disabled")
                robotMan.setRecognizable(false)
            }
            "robot/faceType" -> {
                faceType.value = Face.valueOf(message)
            }
            "robot/interactionState" -> {
                interactionState.value = InteractionState.valueOf(message)
            }
            "robot/close_screen" -> {
                closeDrivingScreenFace.value = true
            }
            "robot/wake_up" -> {
                robotMan.wakeUp()
            }
            "robot/nav_cmds/driving_finished" -> {
                Log.d("DRIVINGFINISHED","Preparing home screen")
                isDriving.value = false
            }
            "robot/open_homescreen" -> {
                openHomeScreen.value = true
            }
            "robot/notUnderstood" -> {
                notUnderstood.value = true
            }
            "robot/nav_cmds/request_move" -> {
            }
            "zigbee2mqtt/Pulsador/action" -> {
                Log.d("ZIGBEE", message.toString())
                if(message.toString() == "single"){
                    if(RobotApi.getInstance().chargeStatus) {
                        robotMan.scheduleWithCoroutine()
                    }else{
                        robotMan.startNavigation(0,"entrada",0.1234,0)
                    }
                }
            }
        }
    }

    fun closeEyescreen(){
        openEyesScreen.value = false
    }

    fun closeHomescreen(){
        openHomeScreen.value = false
    }

    fun getListPoses(){
        posesList.value = robotMan.getPoses()
    }

    fun deactivateOpenDrivingScreenFace(){
        showDrivingScreenFace.value = false
    }

    fun deactivateCloseDrivingScreenFace(){
        closeDrivingScreenFace.value = false
    }

    // LiveData to handle keyboard hide state
    private val _shouldHideKeyboard = mutableStateOf(false)
    val shouldHideKeyboard: State<Boolean> get() = _shouldHideKeyboard

    // Function to trigger hiding the keyboard
    fun triggerHideKeyboard() {
        Log.d("MQTTViewModel", "Triggering hide keyboard")
        _shouldHideKeyboard.value = true
    }

    fun resetHideKeyboardTrigger() {
        _shouldHideKeyboard.value = false
    }

    fun showQuestionsDialog(){
        Log.d("MQTTViewModel", "Showing questions dialog")
        showQuestionsDialog.value = true
    }

    fun hideQuestionsDialog(){
        Log.d("MQTTViewModel", "Hiding questions dialog")
        showQuestionsDialog.value = false
    }

    fun showWelcomeDialog(){
        Log.d("MQTTViewModel", "Showing welcome dialog")
        showWelcomeDialog.value = true
    }

    fun hideWelcomeDialog(){
        Log.d("MQTTViewModel", "Hiding welcome dialog")
        showWelcomeDialog.value = false
    }

    fun setPaused(isPaused_temp : Boolean) {
        isPaused.value = isPaused_temp
        robotMan.pauseNavigation(0)
    }

    fun setAdminState(newState: Boolean) {
        adminState.value = newState
        Log.d("ADMIN STATE", adminState.value.toString())
    }
}
