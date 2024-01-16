package com.intec.telemedicina.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.intec.telemedicina.data.MeetingResponse
import com.intec.telemedicina.robotinterface.RobotManager
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.Duration
import java.time.LocalTime

class NumericPanelViewModel(
    application: Application,
    robotMan: RobotManager
) : AndroidViewModel(application) {

    var collectedMeetingInfo =
        mutableStateOf(MeetingResponse(0, "", "", "", "", "", "", "", "", ""))

    private val _navigationState =
        MutableStateFlow(MqttViewModel.NavigationState.NumericPanelScreen)
    val navigationState: StateFlow<MqttViewModel.NavigationState> = _navigationState.asStateFlow()

    var robotMan = robotMan

    // Estado para almacenar el código ingresado
    var enteredCode = mutableStateOf("")

    // Estado para controlar si se debe mostrar la animación de error
    var showErrorAnimation = mutableStateOf(false)

    // Nuevo estado para indicar si la carga está en progreso
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Función para activar la animación de error
    fun triggerErrorAnimation() {
        showErrorAnimation.value = true
        // Restablecer el estado después de un tiempo para permitir que la animación se ejecute
        viewModelScope.launch {
            delay(1000) // Duración de la animación
            showErrorAnimation.value = false
        }
    }

    // Función para añadir un dígito al código
    fun addDigit(digit: Char) {
        enteredCode.value += digit
    }

    // Función para borrar el último dígito
    fun removeLastDigit() {
        if (enteredCode.value.isNotEmpty()) {
            enteredCode.value = enteredCode.value.dropLast(1)
        }
    }

    fun resetDigits() {
        enteredCode.value = ""
    }

    // Función para verificar el código para configuración avanzada
    fun checkForAdvancedSettingsAccess(): Boolean {
        // Aquí iría la lógica para verificar el código
        if (enteredCode.value == "8998") {
            resetDigits()
            return true
        } else {
            return false
        }
    }

    private fun navigateToNumericPanelScreen() {
        robotMan.speak("Deacuerdo, introduce el código que se te ha proporcionado", false)
        _navigationState.value = MqttViewModel.NavigationState.NumericPanelScreen
    }

    // Estado para indicar si el código es correcto
    private val _isCodeCorrect = MutableStateFlow(false)
    val isCodeCorrect: StateFlow<Boolean> = _isCodeCorrect.asStateFlow()

    fun checkForTaskExecution() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                _isLoading.value = true

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://t2o.intecrobots.com/api/visitas/consultacodigototal?codigo=${enteredCode.value}")
                    .addHeader(
                        "Authorization",
                        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiMTM1NjZjOWQ4NmM2Nzc2YWI1ODYwMTI5NTAzYWIwOWNiNzZlMWNhNzlhZGVkOTg3NTM2MTU0NmE1MmUyNGU2MmIzM2YxOWIyMGI2MzI4YWQiLCJpYXQiOjE3MDUzMTI5NDEuNTM4MTQyOTE5NTQwNDA1MjczNDM3NSwibmJmIjoxNzA1MzEyOTQxLjUzODE0NTA2NTMwNzYxNzE4NzUsImV4cCI6MTczNjkzNTM0MS41MzI1OTk5MjU5OTQ4NzMwNDY4NzUsInN1YiI6IjEiLCJzY29wZXMiOltdfQ.P6Sah1BK-FyV3taMI8hK5VH5qZWdwBbh25qZOy7ejWI-tow0qSs9S0a5DR9rp1TQOCFR1sTT0Cvn-rlH_0jcfpQ8atvzmbGOXEmq-18LrVhNcKiHzM71vnOYdl-YPPd77wGqNpsR3hLJecUCvSitKF-dtmqnfsjHgJGERdEA0cQtEw_XNG6gUoJoT9_ZEbp1aALb9M6WX1OD6xGyqysH3em-qbEcLh66h7vEWuo3IohbQLl1XaPvUqb7g4EFCEYd300Hj24IRJ7F6rxjeLapjTpy8zFZRjL5L_BT2EpHFaHYY0XnOZ9FY6_YsLFNfG-s3HRr2yPtNwDs_7rJN6fmo0zycaJZyiW7lUPC06pFCMIUOoetwZ2Z3BcxVqsKoy0nkWq2TxE7TevToqsAh0_1XkYrOlbfk0V7HNbP9IbVEYfDY_gUOwZW0y_kbSR5rc_IrQX7Fp8GjDzmZLSOI2WqyG7vpQGAbFeJGk5D3zxwnj0aeD_QpvrVG3zjnSwp-cZjIGx_B_CYoAwNPlPicYjO2KXPU8hUPM-X_2DPbZDHYM56aimXSX6Io-q7Z8qzXw6Gqxpp3ruFzGezMIeSJ3GQ5Yn5z8QmbTp2Kj1X4gUPR9bXmh3mrKvg3dS0fRm7w4LMJSGEPc5F_aJm2nVm9yJwtpuAWLcaFN8m2fUxiSwda3k"
                    )
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d("CheckForApiExecution", response.toString())
                    if (response.isSuccessful) {
                        // Si la respuesta es exitosa, consideramos que el código es correcto.
                        val responseData = response.body?.string() ?: ""
                        val gson = Gson()
                        // Nuevo código para manejar la respuesta como un arreglo
                        val type = object : TypeToken<List<MeetingResponse>>() {}.type
                        val meetingInfoList =
                            gson.fromJson<List<MeetingResponse>>(responseData, type)

                        withContext(Dispatchers.Main) {
                            if (meetingInfoList.isNotEmpty()) {
                                val meetingInfo = meetingInfoList.first()
                                collectedMeetingInfo.value = meetingInfo
                                _isCodeCorrect.value = true
                                Log.d(
                                    "CheckForApiExecution",
                                    "El código es correcto: ${isCodeCorrect.value}"
                                )
                                Log.d("meetingInfo", "${collectedMeetingInfo.value}")
                            } else {
                                // Manejar el caso de que la lista esté vacía
                                triggerErrorAnimation()
                                robotMan.speak("El código no es válido. Inténtelo de nuevo", false)
                            }
                        }

                    } else {
                        // Si la respuesta no es exitosa, activamos la animación y hacemos que el robot hable.
                        withContext(Dispatchers.Main) {
                            triggerErrorAnimation()
                            robotMan.speak("El código no es válido. Inténtelo de nuevo", false)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    triggerErrorAnimation()
                    robotMan.speak(
                        "El código no es correcto. Inténtelo de nuevo o contacte con un miembro del staff",
                        false
                    )
                    Log.e("Error", "Error en la solicitud de red: ${e.message}")
                }
            } finally {
                // Indicar que la carga ha terminado (independientemente del resultado)
                _isLoading.value = false
            }
        }
    }

    fun getMeetingTimeThreshold(): Long {
        val sharedPrefs =
            getApplication<Application>().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getLong("meetingTimeThreshold", 10)
    }

    fun isMeetingTimeWithinThreshold(): Boolean {
        val meetingTime = LocalTime.parse(collectedMeetingInfo.value.start_time)
        val currentTime = LocalTime.now()
        Log.d("MeetingTime", "$meetingTime - $currentTime")
        val thresholdMinutes = getMeetingTimeThreshold()
        val threshold = Duration.ofMinutes(thresholdMinutes)

        Log.d(
            "MeetingTime",
            "${
                !currentTime.isBefore(meetingTime.minus(threshold)) && !currentTime.isAfter(
                    meetingTime.plus(threshold)
                )
            }"
        )

        return !currentTime.isBefore(meetingTime.minus(threshold)) && !currentTime.isAfter(
            meetingTime.plus(threshold)
        )
    }
}
