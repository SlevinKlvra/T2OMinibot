package com.intec.t2o.viewmodels

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
import com.intec.t2o.data.APIConfig
import com.intec.t2o.data.Face
import com.intec.t2o.data.InteractionState
import com.intec.t2o.data.RobotConfig
import com.intec.t2o.mqtt.MQTTConfig
import com.intec.t2o.mqtt.MqttManager
import com.intec.t2o.mqtt.MqttManagerCallback
import com.intec.t2o.mqtt.MqttMessageListener
import com.intec.t2o.preferences.PreferencesRepository
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.robotinterface.SkillApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MqttViewModel @Inject constructor(
    application: Application,
    private val robotMan: RobotManager,
    private val skillApiService: SkillApiService,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application), MqttMessageListener {

    //COUNTDOWN DEL ESTADO DE PAUSA
    //TO DO: AÑADIR A SETPREFERENCES LA VARIABLE PARA PODER EDITARLA
    private val _countdownState = MutableStateFlow(10)
    val countdownState: StateFlow<Int> = _countdownState

    val countdownFlag = MutableStateFlow(false)
    val isNavigating = mutableStateOf(false)

    // Estados del estado de la secuencia meetingScreen
    val messageIndexState = mutableStateOf(0)
    val currentPageState = mutableStateOf(0)

    val isFollowing = robotMan.isFollowing

    // Métodos para actualizar estos estados
    fun setMessageIndex(index: Int) {
        messageIndexState.value = index
    }

    fun setCurrentPage(page: Int) {
        currentPageState.value = page
    }

    //Método para saber si está regresando
    val isReturningHome = mutableStateOf(false)

    fun setReturningHome(returning: Boolean) {
        isReturningHome.value = returning
    }

    //VARIABLES PARA LA ESCUCHA ACTIVA DE UNKNOWN VISITS
    private val _capturedText = MutableStateFlow("")
    val capturedText: StateFlow<String> = _capturedText

    private val _isListening = MutableLiveData<Boolean>()
    val isListening: LiveData<Boolean> = _isListening

    fun setCountdownFlagState(newState: Boolean) {
        countdownFlag.value = newState
        Log.d("ADMIN STATE", adminState.value.toString())
    }

    var coutndownJob: Job? = null
    fun startCountdown() {
        coutndownJob = viewModelScope.launch {
            var currentCount = 10
            while (currentCount >= 0) {
                _countdownState.value = currentCount
                delay(1000)
                currentCount--
            }
            coutndownJob?.cancel()
            navigateToEyesScreen()
            reanudarNavegacion()
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

    private var initiatedStatus = mutableStateOf(false)

    private val mqttCallback = MqttManagerCallback(_connectionState, {
        val updatedMessages = _incomingMessages.value.toMutableList()
        updatedMessages.add(it)
        _incomingMessages.value = updatedMessages
    }, this)

    // LiveData para cada configuración
    private val brokerIp: MutableLiveData<String> = MutableLiveData()
    private val brokerPort: MutableLiveData<String> = MutableLiveData()
    private val brokerUser: MutableLiveData<String> = MutableLiveData()
    private val brokerPassword: MutableLiveData<String> = MutableLiveData()
    private val brokerQoS: MutableLiveData<String> = MutableLiveData()
    private val brokerClient: MutableLiveData<String> = MutableLiveData()
    private val idleWaitingTime: MutableLiveData<Int> = MutableLiveData()
    private val meetingTimeThreshold: MutableLiveData<Int> = MutableLiveData()
    private val apiUser: MutableLiveData<String> = MutableLiveData()
    private val apiPassword: MutableLiveData<String> = MutableLiveData()
    var returnDestination: MutableLiveData<String> = MutableLiveData()
    val coordinateDeviation: MutableLiveData<Float> = MutableLiveData()
    val navigationTimeout: MutableLiveData<Long> = MutableLiveData()

    private val mqttManager: MqttManager
    private var mqttConfigInstance = MQTTConfig(
        SERVER_URI = "",
        client_id = "",
        qos = 0,
        user = "",
        password = ""
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
    private val navigationFinished: StateFlow<Boolean> = _navigationFinished.asStateFlow()

    // Variable de control
    private var hasHandledPersonDetection = false

    enum class NavigationState {
        EyesScreen, HomeScreen, NumericPanelScreen, MeetingScreen, UnknownVisitsScreen, PackageAndMailManagementScreen, DrivingScreen, MqttScreen, AdminPanelScreen, ClockInScreen
    }

    var currentNavigationContext = MutableLiveData<NavigationState>()

    // Observador para cambios en brokerIp
    private val brokerIpObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerPort
    private val brokerPortObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerUser
    private val brokerUserMqttObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerPassword
    private val brokerPasswordMqttObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerQoS
    private val brokerQoSMqttObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    // Observador para cambios en brokerClient
    private val brokerClientMqttObserver = Observer<String> { _ ->
        actualizarConfiguracionMQTT()
    }

    //CAMBIOS API
    // Observador para cambios en API User
    private val apiUserObserver = Observer<String> { _ ->
        actualizarConfiguracionAPI()
    }

    // Observador para cambios en API Password
    private val apiPasswordObserver = Observer<String> { _ ->
        actualizarConfiguracionAPI()
    }

    //CAMBIOS ROBOT
    // Observador para cambios en waiting Idle Time
    private val waitingIdleTimeObserver = Observer<Int> { _ ->
        actualizarConfiguracionRobot()
    }

    // Observador para cambios en Meeting time
    private val meetingTimeThresholdObserver = Observer<Int> { _ ->
        actualizarConfiguracionRobot()
    }

    // Observador para cambios en API Password
    private val returnDestinationObserver = Observer<String> { _ ->
        actualizarConfiguracionRobot()
    }

    private val coordinateNavigationObserver = Observer<Float> { _ ->
        actualizarConfiguracionRobot()
    }

    private val navigationTimeoutObserver = Observer<Long> { _ ->
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

    private val _speechText = MutableStateFlow("")
    val speechText = _speechText.asStateFlow()

    init {
        mqttManager = MqttManager(getApplication(), mqttCallback, mqttConfigInstance, application)

        getListPoses()
        resetAndRestartDetection()

        // Inicializar los valores de las configuraciones
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

        configurePersonDetection()

        skillApiService.partialSpeechResult.observeForever{speechResult ->
            viewModelScope.launch {
                Log.d("mqttViewModel", "speechResult: $speechResult")
                _speechText.value = speechResult
            }
        }
    }

    fun clearRecognizedText() {
        _speechText.value = ""
    }

    private fun configurePersonDetection() {
        robotMan.onPersonDetected = { personList ->
            if (personList != null && !hasHandledPersonDetection) {
                handlePersonDetection(personList)
            } else {
                startPersonDetection(robotConfigInstance.idleWaitingTime)
            }
        }
    }

    private fun handlePersonDetection(personList: List<Any>) { // Asumiendo un tipo genérico Any por ahora
        if (personList.isNotEmpty() && !hasHandledPersonDetection) {
            hasHandledPersonDetection = true
            detectionJob?.cancel()
            navigateToHomeScreen()
            robotMan.questionPrueba()
            listenToSpeechResult()
            // Reinicia la detección después de un delay para dar tiempo a completar las acciones
            /*viewModelScope.launch {
                delay(5000) // Espera 5 segundos antes de reiniciar la detección
                resetAndRestartDetection()
            }*/
        } else {
            startPersonDetection(robotConfigInstance.idleWaitingTime)
        }
    }

    private fun resetAndRestartDetection() {
        hasHandledPersonDetection = false
        startPersonDetection(robotConfigInstance.idleWaitingTime)
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

    private fun actualizarConfiguracionRobot() {
        idleWaitingTime.value.let {
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

    private fun actualizarConfiguracionAPI() {
        apiUser.value.let {
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

    private fun listenToSpeechResult() {
        robotMan.onSpeechResultReceived = { speechResult ->
            if (speechResult.isNotEmpty()) {
                Log.d("listenToSpeechResult", "speechResult: $speechResult")
                onResultUnknownVisitor(speechResult)
                Log.d("listenToSpeechResult", "actualiza capturedText $_capturedText")
                processSpeechResult(speechResult)
            }
        }
    }

    private val isNavigationComplete = MutableLiveData(false)

    fun onNavigationCompleted() {
        isNavigationComplete.value = true
    }

    private val isSpeakFinish = MutableLiveData(false)

    fun onSpeakFinished() {
        isSpeakFinish.value = true
    }

    private val isNavigationStart = MutableLiveData(false)

    fun onNavigationStarted() {
        isNavigationStart.value = true
    }

    private fun setDrivingState() {
        Log.d("DrivingState", "setDrivingState")
        robotMan.onNavigationStarted = { navigationStarted -> //navigationStarted: Boolean
            if (navigationStarted) {
                Log.d("DrivingState", navigationState.value.toString())
                isDriving.value = true
                isPaused.value = false
            }
        }
    }

    private fun onResultUnknownVisitor(text: String) {
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
            robotMan.speak(
                "Deacuerdo, por aquí por favor",
                false
            ){
                Log.d("mqttViewModel processSpeechResult", "Deacuerdo, por aquí por favor")
            }
        } else {
            Log.d("speechResult", "No se ha detectado nada")
            // Lógica cuando no se detectan palabras clave
            //repeatCommand()
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

    fun navigateToMeetingScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.MeetingScreen
    }

    fun navigateToEyesScreen() {
        Log.d("EYES SCREEN", navigationState.toString())
        detectionJob?.cancel()
        _navigationState.value = NavigationState.EyesScreen
    }

    fun navigateToClockInScreen() {
        detectionJob?.cancel()
        _navigationState.value = NavigationState.ClockInScreen
    }

    private fun repeatCommand() {
        speak(
            "Por favor repita el comando",
            true){
                Log.d("repeatCommand", "Por favor repita el comando")
            }
    }

    private fun startPersonDetection(waitTimeInSeconds: Int) {
        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            val waitTimeInMillis = waitTimeInSeconds * 1000L
            var elapsedTime = 0L // Reinicia el temporizador
            val detectedPerson: List<Person>? = robotMan.detectPerson(0)

            while (detectedPerson.isNullOrEmpty() && elapsedTime < waitTimeInMillis) {

                delay(1000)
                elapsedTime += 1000
                /*Log.d(
                    "startDetection",
                    "CURRENT ELAPSED TIME: $elapsedTime is less than $waitTimeInMillis. current detection state: ${detectedPerson.isNullOrEmpty()}"
                )*/

            }

            if (!detectedPerson.isNullOrEmpty()) {
                //Log.d("startDetection", "PERSONS LIST IS NOT NULL NEITHER EMPTY. RESTARTING TIME")
                elapsedTime = 0 // Reinicia el temporizador
                detectionJob?.cancel()
            }

            if (detectedPerson.isNullOrEmpty() && elapsedTime >= waitTimeInMillis && !navigationFinished.value) {
                /*Log.d(
                    "startDetection",
                    "ELAPSED TIME: $elapsedTime, detection state: ${detectedPerson.isNullOrEmpty()}, return to pos: ${returnDestination.value}"
                )*/
                returnToPosition(returnDestination.value!!)
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
        val currentMessages = _incomingMessages.value
        _incomingMessages.value = currentMessages + message
    }

    fun connect() {
        /*Log.d("MQTTViewModel", "Connecting to broker: ${mqttConfigInstance.SERVER_URI}")
        Log.d("MQTTViewModel", "Client id: ${mqttConfigInstance.client_id}")
        Log.d("MQTTViewModel", "User: ${mqttConfigInstance.user}")
        Log.d("MQTTViewModel", "Pwd: ${mqttConfigInstance.password}")*/
        addIncomingMessage("Connecting to broker: ${mqttConfigInstance.SERVER_URI}")
        addIncomingMessage("User: ${mqttConfigInstance.user}")
        addIncomingMessage("Password: ${mqttConfigInstance.password}")
        addIncomingMessage("Client id: ${mqttConfigInstance.client_id}")
        addIncomingMessage("QoS: ${mqttConfigInstance.qos}")
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
        Log.d("MQTTViewModel", "Subscribing to all topics, $topics")
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
                startNavigation(
                    message
                ){
                    Log.d("MQTTViewModel message", "OnNavigationComplete")
                    speak("Llegamos a nuestro destinocvia mqtt", false){
                        Log.d("MQTTViewModel message", "OnSpeakComplete")
                    }
                }
            }

            "robot/nav_cmds/go_charger" -> RobotApi.getInstance().goCharging(0)
            "robot/nav_cmds/stop_navigation" -> {
                detenerNavegacion()
            }

            "robot/nav_cmds/pause_navigation" -> {
                robotMan.pauseNavigation()
            }

            "robot/nav_cmds/resume_navigation" -> {
                reanudarNavegacion()
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
                startWelcomeProcess()
                triggerGreetVisitorEvent()
                startNavigation(
                    "entrada"
                ) {
                    // Acciones a realizar después de hablar
                    publishMessage("zigbee2mqtt/Cerradura/left/set", "ON")
                    publishMessage("zigbee2mqtt/Cerradura/right/set", "ON")
                    addIncomingMessage("Opening door")
                    Log.d("EVENT TRIGGER", "Finish welcome process")
                    //finishWelcomeProcess()
                }
            }
            "/robot/tts/speak" -> {
                speak(message, false){
                    Log.d("MQTTViewModel message", "OnSpeakComplete")
                }
            }
            "/api/robot/control" -> {

                when(message) {
                    "adelante" -> {
                        robotMan.moveForward()
                    }
                    "derecha" -> {
                        robotMan.turnRight()
                    }
                    "izquierda" -> {
                        robotMan.turnLeft()
                    }
                    "detener" -> {
                        Log.d("MQTTViewModel", "Deteniendo robot")
                        robotMan.stopMove()
                    }
                    "arriba" -> {
                        Log.d("MQTTViewModel", "Mover cabeza arriba")
                        robotMan.moveHeadUp()
                    }
                    "abajo" -> {
                        Log.d("MQTTViewModel", "Mover cabeza abajo")
                        robotMan.moveHeadDown()
                    }
                    else -> {
                        Log.d("MQTTViewModel", "ELSE: Deteniendo robot")
                        robotMan.stopMove()
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

    fun speak(text: String, listen: Boolean, onSpeakComplete: () -> Unit) {
        clearRecognizedText()
        robotMan.speak(text, listen, onSpeakComplete)
    }

    fun startNavigation(goalPosition: String, onNavigationComplete: () -> Unit) {
        // Ejemplo de valores hardcoded para coordinateDeviation y navigationTimeout.
        // Reemplázalos con los valores apropiados según tu caso de uso.
        robotMan.startNavigation(goalPosition, 0.5, 3000L, onNavigationComplete)
    }

    /*fun speak(text: String, listen: Boolean, onComplete: RobotManager.SpeakCompleteListener){
        Log.d("MqttViewModel", "speak: $text, listen: $listen")
        /*robotMan.speak(text, listen, object : RobotManager.SpeakCompleteListener {
            override fun onSpeakComplete() {
                Log.d("OnSpeak", "OnSpeak Completed")
            }
        })*/
        robotMan.speak(text, listen, onComplete)
    }*/

    fun detenerNavegacion(){
        robotMan.stopNavigation()
    }

    fun iniciarFocus(){
        robotMan.startFocusFollow(0)
    }

    fun detenerFocus(){
        robotMan.stopFocusFollow()
    }

    fun registrarPersonListener(){
        robotMan.registerPersonListener()
    }

    fun desregistrarPersonListener(){
        robotMan.unregisterPersonListener()
    }

    fun reanudarNavegacion(){
        robotMan.resumeNavigation{
            isNavigating.value = false
            when (currentNavigationContext.value) {
                NavigationState.PackageAndMailManagementScreen -> {
                    if (messageIndexState.value == 3) {
                        Log.d("if message index", "entramos en condicion")
                        setMessageIndex(7)
                    } else {
                        Log.d("if message no index", "no entramos en condicion")
                        navigateToEyesScreen()
                    }
                }
                NavigationState.MeetingScreen -> {
                    if (messageIndexState.value == 7) setMessageIndex(4)
                    else navigateToEyesScreen()
                }
                NavigationState.HomeScreen -> {
                    isNavigating.value = false
                    navigateToEyesScreen()
                }
                NavigationState.DrivingScreen -> {
                    isNavigating.value = false
                    navigateToEyesScreen()
                }
                else -> Unit // Manejar cualquier caso no esperado
            }
        }
    }

    fun returnToPosition(positionToReturn: String) {
        navigateToEyesScreen()
        if (positionToReturn != "") {
            startNavigation(
                positionToReturn
            ) {
                // Este es el callback de navegación completa.
                if (isReturningHome.value) {
                    navigateToEyesScreen()
                    isNavigating.value = false
                    setMessageIndex(0)
                    setCurrentPage(0)
                    setReturningHome(false)
                    resetAndRestartDetection()
                }
            }
        } else {
            speak("Actualmente no existe un destino al que haya ido previamente", false) {
                // Este callback se ejecutará después de hablar, si necesitas hacer algo aquí.
                Log.d("MqttViewModel Speak", "Finished speaking")
            }
        }
    }

    fun irACargar(){
        robotMan.goCharge()
    }

    //FUNCIONES PARA INICIAR LA INTERACCIÓN DE SALUDO
    private val _greetVisitorEventCount = MutableStateFlow(0) // Contador de eventos
    val greetVisitorEventCount = _greetVisitorEventCount.asStateFlow()

    private val _isInWelcomeProcess = MutableStateFlow(false)
    val isInWelcomeProcess = _isInWelcomeProcess.asStateFlow()

    fun startWelcomeProcess() {
        Log.d("EVENT TRIGGER", "Start welcome process")
        _isInWelcomeProcess.value = true
    }

    fun finishWelcomeProcess() {
        Log.d("EVENT TRIGGER", "Finish welcome process")
        _isInWelcomeProcess.value = false
        resetGreetVisitorEventCount()
    }

    fun triggerGreetVisitorEvent() {
        viewModelScope.launch {
            Log.d("EVENT TRIGGER", "Emitiendo evento de saludo")
            _greetVisitorEventCount.value = _greetVisitorEventCount.value + 1 // Incrementar el contador
        }
    }

    private fun resetGreetVisitorEventCount() {
        _greetVisitorEventCount.value = 0
    }
}