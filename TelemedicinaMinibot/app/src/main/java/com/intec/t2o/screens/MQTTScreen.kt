@file:OptIn(ExperimentalComposeUiApi::class)

package com.intec.t2o.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.t2o.components.GoBackButton
import com.intec.t2o.components.mqtt.DownloadInstallButton
import com.intec.t2o.components.mqtt.MqttScreenOptionTitle
import com.intec.t2o.components.mqtt.MqttSeparator
import com.intec.t2o.viewmodels.MqttViewModel

const val REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION = 123

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MQTTScreen(navController: NavController, mqttViewModel: MqttViewModel) {

    Log.d("Current Screen", "MQTTScreen")
    val viewModel: MqttViewModel = mqttViewModel

    val connectionState by viewModel.connectionState
    val messages by viewModel.incomingMessages

    val shouldHideKeyboard by viewModel.shouldHideKeyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(shouldHideKeyboard) {
        if (shouldHideKeyboard) {
            keyboardController?.hide()
            viewModel.resetHideKeyboardTrigger()
        }
    }

    //VARIABLES CONFIGURACION MQTT
    var ipMqtt by remember { mutableStateOf(viewModel.getBrokerIpDefaultValue()) }
    var portMqtt by remember { mutableStateOf(viewModel.getBrokerPortDefaultValue()) }
    var usuarioMqtt by remember { mutableStateOf(viewModel.getBrokerUserDefaultValue()) }
    var passwordMqtt by remember { mutableStateOf(viewModel.getBrokerPasswordDefaultValue()) }
    var clientMqtt by remember { mutableStateOf(viewModel.getBrokerClientDefaultValue()) }
    var qos by remember { mutableStateOf(viewModel.getBrokerQoSDefaultValue()) }

    //VARIABLES CONFIGURACION API
    var usuarioApi by remember { mutableStateOf(viewModel.getApiUserDefaultValue()) }
    var passwordApi by remember { mutableStateOf(viewModel.getApiPasswordDefaultValue()) }

    //VARIBLES CONFIGURACION ROBOT
    var waitTime by remember { mutableStateOf(viewModel.getWaitingTimeDefaultValue()) }
    var meetingMeetingThreshold by remember { mutableStateOf(viewModel.getMeetingTimeThresholdDefaultValue()) }
    var returnDestination by remember { mutableStateOf(viewModel.getReturnDestinationDefaultValue()) }
    var coordinateDeviation by remember { mutableStateOf(viewModel.getCoordinateDeviationDefaultValue()) }
    var navigationTimeout by remember { mutableStateOf(viewModel.getNavigationTimeoutDefaultValue()) }

    val destinations: List<Pose> by mqttViewModel.posesList.collectAsState()
    val expanded = remember { mutableStateOf(false) }
    val selectedItem = mqttViewModel.selectedItem.value

    FuturisticGradientBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    GoBackButton(onClick = { mqttViewModel.navigateToHomeScreen() })
                }

                Column {
                    Text(
                        "Modo administrador:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "(Actívalo para poder navegar a cualquier lugar desde la pantalla de inicio)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Admin Mode",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SwitchDemo(mqttViewModel)
                    }
                }
                MqttSeparator()
                Column {
                    Text("Cargar robot:", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "(Envía el robot a la base de carga)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            viewModel.robotMan.goCharge()
                            viewModel.navigateToEyesScreen()
                        }) {
                            Text("Cargar robot", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
                MqttSeparator()
                MqttScreenOptionTitle(
                    title = "Descargar e instalar actualizaciones:",
                    subtitle = "(Si la primera vez pide permisos y la descarga no se inicia, pulse el botón de nuevo)"
                )
                DownloadInstallButton()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Versión 1.1.1",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                MqttSeparator()
                MqttScreenOptionTitle(
                    title = "MQTT Connection params:",
                    subtitle = "(Parámetros necesarios para realizar la conexión MQTT)"
                )
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Broker IP",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = ipMqtt,
                            onValueChange = { ipMqtt = it },
                            label = { Text("IP") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Puerto",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = portMqtt,
                            onValueChange = { portMqtt = it },
                            label = { Text("Puerto") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Id cliente",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = clientMqtt,
                            onValueChange = { clientMqtt = it },
                            label = { Text("Id cliente") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Usuario",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = usuarioMqtt,
                            onValueChange = { usuarioMqtt = it },
                            label = { Text("Usuario Mqtt") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Contraseña",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = passwordMqtt,
                            onValueChange = { passwordMqtt = it },
                            label = { Text("Contraseña") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "QoS",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = qos,
                            onValueChange = { qos = it },
                            label = { Text("QoS") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    Log.d(
                        "MqttScreen",
                        "Guardando configuración: $ipMqtt, $portMqtt, $usuarioMqtt, $passwordMqtt, $qos, $clientMqtt"
                    )
                    viewModel.guardarConfiguracionMqtt(
                        ipMqtt,
                        portMqtt,
                        usuarioMqtt,
                        passwordMqtt,
                        qos,
                        clientMqtt
                    )
                }) {
                    Text("Guardar Configuración MQTT", color = Color.White)
                }
                MqttSeparator()
                MqttScreenOptionTitle(
                    title = "Robot params:",
                    subtitle = "(Parámetros de configuración del robot)"
                )
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Tiempo de espera",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = waitTime.toString(),
                            onValueChange = {
                                waitTime = it.toIntOrNull()
                                    ?: 0
                            },
                            label = { Text("Tiempo de Espera (segundos)") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Margen de impuntualidad",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = meetingMeetingThreshold.toString(),
                            onValueChange = {
                                meetingMeetingThreshold = it.toIntOrNull()
                                    ?: 0
                            },
                            label = { Text("Margen de impuntualidad") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Margen de desviación de coordenadas",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = coordinateDeviation.toString(),
                            onValueChange = {
                                coordinateDeviation = it.toFloatOrNull()
                                    ?: 0f
                            },
                            label = { Text("Margen de desviación de coordenadas") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Timeout de navegación",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = navigationTimeout.toString(),
                            onValueChange = {
                                navigationTimeout = it.toLongOrNull()
                                    ?: 0
                            },
                            label = { Text("Timeout de navegación") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        expanded.value = !expanded.value
                        mqttViewModel.getListPoses()
                        Log.d("MQTTScreen", "Expanded: $destinations")
                    }) {
                        Text("Modificar punto retorno", color = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                    ) {
                        destinations.forEachIndexed { _, label ->
                            DropdownMenuItem(
                                text = { Text(label.name) },
                                onClick = {
                                    returnDestination = label.name
                                    mqttViewModel.setSelectedItem(label.name)
                                    expanded.value = false
                                },
                                enabled = expanded.value
                            )
                        }
                    }
                    Text(
                        text = selectedItem ?: "Ninguno",
                        modifier = Modifier.padding(end = 5.dp),
                        color = Color.White,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Button(onClick = {
                        Log.d("MqttScreen", "Guardando configuración Robot: $waitTime, ")
                        viewModel.guardarConfiguracionRobot(
                            waitTime,
                            meetingMeetingThreshold,
                            returnDestination,
                            coordinateDeviation,
                            navigationTimeout
                        )
                    }) {
                        Text("Guardar Configuración Robot", color = Color.White)
                    }
                }
                MqttSeparator()
                MqttScreenOptionTitle(
                    title = "API params:",
                    subtitle = "(Parámetros necesarios para realizar la conexión a la API)"
                )
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Usuario API",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = usuarioApi,
                            onValueChange = { usuarioApi = it },
                            label = { Text("Usuario Api") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = "Password API",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = passwordApi,
                            onValueChange = { passwordApi = it },
                            label = { Text("Password Api") }
                        )
                    }
                }
                Button(onClick = {
                    Log.d("MqttScreen", "Guardando configuración API: $usuarioApi, $passwordApi")
                    viewModel.guardarConfiguracionAPI(usuarioApi, passwordApi)
                }) {
                    Text("Guardar Configuración API", color = Color.White)
                }
                MqttSeparator()
                Text(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    text = "MQTT Connection Status: $connectionState"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        viewModel.connect()
                        viewModel.triggerHideKeyboard()
                    }) {
                        Text("Connect", color = Color.White)
                    }
                    Button(onClick = {
                        viewModel.disconnect()
                        viewModel.triggerHideKeyboard()
                    }) {
                        Text("Disconnect", color = Color.White)
                    }
                }
                MQTTParams(messages = messages, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MQTTParams(messages: List<String>, viewModel: MqttViewModel) {
    var isExpanded by remember { mutableStateOf(false) }

    var topicToSubscribe by remember { mutableStateOf("") }
    var topicToPublish by remember { mutableStateOf("") }
    var messageToPublish by remember { mutableStateOf("") }

    Button(onClick = { isExpanded = !isExpanded }) {
        Text(text = if (isExpanded) "Ocultar mensajes" else "Mostrar mensajes")
    }

    if (isExpanded) {
        TextField(
            value = topicToSubscribe,
            onValueChange = { topicToSubscribe = it },
            label = { Text("Topic to Subscribe") },
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            Button(onClick = {
                viewModel.subscribeToTopic(topicToSubscribe)
                viewModel.triggerHideKeyboard()
            }) {
                Text("Subscribe", color = Color.White)
            }
            Button(onClick = {
                val topics: MutableList<String> =
                    viewModel.resumeTopics()
                viewModel.subscribeToAllTopics(topics)
                viewModel.triggerHideKeyboard()
            }) {
                Text("Suscripción rápida", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = topicToPublish,
            onValueChange = { topicToPublish = it },
            label = { Text("Topic to Publish") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = messageToPublish,
            onValueChange = { messageToPublish = it },
            label = { Text("Message to Publish") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            viewModel.publishMessage(topicToPublish, messageToPublish)
            viewModel.triggerHideKeyboard()
        }) {
            Text("Publish Message", color = Color.White)
        }

        Column(modifier = Modifier.padding(8.dp)) {
            messages.forEach { message ->
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun SwitchDemo(mqttViewModel: MqttViewModel) {
    val checkedState by mqttViewModel.adminState.collectAsState()

    Switch(
        checked = checkedState,
        onCheckedChange = { newValue -> mqttViewModel.setAdminState(newValue) }
    )
}
