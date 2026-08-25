package com.example.cardceeza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardceeza.data.local.entity.SupportTicketEntity
import com.example.cardceeza.data.local.entity.UserEntity
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TicketStatus
import com.example.cardceeza.model.UserRole
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald50
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupportScreen(
    currentUser: UserEntity?,
    tickets: List<SupportTicketEntity>,
    onBack: () -> Unit,
    onCreateTicket: (subject: String, category: TicketCategory, message: String, (SupportTicketEntity) -> Unit) -> Unit,
    onSendMessage: (ticketId: String, text: String, isSupportStaff: Boolean) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    // Active Ticket Discussion Modal
    if (selectedTicket != null) {
        var replyText by remember { mutableStateOf("") }
        val isStaff = currentUser?.role == UserRole.SUPPORT || currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.SUPER_ADMIN

        val messageList = remember(selectedTicket!!.messagesJson) {
            val list = mutableListOf<Triple<String, String, Long>>() // sender, text, time
            try {
                val arr = JSONArray(selectedTicket!!.messagesJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Triple(obj.optString("senderName", "User"), obj.optString("text", ""), obj.optLong("time", System.currentTimeMillis())))
                }
            } catch (e: Exception) {
                list.add(Triple("System", "Chat started", System.currentTimeMillis()))
            }
            list
        }

        Dialog(onDismissRequest = { selectedTicket = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("ticket_chat_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedTicket!!.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${selectedTicket!!.ticketNumber} • ${selectedTicket!!.category.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedTicket = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(messageList) { (sender, text, time) ->
                            val isMe = !sender.contains("Support", ignoreCase = true)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    color = if (isMe) Emerald700 else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(sender, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color(0xFFD1FAE5) else Color(0xFF475569))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text, fontSize = 13.sp, color = if (isMe) Color.White else Color.Black)
                                    }
                                }
                                Text(dateFormat.format(Date(time)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Type reply...") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onSendMessage(selectedTicket!!.id, replyText, isStaff)
                                    replyText = ""
                                    selectedTicket = null // Dismiss and refresh
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Emerald700)
                        }
                    }
                }
            }
        }
    }

    // New Ticket Modal
    if (showCreateDialog) {
        var subjectText by remember { mutableStateOf("") }
        var messageText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(TicketCategory.TRADE_ISSUE) }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).testTag("create_ticket_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Open Support Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subjectText,
                        onValueChange = { subjectText = it },
                        label = { Text("Subject") },
                        placeholder = { Text("e.g. Card verification question") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Message details") },
                        placeholder = { Text("Describe your issue...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showCreateDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (subjectText.isNotBlank() && messageText.isNotBlank()) {
                                    onCreateTicket(subjectText, category, messageText) { created ->
                                        showCreateDialog = false
                                        selectedTicket = created
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Submit Ticket")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Emerald700,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_ticket_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Ticket")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("support_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Help & Support Desk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

            if (tickets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = Emerald700, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active support tickets", fontWeight = FontWeight.Bold)
                        Text("Tap + to submit a question to the 24/7 desk.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(tickets) { ticket ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTicket = ticket }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        color = if (ticket.status == TicketStatus.RESOLVED) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = ticket.status.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.status == TicketStatus.RESOLVED) Color(0xFF15803D) else Color(0xFF0369A1),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${ticket.ticketNumber} • ${ticket.category.name} • ${dateFormat.format(Date(ticket.updatedAt))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
