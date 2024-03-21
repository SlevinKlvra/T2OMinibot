package com.intec.t2o.components.novisit

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.t2o.R
import com.intec.t2o.components.ButtonCard
import com.intec.t2o.screens.UserType
import com.intec.t2o.ui.theme.textColor
import com.intec.t2o.viewmodels.MqttViewModel

@Composable
fun UserTypeSelection(mqttViewModel: MqttViewModel, onUserTypeSelected: (UserType) -> Unit) {


    val speechText by mqttViewModel.speechText.collectAsState()

    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    // Efecto para reaccionar a los cambios en speechText
    LaunchedEffect(speechText) {
        when (speechText.lowercase()) {
            "cliente" -> {
                onUserTypeSelected(UserType.CLIENTE)
                mqttViewModel.clearRecognizedText()
            }
            "proveedor" -> {
                onUserTypeSelected(UserType.PROVEEDOR)
                mqttViewModel.clearRecognizedText()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        /*Text(
            text = "Cliente o proveedor",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Seleccione si es cliente o proveedor",
            color = textColor,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )*/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 35.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ButtonCard(text = "Cliente",
                icon = Icons.Outlined.ShoppingCart,
                onClick = { onUserTypeSelected(UserType.CLIENTE) })
            ButtonCard(text = "Proveedor",
                icon = ImageVector.vectorResource(R.drawable.shipping),
                onClick = { onUserTypeSelected(UserType.PROVEEDOR) })
        }
        if(speechText.isNotEmpty()){
            Log.d("HomeScreen STT", "Speech Text not empty: $speechText")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Transparent)
                    .padding(30.dp)
            ){
                Text(
                    text = speechText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center) // Asegura que el Text esté centrado dentro del Box.
                        .fillMaxWidth() // Hace que el Text ocupe todo el ancho disponible.
                )
            }
        }else{
            Log.d("HomeScreen STT", "Speech Text empty")
            Image(
                painter = rememberAsyncImagePainter(
                    R.drawable.speechrecognition,
                    imageEmotionsLoader
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(35.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(30.dp)
            )
        }
    }
}