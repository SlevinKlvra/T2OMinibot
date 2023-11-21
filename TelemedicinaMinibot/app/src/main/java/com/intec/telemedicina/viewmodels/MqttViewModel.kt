package com.intec.telemedicina.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.intec.telemedicina.mqtt.MQTTConfig
import com.intec.telemedicina.mqtt.MqttManager
import com.intec.telemedicina.mqtt.MqttManagerCallback
import com.intec.telemedicina.mqtt.MqttMessageListener
import com.intec.telemedicina.robotinterface.RobotManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MqttViewModel @Inject constructor(
    application: Application,
    robotMan : RobotManager
) : AndroidViewModel(application), MqttMessageListener {

    var robotMan = robotMan

    //TODO:revove skillapi
    val skillApi = SkillApi()

    private val _connectionState = mutableStateOf("Disconnected")
    val connectionState get()= _connectionState
    var navigation : Boolean = false
    val listeningTopics : String = "robot/nav_cmds/stop_navigation"
    private var _mqttQuestion = MutableLiveData<String>()
    val mqttQuestion : MutableLiveData<String> get() = _mqttQuestion

    val showQuestionsDialog = MutableStateFlow(false)
    val showWelcomeDialog = MutableStateFlow(false)

    var initiated_status = false

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
        password = "metricsmetricsmetricsmetrics"
    )

    init {
        mqttManager = MqttManager(getApplication(), mqttCallback, mqttConfigInstance, application)

        Log.d("MQTTManager", "MqttManager created: $mqttManager")
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
        addIncomingMessage("Connecting to broker: ${mqttConfigInstance.SERVER_URI}")
        mqttManager.connect()
        initiated_status = true
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
        mqttManager.subscribeToAllTopics(topics)
    }

    fun publishMessage(topic: String, message: String) {
        Log.d("MQTTViewModel", "Publishing message: $message to topic: $topic")
        addIncomingMessage("Publishing message: $message to topic: $topic")
        mqttManager.publishMessage(topic, message)
    }

    fun playTextViaTTS(text: String) {

        /*skillApi.playText(TTSEntity(text), object : TextListener() {
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
        })*/

        SkillApi().playText(TTSEntity("sid-012345",text), object : TextListener() {
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

    override fun onMessageReceived(topic: String, message: String) {
        addIncomingMessage(message)

        when(topic){
            //"robot/nav_pub/status" -> robotApi.currentPose
            "robot/nav_cmds/go_to" -> {
                Log.d("MQTTViewModel", "Starting navigation to: $message")
                //robotApi.startNavigation(1, message.toString(), 0.01, 100000, navigationListener)
                //RobotManager(getApplication<Application>().applicationContext).getRobotInterfaceMethod().startNavigation(1, message.toString(), 0.01, 100000)

                Log.d("Poses",robotMan.getPoses().toString())
                //robotMan.printPoses()
                //robotMan.startNavigation(0,"sillon",0.1234,0)

                //RobotManager(getApplication<Application>().applicationContext).getRobotInterfaceMethod().goPosition()
            }
            /*"robot/nav_cmds/go_to_coord" -> robotApi.startNavigation(1, message,0.01, 100000, navigationListener)
            "robot/nav_cmds/go_charger" -> robotApi.goCharging(1)*/
            "robot/nav_cmds/stop_navigation" -> {
                //robotApi.stopNavigation(1)
                robotMan.stopNavigation(0)
            }

            //navigationListener.onStatusUpdate(Definition.ACTION_NAVI_STOP_MOVE,"YESSSSS")
            "robot/voice_cmds/text_to_speech" -> {
                Log.d("TextToSpeech", message)
                //playTextViaTTS(message)
                robotMan.speak(message)
            }

            "robot/voice_cmds/question" -> {
                Log.d("Question",message)
                _mqttQuestion.value = message

                showQuestionsDialog()
                //Open window with question --> Yes/No
                //Send response back
            }
            "robot/welcome_cmd" -> { //return answer on --> "robot/welcome_pub"
                //robotApi.startNavigation(1, "Punto de recepción", 0.01, 100000, navigationListener)
                showWelcomeDialog()
            }
        }
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
}
