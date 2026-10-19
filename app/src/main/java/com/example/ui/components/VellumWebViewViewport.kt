package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.UrlSanitizerAndSearch
import com.example.engine.VellumShieldEngine
import com.example.viewmodel.BrowserTab
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.WebViewCommand
import kotlin.math.abs

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

class VellumJsBridge(
    private val mainHandler: Handler,
    private val onElementZapped: (String) -> Unit,
    private val onReaderExtracted: (String, String, String) -> Unit
) {
    @JavascriptInterface
    fun onElementZapped(selector: String) {
        mainHandler.post {
            onElementZapped.invoke(selector)
        }
    }

    @JavascriptInterface
    fun onReaderArticleExtracted(title: String, byline: String, joinedParagraphs: String) {
        mainHandler.post {
            onReaderExtracted.invoke(title, byline, joinedParagraphs)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VellumWebViewViewport(
    activeTab: BrowserTab,
    viewModel: BrowserViewModel,
    hideCookieBanners: Boolean,
    stripTrackingParams: Boolean,
    isZapModeActive: Boolean,
    webViewCommand: WebViewCommand?,
    modifier: Modifier = Modifier
) {
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val currentTabState by rememberUpdatedState(activeTab)
    val currentHideCookies by rememberUpdatedState(hideCookieBanners)
    val currentStripParams by rememberUpdatedState(stripTrackingParams)
    val currentViewModel by rememberUpdatedState(viewModel)

    // Persistent single WebView instance across tab switches & Start Page transitions
    // so we never trigger aw_browser_terminator by destroying active renderer processes
    var rendererGeneration by remember { mutableIntStateOf(0) }
    var webViewRef by remember(rendererGeneration) { mutableStateOf<WebView?>(null) }
    var lastLoadedTabId by remember(rendererGeneration) { mutableStateOf("") }
    var lastLoadedUrl by remember(rendererGeneration) { mutableStateOf("") }
    var lastAppliedDesktopMode by remember(rendererGeneration) { mutableStateOf<Boolean?>(null) }

    // Load or synchronize URL when activeTab.url or activeTab.id changes
    LaunchedEffect(webViewRef, activeTab.id, activeTab.url, rendererGeneration) {
        val wv = webViewRef ?: return@LaunchedEffect
        val targetUrl = activeTab.url
        if (targetUrl.isBlank()) {
            // When returning to Start Page, pause media/JS timers instead of destroying the WebView
            wv.stopLoading()
            return@LaunchedEffect
        }
        if (lastLoadedTabId != activeTab.id || lastLoadedUrl != targetUrl) {
            lastLoadedTabId = activeTab.id
            lastLoadedUrl = targetUrl
            if (targetUrl == "vellum://shield-test") {
                val shieldOn = currentViewModel.isShieldEnabledForDomain("vellum://shield-test")
                val html = VellumShieldEngine.buildShieldTestBenchHtml(shieldOn)
                wv.loadDataWithBaseURL(
                    "https://vellum.diagnostic.local/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                wv.loadUrl(targetUrl)
            }
        }
    }

    // Apply Desktop User-Agent only when desktop mode explicitly changes
    LaunchedEffect(webViewRef, activeTab.isDesktopMode) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (lastAppliedDesktopMode != null && lastAppliedDesktopMode != activeTab.isDesktopMode) {
            val defaultUa = WebSettings.getDefaultUserAgent(wv.context)
            wv.settings.userAgentString = if (activeTab.isDesktopMode) DESKTOP_USER_AGENT else defaultUa
            if (activeTab.url.isNotBlank()) {
                wv.reload()
            }
        }
        lastAppliedDesktopMode = activeTab.isDesktopMode
    }

    // Activate or deactivate Zap Distraction element picker in the live DOM
    LaunchedEffect(webViewRef, isZapModeActive) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (activeTab.url.isNotBlank()) {
            val script = VellumShieldEngine.buildZapModeActivationScript(isZapModeActive)
            runCatching { wv.evaluateJavascript(script, null) }
        }
    }

    // Handle explicit WebView commands (Back, Forward, Reload, Reader extraction, Find in page)
    LaunchedEffect(webViewRef, webViewCommand) {
        val wv = webViewRef ?: return@LaunchedEffect
        when (val cmd = webViewCommand) {
            is WebViewCommand.GoBack -> {
                if (wv.canGoBack()) {
                    wv.goBack()
                } else {
                    currentViewModel.returnToStartPage()
                }
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.GoForward -> {
                if (wv.canGoForward()) {
                    wv.goForward()
                }
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.Reload -> {
                if (currentTabState.url == "vellum://shield-test") {
                    val shieldOn = currentViewModel.isShieldEnabledForDomain("vellum://shield-test")
                    val html = VellumShieldEngine.buildShieldTestBenchHtml(shieldOn)
                    wv.loadDataWithBaseURL(
                        "https://vellum.diagnostic.local/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else if (currentTabState.url.isNotBlank()) {
                    wv.reload()
                }
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.ExtractReaderArticle -> {
                wv.evaluateJavascript(
                    VellumShieldEngine.buildReaderExtractionScript(),
                    null
                )
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.FindText -> {
                if (cmd.query.isNotBlank()) {
                    wv.findAllAsync(cmd.query)
                } else {
                    wv.clearMatches()
                }
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.FindNext -> {
                wv.findNext(cmd.forward)
                currentViewModel.consumeWebViewCommand()
            }
            is WebViewCommand.ClearFind -> {
                wv.clearMatches()
                currentViewModel.consumeWebViewCommand()
            }
            null -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        key(rendererGeneration) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Keep renderer process at IMPORTANT priority even when covered by sheets/StartPage
                        // so the OS low-memory killer never SIGKILLs the renderer process (code -1)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadsImagesAutomatically = true
                            mediaPlaybackRequiresUserGesture = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            setSupportMultipleWindows(false)
                            javaScriptCanOpenWindowsAutomatically = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            if (currentTabState.isDesktopMode) {
                                userAgentString = DESKTOP_USER_AGENT
                            }
                        }

                        runCatching {
                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                        }

                        addJavascriptInterface(
                            VellumJsBridge(
                                mainHandler = mainHandler,
                                onElementZapped = { selector ->
                                    currentViewModel.onElementZappedFromPage(selector)
                                },
                                onReaderExtracted = { title, byline, paragraphs ->
                                    currentViewModel.onReaderArticleExtractedFromDom(title, byline, paragraphs)
                                }
                            ),
                            "VellumBridge"
                        )

                        setOnLongClickListener { view ->
                            val hitView = view as? WebView ?: return@setOnLongClickListener false
                            val hitResult = hitView.hitTestResult
                            val extra = hitResult.extra
                            if ((hitResult.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                                    hitResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) &&
                                !extra.isNullOrBlank()
                            ) {
                                currentViewModel.openPeekPreview(extra)
                                true
                            } else {
                                false
                            }
                        }

                        setDownloadListener(
                            DownloadListener { url, _, contentDisposition, mimeType, contentLength ->
                                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                                currentViewModel.recordFileDownload(
                                    fileName = fileName,
                                    url = url,
                                    mimeType = mimeType ?: "application/octet-stream",
                                    contentLength = contentLength
                                )
                            }
                        )

                        webViewClient = object : WebViewClient() {

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val reqUri = request?.url ?: return null
                                if (request.isForMainFrame) return null

                                val tabSnapshot = currentTabState
                                val pageDomain = tabSnapshot.domain
                                val shieldEnabled = currentViewModel.isShieldEnabledForDomain(pageDomain)
                                val strictMode = currentViewModel.isStrictModeForDomain(pageDomain)

                                val decision = VellumShieldEngine.evaluateNetworkRequest(
                                    requestUri = reqUri,
                                    pageUrl = tabSnapshot.url,
                                    shieldEnabled = shieldEnabled,
                                    strictMode = strictMode
                                )

                                if (decision.shouldBlock) {
                                    mainHandler.post {
                                        currentViewModel.onRequestBlocked(
                                            tabId = tabSnapshot.id,
                                            matchedDomain = decision.matchedDomain,
                                            category = decision.category
                                        )
                                    }
                                    return decision.response
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val targetUri = request?.url ?: return false
                                val targetUrl = targetUri.toString()

                                if (targetUrl.startsWith("vellum://shield-test", ignoreCase = true)) {
                                    currentViewModel.navigateToInput("vellum://shield-test")
                                    return true
                                }

                                if (currentStripParams && targetUrl.contains("?")) {
                                    val sanitized = UrlSanitizerAndSearch.sanitizeUrl(targetUrl)
                                    if (sanitized.strippedParamsCount > 0 && sanitized.finalUrl != targetUrl) {
                                        lastLoadedUrl = sanitized.finalUrl
                                        view?.loadUrl(sanitized.finalUrl)
                                        currentViewModel.showBanner(
                                            "Stripped ${sanitized.strippedParamsCount} tracking param(s)"
                                        )
                                        return true
                                    }
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                val tabSnapshot = currentTabState
                                if (tabSnapshot.url.isBlank()) return
                                val effectiveUrl = if (lastLoadedUrl == "vellum://shield-test") {
                                    "vellum://shield-test"
                                } else {
                                    url.orEmpty()
                                }
                                if (effectiveUrl.isNotBlank() && effectiveUrl != "about:blank") {
                                    lastLoadedUrl = effectiveUrl
                                    currentViewModel.onTabUrlAndStateUpdated(
                                        tabId = tabSnapshot.id,
                                        url = effectiveUrl,
                                        title = view?.title,
                                        isLoading = true,
                                        progress = 20,
                                        canGoBack = view?.canGoBack() == true,
                                        canGoForward = view?.canGoForward() == true
                                    )
                                }
                            }

                            override fun onPageCommitVisible(view: WebView?, url: String?) {
                                super.onPageCommitVisible(view, url)
                                injectCosmeticShield(view)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val tabSnapshot = currentTabState
                                if (tabSnapshot.url.isBlank() || url == "about:blank") return
                                injectCosmeticShield(view)
                                val effectiveUrl = if (lastLoadedUrl == "vellum://shield-test") {
                                    "vellum://shield-test"
                                } else {
                                    url.orEmpty()
                                }
                                currentViewModel.onTabUrlAndStateUpdated(
                                    tabId = tabSnapshot.id,
                                    url = effectiveUrl,
                                    title = view?.title,
                                    isLoading = false,
                                    progress = 100,
                                    canGoBack = view?.canGoBack() == true,
                                    canGoForward = view?.canGoForward() == true
                                )
                                currentViewModel.onPageFinishedRecordHistory(
                                    tabId = tabSnapshot.id,
                                    url = effectiveUrl,
                                    title = view?.title
                                )
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                runCatching {
                                    (view?.parent as? ViewGroup)?.removeView(view)
                                    view?.destroy()
                                }
                                webViewRef = null
                                lastLoadedUrl = ""
                                mainHandler.post {
                                    rendererGeneration += 1
                                }
                                return true
                            }

                            private fun injectCosmeticShield(view: WebView?) {
                                val tabSnapshot = currentTabState
                                if (tabSnapshot.url.isBlank()) return
                                val domain = tabSnapshot.domain
                                val shieldOn = currentViewModel.isShieldEnabledForDomain(domain)
                                val zapped = currentViewModel.getZappedSelectorsForDomain(domain)
                                val script = VellumShieldEngine.buildCosmeticInjectionScript(
                                    shieldEnabled = shieldOn,
                                    hideCookieBanners = currentHideCookies,
                                    customZappedSelectors = zapped
                                )
                                if (script.isNotBlank()) {
                                    runCatching { view?.evaluateJavascript(script, null) }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            private var lastReportedProgress = 0

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                val tabSnapshot = currentTabState
                                if (tabSnapshot.url.isBlank()) return
                                if (newProgress == 100 || abs(newProgress - lastReportedProgress) >= 15) {
                                    lastReportedProgress = newProgress
                                    val effectiveUrl = if (lastLoadedUrl == "vellum://shield-test") {
                                        "vellum://shield-test"
                                    } else {
                                        view?.url ?: tabSnapshot.url
                                    }
                                    if (effectiveUrl.isNotBlank() && effectiveUrl != "about:blank") {
                                        currentViewModel.onTabUrlAndStateUpdated(
                                            tabId = tabSnapshot.id,
                                            url = effectiveUrl,
                                            title = view?.title,
                                            isLoading = newProgress < 100,
                                            progress = newProgress,
                                            canGoBack = view?.canGoBack() == true,
                                            canGoForward = view?.canGoForward() == true
                                        )
                                    }
                                }
                            }
                        }
                    }.also { created ->
                        webViewRef = created
                    }
                },
                update = { /* Managed via LaunchedEffects */ },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("browser_webview")
            )
        }

        // Floating HUD Banner when Zap Distraction Mode is active
        if (isZapModeActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Zap Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Zap Mode: Tap any element on the page to hide it permanently",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { viewModel.cancelZapDistractionMode() },
                    modifier = Modifier.testTag("cancel_zap_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancel Zap Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
