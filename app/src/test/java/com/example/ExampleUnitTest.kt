package com.example

import com.example.data.SearchEngineOption
import com.example.engine.UrlSanitizerAndSearch
import com.example.engine.VellumShieldEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun braveSearchRouting_andBangShortcuts_resolveCorrectly() {
        val searchResult = UrlSanitizerAndSearch.resolveInputToNavigation(
            rawInput = "minimalist architecture",
            searchEngine = SearchEngineOption.BRAVE,
            stripTrackingParams = true
        )
        assertEquals(
            "https://search.brave.com/search?q=minimalist+architecture",
            searchResult.finalUrl
        )

        val wikiBang = UrlSanitizerAndSearch.resolveInputToNavigation(
            rawInput = "!w Dieter Rams",
            searchEngine = SearchEngineOption.BRAVE,
            stripTrackingParams = true
        )
        assertNotNull(wikiBang.matchedBang)
        assertTrue(wikiBang.finalUrl.contains("wikipedia.org"))
        assertTrue(wikiBang.finalUrl.contains("Dieter+Rams"))
    }

    @Test
    fun cosmeticScriptGenerator_includesAdAndCookieSelectors() {
        val script = VellumShieldEngine.buildCosmeticInjectionScript(
            shieldEnabled = true,
            hideCookieBanners = true,
            customZappedSelectors = listOf("#custom-annoyance")
        )
        assertTrue(script.contains("ins.adsbygoogle"))
        assertTrue(script.contains("#onetrust-consent-sdk"))
        assertTrue(script.contains("#custom-annoyance"))
    }
}
