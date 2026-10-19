package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.DownloadItemEntity
import com.example.data.HistoryEntity
import com.example.data.OfflineArticleEntity
import com.example.data.SearchEngineOption
import com.example.data.SiteShieldRuleEntity
import com.example.data.UserSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NewsreaderSerifFamily
import com.example.ui.theme.VellumThemeMode
import com.example.viewmodel.LibrarySection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumLibrarySheet(
    activeSection: LibrarySection,
    bookmarks: List<BookmarkEntity>,
    offlineArticles: List<OfflineArticleEntity>,
    history: List<HistoryEntity>,
    onSelectSection: (LibrarySection) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenOfflineArticle: (OfflineArticleEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDeleteOfflineArticle: (Long) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistoryRange: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchFilter by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("library_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Vellum Library",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Segmented Section Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LibraryTabButton(
                    label = "Saved (${bookmarks.size})",
                    selected = activeSection == LibrarySection.BOOKMARKS,
                    onClick = { onSelectSection(LibrarySection.BOOKMARKS) },
                    modifier = Modifier.weight(1f)
                )
                LibraryTabButton(
                    label = "Reading (${offlineArticles.size})",
                    selected = activeSection == LibrarySection.READING_LIST,
                    onClick = { onSelectSection(LibrarySection.READING_LIST) },
                    modifier = Modifier.weight(1f)
                )
                LibraryTabButton(
                    label = "History (${history.size})",
                    selected = activeSection == LibrarySection.HISTORY,
                    onClick = { onSelectSection(LibrarySection.HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                placeholder = { Text("Filter library items…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            when (activeSection) {
                LibrarySection.BOOKMARKS -> {
                    val filtered = remember(bookmarks, searchFilter) {
                        val q = searchFilter.trim().lowercase()
                        if (q.isEmpty()) bookmarks
                        else bookmarks.filter {
                            it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { bm ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onDismiss()
                                        onOpenUrl(bm.url)
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = bm.monogramLetter,
                                                fontFamily = NewsreaderSerifFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bm.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = bm.domain,
                                                fontFamily = JetBrainsMonoFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete Bookmark",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LibrarySection.READING_LIST -> {
                    val filtered = remember(offlineArticles, searchFilter) {
                        val q = searchFilter.trim().lowercase()
                        if (q.isEmpty()) offlineArticles
                        else offlineArticles.filter {
                            it.title.lowercase().contains(q) || it.excerpt.lowercase().contains(q)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.id }) { article ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenOfflineArticle(article) }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${article.domain.uppercase(Locale.US)} · ${article.readingMinutes} MIN READ",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = article.title,
                                            fontFamily = NewsreaderSerifFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = article.excerpt,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { onDeleteOfflineArticle(article.id) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete Offline Article",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LibrarySection.HISTORY -> {
                    // Quick Clear Range Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clear:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { onClearHistoryRange(15) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("15m", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onClearHistoryRange(60) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("1h", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onClearHistoryRange(1440) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("24h", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onClearHistoryRange(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_all_history_button")
                        ) {
                            Text("All", fontSize = 12.sp)
                        }
                    }

                    val filtered = remember(history, searchFilter) {
                        val q = searchFilter.trim().lowercase()
                        if (q.isEmpty()) history
                        else history.filter {
                            it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
                        }
                    }

                    val timeFormat = remember { SimpleDateFormat("HH:mm · MMM d", Locale.US) }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onDismiss()
                                        onOpenUrl(item.url)
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.domain} · ${timeFormat.format(Date(item.visitedAt))}",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteHistoryItem(item.id) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete History Item",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumDownloadsVaultSheet(
    downloads: List<DownloadItemEntity>,
    onDeleteDownload: (Long) -> Unit,
    onClearAllDownloads: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = listOf("All", "Documents", "Images", "Media", "Archives")
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = remember(downloads, selectedCategory) {
        if (selectedCategory == "All") downloads
        else downloads.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("downloads_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Downloads Vault",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Categorized artifacts with tabular size verification",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (downloads.isNotEmpty()) {
                    TextButton(onClick = onClearAllDownloads) {
                        Text("Clear List")
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    val mbString = remember(item.totalBytes) {
                        String.format(Locale.US, "%.2f MB", item.totalBytes / 1_000_000f)
                    }
                    val icon = when (item.category) {
                        "Images" -> Icons.Outlined.Image
                        "Media" -> Icons.Outlined.Movie
                        "Archives" -> Icons.Outlined.FolderZip
                        else -> Icons.Outlined.Description
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.category} · $mbString · ${item.domain} · ${item.status}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteDownload(item.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete Download",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumSettingsSheet(
    settings: UserSettings,
    siteRules: List<SiteShieldRuleEntity>,
    onUpdateTheme: (VellumThemeMode) -> Unit,
    onUpdateSearchEngine: (SearchEngineOption) -> Unit,
    onUpdateGlobalShield: (Boolean) -> Unit,
    onUpdateStrictShield: (Boolean) -> Unit,
    onUpdateStripParams: (Boolean) -> Unit,
    onUpdateHideCookieBanners: (Boolean) -> Unit,
    onUpdateBottomToolbar: (Boolean) -> Unit,
    onUpdatePrivateVaultLock: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
                .testTag("settings_sheet"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vellum Preferences",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Craftsmanship, privacy, and ergonomics without bloat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Visual Palette & Surface Theme
            item {
                PreferenceSectionCard(title = "DESIGN PALETTE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.themeMode == VellumThemeMode.CARBON_NIGHT,
                            onClick = { onUpdateTheme(VellumThemeMode.CARBON_NIGHT) },
                            label = { Text("Carbon & Brass") },
                            modifier = Modifier.testTag("theme_carbon_chip")
                        )
                        FilterChip(
                            selected = settings.themeMode == VellumThemeMode.ALABASTER_DAY,
                            onClick = { onUpdateTheme(VellumThemeMode.ALABASTER_DAY) },
                            label = { Text("Alabaster & Ink") },
                            modifier = Modifier.testTag("theme_alabaster_chip")
                        )
                        FilterChip(
                            selected = settings.themeMode == VellumThemeMode.SYSTEM,
                            onClick = { onUpdateTheme(VellumThemeMode.SYSTEM) },
                            label = { Text("System") }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    ToggleSettingRow(
                        title = "Bottom-Anchored Omnibox Dock",
                        subtitle = "Keep URL bar, Shield, and Tabs within one-thumb reach at the bottom",
                        checked = settings.bottomToolbar,
                        onCheckedChange = onUpdateBottomToolbar
                    )
                }
            }

            // 2. Search Engine (Brave Default)
            item {
                PreferenceSectionCard(title = "DEFAULT SEARCH ENGINE") {
                    SearchEngineOption.entries.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUpdateSearchEngine(engine) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = engine.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = engine.homeUrl,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = settings.searchEngine == engine,
                                onClick = { onUpdateSearchEngine(engine) }
                            )
                        }
                    }
                }
            }

            // 3. Three-Stage Native Ad & Tracker Shield
            item {
                PreferenceSectionCard(title = "NATIVE AD & TRACKER SHIELD") {
                    ToggleSettingRow(
                        title = "Master Network & Cosmetic Shield",
                        subtitle = "Block ad servers, behavioral analytics, and collapse empty DOM ad slots",
                        checked = settings.globalShieldEnabled,
                        onCheckedChange = onUpdateGlobalShield
                    )
                    ToggleSettingRow(
                        title = "Aggressive Strict Mode by Default",
                        subtitle = "Block cross-origin third-party beacons, fingerprinting, and social pixels",
                        checked = settings.strictShieldByDefault,
                        onCheckedChange = onUpdateStrictShield
                    )
                    ToggleSettingRow(
                        title = "Strip URL Tracking Tokens",
                        subtitle = "Automatically remove utm_*, fbclid, gclid, and msclkid on navigation & share",
                        checked = settings.stripTrackingParams,
                        onCheckedChange = onUpdateStripParams
                    )
                    ToggleSettingRow(
                        title = "Auto-Hide GDPR Cookie Consent Banners",
                        subtitle = "Suppress intrusive OneTrust, Cookiebot, and consent overlays via CSS",
                        checked = settings.hideCookieBanners,
                        onCheckedChange = onUpdateHideCookieBanners
                    )
                }
            }

            // 4. Nocturne Private Vault
            item {
                PreferenceSectionCard(title = "NOCTURNE PRIVATE VAULT") {
                    ToggleSettingRow(
                        title = "Require Biometric / PIN Lock for Private Tabs",
                        subtitle = "Gate access to open Private Vault tabs with Biometrics or PIN (1234)",
                        checked = settings.privateVaultLockEnabled,
                        onCheckedChange = onUpdatePrivateVaultLock
                    )
                }
            }

            // 5. Per-Site Shield Exceptions Summary
            if (siteRules.isNotEmpty()) {
                item {
                    PreferenceSectionCard(title = "PER-SITE SHIELD RULES (${siteRules.size})") {
                        siteRules.forEach { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rule.domain,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = buildString {
                                        append(if (rule.shieldEnabled) "Protected" else "Paused")
                                        if (rule.strictMode) append(" · Strict")
                                        if (rule.zappedSelectors.isNotEmpty()) {
                                            append(" · ${rule.zappedSelectors.size} zapped")
                                        }
                                    },
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
