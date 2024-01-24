package com.intec.t2o.robotinterface

import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener

interface RobotInterface {
    fun startNavigation(reqId: Int, destName: String, coordinateDeviation: Double, time: Long)
    fun stopNavigation(reqId: Int)
    fun goPosition(reqId: Int, position: String, goPositionListener: CommandListener)
    fun stopGoPosition(reqId: Int)
    fun getPosition(reqId: Int, commandListener: CommandListener)
    fun getNavigationListener(): ActionListener
    fun getPlaceList()
    fun getPoses() : MutableList<Pose>
    // Add other necessary methods here
}