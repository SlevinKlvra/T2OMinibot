package com.intec.telemedicina.preferences

import android.content.SharedPreferences
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences) {

    // Ya existentes
    fun getBrokerIp(): String = sharedPreferences.getString("broker_ip", "") ?: ""
    fun setBrokerIp(ip: String) = sharedPreferences.edit().putString("broker_ip", ip).apply()

    // Agregar nuevos
    fun getBrokerPort(): String = sharedPreferences.getString("puerto", "") ?: ""
    fun setBrokerPort(puerto: String) = sharedPreferences.edit().putString("puerto", puerto).apply()

    fun getMqttUsuario(): String = sharedPreferences.getString("mqtt_usuario", "") ?: ""
    fun setMqttUsuario(usuarioMqtt: String) = sharedPreferences.edit().putString("mqtt_usuario", usuarioMqtt).apply()

    fun getMqttPassword(): String = sharedPreferences.getString("mqtt_password", "") ?: ""
    fun setMqttPassword(passwordMqtt: String) = sharedPreferences.edit().putString("mqtt_password", passwordMqtt).apply()

    fun getMqttQoS(): String = sharedPreferences.getString("mqtt_qos", "") ?: ""
    fun setMqttQoS(qosMqtt: String) = sharedPreferences.edit().putString("mqtt_qos", qosMqtt).apply()

    fun getMqttClient(): String = sharedPreferences.getString("mqtt_client", "") ?: ""
    fun setMqttClient(clientMqtt: String) = sharedPreferences.edit().putString("mqtt_client", clientMqtt).apply()

    fun getIdleWaitingTime(): Int = sharedPreferences.getInt("idle_waiting_time", 10)
    fun setIdleWaitingTime(idleWaitingTime: Int) = sharedPreferences.edit().putInt("idle_waiting_time", idleWaitingTime).apply()

    fun getMeetingTimeThreshold(): Int = sharedPreferences.getInt("waiting_meeting_time", 10)
    fun setMeetingTimeThreshold(waitingMeetingTimeThreshold: Int) = sharedPreferences.edit().putInt("waiting_meeting_time", waitingMeetingTimeThreshold).apply()

    fun getApiUsuario(): String = sharedPreferences.getString("api_usuario", "") ?: ""
    fun setApiUsuario(usuario: String) = sharedPreferences.edit().putString("api_usuario", usuario).apply()

    fun getApiPassword(): String = sharedPreferences.getString("api_password", "") ?: ""
    fun setApiPassword(passwordApi: String) = sharedPreferences.edit().putString("api_password", passwordApi).apply()

}