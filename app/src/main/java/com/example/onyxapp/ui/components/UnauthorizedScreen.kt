package com.example.onyxapp.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.MainViewModel

@Composable
fun UnauthorizedScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    val onContactClick = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/carlosnvz_"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

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

                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF00B4D8))
                } else {
                    // Botón para verificar cambios (meses agregados, etc)
                    Button(
                        onClick = { viewModel.refreshAccess() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(12.dp))
                        Text("VERIFICAR NUEVAMENTE", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botón de Contacto
                Button(
                    onClick = onContactClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)), // Color Instagram
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.SupportAgent, null)
                    Spacer(Modifier.width(12.dp))
                    Text("CONTACTAR DUEÑO", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

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
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(12.dp))
                    Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
