package com.mobileandroid.appsnews.ui.Activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.databinding.ActivityCompleteNewsBinding


class CompleteNewsActivity : AppCompatActivity() {

    lateinit var  binding : ActivityCompleteNewsBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        val url = intent.getStringExtra("newsLink")

        Log.d("news url", url.toString())

        binding.toolbarCompleteNews.title=""
        setSupportActionBar(binding.toolbarCompleteNews)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarCompleteNews.setNavigationOnClickListener {
            finish()
        }


        binding.webView.settings.javaScriptEnabled=true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true

        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.defaultFontSize = 14

        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyWebChromeClient()

        binding.webView.loadUrl(url!!)

        setupWebView(url)

    }

    private fun setupWebView(url: String?) {
        val webSettings = binding.webView.settings

        //  Dark/Light Mode settings
        applyDarkMode()

        webSettings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            builtInZoomControls = true
            displayZoomControls = false
            defaultFontSize = 14
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyWebChromeClient()

        url?.let { binding.webView.loadUrl(it) }
    }

    private fun applyDarkMode() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                binding.webView.settings.isAlgorithmicDarkeningAllowed = true
            }

            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

            WebSettingsCompat.setForceDark(
                binding.webView.settings,
                if (isDarkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            )

            //  Force background black in dark mode
            if (isDarkMode) {
                binding.webView.setBackgroundColor(Color.BLACK)
            } else {
                binding.webView.setBackgroundColor(Color.WHITE)
            }
        }
    }



    inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            binding.completeNewsProgressBar.setProgress(newProgress)
            if (newProgress == 100) {
                binding.completeNewsProgressBar.setVisibility(View.GONE)
            } else {
                binding.completeNewsProgressBar.setVisibility(View.VISIBLE)
            }
        }
    }

    private fun handleBackPress() {

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               finish()
            }
        })
    }


//    inner class MyWebViewClient : WebViewClient() {
//
//        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
//            return super.shouldOverrideUrlLoading(view, request)
//        }
//    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

            if (isDarkMode) {
                view?.evaluateJavascript(
                    """
                (function() {
                    document.body.style.backgroundColor = "black";
                    document.body.style.color = "white";
                })();
                """.trimIndent(), null
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.comple_news_menu,menu)
        return true
    }
}