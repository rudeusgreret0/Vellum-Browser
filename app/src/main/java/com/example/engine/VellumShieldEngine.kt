package com.example.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

enum class TrackerCategory(val label: String, val estimatedBytes: Long) {
    AD_SERVER("Ad Network", 32_000L),
    CROSS_SITE_TRACKER("Analytics & Telemetry", 18_500L),
    FINGERPRINTING("Device Fingerprinting", 24_000L),
    SOCIAL_PIXEL("Social Pixel", 15_000L),
    POPUP_MALWARE("Malicious / Ad Popup", 42_000L)
}

data class BlockedDomainEntry(
    val domain: String,
    val category: TrackerCategory,
    val count: Int = 1,
    val lastTimestamp: Long = System.currentTimeMillis()
)

data class InterceptDecision(
    val shouldBlock: Boolean,
    val matchedDomain: String = "",
    val category: TrackerCategory = TrackerCategory.AD_SERVER,
    val response: WebResourceResponse? = null
)

object VellumShieldEngine {

    // 1x1 transparent GIF (GIF89a) for image ad requests so broken-image icons never appear
    private val TRANSPARENT_GIF_BYTES: ByteArray = byteArrayOf(
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00,
        0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
        0x21, 0xF9.toByte(), 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2C,
        0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02,
        0x02, 0x44, 0x01, 0x00, 0x3B
    )

    // Curated, categorized high-impact ad, tracker, fingerprinting, and social pixel domains
    private val DOMAIN_CATEGORY_MAP: Map<String, TrackerCategory> = buildMap {
        // Major Ad Servers & Real-Time Bidding Networks
        val adServers = listOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adservice.google.com", "pagead2.googlesyndication.com", "tpc.googlesyndication.com",
            "adnxs.com", "amazon-adsystem.com", "aax.amazon-adsystem.com", "criteo.com",
            "criteo.net", "rubiconproject.com", "pubmatic.com", "openx.net",
            "casalemedia.com", "smartadserver.com", "advertising.com", "adform.net",
            "taboola.com", "trc.taboola.com", "outbrain.com", "widgets.outbrain.com",
            "revcontent.com", "mgid.com", "zergnet.com", "teads.tv", "moatads.com",
            "adsafeprotected.com", "doubleverify.com", "2mdn.net", "adroll.com",
            "RhythmOne.com", "sovrn.com", "lijit.com", "3lift.com", "triplelift.com",
            "indexexchange.com", "spotxchange.com", "sharethrough.com", "gumgum.com",
            "kargo.com", "nativo.com", "yieldmo.com", "mediavine.com", "adthrive.com",
            "ezodn.com", "ezoic.net", "carbonads.com", "buysellads.com", "adzerk.net",
            "bidswitch.net", "contextweb.com", "districtm.io", "fyber.com", "inmobi.com",
            "smaato.net", "unityads.unity3d.com", "vungle.com", "applovin.com", "chartboost.com"
        )
        adServers.forEach { put(it.lowercase(), TrackerCategory.AD_SERVER) }

        // Cross-Site Analytics & Behavioral Telemetry
        val analyticsTrackers = listOf(
            "google-analytics.com", "ssl.google-analytics.com", "googletagmanager.com",
            "googletagservices.com", "scorecardresearch.com", "sb.scorecardresearch.com",
            "quantserve.com", "quantcount.com", "omtrdc.net", "2o7.net", "demdex.net",
            "everesttech.net", "chartbeat.com", "chartbeat.net", "parsely.com",
            "newrelic.com", "nr-data.net", "segment.io", "segment.com", "mixpanel.com",
            "amplitude.com", "heapanalytics.com", "kissmetrics.com", "optimizely.com",
            "VWO.com", "crazyegg.com", "mouseflow.com", "luckyorange.com",
            "fullstory.com", "logrocket.io", "smartlook.com", "statcounter.com",
            "histats.com", "yandex.ru/metrika", "mc.yandex.ru", "MATOMO.cloud",
            "pardot.com", "mktoresp.com", "hubspot.com", "hs-analytics.net",
            "hs-scripts.com", "braze.com", "iad-01.braze.com", "clevertap.com",
            "appsflyer.com", "adjust.com", "branch.io", "app.link", "kochava.com",
            "bugsnag.com", "omnicloud.com", "snowplowanalytics.com", "krxd.net",
            "bluekai.com", "exelator.com", "rlcdn.com", "tapad.com", "lotame.com",
            "crwdcntrl.net", "agkn.com", "mathtag.com", "turn.com", "eyeota.net"
        )
        analyticsTrackers.forEach { put(it.lowercase(), TrackerCategory.CROSS_SITE_TRACKER) }

        // Device Fingerprinting & Session Recording
        val fingerprinting = listOf(
            "hotjar.com", "static.hotjar.com", "vars.hotjar.com", "clarity.ms",
            "fpjs.io", "fingerprintjs.com", "api.fpjs.io", "iovation.com",
            "threatmetrix.com", "online-metrix.net", "perimeterx.net", "px-cdn.net",
            "datadome.co", "sift.com", "siftscience.com", "forfend.io", "clientmetrics-pa.googleapis.com"
        )
        fingerprinting.forEach { put(it.lowercase(), TrackerCategory.FINGERPRINTING) }

        // Social Pixels & Cross-Site Graph Trackers
        val socialPixels = listOf(
            "connect.facebook.net", "graph.facebook.com", "pixel.facebook.com",
            "an.facebook.com", "analytics.tiktok.com", "business-api.tiktok.com",
            "ads-twitter.com", "static.ads-twitter.com", "analytics.twitter.com",
            "t.co/i/adsct", "px.ads.linkedin.com", "snap.licdn.com",
            "sc-static.net", "tr.snapchat.com", "ct.pinterest.com", "trk.pinterest.com",
            "events.redditmedia.com", "alb.reddit.com"
        )
        socialPixels.forEach { put(it.lowercase(), TrackerCategory.SOCIAL_PIXEL) }

        // Popups, Redirectors & Malvertising
        val popups = listOf(
            "popads.net", "popcash.net", "propellerads.com", "adcash.com",
            "exoclick.com", "juicyads.com", "trafficjunky.net", "hilltopads.net",
            "clickadu.com", "adsterra.com", "pushcrew.com", "onesignal.com",
            "pushengage.com", "webpushs.com", "izooto.com"
        )
        popups.forEach { put(it.lowercase(), TrackerCategory.POPUP_MALWARE) }
    }

    // URL path/query heuristic patterns for ad scripts hosted on CDNs
    private val URL_SIGNATURE_KEYWORDS = listOf(
        "/pagead/", "/ads/ga-audiences", "/gpt/pubads_impl", "/adserver/",
        "/prebid", "ads.js", "adframe.js", "analytics.js", "fbevents.js",
        "gtm.js", "/pixel/track", "/collect?v=2&tid="
    )

    fun evaluateNetworkRequest(
        requestUri: Uri,
        pageUrl: String?,
        shieldEnabled: Boolean,
        strictMode: Boolean
    ): InterceptDecision {
        if (!shieldEnabled) {
            return InterceptDecision(shouldBlock = false)
        }

        val reqHost = requestUri.host?.lowercase()?.removePrefix("www.") ?: return InterceptDecision(false)
        val fullUrlLower = requestUri.toString().lowercase()

        // 1. Check domain and parent domain segments in O(1) hash map lookups
        var candidate = reqHost
        while (candidate.contains('.')) {
            val matchedCategory = DOMAIN_CATEGORY_MAP[candidate]
            if (matchedCategory != null) {
                return createBlockDecision(
                    matchedDomain = candidate,
                    category = matchedCategory,
                    path = requestUri.path.orEmpty()
                )
            }
            candidate = candidate.substringAfter('.')
        }

        // 2. Check high-confidence URL path signatures
        for (keyword in URL_SIGNATURE_KEYWORDS) {
            if (fullUrlLower.contains(keyword)) {
                return createBlockDecision(
                    matchedDomain = reqHost,
                    category = TrackerCategory.AD_SERVER,
                    path = requestUri.path.orEmpty()
                )
            }
        }

        // 3. In Strict Mode, block cross-origin third-party scripts/pixels with tracking keywords
        if (strictMode && !pageUrl.isNullOrBlank()) {
            val pageHost = runCatching { Uri.parse(pageUrl).host?.lowercase()?.removePrefix("www.") }.getOrNull()
            if (!pageHost.isNullOrBlank() && !isSameRootDomain(reqHost, pageHost)) {
                if (reqHost.startsWith("ads.") ||
                    reqHost.startsWith("track.") ||
                    reqHost.startsWith("telemetry.") ||
                    reqHost.startsWith("metrics.") ||
                    reqHost.startsWith("pixel.") ||
                    reqHost.startsWith("beacon.")
                ) {
                    return createBlockDecision(
                        matchedDomain = reqHost,
                        category = TrackerCategory.FINGERPRINTING,
                        path = requestUri.path.orEmpty()
                    )
                }
            }
        }

        return InterceptDecision(shouldBlock = false)
    }

    private fun isSameRootDomain(hostA: String, hostB: String): Boolean {
        if (hostA == hostB) return true
        val rootA = hostA.split('.').takeLast(2).joinToString(".")
        val rootB = hostB.split('.').takeLast(2).joinToString(".")
        return rootA == rootB
    }

    private fun createBlockDecision(
        matchedDomain: String,
        category: TrackerCategory,
        path: String
    ): InterceptDecision {
        val isImage = path.endsWith(".gif", true) ||
            path.endsWith(".png", true) ||
            path.endsWith(".jpg", true) ||
            path.endsWith(".webp", true)

        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Cache-Control" to "no-store"
        )

        val response = if (isImage) {
            WebResourceResponse(
                "image/gif",
                null,
                200,
                "OK",
                headers,
                ByteArrayInputStream(TRANSPARENT_GIF_BYTES)
            )
        } else {
            WebResourceResponse(
                "text/plain",
                "UTF-8",
                200,
                "OK",
                headers,
                ByteArrayInputStream(ByteArray(0))
            )
        }

        return InterceptDecision(
            shouldBlock = true,
            matchedDomain = matchedDomain,
            category = category,
            response = response
        )
    }

    /**
     * Generates the Stage 2 Cosmetic DOM Filter + Cookie Consent Hider + User Zap Rules JS snippet.
     */
    fun buildCosmeticInjectionScript(
        shieldEnabled: Boolean,
        hideCookieBanners: Boolean,
        customZappedSelectors: List<String>
    ): String {
        val selectors = mutableListOf<String>()

        if (shieldEnabled) {
            selectors.addAll(
                listOf(
                    "ins.adsbygoogle",
                    "[id^='google_ads_']",
                    "[id^='div-gpt-ad']",
                    ".ad-container",
                    ".ad-slot",
                    ".ad-banner",
                    ".ad-wrapper",
                    ".advertisement",
                    ".sponsored-content",
                    ".taboola-container",
                    "#taboola-below-article-thumbnails",
                    ".OUTBRAIN",
                    "[data-ad-slot]",
                    "[data-ad-client]",
                    "[data-testid='ad-slot']",
                    ".vellum-demo-ad-banner"
                )
            )
        }

        if (hideCookieBanners) {
            selectors.addAll(
                listOf(
                    "#onetrust-consent-sdk",
                    "#onetrust-banner-sdk",
                    ".onetrust-pc-dark-filter",
                    "#CybotCookiebotDialog",
                    ".cc-window",
                    ".cc-banner",
                    "#cookie-banner",
                    ".cookie-consent-banner",
                    "[aria-label*='cookie consent' i]",
                    ".qc-cmp2-container",
                    ".fc-consent-root",
                    ".vellum-demo-cookie-banner"
                )
            )
        }

        selectors.addAll(customZappedSelectors)

        if (selectors.isEmpty()) return ""

        val combinedCss = selectors.joinToString(", ") +
            " { display: none !important; visibility: hidden !important; height: 0 !important; min-height: 0 !important; margin: 0 !important; padding: 0 !important; pointer-events: none !important; }"

        val escapedCss = combinedCss.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")

        return """
            (function() {
                try {
                    var styleId = 'vellum-cosmetic-shield-style';
                    var existing = document.getElementById(styleId);
                    if (!existing) {
                        existing = document.createElement('style');
                        existing.id = styleId;
                        (document.head || document.documentElement).appendChild(existing);
                    }
                    existing.textContent = '$escapedCss';
                    var videos = document.querySelectorAll('video');
                    for (var i = 0; i < videos.length; i++) {
                        var v = videos[i];
                        v.preload = 'metadata';
                        if (v.autoplay || v.hasAttribute('autoplay')) {
                            v.autoplay = false;
                            v.removeAttribute('autoplay');
                            v.pause();
                        }
                    }
                } catch (e) {}
            })();
        """.trimIndent()
    }

    /**
     * Stage 3: Interactive "Zap Distraction" Element Picker JS.
     * Lets the user tap any annoying element on the live webpage, highlights it in Burnished Bronze,
     * computes its unique CSS selector, removes it smoothly, and reports it back to VellumBridge.
     */
    fun buildZapModeActivationScript(enable: Boolean): String {
        if (!enable) {
            return """
                (function() {
                    if (window.__vellumZapHandler) {
                        document.removeEventListener('click', window.__vellumZapHandler, true);
                        window.__vellumZapHandler = null;
                    }
                    var badge = document.getElementById('vellum-zap-overlay-hint');
                    if (badge) badge.remove();
                })();
            """.trimIndent()
        }

        return """
            (function() {
                if (window.__vellumZapHandler) {
                    document.removeEventListener('click', window.__vellumZapHandler, true);
                }
                function computeSelector(el) {
                    if (!el || el.nodeType !== 1) return '';
                    if (el.id) return '#' + CSS.escape(el.id);
                    var path = [];
                    var curr = el;
                    var depth = 0;
                    while (curr && curr.nodeType === 1 && curr.tagName.toLowerCase() !== 'html' && depth < 4) {
                        var tag = curr.tagName.toLowerCase();
                        if (curr.id) {
                            path.unshift('#' + CSS.escape(curr.id));
                            break;
                        }
                        var cls = (curr.className && typeof curr.className === 'string')
                            ? curr.className.trim().split(/\s+/).slice(0, 2).map(function(c){ return '.' + CSS.escape(c); }).join('')
                            : '';
                        var parent = curr.parentElement;
                        if (parent) {
                            var siblings = Array.prototype.filter.call(parent.children, function(child) {
                                return child.tagName === curr.tagName;
                            });
                            if (siblings.length > 1) {
                                var idx = siblings.indexOf(curr) + 1;
                                path.unshift(tag + ':nth-of-type(' + idx + ')');
                            } else {
                                path.unshift(tag + cls);
                            }
                        } else {
                            path.unshift(tag + cls);
                        }
                        curr = parent;
                        depth++;
                    }
                    return path.join(' > ');
                }
                window.__vellumZapHandler = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    var target = e.target;
                    if (!target || target.tagName === 'BODY' || target.tagName === 'HTML') return false;
                    var selector = computeSelector(target);
                    target.style.transition = 'all 0.22s ease-out';
                    target.style.outline = '2px solid #C86D51';
                    target.style.backgroundColor = 'rgba(200, 109, 81, 0.25)';
                    target.style.transform = 'scale(0.96)';
                    target.style.opacity = '0';
                    setTimeout(function() {
                        target.style.display = 'none';
                        if (window.VellumBridge && selector) {
                            window.VellumBridge.onElementZapped(selector);
                        }
                    }, 220);
                    document.removeEventListener('click', window.__vellumZapHandler, true);
                    window.__vellumZapHandler = null;
                    return false;
                };
                document.addEventListener('click', window.__vellumZapHandler, true);
            })();
        """.trimIndent()
    }

    /**
     * Extracts clean editorial prose from the current DOM for Vellum's Reader View.
     */
    fun buildReaderExtractionScript(): String {
        return """
            (function() {
                try {
                    var title = document.title || '';
                    var h1 = document.querySelector('h1');
                    if (h1 && h1.innerText && h1.innerText.trim().length > 6) {
                        title = h1.innerText.trim();
                    }
                    var bylineEl = document.querySelector('[rel="author"], .author, .byline, [itemprop="author"]');
                    var byline = bylineEl ? bylineEl.innerText.trim() : '';
                    var root = document.querySelector('article, [role="main"], main, .post-content, .article-body') || document.body;
                    var nodes = root.querySelectorAll('p, h2, h3, blockquote');
                    var paragraphs = [];
                    for (var i = 0; i < nodes.length; i++) {
                        var txt = (nodes[i].innerText || '').replace(/\s+/g, ' ').trim();
                        if (txt.length >= 45) {
                            paragraphs.push(txt);
                        }
                    }
                    if (paragraphs.length === 0) {
                        var bodyText = (document.body.innerText || '').split(/\n+/).map(function(s){ return s.trim(); }).filter(function(s){ return s.length > 40; });
                        paragraphs = bodyText.slice(0, 35);
                    }
                    var joined = paragraphs.join('\n\n');
                    if (window.VellumBridge) {
                        window.VellumBridge.onReaderArticleExtracted(title, byline, joined);
                    }
                } catch (e) {
                    if (window.VellumBridge) {
                        window.VellumBridge.onReaderArticleExtracted(document.title || 'Article', '', '');
                    }
                }
            })();
        """.trimIndent()
    }

    /**
     * Built-in Interactive Shield & Reader Test Bench (`vellum://shield-test`)
     * Fires real requests to known ad/tracker domains so `shouldInterceptRequest` catches them live,
     * includes cosmetic ad slots, cookie banners, tracking links, and a full editorial article!
     */
    fun buildShieldTestBenchHtml(shieldEnabled: Boolean): String {
        val statusLabel = if (shieldEnabled) "ACTIVE & INTERCEPTING" else "PAUSED ON THIS SITE"
        val statusColor = if (shieldEnabled) "#4E9F86" else "#D9534F"
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Vellum Shield Live Diagnostic & Editorial Test Bench</title>
                <style>
                    :root {
                        color-scheme: dark light;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        background: #121316;
                        color: #ECEAE4;
                        margin: 0;
                        padding: 20px 18px 96px 18px;
                        line-height: 1.65;
                    }
                    .badge {
                        display: inline-block;
                        font-family: monospace;
                        font-size: 11px;
                        letter-spacing: 0.08em;
                        padding: 4px 10px;
                        border-radius: 99px;
                        background: rgba(78, 159, 134, 0.16);
                        color: $statusColor;
                        border: 1px solid $statusColor;
                        margin-bottom: 12px;
                    }
                    h1 {
                        font-size: 24px;
                        line-height: 1.25;
                        margin: 0 0 8px 0;
                        color: #F7F6F2;
                    }
                    .byline {
                        font-size: 13px;
                        color: #94928C;
                        margin-bottom: 20px;
                    }
                    .card {
                        background: #1A1C22;
                        border: 1px solid #2A2D35;
                        border-radius: 14px;
                        padding: 16px;
                        margin: 16px 0;
                    }
                    .vellum-demo-ad-banner {
                        background: #2C1919;
                        border: 1px dashed #D9534F;
                        color: #F2B8B5;
                        padding: 18px;
                        border-radius: 12px;
                        margin: 16px 0;
                        text-align: center;
                        font-family: monospace;
                    }
                    .vellum-demo-cookie-banner {
                        background: #262318;
                        border: 1px solid #D4A359;
                        color: #F5DFB8;
                        padding: 14px;
                        border-radius: 12px;
                        margin: 14px 0;
                        font-size: 13px;
                    }
                    .zap-target {
                        background: linear-gradient(135deg, #25221E, #1D1A17);
                        border: 1px solid #C86D51;
                        border-radius: 12px;
                        padding: 14px;
                        margin: 16px 0;
                    }
                    a {
                        color: #D4A359;
                    }
                    code {
                        font-family: monospace;
                        background: #22242C;
                        padding: 2px 6px;
                        border-radius: 5px;
                        font-size: 12px;
                    }
                </style>
                <!-- Live Simulated Third-Party Ad & Tracker Calls intercepted by VellumShieldEngine -->
                <script src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js" async></script>
                <script src="https://securepubads.g.doubleclick.net/tag/js/gpt.js" async></script>
                <script src="https://www.google-analytics.com/analytics.js" async></script>
                <script src="https://cdn.taboola.com/libtrc/impl.js" async></script>
                <script src="https://widgets.outbrain.com/outbrain.js" async></script>
                <script src="https://static.hotjar.com/c/hotjar-1000.js" async></script>
                <script src="https://connect.facebook.net/en_US/fbevents.js" async></script>
                <script src="https://sb.scorecardresearch.com/beacon.js" async></script>
            </head>
            <body>
                <div class="badge">SHIELD ENGINE: $statusLabel</div>
                <h1>Architecture of a Quiet Browser: Live Shield & Reader Benchmark</h1>
                <div class="byline">By Vellum Systems Engineering · Interactive Verification Page</div>

                <div class="vellum-demo-ad-banner">
                    [COSMETIC AD SLOT] 300×250 Programmatic Billboard<br/>
                    <small>When Vellum Shield is enabled, Stage 2 Cosmetic CSS collapses this slot to 0px automatically. Toggle Shield off in the bottom bar to see it reappear!</small>
                </div>

                <div class="vellum-demo-cookie-banner">
                    <strong>GDPR Cookie Consent Overlay:</strong> Hidden automatically when Vellum's Cookie Banner Blocker is active.
                </div>

                <div class="zap-target" id="annoying-newsletter-callout">
                    <strong>⚡ Try "Zap Distraction" On This Box:</strong><br/>
                    Tap the Shield icon in the bottom bar → select <code>Zap Distraction</code> → then tap this card to permanently vaporize it from the DOM!
                </div>

                <p>This live diagnostic page triggers <strong>8 real third-party network requests</strong> to <code>doubleclick.net</code>, <code>googlesyndication.com</code>, <code>google-analytics.com</code>, <code>taboola.com</code>, <code>outbrain.com</code>, <code>hotjar.com</code>, <code>connect.facebook.net</code>, and <code>scorecardresearch.com</code>.</p>

                <p>Tap the <strong>Shield emblem</strong> in the bottom dock right now to inspect the live Monospace Tracker Log and see every intercepted domain, its tracker classification, and the bandwidth saved before a single socket opened.</p>

                <h2>Testing Reader View & Link Peek</h2>
                <p>You can also tap the <strong>Reader View (Book icon)</strong> in the bottom address bar right now to extract the prose on this page into Vellum's calm editorial layout with Newsreader Serif typography and Alabaster, Warm Sepia, or Carbon Night paper themes.</p>

                <p>Long-press any link below to trigger <strong>Vellum Peek</strong> (an instant floating preview sheet) or tap to test automatic <code>utm_source</code> and <code>fbclid</code> tracking parameter removal:</p>

                <div class="card">
                    <p style="margin-top:0;"><strong>1. Dirty Tracking Link (Tests URL Sanitizer):</strong><br/>
                    <a href="https://en.m.wikipedia.org/wiki/Web_browser?utm_source=newsletter&utm_medium=email&fbclid=IwAR9988776655&gclid=Cj0KCQjw">wikipedia.org/wiki/Web_browser?utm_source=...&amp;fbclid=...</a></p>
                    <p style="margin-bottom:0;"><strong>2. Hacker News Frontpage:</strong><br/>
                    <a href="https://news.ycombinator.com">https://news.ycombinator.com</a></p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
