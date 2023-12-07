package com.intec.telemedicina.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.Person
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.intec.telemedicina.data.Face
import com.intec.telemedicina.data.InteractionState
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

    val openHomescreen = MutableStateFlow(false)

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
                robotMan.question_si_no(message, true)
                question.value = message
            }

            "robot/voice_cmds/question" -> {
                //Log.d("Question",message)
                //_mqttQuestion.value = message
                robotMan.question_si_no(message,false)
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
                openHomescreen.value = true
            }
            "robot/notUnderstood" -> {
                notUnderstood.value = true
            }
            "robot/nav_cmds/request_move" -> {
                robotMan.question_move()
            }
        }
    }

    fun closeHomescreen(){
        openHomescreen.value = false
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
}
