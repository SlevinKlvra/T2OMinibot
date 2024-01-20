package com.intec.telemedicina.data

data class RobotConfig(val idleWaitingTime: Int, val meetingTimeThreshold: Int, val returnDestination: String, val coordinateDeviation: Double, val navigationTimeout: Long)
