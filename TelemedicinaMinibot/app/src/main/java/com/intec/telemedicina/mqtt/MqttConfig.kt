package com.intec.telemedicina.mqtt

data class MQTTConfig (
    var SERVER_URI: String,
    var client_id: String,
    var qos: Int,
    var user: String,
    var password: String,
)