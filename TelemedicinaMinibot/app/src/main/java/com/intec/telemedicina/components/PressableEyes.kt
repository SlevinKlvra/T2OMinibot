package com.intec.telemedicina.components

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.telemedicina.R
import com.intec.telemedicina.screens.FuturisticGradientBackground

@Composable
fun PressableEyes(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        ImageExample()
    }
}

@Composable
fun ImageExample() {
    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
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
            // Background big image
            Image(
                painter = rememberAsyncImagePainter(
                    R.drawable.white_neutral,
                    imageEmotionsLoader
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

