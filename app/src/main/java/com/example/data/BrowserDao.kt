package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // --- Bookmarks & Speed Dials ---
    @Query("SELECT * FROM bookmarks WHERE isSpeedDial = 1 ORDER BY sortOrder ASC, createdAt ASC")
    fun observeSpeedDials(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY isSpeedDial DESC, createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    // --- History ---
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 250")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history WHERE visitedAt >= :sinceTimestamp")
    suspend fun deleteHistorySince(sinceTimestamp: Long)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    // --- Offline Reading List ---
    @Query("SELECT * FROM offline_articles ORDER BY savedAt DESC")
    fun observeOfflineArticles(): Flow<List<OfflineArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineArticle(article: OfflineArticleEntity)

    @Query("DELETE FROM offline_articles WHERE id = :id")
    suspend fun deleteOfflineArticleById(id: Long)

    // --- Per-Site Shield Rules ---
    @Query("SELECT * FROM site_shield_rules ORDER BY updatedAt DESC")
    fun observeAllSiteRules(): Flow<List<SiteShieldRuleEntity>>

    @Query("SELECT * FROM site_shield_rules WHERE domain = :domain LIMIT 1")
    suspend fun getSiteRule(domain: String): SiteShieldRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSiteRule(rule: SiteShieldRuleEntity)

    @Query("DELETE FROM site_shield_rules WHERE domain = :domain")
    suspend fun deleteSiteRule(domain: String)

    // --- Downloads ---
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeDownloads(): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity): Long

    @Update
    suspend fun updateDownload(item: DownloadItemEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}
