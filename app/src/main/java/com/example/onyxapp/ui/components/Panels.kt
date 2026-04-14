package com.example.onyxapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.MainViewModel

@Composable
fun SettingButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    var showPassDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("CONFIGURACIÓN", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)

        SettingButton("Contacto y Soporte", Icons.Default.Info, Color(0xFF00B4D8)) {
            showInfoDialog = true
            onInteraction()
        }

        if (viewModel.isUserAuthenticated) {
            SettingButton("Cambiar mi Contraseña", Icons.Default.Lock, Color(0xFF00B4D8)) {
                showPassDialog = true
                onInteraction()
            }

            SettingButton("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, Color.Red) {
                showLogoutConfirm = true
                onInteraction()
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("SOPORTE ONYX TV", color = Color(0xFF00B4D8), fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Para renovaciones o compra de cuentas premium, contacta al administrador.", textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(20.dp))
                    Text("Instagram: @carlosnvz_", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("CERRAR", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("¿CERRAR SESIÓN?", color = Color.Red, fontWeight = FontWeight.Black) },
            text = {
                Text("¿Estás seguro de que quieres cerrar sesión? Tendrás acceso limitado a los canales.", color = Color.White.copy(alpha = 0.8f))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SÍ, SALIR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("CANCELAR", color = Color.White)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPassDialog) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text("CAMBIAR CONTRASEÑA", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Introduce tu nueva contraseña:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    TextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(0.05f),
                            unfocusedContainerColor = Color.White.copy(0.05f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPass.length >= 6) {
                        viewModel.updateOwnPassword(newPass)
                        showPassDialog = false
                    }
                }) {
                    Text("ACTUALIZAR", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPassDialog = false }) {
                    Text("CANCELAR", color = Color.White)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
