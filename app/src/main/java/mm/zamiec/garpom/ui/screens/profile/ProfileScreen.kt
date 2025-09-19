package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.auth.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    Column {
        Text("Profile")
        Button(onClick = {
            Log.d("a",viewModel.test())
        }) {
            Text("Test")
        }
    }
}