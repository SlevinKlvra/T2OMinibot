package com.intec.t2o.robotinterface

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.SkillCallback
import javax.inject.Inject

class SkillApiService @Inject constructor(
    private val context: Context
) {
    private var skillApi: SkillApi = SkillApi()

    // LiveData para observar los resultados parciales del reconocimiento de voz
    val partialSpeechResult = MutableLiveData<String>()

    private val apiListener = object : ApiListener {
        override fun handleApiDisabled() {
            // Manejar API deshabilitada
        }

        override fun handleApiConnected() {
            // Conexión exitosa, registrar callback
            skillApi.registerCallBack(mSkillCallback)
        }

        override fun handleApiDisconnected() {
            // Manejar desconexión
        }
    }

    private val mSkillCallback = object : SkillCallback() {

        override fun onSpeechParResult(text: String?) {
            text?.let {
                Log.d("SkillApi SpeechParResult", it)
                partialSpeechResult.postValue(it)
            }
        }

        override fun onStart() {
            //TO-DO("Not yet implemented")
        }

        override fun onStop() {
            //TO-DO("Not yet implemented")
        }

        override fun onVolumeChange(volume: Int) {
            //TO-DO("Not yet implemented")
        }

        override fun onQueryEnded(queryEndStatus: Int) {
            //TO-DO("Not yet implemented")
        }
    }

    init {
        connectApi()
    }

    private fun connectApi() {
        skillApi.connectApi(context, apiListener)
    }

    // Métodos adicionales según sea necesario...
}