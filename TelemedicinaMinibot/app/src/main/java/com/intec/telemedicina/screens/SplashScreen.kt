package com.intec.telemedicina.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.intec.telemedicina.navigation.AppScreens
import kotlinx.coroutines.delay
import com.intec.telemedicina.viewmodels.SplashScreenViewModel
import com.intec.telemedicina.R
import com.intec.telemedicina.di.SplashScreenViewModelFactory

@Composable
fun SplashScreen(navController: NavHostController, viewModelFactory: SplashScreenViewModelFactory) {
    //Instancia del viewModel
    val viewModel: SplashScreenViewModel = viewModel(factory = viewModelFactory)

    // Observa los datos del ViewModel
    //val data by viewModel.data.observeAsState()

    LaunchedEffect(key1 = true){
        delay(3000)
        navController.popBackStack()
        navController.navigate(AppScreens.HomeScreen.route)
    }
    Splash(viewModel)
}

@Composable
fun Splash (viewModel : SplashScreenViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.intecrobots_circulo),
            contentDescription ="logo",
            modifier = Modifier.size(dimensionResource(id = R.dimen.image_size)))

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Bienvenid@s",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = viewModel.currentAction,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}