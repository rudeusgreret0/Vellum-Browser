package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["url"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val domain: String,
    val folder: String = "General",
    val monogramLetter: String,
    val accentHex: String = "#C86D51",
    val isSpeedDial: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "history",
    indices = [Index(value = ["visitedAt"]), Index(value = ["url"])]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val domain: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val blockedCountOnVisit: Int = 0
)

@Entity(
    tableName = "offline_articles",
    indices = [Index(value = ["url"], unique = true)]
)
data class OfflineArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val domain: String,
    val authorOrByline: String = "",
    val excerpt: String,
    val paragraphsJoined: String,
    val wordCount: Int,
    val readingMinutes: Int,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "site_shield_rules",
    indices = [Index(value = ["domain"], unique = true)]
)
data class SiteShieldRuleEntity(
    @PrimaryKey val domain: String,
    val shieldEnabled: Boolean = true,
    val strictMode: Boolean = false,
    val zappedSelectorsDelimited: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val zappedSelectors: List<String>
        get() = if (zappedSelectorsDelimited.isBlank()) emptyList()
        else zappedSelectorsDelimited.split("|||").filter { it.isNotBlank() }
}

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val domain: String,
    val mimeType: String,
    val category: String, // Documents, Images, Media, Archives, Other
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: String, // COMPLETED, IN_PROGRESS, PAUSED, FAILED
    val createdAt: Long = System.currentTimeMillis()
)
