package com.musichub.app.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.musichub.app.R
import com.musichub.app.databinding.ActivityWebViewBinding
import android.content.Intent
import android.net.Uri


class WebViewActivity : AppCompatActivity() {
    lateinit var binding:ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_web_view)
        initView()
    }

    private fun initView() {
        val url = intent.getStringExtra("url")

        binding.webView.settings.userAgentString=System.getProperty("http.agent")+"MusicHub"
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("com.musichub.app://genius")) {
                    val uri: Uri =
                        Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                binding.loading = true
                view.settings.javaScriptEnabled = true
                view.settings.domStorageEnabled = true
                view.loadUrl(url)
                return false
            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                if (view.progress < 100) {
                    binding.progressBar8.progress = view.progress
                } else {
                    binding.progressBar8.visibility = View.GONE
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
        binding.webView.loadUrl(url!!)

    }
}