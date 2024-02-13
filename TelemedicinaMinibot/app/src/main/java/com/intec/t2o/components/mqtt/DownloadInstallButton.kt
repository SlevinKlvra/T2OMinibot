package com.intec.t2o.components.mqtt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.intec.t2o.screens.REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION
import java.io.File

@Composable
fun DownloadInstallButton() {
    var isDownloading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // URL de la APK que deseas descargar
    val apkUrl = "https://testdownload.onrender.com/descargar-archivo"

    Button(onClick = {
        // Verificar permisos de almacenamiento externo
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Iniciar la descarga
            downloadAndInstall(context, apkUrl)
            isDownloading = true
        } else {
            // Solicitar permisos de almacenamiento externo
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }) {
        Text("Buscar actualizaciones")
    }

    // Mostrar ProgressBar durante la descarga
    if (isDownloading) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}


@SuppressLint("UnspecifiedRegisterReceiverFlag")
private fun downloadAndInstall(context: Context, apkUrl: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Crear una solicitud de descarga
    val request = DownloadManager.Request(Uri.parse(apkUrl))
        .setTitle("app-debug.apk")
        .setDescription("Descargando la última versión...")
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-debug.apk")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    // Enqueue la solicitud de descarga
    val downloadId = downloadManager.enqueue(request)

    // Agregar un BroadcastReceiver para controlar la finalización de la descarga
    val onComplete = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context, intent: Intent) {
            val downloadedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadedId == downloadId) {
                Log.d("Download", "Descarga completada")

                // Obtener el URI de la APK descargada
                val downloadQuery = DownloadManager.Query()
                downloadQuery.setFilterById(downloadId)
                val cursor = downloadManager.query(downloadQuery)
                if (cursor.moveToFirst()) {
                    val filePath =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    cursor.close()

                    // Instalar la APK
                    installApk(context, filePath)
                }
            }
        }
    }
    // Registrar el BroadcastReceiver
    context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}

private fun installApk(context: Context, filePath: String) {
    try {
        // Convertir el URI a un archivo
        val file = File(Uri.parse(filePath).path!!)
        Log.d("InstallApk", "File path: ${file.absolutePath}")

        // Obtener el directorio de descargas
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Construir la ruta completa del archivo APK en el directorio de descargas
        val apkFile = File(downloadsDirectory, file.name)
        Log.d("InstallApk", "APK File path: ${apkFile.absolutePath}")

        // Obtener el URI del archivo utilizando FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "com.intec.t2o.fileprovider",
            apkFile
        )
        Log.d("InstallApk", "Content URI: $contentUri")

        // Crear un intent para la instalación de la APK
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            data = contentUri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }


        // Iniciar la actividad de instalación
        Log.d("InstallApk", "Starting installation activity...")
        context.startActivity(installIntent)

        Log.d("InstallApk", "Intento de instalación exitoso")

    } catch (e: Exception) {
        // Capturar excepciones y registrarlas
        Log.e("InstallApk", "Error al intentar instalar la APK", e)
    }
}