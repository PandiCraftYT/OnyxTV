package com.example.onyxapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF00141A), Color.Black))),
        contentAlignment = Alignment.Center
    ) {
        val containerModifier = if (isPortrait) {
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 32.dp)
        } else {
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        }

        if (isPortrait) {
            Column(
                modifier = containerModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showPromo) {
                    PromoCard(
                        modifier = Modifier.fillMaxWidth(),
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/carlosnvz__"))
                            context.startActivity(intent)
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }
                LoginForm(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    onUserChange = { user = it },
                    pass = pass,
                    onPassChange = { pass = it },
                    passVisible = passVisible,
                    onTogglePass = { passVisible = !passVisible },
                    isLoading = viewModel.isLoading,
                    onLogin = { viewModel.signIn(user, pass) },
                    onDismiss = onDismiss
                )
            }
        } else {
            Row(
                modifier = containerModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showPromo) {
                    PromoCard(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(end = 24.dp)
                            .fillMaxHeight(0.85f),
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/carlosnvz__"))
                            context.startActivity(intent)
                        }
                    )
                }
                LoginForm(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 450.dp)
                        .fillMaxHeight(0.85f),
                    user = user,
                    onUserChange = { user = it },
                    pass = pass,
                    onPassChange = { pass = it },
                    passVisible = passVisible,
                    onTogglePass = { passVisible = !passVisible },
                    isLoading = viewModel.isLoading,
                    onLogin = { viewModel.signIn(user, pass) },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun PromoCard(modifier: Modifier, onContactClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00B4D8).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFF00B4D8).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("ONYX PREMIUM", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PromoItem("Más de 60 Canales en Vivo")
                PromoItem("Calidad Full HD y 4K")
                PromoItem("Sin anuncios ni interrupciones")
                PromoItem("Soporte 24/7 Personalizado")
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Para contratar el servicio Premium contacta directamente por Instagram:",
                color = Color.White.copy(0.6f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onContactClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("INSTAGRAM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
    isLoading: Boolean,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    val fieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.White.copy(0.05f),
        unfocusedContainerColor = Color.White.copy(0.05f),
        cursorColor = Color(0xFF00B4D8),
        focusedIndicatorColor = Color(0xFF00B4D8),
        unfocusedIndicatorColor = Color.White.copy(0.2f),
        focusedLabelColor = Color(0xFF00B4D8),
        unfocusedLabelColor = Color.White.copy(0.5f)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("BIENVENIDO", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Ingresa a tu cuenta", color = Color.White.copy(0.5f), fontSize = 14.sp)
            
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = user,
                onValueChange = onUserChange,
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF00B4D8)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00B4D8)) },
                trailingIcon = {
                    IconButton(onClick = onTogglePass) {
                        Icon(if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 8.dp)) {
                Text("VOLVER ATRÁS", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
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
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White.copy(0.8f), fontSize = 14.sp)
    }
}
