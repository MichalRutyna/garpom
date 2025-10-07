package mm.zamiec.garpom.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import mm.zamiec.garpom.controller.auth.ChangeUsernameResult

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val uiState: ProfileScreenState by profileViewModel.uiState.collectAsState(ProfileScreenState())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            Modifier.padding(paddingValues)
        ) {
            if (uiState.isAnonymous) {
                AnonymousScreen(onNavigateToAuth)
            } else {
                LoggedInScreen(
                    uiState,
                    profileViewModel::logOut,
                    { username ->
                        scope.launch {
                            val result = profileViewModel.changeUsername(username)
                            when (result) {
                                is ChangeUsernameScreenState.Error -> snackbarHostState.showSnackbar(result.message)
                                is ChangeUsernameScreenState.Success -> snackbarHostState.showSnackbar("Username changed")
                                is ChangeUsernameScreenState.Loading -> { }
                            }
                        }
                    },
                )
            }
//            Text("User id: ${uiState.userId}")
        }
    }
}

@Composable
private fun LoggedInScreen(
    uiState: ProfileScreenState,
    onLogout: () -> Unit,
    onChangeUsername: (String) -> Unit,
) {
    val usernameChange = remember { mutableStateOf("") }

    Column {
        Row (
            modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                uiState.username,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 5.dp)
            )

            Button(onClick = onLogout) {
                Text("Log out")
            }
        }
        HorizontalDivider(thickness = 2.dp)

        Row (
            Modifier.padding(top = 10.dp)
        ) {
            TextField(
                value = usernameChange.value,
                onValueChange = {
                    usernameChange.value = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp)
            )
            Button(onClick = {
                onChangeUsername(usernameChange.value)
            }) {
                Text("Change username")
            }
        }
    }
}

@Composable
private fun AnonymousScreen(onNavigateToAuth: () -> Unit) {
    Row {
        Text(
            "Anonymous",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = {
            onNavigateToAuth()
        }) {
            Text("Log in")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val uiState = ProfileScreenState(
        "",
        "Test user",
        false
    )
    LoggedInScreen(
        uiState, {}, {},
    )
}