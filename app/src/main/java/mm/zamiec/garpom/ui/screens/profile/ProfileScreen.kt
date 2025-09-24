package mm.zamiec.garpom.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val uiState: ProfileState by profileViewModel.uiState.collectAsState(ProfileState())

    Column {
        Text("Profile")
        if (uiState.isAnonymous) {
            Text("Welcome!", style= MaterialTheme.typography.headlineLarge)

            Button(onClick = {
                onNavigateToAuth()
            }) {
                Text("Log in")
            }
        } else {
            Text("Welcome, ${uiState.username}", style= MaterialTheme.typography.headlineLarge)

            Button(onClick = {
                profileViewModel.logOut()
            }) {
                Text("Log out")
            }
        }


    }
}