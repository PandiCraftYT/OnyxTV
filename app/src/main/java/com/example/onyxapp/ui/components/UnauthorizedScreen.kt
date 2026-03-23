package com.example.onyxapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.MainViewModel

@Composable
fun UnauthorizedScreen(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0000), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "ACCESO RESTRINGIDO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = viewModel.accountStatusMessage ?: "Tu cuenta no tiene autorización para acceder en este momento. Contacta al soporte para más información.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(Modifier.height(40.dp))

                if (viewModel.accountStatusMessage?.contains("vinculada", ignoreCase = true) == true) {
                    Button(
                        onClick = { viewModel.relinkDevice() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Tv, null)
                        Spacer(Modifier.width(12.dp))
                        Text("VINCULAR ESTE DISPOSITIVO", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(12.dp))
                    Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
