package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileAuthScreen(modifier: Modifier = Modifier) {
    var isLoggedIn by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoggedIn) {
            Text("Welcome to Brain Learning!", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Data synced with Firebase Firestore.", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { isLoggedIn = false }) {
                Text("Sign Out")
            }
        } else {
            Text("Sign in to sync your progress", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { isLoggedIn = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in with Google")
            }
        }
    }
}
