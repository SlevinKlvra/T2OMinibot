package com.intec.t2o.components.novisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.intec.t2o.R
import com.intec.t2o.components.ButtonCard
import com.intec.t2o.screens.UserType
import com.intec.t2o.ui.theme.textColor

@Composable
fun UserTypeSelection(onUserTypeSelected: (UserType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        Text(
            text = "Cliente o proveedor",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Seleccione si es cliente o proveedor",
            color = textColor,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 35.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ButtonCard(text = "Cliente",
                icon = Icons.Outlined.ShoppingCart,
                onClick = { onUserTypeSelected(UserType.CLIENTE) })
            ButtonCard(text = "Proveedor",
                icon = ImageVector.vectorResource(R.drawable.shipping),
                onClick = { onUserTypeSelected(UserType.PROVEEDOR) })
        }
    }
}