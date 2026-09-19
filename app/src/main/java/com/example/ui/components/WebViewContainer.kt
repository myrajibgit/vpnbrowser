package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adblock.YouTubeAdBlocker
import com.example.model.BrowserTab
import com.example.viewmodel.BrowserViewModel
import com.example.vpn.VpnStatus

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    viewModel: BrowserViewModel,
    tab: BrowserTab,
    isDesktopMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webViewRecreateKey by remember { mutableIntStateOf(0) }
    val currentUrlAtomic = remember { java.util.concurrent.atomic.AtomicReference(tab.url) }

    LaunchedEffect(tab.url) {
        currentUrlAtomic.set(tab.url)
    }

    // Intercept back button if fullscreen video is showing or navigate back/home
    BackHandler(enabled = customView != null || tab.canGoBack || tab.url != "about:home") {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
        } else if (tab.canGoBack) {
            viewModel.goBack()
        } else if (tab.url != "about:home") {
            viewModel.goHome()
        }
    }

    // Observe triggers
    val navTrigger by viewModel.navigationTrigger.collectAsStateWithLifecycle()
    val backTrigger by viewModel.backTrigger.collectAsStateWithLifecycle()
    val forwardTrigger by viewModel.forwardTrigger.collectAsStateWithLifecycle()
    val reloadTrigger by viewModel.reloadTrigger.collectAsStateWithLifecycle()

    fun loadUrlSafely(webView: WebView?, url: String) {
        if (webView == null || url.isBlank() || url == "about:home") return
        webView.tag = url
        val vpn = viewModel.vpnManager.vpnState.value
        if (vpn.status == com.example.vpn.VpnStatus.CONNECTED) {
            val headers = mapOf(
                "X-Forwarded-For" to vpn.activeCountry.virtualIp,
                "Client-IP" to vpn.activeCountry.virtualIp,
                "X-Real-IP" to vpn.activeCountry.virtualIp,
                "Accept-Language" to vpn.activeCountry.locale
            )
            webView.loadUrl(url, headers)
        } else {
            webView.loadUrl(url)
        }
    }

    LaunchedEffect(webViewRef, navTrigger) {
        val trigger = navTrigger
        val view = webViewRef
        if (trigger != null) {
            if (view != null) {
                if (trigger != "about:home") {
                    loadUrlSafely(view, trigger)
                }
                viewModel.onNavigationTriggerConsumed()
            }
        }
    }

    LaunchedEffect(backTrigger) {
        if (backTrigger > 0) {
            val view = webViewRef
            if (view != null && view.canGoBack()) {
                view.goBack()
                viewModel.updateNavigationState(view.canGoBack(), view.canGoForward())
            } else {
                viewModel.goHome()
            }
        }
    }

    LaunchedEffect(forwardTrigger) {
        if (forwardTrigger > 0) {
            val view = webViewRef
            if (view != null && view.canGoForward()) {
                view.goForward()
                viewModel.updateNavigationState(view.canGoBack(), view.canGoForward())
            }
        }
    }

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            webViewRef?.reload()
        }
    }

    LaunchedEffect(isDesktopMode) {
        webViewRef?.let { webView ->
            val settings = webView.settings
            val defaultUa = WebSettings.getDefaultUserAgent(context)
            if (isDesktopMode) {
                settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
            } else {
                settings.userAgentString = defaultUa
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        key(webViewRecreateKey, tab.id) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Use LAYER_TYPE_NONE to avoid offscreen GL buffer allocation that triggers missing Mesa DRI rendernode errors on emulators
                        setLayerType(View.LAYER_TYPE_NONE, null)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = false
                            allowContentAccess = false
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setGeolocationEnabled(true)
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            offscreenPreRaster = false
                        }

                        // Expose YouTube AdBlock JS Interface
                        addJavascriptInterface(
                            viewModel.adBlocker.AndroidAdBlockInterface(),
                            "AndroidAdBlock"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val requestUrl = request?.url?.toString() ?: return false
                                if (requestUrl.startsWith("http://") || requestUrl.startsWith("https://") || requestUrl.startsWith("about:")) {
                                    val vpn = viewModel.vpnManager.vpnState.value
                                    if (vpn.status == com.example.vpn.VpnStatus.CONNECTED) {
                                        val country = vpn.activeCountry
                                        if (requestUrl.contains("google.") && requestUrl.contains("/search") && !requestUrl.contains("gl=")) {
                                            val sep = if (requestUrl.contains("?")) "&" else "?"
                                            val localizedUrl = "$requestUrl${sep}gl=${country.code.lowercase()}&pws=0"
                                            loadUrlSafely(view, localizedUrl)
                                            return true
                                        }
                                    }
                                    return false
                                }
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
                                    view?.context?.startActivity(intent)
                                } catch (e: Exception) {
                                    // ignore
                                }
                                return true
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                try {
                                    // YouTube ad blocker network filter strictly scoped to YouTube
                                    // CRITICAL: Never call methods on 'view' (such as view.getUrl()) here,
                                    // because shouldInterceptRequest is called on Chromium's background thread pool.
                                    val currentUrl = currentUrlAtomic.get()
                                    val blocked = viewModel.adBlocker.shouldIntercept(request, currentUrl)
                                    if (blocked != null) return blocked
                                } catch (e: Throwable) {
                                    // Guard against background thread issues
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean
                            ) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                view?.let {
                                    viewModel.updateNavigationState(
                                        canGoBack = it.canGoBack(),
                                        canGoForward = it.canGoForward()
                                    )
                                }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { safeUrl ->
                                    currentUrlAtomic.set(safeUrl)
                                    viewModel.onPageStarted(
                                        url = safeUrl,
                                        canGoBack = view?.canGoBack() ?: false,
                                        canGoForward = view?.canGoForward() ?: false
                                    )

                                    // Inject VPN spoofing script (location, timezone, locale)
                                    val vpn = viewModel.vpnManager.vpnState.value
                                    if (vpn.status == VpnStatus.CONNECTED && vpn.spoofGpsEnabled) {
                                        val js = viewModel.vpnManager.getSpoofingJavascript(vpn.activeCountry)
                                        view?.evaluateJavascript(js, null)
                                    }

                                    // If on YouTube, inject ad-block script early
                                    if (viewModel.adBlocker.isYouTubeUrl(safeUrl) && viewModel.adBlocker.state.value.isEnabled) {
                                        view?.evaluateJavascript(YouTubeAdBlocker.YOUTUBE_ADBLOCK_INJECTION_JS, null)
                                    }
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { safeUrl ->
                                    viewModel.onPageFinished(
                                        url = safeUrl,
                                        title = view?.title,
                                        canGoBack = view?.canGoBack() ?: false,
                                        canGoForward = view?.canGoForward() ?: false
                                    )

                                    // Re-inject VPN & Adblock scripts on DOM completion
                                    val vpn = viewModel.vpnManager.vpnState.value
                                    if (vpn.status == VpnStatus.CONNECTED && vpn.spoofGpsEnabled) {
                                        val js = viewModel.vpnManager.getSpoofingJavascript(vpn.activeCountry)
                                        view?.evaluateJavascript(js, null)
                                    }

                                    if (viewModel.adBlocker.isYouTubeUrl(safeUrl) && viewModel.adBlocker.state.value.isEnabled) {
                                        view?.evaluateJavascript(YouTubeAdBlocker.YOUTUBE_ADBLOCK_INJECTION_JS, null)
                                    }
                                }
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                // Safely handle renderer termination without crashing the app
                                try {
                                    if (webViewRef == view) {
                                        webViewRef = null
                                    }
                                    view?.let {
                                        (it.parent as? ViewGroup)?.removeView(it)
                                        it.destroy()
                                    }
                                    // Reset loading indicators in tab state
                                    viewModel.onPageFinished(
                                        url = tab.url,
                                        title = tab.title,
                                        canGoBack = false,
                                        canGoForward = false
                                    )
                                    // Recreate fresh WebView instance cleanly
                                    webViewRecreateKey++
                                } catch (e: Throwable) {
                                    // ignore
                                }
                                return true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.onProgressChanged(newProgress)
                            }

                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                // Automatically grant geolocation permission with spoofed coordinates
                                callback?.invoke(origin, true, false)
                            }

                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                super.onShowCustomView(view, callback)
                                if (customView != null) {
                                    callback?.onCustomViewHidden()
                                    return
                                }
                                customView = view
                                customViewCallback = callback
                            }

                            override fun onHideCustomView() {
                                super.onHideCustomView()
                                customView = null
                                customViewCallback?.onCustomViewHidden()
                                customViewCallback = null
                            }
                        }

                        webViewRef = this
                        if (tab.url.isNotBlank() && tab.url != "about:home") {
                            loadUrlSafely(this, tab.url)
                        }
                    }
                },
                update = { view ->
                    webViewRef = view
                    if (tab.url.isNotBlank() && tab.url != "about:home" && (view.url != tab.url && view.tag != tab.url)) {
                        loadUrlSafely(view, tab.url)
                    }
                }
            )
        }

        // Full-screen video view overlay (e.g. YouTube full-screen)
        customView?.let { v ->
            AndroidView(
                factory = { _ ->
                    FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(
                            v,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            )
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            webViewRef?.stopLoading()
        }
    }
}
