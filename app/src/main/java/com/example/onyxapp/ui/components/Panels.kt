package com.example.onyxapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.onyxapp.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SettingButton(
    text: String, 
    icon: ImageVector, 
    color: Color, 
    statusText: String? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = if (isFocused) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, if (isFocused) color else Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(icon, contentDescription = null, tint = color)
                Text(
                    text = text,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                )
            }
            if (statusText != null) {
                Text(
                    text = statusText,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.background(color.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    var showPassDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("CONFIGURACIÓN", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)

        // BOTÓN DE ACELERACIÓN DE HARDWARE
        SettingButton(
            text = "Modo de Reproducción", 
            icon = Icons.Default.Memory, 
            color = if (viewModel.isHwEnabled) Color(0xFF00FF00) else Color(0xFFFFA500),
            statusText = if (viewModel.isHwEnabled) "GPU (Hardware)" else "CPU (Software)"
        ) {
            viewModel.toggleHwAcceleration()
            onInteraction()
        }

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
        
        Spacer(Modifier.weight(1f))
        
        // Versión de la App al final
        Text(
            text = "Versión 2.0.0 - Onyx TV",
            color = Color.White.copy(0.3f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // --------------------------------------------------------
    // DIÁLOGO DE SOPORTE
    // --------------------------------------------------------
    if (showInfoDialog) {
        val closeFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            try { closeFocusRequester.requestFocus() } catch (e: Exception) {}
        }

        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f).widthIn(max = 400.dp),
            title = { Text("SOPORTE ONYX TV", color = Color(0xFF00B4D8), fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Para renovaciones o compra de cuentas premium, contacta al administrador.", textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(20.dp))
                    Text("Instagram: @carlosnvz_", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier.focusRequester(closeFocusRequester)
                ) {
                    Text("CERRAR", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --------------------------------------------------------
    // DIÁLOGO DE CERRAR SESIÓN
    // --------------------------------------------------------
    if (showLogoutConfirm) {
        val cancelFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            try { cancelFocusRequester.requestFocus() } catch (e: Exception) {}
        }

        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f).widthIn(max = 400.dp),
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
                TextButton(
                    onClick = { showLogoutConfirm = false },
                    modifier = Modifier.focusRequester(cancelFocusRequester)
                ) {
                    Text("CANCELAR", color = Color.White)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --------------------------------------------------------
    // DIÁLOGO DE CONTRASEÑA
    // --------------------------------------------------------
    if (showPassDialog) {
        var newPass by remember { mutableStateOf("") }
        val updateFocusRequester = remember { FocusRequester() }
        val inputFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(200)
            try { inputFocusRequester.requestFocus() } catch (e: Exception) {}
        }

        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f).widthIn(max = 450.dp),
            title = { Text("CAMBIAR CONTRASEÑA", color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Introduce tu nueva contraseña (mínimo 6 caracteres):", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(inputFocusRequester),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                try { updateFocusRequester.requestFocus() } catch(e: Exception) {}
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(0.05f),
                            unfocusedContainerColor = Color.White.copy(0.05f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF00B4D8),
                            unfocusedIndicatorColor = Color.White.copy(0.2f),
                            cursorColor = Color(0xFF00B4D8)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        if (newPass.length >= 6) {
                            viewModel.updateOwnPassword(newPass)
                            showPassDialog = false
                        }
                    },
                    modifier = Modifier.focusRequester(updateFocusRequester),
                    enabled = newPass.length >= 6,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B4D8),
                        disabledContainerColor = Color(0xFF00B4D8).copy(0.3f)
                    )
                ) {
                    Text("ACTUALIZAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    keyboardController?.hide()
                    showPassDialog = false
                }) {
                    Text("CANCELAR", color = Color.White)
                }
            },
            containerColor = Color(0xFF0A0E12),
            shape = RoundedCornerShape(16.dp)
        )
    }
}