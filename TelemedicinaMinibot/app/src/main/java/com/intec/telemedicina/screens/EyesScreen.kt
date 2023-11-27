package com.intec.telemedicina.screens

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.telemedicina.R
import com.intec.telemedicina.data.Face
import com.intec.telemedicina.data.InteractionState
import com.intec.telemedicina.navigation.AppScreens
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun EyesScreen(navController: NavController, mqttViewModel: MqttViewModel){
    val mqttViewModel: MqttViewModel = mqttViewModel

    val faceType by mqttViewModel.faceType.collectAsState()
    val interactionState by mqttViewModel.interactionState.collectAsState()
    val isDriving by mqttViewModel.isDriving.collectAsState()
    val isPaused by mqttViewModel.isPaused.collectAsState()
    val question by mqttViewModel.question.collectAsState()

    if(!mqttViewModel.getInitiatedStatus()){
        mqttViewModel.connect()
        Log.d("Topicslist",mqttViewModel.resumeTopics().toString())
        //mqttViewModel.subscribeToAllTopics(mqttViewModel.resumeTopics())
    }

    Box(
        Modifier
            .clickable {

                if(isDriving){
                    mqttViewModel.setPaused(true)
                    navController.navigate(AppScreens.DrivingScreen.route)
                }
                else{
                    navController.navigate(AppScreens.HomeScreen.route)
                }}
            .background(Color.Black)
            .fillMaxSize()){
        ImageExample(faceType, interactionState, question)
    }



}

@Composable
fun ImageExample(faceType : Face, interactionState: InteractionState, question : String) {
    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val imageInteractionLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background big image
        when(faceType){
            Face.NEUTRAL -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.neutral,imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Face.HAPPY -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.happy, imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Face.BORED -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.bored, imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Face.MAD -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.mad, imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Face.SAD -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.sad, imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Face.LOVE -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.love, imageEmotionsLoader),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Overlay smaller image at the bottom center
        when(interactionState){
            InteractionState.NONE -> {
                // Nothing to show for now
            }
            InteractionState.THINKING -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.dots,imageInteractionLoader),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp) // Adjust the padding as needed
                        .width(70.dp)
                        .height(70.dp)
                )
            }
            InteractionState.LISTENING -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.microphone,imageInteractionLoader),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp) // Adjust the padding as needed
                        .width(70.dp)
                        .height(70.dp)
                )
            }
            InteractionState.SPEAKING -> {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.speaking,imageInteractionLoader),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp) // Adjust the padding as needed
                        .width(70.dp)
                        .height(70.dp)
                )
            }
        }



        // Text field at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
        ) {
            Text(
                text = question,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                //fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(16.dp) // Adjust padding as needed
                    .align(Alignment.TopCenter)
            )
        }
    }


}