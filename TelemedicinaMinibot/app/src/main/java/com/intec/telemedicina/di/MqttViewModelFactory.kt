package com.intec.telemedicina.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.robot.skillcallback.mSkillCallback
import com.intec.telemedicina.viewmodels.MqttViewModel
import dagger.hilt.android.internal.Contexts.getApplication
import javax.inject.Inject

class MqttViewModelFactory @Inject constructor(
    private val application : Application,
    private val robotApi: RobotApi,
    private val skillApi: SkillApi,
    private val actionListener: ActionListener
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MqttViewModel::class.java)) {
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
            return MqttViewModel(application, robotApi, skillApi, actionListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}