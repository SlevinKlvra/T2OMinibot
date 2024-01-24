package com.intec.t2o.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intec.t2o.viewmodels.NumericPanelViewModel

@Composable
fun NumericPad(
    numericPanelViewModel: NumericPanelViewModel,
    onClick: () -> Unit,
    titleText: String
) {
    val showErrorAnimation by numericPanelViewModel.showErrorAnimation

    val shakeModifier = if (showErrorAnimation) {
        val anim = rememberInfiniteTransition().animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Modifier.graphicsLayer { translationX = anim.value }
    } else Modifier

    val textStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = Color.Black
    )

    DisposableEffect(Unit) {
        numericPanelViewModel.resetDigits()
        onDispose { }
    }

    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    if (isLoading) LoadingSpinner()
    else {
        Box(modifier = shakeModifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titleText,
                    style = textStyle.copy(fontSize = 15.sp),
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = Color.White
                )
                Text(
                    text = numericPanelViewModel.enteredCode.value,
                    style = textStyle.copy(fontSize = 12.sp),
                    modifier = Modifier
                        .padding(6.dp),
                    color = Color.White
                )

                // Teclado numérico
                val buttons =
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Borrar", "0", "Enviar")
                buttons.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { number ->
                            if (number.isNotBlank()) {
                                NumericPadButton(
                                    text = number,
                                    onClick = {
                                        when (number) {
                                            "Borrar" -> {
                                                numericPanelViewModel.removeLastDigit()
                                            }

                                            "Enviar" -> {
                                                onClick()
                                            }

                                            else -> numericPanelViewModel.addDigit(number.first())
                                        }
                                    },
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .height(30.dp)
                                        .width(100.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
