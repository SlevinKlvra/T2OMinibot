package com.upct.addim.utils.robot

import android.os.RemoteException

import com.ainirobot.coreservice.client.module.ModuleCallbackApi


class ModuleCallback : ModuleCallbackApi() {
    @Throws(RemoteException::class)
    override fun onSendRequest(
        reqId: Int,
        reqType: String,
        reqText: String,
        reqParam: String
    ): Boolean {
        //receive voice command,        
        //reqTyp : voice command type        
        //reqText : voice to text        
        //reqParam : voice command parameter        
        return true
    }

    @Throws(RemoteException::class)
    override fun onRecovery() {
        //When receiving the event, regain control of the robot    
    }

    @Throws(RemoteException::class)
    override fun onSuspend() {
        //Control is deprived by the system. When receiving this event, all Api calls are invalid    
    }
}