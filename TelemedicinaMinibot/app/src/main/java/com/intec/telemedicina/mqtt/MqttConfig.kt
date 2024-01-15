package com.intec.telemedicina.mqtt

data class MQTTConfig (
    var SERVER_URI: String,
    var client_id: String,
    var qos: Int,
    var user: String,
    var password: String,
    // Añadir nuevos campos
    var apiUser: String,
    var apiPassword: String,
    var waitingIdleTime: Int,
    var meetingTimeThreshold: Int
)