package mm.zamiec.garpom.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    Column {
        Text("Profile")
            // TODO switch to ui state
        when (profileViewModel.currentUser) {
            null -> {

            }
        }
        Text("Name: "+profileViewModel.currentUser?.displayName)
        Button(onClick = {
            profileViewModel.test()
        }) {
            Text("Set name")
        }
        Button(onClick = {
            onNavigateToAuth()
        }) {
            Text("Log in")
        }
    }
}