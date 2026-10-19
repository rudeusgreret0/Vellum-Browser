package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.OfflineArticleEntity
import com.example.data.UserSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalAmbientTheme
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PrivateVaultAccent
import com.example.ui.theme.PrivateVaultSkyBottom
import com.example.ui.theme.PrivateVaultSkyTop
import com.example.ui.theme.VellumAmbientTheme
import com.example.viewmodel.UploadedFileSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VellumStartPage(
    settings: UserSettings,
    isPrivateMode: Boolean,
    speedDials: List<BookmarkEntity>,
    offlineArticles: List<OfflineArticleEntity>,
    onOpenOmnibox: (String) -> Unit,
    onNavigateToUrl: (String) -> Unit,
    onOpenShieldSheet: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOfflineArticle: (OfflineArticleEntity) -> Unit,
    onAddSpeedDial: (String, String) -> Unit,
    onDeleteSpeedDial: (Long) -> Unit,
    onIncineratePrivate: () -> Unit,
    onCycleAmbientTheme: () -> Unit = {},
    onFilesPickedFromStart: (List<UploadedFileSummary>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ambient = LocalAmbientTheme.current

    var showAddDialDialog by remember { mutableStateOf(false) }
    var dialToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }

    val directUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val summaries = uris.map { extractFileMetadata(context, it) }
            onFilesPickedFromStart(summaries)
        }
    }

    val currentTimeStr = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDateStr = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }

    val savedMbStr = remember(settings.lifetimeBytesSaved) {
        String.format(Locale.US, "%.1f MB", settings.lifetimeBytesSaved / 1_000_000f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Scenic Multi-Layer Ambient Wallpaper Canvas
        ScenicAmbientWallpaper(
            ambient = ambient,
            isPrivateMode = isPrivateMode,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Clean, Minimal-Text, Visual Start Content
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Top Glass Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1-Tap Scenic Theme Switcher Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(ambient.surfaceGlass)
                            .border(1.dp, ambient.cardBorder, RoundedCornerShape(99.dp))
                            .clickable { onCycleAmbientTheme() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("cycle_theme_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Change Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPrivateMode) "Private" else ambient.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Top Quick Icon Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlassCircleIconButton(
                            icon = Icons.Outlined.CloudUpload,
                            contentDescription = "Upload File",
                            onClick = { directUploadLauncher.launch(arrayOf("*/*")) },
                            testTag = "start_upload_button"
                        )
                        GlassCircleIconButton(
                            icon = Icons.Outlined.Download,
                            contentDescription = "Downloads",
                            onClick = onOpenDownloads,
                            testTag = "start_downloads_button"
                        )
                        GlassCircleIconButton(
                            icon = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Saved",
                            onClick = onOpenLibrary,
                            testTag = "start_library_button"
                        )
                        GlassCircleIconButton(
                            icon = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            onClick = onOpenSettings,
                            testTag = "start_settings_button"
                        )
                    }
                }
            }

            // Center Clock & Compact Shield Status Pill
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentTimeStr,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 54.sp,
                        letterSpacing = (-1.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = currentDateStr.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Compact Interactive Ad-Blocker Status Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(ambient.surfaceGlass)
                            .border(1.dp, ambient.cardBorder, RoundedCornerShape(99.dp))
                            .clickable { onOpenShieldSheet() }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                            .testTag("start_shield_telemetry_card"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.globalShieldEnabled) {
                                Icons.Outlined.VerifiedUser
                            } else {
                                Icons.Outlined.Shield
                            },
                            contentDescription = "Ad Blocker",
                            tint = if (settings.globalShieldEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "%,d Blocked".format(Locale.US, settings.lifetimeBlockedCount),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$savedMbStr Saved",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Floating Frosted Glass Search Pill
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onOpenOmnibox("") }
                        .border(
                            width = 1.2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("start_page_search_bar"),
                    color = ambient.surfaceGlass,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Search ${settings.searchEngine.badge} or enter URL…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = settings.searchEngine.badge,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 4x2 Visual Icon Grid (Minimal 1-Word Labels)
            item {
                val chunkedDials = remember(speedDials) { speedDials.chunked(4) }
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    chunkedDials.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { dial ->
                                VisualSpeedDialIcon(
                                    dial = dial,
                                    glassColor = ambient.surfaceGlass,
                                    onClick = { onNavigateToUrl(dial.url) },
                                    onLongClick = { dialToDelete = dial },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size < 4) {
                                // Add '+' button in the remaining slot
                                AddSpeedDialTile(
                                    glassColor = ambient.surfaceGlass,
                                    borderColor = ambient.cardBorder,
                                    onClick = { showAddDialDialog = true },
                                    modifier = Modifier.weight(1f)
                                )
                                repeat((3 - rowItems.size).coerceAtLeast(0)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (speedDials.size % 4 == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(ambient.surfaceGlass)
                                    .border(1.dp, ambient.cardBorder, RoundedCornerShape(99.dp))
                                    .clickable { showAddDialDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("add_speed_dial_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Pin Site",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3 Compact Visual Action Pills (Ad Blocker, File Upload, Downloads)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactActionCard(
                        icon = Icons.Outlined.VerifiedUser,
                        label = "Ad Blocker",
                        glassColor = ambient.surfaceGlass,
                        borderColor = ambient.cardBorder,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenShieldSheet,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("launch_shield_test_button")
                    )
                    CompactActionCard(
                        icon = Icons.Outlined.CloudUpload,
                        label = "Upload",
                        glassColor = ambient.surfaceGlass,
                        borderColor = ambient.cardBorder,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { directUploadLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactActionCard(
                        icon = Icons.Outlined.Download,
                        label = "Downloads",
                        glassColor = ambient.surfaceGlass,
                        borderColor = ambient.cardBorder,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenDownloads,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isPrivateMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { onIncineratePrivate() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("start_incinerate_private_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = "Wipe",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wipe Private Session",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    if (showAddDialDialog) {
        var titleInput by remember { mutableStateOf("") }
        var urlInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialDialog = false },
            title = { Text("Pin Site", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dial_title_input")
                    )
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL (e.g. youtube.com)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dial_url_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            onAddSpeedDial(titleInput.trim(), urlInput.trim())
                            showAddDialDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_dial_button")
                ) {
                    Text("Pin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    dialToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { dialToDelete = null },
            title = { Text("Unpin ${target.title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSpeedDial(target.id)
                        dialToDelete = null
                    }
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ScenicAmbientWallpaper(
    ambient: VellumAmbientTheme,
    isPrivateMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_drift")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_drift"
    )

    val topColor = if (isPrivateMode) PrivateVaultSkyTop else ambient.skyTop
    val midColor = if (isPrivateMode) PrivateVaultSkyBottom else ambient.skyMid
    val bottomColor = if (isPrivateMode) PrivateVaultSkyBottom else ambient.skyBottom
    val glow1 = if (isPrivateMode) PrivateVaultAccent else ambient.glowPrimary
    val glow2 = if (isPrivateMode) Color(0xFF38BDF8) else ambient.glowSecondary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Rich Multi-Stop Atmospheric Sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, midColor, bottomColor)
            )
        )

        // 2. Soft Drifting Aurora / Sunset Light Orbs
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow1.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(w * (0.25f + 0.15f * drift), h * 0.22f),
                radius = w * 0.65f
            ),
            center = Offset(w * (0.25f + 0.15f * drift), h * 0.22f),
            radius = w * 0.65f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow2.copy(alpha = 0.24f), Color.Transparent),
                center = Offset(w * (0.78f - 0.12f * drift), h * 0.36f),
                radius = w * 0.6f
            ),
            center = Offset(w * (0.78f - 0.12f * drift), h * 0.36f),
            radius = w * 0.6f
        )

        // 3. Layered Scenic Mountain / Silk Wave Horizon
        val backWave = Path().apply {
            moveTo(0f, h * 0.76f)
            cubicTo(
                w * 0.28f, h * (0.68f - 0.02f * drift),
                w * 0.65f, h * (0.82f + 0.02f * drift),
                w, h * 0.72f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = backWave, color = ambient.horizonBack.copy(alpha = 0.65f))

        val frontWave = Path().apply {
            moveTo(0f, h * 0.84f)
            cubicTo(
                w * 0.35f, h * (0.89f - 0.02f * drift),
                w * 0.72f, h * (0.77f + 0.02f * drift),
                w, h * 0.83f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = frontWave, color = ambient.horizonFront.copy(alpha = 0.82f))
    }
}

@Composable
private fun GlassCircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    val ambient = LocalAmbientTheme.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(ambient.surfaceGlass)
            .border(1.dp, ambient.cardBorder, CircleShape)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(19.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VisualSpeedDialIcon(
    dial: BookmarkEntity,
    glassColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedAccent = remember(dial.accentHex) {
        runCatching { Color(android.graphics.Color.parseColor(dial.accentHex)) }
            .getOrDefault(Color(0xFF00E5C3))
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
            .testTag("speed_dial_${dial.domain}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(glassColor)
                .border(
                    width = 1.dp,
                    color = parsedAccent.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                parsedAccent.copy(alpha = 0.32f),
                                parsedAccent.copy(alpha = 0.14f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dial.monogramLetter,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = parsedAccent
                )
            }
        }

        Text(
            text = dial.title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddSpeedDialTile(
    glassColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("add_speed_dial_button"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(glassColor)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Pin Site",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "Pin",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    glassColor: Color,
    borderColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(glassColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
