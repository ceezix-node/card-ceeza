package com.example.cardceeza.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.cardceeza.model.TradeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class PushNotificationTtsService(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    var isTtsVoiceEnabled: Boolean = true

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(0.95f)
                    isTtsInitialized = true
                    Log.d("CardCeezaTTS", "TTS Engine initialized successfully")
                } else {
                    Log.w("CardCeezaTTS", "Failed to initialize TTS Engine (status: $status)")
                }
            }
        } catch (e: Exception) {
            Log.e("CardCeezaTTS", "Error during TTS init: ${e.localizedMessage}")
        }
    }

    /**
     * Speaks trade status transitions aloud to the user when enabled.
     */
    fun speakTradeStatusUpdate(tradeRef: String, newStatus: TradeStatus, payoutNgn: Double = 0.0) {
        if (!isTtsVoiceEnabled || !isTtsInitialized || tts == null) return

        val speechText = when (newStatus) {
            TradeStatus.PAID -> {
                val formattedPayout = "%,d".format(payoutNgn.toLong())
                "Great news! Your CardCeeza trade $tradeRef has been paid. Your payout of $formattedPayout Naira has been sent to your bank account."
            }
            TradeStatus.UNDER_REVIEW -> {
                "Trade update: Your trade $tradeRef is now under review by the CardCeeza verification team."
            }
            TradeStatus.APPROVED -> {
                "Your trade $tradeRef has been approved! Payout processing has commenced."
            }
            TradeStatus.VERIFICATION_REQUIRED -> {
                "Attention: Additional verification evidence is required for trade $tradeRef."
            }
            TradeStatus.REJECTED -> {
                "Notice: Your trade $tradeRef was declined by the verification desk."
            }
            else -> null
        }

        speechText?.let { text ->
            try {
                tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "trade_status_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e("CardCeezaTTS", "Error speaking text: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Handles simulated or live Firebase Cloud Messaging push payloads.
     */
    fun handleIncomingPushNotification(title: String, body: String, tradeRef: String? = null, status: TradeStatus? = null, payoutNgn: Double = 0.0) {
        if (status != null && tradeRef != null) {
            speakTradeStatusUpdate(tradeRef, status, payoutNgn)
        } else if (isTtsVoiceEnabled && isTtsInitialized) {
            tts?.speak("$title. $body", TextToSpeech.QUEUE_ADD, null, "push_${System.currentTimeMillis()}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("CardCeezaTTS", "Error shutting down TTS: ${e.localizedMessage}")
        }
    }
}
