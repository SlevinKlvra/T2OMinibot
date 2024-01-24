package com.intec.t2o.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.viewmodels.NumericPanelViewModel
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