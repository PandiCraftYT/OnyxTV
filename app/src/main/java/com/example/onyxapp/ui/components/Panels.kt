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
import androidx.compose.ui.text.font.FontWeight
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
    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("CONFIGURACIÓN", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        
        SettingButton("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, Color.Red) {
            viewModel.logout()
            onInteraction()
        }
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
    var showAddDialog by remember { mutableStateOf(false) }
    var adminSearchQuery by remember { mutableStateOf("") }
    
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val addButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) { 
        // Selector de Pestañas
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

        // Barra de Búsqueda
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
            UserManagementList(viewModel, filteredUsers, onInteraction)
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
                title = { Text("Editar Canal: ${editingChannel?.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Actualiza la URL si el canal está caído:", fontSize = 12.sp)
                        TextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it; onInteraction() },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        editingChannel?.let { viewModel.updateChannelUrl(it.url, tempUrl) }
                        editingChannel = null
                        onInteraction()
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { editingChannel = null; onInteraction() }) { Text("Cancelar") }
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
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false; onInteraction() }) { Text("Cancelar") }
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
        modifier = Modifier.height(35.dp).widthIn(min = 100.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    onInteraction: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            val uid = user["uid"] as? String ?: ""
            val username = user["username"] as? String ?: "Desconocido"
            val isActive = when(val active = user["isActive"]) {
                is Boolean -> active
                is String -> active.toBoolean()
                else -> false
            }
            
            Surface(
                onClick = { viewModel.toggleUserStatus(uid, isActive); onInteraction() },
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onInteraction() },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isActive) Color.White.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(username.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("ID: $uid", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                    Text(
                        text = if (isActive) "ACTIVO" else "BLOQUEADO", 
                        color = if (isActive) Color.Green else Color.Red, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
