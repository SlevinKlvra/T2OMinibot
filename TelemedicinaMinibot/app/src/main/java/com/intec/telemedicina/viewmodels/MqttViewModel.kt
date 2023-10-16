package com.intec.telemedicina.viewmodels

import android.app.Application
import android.os.RemoteException
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.mqtt.MQTTConfig
import com.intec.telemedicina.mqtt.MqttManager
import com.intec.telemedicina.mqtt.MqttManagerCallback
import com.intec.telemedicina.mqtt.MqttMessageListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MqttViewModel @Inject constructor(
    application: Application,
    private val robotApi: RobotApi,
    private val skillApi: SkillApi,
    private var actionListener: ActionListener
) : AndroidViewModel(application), MqttMessageListener {

    private val _connectionState = mutableStateOf("Disconnected")
    val connectionState get()= _connectionState
    var navigation : Boolean = false
    val listeningTopics : String = "robot/nav_cmds/stop_navigation"
    private var _mqttQuestion = MutableLiveData<String>()
    val mqttQuestion : MutableLiveData<String> get() = _mqttQuestion

    val showQuestionsDialog = MutableStateFlow(false)

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
        mqttManager = MqttManager(getApplication(), mqttCallback, mqttConfigInstance ,application)

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
    }

    fun disconnect() {
        addIncomingMessage("Disconnected")
        mqttManager.disconnect()
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

        skillApi.playText(text, object : TextListener() {
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
            "robot/nav_pub/status" -> robotApi.currentPose
            "robot/nav_cmds/go_to" -> {
                Log.d("MQTTViewModel", "Starting navigation to: $message")
                robotApi.startNavigation(1, message.toString(), 0.01, 100000, navigationListener)
            }
            "robot/nav_cmds/go_to_coord" -> robotApi.startNavigation(1, message,0.01, 100000, navigationListener)
            "robot/nav_cmds/go_charger" -> robotApi.goCharging(1)
            "robot/nav_cmds/stop_navigation" -> {
                robotApi.stopNavigation(1)
            }

            //navigationListener.onStatusUpdate(Definition.ACTION_NAVI_STOP_MOVE,"YESSSSS")
            "robot/voice_cmds/text_to_speech" -> {
                Log.d("TextToSpeech", message)
                playTextViaTTS(message)
            }

            "robot/voice_cmds/question" -> {
                Log.d("Question",message)
                _mqttQuestion.value = message

                showQuestionsDialog()
                //Open window with question --> Yes/No
                //Send response back
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


    private var navigationListener: ActionListener = object : ActionListener() {
        @Throws(RemoteException::class)
        override fun onResult(status: Int, response: String) {
            when (status) {
                Definition.RESULT_OK -> if ("true" == response) {
                    //navigation is successful
                    publishMessage("robot/nav_pub/status",robotApi.currentPose.toString())

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

    fun showQuestionsDialog(){
        Log.d("MQTTViewModel", "Showing questions dialog")
        showQuestionsDialog.value = true
    }

    fun hideQuestionsDialog(){
        Log.d("MQTTViewModel", "Hiding questions dialog")
        showQuestionsDialog.value = false
    }
}
