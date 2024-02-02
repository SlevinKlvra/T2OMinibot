package com.intec.t2o.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intec.t2o.data.MeetingResponse
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.screens.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import java.time.LocalTime


class NumericPanelViewModel(
    application: Application, var robotMan: RobotManager
) : AndroidViewModel(application) {

    data class EmailData(
        val idvisita: String?, val visitante: String?
    )

    var collectedMeetingInfo =
        mutableStateOf(MeetingResponse(0, "", "", "", "", "", "", "", "", ""))

    // Estado para almacenar el código ingresado
    var enteredCode = mutableStateOf("")

    // Estado para controlar si se debe mostrar la animación de error
    var showErrorAnimation = mutableStateOf(false)

    // Nuevo estado para indicar si la carga está en progreso
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Función para activar la animación de error
    private fun triggerErrorAnimation() {
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
        _isLoading.value = true
        return if (enteredCode.value == "8998") {
            _isLoading.value = false
            true
        } else {
            triggerErrorAnimation()
            Log.e("Error", "La respuesta es no válida")
            robotMan.speak("El código no es válido. Inténtelo de nuevo",
                false,
                object : RobotManager.SpeakCompleteListener {
                    override fun onSpeakComplete() {
                        // Acciones a realizar después de hablar
                    }
                })
            _isLoading.value = false
            false
        }
    }

    fun clockIn(): Boolean {
        _isLoading.value = true
        return if (enteredCode.value == "12321") {
            Log.d(
                "current time", System.currentTimeMillis().toString()
            )
            _isLoading.value = false
            true
        } else {
            triggerErrorAnimation()
            Log.e("Error", "La respuesta es no válida")
            robotMan.speak("El código no es válido. Inténtelo de nuevo",
                false,
                object : RobotManager.SpeakCompleteListener {
                    override fun onSpeakComplete() {
                        // Acciones a realizar después de hablar
                    }
                })
            _isLoading.value = false
            false
        }
    }

    // Estado para indicar si el código es correcto
    private val _isCodeCorrect = MutableStateFlow(false)
    val isCodeCorrect: StateFlow<Boolean> = _isCodeCorrect.asStateFlow()

    private var currentToken: String = ""

    fun checkForTaskExecution() {
        Log.d("NumericPanelViewModel", "checkForTaskExecution")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                if (currentToken.isEmpty()) {
                    Log.d("checkForTaskExecution", "currentToken is empty")
                    currentToken = refreshToken() ?: ""
                }

                if (currentToken.isNotEmpty()) {
                    Log.d("checkForTaskExecution", "currentToken is not empty: $currentToken")
                    val response = makeApiCall(currentToken)
                    withContext(Dispatchers.Main) {
                        Log.d("TAG MAIN", "Respuesta: $response")
                        handleApiResponse(response)
                        Log.d("TAG MAIN", "Respuesta manejada")
                    }
                }
            } finally {
                Log.d("TAG MAIN", "Finally")
                _isLoading.value = false
            }
        }
    }

    private fun makeApiCall(token: String): Response {
        Log.d("TAG MAIN", "Haciendo llamada a la API: token: $token")

        // Log enteredCode value
        Log.d("makeApiCall", "enteredCode value: ${enteredCode.value}")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://t2o.intecrobots.com/api/visitas/consultacodigototal?codigo=${enteredCode.value}")
            .addHeader("Authorization", "Bearer $token").get().build()

        // Log the complete URL
        Log.d("makeApiCall Request", "URL: $request")
        resetDigits()
        return client.newCall(request).execute()
    }


    private suspend fun handleApiResponse(response: Response?) {
        if (response != null) {
            val responseBodyString = withContext(Dispatchers.IO) {
                response.body?.string()  // Leer el cuerpo de la respuesta
            }
            Log.d("handleApiResponse", responseBodyString ?: "Respuesta vacía")

            if (response.isSuccessful) {
                // Manejar respuesta exitosa
                // Si la respuesta es exitosa, consideramos que el código es correcto.
                if (responseBodyString?.contains("error") == true) {
                    // Manejar caso de error en el cuerpo de la respuesta
                    withContext(Dispatchers.Main) {
                        triggerErrorAnimation()
                        Log.e("Error", "La respuesta es no válida")
                        robotMan.speak("El código no es válido. Inténtelo de nuevo",
                            false,
                            object : RobotManager.SpeakCompleteListener {
                                override fun onSpeakComplete() {
                                    resetDigits()
                                    _isCodeCorrect.value = false
                                }
                            })
                    }
                    return
                }
                val gson = Gson()
                // Nuevo código para manejar la respuesta como un arreglo
                val type = object : TypeToken<List<MeetingResponse>>() {}.type
                val meetingInfoList = gson.fromJson<List<MeetingResponse>>(responseBodyString, type)
                Log.d("handleApiResponse Success", "meetingInfoList: $meetingInfoList)")
                withContext(Dispatchers.Main) {
                    if (meetingInfoList.isNotEmpty()) {
                        Log.d("handleApiResponse Success", "meetingInfoList is not empty")
                        val meetingInfo = meetingInfoList.first()
                        collectedMeetingInfo.value = meetingInfo
                        _isCodeCorrect.value = true
                        Log.d(
                            "CheckForApiExecution", "El código es correcto: ${isCodeCorrect.value}"
                        )
                        val emailData = EmailData(
                            idvisita = collectedMeetingInfo.value.id.toString(),
                            visitante = collectedMeetingInfo.value.visitante
                        )
                        sendMeetingEmail(emailData)
                        Log.d("meetingInfo", "${collectedMeetingInfo.value}")
                        resetDigits()
                        _isCodeCorrect.value = false
                    } else {
                        // Manejar el caso de que la lista esté vacía
                        triggerErrorAnimation()
                        robotMan.speak("El código no es válido. Inténtelo de nuevo",
                            false,
                            object : RobotManager.SpeakCompleteListener {
                                override fun onSpeakComplete() {
                                    resetDigits()
                                    _isCodeCorrect.value = false
                                }
                            })
                    }
                }
            } else if (response.code == 401) {
                // Manejar error de autenticación
                val newToken = refreshToken()
                if (newToken != null) {
                    Log.d("TAG MAIN", "Token refrescado: $newToken")
                    currentToken = newToken  // Actualizar el token actual
                    val newResponse = makeApiCall(newToken)
                    handleApiResponse(newResponse)  // Manejar la nueva respuesta
                } else {
                    withContext(Dispatchers.Main) {
                        // Manejar otros errores
                        triggerErrorAnimation()
                        robotMan.speak("El código no es válido. Inténtelo de nuevo",
                            false,
                            object : RobotManager.SpeakCompleteListener {
                                override fun onSpeakComplete() {
                                    resetDigits()
                                    _isCodeCorrect.value = false
                                }
                            })
                        Log.e("Error", "Error en la solicitud: Código ${response.code}")
                    }
                }
            } else {
                // Manejar otros errores de respuesta
                withContext(Dispatchers.Main) {
                    // Manejar otros errores
                    triggerErrorAnimation()
                    Log.e("Error", "Error en la solicitud: Código ${response.code}")
                    robotMan.speak("El código no es válido. Inténtelo de nuevo",
                        false,
                        object : RobotManager.SpeakCompleteListener {
                            override fun onSpeakComplete() {
                                resetDigits()
                                _isCodeCorrect.value = false
                            }
                        })
                }
                // Mostrar mensaje de error en la UI o realizar alguna acción
            }
        } else {
            // Manejar el caso en que la respuesta es null
            withContext(Dispatchers.Main) {
                // Manejar el caso en que la respuesta es null
                triggerErrorAnimation()
                Log.e("Error", "La respuesta es null")
                robotMan.speak("El código no es válido. Inténtelo de nuevo",
                    false,
                    object : RobotManager.SpeakCompleteListener {
                        override fun onSpeakComplete() {
                            resetDigits()
                            _isCodeCorrect.value = false
                        }
                    })
            }
            // Mostrar mensaje de error en la UI o realizar alguna acción
        }
    }


    private fun refreshToken(): String? {
        return try {

            val client = OkHttpClient()
            val formBody = FormBody.Builder().add("username", "sergio.escudero@intecrobots.com")
                .add("password", "sec000611").build()
            val request =
                Request.Builder().url("https://t2o.intecrobots.com/api/auth/login").post(formBody)
                    .build()
            Log.d("Request", "URL: ${request.url}, Body: $formBody")
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: ""
                    val jsonObject = JSONObject(responseData)
                    jsonObject.getString("token")
                } else null
            }
        } catch (e: Exception) {
            Log.e("Error", "Error al refrescar el token: ${e.message}")
            null
        }
    }

    private fun bodyToString(request: Request): String {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body!!.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }
    }

    private suspend fun sendMeetingEmail(emailData: EmailData): Boolean? {
        return try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                _isLoading.value = true

                if (currentToken.isEmpty()) {
                    Log.d("checkForTaskExecution", "currentToken is empty")
                    currentToken = refreshToken() ?: ""
                }

                // Crear un objeto JSON con los datos del usuario
                val json = JSONObject().apply {
                    put("visitante", emailData.visitante)
                    put("idvisita", emailData.idvisita)
                    // Agregar más campos según sea necesario
                }

                // Crear el cuerpo de la solicitud como JSON
                val requestBody: RequestBody =
                    json.toString().toRequestBody("application/json".toMediaType())

                // Crear la solicitud con el encabezado y el cuerpo JSON
                val request = Request.Builder()
                    .url("https://t2o.intecrobots.com/api/contactosvisitas/notificaremailllegadas")
                    .post(requestBody).addHeader(
                        "Authorization", "Bearer $currentToken"
                    )  // Agregar el encabezado de autorización
                    .build()
                Log.d("tokennnn", currentToken)
                Log.d("cuerpo", bodyToString(request))

                // Realizar la solicitud y procesar la respuesta
                client.newCall(request).execute().use { response ->
                    Log.d("Response", response.toString())
                    Log.d("ResponseBody", response.body?.string() ?: "No response body")
                    if (response.code == 400) {
                        val newToken = refreshToken()
                        if (newToken != null) {
                            Log.d("TAG MAIN", "Token refrescado: $newToken")
                            currentToken = newToken
                        }
                    }
                    _isLoading.value = false
                    return@withContext response.isSuccessful
                }
            }
        } catch (e: Exception) {
            Log.e("Error", "Error al enviar solicitud: $e")
            return null
        }
    }

    suspend fun postUnknownVisitor(userData: UserData): Boolean? {
        return try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                _isLoading.value = true

                if (currentToken.isEmpty()) {
                    Log.d("checkForTaskExecution", "currentToken is empty")
                    currentToken = refreshToken() ?: ""
                }

                // Crear un objeto JSON con los datos del usuario
                val json = JSONObject().apply {
                    put("nombre", userData.nombre)
                    put("email", userData.email)
                    put("empresa", userData.empresa)
                    put("asunto", userData.asunto)
                    put("tipo", userData.tipo.toString())
                    // Agregar más campos según sea necesario
                }

                // Crear el cuerpo de la solicitud como JSON
                val requestBody: RequestBody =
                    json.toString().toRequestBody("application/json".toMediaType())

                // Crear la solicitud con el encabezado y el cuerpo JSON
                val request = Request.Builder().url("https://t2o.intecrobots.com/api/novisitas/add")
                    .post(requestBody).addHeader(
                        "Authorization", "Bearer $currentToken"
                    )  // Agregar el encabezado de autorización
                    .build()
                Log.d("tokennnn", currentToken)
                Log.d("cuerpo", bodyToString(request))

                // Realizar la solicitud y procesar la respuesta
                client.newCall(request).execute().use { response ->
                    Log.d("Response", response.toString())
                    Log.d("ResponseBody", response.body?.string() ?: "No response body")
                    if (response.code == 400) {
                        val newToken = refreshToken()
                        if (newToken != null) {
                            Log.d("TAG MAIN", "Token refrescado: $newToken")
                            currentToken = newToken
                        }
                    }
                    _isLoading.value = false
                    return@withContext response.isSuccessful
                }
            }
        } catch (e: Exception) {
            Log.e("Error", "Error al enviar solicitud: $e")
            return null
        }
    }


    private fun getMeetingTimeThreshold(): Long {
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
            "MeetingTime", "${
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