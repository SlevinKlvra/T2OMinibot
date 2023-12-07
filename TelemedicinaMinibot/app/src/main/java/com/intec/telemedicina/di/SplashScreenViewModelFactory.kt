package com.intec.telemedicina.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.robot.skillcallback.mSkillCallback
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import javax.inject.Inject

class SplashScreenViewModelFactory @Inject constructor(
    private val application : Application,
    private val skillApi: SkillApi,
    private val robotManager: RobotManager,
    private val actionListener: ActionListener
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashScreenViewModel::class.java)) {
            skillApi.connectApi(application, object : ApiListener {
                    override fun handleApiDisabled() {
                        TODO("Not yet implemented")
                    }
    
                    override fun handleApiConnected() {
                        skillApi.registerCallBack(mSkillCallback)
                    }
    
                    override fun handleApiDisconnected() {
                        TODO("Not yet implemented")
                    }
                    // Implementa otros métodos según sea necesario.
                })
            return SplashScreenViewModel(application, skillApi, robotManager, actionListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}