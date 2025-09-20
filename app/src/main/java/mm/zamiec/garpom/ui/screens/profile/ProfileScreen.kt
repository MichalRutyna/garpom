package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import mm.zamiec.garpom.auth.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    Column {
        Text("Profile")
        Button(onClick = {
            onNavigateToAuth()
        }) {
            Text("Log in")
        }
    }
}