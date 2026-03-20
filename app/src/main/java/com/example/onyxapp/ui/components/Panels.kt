package com.example.onyxapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
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
            .clip(RoundedCornerShape(20.dp))
            .background(if (viewModel.isAdmin) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF00B4D8).copy(alpha = 0.1f))
            .border(1.dp, if (viewModel.isAdmin) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFF00B4D8).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imageVector = if (viewModel.isAdmin) Icons.Default.Shield else Icons.Default.AccountCircle
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = if (viewModel.isAdmin) Color(0xFFFFD700) else Color(0xFF00B4D8),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Text(viewModel.currentUsername.uppercase(), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Black)
                val expiryStr = viewModel.userExpiryDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "N/A"
                Text("Expira: $expiryStr", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
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

        SettingButton("Cambiar mi Contraseña", Icons.Default.Lock, Color(0xFF00B4D8)) {
            showPassDialog = true
            onInteraction()
        }

        SettingButton("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, Color.Red) {
            viewModel.logout()
            onInteraction()
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
                        "Para renovaciones, problemas técnicos o compra de cuentas premium, puedes contactar directamente al administrador.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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

    if (showPassDialog) {
        var newPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text("Nueva Contraseña") },
            text = {
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Escribe tu nueva clave") },
                    visualTransformation = PasswordVisualTransformation()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPassword.isNotEmpty()) {
                        viewModel.updateOwnPassword(newPassword)
                        showPassDialog = false
                    }
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { showPassDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
    var adminSearchQuery by remember { mutableStateOf("") }

    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val addButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(bottom = 15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TabButton("CANALES", adminTab == "CANALES") {
                adminTab = "CANALES"
                adminSearchQuery = ""
                isSearchActive = false
                onInteraction()
            }
            TabButton("USUARIOS", adminTab == "USUARIOS") {
                adminTab = "USUARIOS"
                adminSearchQuery = ""
                isSearchActive = false
                onInteraction()
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp)) {
            if (!isSearchActive && adminSearchQuery.isEmpty()) {
                Surface(
                    onClick = { isSearchActive = true; onInteraction() },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onInteraction() },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, Color(0xFFFFD700)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Buscar...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                    }
                }
            } else {
                TextField(
                    value = adminSearchQuery,
                    onValueChange = {
                        adminSearchQuery = it
                        onInteraction()
                    },
                    placeholder = { Text("Buscar...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester)
                        .onFocusChanged {
                            if (it.isFocused) onInteraction()
                            if (!it.isFocused && adminSearchQuery.isEmpty()) isSearchActive = false
                        },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFFFFD700),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD700)) },
                    trailingIcon = {
                        IconButton(onClick = {
                            adminSearchQuery = ""
                            isSearchActive = false
                            onInteraction()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                )

                LaunchedEffect(isSearchActive) {
                    if (isSearchActive) searchFocusRequester.requestFocus()
                }
            }
        }

        if (adminTab == "USUARIOS") {
            val filteredUsers = viewModel.allUsers.filter {
                val username = it["username"] as? String ?: ""
                val uid = it["uid"] as? String ?: ""
                username.contains(adminSearchQuery, ignoreCase = true) || uid.contains(adminSearchQuery, ignoreCase = true)
            }
            Box(Modifier.fillMaxSize()) {
                UserManagementList(viewModel, filteredUsers, onEdit = { editingUser = it }, onInteraction = onInteraction)

                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        onClick = { showUserAddDialog = true; onInteraction() },
                        modifier = Modifier.size(50.dp),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        } else {
            val filteredChannels = viewModel.allChannels.filter {
                it.name.contains(adminSearchQuery, ignoreCase = true) || it.group?.contains(adminSearchQuery, ignoreCase = true) == true
            }
            Box(Modifier.fillMaxSize()) {
                ChannelManagementList(
                    channels = filteredChannels,
                    viewModel = viewModel,
                    onEdit = { editingChannel = it; onInteraction() },
                    onInteraction = onInteraction,
                    addButtonFocusRequester = addButtonFocusRequester
                )

                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        onClick = { showAddDialog = true; onInteraction() },
                        modifier = Modifier
                            .size(50.dp)
                            .focusRequester(addButtonFocusRequester),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFFFFD700))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
            }
        }

        if (editingChannel != null) {
            var tempUrl by remember { mutableStateOf(editingChannel?.url ?: "") }
            AlertDialog(
                onDismissRequest = { editingChannel = null },
                title = { Text("Editar Canal") },
                text = {
                    TextField(value = tempUrl, onValueChange = { tempUrl = it; onInteraction() }, modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    Button(onClick = {
                        editingChannel?.let { viewModel.updateChannelUrl(it.url, tempUrl) }
                        editingChannel = null
                        onInteraction()
                    }) { Text("Guardar") }
                }
            )
        }

        if (editingUser != null) {
            var username by remember { mutableStateOf(editingUser!!["username"] as? String ?: "") }
            var role by remember { mutableStateOf(editingUser!!["role"] as? String ?: "USER") }
            val uid = editingUser!!["uid"] as String

            AlertDialog(
                onDismissRequest = { editingUser = null },
                title = { Text("Gestionar Usuario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextField(value = username, onValueChange = { username = it }, label = { Text("Nombre") })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rol: ")
                            TabButton("USER", role == "USER") { role = "USER" }
                            Spacer(Modifier.width(5.dp))
                            TabButton("ADMIN", role == "ADMIN") { role = "ADMIN" }
                        }
                        Button(
                            onClick = {
                                val newExpiry = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
                                viewModel.updateUserDetails(uid, mapOf("expiryDate" to com.google.firebase.Timestamp(newExpiry)))
                                onInteraction()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                        ) {
                            Text("Renovar +30 Días")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateUserDetails(uid, mapOf("username" to username, "role" to role))
                        editingUser = null
                        onInteraction()
                    }) { Text("Guardar Cambios") }
                },
                dismissButton = {
                    TextButton(onClick = { editingUser = null }) { Text("Cerrar") }
                }
            )
        }

        if (showUserAddDialog) {
            var userEmail by remember { mutableStateOf("") }
            var userPass by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showUserAddDialog = false },
                title = { Text("Crear Nuevo Usuario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(value = userEmail, onValueChange = { userEmail = it }, label = { Text("Username") })
                        TextField(value = userPass, onValueChange = { userPass = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                        Text("Nota: Se creará como @onyxtv.app", fontSize = 10.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createManualUser(userEmail, userPass)
                        showUserAddDialog = false
                        onInteraction()
                    }) { Text("Crear") }
                }
            )
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var url by remember { mutableStateOf("") }
            var group by remember { mutableStateOf("TODOS") }
            var logo by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Canal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(value = name, onValueChange = { name = it; onInteraction() }, label = { Text("Nombre") })
                        TextField(value = url, onValueChange = { url = it; onInteraction() }, label = { Text("URL") })
                        TextField(value = group, onValueChange = { group = it; onInteraction() }, label = { Text("Grupo") })
                        TextField(value = logo, onValueChange = { logo = it }, label = { Text("Logo (URL)") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addChannel(name, url, group, logo)
                        showAddDialog = false
                        onInteraction()
                    }) { Text("Agregar") }
                }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(35.dp).widthIn(min = 80.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelManagementList(
    channels: List<Channel>,
    viewModel: MainViewModel,
    onEdit: (Channel) -> Unit,
    onInteraction: () -> Unit,
    addButtonFocusRequester: FocusRequester
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(channels) { channel ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { onEdit(channel) },
                    modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) onInteraction() },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(channel.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(channel.url, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1)
                    }
                }

                Surface(
                    onClick = { onEdit(channel) },
                    modifier = Modifier.size(48.dp).onFocusChanged { if (it.isFocused) onInteraction() },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, Color(0xFFFFD700)))
                    )
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    }
                }

                Surface(
                    onClick = { viewModel.deleteChannel(channel.url); onInteraction() },
                    modifier = Modifier
                        .size(48.dp)
                        .onFocusChanged { if (it.isFocused) onInteraction() }
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight) {
                                addButtonFocusRequester.requestFocus()
                                true
                            } else false
                        },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.Red.copy(alpha = 0.2f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, Color.Red))
                    )
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UserManagementList(
    viewModel: MainViewModel,
    users: List<Map<String, Any>>,
    onEdit: (Map<String, Any>) -> Unit,
    onInteraction: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            val uid = user["uid"] as? String ?: ""
            val username = user["username"] as? String ?: "Desconocido"
            val role = user["role"] as? String ?: "USER"
            val isActive = when(val active = user["isActive"]) {
                is Boolean -> active
                is String -> active.toBoolean()
                else -> false
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { viewModel.toggleUserStatus(uid, isActive); onInteraction() },
                    modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) onInteraction() },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isActive) Color.White.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (role == "ADMIN") Icon(Icons.Default.Shield, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Column {
                                Text(username.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val exp = user["expiryDate"] as? com.google.firebase.Timestamp
                                val expStr = exp?.toDate()?.let { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) } ?: "N/A"
                                Text("Expira: $expStr", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                        }
                        Text(
                            text = if (isActive) "ACTIVO" else "BLOQUEADO",
                            color = if (isActive) Color.Green else Color.Red,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    onClick = { onEdit(user); onInteraction() },
                    modifier = Modifier.size(48.dp).onFocusChanged { if (it.isFocused) onInteraction() },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color(0xFFFFD700))))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
