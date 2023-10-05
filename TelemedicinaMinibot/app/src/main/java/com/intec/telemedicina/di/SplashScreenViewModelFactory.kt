package com.intec.telemedicina.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import javax.inject.Inject

class SplashScreenViewModelFactory @Inject constructor(
    private val application : Application,
    private val robotApi: RobotApi,
    private val actionListener: ActionListener
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashScreenViewModel::class.java)) {
            return SplashScreenViewModel(application, robotApi, actionListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}