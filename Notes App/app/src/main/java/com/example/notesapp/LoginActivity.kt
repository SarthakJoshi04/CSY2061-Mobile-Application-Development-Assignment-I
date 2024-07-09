package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.NotesAppTheme

class LoginActivity : ComponentActivity() {
    // Override onCreate to set the content of the activity using a Composable function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge mode
        setContent {
            // This theme provides the overall styling for the app, which is also used for this login screen.
            NotesAppTheme{
                // Login composable with callback for successful login
                LoginPage(onLoginSuccess = {
                    // Navigate to MainActivity on successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}

@Composable
fun LoginPage(onLoginSuccess: () -> Unit) {
    // State variables to store user input and error state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).statusBarsPadding()) {
            // Display login title
            Text(text = "Login to Notes", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            // Username text field
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Password text field with password masking
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) // Changes to secure keyboard when entering the password
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Display error message for unsuccessful login attempts
            if (loginError) {
                Text(text = "Login failed. Incorrect username or password.", color = MaterialTheme.colorScheme.error)
            }
            // Login button with logic for successful login or error handling
            Button(onClick = {
                if(username == "user" && password == "password"){
                    onLoginSuccess() // Call the callback for successful login
                } else{
                    loginError = true // Set error state for unsuccessful login
                }
            }) {
                Text(text = "Login")
            }
        }
    }
}
