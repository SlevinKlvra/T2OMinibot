package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.NumericPanelViewModel

enum class UserType {
    CLIENTE, PROVEEDOR
}

enum class UserExistence {
    NUEVO, EXISTENTE
}

@Composable
fun UnknownVisitScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager,
    numericPanelViewModel: NumericPanelViewModel
) {
    var userType by remember { mutableStateOf<UserType?>(null) }
    var userExistence by remember { mutableStateOf<UserExistence?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4

    val text by mqttViewModel.capturedText.collectAsState()
    Text("Captured Text: $text")

    val isListening by mqttViewModel.isListening.observeAsState(true)

    LaunchedEffect(isListening) {
        mqttViewModel.robotMan.questionPrueba()
        mqttViewModel.listenToSpeechResult()
        if (text != "") {
            Log.d("UnknownVisitScreen", "Escuchando... $text")
            mqttViewModel.stopListening()
        }
    }

    FuturisticGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (currentPage) {
                0 -> UserTypeSelection(onUserTypeSelected = {
                    userType = it
                    currentPage++
                })
                1 -> UserExistenceSelection(onUserExistenceSelected = {
                    userExistence = it
                    currentPage++
                })
                else -> {
                    when (currentPage) {
                        2 -> NameStep(name = name, onNameChange = { name = it })
                        3 -> EmailStep(email = email, onEmailChange = { email = it })
                        4 -> MessageStep(message = message, onMessageChange = { message = it })
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentPage > 2) {
                            TextButton(onClick = { currentPage-- }) {
                                Text("Anterior")
                            }
                        }

                        if (currentPage < totalPages - 1) {
                            TextButton(onClick = { currentPage++ }) {
                                Text("Siguiente")
                            }
                        }

                        if (currentPage == totalPages - 1) {
                            Button(
                                onClick = {
                                    // Acción final según tu lógica
                                },
                                enabled = validateFields()
                            ) {
                                Text("Enviar")
                            }
                        }
                    }
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
        Button(onClick = { onUserTypeSelected(UserType.CLIENTE) }) {
            Text("Cliente")
        }

        Button(onClick = { onUserTypeSelected(UserType.PROVEEDOR) }) {
            Text("Proveedor")
        }
    }
}

@Composable
fun UserExistenceSelection(onUserExistenceSelected: (UserExistence) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { onUserExistenceSelected(UserExistence.NUEVO) }) {
            Text("Nuevo")
        }

        Button(onClick = { onUserExistenceSelected(UserExistence.EXISTENTE) }) {
            Text("Existente")
        }
    }
}

fun validateFields(): Boolean {
    // Implementa la lógica de validación según tus necesidades
    // Aquí puedes verificar si los campos requeridos están llenos y retornar true o false
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Nombre",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Diga o introduzca su nombre",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        TextField(
            value = name,
            onValueChange = { onNameChange(it) },
            label = { Text("Nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailStep(email: String, onEmailChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Correo Electrónico",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Introduzca su correo electrónico",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        TextField(
            value = email,
            onValueChange = { onEmailChange(it) },
            label = { Text("Correo Electrónico") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageStep(message: String, onMessageChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mensaje",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Diga o introduzca su mensaje",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        TextField(
            value = message,
            onValueChange = { onMessageChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(10.dp)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        )
    }
}


