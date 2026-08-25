package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TicketPriority
import com.example.cardceeza.model.TicketStatus

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val ticketNumber: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val subject: String,
    val category: TicketCategory,
    val priority: TicketPriority = TicketPriority.MEDIUM,
    val status: TicketStatus = TicketStatus.OPEN,
    val messagesJson: String, // Array of message objects formatted as json string
    val relatedTradeId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
