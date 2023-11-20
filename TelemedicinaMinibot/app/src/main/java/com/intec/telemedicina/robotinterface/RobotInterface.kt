package com.intec.telemedicina.robotinterface

import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener

interface RobotInterface {
    fun startNavigation(reqId: Int, destName: String, coordinateDeviation: Float, time: Long, navigationListener: ActionListener)
    fun stopNavigation(reqId: Int)
    fun goPosition(reqId: Int, position: String, goPositionListener: CommandListener)
    fun stopGoPosition(reqId: Int)
    fun getPosition(reqId: Int, commandListener: CommandListener)
    // Add other necessary methods here
}