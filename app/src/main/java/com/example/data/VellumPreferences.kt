package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.VellumThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vellumDataStore by preferencesDataStore(name = "vellum_preferences")

enum class SearchEngineOption(
    val id: String,
    val label: String,
    val badge: String,
    val queryTemplate: String,
    val homeUrl: String
) {
    BRAVE(
        id = "BRAVE",
        label = "Brave Search (Default)",
        badge = "Brave",
        queryTemplate = "https://search.brave.com/search?q=%s",
        homeUrl = "https://search.brave.com"
    ),
    DUCKDUCKGO(
        id = "DUCKDUCKGO",
        label = "DuckDuckGo",
        badge = "DDG",
        queryTemplate = "https://duckduckgo.com/?q=%s",
        homeUrl = "https://duckduckgo.com"
    ),
    STARTPAGE(
        id = "STARTPAGE",
        label = "Startpage (Private Google)",
        badge = "SP",
        queryTemplate = "https://www.startpage.com/do/dsearch?query=%s",
        homeUrl = "https://www.startpage.com"
    ),
    WIKIPEDIA(
        id = "WIKIPEDIA",
        label = "Wikipedia Direct",
        badge = "Wiki",
        queryTemplate = "https://en.m.wikipedia.org/w/index.php?search=%s",
        homeUrl = "https://en.m.wikipedia.org"
    );

    companion object {
        fun fromId(id: String?): SearchEngineOption =
            entries.firstOrNull { it.id == id } ?: BRAVE
    }
}

enum class ReaderThemeOption(val label: String) {
    ALABASTER("Alabaster"),
    SEPIA("Warm Sepia"),
    CARBON("Carbon Night")
}

enum class ReaderFontOption(val label: String) {
    SERIF("Newsreader Serif"),
    SANS("Jakarta Sans"),
    MONO("JetBrains Mono")
}

data class UserSettings(
    val themeMode: VellumThemeMode = VellumThemeMode.CARBON_NIGHT,
    val searchEngine: SearchEngineOption = SearchEngineOption.BRAVE,
    val globalShieldEnabled: Boolean = true,
    val strictShieldByDefault: Boolean = false,
    val stripTrackingParams: Boolean = true,
    val hideCookieBanners: Boolean = true,
    val blockPopups: Boolean = true,
    val bottomToolbar: Boolean = true,
    val autoHideToolbarOnScroll: Boolean = true,
    val privateVaultLockEnabled: Boolean = false,
    val privateVaultPin: String = "1234",
    val readerTheme: ReaderThemeOption = ReaderThemeOption.CARBON,
    val readerFont: ReaderFontOption = ReaderFontOption.SERIF,
    val readerFontSizeSp: Int = 18,
    val lifetimeBlockedCount: Long = 142,
    val lifetimeBytesSaved: Long = 3_480_000L,
    val lifetimeParamsStripped: Int = 19
)

class VellumPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val GLOBAL_SHIELD = booleanPreferencesKey("global_shield")
        val STRICT_SHIELD = booleanPreferencesKey("strict_shield")
        val STRIP_TRACKING_PARAMS = booleanPreferencesKey("strip_tracking_params")
        val HIDE_COOKIE_BANNERS = booleanPreferencesKey("hide_cookie_banners")
        val BLOCK_POPUPS = booleanPreferencesKey("block_popups")
        val BOTTOM_TOOLBAR = booleanPreferencesKey("bottom_toolbar")
        val AUTO_HIDE_TOOLBAR = booleanPreferencesKey("auto_hide_toolbar")
        val PRIVATE_VAULT_LOCK = booleanPreferencesKey("private_vault_lock")
        val PRIVATE_VAULT_PIN = stringPreferencesKey("private_vault_pin")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val READER_FONT = stringPreferencesKey("reader_font")
        val READER_FONT_SIZE = intPreferencesKey("reader_font_size")
        val LIFETIME_BLOCKED = longPreferencesKey("lifetime_blocked")
        val LIFETIME_BYTES_SAVED = longPreferencesKey("lifetime_bytes_saved")
        val LIFETIME_PARAMS_STRIPPED = intPreferencesKey("lifetime_params_stripped")
    }

    val settingsFlow: Flow<UserSettings> = context.vellumDataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: VellumThemeMode.CARBON_NIGHT.name
        val themeMode = runCatching { VellumThemeMode.valueOf(themeStr) }
            .getOrDefault(VellumThemeMode.CARBON_NIGHT)
        val readerThemeStr = prefs[Keys.READER_THEME] ?: ReaderThemeOption.CARBON.name
        val readerTheme = runCatching { ReaderThemeOption.valueOf(readerThemeStr) }
            .getOrDefault(ReaderThemeOption.CARBON)
        val readerFontStr = prefs[Keys.READER_FONT] ?: ReaderFontOption.SERIF.name
        val readerFont = runCatching { ReaderFontOption.valueOf(readerFontStr) }
            .getOrDefault(ReaderFontOption.SERIF)

        UserSettings(
            themeMode = themeMode,
            searchEngine = SearchEngineOption.fromId(prefs[Keys.SEARCH_ENGINE]),
            globalShieldEnabled = prefs[Keys.GLOBAL_SHIELD] ?: true,
            strictShieldByDefault = prefs[Keys.STRICT_SHIELD] ?: false,
            stripTrackingParams = prefs[Keys.STRIP_TRACKING_PARAMS] ?: true,
            hideCookieBanners = prefs[Keys.HIDE_COOKIE_BANNERS] ?: true,
            blockPopups = prefs[Keys.BLOCK_POPUPS] ?: true,
            bottomToolbar = prefs[Keys.BOTTOM_TOOLBAR] ?: true,
            autoHideToolbarOnScroll = prefs[Keys.AUTO_HIDE_TOOLBAR] ?: true,
            privateVaultLockEnabled = prefs[Keys.PRIVATE_VAULT_LOCK] ?: false,
            privateVaultPin = prefs[Keys.PRIVATE_VAULT_PIN] ?: "1234",
            readerTheme = readerTheme,
            readerFont = readerFont,
            readerFontSizeSp = prefs[Keys.READER_FONT_SIZE] ?: 18,
            lifetimeBlockedCount = prefs[Keys.LIFETIME_BLOCKED] ?: 142L,
            lifetimeBytesSaved = prefs[Keys.LIFETIME_BYTES_SAVED] ?: 3_480_000L,
            lifetimeParamsStripped = prefs[Keys.LIFETIME_PARAMS_STRIPPED] ?: 19
        )
    }

    suspend fun setThemeMode(mode: VellumThemeMode) {
        context.vellumDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSearchEngine(engine: SearchEngineOption) {
        context.vellumDataStore.edit { it[Keys.SEARCH_ENGINE] = engine.id }
    }

    suspend fun setGlobalShield(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.GLOBAL_SHIELD] = enabled }
    }

    suspend fun setStrictShield(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.STRICT_SHIELD] = enabled }
    }

    suspend fun setStripTrackingParams(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.STRIP_TRACKING_PARAMS] = enabled }
    }

    suspend fun setHideCookieBanners(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.HIDE_COOKIE_BANNERS] = enabled }
    }

    suspend fun setBlockPopups(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.BLOCK_POPUPS] = enabled }
    }

    suspend fun setBottomToolbar(bottom: Boolean) {
        context.vellumDataStore.edit { it[Keys.BOTTOM_TOOLBAR] = bottom }
    }

    suspend fun setAutoHideToolbar(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.AUTO_HIDE_TOOLBAR] = enabled }
    }

    suspend fun setPrivateVaultLock(enabled: Boolean) {
        context.vellumDataStore.edit { it[Keys.PRIVATE_VAULT_LOCK] = enabled }
    }

    suspend fun setReaderTheme(theme: ReaderThemeOption) {
        context.vellumDataStore.edit { it[Keys.READER_THEME] = theme.name }
    }

    suspend fun setReaderFont(font: ReaderFontOption) {
        context.vellumDataStore.edit { it[Keys.READER_FONT] = font.name }
    }

    suspend fun setReaderFontSize(sizeSp: Int) {
        context.vellumDataStore.edit { it[Keys.READER_FONT_SIZE] = sizeSp.coerceIn(14, 28) }
    }

    suspend fun incrementShieldTelemetry(blockedDelta: Int, bytesSavedDelta: Long, paramsStrippedDelta: Int = 0) {
        context.vellumDataStore.edit { prefs ->
            val currentBlocked = prefs[Keys.LIFETIME_BLOCKED] ?: 142L
            val currentBytes = prefs[Keys.LIFETIME_BYTES_SAVED] ?: 3_480_000L
            val currentParams = prefs[Keys.LIFETIME_PARAMS_STRIPPED] ?: 19
            prefs[Keys.LIFETIME_BLOCKED] = currentBlocked + blockedDelta
            prefs[Keys.LIFETIME_BYTES_SAVED] = currentBytes + bytesSavedDelta
            prefs[Keys.LIFETIME_PARAMS_STRIPPED] = currentParams + paramsStrippedDelta
        }
    }
}
