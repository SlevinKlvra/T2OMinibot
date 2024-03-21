package com.intec.t2o.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.t2o.components.GoBackButton
import com.intec.t2o.components.LoadingSpinner
import com.intec.t2o.components.novisit.CompanyStep
import com.intec.t2o.components.novisit.DataStep
import com.intec.t2o.components.novisit.EmailStep
import com.intec.t2o.components.novisit.LastStep
import com.intec.t2o.components.novisit.MessageStep
import com.intec.t2o.components.novisit.NameStep
import com.intec.t2o.components.novisit.SendExistingUserInfoStep
import com.intec.t2o.components.novisit.UserExistenceSelection
import com.intec.t2o.components.novisit.UserExistsStep
import com.intec.t2o.components.novisit.UserTypeSelection
import com.intec.t2o.robotinterface.RobotManager
import com.intec.t2o.ui.theme.cardColor
import com.intec.t2o.ui.theme.iconColor
import com.intec.t2o.viewmodels.MqttViewModel
import com.intec.t2o.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.intec.t2o.ui.theme.textColor

enum class UserType {
    CLIENTE, PROVEEDOR
}

enum class UserExistence {
    NUEVO, EXISTENTE
}

data class UserData(
    val tipo: UserType?,
    val userExistence: UserExistence?,
    val nombre: String,
    val empresa: String,
    val email: String,
    val asunto: String
)

@Composable
fun UnknownVisitScreen(
    navController: NavController,
    mqttViewModel: MqttViewModel,
    numericPanelViewModel: NumericPanelViewModel
) {
    Log.d("Current Screen", "UnknownVisitScreen")

    val speechText by mqttViewModel.speechText.collectAsState()

    var currentTitle by remember { mutableStateOf("") }
    var currentSubtitle by remember { mutableStateOf("") }

    var userData by remember {
        mutableStateOf(
            UserData(
                tipo = null,
                userExistence = null,
                nombre = "",
                empresa = "",
                email = "",
                asunto = ""
            )
        )
    }

    val context = LocalContext.current
    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 8

    var isSendingData by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        currentPage = 1
        mqttViewModel.speak(
            "Por favor, rellene los campos para concertar una visita",
            false){
            //Acciones a realizar después de hablar
            Log.d("Unknown Speak Finished", "Por favor, rellene los campos para concertar una visita")
        }
    }

    LaunchedEffect(isSendingData) {
        if (isSendingData) {
            try {
                val success = withContext(Dispatchers.IO) {
                    numericPanelViewModel.postUnknownVisitor(userData)
                }
                if (success == true) {
                    Log.d("peticion hecha", "peticion")
                    currentPage++
                } else {
                    Log.d("peticion no hecha", "no peticion")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error al enviar la solicitud: $e")
            } finally {
                isSendingData = false
            }
        }
    }

    FuturisticGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            if (!(currentPage == 5 && userData.userExistence == UserExistence.EXISTENTE)) {
                if (currentPage != totalPages) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentPage != totalPages) {
                            GoBackButton(onClick = {
                                if (currentPage > 1) {
                                    currentPage--
                                } else {
                                    mqttViewModel.navigateToHomeScreen()
                                }
                            })
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                if (userData.userExistence == UserExistence.NUEVO && currentPage > 2) {
                                    Text(
                                        text = "${currentPage - 2} de ${totalPages - 3}",
                                        color = Color.White,
                                        modifier = Modifier.widthIn(min = 33.dp)
                                    )
                                }
                                Text(
                                    text = currentTitle,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = currentSubtitle,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                if (currentPage != totalPages - 1) {
                                    currentPage++
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .padding(16.dp),
                            containerColor = if (currentPage != totalPages - 1 && !(currentPage == 5 && userData.userExistence == UserExistence.EXISTENTE)) cardColor else Color.Gray,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = if (currentPage != totalPages - 1 && !(currentPage == 5 && userData.userExistence == UserExistence.EXISTENTE)) iconColor else Color.LightGray
                            )
                        }
                    }
                }
            }

            when (currentPage) {
                1 ->
                {
                    currentTitle = "Cliente o proveedor"
                    currentSubtitle = "Diga o indique si es cliente o proveedor"
                    mqttViewModel.speak("Diga o indique si es cliente o proveedor", true)
                    {
                        //Acciones a realizar después de hablar
                        Log.d("Unknown Speak Finished", "Diga o indique si es cliente o proveedor")
                    }
                    UserTypeSelection(
                        mqttViewModel,
                        onUserTypeSelected = {
                        userData = userData.copy(tipo = it)
                        currentPage++
                    })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(Color.Transparent)
                            .padding(10.dp)
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
                }

                2 ->
                {
                    currentTitle = "Usuario nuevo o existente"
                    currentSubtitle = "Seleccione si es usuario nuevo o usuario existente"
                    mqttViewModel.speak("¿Es usted un cliente nuevo o ya nos conocemos?", true
                    )
                    {
                        //Acciones a realizar después de hablar
                        Log.d("Unknown Speak Finished", "Diga o indique si es cliente o proveedor")
                    }
                    UserExistenceSelection(onUserExistenceSelected = {
                    userData = userData.copy(userExistence = it)
                    currentPage++
                })}

                3 ->
                {
                    if (userData.userExistence == UserExistence.EXISTENTE) {
                        currentTitle = "Avisar a"
                        currentSubtitle = "Indique a quién quiere notificar"
                        mqttViewModel.speak("¿A quién quiere notificar?", true)
                        {
                            //Acciones a realizar después de hablar
                            Log.d("Unknown Speak Finished", "Diga o indique si es cliente o proveedor")
                        }
                        UserExistsStep(
                            numericPanelViewModel = numericPanelViewModel
                        )
                    } else {
                        currentTitle = "Nombre"
                        currentSubtitle = "Diga o indique su nombre"
                        mqttViewModel.speak("¿Cuál es su nombre?", true)
                        {
                            //Acciones a realizar después de hablar
                            Log.d("Unknown Speak Finished", "Cual es su nombre?")

                        }
                        if(speechText.isNotEmpty()) {
                            Log.d("HomeScreen STT", "Speech Text not empty: $speechText")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .background(Color.Transparent)
                                    .padding(10.dp)
                            ) {
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
                        }
                        NameStep(
                            name = userData.nombre,
                            onNameChange = { userData = userData.copy(nombre = it) },
                        )
                    }
                }

                4 -> {
                    if (userData.userExistence == UserExistence.EXISTENTE) {
                        SendExistingUserInfoStep()
                    } else {
                        currentTitle = "Empresa"
                        currentSubtitle = "Diga o indique su empresa o la empresa a la que representa"
                        CompanyStep(
                            company = userData.empresa,
                            onCompanyChange = { userData = userData.copy(empresa = it) },
                        )
                    }
                }

                5 -> {
                    if (userData.userExistence == UserExistence.EXISTENTE) {

                        mqttViewModel.speak(
                            "Proceso completado. En breves se pondrán en contacto con usted para concertar una visita. Muchas gracias.",
                            false
                        ){
                            //Acciones a realizar después de hablar
                            Log.d("Unknown Speak Finished", "Proceso completado. En breves se pondrán en contacto con usted para concertar una visita. Muchas gracias.")
                            mqttViewModel.setReturningHome(true)
                            mqttViewModel.returnToPosition(mqttViewModel.returnDestination.value!!)
                        }
                        LastStep(mqttViewModel)
                    } else {
                        currentTitle = "Correo electrónico"
                        currentSubtitle = "Diga o indique el email por el que quiere ser contactado"
                        EmailStep(
                            email = userData.email,
                            onEmailChange = { userData = userData.copy(email = it) })
                    }
                }

                6 -> {
                    currentTitle = "Asunto"
                    currentSubtitle = "Diga o indique el motivo de su visita"
                    MessageStep(
                        message = userData.asunto,
                        onMessageChange = { userData = userData.copy(asunto = it) }
                    )
                }

                7 -> {
                    if (isLoading) LoadingSpinner()
                    else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) {
                            currentTitle = "¿Son estos datos correctos?"
                            currentSubtitle = "Pulse en \"Enviar\" para concertar una cita con los datos mostrados"
                            mqttViewModel.speak(
                                "¿Son estos datos correctos? Diga sí o pulse en enviar para concertar una cita con los datos mostrados.",
                                false
                            ){

                            }

                            DataStep(userData)
                            Button(
                                onClick = {
                                    if (validateFields(context, userData)) {
                                        isSendingData = true
                                    }
                                },
                                enabled = !isSendingData,
                                modifier = Modifier
                                    .widthIn(min = 33.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text("Enviar")
                            }
                        }
                    }
                }

                8 -> {
                    mqttViewModel.speak(
                        "Proceso completado. En breves se pondrán en contacto con usted para concertar una visita. Muchas gracias.",
                        false
                    )
                    {
                        Log.d("Unknown Speak Finished", "Proceso completado. En breves se pondrán en contacto con usted para concertar una visita. Muchas gracias.")
                    }
                    currentTitle = "Muchas gracias por su visita"
                    currentSubtitle = "Su información ha sido enviada. Nos pondremos en contacto con usted lo antes posible."
                    LastStep(mqttViewModel)
                }
            }
        }
    }
}


fun validateFields(context: Context, userData: UserData): Boolean {
    // Check if all fields are filled
    if (userData.tipo == null || userData.userExistence == null ||
        userData.nombre.isBlank() || userData.empresa.isBlank() ||
        userData.email.isBlank() || userData.asunto.isBlank()
    ) {
        showToast(context, "Por favor, rellene todos los campos")
        return false
    }

    // Check if the email address is valid
    val emailRegex = Regex("^[A-Za-z](.*)(@)(.+)(\\.)(.+)")
    if (!userData.email.matches(emailRegex)) {
        showToast(context, "Ingrese una dirección de correo electrónico válida")
        return false
    }

    return true
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
