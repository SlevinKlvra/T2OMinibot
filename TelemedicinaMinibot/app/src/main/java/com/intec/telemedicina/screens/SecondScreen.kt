package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(navController: NavController, text:String?) {
    Scaffold(topBar = {
        MediumTopAppBar({
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = "Flecha",
                modifier = Modifier.clickable {
                    navController.popBackStack()
                })
            Spacer(modifier = Modifier.padding(12.dp))
        })
    }) {
        SecondBodyContent(navController, text)
    }
}


/**/

@Composable
fun SecondBodyContent(navController: NavController, text: String?){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Volver a first screen")
        text?.let {
            Text(it)
        }
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(text = "Navega Capo")
        }


    }
}
