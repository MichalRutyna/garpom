package com.example.myapplication.v7

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.v7.ui.theme.MyApplicationTheme

class Nav3Activity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavExample()
            }

        }
    }
}

data object Home
data class Product(val id: String)

@Composable
fun NavExample() {
    val backStack = rememberNavBackStack(Destination.Home)

    Scaffold(
        bottomBar = {
            AppNavigationBar(
                backStack.lastOrNull() as? Destination,
                onClick = { screen ->
                    backStack.clear()
                    backStack.add(screen)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Destination.Home> {
                        Column {
                            Text("Home")
                        }
                    }
                    entry<Destination.Alarms> {
                        Column {
                            Text("Alarms")
                        }
                    }
                    entry<Destination.Configure> {
                        Column {
                            Text("Configure")
                        }
                    }
                    entry<Destination.Profile> {
                        Column {
                            Text("Profile")
                        }
                    }
                }
            )
        }
    }
}