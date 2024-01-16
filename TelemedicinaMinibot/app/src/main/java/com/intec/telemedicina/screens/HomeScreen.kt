package com.intec.telemedicina.screens

import NavigationDialog
import WelcomeDialog
import android.util.Log
import android.view.SoundEffectConstants
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.telemedicina.R
import com.intec.telemedicina.components.TransparentButtonWithIconAndText
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.ui.theme.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun HomeScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel, mqttViewModel: MqttViewModel, robotManager: RobotManager){

    val splashScreenViewModel: SplashScreenViewModel = splashScreenViewModel
    val mqttViewModel: MqttViewModel = mqttViewModel
    val robotManager = robotManager

    val showDialog by splashScreenViewModel.showNavigationDialog.collectAsState()
    val showQuestionsDialog by mqttViewModel.showQuestionsDialog.collectAsState()
    val showWelcomeDialog by mqttViewModel.showWelcomeDialog.collectAsState()

    val showDrivingScreenFace by mqttViewModel.showDrivingScreenFace.collectAsState()
    val closeDrivingScreenFace by mqttViewModel.closeDrivingScreenFace.collectAsState()

    val adminMode by mqttViewModel.adminState.collectAsState(initial = false)


    /*if(!mqttViewModel.getInitiatedStatus()){
        mqttViewModel.connect()
        Log.d("Topicslist",mqttViewModel.resumeTopics().toString())
        //mqttViewModel.subscribeToAllTopics(mqttViewModel.resumeTopics())
    }*/

    /*if(showDrivingScreenFace){ //&& (navController.currentDestination?.route.toString() != "driving_face_screen")) {
        Log.d("DRIVINGSCREEN", "Open the drivingscreen: ${navController.currentDestination!!.route}")
        navController.navigate(AppScreens.DrivingScreen.route)
        mqttViewModel.deactivateOpenDrivingScreenFace()
    }*/

    /*if(closeDrivingScreenFace) {
        navController.navigateUp()
        mqttViewModel.deactivateCloseDrivingScreenFace()
    }*/

    val openEyesScreen by mqttViewModel.openEyesScreen.collectAsState()

    if(openEyesScreen){
        Log.d("HomeScreen openEyes", "true")
        navController.navigate(AppScreens.EyesScreen.route)
        mqttViewModel.closeHomescreen()
    }

    if (showDialog) {
        NavigationDialog(
            onDismiss = { splashScreenViewModel.hideNavigationDialog() },
            onStopNavigation = { /*robotMan.getRobotInterfaceMethod().stopNavigation(1)*/ /*splashScreenViewModel.stopNavigation()*/ },
            onReturnToPreviousOrReception = { /*splashScreenViewModel.navigateToLastDestiny()*/ },
            onEmergencyCall = { Log.d("EMERGENCY", "PLEASE HELP") },
            splashScreenViewModel = splashScreenViewModel
        )
    }

    if(showQuestionsDialog) {
        Log.d("QUESTIONS", "ENTRA AL DIALOGO")
        QuestionsDialog(
            title = "Question",
            onAccept = {},
            onDismiss = {},
            mqttViewModel = mqttViewModel
        )
    }

    if(showWelcomeDialog) {
        Log.d("QUESTIONS", "ENTRA AL DIALOGO")
        WelcomeDialog(
            onDismiss = {
                //navController.navigate(AppScreens.DrivingFaceScreen.route)
                mqttViewModel.hideWelcomeDialog() },
            onStopNavigation = {  },
            onReturnToPreviousOrReception = {  },
            onEmergencyCall = {  },
            mqttViewModel = mqttViewModel
        )
    }

    FuturisticGradientBackground {

        Column(modifier = Modifier.fillMaxSize()) {
            Cabecera(navController = navController)
            Spacer(Modifier.height(10.dp))
            Botones(navController = navController)
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                FloatingActionButton(
                    onClick = {
                        mqttViewModel.robotMan.speak("Vuelvo a mi puesto. Muchas gracias", false)
                        mqttViewModel.robotMan.goCharge()
                        navController.navigate(AppScreens.EyesScreen.route) },
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
                if(adminMode){
                    LazyRowUbicaciones(mqttViewModel = mqttViewModel, modifier = Modifier.align(Alignment.BottomCenter), navController)
                }
            }
        }
    }
}

@Composable
fun Cabecera(navController: NavController){

    // Una columna con un espacio fijo entre sus elementos
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // El Row que contiene el título
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            MqttButton(navController = navController)
            SoundButton(navController)
        }
        // El Text que contiene el subtítulo
        Text(text = "¿Cuál es el motivo de su visita?", modifier = Modifier.fillMaxWidth(), color = Color.White,fontSize = 20.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
    }
}

@Composable
fun LazyRowUbicaciones(mqttViewModel: MqttViewModel, modifier : Modifier = Modifier, navController: NavController){

    /*var destinations : List<String> = mutableListOf()
    destinations = splashScreenViewModel.destinationsList.value!!

    LaunchedEffect(Unit){
        splashScreenViewModel.getPlaceList()
    }*/

    mqttViewModel.getListPoses()
    val destinations : List<Pose> by mqttViewModel.posesList.collectAsState()

    LazyRow(modifier = modifier.then(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(start = 40.dp)),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Los botones que se crean a partir de la lista del viewModel
        items(items = destinations) { item ->
            Log.d("DESTINATIONS", item.name)
            // Un botón con el onClick y el estilo que quieras
            Box(
                modifier = Modifier

                    .clickable {
                        /*splashScreenViewModel.navigateToDestiny(item)*/
                        mqttViewModel.robotMan.startNavigation(0, item.name, 0.1, 1000000)
                        navController.navigate(AppScreens.EyesScreen.route)
                    }
                    .padding(horizontal = 15.dp, vertical = 12.dp)

                    .background(
                        color = md_theme_light_tertiary,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center, // Centra el contenido del Box

            ) {
                Text(
                    text = item.name,
                    maxLines = 1, // Asegúrate de que el texto no exceda una línea
                    overflow = TextOverflow.Ellipsis, // Usa "..." si el texto es demasiado largo
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun Botones(navController: NavController) {

    val rutas = listOf(
        AppScreens.UnknownVisitScreen.route,
        AppScreens.NumericPanelScreen.route,
        AppScreens.TourScreen.route
    )
    // Una lista de iconos para los botones
    val iconos = listOf(Icons.Default.Person, Icons.Default.Place, Icons.Default.MailOutline)
    // Una lista de textos para los botones
    val textos = listOf("VISITA", "REUNIÓN", "MENSAJERÍA")
    val indicaciones = listOf("\"tengo una visita..\"", "\"tengo una reunión..\"", "\"soy de mensajería\"")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Ajusta la altura al contenido,
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ){

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            // Alineación y espaciado adicionales si son necesarios
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center) {
            // Tre botones con los colores, iconos y textos correspondientes
            items(3) { index ->

                TransparentButtonWithIconAndText(
                    text = textos[index],
                    indicacion = indicaciones[index],
                    icon = iconos[index], // Reemplaza con tu icono
                    onClick = {
                        // Acción al hacer clic en el botón
                        navController.navigate(rutas[index])
                    }
                )

            // Un botón con el color de fondo, el canto redondeado y el onClick que quieras
                /*Button(
                    modifier = Modifier
                        .height(100.dp)

                        .fillMaxWidth()
                        .padding(9.dp),
                    onClick = { navController.navigate(rutas[index]) },
                    colors = ButtonDefaults.buttonColors(contentColor = ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    // Un Row con el icono y el texto del botón
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = iconos[index], contentDescription = textos[index], tint= md_theme_light_onPrimary, modifier = Modifier.size(65.dp))
                        Spacer(modifier = Modifier.width(30.dp))
                        Text(textos[index], color = md_theme_light_onPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }*/
            }
        }
    }
}

@Composable
fun MqttButton(navController: NavController){
    Box(contentAlignment = Alignment.TopStart){
        Image(
            painter = painterResource(id = R.drawable.intecrobots_circulo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(5.dp)
                .size(dimensionResource(id = R.dimen.mqtt_button_size))
                .clickable { navController.navigate(AppScreens.AdminPanelScreen.route) })
    }
}

@Composable
fun SoundButton(navController: NavController) {
    // Una variable que guarda el estado del sonido (true = activado, false = desactivado)
    var soundOn by remember { mutableStateOf(true) }
    // El icono que se muestra según el estado del sonido
    val soundIcon : Painter = painterResource(id = if (soundOn) R.drawable.volume_on else R.drawable.volume_off)
    // La vista actual para reproducir el sonido
    val view = LocalView.current
    // El botón que cambia el estado del sonido y reproduce el sonido al pulsarlo

    Box(
        contentAlignment = Alignment.TopEnd){
        IconButton(
            onClick = {

                // Cambiar el estado del sonido
                soundOn = !soundOn
                // Reproducir el sonido de click si el sonido está activado
                if (soundOn) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                }
            },
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.mqtt_button_size))
                // Alinear el botón a la derecha
                .align(Alignment.TopEnd)
                // Añadir un padding de 16 dp
                .padding(5.dp)
        ) {
            // Mostrar el icono del sonido con el color correspondiente
            Icon(painter = soundIcon, contentDescription = "Sound", tint = Color.White)
        }
    }
}
