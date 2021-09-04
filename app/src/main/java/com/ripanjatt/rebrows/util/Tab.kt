package com.ripanjatt.rebrows.util

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.helper.widget.Layer
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.databinding.TabLayoutBinding
import com.ripanjatt.rebrows.downloader.Downloader
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class Tab(url: String?, private val activity: Home) {

    private val homePage = "file:///android_asset/index.html"
    private val errorPage = "file:///android_asset/blank.html"
    private val binding = TabLayoutBinding.inflate(activity.layoutInflater)
    private val google = "https://www.google.com/search?q="
    var currentURL = url
    var pageTitle = "Home"

    init {
        setUp()
    }

    fun goBack(): Boolean {
        return if(binding.webView.canGoBack()) {
            binding.webView.goBack()
            true
        } else {
            false
        }
    }

    fun loadCurrent(url: String) {
        currentURL = url
        load()
    }

    fun goHome() {
        binding.webView.loadUrl(homePage)
    }

    fun goForward(): Boolean {
        return if(binding.webView.canGoForward()) {
            binding.webView.goForward()
            true
        } else {
            false
        }
    }

    fun getView(): View {
        return binding.root
    }

    fun speedMode(mode: Boolean) {
        val agent = "Opera/9.80 (J2ME/MIDP; Opera Mini/4.2.14912Mod.By.www.9jamusic.cz.cc/22.387; U; en) Presto/2.5.25 Version/10.54"
        var current = binding.webView.settings.userAgentString
        current = if(mode) {
            current.replace(current.substring(current.indexOf("("), current.indexOf(")") + 1), agent)
        } else {
            null
        }
        binding.webView.settings.userAgentString = current
        binding.webView.settings.useWideViewPort = !mode
        binding.webView.settings.loadWithOverviewMode = !mode
        reload()
    }

    fun getHitTestResults(): WebView.HitTestResult {
        return binding.webView.hitTestResult
    }

    private fun reload() {
        binding.webView.reload()
    }

    private fun load() {
        if(currentURL != null) {
            binding.webView.loadUrl(currentURL.toString())
        } else {
            binding.webView.loadUrl(homePage)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUp() {
        binding.webView.setLayerType(Layer.LAYER_TYPE_HARDWARE, null)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        binding.webView.webViewClient = object: WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if(error != null && (error.errorCode == -2 || error.errorCode == -6)) {
                    binding.webView.loadUrl(errorPage)
                }
                super.onReceivedError(view, request, error)
            }

            override fun onPageStarted(wV: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(wV, url, favicon)
                binding.swipeRefresh.isRefreshing = false
                if(url.toString() != "data:text/html," && url.toString() != homePage && url.toString() != errorPage) {
                    currentURL = url.toString()
                    pageTitle = url.toString()
                    Repository.updateHistory(
                        History(url.toString(), SimpleDateFormat.getDateTimeInstance().format(Date())),
                        activity.applicationContext
                    )
                } else {
                    currentURL = if(url.toString() != errorPage) null else currentURL
                    pageTitle = if(url.toString() != errorPage) "Home" else "Error"
                }
                binding.alpha = 1
                binding.progress = 0
                binding.currentUrl = currentURL
            }
        }
        binding.webView.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(wV: WebView?, newProgress: Int) {
                binding.progress = newProgress
                if(newProgress == 100) {
                    binding.alpha = 0
                }
                super.onProgressChanged(wV, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if(title.toString() != "data:text/html,") {
                    pageTitle = title.toString()
                }
            }
        }
        binding.webView.setDownloadListener { url, _, contentDisposition, mimetype, length ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(fileName)
            builder.setMessage("Size: ${DecimalFormat("0.00").format(length.toDouble() / 1024.0.pow(2))} MB")
            builder.setNegativeButton("Cancel", null)
            builder.setPositiveButton("Download") { _, _ ->
                Repository.updateHistory(
                    History(url.toString(), SimpleDateFormat.getDateTimeInstance().format(Date())),
                    activity.applicationContext
                )
                Downloader.saveFile(url, fileName, mimetype, length, activity)
            }
            builder.show()
        }
        binding.urlInput.setOnKeyListener { _, _, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                if(binding.urlInput.text.toString() != "") {
                    val temp = binding.urlInput.text.toString()
                    currentURL = if(temp.contains(" ") || !temp.contains(".")) {
                        google + temp.replace(" ", "+")
                    } else {
                        temp
                    }
                    if(!currentURL.toString().contains("http")) {
                        currentURL = "https://$currentURL"
                    }
                    load()
                }
            }
            return@setOnKeyListener true
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            load()
        }
        activity.registerForContextMenu(binding.webView)
        speedMode(Util.getSpeedMode(activity.applicationContext))
        load()
    }
}