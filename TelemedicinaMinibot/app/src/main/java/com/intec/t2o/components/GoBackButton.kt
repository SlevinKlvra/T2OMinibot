package com.intec.t2o.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intec.t2o.ui.theme.cardColor
import com.intec.t2o.ui.theme.iconColor

@Composable
fun GoBackButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = Modifier
            .size(56.dp)
            .padding(16.dp),
        containerColor = cardColor
    ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = iconColor)
    }
}