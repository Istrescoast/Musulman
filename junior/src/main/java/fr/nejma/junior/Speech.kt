package fr.nejma.junior

import android.content.Context
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import java.util.Locale

/**
 * Lecture à voix haute pour l'interface (window.AndroidTTS en JavaScript).
 * La WebView d'Android n'a pas la synthèse vocale du navigateur : on passe par celle du téléphone.
 */
class Speech(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
    }

    private fun locale(tag: String): Locale = Locale.forLanguageTag(tag)

    @JavascriptInterface
    fun isReady(): Boolean = ready

    /** Vrai si le téléphone sait lire cette langue (« fr-FR », « ar-SA »…). */
    @JavascriptInterface
    fun hasLanguage(tag: String): Boolean =
        ready && tts.isLanguageAvailable(locale(tag)) >= TextToSpeech.LANG_AVAILABLE

    @JavascriptInterface
    fun speak(text: String, tag: String, rate: Float) {
        if (!ready) return
        tts.language = locale(tag)
        tts.setSpeechRate(rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nejma")
    }

    @JavascriptInterface
    fun stop() {
        if (ready) tts.stop()
    }

    fun shutdown() = tts.shutdown()
}
