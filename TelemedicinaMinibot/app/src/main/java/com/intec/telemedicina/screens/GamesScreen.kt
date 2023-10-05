package com.intec.telemedicina.screens

import android.webkit.WebViewClient
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button

@Composable
fun GamesScreen(navController: NavController){

    Column {
        Button(onClick = {navController.popBackStack()}) {
            Text(text = "Back")
        }

        var url = "https://mowatave.github.io/icaria-demo/"
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.allowContentAccess = true
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }, update = {view ->
            view.loadUrl(url)
        })
    }
}