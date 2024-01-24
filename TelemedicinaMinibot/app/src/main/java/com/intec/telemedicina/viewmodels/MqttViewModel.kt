package com.intec.telemedicina.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.Person
import com.intec.telemedicina.data.APIConfig
import com.intec.telemedicina.data.Face
import com.intec.telemedicina.data.InteractionState
import com.intec.telemedicina.data.RobotConfig
import com.intec.telemedicina.mqtt.MQTTConfig
import com.intec.telemedicina.mqtt.MqttManager
import com.intec.telemedicina.mqtt.MqttManagerCallback
import com.intec.telemedicina.mqtt.MqttMessageListener
import com.intec.telemedicina.preferences.PreferencesRepository
import com.intec.telemedicina.robotinterface.RobotManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MqttViewModel @Inject constructor(
    application: Application,
    var robotMan: RobotManager,
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

    //MEETING SECUENCE
    fun stopListening() {
        // Detener el reconocimiento de voz
        _isListening.value = false
    }

    fun setCountdownFlagState(newState: Boolean) {
        countdownFlag.value = newState
        Log.d("ADMIN STATE", adminState.value.toString())
    }

    var coutndownJob: Job? = null
    fun startCountdown() {
        coutndownJob = viewModelScope.launch {
            var currentCount = 5
            while (currentCount >= 0) {
                _countdownState.value = currentCount
                delay(1000)
                currentCount--
            }
            coutndownJob?.cancel()
            robotMan.resumeNavigation()
            setCountdownFlagState(true)
        }
    }

    private var detectionJob: Job? = null

    //Admin Mode
    val adminState = MutableStateFlow(false)

    private val _connectionState = mutableStateOf("Disconnected")
    val connectionState get() = _connectionState
    var navigation: Boolean = false

    val posesList = MutableStateFlow(emptyList<Pose>())

    // Variable para almacenar el elemento seleccionado
    var selectedItem = mutableStateOf<String?>(null)
        private set

    // Función para actualizar el elemento seleccionado
    fun setSelectedItem(item: String) {
        selectedItem.value = item
    }

    var faceType = MutableStateFlow(Face.NEUTRAL)
    var interactionState = MutableStateFlow(InteractionState.NONE)
    var isDriving = MutableStateFlow(false)
    var isPaused = MutableStateFlow(false)
    var isFinished = MutableStateFlow(false)
    var question = MutableStateFlow("")
    var notUnderstood = MutableStateFlow(false)

    val closeDrivingScreenFace = MutableStateFlow(false)

    val openEyesScreen = MutableStateFlow(false)

    var initiatedStatus = mutableStateOf(false)
        private set

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
    var returnDestination: MutableLiveData<String> = MutableLiveData()
    val coordinateDeviation: MutableLiveData<Float> = MutableLiveData()
    val navigationTimeout: MutableLiveData<Long> = MutableLiveData()

    private val mqttManager: MqttManager
    private var mqttConfigInstance = MQTTConfig(
        SERVER_URI = "tcp://192.168.2.243:1883",
        client_id = "Robot",
        qos = 0,
        user = "intecfull",
        password = "intecfullpassword"
    )

    private var apiConfigInstance =
        APIConfig(APIUser = "api_default_user", APIPassword = "api_default_pass")

    private var robotConfigInstance = RobotConfig(
        idleWaitingTime = 10,
        meetingTimeThreshold = 10,
        returnDestination = "entrada",
        coordinateDeviation = 0.1,
        navigationTimeout = 1000000
    )

    //NAVEGACIÓN ENTRE SCREENS
    private val _navigationState = MutableStateFlow(NavigationState.EyesScreen)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val _navigationFinished = MutableStateFlow(false)
    val navigationFinished: StateFlow<Boolean> = _navigationFinished.asStateFlow()

    // Variable de control
    private var hasHandledPersonDetection = false

    enum class NavigationState {
        EyesScreen, HomeScreen, NumericPanelScreen, MeetingScreen, UnknownVisitsScreen, PackageAndMailManagementScreen, DrivingScreen, MqttScreen, AdminPanelScreen, ClockInScreen
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

    //CAMBIOS API
    // Observador para cambios en API User
    private val apiUserObserver = Observer<String> { nuevoApiUser ->
        actualizarConfiguracionAPI()
    }

    // Observador para cambios en API Password
    private val apiPasswordObserver = Observer<String> { nuevoApiPassword ->
        actualizarConfiguracionAPI()
    }

    //CAMBIOS ROBOT
    // Observador para cambios en waiting Idle Time
    private val waitingIdleTimeObserver = Observer<Int> { nuevoWaitingIdleTime ->
        actualizarConfiguracionRobot()
    }

    // Observador para cambios en Meeting time
    private val meetingTimeThresholdObserver = Observer<Int> { nuevomeetingTimeThreshold ->
        actualizarConfiguracionRobot()
    }

    // Observador para cambios en API Password
    private val returnDestinationObserver = Observer<String> { nuevoreturnDestination ->
        actualizarConfiguracionRobot()
    }

    private val coordinateNavigationObserver = Observer<Float> { nuevoCoordinateDeviation ->
        actualizarConfiguracionRobot()
    }

    private val navigationTimeoutObserver = Observer<Long> { nuevoNavigationTimeout ->
        actualizarConfiguracionRobot()
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

    fun getMeetingTimeThresholdDefaultValue(): Int {
        return preferencesRepository.getMeetingTimeThreshold()
    }

    fun getReturnDestinationDefaultValue(): String {
        return preferencesRepository.getReturnDestination()
    }

    fun setReturnDestinationDefaultValue() {
        returnDestination.value = getReturnDestinationDefaultValue()
    }

    fun getCoordinateDeviationDefaultValue(): Float {
        return preferencesRepository.getCoordinateDeviation()
    }

    fun getNavigationTimeoutDefaultValue(): Long {
        return preferencesRepository.getNavigationTimeout()
    }

    init {
        mqttManager = MqttManager(getApplication(), mqttCallback, mqttConfigInstance, application)

        getListPoses()

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
        returnDestination.value = preferencesRepository.getReturnDestination()
        coordinateDeviation.value = preferencesRepository.getCoordinateDeviation()
        navigationTimeout.value = preferencesRepository.getNavigationTimeout()

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
        returnDestination.observeForever(returnDestinationObserver)
        coordinateDeviation.observeForever(coordinateNavigationObserver)
        navigationTimeout.observeForever(navigationTimeoutObserver)
        //actualizarConfiguracionMQTT()
        //TODO Inicializar los LiveData para cada configuración

        robotMan.onPersonDetected = { personList ->
            if (!personList.isNullOrEmpty() && !hasHandledPersonDetection) {
                hasHandledPersonDetection = true
                detectionJob?.cancel()
                navigateToHomeScreen()
                robotMan.questionPrueba()
                listenToSpeechResult()
            } else {
                startPersonDetection(robotConfigInstance.idleWaitingTime)
            }
        }
    }

    private fun actualizarConfiguracionMQTT() {
        brokerIp.value?.let { serverUri ->
            //Obtener campos MQTT
            val port = preferencesRepository.getBrokerPort()
            val clientId = preferencesRepository.getMqttClient()
            val user = preferencesRepository.getMqttUsuario()
            val password = preferencesRepository.getMqttPassword()

            val fullServerUri = "tcp://$serverUri:$port"

            // Actualizar la configuración mqtt
            mqttConfigInstance = mqttConfigInstance.copy(
                SERVER_URI = fullServerUri,
                client_id = clientId,
                user = user,
                password = password
            )

            mqttManager.actualizarConfiguracion(mqttConfigInstance)  // Suponiendo que tienes un método para actualizar la configuración
        }
    }

    fun actualizarConfiguracionRobot() {
        idleWaitingTime.value.let { idleWaitingTime ->
            //Obtener campos Robot
            val idleWaitingTime = preferencesRepository.getIdleWaitingTime()
            val meetingTimeThreshold = preferencesRepository.getMeetingTimeThreshold()
            val returnDestination = preferencesRepository.getReturnDestination()
            val coordinateDeviation = preferencesRepository.getCoordinateDeviation()
            val navigationTimeout = preferencesRepository.getNavigationTimeout()

            //Actualizar configuración Robot
            robotConfigInstance = robotConfigInstance.copy(
                idleWaitingTime = idleWaitingTime,
                meetingTimeThreshold = meetingTimeThreshold,
                returnDestination = returnDestination,
                coordinateDeviation = coordinateDeviation.toDouble(),
                navigationTimeout = navigationTimeout
            )
        }
    }

    fun actualizarConfiguracionAPI() {
        apiUser.value.let { apiUser ->
            // Obtener campos API
            val apiUser = preferencesRepository.getApiUsuario()
            val apiPassword = preferencesRepository.getApiPassword()

            //Actualizar configuración API
            Log.d("API", "Actualizando configuración: $apiUser")
            apiConfigInstance = apiConfigInstance.copy(APIUser = apiUser, APIPassword = apiPassword)

        }
    }

    //waitTime, meetingMeetingThreshold, returnDestination, coordinateDeviation, navigationTimeout
    fun guardarConfiguracionRobot(
        receivedWaitingIdleTime: Int,
        receivedMeetingTimeThreshold: Int,
        receivedReturnDestination: String,
        receivedCoordinateDeviation: Float,
        receivedRobotTimeout: Long,
    ) {
        Log.d(
            "MqttViewModel",
            "Guardando configuración Robot: $receivedWaitingIdleTime, $receivedMeetingTimeThreshold, $receivedReturnDestination, $receivedCoordinateDeviation, $receivedRobotTimeout"
        )

        preferencesRepository.setIdleWaitingTime(receivedWaitingIdleTime)
        preferencesRepository.setMeetingTimeThreshold(receivedMeetingTimeThreshold)
        preferencesRepository.setReturnDestination(receivedReturnDestination)
        preferencesRepository.setCoordinateDeviation(receivedCoordinateDeviation)
        preferencesRepository.setNavigationTimeout(receivedRobotTimeout)

        idleWaitingTime.value = receivedWaitingIdleTime
        meetingTimeThreshold.value = receivedMeetingTimeThreshold
        returnDestination.value = receivedReturnDestination
        coordinateDeviation.value = receivedCoordinateDeviation
        navigationTimeout.value = receivedRobotTimeout
    }

    fun guardarConfiguracionAPI(
        receivedApiUser: String,
        receivedApiPassword: String,
    ) {
        Log.d(
            "MqttViewModel",
            "Guardando configuración API: $receivedApiPassword, $receivedApiUser"
        )

        preferencesRepository.setApiUsuario(receivedApiUser)
        preferencesRepository.setApiPassword(receivedApiPassword)
        apiUser.value = receivedApiUser
        apiPassword.value = receivedApiPassword
    }

    fun guardarConfiguracionMqtt(
        ip: String,
        port: String,
        mqttUser: String,
        mqttPassword: String,
        mqttQoS: String,
        mqttClient: String,
    ) {
        Log.d(
            "MqttViewModel",
            "Guardando configuración: $ip, $port, $mqttUser, $mqttPassword, $mqttQoS, $mqttClient"
        )
        preferencesRepository.setBrokerIp(ip)
        preferencesRepository.setBrokerPort(port)
        preferencesRepository.setMqttUsuario(mqttUser)
        preferencesRepository.setMqttPassword(mqttPassword)
        preferencesRepository.setMqttQoS(mqttQoS)
        preferencesRepository.setMqttClient(mqttClient)

        // Guardar otros campos en SharedPreferences
        // No olvides actualizar los LiveData
        brokerIp.value = ip
        brokerPort.value = port
        brokerUser.value = mqttUser
        brokerPassword.value = mqttPassword
        brokerQoS.value = mqttQoS
        brokerClient.value = mqttClient

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
        returnDestination.removeObserver(returnDestinationObserver)
        coordinateDeviation.removeObserver(coordinateNavigationObserver)
        navigationTimeout.removeObserver(navigationTimeoutObserver)
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

    val isNavigationComplete = MutableLiveData<Boolean>(false)

    fun onNavigationCompleted() {
        isNavigationComplete.value = true
    }

    val isSpeakFinish = MutableLiveData<Boolean>(false)

    fun onSpeakFinished() {
        isSpeakFinish.value = true
    }

    val isNavigationStart = MutableLiveData<Boolean>(false)

    fun onNavigationStarted() {
        isNavigationStart.value = true
    }

    fun setDrivingState() {
        Log.d("DrivingState", "setDrivingState")
        robotMan.onNavigationStarted = { navigationStarted -> //navigationStarted: Boolean
            if (navigationStarted) {
                Log.d("DrivingState", navigationState.value.toString())
                isDriving.value = true
                isPaused.value = false
            }
        }
    }

    fun onResultUnknownVisitor(text: String) {
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
        } else if (containsVisitKeyword(speechResult)) {
            Log.d("speechResult", "Se ha detectado una visita")
            navigateToUnknownVisitsScreen()
        } else if (containsDealerKeyword(speechResult)) {
            Log.d("speechResult", "Se ha detectado un repartidor")
            navigateToPackageAndMailManagementScreen()
        } else if (containSiWord(speechResult)) {
            Log.d("speechResult", "Se ha detectado un si")
            //robotMan.stopFocusFollow()
            robotMan.speak("Deacuerdo, por aquí por favor", false, object : RobotManager.SpeakCompleteListener {
                override fun onSpeakComplete() {
                    // Acciones a realizar después de hablar
                }
            })
        }
        else {
            Log.d("speechResult", "No se ha detectado nada")
            // Lógica cuando no se detectan palabras clave
            repeatCommand()
        }
    }

    fun navigateToHomeScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.HomeScreen
    }

    fun navigateToMqttScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.MqttScreen
    }

    fun navigateToAdminPanelScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.AdminPanelScreen
    }

    fun navigateToDrivingScreen() {
        Log.d("MqttViewModel", "Navigating to DrivingScreen")
        detectionJob?.cancel()
        _navigationState.value = NavigationState.DrivingScreen
    }

    fun navigateToNumericPanelScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.NumericPanelScreen
    }

    fun navigateToUnknownVisitsScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.UnknownVisitsScreen
    }

    fun navigateToPackageAndMailManagementScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.PackageAndMailManagementScreen
    }

    fun navigateToEyesScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.EyesScreen
    }

    fun navigateToClockInScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.ClockInScreen
    }

    private fun repeatCommand() {
        robotMan.speak("Por favor repita el comando", true, object : RobotManager.SpeakCompleteListener {
            override fun onSpeakComplete() {
                // Acciones a realizar después de hablar
            }
        })
    }

    fun startPersonDetection(waitTimeInSeconds: Int) {
        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            val waitTimeInMillis = waitTimeInSeconds * 1000L
            var elapsedTime = 0L // Reinicia el temporizador
            val detectedPerson: List<Person>? = robotMan.detectPerson(0)

            while (detectedPerson.isNullOrEmpty() && elapsedTime < waitTimeInMillis) {

                delay(1000)
                elapsedTime += 1000
                Log.d(
                    "startDetection",
                    "CURRENT ELAPSED TIME: $elapsedTime is less than $waitTimeInMillis. current detection state: ${detectedPerson.isNullOrEmpty()}"
                )

            }

            if (!detectedPerson.isNullOrEmpty()) {
                Log.d("startDetection", "PERSONS LIST IS NOT NULL NEITHER EMPTY. RESTARTING TIME")
                elapsedTime = 0 // Reinicia el temporizador
                detectionJob?.cancel()
            }

            if (detectedPerson.isNullOrEmpty() && elapsedTime >= waitTimeInMillis && !navigationFinished.value) {
                Log.d(
                    "startDetection",
                    "ELAPSED TIME: $elapsedTime, detection state: ${detectedPerson.isNullOrEmpty()}, return to pos: ${returnDestination.value}"
                )
                returnToPosition(returnDestination.value!!)
                navigateToEyesScreen()
                detectionJob?.cancel()
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
            "paquete", "envío", "repartidor", "reparto",
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
        initiatedStatus.value = true
        //subscribeToAllTopics(resumeTopics())
    }

    fun disconnect() {
        addIncomingMessage("Disconnected")
        mqttManager.disconnect()
        initiatedStatus.value = false
    }

    fun getInitiatedStatus(): Boolean {
        return initiatedStatus.value
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
        Log.d("MQTT Message", "$topic: $message")

        when (topic) {
            "robot/nav_cmds/go_to" -> {

                Log.d(
                    "DISTANCE TO SILLON",
                    RobotApi.getInstance().getPlaceDistance("sillon").toString()
                )
                Log.d("UPDATE SAFE DISTANCE", "Update the safe distance")

                setDrivingState()
                robotMan.startNavigation(0, message, 0.1234, 0, navigationCompleteListener = object :
                    RobotManager.NavigationCompleteListener {
                    override fun onNavigationComplete() {
                        // Acciones a realizar después de hablar
                    }
                })
            }

            "robot/nav_cmds/go_charger" -> RobotApi.getInstance().goCharging(0)
            "robot/nav_cmds/stop_navigation" -> {
                robotMan.stopNavigation()
            }

            "robot/nav_cmds/pause_navigation" -> {
                robotMan.pauseNavigation()
            }

            "robot/nav_cmds/resume_navigation" -> {
                robotMan.resumeNavigation()
                isPaused.value = false
            }

            "robot/move_forward" -> {
                robotMan.moveForward()
            }

            "robot/wake_up" -> {
                robotMan.wakeUp()
            }

            "robot/nav_cmds/driving_finished" -> {
                Log.d("DRIVINGFINISHED", "Preparing home screen")
            }

            "zigbee2mqtt/Pulsador/action" -> {
                Log.d("ZIGBEE", message)
                if (message == "single") {
                    if (RobotApi.getInstance().chargeStatus) {
                        robotMan.scheduleWithCoroutine()
                    } else {
                        Log.d("ZIGBEE MQTT", "selected position: ${selectedItem.toString()}")
                        Log.d(
                            "robot params",
                            "${robotConfigInstance.returnDestination},${robotConfigInstance.coordinateDeviation},${robotConfigInstance.navigationTimeout}"
                        )
                        robotMan.startNavigation(
                            0,
                            robotConfigInstance.returnDestination,
                            robotConfigInstance.coordinateDeviation,
                            robotConfigInstance.navigationTimeout, navigationCompleteListener = object :
                                RobotManager.NavigationCompleteListener {
                                override fun onNavigationComplete() {
                                    // Acciones a realizar después de hablar
                                }
                        })
                    }
                }
            }
        }
    }

    fun getListPoses() {
        posesList.value = robotMan.getPoses()
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

    fun setPaused(isPausedTemp: Boolean) {
        Log.d("MQTTViewModel", "Setting isPaused: $isPausedTemp")
        isPaused.value = isPausedTemp
        robotMan.pauseNavigation()
    }

    fun setAdminState(newState: Boolean) {
        adminState.value = newState
        Log.d("ADMIN STATE", adminState.value.toString())
    }

    fun returnToPosition(positionToReturn: String) {
        //TODO: Save last known coordinates when starting a navigation
        if(positionToReturn != ""){
            robotMan.startNavigation(0,positionToReturn,coordinateDeviation.value!!.toDouble(),navigationTimeout.value!!.toLong(), navigationCompleteListener = object :
                RobotManager.NavigationCompleteListener {
                override fun onNavigationComplete() {
                    navigateToEyesScreen()
                }
            })
        }
        else{
            robotMan.speak("Actualmente no existe un destino al que haya ido previamente",false, object : RobotManager.SpeakCompleteListener {
                override fun onSpeakComplete() {
                    // Acciones a realizar después de hablar
                }
            })
        }
    }
}
