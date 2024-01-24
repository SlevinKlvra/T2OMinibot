package com.intec.t2o.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.t2o.components.GoBackButton
import com.intec.t2o.components.LoadingSpinner
import com.intec.t2o.components.novisit.CompanyStep
import com.intec.t2o.components.novisit.EmailStep
import com.intec.t2o.components.novisit.LastStep
import com.intec.t2o.components.novisit.MessageStep
import com.intec.t2o.components.novisit.NameStep
import com.intec.t2o.components.novisit.UserExistenceSelection
import com.intec.t2o.components.novisit.UserTypeSelection
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class UserType {
    CLIENTE, PROVEEDOR
}

enum class UserExistence {
    NUEVO, EXISTENTE
}

data class UserData(
    val tipo: UserType?,
    val userExistence: UserExistence?,
    val nombre: String,
    val empresa: String,
    val email: String,
    val asunto: String
)

@Composable
fun UnknownVisitScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager,
    numericPanelViewModel: NumericPanelViewModel
) {
    Log.d("Current Screen", "UnknownVisitScreen")
    var userData by remember {
        mutableStateOf(
            UserData(
                tipo = null,
                userExistence = null,
                nombre = "",
                empresa = "",
                email = "",
                asunto = ""
            )
        )
    }

    val context = LocalContext.current
    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 7

    val text by mqttViewModel.capturedText.collectAsState()

    val isListening by mqttViewModel.isListening.observeAsState(true)

    LaunchedEffect(isListening) {
        Log.d("isListening true", "launchedEffect")
        mqttViewModel.robotMan.questionPrueba()
        mqttViewModel.listenToSpeechResult()
        if (text != "") {
            Log.d("UnknownVisitScreen", "Escuchando... $text")
            mqttViewModel.stopListening()
        }
    }

    var isSendingData by remember { mutableStateOf(false) }

    LaunchedEffect(isSendingData) {
        if (isSendingData) {
            try {
                val success = withContext(Dispatchers.IO) {
                    numericPanelViewModel.postUnknownVisitor(userData)
                }
                if (success == true) {
                    Log.d("peticion hecha", "peticion")
                    currentPage++
                } else {
                    Log.d("peticion no hecha", "no peticion")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error al enviar la solicitud: $e")
            } finally {
                isSendingData = false
            }
        }
    }

    FuturisticGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
        ) {
            if (currentPage != totalPages) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Anterior
                    if (currentPage != totalPages) {
                        if (currentPage == 1) GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
                        else {
                            TextButton(
                                onClick = {
                                    if (currentPage > 1) {
                                        currentPage--
                                    } else {
                                        mqttViewModel.navigateToHomeScreen()
                                    }
                                },
                                modifier = Modifier.widthIn(min = 33.dp) // Establecer el ancho deseado
                            ) {
                                Text("Anterior")
                            }
                        }
                    }
                    Text(
                        text = "$currentPage de ${totalPages - 1}",
                        color = Color.White,
                        modifier = Modifier.widthIn(min = 33.dp) // Establecer el ancho deseado
                    )
                    // Botón Siguiente
                    if (currentPage != totalPages - 1) {
                        TextButton(
                            onClick = {
                                if (currentPage < totalPages) {
                                    currentPage++
                                }
                            },
                            enabled = userData.userExistence != null && userData.tipo != null,
                            modifier = Modifier.widthIn(min = 33.dp) // Establecer el ancho deseado
                        ) {
                            Text("Siguiente")
                        }
                    }

                    // Botón Enviar en la última página
                    if (currentPage == totalPages - 1) {
                        Button(
                            onClick = {
                                if (validateFields(context, userData)) {
                                    isSendingData = true
                                }
                            },
                            enabled = !isSendingData,
                            modifier = Modifier.widthIn(min = 33.dp)
                        ) {
                            if (isSendingData) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Enviar")
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier) {
                when (currentPage) {
                    1 -> UserTypeSelection(onUserTypeSelected = {
                        userData = userData.copy(tipo = it)
                        currentPage++
                    })

                    2 -> UserExistenceSelection(onUserExistenceSelected = {
                        userData = userData.copy(userExistence = it)
                        currentPage++
                    })

                    3 -> NameStep(
                        name = userData.nombre,
                        onNameChange = { userData = userData.copy(nombre = it) },
                    )

                    4 -> CompanyStep(
                        company = userData.empresa,
                        onCompanyChange = { userData = userData.copy(empresa = it) },
                    )

                    5 -> EmailStep(
                        email = userData.email,
                        onEmailChange = { userData = userData.copy(email = it) })

                    6 -> MessageStep(
                        message = userData.asunto,
                        onMessageChange = { userData = userData.copy(asunto = it) })

                    7 -> {
                        if (isLoading) LoadingSpinner()
                        else {
                            LastStep(mqttViewModel)
                        }
                    }
                }
            }
        }
    }
}

fun validateFields(context: Context, userData: UserData): Boolean {
    // Check if all fields are filled
    if (userData.tipo == null || userData.userExistence == null ||
        userData.nombre.isBlank() || userData.empresa.isBlank() ||
        userData.email.isBlank() || userData.asunto.isBlank()
    ) {
        showToast(context, "Todos los campos deben llenarse")
        return false
    }

    // Check if the email address is valid
    val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
    if (!userData.email.matches(emailRegex)) {
        showToast(context, "Ingrese una dirección de correo electrónico válida")
        return false
    }

    return true
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
