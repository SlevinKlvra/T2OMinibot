import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intec.telemedicina.viewmodels.SplashScreenViewModel

@Composable
fun NavigationDialog(
    onDismiss: () -> Unit,
    onStopNavigation: () -> Unit,
    onReturnToPreviousOrReception: () -> Unit,
    onEmergencyCall : () -> Unit,
    splashScreenViewModel: SplashScreenViewModel
) {

    val navigationStatus by splashScreenViewModel.navigationStatus.observeAsState(initial = "")

    AlertDialog(
        onDismissRequest = { onDismiss },
        title = { Text(text = "Estado de la Navegación") },
        text = { Text(text = navigationStatus) },
        confirmButton = {
            Button(onClick = {
                /*splashScreenViewModel.stopNavigation()*/
                splashScreenViewModel.hideNavigationDialog()
            }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Detener",
                        modifier = Modifier.size(24.dp).padding(end = 10.dp)
                    )
                    Text("Detener")
                }
            }
        },
        dismissButton = {
            Button(onClick = onReturnToPreviousOrReception) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Regresar",
                        modifier = Modifier.size(24.dp).padding(end = 10.dp)
                    )
                    Text("Regresar al Punto Anterior/Recepción")
                }
            }
            Button(onClick = onEmergencyCall){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Regresar",
                        modifier = Modifier.size(24.dp).padding(end = 10.dp)
                    )
                    Text("Llamar a Emergencias")

                }
            }
        }
    )
}
