package com.intec.t2o.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.padding(end = 10.dp, start = 4.dp),
                    textAlign = TextAlign.Center,
                    text = text,
                    maxLines = 1,
                    fontSize = 8.5.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}