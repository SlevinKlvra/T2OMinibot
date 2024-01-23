package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.telemedicina.R
import com.intec.telemedicina.components.NavigationButton
import com.intec.telemedicina.components.TransparentButtonWithIconAndText
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager
) {

    Log.d("Current Screen", "HomeScreen")
    val mqttViewModel: MqttViewModel = mqttViewModel

    val adminMode by mqttViewModel.adminState.collectAsState(initial = false)

    val openEyesScreen by mqttViewModel.openEyesScreen.collectAsState()

    LaunchedEffect(key1 = true) {
        robotManager.speak("Bienvenido a t, 2, o, media. ¿Cuál es el motivo de su visita?", true)
    }

    if (openEyesScreen) {
        Log.d("HomeScreen openEyes", "true")
        //navController.navigate(AppScreens.EyesScreen.route)
        mqttViewModel.navigateToEyesScreen()
        mqttViewModel.closeHomescreen()
    }

    FuturisticGradientBackground {

        Column(modifier = Modifier.fillMaxSize()) {
            Cabecera(navController = navController, mqttViewModel = mqttViewModel)
            if (!adminMode) Spacer(modifier = Modifier.size(30.dp))
            Botones(mqttViewModel = mqttViewModel)
            if (adminMode) {
                LazyRowUbicaciones(
                    mqttViewModel = mqttViewModel,
                    modifier = Modifier,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun Cabecera(navController: NavController, mqttViewModel: MqttViewModel) {
    // Una columna con un espacio fijo entre sus elementos
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // El Row que contiene el título
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MqttButton(mqttViewModel = mqttViewModel)
            Text(
                text = "¿Cuál es el motivo de su visita?",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            ClockInBtn(mqttViewModel = mqttViewModel)
        }
    }
}

@Composable
fun LazyRowUbicaciones(
    mqttViewModel: MqttViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
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
                mqttViewModel.robotMan.startNavigation(0, item.name, 0.1, 1000000)
                navController.navigate(AppScreens.EyesScreen.route)
            })
        }
    }
}

@Composable
fun Botones(mqttViewModel: MqttViewModel) {

    /*val rutas = listOf(
        AppScreens.UnknownVisitScreen.route,
        AppScreens.NumericPanelScreen.route,
        AppScreens.PackageAndMailManagementScreen.route
    )*/
    // Una lista de iconos para los botones
    val iconos = listOf(Icons.Default.Person, Icons.Default.Place, Icons.Default.MailOutline)
    // Una lista de textos para los botones
    val textos = listOf("VISITA", "REUNIÓN", "MENSAJERÍA")
    val indicaciones =
        listOf("\"tengo una visita...\"", "\"tengo una reunión...\"", "\"soy de mensajería...\"")

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            // Tre botones con los colores, iconos y textos correspondientes
            items(3) { index ->

                TransparentButtonWithIconAndText(
                    text = textos[index],
                    indicacion = indicaciones[index],
                    icon = iconos[index], // Reemplaza con tu icono
                    onClick = {
                        when (index) {
                            0 -> mqttViewModel.navigateToUnknownVisitsScreen()
                            1 -> mqttViewModel.navigateToNumericPanelScreen()
                            2 -> mqttViewModel.navigateToPackageAndMailManagementScreen()
                        }
                    }
                )
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
