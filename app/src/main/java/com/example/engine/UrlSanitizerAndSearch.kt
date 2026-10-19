package com.example.engine

import android.net.Uri
import com.example.data.SearchEngineOption
import java.net.URLEncoder

data class BangShortcut(
    val trigger: String,
    val name: String,
    val description: String,
    val urlTemplate: String
)

data class SanitizedNavigationResult(
    val finalUrl: String,
    val strippedParamsCount: Int,
    val strippedParamNames: List<String>,
    val matchedBang: BangShortcut? = null
)

object UrlSanitizerAndSearch {

    val bangShortcuts: List<BangShortcut> = listOf(
        BangShortcut(
            trigger = "!b",
            name = "Brave Search",
            description = "Private independent index",
            urlTemplate = "https://search.brave.com/search?q=%s"
        ),
        BangShortcut(
            trigger = "!w",
            name = "Wikipedia",
            description = "Direct encyclopedia lookup",
            urlTemplate = "https://en.m.wikipedia.org/w/index.php?search=%s"
        ),
        BangShortcut(
            trigger = "!hn",
            name = "Hacker News",
            description = "Search Algolia HN index",
            urlTemplate = "https://hn.algolia.com/?q=%s"
        ),
        BangShortcut(
            trigger = "!gh",
            name = "GitHub",
            description = "Search repositories & code",
            urlTemplate = "https://github.com/search?q=%s"
        ),
        BangShortcut(
            trigger = "!yt",
            name = "YouTube",
            description = "Direct video search",
            urlTemplate = "https://m.youtube.com/results?search_query=%s"
        ),
        BangShortcut(
            trigger = "!r",
            name = "Reddit",
            description = "Search discussions & threads",
            urlTemplate = "https://www.reddit.com/search/?q=%s"
        ),
        BangShortcut(
            trigger = "!ar",
            name = "Archive.org",
            description = "Wayback Machine & texts",
            urlTemplate = "https://archive.org/search?query=%s"
        ),
        BangShortcut(
            trigger = "!ddg",
            name = "DuckDuckGo",
            description = "Private search query",
            urlTemplate = "https://duckduckgo.com/?q=%s"
        )
    )

    private val trackingParamExactNames = setOf(
        "fbclid", "gclid", "dclid", "gbraid", "wbraid", "msclkid", "twclid",
        "yclid", "mc_cid", "mc_eid", "igshid", "_hsenc", "_hsmi", "hsCtaTracking",
        "vero_id", "rb_clickid", "s_kwcid", "mkt_tok", "oly_enc_id", "oly_anon_id",
        "spm", "scm", "ref_src", "ref_url", "si"
    )

    private val trackingParamPrefixes = listOf(
        "utm_", "ga_", "pk_", "hsa_", "matomo_"
    )

    fun detectActiveBang(rawInput: String): BangShortcut? {
        val trimmed = rawInput.trim()
        if (!trimmed.startsWith("!")) return null
        val firstToken = trimmed.substringBefore(" ").lowercase()
        return bangShortcuts.firstOrNull { it.trigger == firstToken }
    }

    fun resolveInputToNavigation(
        rawInput: String,
        searchEngine: SearchEngineOption,
        stripTrackingParams: Boolean
    ): SanitizedNavigationResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return SanitizedNavigationResult(
                finalUrl = searchEngine.homeUrl,
                strippedParamsCount = 0,
                strippedParamNames = emptyList()
            )
        }

        if (trimmed.equals("vellum://shield-test", ignoreCase = true)) {
            return SanitizedNavigationResult(
                finalUrl = "vellum://shield-test",
                strippedParamsCount = 0,
                strippedParamNames = emptyList()
            )
        }

        // Check !bang shortcuts first (e.g. "!w Quantum Computing")
        if (trimmed.startsWith("!")) {
            val parts = trimmed.split(" ", limit = 2)
            val bangToken = parts[0].lowercase()
            val matchedBang = bangShortcuts.firstOrNull { it.trigger == bangToken }
            if (matchedBang != null) {
                val queryPart = if (parts.size > 1) parts[1].trim() else ""
                val encoded = URLEncoder.encode(queryPart, "UTF-8")
                val targetUrl = if (queryPart.isEmpty()) {
                    matchedBang.urlTemplate.substringBefore("?").substringBefore("/search")
                } else {
                    String.format(matchedBang.urlTemplate, encoded)
                }
                return SanitizedNavigationResult(
                    finalUrl = targetUrl,
                    strippedParamsCount = 0,
                    strippedParamNames = emptyList(),
                    matchedBang = matchedBang
                )
            }
        }

        // Determine if input is a direct URL or domain vs a search query
        val isLikelyUrl = isUrlOrDomain(trimmed)
        if (!isLikelyUrl) {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            return SanitizedNavigationResult(
                finalUrl = String.format(searchEngine.queryTemplate, encoded),
                strippedParamsCount = 0,
                strippedParamNames = emptyList()
            )
        }

        val normalizedUrl = if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("vellum://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }

        return if (stripTrackingParams) {
            sanitizeUrl(normalizedUrl)
        } else {
            SanitizedNavigationResult(
                finalUrl = normalizedUrl,
                strippedParamsCount = 0,
                strippedParamNames = emptyList()
            )
        }
    }

    fun sanitizeUrl(url: String): SanitizedNavigationResult {
        if (!url.contains("?")) {
            return SanitizedNavigationResult(url, 0, emptyList())
        }
        return runCatching {
            val uri = Uri.parse(url)
            val queryNames = uri.queryParameterNames
            if (queryNames.isEmpty()) {
                return SanitizedNavigationResult(url, 0, emptyList())
            }
            val strippedNames = mutableListOf<String>()
            val builder = uri.buildUpon().clearQuery()
            for (paramName in queryNames) {
                val lower = paramName.lowercase()
                val isTracker = trackingParamExactNames.contains(lower) ||
                    trackingParamPrefixes.any { lower.startsWith(it) }
                if (isTracker) {
                    strippedNames.add(paramName)
                } else {
                    uri.getQueryParameters(paramName).forEach { value ->
                        builder.appendQueryParameter(paramName, value)
                    }
                }
            }
            SanitizedNavigationResult(
                finalUrl = builder.build().toString(),
                strippedParamsCount = strippedNames.size,
                strippedParamNames = strippedNames
            )
        }.getOrDefault(SanitizedNavigationResult(url, 0, emptyList()))
    }

    fun formatCleanDisplayDomain(url: String): String {
        if (url.isBlank()) return ""
        if (url.startsWith("vellum://shield-test")) return "vellum://shield-test"
        return runCatching {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase()?.removePrefix("www.") ?: return url
            host
        }.getOrDefault(url)
    }

    private fun isUrlOrDomain(input: String): Boolean {
        if (input.contains(" ") || input.contains("\n")) return false
        if (input.startsWith("http://", true) || input.startsWith("https://", true)) return true
        val dotIndex = input.indexOf('.')
        if (dotIndex <= 0 || dotIndex == input.length - 1) return false
        val tldPart = input.substringAfterLast('.').substringBefore('/')
        return tldPart.length in 2..12 && tldPart.all { it.isLetter() }
    }
}
