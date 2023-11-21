package com.intec.telemedicina.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import javax.inject.Inject

class MqttViewModelFactory @Inject constructor(
    private val application : Application,
    private val robotMan : RobotManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MqttViewModel::class.java)) {

            return MqttViewModel(application, robotMan) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}