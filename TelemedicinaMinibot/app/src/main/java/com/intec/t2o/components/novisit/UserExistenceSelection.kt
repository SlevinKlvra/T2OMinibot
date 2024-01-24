package com.intec.t2o.components.novisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.intec.t2o.components.TransparentButtonWithIcon
import com.intec.t2o.screens.UserExistence

@Composable
fun UserExistenceSelection(onUserExistenceSelected: (UserExistence) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Usuario nuevo o existente",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransparentButtonWithIcon(text = "Nuevo usuario",
                icon = Icons.Outlined.AddCircle,
                onClick = { onUserExistenceSelected(UserExistence.NUEVO) })
            TransparentButtonWithIcon(text = "Usuario existente",
                icon = Icons.Outlined.Person,
                onClick = { onUserExistenceSelected(UserExistence.EXISTENTE) })
        }
    }
}