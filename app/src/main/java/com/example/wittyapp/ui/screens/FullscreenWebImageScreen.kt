package com.example.wittyapp.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.wittyapp.ui.strings.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenWebImageScreen(
    url: String,
    title: String,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { pad ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()

                    val html = """
                        <html>
                        <head><meta name="viewport" content="width=device-width, initial-scale=1.0"/></head>
                        <body style="margin:0;background:black;display:flex;align-items:center;justify-content:center;">
                          <img src="$url" style="max-width:100vw;max-height:100vh;object-fit:contain;"/>
                        </body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                }
            }
        )
    }
}
