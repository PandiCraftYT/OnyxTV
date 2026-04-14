package com.example.onyxapp.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onyxapp.MainViewModel

@Composable
fun LoginScreen(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    LaunchedEffect(Unit) {
        viewModel.stopPlayback()
    }
    
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val scrollState = rememberScrollState()
    val showPromo = viewModel.isFromPromoChannel || !viewModel.isUserAuthenticated

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
            .background(Brush.verticalGradient(listOf(Color(0xFF00141A), Color.Black))),
        contentAlignment = Alignment.Center
    ) {
        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showPromo) {
                    PromoCard(
                        modifier = Modifier.fillMaxWidth(),
                        onContactClick = onContactClick
                    )
                    Spacer(Modifier.height(32.dp))
                }
                LoginForm(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    onUserChange = { user = it; viewModel.clearAuthError() },
                    pass = pass,
                    onPassChange = { pass = it; viewModel.clearAuthError() },
                    passVisible = passVisible,
                    onTogglePass = { passVisible = !passVisible },
                    isAuthLoading = viewModel.isAuthLoading,
                    authError = viewModel.authError,
                    onLogin = { viewModel.signIn(user, pass) },
                    onDismiss = onDismiss
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showPromo) {
                    PromoCard(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(0.9f)
                            .padding(end = 32.dp),
                        onContactClick = onContactClick
                    )
                }
                LoginForm(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 450.dp)
                        .fillMaxHeight(0.9f),
                    user = user,
                    onUserChange = { user = it; viewModel.clearAuthError() },
                    pass = pass,
                    onPassChange = { pass = it; viewModel.clearAuthError() },
                    passVisible = passVisible,
                    onTogglePass = { passVisible = !passVisible },
                    isAuthLoading = viewModel.isAuthLoading,
                    authError = viewModel.authError,
                    onLogin = { viewModel.signIn(user, pass) },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun PromoCard(modifier: Modifier, onContactClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onContactClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = if (isFocused) Color(0xFF00B4D8).copy(alpha = 0.15f) else Color(0xFF00B4D8).copy(alpha = 0.08f)),
        border = BorderStroke(if (isFocused) 3.dp else 1.dp, if (isFocused) Color(0xFF00B4D8) else Color(0xFF00B4D8).copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("ONYX PREMIUM", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(24.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PromoItem("Más de 60 Canales en Vivo")
                PromoItem("Calidad Full HD y 4K")
                PromoItem("Sin anuncios ni interrupciones")
                PromoItem("Soporte 24/7 Personalizado")
            }
            
            Spacer(Modifier.weight(1f))
            
            Text(
                "Para contratar el servicio Premium contacta por Instagram:",
                color = Color.White.copy(0.6f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onContactClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("CONTRATAR AQUÍ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun LoginForm(
    modifier: Modifier,
    user: String,
    onUserChange: (String) -> Unit,
    pass: String,
    onPassChange: (String) -> Unit,
    passVisible: Boolean,
    onTogglePass: () -> Unit,
    isAuthLoading: Boolean,
    authError: String?,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isFormFocused by remember { mutableStateOf(false) }
    var isUserFocused by remember { mutableStateOf(false) }
    var isPassFocused by remember { mutableStateOf(false) }

    val fieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.White.copy(0.12f),
        unfocusedContainerColor = Color.White.copy(0.05f),
        cursorColor = Color(0xFF00B4D8),
        focusedIndicatorColor = Color(0xFF00B4D8),
        unfocusedIndicatorColor = Color.White.copy(0.2f),
        focusedLabelColor = Color(0xFF00B4D8),
        unfocusedLabelColor = Color.White.copy(0.5f)
    )

    Card(
        modifier = modifier.onFocusChanged { isFormFocused = it.isFocused },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = if (isFormFocused) 0.08f else 0.05f)),
        border = BorderStroke(if (isFormFocused) 3.dp else 1.dp, if (isFormFocused) Color(0xFF00B4D8).copy(0.8f) else Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(40.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("BIENVENIDO", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Ingresa a tu cuenta", color = Color.White.copy(0.5f), fontSize = 15.sp)
            
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = user,
                onValueChange = onUserChange,
                label = { Text("Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isUserFocused = it.isFocused },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = if (isUserFocused) Color(0xFF00B4D8) else Color.White.copy(0.6f)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isPassFocused = it.isFocused },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (isPassFocused) Color(0xFF00B4D8) else Color.White.copy(0.6f)) },
                trailingIcon = {
                    IconButton(onClick = onTogglePass) {
                        Icon(if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.White.copy(0.6f))
                    }
                },
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            AnimatedVisibility(visible = authError != null) {
                authError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    focusManager.clearFocus()
                    onLogin() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                shape = RoundedCornerShape(14.dp),
                enabled = !isAuthLoading
            ) {
                if (isAuthLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) {
                Text("CONTINUAR COMO INVITADO", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PromoItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White.copy(0.85f), fontSize = 15.sp)
    }
}
