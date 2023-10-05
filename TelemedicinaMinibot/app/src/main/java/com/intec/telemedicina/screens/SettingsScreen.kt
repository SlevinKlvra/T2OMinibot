package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, text:String?) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar({
            Modifier.background(Color.Blue)
            Text(text = "Ajustes")
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = "Flecha",
                modifier = Modifier

                    .clickable {navController.popBackStack()}
            )
            Spacer(modifier = Modifier.padding(12.dp))
        })
    }) {
        SettingsBodyContent(navController, text)
    }
}

@Composable
fun SettingsBodyContent(navController: NavController, text: String?){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings Screen")
        text?.let {
            Text(it)
        }
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(text = "Volver")
        }
    }
}