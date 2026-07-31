package com.custom.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class VpnServer(val name: String, val config: String, val type: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VpnMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnMainScreen() {
    var isConnected by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableLongStateOf(0L) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val servers = remember {
        mutableStateListOf(
            VpnServer("DE - Frankfurt 01", "vless://example1", "VLESS"),
            VpnServer("NL - Amsterdam 02", "vless://example2", "VLESS")
        )
    }
    var selectedServer by remember { mutableStateOf(servers.firstOrNull()) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            durationSeconds = 0L
            while (true) {
                delay(1000L)
                durationSeconds++
            }
        }
    }

    val pulseAnim by animateFloatAsState(
        targetValue = if (isConnected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonPulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Xray VPN") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isConnected) "ПОДКЛЮЧЕНО" else "ОТКЛЮЧЕНО",
                    color = if (isConnected) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTimer(durationSeconds),
                    fontSize = 36.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseAnim)
                    .background(
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935),
                        shape = CircleShape
                    )
                    .clickable { isConnected = !isConnected }
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Connect",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Выберите сервер:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(servers) { server ->
                        ServerCard(
                            server = server,
                            isSelected = server == selectedServer,
                            onSelect = { selectedServer = server }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddConfigDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { input ->
                if (input.startsWith("vless://")) {
                    servers.add(VpnServer("VLESS Config ${servers.size + 1}", input, "VLESS"))
                } else if (input.startsWith("https://")) {
                    servers.add(VpnServer("HTTPS Subscription", input, "HTTPS"))
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ServerCard(server: VpnServer, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = server.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = server.type, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onSelect)
        }
    }
}

@Composable
fun AddConfigDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить подписку или VLESS") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("https://... или vless://...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onAdd(text) }) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

fun formatTimer(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}