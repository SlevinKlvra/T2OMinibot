package com.intec.telemedicina.mqtt

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.ainirobot.coreservice.client.listener.ActionListener
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence

class MqttManager(private val context: Context, private val callback: MqttManagerCallback, private val mqttConfig: MQTTConfig, application: Application) : AndroidViewModel(application){
    private var mqttAndroidClient: MqttAndroidClient
    private val app = getApplication<Application>()
    private val persistence = MqttDefaultFilePersistence(app.filesDir.path)
    private var actionListener = ActionListener()

    init {
        mqttAndroidClient = MqttAndroidClient(context, mqttConfig.SERVER_URI, mqttConfig.client_id, persistence)
        mqttAndroidClient.setCallback(callback)
    }

    fun connect(
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Throwable?) -> Unit)? = null
    ){

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // We are connected
                    Log.d("MQTT CONNECTION", "onSuccess")
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    onSuccess?.invoke()
                    //addToHistory("Connected to $serverUri")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT CONNECTION", "onFailure")
                    onFailure?.invoke(exception)
                    //addToHistory("Failed to connect: $serverUri")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MQTT CONNECTION", "ERROR: ${e.message}")
        }
    }

    fun disconnect(){
        try {
            mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT DISCONNECTION", "onSuccess")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT DISCONNECTION", "onFailure")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MQTT DISCONNECTION", "ERROR: ${e.message}")
        }
    }

    fun getTopics(): MutableList<String>{
        return resumeTopics()
    }

    fun resumeTopics(): MutableList<String> {
        var listaTopics : MutableList<String> = mutableListOf()

        listaTopics.add(0,"robot/nav_cmds/go_charger")
        listaTopics.add(1,"robot/status/battery")
        listaTopics.add(2,"robot/nav_pub/current_pose")
        listaTopics.add(3,"robot/nav_pub/status")
        listaTopics.add(4,"robot/nav_pub/nav_error")
        listaTopics.add(5,"robot/simulation/nav_pub/nav_error")
        listaTopics.add(6,"robot/simulation/events/person_pushing")
        listaTopics.add(7,"robot/nav_pub/current_config")
        listaTopics.add(8,"robot/simulation/nav_pub/current_config")
        listaTopics.add(9,"robot/simulation/nav_pub/stored_locations")
        listaTopics.add(10,"robot/simulation/events/bumper")
        listaTopics.add(11,"robot/simulation/status/error")
        listaTopics.add(12,"robot/voice_cmds/text_to_speech")
        listaTopics.add(13,"robot/voice_recog/speech_to_text")
        listaTopics.add(14,"robot/voice_recog/response")
        listaTopics.add(15,"robot/voice_info/sound_record")
        listaTopics.add(16,"robot/human_cmd/cmd")
        listaTopics.add(17,"robot/simulation/ai_vision/new_person")
        listaTopics.add(18,"robot/nav_cmds/go_to")
        listaTopics.add(19,"robot/nav_cmds/stop_navigation")
        listaTopics.add(20,"robot/voice_cmds/question")
        listaTopics.add(21,"robot/welcome_cmd")
        return listaTopics
    }

    fun subscribeToAllTopics(topicList: MutableList<String>){
        while(topicList.isNotEmpty())
        {
            var currentTopic = topicList[0]
            //addToHistory("Subscribing to $currentTopic")
            subscribeToTopic(currentTopic)
            topicList.removeAt(0)
        }
    }

    fun subscribeToTopic(currentTopic: String) {
        Log.d("SUBSCRIPTION", "SUBSCRIBING TO TOPIC!")

        try {
            mqttAndroidClient.subscribe(currentTopic, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d("SUBSCRIPTION", "SUBSCRIPTION to $currentTopic SUCCESS!")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("SUBSCRIPTION", "SUBSCRIPTION to $currentTopic FAILURE!")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("SUBSCRIPTION", "$currentTopic ERROR: ${e.message}")
        }
    }

    fun publishMessage(topic: String, mensage: String, qos: Int = 1) {

        try {
            val message = MqttMessage()
            message.payload = mensage.toByteArray()
            Log.d("PUBLISH", (message.payload).toString())
            if (mqttAndroidClient.isConnected) {
                mqttAndroidClient.publish(topic, message)
                //addToHistory("Message Published >$publishedMessage<")
                if (!mqttAndroidClient.isConnected) {
                    //addToHistory(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
                }
            } else {
                //Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            }
        }catch (e: Exception) {
            e.printStackTrace()
            Log.d("PUBLISH", "ERROR: ${e.message}")
        }
    }


}