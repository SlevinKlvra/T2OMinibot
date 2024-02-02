@file:OptIn(ExperimentalComposeUiApi::class)

package com.intec.t2o.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.t2o.ui.theme.md_theme_light_tertiary
import com.intec.t2o.viewmodels.MqttViewModel
import java.io.File

//TODO: Implement a class which contains all the robot actions and call them from here.
//- Navigate to a point
//- Stop navigation
//- ...

private const val REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION = 123

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MQTTScreen(navController: NavController, mqttViewModel: MqttViewModel) {

    Log.d("Current Screen", "MQTTScreen")
    val viewModel: MqttViewModel = mqttViewModel

    // Estados para manejar la entrada del usuario

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

    val destinations: List<Pose> by mqttViewModel.posesList.collectAsState()
    val expanded = remember { mutableStateOf(false) }
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
                        destinations.forEachIndexed { _, label ->
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
            Text("Descargar e instalar actualizaciones:")
            Text(
                "(Si la primera vez pide permisos y la descarga no se inicia, pulse el botón de nuevo)",
                modifier = Modifier.size(5.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            DownloadInstallButton()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "V 1.0",
            )
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

@Composable
fun SwitchDemo(mqttViewModel: MqttViewModel) {
    val checkedState by mqttViewModel.adminState.collectAsState()

    Switch(
        checked = checkedState,
        onCheckedChange = { newValue -> mqttViewModel.setAdminState(newValue) }
    )
}

@Composable
fun DownloadInstallButton() {
    var isDownloading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // URL de la APK que deseas descargar
    val apkUrl = "https://testdownload.onrender.com/descargar-archivo"

    Button(onClick = {
        // Verificar permisos de almacenamiento externo
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Iniciar la descarga
            downloadAndInstall(context, apkUrl)
            isDownloading = true
        } else {
            // Solicitar permisos de almacenamiento externo
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }) {
        Text("Buscar actualizaciones")
    }

    // Mostrar ProgressBar durante la descarga
    if (isDownloading) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}


@SuppressLint("UnspecifiedRegisterReceiverFlag")
private fun downloadAndInstall(context: Context, apkUrl: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Crear una solicitud de descarga
    val request = DownloadManager.Request(Uri.parse(apkUrl))
        .setTitle("app-debug.apk")
        .setDescription("Descargando la última versión...")
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-debug.apk")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    // Enqueue la solicitud de descarga
    val downloadId = downloadManager.enqueue(request)

    // Agregar un BroadcastReceiver para controlar la finalización de la descarga
    val onComplete = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context, intent: Intent) {
            val downloadedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadedId == downloadId) {
                Log.d("Download", "Descarga completada")

                // Obtener el URI de la APK descargada
                val downloadQuery = DownloadManager.Query()
                downloadQuery.setFilterById(downloadId)
                val cursor = downloadManager.query(downloadQuery)
                if (cursor.moveToFirst()) {
                    val filePath =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    cursor.close()

                    // Instalar la APK
                    installApk(context, filePath)
                }
            }
        }
    }
    // Registrar el BroadcastReceiver
    context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}

private fun installApk(context: Context, filePath: String) {
    try {
        // Convertir el URI a un archivo
        val file = File(Uri.parse(filePath).path!!)
        Log.d("InstallApk", "File path: ${file.absolutePath}")

        // Obtener el directorio de descargas
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Construir la ruta completa del archivo APK en el directorio de descargas
        val apkFile = File(downloadsDirectory, file.name)
        Log.d("InstallApk", "APK File path: ${apkFile.absolutePath}")

        // Obtener el URI del archivo utilizando FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "com.intec.t2o.fileprovider",
            apkFile
        )
        Log.d("InstallApk", "Content URI: $contentUri")

        // Crear un intent para la instalación de la APK
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            data = contentUri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }


        // Iniciar la actividad de instalación
        Log.d("InstallApk", "Starting installation activity...")
        context.startActivity(installIntent)

        Log.d("InstallApk", "Intento de instalación exitoso")

    } catch (e: Exception) {
        // Capturar excepciones y registrarlas
        Log.e("InstallApk", "Error al intentar instalar la APK", e)
    }
}









