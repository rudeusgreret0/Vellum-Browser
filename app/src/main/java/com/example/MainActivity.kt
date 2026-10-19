package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.UrlSanitizerAndSearch
import com.example.ui.components.VellumDownloadsVaultSheet
import com.example.ui.components.VellumEditorialReaderOverlay
import com.example.ui.components.VellumFindInPageBar
import com.example.ui.components.VellumLibrarySheet
import com.example.ui.components.VellumOmniboxCommandOverlay
import com.example.ui.components.VellumOverflowMenuSheet
import com.example.ui.components.VellumPeekPreviewSheet
import com.example.ui.components.VellumSettingsSheet
import com.example.ui.components.VellumShieldInspectorSheet
import com.example.ui.components.VellumSpatialTabSwitcherSheet
import com.example.ui.components.VellumStartPage
import com.example.ui.components.VellumUnifiedToolbar
import com.example.ui.components.VellumWebViewViewport
import com.example.ui.theme.VellumTheme
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.LibrarySection
import com.example.viewmodel.WebViewCommand
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.dataString?.let { incomingUrl ->
            if (incomingUrl.startsWith("http://") || incomingUrl.startsWith("https://")) {
                viewModel.navigateToInput(incomingUrl)
            }
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isPrivateMode by viewModel.isPrivateMode.collectAsStateWithLifecycle()

            VellumTheme(
                ambientTheme = settings.ambientTheme,
                isPrivateMode = isPrivateMode
            ) {
                VellumBrowserApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VellumBrowserApp(viewModel: BrowserViewModel) {
    val context = LocalContext.current

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val isPrivateMode by viewModel.isPrivateMode.collectAsStateWithLifecycle()
    val isPrivateVaultUnlocked by viewModel.isPrivateVaultUnlocked.collectAsStateWithLifecycle()
    val speedDials by viewModel.speedDials.collectAsStateWithLifecycle()
    val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val offlineArticles by viewModel.offlineArticles.collectAsStateWithLifecycle()
    val siteRules by viewModel.siteRules.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val recentUploads by viewModel.recentUploads.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val activeLibrarySection by viewModel.activeLibrarySection.collectAsStateWithLifecycle()
    val readerSession by viewModel.readerSession.collectAsStateWithLifecycle()
    val peekSession by viewModel.peekSession.collectAsStateWithLifecycle()
    val isZapModeActive by viewModel.isZapModeActive.collectAsStateWithLifecycle()
    val findInPageVisible by viewModel.findInPageVisible.collectAsStateWithLifecycle()
    val findInPageQuery by viewModel.findInPageQuery.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val webViewCommand by viewModel.webViewCommand.collectAsStateWithLifecycle()

    val activeTab = tabs.firstOrNull { it.id == activeTabId }
        ?: tabs.firstOrNull { it.isPrivate == isPrivateMode }
        ?: tabs.first()

    val tabCountInMode = tabs.count { it.isPrivate == isPrivateMode }
    val shieldEnabledOnSite = viewModel.isShieldEnabledForDomain(activeTab.domain)
    val strictModeOnSite = viewModel.isStrictModeForDomain(activeTab.domain)
    val zappedCountOnSite = viewModel.getZappedSelectorsForDomain(activeTab.domain).size

    LaunchedEffect(statusBannerMessage) {
        if (statusBannerMessage != null) {
            delay(2500)
            viewModel.dismissBanner()
        }
    }

    BackHandler(
        enabled = readerSession != null ||
            peekSession != null ||
            activeSheet != ActiveSheet.NONE ||
            isZapModeActive ||
            findInPageVisible ||
            activeTab.url.isNotBlank()
    ) {
        when {
            readerSession != null -> viewModel.closeReaderMode()
            peekSession != null -> viewModel.closePeekPreview()
            activeSheet != ActiveSheet.NONE -> viewModel.closeSheet()
            isZapModeActive -> viewModel.cancelZapDistractionMode()
            findInPageVisible -> viewModel.setFindInPageVisible(false)
            activeTab.canGoBack -> viewModel.sendWebViewCommand(WebViewCommand.GoBack)
            activeTab.url.isNotBlank() -> viewModel.returnToStartPage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column {
                    if (findInPageVisible) {
                        VellumFindInPageBar(
                            query = findInPageQuery,
                            onQueryChange = { viewModel.updateFindInPageQuery(it) },
                            onFindNext = { viewModel.findNext(it) },
                            onClose = { viewModel.setFindInPageVisible(false) }
                        )
                    } else if (!settings.bottomToolbar) {
                        VellumUnifiedToolbar(
                            activeTab = activeTab,
                            tabCountInMode = tabCountInMode,
                            shieldEnabledOnSite = shieldEnabledOnSite,
                            isBottomBar = false,
                            onOpenShieldSheet = { viewModel.openSheet(ActiveSheet.SHIELD_INSPECTOR) },
                            onOpenOmnibox = { viewModel.openSheet(ActiveSheet.OMNIBOX_EDITOR) },
                            onTriggerReaderMode = { viewModel.triggerReaderModeForActiveTab() },
                            onOpenTabSwitcher = { viewModel.openSheet(ActiveSheet.TAB_SWITCHER) },
                            onOpenOverflowMenu = { viewModel.openSheet(ActiveSheet.OVERFLOW_MENU) },
                            onSwipeAdjacentTab = { viewModel.switchAdjacentTab(it) },
                            onNavigateBack = { viewModel.sendWebViewCommand(WebViewCommand.GoBack) },
                            onRefresh = { viewModel.sendWebViewCommand(WebViewCommand.Reload) }
                        )
                    }
                }
            },
            bottomBar = {
                if (settings.bottomToolbar) {
                    VellumUnifiedToolbar(
                        activeTab = activeTab,
                        tabCountInMode = tabCountInMode,
                        shieldEnabledOnSite = shieldEnabledOnSite,
                        isBottomBar = true,
                        onOpenShieldSheet = { viewModel.openSheet(ActiveSheet.SHIELD_INSPECTOR) },
                        onOpenOmnibox = { viewModel.openSheet(ActiveSheet.OMNIBOX_EDITOR) },
                        onTriggerReaderMode = { viewModel.triggerReaderModeForActiveTab() },
                        onOpenTabSwitcher = { viewModel.openSheet(ActiveSheet.TAB_SWITCHER) },
                        onOpenOverflowMenu = { viewModel.openSheet(ActiveSheet.OVERFLOW_MENU) },
                        onSwipeAdjacentTab = { viewModel.switchAdjacentTab(it) },
                        onNavigateBack = { viewModel.sendWebViewCommand(WebViewCommand.GoBack) },
                        onRefresh = { viewModel.sendWebViewCommand(WebViewCommand.Reload) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .then(
                        if (settings.bottomToolbar && !findInPageVisible) {
                            Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        } else {
                            Modifier
                        }
                    )
            ) {
                VellumWebViewViewport(
                    activeTab = activeTab,
                    viewModel = viewModel,
                    hideCookieBanners = settings.hideCookieBanners,
                    stripTrackingParams = settings.stripTrackingParams,
                    isZapModeActive = isZapModeActive,
                    webViewCommand = webViewCommand
                )

                if (activeTab.url.isBlank()) {
                    VellumStartPage(
                        settings = settings,
                        isPrivateMode = isPrivateMode,
                        speedDials = speedDials,
                        offlineArticles = offlineArticles,
                        onOpenOmnibox = { viewModel.openSheet(ActiveSheet.OMNIBOX_EDITOR) },
                        onNavigateToUrl = { url -> viewModel.navigateToInput(url) },
                        onOpenShieldSheet = { viewModel.openSheet(ActiveSheet.SHIELD_INSPECTOR) },
                        onOpenLibrary = { viewModel.openSheet(ActiveSheet.LIBRARY, LibrarySection.BOOKMARKS) },
                        onOpenDownloads = { viewModel.openSheet(ActiveSheet.DOWNLOADS) },
                        onOpenSettings = { viewModel.openSheet(ActiveSheet.SETTINGS) },
                        onOpenOfflineArticle = { article -> viewModel.openOfflineArticleInReader(article) },
                        onAddSpeedDial = { title, url -> viewModel.addPinnedSpeedDial(title, url) },
                        onDeleteSpeedDial = { id -> viewModel.removeBookmark(id) },
                        onIncineratePrivate = { viewModel.incineratePrivateSession() },
                        onCycleAmbientTheme = { viewModel.cycleAmbientTheme() },
                        onFilesPickedFromStart = { files ->
                            viewModel.onFilesSelectedForUpload(files, fromWebsiteInput = false)
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = activeSheet == ActiveSheet.OMNIBOX_EDITOR,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VellumOmniboxCommandOverlay(
                initialQuery = activeTab.url,
                searchEngine = settings.searchEngine,
                bookmarks = allBookmarks,
                history = history,
                onSubmitQueryOrUrl = { raw -> viewModel.navigateToInput(raw) },
                onDismiss = { viewModel.closeSheet() }
            )
        }

        AnimatedVisibility(
            visible = readerSession != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            readerSession?.let { session ->
                VellumEditorialReaderOverlay(
                    session = session,
                    settings = settings,
                    onSaveOffline = { viewModel.saveCurrentReaderArticleOffline() },
                    onSelectTheme = { viewModel.updateReaderTheme(it) },
                    onSelectFont = { viewModel.updateReaderFont(it) },
                    onChangeFontSize = { viewModel.updateReaderFontSize(it) },
                    onClose = { viewModel.closeReaderMode() }
                )
            }
        }

        when (activeSheet) {
            ActiveSheet.SHIELD_INSPECTOR -> {
                VellumShieldInspectorSheet(
                    activeTab = activeTab,
                    settings = settings,
                    siteShieldEnabled = shieldEnabledOnSite,
                    siteStrictMode = strictModeOnSite,
                    zappedSelectorsCount = zappedCountOnSite,
                    onToggleSiteShield = { viewModel.toggleSiteShield(activeTab.domain) },
                    onToggleSiteStrict = { viewModel.toggleSiteStrictMode(activeTab.domain) },
                    onStartZapMode = { viewModel.startZapDistractionMode() },
                    onResetZappedElements = { viewModel.resetZappedElementsForDomain(activeTab.domain) },
                    onLaunchDiagnosticPage = { viewModel.navigateToInput("vellum://shield-test") },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.TAB_SWITCHER -> {
                val isVaultLocked = settings.privateVaultLockEnabled && !isPrivateVaultUnlocked
                VellumSpatialTabSwitcherSheet(
                    tabs = tabs,
                    activeTabId = activeTab.id,
                    isPrivateMode = isPrivateMode,
                    isPrivateVaultLocked = isVaultLocked,
                    onSelectMode = { viewModel.setPrivateMode(it) },
                    onSelectTab = { viewModel.selectTab(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onCreateNewTab = { viewModel.createNewTab() },
                    onCloseAllInMode = { viewModel.closeAllTabsInMode(isPrivateMode) },
                    onIncineratePrivate = { viewModel.incineratePrivateSession() },
                    onUnlockPrivateVault = { viewModel.unlockPrivateVault(it) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.OVERFLOW_MENU -> {
                VellumOverflowMenuSheet(
                    activeTab = activeTab,
                    isBookmarked = viewModel.isActiveTabBookmarked(),
                    onToggleBookmark = { asSpeedDial -> viewModel.toggleBookmarkForActiveTab(asSpeedDial) },
                    onReturnHome = { viewModel.returnToStartPage() },
                    onOpenLibrary = { viewModel.openSheet(ActiveSheet.LIBRARY, LibrarySection.BOOKMARKS) },
                    onOpenDownloads = { viewModel.openSheet(ActiveSheet.DOWNLOADS) },
                    onOpenReaderView = { viewModel.triggerReaderModeForActiveTab() },
                    onStartZapMode = { viewModel.startZapDistractionMode() },
                    onToggleFindInPage = { viewModel.setFindInPageVisible(true) },
                    onToggleDesktopSite = { viewModel.toggleDesktopMode() },
                    onShareCurrentUrl = {
                        val rawUrl = activeTab.url.ifBlank { settings.searchEngine.homeUrl }
                        val cleanUrl = UrlSanitizerAndSearch.sanitizeUrl(rawUrl).finalUrl
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, cleanUrl)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Link"))
                    },
                    onOpenSettings = { viewModel.openSheet(ActiveSheet.SETTINGS) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.LIBRARY -> {
                VellumLibrarySheet(
                    activeSection = activeLibrarySection,
                    bookmarks = allBookmarks,
                    offlineArticles = offlineArticles,
                    history = history,
                    onSelectSection = { viewModel.setLibrarySection(it) },
                    onOpenUrl = { viewModel.navigateToInput(it) },
                    onOpenOfflineArticle = { viewModel.openOfflineArticleInReader(it) },
                    onDeleteBookmark = { id -> viewModel.removeBookmark(id) },
                    onDeleteOfflineArticle = { id -> viewModel.removeOfflineArticle(id) },
                    onDeleteHistoryItem = { id -> viewModel.removeHistoryItem(id) },
                    onClearHistoryRange = { minutes -> viewModel.clearHistoryByRange(minutes) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.DOWNLOADS -> {
                VellumDownloadsVaultSheet(
                    downloads = downloads,
                    recentUploads = recentUploads,
                    onStartDirectDownload = { url ->
                        viewModel.startRealFileDownload(url = url)
                    },
                    onFilesUploaded = { files ->
                        viewModel.onFilesSelectedForUpload(files, fromWebsiteInput = false)
                    },
                    onDeleteDownload = { id -> viewModel.removeDownloadItem(id) },
                    onClearAllDownloads = { viewModel.clearDownloadsVault() },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.SETTINGS -> {
                VellumSettingsSheet(
                    settings = settings,
                    siteRules = siteRules,
                    onUpdateAmbientTheme = { viewModel.updateAmbientTheme(it) },
                    onUpdateTheme = { viewModel.updateThemeMode(it) },
                    onUpdateSearchEngine = { viewModel.updateSearchEngine(it) },
                    onUpdateGlobalShield = { viewModel.updateGlobalShield(it) },
                    onUpdateStrictShield = { viewModel.updateStrictShield(it) },
                    onUpdateStripParams = { viewModel.updateStripTrackingParams(it) },
                    onUpdateHideCookieBanners = { viewModel.updateHideCookieBanners(it) },
                    onUpdateBottomToolbar = { viewModel.updateBottomToolbar(it) },
                    onUpdatePrivateVaultLock = { viewModel.updatePrivateVaultLock(it) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            ActiveSheet.NONE, ActiveSheet.OMNIBOX_EDITOR -> Unit
        }

        peekSession?.let { peek ->
            VellumPeekPreviewSheet(
                peekSession = peek,
                onPromoteToTab = { viewModel.promotePeekToFullTab() },
                onDownloadUrl = { viewModel.downloadPeekUrl() },
                onDismiss = { viewModel.closePeekPreview() }
            )
        }

        AnimatedVisibility(
            visible = statusBannerMessage != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
        ) {
            statusBannerMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            RoundedCornerShape(99.dp)
                        )
                        .clickable { viewModel.dismissBanner() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("status_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
