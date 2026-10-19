package com.example.viewmodel

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkEntity
import com.example.data.BrowserRepository
import com.example.data.DownloadItemEntity
import com.example.data.HistoryEntity
import com.example.data.OfflineArticleEntity
import com.example.data.ReaderFontOption
import com.example.data.ReaderThemeOption
import com.example.data.SearchEngineOption
import com.example.data.SiteShieldRuleEntity
import com.example.data.UserSettings
import com.example.data.VellumDatabase
import com.example.data.VellumPreferences
import com.example.engine.BlockedDomainEntry
import com.example.engine.TrackerCategory
import com.example.engine.UrlSanitizerAndSearch
import com.example.ui.theme.VellumThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "", // Empty string = Vellum Start Page
    val title: String = "Start Page",
    val domain: String = "",
    val isPrivate: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val blockedCount: Int = 0,
    val bytesSaved: Long = 0L,
    val strippedParamsCount: Int = 0,
    val blockedDomains: List<BlockedDomainEntry> = emptyList(),
    val isReaderAvailable: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis()
)

enum class ActiveSheet {
    NONE,
    OMNIBOX_EDITOR,
    TAB_SWITCHER,
    SHIELD_INSPECTOR,
    LIBRARY,
    DOWNLOADS,
    SETTINGS,
    OVERFLOW_MENU
}

enum class LibrarySection {
    BOOKMARKS,
    READING_LIST,
    HISTORY
}

data class ReaderArticleSession(
    val url: String,
    val title: String,
    val domain: String,
    val byline: String,
    val paragraphs: List<String>,
    val wordCount: Int,
    val readingMinutes: Int,
    val isSavedOffline: Boolean = false
)

data class PeekPreviewSession(
    val url: String,
    val domain: String,
    val blockedCount: Int = 0
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VellumDatabase.getInstance(application)
    private val preferences = VellumPreferences(application)
    val repository = BrowserRepository(database.browserDao(), preferences)

    val settings: StateFlow<UserSettings> = preferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val speedDials: StateFlow<List<BookmarkEntity>> = repository.speedDials.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VellumDatabase.defaultSpeedDials()
    )

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryEntity>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val offlineArticles: StateFlow<List<OfflineArticleEntity>> = repository.offlineArticles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val siteRules: StateFlow<List<SiteShieldRuleEntity>> = repository.siteRules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloads: StateFlow<List<DownloadItemEntity>> = repository.downloads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val initialTab = BrowserTab(id = "tab-initial-1", isPrivate = false)
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode: StateFlow<Boolean> = _isPrivateMode.asStateFlow()

    private val _isPrivateVaultUnlocked = MutableStateFlow(false)
    val isPrivateVaultUnlocked: StateFlow<Boolean> = _isPrivateVaultUnlocked.asStateFlow()

    private val _activeSheet = MutableStateFlow(ActiveSheet.NONE)
    val activeSheet: StateFlow<ActiveSheet> = _activeSheet.asStateFlow()

    private val _activeLibrarySection = MutableStateFlow(LibrarySection.BOOKMARKS)
    val activeLibrarySection: StateFlow<LibrarySection> = _activeLibrarySection.asStateFlow()

    private val _readerSession = MutableStateFlow<ReaderArticleSession?>(null)
    val readerSession: StateFlow<ReaderArticleSession?> = _readerSession.asStateFlow()

    private val _peekSession = MutableStateFlow<PeekPreviewSession?>(null)
    val peekSession: StateFlow<PeekPreviewSession?> = _peekSession.asStateFlow()

    private val _isZapModeActive = MutableStateFlow(false)
    val isZapModeActive: StateFlow<Boolean> = _isZapModeActive.asStateFlow()

    private val _findInPageVisible = MutableStateFlow(false)
    val findInPageVisible: StateFlow<Boolean> = _findInPageVisible.asStateFlow()

    private val _findInPageQuery = MutableStateFlow("")
    val findInPageQuery: StateFlow<String> = _findInPageQuery.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Event counter to trigger WebView reload or commands
    private val _webViewCommand = MutableStateFlow<WebViewCommand?>(null)
    val webViewCommand: StateFlow<WebViewCommand?> = _webViewCommand.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun showBanner(message: String) {
        _statusBannerMessage.value = message
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    fun openSheet(sheet: ActiveSheet, librarySection: LibrarySection? = null) {
        if (librarySection != null) {
            _activeLibrarySection.value = librarySection
        }
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.NONE
    }

    fun setLibrarySection(section: LibrarySection) {
        _activeLibrarySection.value = section
    }

    // --- Tab & Navigation Operations ---

    fun getActiveTab(): BrowserTab {
        val currentId = _activeTabId.value
        return _tabs.value.firstOrNull { it.id == currentId }
            ?: _tabs.value.firstOrNull { it.isPrivate == _isPrivateMode.value }
            ?: _tabs.value.first()
    }

    fun navigateToInput(rawInput: String) {
        val currentSettings = settings.value
        val resolution = UrlSanitizerAndSearch.resolveInputToNavigation(
            rawInput = rawInput,
            searchEngine = currentSettings.searchEngine,
            stripTrackingParams = currentSettings.stripTrackingParams
        )
        _activeSheet.value = ActiveSheet.NONE
        _readerSession.value = null
        _isZapModeActive.value = false

        val domain = UrlSanitizerAndSearch.formatCleanDisplayDomain(resolution.finalUrl)
        val activeId = getActiveTab().id

        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        url = resolution.finalUrl,
                        title = domain.ifBlank { resolution.finalUrl },
                        domain = domain,
                        isLoading = true,
                        progress = 15,
                        blockedCount = 0,
                        bytesSaved = 0L,
                        strippedParamsCount = tab.strippedParamsCount + resolution.strippedParamsCount,
                        blockedDomains = emptyList(),
                        lastAccessedAt = System.currentTimeMillis()
                    )
                } else tab
            }
        }

        if (resolution.strippedParamsCount > 0) {
            viewModelScope.launch {
                preferences.incrementShieldTelemetry(0, 0L, resolution.strippedParamsCount)
            }
            showBanner("Stripped ${resolution.strippedParamsCount} tracking parameter(s) from URL")
        }
    }

    fun returnToStartPage() {
        val activeId = getActiveTab().id
        _readerSession.value = null
        _isZapModeActive.value = false
        _findInPageVisible.value = false
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        url = "",
                        title = "Start Page",
                        domain = "",
                        isLoading = false,
                        progress = 0,
                        blockedCount = 0,
                        blockedDomains = emptyList()
                    )
                } else tab
            }
        }
    }

    fun createNewTab(url: String = "", isPrivate: Boolean = _isPrivateMode.value, selectImmediately: Boolean = true) {
        val domain = if (url.isBlank()) "" else UrlSanitizerAndSearch.formatCleanDisplayDomain(url)
        val newTab = BrowserTab(
            url = url,
            title = if (url.isBlank()) "Start Page" else domain,
            domain = domain,
            isPrivate = isPrivate,
            lastAccessedAt = System.currentTimeMillis()
        )
        _tabs.update { it + newTab }
        if (selectImmediately) {
            _isPrivateMode.value = isPrivate
            _activeTabId.value = newTab.id
            _activeSheet.value = ActiveSheet.NONE
            _readerSession.value = null
        }
    }

    fun selectTab(tabId: String) {
        val target = _tabs.value.firstOrNull { it.id == tabId } ?: return
        _isPrivateMode.value = target.isPrivate
        _activeTabId.value = target.id
        _tabs.update { list ->
            list.map {
                if (it.id == tabId) it.copy(lastAccessedAt = System.currentTimeMillis()) else it
            }
        }
        _activeSheet.value = ActiveSheet.NONE
        _readerSession.value = null
    }

    fun switchAdjacentTab(direction: Int) {
        val modeTabs = _tabs.value.filter { it.isPrivate == _isPrivateMode.value }
        if (modeTabs.size <= 1) return
        val currentIdx = modeTabs.indexOfFirst { it.id == _activeTabId.value }.coerceAtLeast(0)
        val nextIdx = (currentIdx + direction).mod(modeTabs.size)
        val nextTab = modeTabs[nextIdx]
        _activeTabId.value = nextTab.id
        _tabs.update { list ->
            list.map {
                if (it.id == nextTab.id) it.copy(lastAccessedAt = System.currentTimeMillis()) else it
            }
        }
        _readerSession.value = null
        showBanner("Tab ${nextIdx + 1} of ${modeTabs.size} · ${nextTab.title.take(24)}")
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        val closingTab = currentList.firstOrNull { it.id == tabId } ?: return
        val remaining = currentList.filterNot { it.id == tabId }
        val remainingInSameMode = remaining.filter { it.isPrivate == closingTab.isPrivate }

        if (remainingInSameMode.isEmpty()) {
            val replacement = BrowserTab(isPrivate = closingTab.isPrivate)
            _tabs.value = remaining + replacement
            if (_activeTabId.value == tabId) {
                _activeTabId.value = replacement.id
            }
        } else {
            _tabs.value = remaining
            if (_activeTabId.value == tabId) {
                _activeTabId.value = remainingInSameMode.last().id
            }
        }
    }

    fun closeAllTabsInMode(isPrivate: Boolean) {
        val kept = _tabs.value.filter { it.isPrivate != isPrivate }
        val fresh = BrowserTab(isPrivate = isPrivate)
        _tabs.value = kept + fresh
        if (_isPrivateMode.value == isPrivate) {
            _activeTabId.value = fresh.id
        }
    }

    fun setPrivateMode(privateMode: Boolean) {
        _isPrivateMode.value = privateMode
        val modeTabs = _tabs.value.filter { it.isPrivate == privateMode }
        if (modeTabs.isEmpty()) {
            val fresh = BrowserTab(isPrivate = privateMode)
            _tabs.update { it + fresh }
            _activeTabId.value = fresh.id
        } else if (_tabs.value.firstOrNull { it.id == _activeTabId.value }?.isPrivate != privateMode) {
            _activeTabId.value = modeTabs.last().id
        }
    }

    fun unlockPrivateVault(enteredPin: String): Boolean {
        val expected = settings.value.privateVaultPin
        return if (enteredPin == expected || enteredPin == "BIOMETRIC_OK") {
            _isPrivateVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun incineratePrivateSession() {
        runCatching {
            CookieManager.getInstance().removeSessionCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
        }
        val standardTabs = _tabs.value.filter { !it.isPrivate }
        val freshPrivate = BrowserTab(isPrivate = true)
        _tabs.value = standardTabs + freshPrivate
        if (_isPrivateMode.value) {
            _activeTabId.value = freshPrivate.id
        }
        _readerSession.value = null
        _peekSession.value = null
        showBanner("Private Vault incinerated · All ephemeral tabs & cookies wiped")
    }

    // --- WebView Telemetry & State Updates ---

    fun onTabUrlAndStateUpdated(
        tabId: String,
        url: String,
        title: String?,
        isLoading: Boolean,
        progress: Int,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        val domain = UrlSanitizerAndSearch.formatCleanDisplayDomain(url)
        val resolvedTitle = when {
            url.isBlank() -> "Start Page"
            !title.isNullOrBlank() && !title.startsWith("http") -> title
            domain.isNotBlank() -> domain
            else -> url
        }
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        url = url,
                        title = resolvedTitle,
                        domain = domain,
                        isLoading = isLoading,
                        progress = progress,
                        canGoBack = canGoBack,
                        canGoForward = canGoForward,
                        isReaderAvailable = url.isNotBlank()
                    )
                } else tab
            }
        }
    }

    fun onPageFinishedRecordHistory(tabId: String, url: String, title: String?) {
        val tab = _tabs.value.firstOrNull { it.id == tabId } ?: return
        if (tab.isPrivate || url.isBlank() || url == "about:blank") return
        val domain = UrlSanitizerAndSearch.formatCleanDisplayDomain(url)
        viewModelScope.launch {
            repository.recordHistory(
                url = url,
                title = title ?: tab.title,
                domain = domain,
                blockedCount = tab.blockedCount
            )
        }
    }

    fun onRequestBlocked(tabId: String, matchedDomain: String, category: TrackerCategory) {
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == tabId) {
                    val existing = tab.blockedDomains.firstOrNull { it.domain == matchedDomain }
                    val updatedDomains = if (existing != null) {
                        tab.blockedDomains.map {
                            if (it.domain == matchedDomain) {
                                it.copy(count = it.count + 1, lastTimestamp = System.currentTimeMillis())
                            } else it
                        }
                    } else {
                        listOf(BlockedDomainEntry(domain = matchedDomain, category = category, count = 1)) + tab.blockedDomains
                    }
                    tab.copy(
                        blockedCount = tab.blockedCount + 1,
                        bytesSaved = tab.bytesSaved + category.estimatedBytes,
                        blockedDomains = updatedDomains.sortedByDescending { it.count }
                    )
                } else tab
            }
        }
        viewModelScope.launch {
            preferences.incrementShieldTelemetry(
                blockedDelta = 1,
                bytesSavedDelta = category.estimatedBytes
            )
        }
    }

    // --- Shield Per-Site & Zap Distraction Controls ---

    fun isShieldEnabledForDomain(domain: String): Boolean {
        val global = settings.value.globalShieldEnabled
        if (!global) return false
        val rule = siteRules.value.firstOrNull { it.domain.equals(domain, ignoreCase = true) }
        return rule?.shieldEnabled ?: true
    }

    fun isStrictModeForDomain(domain: String): Boolean {
        val rule = siteRules.value.firstOrNull { it.domain.equals(domain, ignoreCase = true) }
        return rule?.strictMode ?: settings.value.strictShieldByDefault
    }

    fun getZappedSelectorsForDomain(domain: String): List<String> {
        val rule = siteRules.value.firstOrNull { it.domain.equals(domain, ignoreCase = true) }
        return rule?.zappedSelectors ?: emptyList()
    }

    fun toggleSiteShield(domain: String) {
        if (domain.isBlank()) return
        val currentlyEnabled = isShieldEnabledForDomain(domain)
        viewModelScope.launch {
            repository.setSiteShieldEnabled(domain, !currentlyEnabled)
            sendWebViewCommand(WebViewCommand.Reload)
            showBanner(if (!currentlyEnabled) "Shield active on $domain" else "Shield paused on $domain")
        }
    }

    fun toggleSiteStrictMode(domain: String) {
        if (domain.isBlank()) return
        val currentlyStrict = isStrictModeForDomain(domain)
        viewModelScope.launch {
            repository.setSiteStrictMode(domain, !currentlyStrict)
            sendWebViewCommand(WebViewCommand.Reload)
        }
    }

    fun startZapDistractionMode() {
        _activeSheet.value = ActiveSheet.NONE
        _isZapModeActive.value = true
        showBanner("Tap any annoying element on the page to vaporize it")
    }

    fun cancelZapDistractionMode() {
        _isZapModeActive.value = false
    }

    fun onElementZappedFromPage(cssSelector: String) {
        _isZapModeActive.value = false
        val domain = getActiveTab().domain
        if (domain.isNotBlank() && cssSelector.isNotBlank()) {
            viewModelScope.launch {
                repository.addZappedSelectorForSite(domain, cssSelector)
                showBanner("Distraction hidden permanently on $domain")
            }
        }
    }

    fun resetZappedElementsForDomain(domain: String) {
        viewModelScope.launch {
            repository.clearZappedSelectorsForSite(domain)
            sendWebViewCommand(WebViewCommand.Reload)
            showBanner("Restored hidden elements on $domain")
        }
    }

    // --- Reader View & Offline Articles ---

    fun triggerReaderModeForActiveTab() {
        val active = getActiveTab()
        if (active.url.isBlank()) {
            // Open our signature Editorial Manifesto if invoked on Start Page
            val defaultArticle = VellumDatabase.defaultOfflineArticles().first()
            openOfflineArticleInReader(defaultArticle)
            return
        }
        sendWebViewCommand(WebViewCommand.ExtractReaderArticle)
    }

    fun onReaderArticleExtractedFromDom(title: String, byline: String, joinedParagraphs: String) {
        val active = getActiveTab()
        val rawParagraphs = joinedParagraphs.split("\n\n").map { it.trim() }.filter { it.isNotBlank() }
        val finalParagraphs = if (rawParagraphs.isNotEmpty()) {
            rawParagraphs
        } else {
            listOf(
                "Vellum Reader View distilled the primary text of ${active.domain.ifBlank { active.url }}.",
                "This page uses a dynamic layout or application shell with minimal static paragraph elements. You can still save it to your Offline Reading List or adjust typography settings below."
            )
        }
        val words = finalParagraphs.sumOf { it.split(Regex("\\s+")).size }
        val minutes = (words / 200).coerceAtLeast(1)
        val isSaved = offlineArticles.value.any { it.url.equals(active.url, ignoreCase = true) }

        _readerSession.value = ReaderArticleSession(
            url = active.url,
            title = title.ifBlank { active.title },
            domain = active.domain.ifBlank { "vellum.reader" },
            byline = byline.ifBlank { active.domain },
            paragraphs = finalParagraphs,
            wordCount = words,
            readingMinutes = minutes,
            isSavedOffline = isSaved
        )
    }

    fun openOfflineArticleInReader(article: OfflineArticleEntity) {
        _activeSheet.value = ActiveSheet.NONE
        _readerSession.value = ReaderArticleSession(
            url = article.url,
            title = article.title,
            domain = article.domain,
            byline = article.authorOrByline.ifBlank { article.domain },
            paragraphs = article.paragraphsJoined.split("\n\n").filter { it.isNotBlank() },
            wordCount = article.wordCount,
            readingMinutes = article.readingMinutes,
            isSavedOffline = true
        )
    }

    fun closeReaderMode() {
        _readerSession.value = null
    }

    fun saveCurrentReaderArticleOffline() {
        val current = _readerSession.value ?: return
        viewModelScope.launch {
            repository.saveOfflineArticle(
                OfflineArticleEntity(
                    url = current.url,
                    title = current.title,
                    domain = current.domain,
                    authorOrByline = current.byline,
                    excerpt = current.paragraphs.firstOrNull()?.take(140) ?: current.title,
                    paragraphsJoined = current.paragraphs.joinToString("\n\n"),
                    wordCount = current.wordCount,
                    readingMinutes = current.readingMinutes
                )
            )
            _readerSession.update { it?.copy(isSavedOffline = true) }
            showBanner("Saved to Offline Reading List")
        }
    }

    // --- Link Peek Preview ---

    fun openPeekPreview(url: String) {
        if (url.isBlank()) return
        val sanitized = if (settings.value.stripTrackingParams) {
            UrlSanitizerAndSearch.sanitizeUrl(url).finalUrl
        } else url
        _peekSession.value = PeekPreviewSession(
            url = sanitized,
            domain = UrlSanitizerAndSearch.formatCleanDisplayDomain(sanitized)
        )
    }

    fun promotePeekToFullTab() {
        val peek = _peekSession.value ?: return
        _peekSession.value = null
        createNewTab(url = peek.url, isPrivate = _isPrivateMode.value, selectImmediately = true)
    }

    fun closePeekPreview() {
        _peekSession.value = null
    }

    // --- Bookmarks, Desktop Mode, Find in Page & Downloads ---

    fun toggleBookmarkForActiveTab(asSpeedDial: Boolean = false) {
        val active = getActiveTab()
        if (active.url.isBlank()) return
        viewModelScope.launch {
            val added = repository.toggleBookmark(
                url = active.url,
                title = active.title,
                domain = active.domain,
                isSpeedDial = asSpeedDial
            )
            showBanner(if (added) "Saved to Library" else "Removed from Library")
        }
    }

    fun isActiveTabBookmarked(): Boolean {
        val active = getActiveTab()
        if (active.url.isBlank()) return false
        return allBookmarks.value.any { it.url.equals(active.url, ignoreCase = true) }
    }

    fun toggleDesktopMode() {
        val activeId = getActiveTab().id
        _tabs.update { list ->
            list.map {
                if (it.id == activeId) it.copy(isDesktopMode = !it.isDesktopMode) else it
            }
        }
        sendWebViewCommand(WebViewCommand.Reload)
    }

    fun setFindInPageVisible(visible: Boolean) {
        _findInPageVisible.value = visible
        if (!visible) {
            _findInPageQuery.value = ""
            sendWebViewCommand(WebViewCommand.ClearFind)
        }
    }

    fun updateFindInPageQuery(query: String) {
        _findInPageQuery.value = query
        sendWebViewCommand(WebViewCommand.FindText(query))
    }

    fun findNext(forward: Boolean) {
        sendWebViewCommand(WebViewCommand.FindNext(forward))
    }

    fun recordFileDownload(fileName: String, url: String, mimeType: String, contentLength: Long) {
        viewModelScope.launch {
            repository.recordDownload(fileName, url, mimeType, contentLength)
            showBanner("Saved $fileName to Downloads Vault")
        }
    }

    fun addPinnedSpeedDial(title: String, url: String) {
        viewModelScope.launch {
            repository.addSpeedDial(title, url)
            showBanner("Pinned $title to Start Page")
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun removeOfflineArticle(id: Long) {
        viewModelScope.launch {
            repository.deleteOfflineArticle(id)
        }
    }

    fun removeHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearHistoryByRange(minutes: Int?) {
        viewModelScope.launch {
            repository.clearHistoryRange(minutes)
            showBanner("Browsing history cleared")
        }
    }

    fun removeDownloadItem(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearDownloadsVault() {
        viewModelScope.launch {
            repository.clearAllDownloads()
        }
    }

    // --- Preferences Updates ---

    fun updateThemeMode(mode: VellumThemeMode) = viewModelScope.launch {
        preferences.setThemeMode(mode)
    }

    fun updateSearchEngine(engine: SearchEngineOption) = viewModelScope.launch {
        preferences.setSearchEngine(engine)
    }

    fun updateGlobalShield(enabled: Boolean) = viewModelScope.launch {
        preferences.setGlobalShield(enabled)
        sendWebViewCommand(WebViewCommand.Reload)
    }

    fun updateStrictShield(enabled: Boolean) = viewModelScope.launch {
        preferences.setStrictShield(enabled)
        sendWebViewCommand(WebViewCommand.Reload)
    }

    fun updateStripTrackingParams(enabled: Boolean) = viewModelScope.launch {
        preferences.setStripTrackingParams(enabled)
    }

    fun updateHideCookieBanners(enabled: Boolean) = viewModelScope.launch {
        preferences.setHideCookieBanners(enabled)
        sendWebViewCommand(WebViewCommand.Reload)
    }

    fun updateBottomToolbar(bottom: Boolean) = viewModelScope.launch {
        preferences.setBottomToolbar(bottom)
    }

    fun updatePrivateVaultLock(enabled: Boolean) = viewModelScope.launch {
        preferences.setPrivateVaultLock(enabled)
    }

    fun updateReaderTheme(theme: ReaderThemeOption) = viewModelScope.launch {
        preferences.setReaderTheme(theme)
    }

    fun updateReaderFont(font: ReaderFontOption) = viewModelScope.launch {
        preferences.setReaderFont(font)
    }

    fun updateReaderFontSize(sizeSp: Int) = viewModelScope.launch {
        preferences.setReaderFontSize(sizeSp)
    }

    fun sendWebViewCommand(command: WebViewCommand) {
        _webViewCommand.value = command
    }

    fun consumeWebViewCommand() {
        _webViewCommand.value = null
    }
}

sealed class WebViewCommand {
    data object GoBack : WebViewCommand()
    data object GoForward : WebViewCommand()
    data object Reload : WebViewCommand()
    data object ExtractReaderArticle : WebViewCommand()
    data class FindText(val query: String) : WebViewCommand()
    data class FindNext(val forward: Boolean) : WebViewCommand()
    data object ClearFind : WebViewCommand()
}
