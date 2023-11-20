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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.R
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.ui.color.md_theme_light_onPrimary
import com.intec.telemedicina.ui.color.md_theme_light_primary
import com.intec.telemedicina.ui.color.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.MqttViewModel
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun HomeScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel, mqttViewModel: MqttViewModel, robotManager : RobotManager){

    val splashScreenViewModel: SplashScreenViewModel = splashScreenViewModel
    val mqttViewModel: MqttViewModel = mqttViewModel

    val showDialog by splashScreenViewModel.showNavigationDialog.collectAsState()
    val showQuestionsDialog by mqttViewModel.showQuestionsDialog.collectAsState()
    val showWelcomeDialog by mqttViewModel.showWelcomeDialog.collectAsState()

    if (showDialog) {
        NavigationDialog(
            onDismiss = { splashScreenViewModel.hideNavigationDialog() },
            onStopNavigation = { /*splashScreenViewModel.stopNavigation()*/ },
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
            onDismiss = { mqttViewModel.hideWelcomeDialog() },
            onStopNavigation = {  },
            onReturnToPreviousOrReception = {  },
            onEmergencyCall = {  },
            mqttViewModel = mqttViewModel
        )
    }

    FuturisticGradientBackground {

        Column(modifier = Modifier.fillMaxSize()) {
            Cabecera(navController = navController)
            Botones(navController = navController)
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                LazyRowUbicaciones(splashScreenViewModel = splashScreenViewModel, modifier = Modifier.align(Alignment.BottomCenter))
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
            Text(text = "MENÚ PRINCIPAL", modifier = Modifier.padding(3.dp),color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 30.sp)
            SoundButton()
        }
        // El Text que contiene el subtítulo
        Text(text = "Seleccione una opción", modifier = Modifier.fillMaxWidth(), color = Color.Black,fontSize = 15.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
    }
}

@Composable
fun LazyRowUbicaciones(splashScreenViewModel: SplashScreenViewModel, modifier : Modifier = Modifier){
    Log.d("DESTINATIONS", "ENTRA A LA FUNCIÓN")
    Log.d("DESTINATIONS LIST", splashScreenViewModel.destinationsList.value.toString())

    var destinations : List<String> = mutableListOf()
    destinations = splashScreenViewModel.destinationsList.value!!

    LaunchedEffect(Unit){
        /*splashScreenViewModel.getPlaceList()*/
    }

    LazyRow(modifier = modifier.then(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(top = 4.dp)),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Los botones que se crean a partir de la lista del viewModel
        items(items = destinations) { item ->
            Log.d("DESTINATIONS", item)
            // Un botón con el onClick y el estilo que quieras
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 100.dp) // Establece un tamaño fijo para el botón
                    .clickable {
                        /*splashScreenViewModel.navigateToDestiny(item)*/
                        splashScreenViewModel.showNavigationDialog()
                    }
                    .padding(end = 15.dp, bottom = 12.dp, top = 5.dp)

                    .background(
                        color = md_theme_light_tertiary,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center // Centra el contenido del Box
            ) {
                Text(
                    text = item,
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
        AppScreens.GamesScreen.route,
        AppScreens.VideoCallScreen.route,
        AppScreens.TourScreen.route,
        AppScreens.HomeControlScreen.route,
        AppScreens.IcariaScreen.route,
        AppScreens.TestScreen.route
    )
    // Una lista de iconos para los botones
    val iconos = listOf(Icons.Default.PlayArrow, Icons.Default.Call, Icons.Default.Refresh, Icons.Default.Home, Icons.Default.Person, Icons.Default.Settings)
    // Una lista de textos para los botones
    val textos = listOf("JUEGOS", "VIDEOLLAMADA", "TOUR", "CONTROL DE CASA", "ICARIA", "TEST")
    // Un LazyVerticalGrid con tres columnas

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){

        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            // Seis botones con los colores, iconos y textos correspondientes
            items(6) { index ->
                // Un botón con el color de fondo, el canto redondeado y el onClick que quieras
                Button(
                    modifier = Modifier
                        .height(100.dp)

                        .fillMaxWidth()
                        .padding(9.dp),
                    onClick = { navController.navigate(rutas[index]) },
                    colors = ButtonDefaults.buttonColors(contentColor = md_theme_light_primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    // Un Row con el icono y el texto del botón
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = iconos[index], contentDescription = textos[index], tint= md_theme_light_onPrimary, modifier = Modifier.size(45.dp))
                        Spacer(modifier = Modifier.width(30.dp))
                        Text(textos[index], color = md_theme_light_onPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
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
                .clickable { navController.navigate(AppScreens.MqttScreen.route) })
    }
}

@Composable
fun SoundButton() {
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
            Icon(painter = soundIcon, contentDescription = "Sound", tint = Color.Black)
        }
    }
}
