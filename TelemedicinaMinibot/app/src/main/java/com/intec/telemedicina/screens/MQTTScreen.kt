@file:OptIn(ExperimentalComposeUiApi::class)

package com.intec.telemedicina.screens

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.telemedicina.ui.theme.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.MqttViewModel

//TODO: Implement a class which contains all the robot actions and call them from here.
//- Navigate to a point
//- Stop navigation
//- ...

private const val REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION = 123 // Use any unique integer value

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MqttScreen(navController: NavController, mqttViewModel: MqttViewModel) {

    Log.d("Current Screen", "MQTTScreen")
    val viewModel: MqttViewModel = mqttViewModel

    // Estados para manejar la entrada del usuario
    val brokerIp by viewModel.brokerIp.observeAsState("")

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

    var topicToSubscribe by remember { mutableStateOf("") }
    var topicToPublish by remember { mutableStateOf("") }
    var messageToPublish by remember { mutableStateOf("") }

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

    /*if(openScreen && (navController.currentDestination?.route.toString() != "driving_face_screen")) {
        Log.d("DRIVINGSCREEN", "Open the drivingscreen: ${navController.currentDestination!!.route}")
        navController.navigate(AppScreens.DrivingFaceScreen.route)
    }*/
    /*else if(!openScreen && (navController.currentDestination?.route.toString() == "driving_face_screen")){
        navController.popBackStack()
    }*/

    val destinations: List<Pose> by mqttViewModel.posesList.collectAsState()
    var expanded = remember { mutableStateOf(false) }
    val selectedItem = mqttViewModel.selectedItem.value

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
                DownloadInstallButton()
                FloatingActionButton(
                    onClick = { mqttViewModel.navigateToHomeScreen() },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomStart),
                    containerColor = md_theme_light_tertiary
                ) { // You can set the house icon here using painterResource
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }

            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = "Admin Mode",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
                    SwitchDemo(mqttViewModel)
                }
            }
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Button(onClick = {
                        viewModel.robotMan.goCharge()
                        viewModel.navigateToEyesScreen()
                    }) {
                        Text("Cargar robot")
                    }
                    Spacer(modifier = Modifier.width(5.dp)) // Espacio entre el Text y el Switch
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(fontWeight = FontWeight.Bold, text = "MQTT Connection params")
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Broker IP",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Puerto",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Id cliente",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Usuario",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Contraseña",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "QoS",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
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
                Text("Guardar Configuración MQTT")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(fontWeight = FontWeight.Bold, text = "Robot params")
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Tiempo de espera",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
                    TextField(
                        value = waitTime.toString(),
                        onValueChange = {
                            waitTime = it.toIntOrNull()
                                ?: 0 // Convierte de nuevo a Int, usa 0 si no es un número
                        },
                        label = { Text("Tiempo de Espera (segundos)") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Margen de impuntualidad",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
                    TextField(
                        value = meetingMeetingThreshold.toString(),
                        onValueChange = {
                            meetingMeetingThreshold = it.toIntOrNull()
                                ?: 0 // Convierte de nuevo a Int, usa 0 si no es un número
                        },
                        label = { Text("Margen de impuntualidad") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            //TO-DO: Utilizar DropDownMenu para seleccionar el destino de retorno
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Button(onClick = {
                        expanded.value = !expanded.value
                        mqttViewModel.getListPoses()
                        Log.d("MQTTScreen", "Expanded: $destinations")
                    }) {
                        Text("Modificar punto retorno")
                    }

                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                    ) {
                        destinations.forEachIndexed { index, label ->
                            DropdownMenuItem(
                                text = { Text(label.name) },
                                onClick = {
                                    returnDestination = label.name
                                    mqttViewModel.setSelectedItem(label.name)
                                    expanded.value = false
                                },
                                enabled = expanded.value
                            ) // Añade un elemento al menú
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = selectedItem ?: "Ninguno", modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Margen de desviación de coordenadas",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
                    TextField(
                        value = coordinateDeviation.toString(),
                        onValueChange = {
                            coordinateDeviation = it.toFloatOrNull()
                                ?: 0f // Convierte de nuevo a Int, usa 0 si no es un número
                        },
                        label = { Text("Margen de desviación de coordenadas") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Timeout de navegación",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre el Text y el Switch
                    TextField(
                        value = navigationTimeout.toString(),
                        onValueChange = {
                            navigationTimeout = it.toLongOrNull()
                                ?: 0 // Convierte de nuevo a Int, usa 0 si no es un número
                        },
                        label = { Text("Timeout de navegación") }
                    )
                }
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
                    Text("Guardar Configuración Robot")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(fontWeight = FontWeight.Bold, text = "API Connection params")
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Usuario API",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
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
                    modifier = Modifier.fillMaxWidth(), // Asegura que el Row ocupe todo el ancho disponible
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente los elementos en el centro
                ) {
                    Text(
                        text = "Password API",
                        modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible empujando el Switch hacia la derecha
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el Text y el Switch
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
                Text("Guardar Configuración API")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(fontWeight = FontWeight.Bold, text = "MQTT Connection Status: $connectionState")
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    viewModel.connect()
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Connect")
                }
                Button(onClick = {
                    viewModel.disconnect()
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Disconnect")
                }
                Button(onClick = {

                }) {
                    Text("Disconnect")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Subscribe")
                }
                Button(onClick = {
                    val topics: MutableList<String> =
                        viewModel.resumeTopics()  // Asume que resumeTopics está en el ViewModel.
                    viewModel.subscribeToAllTopics(topics)  // Asume que subscribeToAllTopics está en el ViewModel.
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Suscripción rápida")
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
                Text("Publish Message")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(messages) { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

//val destinations : List<Pose> by mqttViewModel.posesList.collectAsState()
//val posesList = MutableStateFlow(emptyList<Pose>())
/*fun getListPoses(){
        posesList.value = robotMan.getPoses()
    }*/
@Composable
fun SwitchDemo(mqttViewModel: MqttViewModel) {
    val checkedState by mqttViewModel.adminState.collectAsState()

    Switch(
        checked = checkedState,
        onCheckedChange = { newValue -> mqttViewModel.setAdminState(newValue) }
    )
}

@RequiresApi(Build.VERSION_CODES.R)
private fun downloadAndInstallApk(context: Context) {

    Log.d("DownloadAndInstall", "Entering downloadAndInstallApk function")

    // Verifica si tienes el permiso MANAGE_EXTERNAL_STORAGE (requerido para la ubicación específica en Android 11 y superior)
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Si no tienes el permiso, solicítalo
        Log.d("DownloadAndInstall", "Requesting MANAGE_EXTERNAL_STORAGE permission")
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION
        )
        return
    }

    // Reemplaza la URL con el enlace de descarga real
    val apkUrl = "https://testdownload.onrender.com/descargar-archivo"

    // Configura la solicitud de descarga
    val request = DownloadManager.Request(Uri.parse(apkUrl))
        .setTitle("testapp")
        .setDescription("Descargando la última versión de la aplicación")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "app-debug.apk")

    // Obtén el servicio de DownloadManager y encola la solicitud de descarga
    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    try {
        downloadManager.enqueue(request)
        Log.d("DownloadAndInstall", "Enqueued the download request")
    } catch (e: Exception) {
        Log.e("DownloadAndInstall", "Error enqueuing the download request: $e")
        e.printStackTrace()
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DownloadInstallButton() {
    val context = LocalContext.current

    // Botón de descarga e instalación
    Button(onClick = {
        downloadAndInstallApk(context)
    }) {
        Text("Descargar e Instalar APK")
    }
}
