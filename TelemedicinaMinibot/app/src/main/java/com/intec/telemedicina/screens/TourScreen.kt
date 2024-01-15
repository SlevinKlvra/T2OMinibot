package com.intec.telemedicina.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.telemedicina.ui.theme.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun TourScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel){
    Button(onClick = {/*splashScreenViewModel.startTour()*/},
        content = {})
    Text("Third Screen")
    Box{
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(36.dp).align(Alignment.BottomStart),
            containerColor = md_theme_light_tertiary
        ) { // You can set the house icon here using painterResource
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )
        }
    }
}