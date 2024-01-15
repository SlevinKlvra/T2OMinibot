package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.telemedicina.AppBottomBar
import com.intec.telemedicina.R
import com.intec.telemedicina.ui.theme.md_theme_light_tertiary
import com.intec.telemedicina.viewmodels.SplashScreenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeControlScreen(navController: NavController){
    val viewModel : SplashScreenViewModel

    val backgroundImage: Painter = painterResource(id = R.drawable.background)

    Scaffold(
        bottomBar = { AppBottomBar(navController) }
    ) {
        BodyContent(navController)
    }

}

@Composable
fun BodyContent(navController: NavController){

    val list = listOf("A", "B", "C", "D") + ((0..100).map { it.toString() })

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(100.dp)
    ){
        items(items = list, itemContent = { item ->
            Log.d("COMPosE", "Rendered $item")
            when(item){
                "A" -> { Text(text = item, style = TextStyle(fontSize = 80.sp))
                }
                "B" -> {
                    Button(onClick = {}) {
                        Text(text = item, style = TextStyle(fontSize = 80.sp))
                    }
                }
            }
        })
    }
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

/*
@Composable
fun BodyContent(navController: NavController){

}

@Preview
@Composable
fun BodyContentPreview() {

    val fruits = listOf("Apple", "Mango", "Banana", "Orange", "Watermelon", "Papaya")

    // Se define un estilo personalizado para los botones
    val buttonStyle = ButtonDefaults.buttonColors(
        containerColor = Color.Blue, // El color de fondo del botón
        contentColor = Color.White // El color del texto del botón
    )

    // Se define un modificador para aplicar un margen a los botones
    val buttonModifier = Modifier.padding(8.dp)

    Column {
        Image(
            painter = painterResource(id = R.drawable.location),
            contentDescription = "Link",
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.button_size))
                .fillMaxWidth()
                .clickable {}
                .background((Color(0xFF0676AA)))
        )
        Spacer(modifier = Modifier.padding(12.dp))
    }
}


@Composable
fun BodyContent(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF40BCF5)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(modifier = Modifier.clip(shape = MaterialTheme.shapes.medium)) {

            //nav 1
            Image(painter = painterResource(id = R.drawable.location),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.NavigationScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
            Spacer(modifier = Modifier.padding(12.dp))
            //nav 2
            Image(painter = painterResource(id = R.drawable.call),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.NavigationScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
            Spacer(modifier = Modifier.padding(12.dp))
            //nav3
            Image(painter = painterResource(id = R.drawable.intecrobots),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.SecondScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
        }
        Spacer(modifier = Modifier.padding(12.dp))
        Row(modifier = Modifier.clip(shape = MaterialTheme.shapes.extraLarge)) {
            //nav4
            Image(painter = painterResource(id = R.drawable.intecrobots),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.SecondScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
            Spacer(modifier = Modifier.padding(12.dp))
            //nav5
            Image(painter = painterResource(id = R.drawable.intecrobots),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.SecondScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
            Spacer(modifier = Modifier.padding(12.dp))
            //nav6
            Image(painter = painterResource(id = R.drawable.intecrobots),
                contentDescription = "Link",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.button_size))
                    .fillMaxWidth()
                    .clickable { navController.navigate(route = AppScreens.SecondScreen.route) }
                    .background((Color(0xFF0676AA)))
            )
        }
    }
}

*/