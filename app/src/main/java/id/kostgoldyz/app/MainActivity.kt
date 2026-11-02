package id.kostgoldyz.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Pembungkus aplikasi Kost Goldyz.
 * Membuka halaman Kost Goldyz di Claude di dalam aplikasi, lengkap dengan
 * unggah foto bukti transfer, dan membuka WhatsApp / Google Calendar di aplikasinya masing-masing.
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var offline: View
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val cb = fileCallback
        fileCallback = null
        cb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res.resultCode, res.data))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        progress = findViewById(R.id.progress)
        offline = findViewById(R.id.offline)
        findViewById<Button>(R.id.retry).setOnClickListener {
            offline.visibility = View.GONE
            web.reload()
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(web, true)
        }

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = false
            mediaPlaybackRequiresUserGesture = true
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
        }

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Bingkai di dalam halaman (isi aplikasi) selalu dimuat di sini.
                if (!request.isForMainFrame) return false
                val uri = request.url
                if (uri.scheme == "https" && isInternal(uri.host ?: "")) return false
                openExternal(uri)
                return true
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                progress.visibility = View.GONE
                CookieManager.getInstance().flush()
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) offline.visibility = View.VISIBLE
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams,
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                return try {
                    pickFile.launch(fileChooserParams.createIntent())
                    true
                } catch (e: ActivityNotFoundException) {
                    fileCallback = null
                    Toast.makeText(this@MainActivity, "Tidak ada aplikasi galeri untuk memilih foto", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (web.canGoBack()) web.goBack() else finish()
        }

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState)
        } else {
            web.loadUrl(getString(R.string.app_url))
        }
    }

    private fun isInternal(host: String): Boolean =
        host == "claude.ai" || host.endsWith(".claude.ai") ||
            host == "anthropic.com" || host.endsWith(".anthropic.com") ||
            host.endsWith(".claudeusercontent.com")

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka link ini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }
}
