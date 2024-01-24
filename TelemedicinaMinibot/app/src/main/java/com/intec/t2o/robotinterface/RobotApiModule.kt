package com.intec.t2o.robotinterface

import android.content.Context
import android.util.Log
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.intec.t2o.robot.modulecallback.ModuleCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RobotApiModule{
    @Singleton
    val robotApi: RobotApi = RobotApi.getInstance()
    var connected = false

    @Singleton
    fun connectServer(context: Context) {
        if(!connected){
            robotApi.connectServer(context, object : ApiListener {
                override fun handleApiDisabled() {
                    // Handle disconnection or other relevant action
                }

                override fun handleApiConnected() {
                    Log.d("SERVER CONNECTION", "Conectado al servidor")
                    robotApi.setCallback(ModuleCallback())
                }

                override fun handleApiDisconnected() {
                    TODO("Not yet implemented")
                }
            })
            connected = true
        }

    }
    @Provides
    @Singleton
    fun provideRobotApi(): RobotApi {
        return robotApi
    }
    
}