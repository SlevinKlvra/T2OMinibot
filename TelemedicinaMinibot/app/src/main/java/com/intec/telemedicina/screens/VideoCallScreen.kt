
import android.annotation.SuppressLint
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun VideoCallScreen(navController: NavController) {
    val url = "https://mowatave.github.io/icaria-demo/"
    //val url = "http://192.168.47.116:8123"
    // Composable function to display the WebView
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewContainer(url: String) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // Enable JavaScript
                    settings.javaScriptEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true



                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("WebViewFinished", "Page finished loading: $url")
                            visibility = View.VISIBLE
                        }

                        override fun onTooManyRedirects(
                            view: WebView?,
                            cancelMsg: Message?,
                            continueMsg: Message?
                        ) {
                            super.onTooManyRedirects(view, cancelMsg, continueMsg)
                            Log.d("WebViewError", "Too many redirects")
                        }

                        @RequiresApi(Build.VERSION_CODES.M)
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            Log.e("WebViewError", "Error loading URL: ${error?.description}")
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WebViewConsole", "${consoleMessage?.message()} at ${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()}")
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Display the WebViewContainer in your Composable
    WebViewContainer(url)
}
