
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intec.telemedicina.viewmodels.MqttViewModel

@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit,
    onStopNavigation: () -> Unit,
    onReturnToPreviousOrReception: () -> Unit,
    onEmergencyCall : () -> Unit,
    mqttViewModel: MqttViewModel
) {

    //Hi who are you? (select from options 'person who lives there', 'healthcare personnel',
    //'family or friends' --> Something like that to know who enters the home and send this response

    AlertDialog(
        onDismissRequest = { onDismiss },
        title = { Text(text = "Hola, bienvenidos a nuestro apartamento adaptado") },
        text = { Text(text = "Quién eres?") },
        confirmButton = {
            Button(onClick = { Log.d("WELCOME","LIVE") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Yo vivo aquí")
                }
            }
            Button(onClick = { Log.d("WELCOME","WORK") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Soy personal sanitario")
                }
            }
            Button(onClick = { Log.d("WELCOME","FRIEND") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Soy un amigo")
                }
            }
            Button(onClick = { Log.d("WELCOME","FAMILY") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Soy un familiar")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(24.dp).padding(end = 10.dp)
                    )
                }
            }
        }
        
    )
}
