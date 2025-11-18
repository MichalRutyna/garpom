package mm.zamiec.garpom.ui.screens.configure.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mm.zamiec.garpom.ui.screens.configure.ScreenState

@Composable
fun ServiceDiscoveryDataScreen(
    data: ScreenState.ServiceDiscoveryData
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "Services",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        itemsIndexed(data.serviceData) { index, service ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Service ${index + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    service.forEach { (key, value) ->
                        Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Characteristics",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        itemsIndexed(data.characteristicsData) { index, characteristicsList ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Characteristics for Service ${index + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    characteristicsList.forEach { map ->
                        map.forEach { (ckey, cvalue) ->
                            Text("$ckey: $cvalue", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}