package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.telemedicina.components.TransparentButtonWithIcon
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel
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
                nombre = "test",
                empresa = "test",
                email = "test@gmail.com",
                asunto = "testesttestestestest"
            )
        )
    }

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

    var sendingData by remember { mutableStateOf(false) }
    LaunchedEffect(sendingData) {
        if (sendingData) {
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
                sendingData = false
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
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón Anterior
                    if (currentPage != totalPages) {
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
                                if (validateFields()) {
                                    sendingData = true
                                }
                            },
                            enabled = validateFields() && !sendingData,
                            modifier = Modifier.widthIn(min = 33.dp) // Establecer el ancho deseado
                        ) {
                            Text("Enviar")
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
                        robotManager
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

                    7 -> LastStep(navController, mqttViewModel)
                }
            }
        }
    }
}


@Composable
fun UserTypeSelection(onUserTypeSelected: (UserType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Cliente o proveedor",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransparentButtonWithIcon(text = "Cliente",
                icon = Icons.Outlined.ShoppingCart,
                onClick = { onUserTypeSelected(UserType.CLIENTE) })
            TransparentButtonWithIcon(text = "Proveedor",
                icon = Icons.Outlined.LocationOn,
                onClick = { onUserTypeSelected(UserType.PROVEEDOR) })
        }
    }
}

@Composable
fun UserExistenceSelection(onUserExistenceSelected: (UserExistence) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Usuario nuevo o existente",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransparentButtonWithIcon(text = "Nuevo usuario",
                icon = Icons.Outlined.AddCircle,
                onClick = { onUserExistenceSelected(UserExistence.NUEVO) })
            TransparentButtonWithIcon(text = "Usuario existente",
                icon = Icons.Outlined.Person,
                onClick = { onUserExistenceSelected(UserExistence.EXISTENTE) })
        }
    }
}

fun validateFields(): Boolean {
    return true
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NameStep(name: String, onNameChange: (String) -> Unit, robotManager: RobotManager) {
    val keyboardController = LocalSoftwareKeyboardController.current

    robotManager.speak("Dime tu nombre por favor", true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Text(
            text = "Nombre",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Diga o introduzca su nombre:",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = name,
            onValueChange = { onNameChange(it) },
            label = { Text("Nombre") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EmailStep(email: String, onEmailChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Correo Electrónico",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Introduzca su correo electrónico:",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = email,
            onValueChange = { onEmailChange(it) },
            label = { Text("Correo Electrónico") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CompanyStep(company: String, onCompanyChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Empresa",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Introduzca empresa:",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = company,
            onValueChange = { onCompanyChange(it) },
            label = { Text("Empresa") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MessageStep(message: String, onMessageChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Mensaje",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Diga o introduzca su mensaje:",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = message,
            onValueChange = { onMessageChange(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(4.dp)
                .border(width = 1.dp, color = Color.Black)
        )
    }
}


@Composable
fun LastStep(navController: NavController, mqttViewModel: MqttViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Muchas gracias por su visita",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Su información ha sido enviada. Nos pondremos en contacto con usted lo más rápido posible.",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        TransparentButtonWithIcon(
            text = "Volver",
            icon = Icons.Outlined.ArrowBack,
            onClick = { mqttViewModel.navigateToHomeScreen()}
        )
    }
}
