package com.intec.t2o.screens

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.t2o.R
import com.intec.t2o.components.DrivingComposable
import com.intec.t2o.components.HomeScreenButtonCard
import com.intec.t2o.components.NavigationButton
import com.intec.t2o.components.PressableEyes
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.viewmodels.MqttViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager
) {

    Log.d("Current Screen", "HomeScreen")


    var showDrivingComposable by remember { mutableStateOf(false) }
    val adminMode by mqttViewModel.adminState.collectAsState(initial = false)

    LaunchedEffect(key1 = true) {
        robotManager.speak(
            "Hola, soy Píter, bienvenido a t2ó, en qué puedo ayudarte? Por favor, pulsa en el menú para elegir alguna de las opciones",
            true,
            object : RobotManager.SpeakCompleteListener {
                override fun onSpeakComplete() {
                }
            })
    }

    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    FuturisticGradientBackground {

        if (!mqttViewModel.isNavigating.value) {


            Column(modifier = Modifier.fillMaxSize()) {
                Cabecera(mqttViewModel = mqttViewModel)
                Botones(
                    mqttViewModel = mqttViewModel,
                )
                if (adminMode) {
                    LazyRowUbicaciones(
                        mqttViewModel = mqttViewModel,
                        modifier = Modifier,
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.speechrecognition,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 5.dp)
                    )
                }
            }
        } else {
            Log.d("SECUENCIA DRIVING", "PressableEyes")
            PressableEyes(
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    mqttViewModel.isNavigating.value = false
                    robotManager.stopNavigation()
                    showDrivingComposable = true
                }
            )
        }

        if (showDrivingComposable) {
            Log.d("SECUENCIA DRIVING", "DrivingComposable")
            DrivingComposable(
                navController = navController,
                mqttViewModel = mqttViewModel,
                robotManager = robotManager,
                onCancel = {
                    mqttViewModel.setReturningHome(true)
                    showDrivingComposable = false
                },
                onContinue = {
                    mqttViewModel.robotMan.resumeNavigation(onNavigationComplete = {
                        mqttViewModel.isNavigating.value = false
                        mqttViewModel.navigateToEyesScreen()
                    })
                    showDrivingComposable = false
                }
            )
        }
    }
}


@Composable
fun Cabecera(mqttViewModel: MqttViewModel) {
    var clickCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logot2o),
            contentDescription = "logo",
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    clickCount++
                    if (clickCount == 5) {
                        mqttViewModel.navigateToMqttScreen()
                        clickCount = 0
                    }
                }
        )
        Text(
            text = "¿Cuál es el motivo de su visita?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun LazyRowUbicaciones(
    mqttViewModel: MqttViewModel,
    modifier: Modifier = Modifier,
) {

    mqttViewModel.getListPoses()
    val destinations: List<Pose> by mqttViewModel.posesList.collectAsState()

    LazyRow(
        modifier = modifier.then(
            Modifier
                .background(Color.Transparent)
                .fillMaxWidth()
        ),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Los botones que se crean a partir de la lista del viewModel
        items(items = destinations) { item ->
            Log.d("DESTINATIONS", item.name)
            // Un botón con el onClick y el estilo que quieras
            NavigationButton(item.name, onClick = {
                Log.d("asdsadasdadsdsada", item.name)
                mqttViewModel.isNavigating.value = true
                mqttViewModel.robotMan.startNavigation(
                    0,
                    item.name,
                    0.1,
                    1000000,
                    navigationCompleteListener = object :
                        RobotManager.NavigationCompleteListener {
                        override fun onNavigationComplete() {
                            mqttViewModel.isNavigating.value = false
                            mqttViewModel.navigateToEyesScreen()
                        }
                    })
            })
        }
    }
}

@Composable
fun Botones(
    mqttViewModel: MqttViewModel,
) {

    // Una lista de iconos para los botones
    val iconos = listOf(
        Icons.Default.DateRange,
        Icons.Default.Person,
        ImageVector.vectorResource(R.drawable.shipping)
    )
    // Una lista de textos para los botones
    val textos = listOf("Cita", "Reunión", "Entregas")
    val indicaciones =
        listOf(
            "\"tengo una visita...\"",
            "\"tengo una reunión...\"",
            "\"tengo una entrega...\""
        )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Ajusta la altura al contenido,
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            // Alineación y espaciado adicionales si son necesarios
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            // Tre botones con los colores, iconos y textos correspondientes
            items(3) { index ->

                HomeScreenButtonCard(
                    text = textos[index],
                    indicacion = indicaciones[index],
                    icon = iconos[index]
                )
                {
                    when (index) {
                        0 -> mqttViewModel.navigateToUnknownVisitsScreen()
                        1 -> mqttViewModel.navigateToNumericPanelScreen()
                        2 -> mqttViewModel.navigateToPackageAndMailManagementScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MqttButton(mqttViewModel: MqttViewModel) {
    Box(contentAlignment = Alignment.TopStart) {
        Image(
            painter = painterResource(id = R.drawable.intecrobots_circulo),
            contentDescription = "logo",
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.mqtt_button_size))
                .clickable {
                    mqttViewModel.navigateToAdminPanelScreen()
                })
    }
}

@Composable
fun ClockInBtn(mqttViewModel: MqttViewModel) {
    Box(contentAlignment = Alignment.TopStart) {
        Image(
            painter = painterResource(id = R.drawable.clockicon),
            contentDescription = "logo",
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.mqtt_button_size))
                .clickable {
                    mqttViewModel.navigateToClockInScreen()
                })
    }
}
