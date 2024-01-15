package com.intec.telemedicina.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.intec.telemedicina.data.MeetingResponse
import com.intec.telemedicina.robotinterface.RobotManager
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
    robotMan : RobotManager
) : ViewModel() {

    var collectedMeetingInfo = mutableStateOf(MeetingResponse(0,"","","","", "", "", ""))

    private val _navigationState = MutableStateFlow(MqttViewModel.NavigationState.NumericPanelScreen)
    val navigationState: StateFlow<MqttViewModel.NavigationState> = _navigationState.asStateFlow()

    var robotMan = robotMan

    // Estado para almacenar el código ingresado
    var enteredCode = mutableStateOf("")

    // Estado para controlar si se debe mostrar la animación de error
    var showErrorAnimation = mutableStateOf(false)

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
        if(enteredCode.value == "8998"){
            resetDigits()
            return true
        }else{
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
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://t2o.intecrobots.com/api/visitas/consultarcodigo?codigo=${enteredCode.value}")
                    .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiNDIyYWJmN2VkNDRiZTExNjJmMzVjMjRhZTlkMjY0YzAwODIyMmFhYjA3ZWNjMjExMTYxMmViYmQ0MGRkODcxNTgxNDllNGFiODJiY2ZjMjEiLCJpYXQiOjE3MDQ4MDY3MjAuNTc5MzA5OTQwMzM4MTM0NzY1NjI1LCJuYmYiOjE3MDQ4MDY3MjAuNTc5MzEyMDg2MTA1MzQ2Njc5Njg3NSwiZXhwIjoxNzM2NDI5MTIwLjU3MzA0OTA2ODQ1MDkyNzczNDM3NSwic3ViIjoiMSIsInNjb3BlcyI6W119.MZICikIr9fKmcMYBLetdkSEO_gL_IX3UmRKUtNW1ASiBfFYpW7_ZRMaXmKsqLqcU94KYF8oK3sgNwfd9njfHw8dxSEiDpyNqcgN0csQMYEPODHo-x0OCRPf0UNwta19UT5k6ZLlvn0p1CcSkUXVwUZAWI3lKlu5-seeoQ9btxbQAlaSTJL9kIGBXB_mg4TKWgtMjl2dq1neqZ1F97d-AwtNc1BYqi6HiQw1CbFAzDjMKdLTqJozLBtzvb3V8g53jR2hJNHbJGdI6DKSWvHKgBCxOQCHz9aDqEkqpVLNmfeLLH2-TkDn5cm4JabFcRuQtkwFozhKHgS4IHV8LrAv-yxc4Ve2BFpPjOm9LmFarxhZvV-8F1soIKkg5RNGooZvXzY2-t4Hx7mKhfWjlmucnNsSULe0mWyJfrs_EACZrMap8LZ4bt8AvU0NwPzlMEOwD7xkBxghuUV9eqh1brUk0X-ZJoGdm-vwPXnaslds2v8WiWNsyhJy9BqODDCnBscjYiHLxSs61oJyvR2en9idBNS3H9DOoN7Y7jEHiHlhvqB3_ARhbEra_wtOBYJo-VQ0lB0IOCebtyuP2SboJ_e0pB7vxfsKH0-o7huAVCL9x1X94t6cETeud9eKHqVmqBdkv5K5renmGN4c-jYscEPPNv8N5jAIRCWBEL_DAQEwG5yU")
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
                        val meetingInfoList = gson.fromJson<List<MeetingResponse>>(responseData, type)

                        withContext(Dispatchers.Main) {
                            if (meetingInfoList.isNotEmpty()) {
                                val meetingInfo = meetingInfoList.first()
                                collectedMeetingInfo.value = meetingInfo
                                _isCodeCorrect.value = true
                                Log.d("CheckForApiExecution", "El código es correcto: ${isCodeCorrect.value}")
                                Log.d("meetingInfo", "${collectedMeetingInfo.value}")
                            }
                            else {
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
                    robotMan.speak("El código no es correcto. Inténtelo de nuevo o contacte con un miembro del staff", false)
                    Log.e("Error", "Error en la solicitud de red: ${e.message}")
                }
            }
        }
    }

    fun isMeetingTimeWithinThreshold(): Boolean {
        val meetingTime = LocalTime.parse(collectedMeetingInfo.value.start_time)
        val currentTime = LocalTime.now()
        val threshold = Duration.ofMinutes(15)

        return !currentTime.isBefore(meetingTime.minus(threshold)) && !currentTime.isAfter(meetingTime.plus(threshold))
    }
}
