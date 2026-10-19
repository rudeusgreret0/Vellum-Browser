package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.data.SearchEngineOption
import com.example.engine.UrlSanitizerAndSearch
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.viewmodel.BrowserTab
import kotlin.math.abs

@Composable
fun VellumUnifiedToolbar(
    activeTab: BrowserTab,
    tabCountInMode: Int,
    shieldEnabledOnSite: Boolean,
    isBottomBar: Boolean,
    onOpenShieldSheet: () -> Unit,
    onOpenOmnibox: () -> Unit,
    onTriggerReaderMode: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onOpenOverflowMenu: () -> Unit,
    onSwipeAdjacentTab: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (activeTab.isLoading) (activeTab.progress / 100f).coerceIn(0.05f, 1f) else 0f,
        label = "loading_progress"
    )

    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBottomBar) {
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                } else {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                }
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top hairline border + 2dp loading progress bar
            if (activeTab.isLoading) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                HorizontalDivider(
                    thickness = 0.75.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Live Ad & Tracker Shield Button + Blocked Badge
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            if (shieldEnabledOnSite) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = 0.75.dp,
                            color = if (shieldEnabledOnSite) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(13.dp)
                        )
                        .clickable { onOpenShieldSheet() }
                        .padding(horizontal = 10.dp)
                        .testTag("shield_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (shieldEnabledOnSite) {
                                Icons.Outlined.VerifiedUser
                            } else {
                                Icons.Outlined.Shield
                            },
                            contentDescription = "Vellum Shield",
                            tint = if (shieldEnabledOnSite) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        if (activeTab.blockedCount > 0) {
                            Text(
                                text = "${activeTab.blockedCount}",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (shieldEnabledOnSite) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                // 2. The Smart Omnibox Pill (Supports Horizontal Swipe to Switch Tabs & Pull Up for Tab Deck)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                        .border(
                            width = 0.75.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .pointerInput(activeTab.id) {
                            detectDragGestures(
                                onDragStart = {
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDragEnd = {
                                    if (abs(dragX) > 90f && abs(dragX) > abs(dragY)) {
                                        if (dragX < 0) onSwipeAdjacentTab(1) else onSwipeAdjacentTab(-1)
                                    } else if (dragY < -75f && abs(dragY) > abs(dragX)) {
                                        onOpenTabSwitcher()
                                    }
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragX += dragAmount.x
                                    dragY += dragAmount.y
                                }
                            )
                        }
                        .clickable { onOpenOmnibox() }
                        .padding(horizontal = 12.dp)
                        .testTag("omnibox_bar"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (activeTab.url.isBlank()) {
                                    Icons.Outlined.Search
                                } else {
                                    Icons.Outlined.Lock
                                },
                                contentDescription = null,
                                tint = if (activeTab.url.isBlank()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(15.dp)
                            )

                            val displayLabel = when {
                                activeTab.url.isBlank() -> "Search Brave or enter address…"
                                activeTab.domain.isNotBlank() -> activeTab.domain
                                else -> activeTab.title
                            }

                            Text(
                                text = displayLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (activeTab.url.isBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (activeTab.url.isNotBlank()) {
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("omnibox_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Reload Page",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Contextual Reader View Button
                IconButton(
                    onClick = onTriggerReaderMode,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("reader_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = "Reader View",
                        tint = if (activeTab.url.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // 4. Spatial Tab Deck Counter Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenTabSwitcher() }
                        .testTag("tab_switcher_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .border(
                                width = 1.6.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(7.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$tabCountInMode",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 5. Overflow Menu Button
                IconButton(
                    onClick = onOpenOverflowMenu,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("overflow_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Browser Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun VellumFindInPageBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Find in page…") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("find_in_page_input")
            )
            IconButton(onClick = { onFindNext(false) }) {
                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Previous Match")
            }
            IconButton(onClick = { onFindNext(true) }) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Next Match")
            }
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_find_button")) {
                Icon(Icons.Outlined.Close, contentDescription = "Close Find")
            }
        }
    }
}

@Composable
fun VellumOmniboxCommandOverlay(
    initialQuery: String,
    searchEngine: SearchEngineOption,
    bookmarks: List<BookmarkEntity>,
    history: List<HistoryEntity>,
    onSubmitQueryOrUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember(initialQuery) { mutableStateOf(initialQuery) }
    val focusRequester = remember { FocusRequester() }
    val clipboardManager = LocalClipboardManager.current

    val activeBang = remember(input) { UrlSanitizerAndSearch.detectActiveBang(input) }

    val matchingSuggestions = remember(input, bookmarks, history) {
        val q = input.trim().lowercase()
        if (q.isEmpty()) {
            bookmarks.take(6).map { SuggestionItem(it.title, it.url, it.domain, isBookmark = true) }
        } else {
            val bmMatches = bookmarks.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }.map { SuggestionItem(it.title, it.url, it.domain, isBookmark = true) }

            val histMatches = history.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }.distinctBy { it.url }
                .map { SuggestionItem(it.title, it.url, it.domain, isBookmark = false) }

            (bmMatches + histMatches).distinctBy { it.url }.take(8)
        }
    }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("omnibox_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Search Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("omnibox_close_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text("Search ${searchEngine.badge} or type URL / !bang…")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (input.isNotBlank()) {
                                onSubmitQueryOrUrl(input)
                            }
                        }
                    ),
                    trailingIcon = {
                        if (input.isNotEmpty()) {
                            IconButton(onClick = { input = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("omnibox_text_field")
                )

                TextButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSubmitQueryOrUrl(input)
                        }
                    },
                    modifier = Modifier.testTag("omnibox_go_button")
                ) {
                    Text("Go", fontWeight = FontWeight.SemiBold)
                }
            }

            // Active !Bang Shortcut Banner
            AnimatedVisibility(visible = activeBang != null) {
                activeBang?.let { bang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Routing directly to ${bang.name} (${bang.description})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = bang.trigger,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Instant !Bang Command Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(UrlSanitizerAndSearch.bangShortcuts) { bang ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                val currentQuery = input.trim().substringAfter(" ", "")
                                input = if (currentQuery.isNotBlank() && input.startsWith("!")) {
                                    "${bang.trigger} $currentQuery"
                                } else if (input.isNotBlank() && !input.startsWith("!")) {
                                    "${bang.trigger} $input"
                                } else {
                                    "${bang.trigger} "
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = bang.trigger,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = bang.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Clipboard Paste & Go Option if available
            val clipboardText = clipboardManager.getText()?.text?.trim().orEmpty()
            if (clipboardText.isNotBlank() && clipboardText != input) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { onSubmitQueryOrUrl(clipboardText) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paste & Go from Clipboard",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = clipboardText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Instant Suggestions & Direct Matches
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (input.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSubmitQueryOrUrl(input) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .testTag("omnibox_primary_suggestion"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = input,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Search with ${searchEngine.label} or navigate directly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(matchingSuggestions, key = { it.url }) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSubmitQueryOrUrl(suggestion.url) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (suggestion.isBookmark) {
                                Icons.Outlined.Public
                            } else {
                                Icons.Outlined.History
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = suggestion.domain.ifBlank { suggestion.url },
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { input = suggestion.url },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NorthWest,
                                contentDescription = "Fill URL",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SuggestionItem(
    val title: String,
    val url: String,
    val domain: String,
    val isBookmark: Boolean
)
