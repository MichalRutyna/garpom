package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import mm.zamiec.garpom.auth.AuthRepository
import mm.zamiec.garpom.auth.AuthViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    Column {
        Text("Profile")
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