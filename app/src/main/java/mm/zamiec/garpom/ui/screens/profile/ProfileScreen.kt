package mm.zamiec.garpom.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val uiState: ProfileState by profileViewModel.uiState.collectAsState(ProfileState())

    Column (
        Modifier.padding(10.dp)
    ) {
        if (uiState.isAnonymous) {
            Row {
                Text("Anonymous",
                    style=MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    onNavigateToAuth()
                }) {
                    Text("Log in")
                }
            }
        } else {
            Row {
                Text(uiState.username,
                    style=MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    profileViewModel.logOut()
                }) {
                    Text("Log out")
                }
            }
        }

        Text("User id: ${uiState.userId}")
    }
}