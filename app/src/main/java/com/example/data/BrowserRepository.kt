package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BrowserRepository(
    private val dao: BrowserDao,
    val preferences: VellumPreferences
) {
    val speedDials: Flow<List<BookmarkEntity>> = dao.observeSpeedDials()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.observeAllBookmarks()
    val history: Flow<List<HistoryEntity>> = dao.observeHistory()
    val offlineArticles: Flow<List<OfflineArticleEntity>> = dao.observeOfflineArticles()
    val siteRules: Flow<List<SiteShieldRuleEntity>> = dao.observeAllSiteRules()
    val downloads: Flow<List<DownloadItemEntity>> = dao.observeDownloads()

    suspend fun ensureSeeded() {
        if (dao.getBookmarkCount() == 0) {
            dao.insertBookmarks(VellumDatabase.defaultSpeedDials())
            VellumDatabase.defaultOfflineArticles().forEach {
                dao.insertOfflineArticle(it)
            }
            // Seed a couple of sample completed downloads so the Downloads vault is immediately inspectable
            val existingDownloads = dao.observeDownloads().first()
            if (existingDownloads.isEmpty()) {
                dao.insertDownload(
                    DownloadItemEntity(
                        fileName = "Vellum_Privacy_Architecture_Whitepaper.pdf",
                        url = "https://vellum.local/docs/whitepaper.pdf",
                        domain = "vellum.local",
                        mimeType = "application/pdf",
                        category = "Documents",
                        totalBytes = 2_450_000L,
                        downloadedBytes = 2_450_000L,
                        status = "COMPLETED"
                    )
                )
                dao.insertDownload(
                    DownloadItemEntity(
                        fileName = "EasyList_Tracker_Signatures_2026.json",
                        url = "https://easylist.to/easylist/easylist.txt",
                        domain = "easylist.to",
                        mimeType = "application/json",
                        category = "Archives",
                        totalBytes = 1_840_000L,
                        downloadedBytes = 1_840_000L,
                        status = "COMPLETED"
                    )
                )
            }
        }
    }

    suspend fun toggleBookmark(
        url: String,
        title: String,
        domain: String,
        isSpeedDial: Boolean = false
    ): Boolean {
        val current = dao.observeAllBookmarks().first()
        val existing = current.firstOrNull { it.url.equals(url, ignoreCase = true) }
        return if (existing != null) {
            dao.deleteBookmarkById(existing.id)
            false
        } else {
            val cleanTitle = title.ifBlank { domain.ifBlank { url } }
            val monogram = (domain.firstOrNull { it.isLetterOrDigit() } ?: 'V').uppercaseChar().toString()
            val palette = listOf("#C86D51", "#D4A359", "#4E9F86", "#6B7FD7", "#8E7DBE", "#7A8B99")
            val chosenColor = palette[kotlin.math.abs(domain.hashCode()) % palette.size]
            dao.insertBookmark(
                BookmarkEntity(
                    url = url,
                    title = cleanTitle,
                    domain = domain,
                    monogramLetter = monogram,
                    accentHex = chosenColor,
                    isSpeedDial = isSpeedDial,
                    sortOrder = current.size + 1
                )
            )
            true
        }
    }

    suspend fun addSpeedDial(title: String, rawUrl: String) {
        val normalizedUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "https://$rawUrl"
        }
        val domain = extractDomain(normalizedUrl)
        val monogram = (title.firstOrNull { it.isLetterOrDigit() }
            ?: domain.firstOrNull { it.isLetterOrDigit() }
            ?: 'V').uppercaseChar().toString()
        val palette = listOf("#C86D51", "#D4A359", "#4E9F86", "#6B7FD7", "#8E7DBE", "#7A8B99")
        val chosenColor = palette[kotlin.math.abs(domain.hashCode()) % palette.size]
        dao.insertBookmark(
            BookmarkEntity(
                url = normalizedUrl,
                title = title.ifBlank { domain },
                domain = domain,
                monogramLetter = monogram,
                accentHex = chosenColor,
                isSpeedDial = true,
                sortOrder = 99
            )
        )
    }

    suspend fun deleteBookmark(id: Long) = dao.deleteBookmarkById(id)

    suspend fun recordHistory(url: String, title: String, domain: String, blockedCount: Int) {
        if (url.isBlank() || url == "about:blank" || url.startsWith("vellum://")) return
        dao.insertHistory(
            HistoryEntity(
                url = url,
                title = title.ifBlank { domain.ifBlank { url } },
                domain = domain,
                visitedAt = System.currentTimeMillis(),
                blockedCountOnVisit = blockedCount
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = dao.deleteHistoryById(id)

    suspend fun clearHistoryRange(rangeMinutes: Int?) {
        if (rangeMinutes == null) {
            dao.clearAllHistory()
        } else {
            val cutoff = System.currentTimeMillis() - (rangeMinutes * 60_000L)
            dao.deleteHistorySince(cutoff)
        }
    }

    suspend fun saveOfflineArticle(article: OfflineArticleEntity) {
        dao.insertOfflineArticle(article)
    }

    suspend fun deleteOfflineArticle(id: Long) = dao.deleteOfflineArticleById(id)

    suspend fun getOrCreateSiteRule(domain: String, defaultStrict: Boolean): SiteShieldRuleEntity {
        if (domain.isBlank()) return SiteShieldRuleEntity(domain = "local", strictMode = defaultStrict)
        return dao.getSiteRule(domain) ?: SiteShieldRuleEntity(
            domain = domain,
            shieldEnabled = true,
            strictMode = defaultStrict
        )
    }

    suspend fun setSiteShieldEnabled(domain: String, enabled: Boolean) {
        if (domain.isBlank()) return
        val existing = dao.getSiteRule(domain) ?: SiteShieldRuleEntity(domain = domain)
        dao.upsertSiteRule(
            existing.copy(
                shieldEnabled = enabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setSiteStrictMode(domain: String, strict: Boolean) {
        if (domain.isBlank()) return
        val existing = dao.getSiteRule(domain) ?: SiteShieldRuleEntity(domain = domain)
        dao.upsertSiteRule(
            existing.copy(
                strictMode = strict,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addZappedSelectorForSite(domain: String, cssSelector: String) {
        if (domain.isBlank() || cssSelector.isBlank()) return
        val existing = dao.getSiteRule(domain) ?: SiteShieldRuleEntity(domain = domain)
        val updatedList = (existing.zappedSelectors + cssSelector.trim()).distinct()
        dao.upsertSiteRule(
            existing.copy(
                zappedSelectorsDelimited = updatedList.joinToString("|||"),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearZappedSelectorsForSite(domain: String) {
        if (domain.isBlank()) return
        val existing = dao.getSiteRule(domain) ?: return
        dao.upsertSiteRule(
            existing.copy(
                zappedSelectorsDelimited = "",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordDownload(
        fileName: String,
        url: String,
        mimeType: String,
        totalBytes: Long
    ): Long {
        val category = when {
            mimeType.contains("pdf", true) ||
                mimeType.contains("text", true) ||
                fileName.endsWith(".pdf", true) ||
                fileName.endsWith(".doc", true) ||
                fileName.endsWith(".txt", true) -> "Documents"
            mimeType.startsWith("image/", true) ||
                fileName.endsWith(".png", true) ||
                fileName.endsWith(".jpg", true) ||
                fileName.endsWith(".webp", true) -> "Images"
            mimeType.startsWith("video/", true) ||
                mimeType.startsWith("audio/", true) ||
                fileName.endsWith(".mp4", true) ||
                fileName.endsWith(".mp3", true) -> "Media"
            fileName.endsWith(".zip", true) ||
                fileName.endsWith(".tar", true) ||
                fileName.endsWith(".gz", true) ||
                fileName.endsWith(".apk", true) ||
                fileName.endsWith(".json", true) -> "Archives"
            else -> "Documents"
        }
        val safeTotal = if (totalBytes > 0) totalBytes else 1_250_000L
        return dao.insertDownload(
            DownloadItemEntity(
                fileName = fileName,
                url = url,
                domain = extractDomain(url),
                mimeType = mimeType.ifBlank { "application/octet-stream" },
                category = category,
                totalBytes = safeTotal,
                downloadedBytes = safeTotal,
                status = "COMPLETED"
            )
        )
    }

    suspend fun deleteDownload(id: Long) = dao.deleteDownloadById(id)

    suspend fun clearAllDownloads() = dao.clearAllDownloads()

    companion object {
        fun extractDomain(url: String): String {
            return runCatching {
                val clean = url.trim()
                val withoutScheme = clean.substringAfter("://", clean)
                val host = withoutScheme.substringBefore("/").substringBefore("?").lowercase()
                host.removePrefix("www.").removePrefix("m.")
            }.getOrDefault(url)
        }
    }
}
