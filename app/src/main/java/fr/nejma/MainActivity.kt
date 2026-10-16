package fr.nejma

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader

/**
 * Nejma : l'interface (HTML/JS) est embarquée dans l'appli et fonctionne hors ligne.
 * Cette activité l'affiche et lui donne accès au téléphone via [Bridge] :
 * position, rappels de prière et widget.
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private var geoPending: Pair<String, GeolocationPermissions.Callback>? = null

    private val askPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        geoPending?.let { (origin, callback) ->
            callback.invoke(origin, hasLocation(), false)
        }
        geoPending = null
        if (result[Manifest.permission.POST_NOTIFICATIONS] == true) Scheduler.reschedule(this)
    }

    private fun hasLocation() =
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web = WebView(this).apply {
            setBackgroundColor(BRAND)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setGeolocationEnabled(true)
            settings.allowFileAccess = false
            settings.textZoom = 100
            addJavascriptInterface(Bridge(this@MainActivity), "Android")
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? =
                    request?.let { loader.shouldInterceptRequest(it.url) }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url ?: return false
                    if (url.host == APP_HOST) return false
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, url)) }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                // La position sert aux horaires de prière et à la qibla.
                override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                    if (hasLocation()) {
                        callback.invoke(origin, true, false)
                    } else {
                        geoPending = origin to callback
                        askPermissions.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                }
            }
        }

        val root = FrameLayout(this).apply {
            setBackgroundColor(BRAND)
            addView(web, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        // Garde l'interface hors des barres système et au-dessus du clavier.
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setContentView(root)

        // Retour du téléphone : l'interface revient d'abord en arrière, puis l'appli se ferme.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("window.appBack?window.appBack():false") { handled ->
                    if (handled != "true") finish()
                }
            }
        })

        // Les rappels ont besoin de l'autorisation des notifications (Android 13 et plus).
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askPermissions.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        Scheduler.reschedule(this)

        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("https://$APP_HOST/assets/www/index.html")
    }

    override fun onResume() {
        super.onResume()
        // L'autorisation des alarmes exactes a pu changer dans les réglages.
        if (::web.isInitialized) web.evaluateJavascript("window.render&&render()", null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    companion object {
        const val APP_HOST = "appassets.androidplatform.net"
        private const val BRAND = 0xFF0B3A29.toInt()
    }
}
