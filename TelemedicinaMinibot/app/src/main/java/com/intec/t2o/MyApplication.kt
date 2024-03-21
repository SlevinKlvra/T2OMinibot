package com.intec.t2o

import android.app.Application
import com.intec.t2o.robotinterface.RobotConnectionService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {

    @Inject
    lateinit var robotConnectionService: RobotConnectionService

    override fun onCreate() {
        super.onCreate()
        //Inicialización específica de la aplicación
        robotConnectionService.connectToRobotApi()
        robotConnectionService.connectToSkillApi()
    }
}