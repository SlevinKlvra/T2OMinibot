package com.intec.telemedicina.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
import javax.inject.Inject

class NumericPanelViewModelFactory @Inject constructor(
    private val application : Application,
    private val robotMan : RobotManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NumericPanelViewModel::class.java)) {

            return NumericPanelViewModel(application, robotMan) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}