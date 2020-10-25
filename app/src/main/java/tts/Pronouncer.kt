package tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*
import kotlin.random.Random

class Pronouncer(ctx: Context) {

    private var tts: TextToSpeech? = null
    private val charPool: List<Char> = ('a'..'z') + ('0'..'9')

    init {
        this.tts = TextToSpeech(ctx) {
            @Override
            fun onInit(status: Int) {
                if (status != TextToSpeech.ERROR) {
                    this.tts?.language = Locale.GERMAN
                }
            }
        }
    }


    fun pronounce(number: Int, speed: Float = 2.0f) {
        val utteranceId = number.toString() + "_" + randomString(4)
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