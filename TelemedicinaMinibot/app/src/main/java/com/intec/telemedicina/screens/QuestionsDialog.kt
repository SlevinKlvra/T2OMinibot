package com.intec.telemedicina.screens

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun QuestionsDialog(
    title: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    mqttViewModel: MqttViewModel
) {
    AlertDialog(
        title = {
            Text(text = mqttViewModel.mqttQuestion.value.toString())
        },
        onDismissRequest = {
            onDismiss()
            Log.d("Response","LEAVE ME ALONE")
            mqttViewModel.hideQuestionsDialog()
        },
        confirmButton = {
            Button(
                onClick = {
                    onAccept()
                    Log.d("Response","YESSSSS")
                    mqttViewModel.publishMessage("robot/voice_pub/response","YES")
                    mqttViewModel.hideQuestionsDialog()
                }
            ) {
                Text(text = "Si")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                    Log.d("Response","NOOOOO")
                    mqttViewModel.publishMessage("robot/voice_pub/response","NO")
                    mqttViewModel.hideQuestionsDialog()
                }
            ) {
                Text(text = "No")
            }
        }
    )
}