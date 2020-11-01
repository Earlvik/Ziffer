package com.vikslop.ziffer.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import java.util.*
import kotlin.random.Random


class Pronouncer(ctx: Context) {

    private var tts: TextToSpeech? = null
    private val charPool: List<Char> = ('a'..'z') + ('0'..'9')

    init {
        this.tts = TextToSpeech(ctx) {
            @Override
            fun onInit(status: Int) {
                if (status != TextToSpeech.ERROR && this.tts?.isLanguageAvailable(Locale.GERMAN)
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    Log.i("Pronouncer", "TTS successfully initialized")
                } else {
                    val installIntent = Intent()
                    installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivity(ctx, installIntent, null)
                }
            }
        }
    }


    fun pronounce(number: Int, speed: Float = 2.0f) {
        val utteranceId = number.toString() + "_" + randomString(4)
        this.tts?.language = Locale.GERMAN
        this.tts?.setSpeechRate(speed)
        this.tts?.speak(number.toString(), TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun randomString(length: Int) {
        (1..length)
            .map { i -> Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}