package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.VellumAmbientTheme
import com.example.ui.theme.VellumThemeMode
import com.example.viewmodel.LibrarySection
import com.example.viewmodel.UploadedFileSummary
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
                .fillMaxHeight(0.84f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("library_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

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
                placeholder = { Text("Search…") },
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
                                                fontFamily = PlusJakartaSansFamily,
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
                                            contentDescription = "Delete",
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
                                            text = article.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${article.domain} · ${article.readingMinutes}m",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteOfflineArticle(article.id) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LibrarySection.HISTORY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onClearHistoryRange(15) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear 15m", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onClearHistoryRange(60) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear 1h", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onClearHistoryRange(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_all_history_button")
                        ) {
                            Text("Clear All", fontSize = 12.sp)
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
                                            contentDescription = "Delete",
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
    recentUploads: List<UploadedFileSummary> = emptyList(),
    onStartDirectDownload: (String) -> Unit = {},
    onFilesUploaded: (List<UploadedFileSummary>) -> Unit = {},
    onDeleteDownload: (Long) -> Unit,
    onClearAllDownloads: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = listOf("All", "Documents", "Images", "Media", "Archives")
    var selectedCategory by remember { mutableStateOf("All") }
    var directDownloadUrl by remember { mutableStateOf("") }

    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val summaries = uris.map { extractFileMetadata(context, it) }
            onFilesUploaded(summaries)
        }
    }

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
                .fillMaxHeight(0.86f)
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
                Text(
                    text = "Downloads & Uploads",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (downloads.isNotEmpty()) {
                    TextButton(onClick = onClearAllDownloads) {
                        Text("Clear")
                    }
                }
            }

            // Direct File Download URL Bar + Upload Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = directDownloadUrl,
                    onValueChange = { directDownloadUrl = it },
                    placeholder = { Text("Paste file URL to download…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("direct_download_input")
                )
                Button(
                    onClick = {
                        val urlToFetch = directDownloadUrl.trim().ifBlank {
                            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                        }
                        onStartDirectDownload(urlToFetch)
                        directDownloadUrl = ""
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("start_direct_download_button")
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = "Download", modifier = Modifier.size(18.dp))
                }
                OutlinedButton(
                    onClick = { uploadLauncher.launch(arrayOf("*/*")) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("sheet_upload_button")
                ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(18.dp))
                }
            }

            // Show Recently Uploaded Files if any
            if (recentUploads.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "RECENT UPLOADS (${recentUploads.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentUploads) { up ->
                            val kbStr = remember(up.sizeBytes) { "${(up.sizeBytes / 1024).coerceAtLeast(1)} KB" }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${up.name} ($kbStr)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
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
                                        text = "${item.category} · $mbString · ${item.status}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteDownload(item.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete",
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
    onUpdateAmbientTheme: (VellumAmbientTheme) -> Unit = {},
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
                .fillMaxHeight(0.86f)
                .padding(horizontal = 20.dp)
                .testTag("settings_sheet"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 1. Visual Scenic Theme Swatches
            item {
                PreferenceSectionCard(title = "WALLPAPER & THEME") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(VellumAmbientTheme.entries) { theme ->
                            val isSelected = settings.ambientTheme == theme
                            Column(
                                modifier = Modifier
                                    .width(104.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onUpdateAmbientTheme(theme) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) theme.primaryAccent else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(theme.surfaceColor)
                                    .padding(10.dp)
                                    .testTag("theme_chip_${theme.id}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    theme.skyTop,
                                                    theme.glowPrimary,
                                                    theme.glowSecondary
                                                )
                                            )
                                        )
                                )
                                Text(
                                    text = theme.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = theme.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    ToggleSettingRow(
                        title = "Bottom Toolbar",
                        checked = settings.bottomToolbar,
                        onCheckedChange = onUpdateBottomToolbar
                    )
                }
            }

            // 2. Search Engine
            item {
                PreferenceSectionCard(title = "SEARCH ENGINE") {
                    SearchEngineOption.entries.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUpdateSearchEngine(engine) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = engine.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = settings.searchEngine == engine,
                                onClick = { onUpdateSearchEngine(engine) }
                            )
                        }
                    }
                }
            }

            // 3. Ad & Popup Shield
            item {
                PreferenceSectionCard(title = "AD & POPUP BLOCKER") {
                    ToggleSettingRow(
                        title = "Block Ads & Video Pre-Rolls",
                        checked = settings.globalShieldEnabled,
                        onCheckedChange = onUpdateGlobalShield
                    )
                    ToggleSettingRow(
                        title = "Strict Popup & Tracker Blocker",
                        checked = settings.strictShieldByDefault,
                        onCheckedChange = onUpdateStrictShield
                    )
                    ToggleSettingRow(
                        title = "Clean Tracking Links",
                        checked = settings.stripTrackingParams,
                        onCheckedChange = onUpdateStripParams
                    )
                    ToggleSettingRow(
                        title = "Hide Cookie Popups",
                        checked = settings.hideCookieBanners,
                        onCheckedChange = onUpdateHideCookieBanners
                    )
                }
            }

            // 4. Private Vault
            item {
                PreferenceSectionCard(title = "PRIVATE VAULT") {
                    ToggleSettingRow(
                        title = "PIN / Biometric Lock",
                        checked = settings.privateVaultLockEnabled,
                        onCheckedChange = onUpdatePrivateVaultLock
                    )
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
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
