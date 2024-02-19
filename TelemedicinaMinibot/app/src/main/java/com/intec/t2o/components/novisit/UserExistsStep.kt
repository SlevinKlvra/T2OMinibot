package com.intec.t2o.components.novisit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.intec.t2o.ui.theme.iconColor
import com.intec.t2o.ui.theme.textColor
import com.intec.t2o.viewmodels.NumericPanelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserExistsStep(
    numericPanelViewModel: NumericPanelViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }

    var selectedOption by remember { mutableStateOf("") }

    var isSendingData by remember { mutableStateOf(false) }

    var textFieldValue by remember { mutableStateOf("") }

    var employeesList by remember { mutableStateOf(listOf<NumericPanelViewModel.Employee>()) }

    val isLoading by numericPanelViewModel.isLoading.collectAsState()

    LaunchedEffect(isSendingData) {
        if (isSendingData) {
            try {
                employeesList = withContext(Dispatchers.IO) {
                    numericPanelViewModel.callGetEmployees(nombre = textFieldValue) ?: listOf()
                }
                if (employeesList.isNotEmpty()) {
                    Log.d("peticion hecha", employeesList.toString())
                } else {
                    Log.d("peticion no hecha", "No se obtuvieron datos")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error al enviar la solicitud: $e")
            } finally {
                isSendingData = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿A quién quiere notificar?",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(15.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Búsqueda por nombre y apellidos: ",
                color = textColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                },
                label = { Text("Nombre...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    textColor = Color.Black,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { isSendingData = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar",
                            tint = iconColor
                        )
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Elija una opción",
            color = textColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Box {
                Text(
                    text = selectedOption.ifEmpty { "Seleccione una opción" },
                    color = Color.Black,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .background(Color.White)
                        .padding(12.dp)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    employeesList.forEach { employee ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${employee.nombre} ${employee.apellidos}",
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                selectedOption = "${employee.nombre} ${employee.apellidos}"
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

