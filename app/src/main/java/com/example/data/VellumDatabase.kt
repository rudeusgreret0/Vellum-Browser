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
    version = 1,
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
                title = "Brave Search",
                domain = "search.brave.com",
                folder = "Essentials",
                monogramLetter = "B",
                accentHex = "#C86D51",
                isSpeedDial = true,
                sortOrder = 1
            ),
            BookmarkEntity(
                url = "https://en.m.wikipedia.org/wiki/Main_Page",
                title = "Wikipedia",
                domain = "wikipedia.org",
                folder = "Reference",
                monogramLetter = "W",
                accentHex = "#7A8B99",
                isSpeedDial = true,
                sortOrder = 2
            ),
            BookmarkEntity(
                url = "https://news.ycombinator.com",
                title = "Hacker News",
                domain = "news.ycombinator.com",
                folder = "Technology",
                monogramLetter = "Y",
                accentHex = "#D4A359",
                isSpeedDial = true,
                sortOrder = 3
            ),
            BookmarkEntity(
                url = "https://www.theverge.com",
                title = "The Verge",
                domain = "theverge.com",
                folder = "Editorial",
                monogramLetter = "V",
                accentHex = "#4E9F86",
                isSpeedDial = true,
                sortOrder = 4
            ),
            BookmarkEntity(
                url = "https://github.com/trending",
                title = "GitHub",
                domain = "github.com",
                folder = "Engineering",
                monogramLetter = "G",
                accentHex = "#6B7FD7",
                isSpeedDial = true,
                sortOrder = 5
            ),
            BookmarkEntity(
                url = "https://arstechnica.com",
                title = "Ars Technica",
                domain = "arstechnica.com",
                folder = "Technology",
                monogramLetter = "A",
                accentHex = "#C86D51",
                isSpeedDial = true,
                sortOrder = 6
            ),
            BookmarkEntity(
                url = "https://archive.org",
                title = "Archive.org",
                domain = "archive.org",
                folder = "Reference",
                monogramLetter = "I",
                accentHex = "#9C7A54",
                isSpeedDial = true,
                sortOrder = 7
            ),
            BookmarkEntity(
                url = "https://lite.cnn.com",
                title = "CNN Lite",
                domain = "lite.cnn.com",
                folder = "Editorial",
                monogramLetter = "L",
                accentHex = "#8E7DBE",
                isSpeedDial = true,
                sortOrder = 8
            )
        )

        fun defaultOfflineArticles(): List<OfflineArticleEntity> = listOf(
            OfflineArticleEntity(
                url = "https://vellum.local/manifesto/quiet-web",
                title = "The Quiet Web: Why Modern Browsers Need Architectural Restraint",
                domain = "vellum.editorial",
                authorOrByline = "Vellum Design Monograph · No. 01",
                excerpt = "Over the last decade, the web browser transformed from a quiet instrument of discovery into an algorithmic billboard. Here is how we reclaim calm, high-speed reading.",
                paragraphsJoined = listOf(
                    "When Tim Berners-Lee designed the first web browser, its purpose was singular: to render hypertext documents clearly, rapidly, and faithfully to the reader's intent. There were no algorithmic tabloid carousels on the start screen, no floating reward badges, and no hidden telemetry pipelines.",
                    "On mobile devices, the modern web faces two compounding crises. First, pages have ballooned in weight—often loading forty to eighty third-party tracking scripts, real-time bidding auctions, and auto-playing video containers before rendering a single paragraph of prose. Second, mobile browsers themselves have placed their primary controls at the very top of tall glass screens, forcing unnatural two-handed gymnastics.",
                    "Vellum was engineered around three foundational convictions. First, the browser's start screen belongs exclusively to the user's intention—zero news feeds, zero sponsored widgets. Second, every primary navigation action belongs within effortless reach of a single thumb at the bottom of the viewport. Third, ad and tracker blocking must operate transparently at the network and cosmetic layers simultaneously.",
                    "When you tap the Shield emblem in Vellum's bottom dock, you are not looking at a decorative checkbox. You are inspecting a live, sub-millisecond interception pipeline that stops ad servers before a socket opens, collapses empty DOM placeholders, and strips tracking tokens from every link you share.",
                    "You can also use Reader View at any time—even on live articles across the web—to distill complex layouts into Newsreader serif typography on warm Alabaster or Carbon paper."
                ).joinToString("\n\n"),
                wordCount = 248,
                readingMinutes = 2
            )
        )
    }
}
