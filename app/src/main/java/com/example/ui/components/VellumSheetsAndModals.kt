package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ReaderFontOption
import com.example.data.ReaderThemeOption
import com.example.data.UserSettings
import com.example.ui.theme.AlabasterCanvas
import com.example.ui.theme.CarbonCanvas
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NewsreaderSerifFamily
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.ReaderSepiaBg
import com.example.ui.theme.ReaderSepiaInk
import com.example.viewmodel.BrowserTab
import com.example.viewmodel.PeekPreviewSession
import com.example.viewmodel.ReaderArticleSession
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumShieldInspectorSheet(
    activeTab: BrowserTab,
    settings: UserSettings,
    siteShieldEnabled: Boolean,
    siteStrictMode: Boolean,
    zappedSelectorsCount: Int,
    onToggleSiteShield: () -> Unit,
    onToggleSiteStrict: () -> Unit,
    onStartZapMode: () -> Unit,
    onResetZappedElements: () -> Unit,
    onLaunchDiagnosticPage: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val domainLabel = activeTab.domain.ifBlank { "All Websites (Global)" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("shield_inspector_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (siteShieldEnabled) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (siteShieldEnabled) {
                                Icons.Outlined.VerifiedUser
                            } else {
                                Icons.Outlined.Shield
                            },
                            contentDescription = null,
                            tint = if (siteShieldEnabled) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Column {
                        Text(
                            text = "Vellum Shield Inspector",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = domainLabel,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (activeTab.domain.isNotBlank()) {
                    Switch(
                        checked = siteShieldEnabled,
                        onCheckedChange = { onToggleSiteShield() },
                        modifier = Modifier.testTag("site_shield_toggle")
                    )
                }
            }

            // Stats Strip for Current Page vs Lifetime
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeTab.blockedCount}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Blocked on Page",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val kbSaved = activeTab.bytesSaved / 1000
                    Text(
                        text = "${kbSaved} KB",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Page Data Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%,d".format(Locale.US, settings.lifetimeBlockedCount),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lifetime Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Protection Level & Zap Distraction Controls
            if (activeTab.domain.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aggressive Strict Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Also blocks cross-origin beacons, social widgets & fingerprinting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = siteStrictMode,
                        onCheckedChange = { onToggleSiteStrict() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartZapMode,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_zap_mode_button")
                    ) {
                        Icon(Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Zap Distraction")
                    }

                    if (zappedSelectorsCount > 0) {
                        OutlinedButton(
                            onClick = onResetZappedElements,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reset ($zappedSelectorsCount)")
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Live Monospace Tracker Interception Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE INTERCEPTION LOG (${activeTab.blockedDomains.size} DOMAINS)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Open Benchmark",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onLaunchDiagnosticPage()
                        }
                        .testTag("shield_sheet_open_benchmark")
                )
            }

            if (activeTab.blockedDomains.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTab.url.isBlank()) {
                            "Open any website or tap 'Open Benchmark' above to inspect live intercepted ad servers and trackers."
                        } else {
                            "Zero third-party trackers detected on this page so far."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeTab.blockedDomains, key = { it.domain }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .border(
                                    0.75.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.domain,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.category.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "×${item.count}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
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
fun VellumSpatialTabSwitcherSheet(
    tabs: List<BrowserTab>,
    activeTabId: String,
    isPrivateMode: Boolean,
    isPrivateVaultLocked: Boolean,
    onSelectMode: (Boolean) -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCreateNewTab: () -> Unit,
    onCloseAllInMode: () -> Unit,
    onIncineratePrivate: () -> Unit,
    onUnlockPrivateVault: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val modeTabs = remember(tabs, isPrivateMode) {
        tabs.filter { it.isPrivate == isPrivateMode }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .padding(horizontal = 18.dp)
                .padding(bottom = 20.dp)
                .testTag("tab_switcher_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Mode Segmented Switcher (Standard Tabs vs Private Vault)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val standardCount = tabs.count { !it.isPrivate }
                    val privateCount = tabs.count { it.isPrivate }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (!isPrivateMode) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { onSelectMode(false) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("tab_mode_standard")
                    ) {
                        Text(
                            text = "Standard ($standardCount)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!isPrivateMode) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPrivateMode) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { onSelectMode(true) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("tab_mode_private")
                    ) {
                        Text(
                            text = "Private Vault ($privateCount)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPrivateMode) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isPrivateMode) {
                    IconButton(
                        onClick = onIncineratePrivate,
                        modifier = Modifier.testTag("tab_sheet_incinerate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = "Incinerate Private Vault",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    TextButton(onClick = onCloseAllInMode) {
                        Text("Clear All")
                    }
                }
            }

            // If Private Vault Lock is active and locked, show unlock gate
            if (isPrivateMode && isPrivateVaultLocked) {
                PrivateVaultLockGate(onUnlock = onUnlockPrivateVault)
            } else {
                // 2-Column Spatial Grid of Tabs
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modeTabs, key = { it.id }) { tab ->
                        val isSelected = tab.id == activeTabId
                        val monogram = (tab.domain.firstOrNull { it.isLetterOrDigit() } ?: 'V')
                            .uppercaseChar().toString()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(148.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onSelectTab(tab.id) }
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .testTag("tab_card_${tab.id}"),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = monogram,
                                                fontFamily = NewsreaderSerifFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = tab.domain.ifBlank { "vellum.start" },
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onCloseTab(tab.id) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("close_tab_${tab.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Close Tab",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = tab.title.ifBlank { "Start Page" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (tab.blockedCount > 0) "🛡 ${tab.blockedCount} blocked" else "🛡 Protected",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Thumb Action Bar inside Tab Deck
                Button(
                    onClick = onCreateNewTab,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("new_tab_button")
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPrivateMode) "New Private Vault Tab" else "New Tab",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PrivateVaultLockGate(
    onUnlock: (String) -> Boolean
) {
    var pinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Vault Locked",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Nocturne Private Vault Locked",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Authenticate with Biometrics or enter your Vault PIN (default: 1234) to view open private tabs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = pinInput,
            onValueChange = {
                pinInput = it
                errorText = null
            },
            label = { Text("Vault PIN") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_pin_input")
        )

        errorText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onUnlock("BIOMETRIC_OK") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("vault_biometric_unlock_button")
            ) {
                Icon(Icons.Outlined.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Biometric")
            }
            Button(
                onClick = {
                    if (!onUnlock(pinInput.trim())) {
                        errorText = "Incorrect PIN (Default is 1234)"
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("vault_pin_unlock_button")
            ) {
                Text("Unlock Vault")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumOverflowMenuSheet(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    onToggleBookmark: (Boolean) -> Unit,
    onReturnHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenReaderView: () -> Unit,
    onStartZapMode: () -> Unit,
    onToggleFindInPage: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onShareCurrentUrl: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("overflow_menu_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Action Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickMenuTile(
                    icon = Icons.Outlined.Home,
                    label = "Start Page",
                    onClick = {
                        onDismiss()
                        onReturnHome()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickMenuTile(
                    icon = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (isBookmarked) "Bookmarked" else "Bookmark",
                    onClick = {
                        onToggleBookmark(false)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickMenuTile(
                    icon = Icons.Outlined.AutoStories,
                    label = "Reader View",
                    onClick = {
                        onDismiss()
                        onOpenReaderView()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickMenuTile(
                    icon = Icons.Outlined.Bolt,
                    label = "Zap Element",
                    onClick = {
                        onDismiss()
                        onStartZapMode()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            MenuRowItem(
                icon = Icons.Outlined.BookmarkAdd,
                title = "Pin to Start Page Speed Dials",
                subtitle = "Add ${activeTab.domain.ifBlank { "current site" }} to your Start Page grid",
                onClick = {
                    onToggleBookmark(true)
                    onDismiss()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.BookmarkBorder,
                title = "Library · Bookmarks, Reading List & History",
                subtitle = "Manage saved pages and clear browsing history",
                onClick = {
                    onDismiss()
                    onOpenLibrary()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.Download,
                title = "Downloads Vault",
                subtitle = "Categorized documents, media, and archives",
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.FindInPage,
                title = "Find in Page",
                subtitle = "Highlight text matches on the active page",
                onClick = {
                    onDismiss()
                    onToggleFindInPage()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.DesktopWindows,
                title = if (activeTab.isDesktopMode) "Desktop Site (Enabled)" else "Request Desktop Site",
                subtitle = "Switch viewport user-agent",
                onClick = {
                    onDismiss()
                    onToggleDesktopSite()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.Share,
                title = "Share Clean Link",
                subtitle = "Automatically strips utm_*, fbclid & gclid trackers",
                onClick = {
                    onDismiss()
                    onShareCurrentUrl()
                }
            )
            MenuRowItem(
                icon = Icons.Outlined.Settings,
                title = "Vellum Preferences",
                subtitle = "Themes, Brave Search, Shield defaults & Private Vault",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )
        }
    }
}

@Composable
private fun QuickMenuTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MenuRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VellumEditorialReaderOverlay(
    session: ReaderArticleSession,
    settings: UserSettings,
    onSaveOffline: () -> Unit,
    onSelectTheme: (ReaderThemeOption) -> Unit,
    onSelectFont: (ReaderFontOption) -> Unit,
    onChangeFontSize: (Int) -> Unit,
    onClose: () -> Unit
) {
    val bgColor = when (settings.readerTheme) {
        ReaderThemeOption.ALABASTER -> AlabasterCanvas
        ReaderThemeOption.SEPIA -> ReaderSepiaBg
        ReaderThemeOption.CARBON -> CarbonCanvas
    }
    val inkColor = when (settings.readerTheme) {
        ReaderThemeOption.ALABASTER -> ObsidianInk
        ReaderThemeOption.SEPIA -> ReaderSepiaInk
        ReaderThemeOption.CARBON -> ParchmentInk
    }
    val chosenFontFamily = when (settings.readerFont) {
        ReaderFontOption.SERIF -> NewsreaderSerifFamily
        ReaderFontOption.SANS -> PlusJakartaSansFamily
        ReaderFontOption.MONO -> JetBrainsMonoFamily
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("reader_mode_overlay"),
        color = bgColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Reader Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_reader_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Exit Reader View",
                            tint = inkColor
                        )
                    }
                    Text(
                        text = "READER VIEW · ${session.readingMinutes} MIN READ",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = inkColor.copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = onSaveOffline,
                    enabled = !session.isSavedOffline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_offline_reader_button")
                ) {
                    Icon(
                        imageVector = if (session.isSavedOffline) {
                            Icons.Outlined.Bookmark
                        } else {
                            Icons.Outlined.BookmarkAdd
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (session.isSavedOffline) "Saved Offline" else "Save Offline")
                }
            }

            // Typography & Paper Theme Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReaderThemeOption.entries.forEach { option ->
                        FilterChip(
                            selected = settings.readerTheme == option,
                            onClick = { onSelectTheme(option) },
                            label = { Text(option.label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = { onChangeFontSize(settings.readerFontSizeSp - 2) }) {
                        Text("A-", color = inkColor, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { onChangeFontSize(settings.readerFontSizeSp + 2) }) {
                        Text("A+", color = inkColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderFontOption.entries.forEach { fontOpt ->
                    FilterChip(
                        selected = settings.readerFont == fontOpt,
                        onClick = { onSelectFont(fontOpt) },
                        label = { Text(fontOpt.label, fontSize = 11.sp) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = inkColor.copy(alpha = 0.15f)
            )

            // Article Body
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = session.domain.uppercase(Locale.US),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = session.title,
                            fontFamily = NewsreaderSerifFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (settings.readerFontSizeSp + 10).sp,
                            lineHeight = (settings.readerFontSizeSp + 16).sp,
                            color = inkColor
                        )
                        if (session.byline.isNotBlank()) {
                            Text(
                                text = "${session.byline} · ${session.wordCount} words",
                                style = MaterialTheme.typography.bodySmall,
                                color = inkColor.copy(alpha = 0.68f)
                            )
                        }
                    }
                }

                items(session.paragraphs) { paragraph ->
                    Text(
                        text = paragraph,
                        fontFamily = chosenFontFamily,
                        fontSize = settings.readerFontSizeSp.sp,
                        lineHeight = (settings.readerFontSizeSp * 1.62f).sp,
                        color = inkColor.copy(alpha = 0.92f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VellumPeekPreviewSheet(
    peekSession: PeekPreviewSession,
    onPromoteToTab: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isHttps = peekSession.url.startsWith("https://", ignoreCase = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("peek_preview_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VELLUM PEEK · LINK INSPECTOR",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = peekSession.domain,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isHttps) MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isHttps) "HTTPS ENCRYPTED" else "UNENCRYPTED HTTP",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHttps) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SANITIZED DESTINATION URL",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = peekSession.url,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss")
                }
                Button(
                    onClick = onPromoteToTab,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("promote_peek_button")
                ) {
                    Icon(Icons.Outlined.OpenInFull, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open in New Tab")
                }
            }
        }
    }
}
