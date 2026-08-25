package com.example.cardceeza.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {
    /**
     * Formats phone number to international WhatsApp format (defaults to Nigeria +234 if local).
     */
    fun formatPhoneNumberForWhatsApp(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.startsWith("234") -> digitsOnly
            digitsOnly.startsWith("0") -> "234" + digitsOnly.substring(1)
            digitsOnly.length == 10 -> "234$digitsOnly"
            else -> digitsOnly
        }
    }

    /**
     * Launches WhatsApp chat with customer with a pre-filled contextual message.
     */
    fun openWhatsAppChat(
        context: Context,
        phoneNumber: String,
        message: String = ""
    ) {
        try {
            val formattedPhone = formatPhoneNumberForWhatsApp(phoneNumber)
            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildTradeStatusMessage(tradeRef: String, cardName: String, status: String, payoutNgn: String): String {
        return "Hello from CardCeeza Support!\nRegarding trade #$tradeRef ($cardName):\nStatus: $status\nPayout Value: ₦$payoutNgn\n\nTrade Gift Cards. Get Paid in NGN."
    }

    fun buildSupportReplyMessage(ticketRef: String, subject: String, replyBody: String): String {
        return "Hello from CardCeeza Support!\nRegarding Ticket #$ticketRef ($subject):\n\n$replyBody\n\nCardCeeza Team"
    }
}
