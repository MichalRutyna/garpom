package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import mm.zamiec.garpom.auth.AuthUiState
import mm.zamiec.garpom.auth.AuthViewModel


@Composable
fun AuthRouteController(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel<AuthViewModel>(),
    isInSubNavigation: MutableState<Boolean>,
) {
    val backStack = rememberNavBackStack(AuthRoutes.PhoneInput)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by authViewModel.uiState.collectAsState()


    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Error -> {
                val errorState = uiState as AuthUiState.Error
                scope.launch {
                    snackbarHostState.showSnackbar(errorState.message)
                }
            }
            is AuthUiState.Idle -> {
                backStack.clear()
                backStack.add(AuthRoutes.PhoneInput)
            }
            is AuthUiState.CodeSent -> {
                backStack.add(AuthRoutes.CodeInput)
            }
            is AuthUiState.Success -> onAuthSuccess()
            is AuthUiState.Loading -> {}
        }
    }
    LaunchedEffect(uiState) {
        isInSubNavigation.value = backStack.count() > 1
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column {
            Text("State: "+uiState)
            Text("Stack: "+backStack.toList())
            Text("IsInSub: "+isInSubNavigation.value)
            Button(onClick = {
                Log.d("Test", ""+backStack.toList().count())
            }) { Text("click")}
        }
        if (uiState == AuthUiState.Loading) {
            LoadingScreen()
        }
        else {
            NavDisplay(
                modifier = Modifier.padding(paddingValues),
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                ),
                backStack = backStack,
                onBack = {
                    backStack.removeLastOrNull()
                    authViewModel.backed()
                },
                entryProvider = entryProvider {
                    entry<AuthRoutes.PhoneInput> {
                        PhoneNumberInputScreen()
                    }
                    entry<AuthRoutes.CodeInput> {
                        CodeVerificationScreen()
                    }
                }
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInputScreen() {
    var phoneNumber by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Phone Authentication",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+1234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.startPhoneNumberVerification(phoneNumber) },
            enabled = phoneNumber.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Verification Code")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeVerificationScreen() {
    var code by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Verification Code",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("We've sent a verification code to your phone")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Verification Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.verifyCode(code) },
            enabled = code.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Code")
        }
    }
}


@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Processing...")
    }
}