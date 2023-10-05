@file:OptIn(ExperimentalComposeUiApi::class)

package com.intec.telemedicina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.intec.telemedicina.viewmodels.MqttViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.intec.telemedicina.components.BackButton
import com.intec.telemedicina.di.MqttViewModelFactory
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MqttScreen(navController: NavController, mqttViewModel: MqttViewModelFactory) {

    val viewModel: MqttViewModel = viewModel(factory = mqttViewModel)
    
    val connectionState by viewModel.connectionState
    val messages by viewModel.incomingMessages

    val shouldHideKeyboard by viewModel.shouldHideKeyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(shouldHideKeyboard){
        if (shouldHideKeyboard) {
            keyboardController?.hide()
            viewModel.resetHideKeyboardTrigger()
        }
    }

    var topicToSubscribe by remember { mutableStateOf("") }
    var topicToPublish by remember { mutableStateOf("") }
    var messageToPublish by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BackButton(navController = navController)
            Text(text = "MQTT Connection Status: $connectionState")
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    viewModel.connect()
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Connect")
                }
                Button(onClick = {
                    viewModel.disconnect()
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Disconnect")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = topicToSubscribe,
                onValueChange = { topicToSubscribe = it },
                label = { Text("Topic to Subscribe") },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                Button(onClick = {
                    viewModel.subscribeToTopic(topicToSubscribe)
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Subscribe")
                }
                Button(onClick = {
                    val topics : MutableList<String> = viewModel.resumeTopics()  // Asume que resumeTopics está en el ViewModel.
                    viewModel.subscribeToAllTopics(topics)  // Asume que subscribeToAllTopics está en el ViewModel.
                    viewModel.triggerHideKeyboard()
                }) {
                    Text("Suscripción rápida")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = topicToPublish,
                onValueChange = { topicToPublish = it },
                label = { Text("Topic to Publish") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = messageToPublish,
                onValueChange = { messageToPublish = it },
                label = { Text("Message to Publish") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                viewModel.publishMessage(topicToPublish, messageToPublish)
                viewModel.triggerHideKeyboard()
            }) {
                Text("Publish Message")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(messages) { message ->
            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
        }
    }
}
