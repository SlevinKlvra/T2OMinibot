package com.intec.telemedicina.screens

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.intec.telemedicina.robotinterface.RobotManager
import com.intec.telemedicina.viewmodels.MqttViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun EyesScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    robotManager: RobotManager
) {
    Log.d("Current Screen", "Eyes Screen")

    val faceType by mqttViewModel.faceType.collectAsState()
    val interactionState by mqttViewModel.interactionState.collectAsState()
    val isDriving by mqttViewModel.isDriving.collectAsState()
    val isPaused by mqttViewModel.isPaused.collectAsState()
    val isFinished by mqttViewModel.isFinished.collectAsState()
    val question by mqttViewModel.question.collectAsState()
    val notUnderstood by mqttViewModel.notUnderstood.collectAsState()

    if (!mqttViewModel.getInitiatedStatus()) {
        Log.d("InitiatedStatus Screen", "getInitiatedStatus is false")
        mqttViewModel.connect()
        Log.d("Topicslist", mqttViewModel.resumeTopics().toString())
    }

    if (isDriving) {
        Log.d("isDriving EyesScreen", "stopFocus and unregister")
        robotManager.stopFocusFollow()
        robotManager.unregisterPersonListener()
    }

    if (isFinished or isPaused) {
        Log.d("isDriving EyesScreen", "register and startFocus")
        robotManager.registerPersonListener()
        robotManager.startFocusFollow(0)
    }

    Box(
        Modifier
            .clickable {
                if (isDriving) {
                    mqttViewModel.setPaused(true)
                    mqttViewModel.navigateToDrivingScreen()

                } else {
                    mqttViewModel.navigateToHomeScreen()
                    robotManager.registerPersonListener()
                    robotManager.startFocusFollow(0)
                }
            }
            .background(Color.Black)
            .fillMaxSize()
    ) {
        ImageExample(faceType, interactionState, question, notUnderstood)
    }
}

@Composable
fun ImageExample(
    faceType: Face,
    interactionState: InteractionState,
    question: String,
    notUnderstood: Boolean
) {
    Log.d("Understood", "$notUnderstood")
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

    FuturisticGradientBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (faceType) {
                Face.NEUTRAL -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.white_neutral,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(380.dp)
                            .padding(top = 24.dp)
                            .align(Alignment.Center)
                    )
                }

                Face.HAPPY -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_happy,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Face.BORED -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_bored,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Face.MAD -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_mad,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Face.SAD -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_sad,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Face.LOVE -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_love,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Overlay smaller image at the bottom center
            when (interactionState) {
                InteractionState.NONE -> {
                    // Nothing to show for now
                }

                InteractionState.THINKING -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.dots,
                            imageInteractionLoader
                        ),
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
                        painter = rememberAsyncImagePainter(
                            R.drawable.microphone,
                            imageInteractionLoader
                        ),
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
                        painter = rememberAsyncImagePainter(
                            R.drawable.speaking,
                            imageInteractionLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 1.dp) // Adjust the padding as needed
                            .width(70.dp)
                            .height(70.dp)
                    )
                }
            }

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
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(16.dp) // Adjust padding as needed
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}