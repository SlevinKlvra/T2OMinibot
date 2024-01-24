package com.intec.t2o.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intec.t2o.preferences.PreferencesRepository
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.viewmodels.MqttViewModel
import javax.inject.Inject

class MqttViewModelFactory @Inject constructor(
    private val application : Application,
    private val robotMan : RobotManager,
    private val preferencesRepo: PreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MqttViewModel::class.java)) {

            return MqttViewModel(application, robotMan, preferencesRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}