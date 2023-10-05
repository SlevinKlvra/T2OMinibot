package com.intec.telemedicina.mqtt

data class MQTTConfig (
    val SERVER_URI : String, // Cambia a "ssl://" si usas SSL
    val client_id: String,
    val qos: Int, // 0, 1 o 2
    val user: String,
    val password: String
)