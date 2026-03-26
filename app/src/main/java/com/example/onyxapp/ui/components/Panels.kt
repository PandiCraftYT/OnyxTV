package com.example.onyxapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.onyxapp.MainViewModel
import com.example.onyxapp.Channel
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri

@Composable
fun AccountInfoCard(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (viewModel.isAdmin) Color(0xFFFFD700).copy(0.2f) else Color(0xFF00B4D8).copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (viewModel.isAdmin) Icons.Default.Shield else Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = if (viewModel.isAdmin) Color(0xFFFFD700) else Color(0xFF00B4D8),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = viewModel.currentUsername.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "N/A"
                Text(
                    text = "EXPIRA: $expiryStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    var showPassDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
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
                viewModel.logout()
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
                    Text("Para renovaciones o compra de cuentas premium, contacta al administrador.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(20.dp))
                    Text("Instagram: @carlosnvz_", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            },
            confirmButton = {
                Button(onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/carlosnvz_"))
                    context.startActivity(intent)
                    showInfoDialog = false 
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))) { Text("Abrir Instagram") }
            },
            dismissButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") } },
            containerColor = Color(0xFF1A1A1A), textContentColor = Color.White
        )
    }

    if (showPassDialog) {
        var newPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text("Nueva Contraseña") },
            text = {
                TextField(value = newPassword, onValueChange = { newPassword = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Escribe tu nueva clave") }, visualTransformation = PasswordVisualTransformation())
            },
            confirmButton = {
                Button(onClick = {
                    if (newPassword.isNotEmpty()) { viewModel.updateOwnPassword(newPassword); showPassDialog = false }
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { showPassDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun SettingButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).clickable { onClick() }.padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(15.dp))
            Text(label, color = Color.White)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AdminPanel(viewModel: MainViewModel, onInteraction: () -> Unit) {
    var adminTab by remember { mutableStateOf("CANALES") }
    var editingChannel by remember { mutableStateOf<Channel?>(null) }
    var editingUser by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showUserAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAllUsers() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(bottom = 15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TabButton("CANALES", adminTab == "CANALES") { adminTab = "CANALES"; onInteraction() }
            TabButton("USUARIOS", adminTab == "USUARIOS") { adminTab = "USUARIOS"; onInteraction() }
        }

        if (adminTab == "USUARIOS") {
            val filteredUsers = viewModel.allUsers.filter {
                val username = it["username"] as? String ?: ""
                val uid = it["uid"] as? String ?: ""
                username.contains(viewModel.searchQuery, true) || uid.contains(viewModel.searchQuery, true)
            }
            Box(Modifier.fillMaxSize()) {
                UserManagementList(viewModel, filteredUsers, onEdit = { editingUser = it }, onInteraction = onInteraction)
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                    FloatingActionButton(onClick = { showUserAddDialog = true; onInteraction() }, containerColor = Color(0xFF00B4D8)) {
                        Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                    }
                }
            }
        } else {
            val allFiltered = viewModel.allChannels.filter {
                it.name.contains(viewModel.searchQuery, true) || it.group?.contains(viewModel.searchQuery, true) == true
            }
            val enabledChannels = allFiltered.filter { it.isActive }
            val disabledChannels = allFiltered.filter { !it.isActive }

            Box(Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    if (enabledChannels.isNotEmpty()) {
                        item { SectionHeader("HABILITADOS - ${enabledChannels.size}", Color.Green) }
                        items(enabledChannels) { ChannelAdminRow(it, viewModel, { editingChannel = it }, onInteraction) }
                    }
                    if (disabledChannels.isNotEmpty()) {
                        item { Spacer(Modifier.height(10.dp)); SectionHeader("DESHABILITADOS - ${disabledChannels.size}", Color.Red) }
                        items(disabledChannels) { ChannelAdminRow(it, viewModel, { editingChannel = it }, onInteraction) }
                    }
                }
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                    FloatingActionButton(onClick = { showAddDialog = true; onInteraction() }, containerColor = Color(0xFFFFD700)) {
                        Icon(Icons.Default.Add, null, tint = Color.Black)
                    }
                }
            }
        }

        if (editingChannel != null) {
            var tempName by remember { mutableStateOf(editingChannel?.name ?: "") }
            var tempUrl by remember { mutableStateOf(editingChannel?.url ?: "") }
            var tempGroup by remember { mutableStateOf(editingChannel?.group ?: "") }
            var tempLogo by remember { mutableStateOf(editingChannel?.logo ?: "") }

            AlertDialog(
                onDismissRequest = { editingChannel = null },
                title = { Text("Editar Canal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Nombre") })
                        TextField(value = tempUrl, onValueChange = { tempUrl = it }, label = { Text("URL") })
                        TextField(value = tempGroup, onValueChange = { tempGroup = it }, label = { Text("Grupo") })
                        TextField(value = tempLogo, onValueChange = { tempLogo = it }, label = { Text("Logo (URL)") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        editingChannel?.let { viewModel.updateChannel(it, tempName, tempUrl, tempGroup, tempLogo) }
                        editingChannel = null; onInteraction()
                    }) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { editingChannel = null }) { Text("Cancelar") } }
            )
        }

        if (editingUser != null) {
            var username by remember { mutableStateOf(editingUser!!["username"] as? String ?: "") }
            var role by remember { mutableStateOf(editingUser!!["role"] as? String ?: "USER") }
            val uid = editingUser!!["uid"] as String
            val isActive = editingUser!!["isActive"] as? Boolean ?: false

            AlertDialog(
                onDismissRequest = { editingUser = null },
                title = { Text("Gestionar Usuario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(value = username, onValueChange = { username = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rol:")
                            Row {
                                TabButton("USER", role == "USER") { role = "USER" }
                                Spacer(Modifier.width(8.dp))
                                TabButton("ADMIN", role == "ADMIN") { role = "ADMIN" }
                            }
                        }
                        Button(onClick = {
                            val newExpiry = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
                            viewModel.updateUserDetails(uid, mapOf("expiryDate" to com.google.firebase.Timestamp(newExpiry)))
                            onInteraction()
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))) {
                            Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text("Renovar +30 Días")
                        }
                        Button(onClick = {
                            viewModel.toggleUserStatus(uid, isActive)
                            editingUser = null; onInteraction()
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (isActive) Color.Red else Color.Green)) {
                            Text(if (isActive) "Bloquear Cuenta" else "Desbloquear Cuenta")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateUserDetails(uid, mapOf("username" to username, "role" to role))
                        editingUser = null; onInteraction()
                    }) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { editingUser = null }) { Text("Cerrar") } }
            )
        }

        if (showUserAddDialog) {
            var userEmail by remember { mutableStateOf("") }; var userPass by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showUserAddDialog = false }, title = { Text("Nuevo Usuario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(value = userEmail, onValueChange = { userEmail = it }, label = { Text("Usuario") })
                        TextField(value = userPass, onValueChange = { userPass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation())
                    }
                },
                confirmButton = { Button(onClick = { viewModel.createManualUser(userEmail, userPass); showUserAddDialog = false; onInteraction() }) { Text("Crear") } }
            )
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf("") }; var url by remember { mutableStateOf("") }; var group by remember { mutableStateOf("TODOS") }; var logo by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false }, title = { Text("Nuevo Canal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                        TextField(value = url, onValueChange = { url = it }, label = { Text("URL") })
                        TextField(value = group, onValueChange = { group = it }, label = { Text("Grupo") })
                        TextField(value = logo, onValueChange = { logo = it }, label = { Text("Logo (URL)") })
                    }
                },
                confirmButton = { Button(onClick = { viewModel.addChannel(name, url, group, logo); showAddDialog = false; onInteraction() }) { Text("Agregar") } }
            )
        }
    }
}

@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(35.dp).widthIn(min = 80.dp).clip(RoundedCornerShape(50)).background(if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(text = title, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
}

@Composable
fun ChannelAdminRow(channel: Channel, viewModel: MainViewModel, onEdit: () -> Unit, onInteraction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (channel.isActive) Color.White.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.1f)).border(1.dp, if (channel.isActive) Color.White.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { viewModel.playVideo(channel.url); onInteraction() }.padding(12.dp)) {
            Column {
                Text(channel.name, color = if (channel.isActive) Color.White else Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(channel.url, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1)
            }
        }
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(if (channel.isActive) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)).border(1.dp, if (channel.isActive) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { viewModel.toggleChannelStatus(channel.url, channel.isActive); onInteraction() }, contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, tint = if (channel.isActive) Color.Green else Color.Red, modifier = Modifier.size(20.dp))
        }
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFD700).copy(alpha = 0.1f)).border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { onEdit() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
        }
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.Red.copy(alpha = 0.1f)).border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { viewModel.deleteChannel(channel.url); onInteraction() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun UserManagementList(viewModel: MainViewModel, users: List<Map<String, Any>>, onEdit: (Map<String, Any>) -> Unit, onInteraction: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(users) { user ->
            val username = user["username"] as? String ?: "Desconocido"
            val isActive = user["isActive"] as? Boolean ?: false
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isActive) Color.White.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.08f)).border(1.dp, if (isActive) Color.White.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).clickable { onEdit(user); onInteraction() }.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(username.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        val exp = user["expiryDate"] as? com.google.firebase.Timestamp
                        val expStr = exp?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "N/A"
                        Text("Expira: $expStr", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Text(text = if (isActive) "ACTIVO" else "BLOQUEADO", color = if (isActive) Color.Green else Color.Red, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}
