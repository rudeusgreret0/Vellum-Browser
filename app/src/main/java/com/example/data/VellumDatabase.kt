package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        OfflineArticleEntity::class,
        SiteShieldRuleEntity::class,
        DownloadItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VellumDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: VellumDatabase? = null

        fun getInstance(context: Context): VellumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VellumDatabase::class.java,
                    "vellum_browser.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        fun defaultSpeedDials(): List<BookmarkEntity> = listOf(
            BookmarkEntity(
                url = "https://search.brave.com",
                title = "Brave",
                domain = "search.brave.com",
                folder = "Essentials",
                monogramLetter = "B",
                accentHex = "#FF7A50",
                isSpeedDial = true,
                sortOrder = 1
            ),
            BookmarkEntity(
                url = "https://m.youtube.com",
                title = "YouTube",
                domain = "youtube.com",
                folder = "Media",
                monogramLetter = "Y",
                accentHex = "#FF4757",
                isSpeedDial = true,
                sortOrder = 2
            ),
            BookmarkEntity(
                url = "https://en.m.wikipedia.org/wiki/Main_Page",
                title = "Wikipedia",
                domain = "wikipedia.org",
                folder = "Reference",
                monogramLetter = "W",
                accentHex = "#38BDF8",
                isSpeedDial = true,
                sortOrder = 3
            ),
            BookmarkEntity(
                url = "https://www.reddit.com",
                title = "Reddit",
                domain = "reddit.com",
                folder = "Social",
                monogramLetter = "R",
                accentHex = "#FF6348",
                isSpeedDial = true,
                sortOrder = 4
            ),
            BookmarkEntity(
                url = "https://github.com/trending",
                title = "GitHub",
                domain = "github.com",
                folder = "Code",
                monogramLetter = "G",
                accentHex = "#A78BFA",
                isSpeedDial = true,
                sortOrder = 5
            ),
            BookmarkEntity(
                url = "https://news.ycombinator.com",
                title = "HN News",
                domain = "news.ycombinator.com",
                folder = "News",
                monogramLetter = "H",
                accentHex = "#FBBF24",
                isSpeedDial = true,
                sortOrder = 6
            ),
            BookmarkEntity(
                url = "https://catbox.moe",
                title = "Cloud Drop",
                domain = "catbox.moe",
                folder = "Tools",
                monogramLetter = "C",
                accentHex = "#34D399",
                isSpeedDial = true,
                sortOrder = 7
            ),
            BookmarkEntity(
                url = "vellum://shield-test",
                title = "Ad Test",
                domain = "vellum.shield",
                folder = "Tools",
                monogramLetter = "S",
                accentHex = "#00F5D4",
                isSpeedDial = true,
                sortOrder = 8
            )
        )

        fun defaultOfflineArticles(): List<OfflineArticleEntity> = listOf(
            OfflineArticleEntity(
                url = "https://vellum.local/manifesto/quiet-web",
                title = "Vellum Reader Mode",
                domain = "vellum.reader",
                authorOrByline = "Offline Reading",
                excerpt = "Clean, distraction-free reading on any website.",
                paragraphsJoined = listOf(
                    "Tap the Reader icon on any article to strip away clutter and read comfortably in Day, Sepia, or Night mode.",
                    "You can also save any webpage article for instant offline reading."
                ).joinToString("\n\n"),
                wordCount = 38,
                readingMinutes = 1
            )
        )
    }
}
