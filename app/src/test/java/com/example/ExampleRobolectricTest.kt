package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.engine.TrackerCategory
import com.example.engine.UrlSanitizerAndSearch
import com.example.engine.VellumShieldEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun appName_andShieldNetworkInterception_workCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vellum Browser", appName)

        // Verify Stage 1 Network Ad & Tracker Interception
        val adUri = Uri.parse("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        val adDecision = VellumShieldEngine.evaluateNetworkRequest(
            requestUri = adUri,
            pageUrl = "https://www.theverge.com",
            shieldEnabled = true,
            strictMode = false
        )
        assertTrue(adDecision.shouldBlock)
        assertEquals(TrackerCategory.AD_SERVER, adDecision.category)

        // Verify legitimate first-party requests are never blocked
        val cleanUri = Uri.parse("https://en.m.wikipedia.org/wiki/Architecture")
        val cleanDecision = VellumShieldEngine.evaluateNetworkRequest(
            requestUri = cleanUri,
            pageUrl = "https://en.m.wikipedia.org",
            shieldEnabled = true,
            strictMode = true
        )
        assertFalse(cleanDecision.shouldBlock)

        // Verify Stage 3 URL Tracking Parameter Sanitizer
        val dirtyUrl = "https://www.theverge.com/2026/tech?utm_source=twitter&utm_medium=social&fbclid=IwAR12345&id=42"
        val sanitized = UrlSanitizerAndSearch.sanitizeUrl(dirtyUrl)
        assertEquals(3, sanitized.strippedParamsCount)
        assertEquals("https://www.theverge.com/2026/tech?id=42", sanitized.finalUrl)
    }
}
