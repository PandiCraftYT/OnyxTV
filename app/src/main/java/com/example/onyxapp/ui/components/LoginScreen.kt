package com.example.onyxapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.AnimatedBackground
import com.example.onyxapp.MainViewModel

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        radius = 1500f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(450.dp)
                    .padding(16.dp)
                    .shadow(
                        elevation = 30.dp, 
                        shape = RoundedCornerShape(28.dp), 
                        ambientColor = Color(0xFF00B4D8), 
                        spotColor = Color(0xFF00B4D8)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E12).copy(alpha = 0.95f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B4D8).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(top = 40.dp, start = 40.dp, end = 40.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ONYX TV",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = "SISTEMA DE STREAMING",
                        fontSize = 12.sp,
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )

                    // CAMPO EMAIL OPTIMIZADO PARA TV
                    var isEmailEditing by remember { mutableStateOf(false) }
                    var isEmailFocused by remember { mutableStateOf(false) }
                    val emailFocusRequester = remember { FocusRequester() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isEmailFocused = it.isFocused }
                            .clickable { 
                                isEmailEditing = true
                                emailFocusRequester.requestFocus()
                            }
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            placeholder = { Text("ejemplo@onyxtv.app") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(emailFocusRequester)
                                .onFocusChanged { if (!it.isFocused) isEmailEditing = false }
                                .focusProperties { canFocus = isEmailEditing },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00B4D8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B4D8),
                                unfocusedBorderColor = if (isEmailFocused) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.1f),
                                focusedLabelColor = Color(0xFF00B4D8),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // CAMPO CONTRASEÑA OPTIMIZADO PARA TV
                    var isPassEditing by remember { mutableStateOf(false) }
                    var isPassFocused by remember { mutableStateOf(false) }
                    val passFocusRequester = remember { FocusRequester() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isPassFocused = it.isFocused }
                            .clickable { 
                                isPassEditing = true
                                passFocusRequester.requestFocus()
                            }
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(passFocusRequester)
                                .onFocusChanged { if (!it.isFocused) isPassEditing = false }
                                .focusProperties { canFocus = isPassEditing },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00B4D8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B4D8),
                                unfocusedBorderColor = if (isPassFocused) Color(0xFF00B4D8) else Color.White.copy(alpha = 0.1f),
                                focusedLabelColor = Color(0xFF00B4D8),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = Color(0xFF00B4D8), modifier = Modifier.size(40.dp))
                    } else {
                        Button(
                            onClick = { viewModel.signIn(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00B4D8),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                "INICIAR SESIÓN",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // BOTÓN DE CONTACTO EN LOGIN
                    TextButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("¿No tienes cuenta? Contacto y Soporte", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }

                    if (viewModel.authError != null) {
                        Surface(
                            modifier = Modifier.padding(top = 16.dp),
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = viewModel.authError!!,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { 
                Text("SOPORTE ONYX TV", color = Color(0xFF00B4D8), fontWeight = FontWeight.Black) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Para adquirir una cuenta premium o renovar tu suscripción, contacta al administrador.",
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Instagram: @carlosnvz__",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/carlosnvz__"))
                        context.startActivity(intent)
                        showInfoDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))
                ) {
                    Text("Abrir Instagram")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") }
            },
            containerColor = Color(0xFF1A1A1A),
            textContentColor = Color.White
        )
    }
}

@Composable
fun UnauthorizedScreen(viewModel: MainViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(500.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF120A0A).copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Red.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "ACCESO RESTRINGIDO",
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.accountStatusMessage ?: "Tu cuenta no está autorizada. Contacta al administrador.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("VOLVER AL LOGIN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
