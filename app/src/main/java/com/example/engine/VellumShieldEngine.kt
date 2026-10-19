package com.example.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

enum class TrackerCategory(val label: String, val estimatedBytes: Long) {
    AD_SERVER("Ad Network", 34_000L),
    CROSS_SITE_TRACKER("Tracker", 18_500L),
    FINGERPRINTING("Fingerprinting", 24_000L),
    SOCIAL_PIXEL("Social Pixel", 15_000L),
    POPUP_MALWARE("Popup / Redirect", 45_000L)
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

    private val CORS_HEADERS = mapOf(
        "Access-Control-Allow-Origin" to "*",
        "Access-Control-Allow-Methods" to "GET, POST, OPTIONS",
        "Cache-Control" to "no-store"
    )

    // Comprehensive Ad, Tracker, Popup, Redirect, Betting & Malvertising Domain Set
    private val DOMAIN_CATEGORY_MAP: Map<String, TrackerCategory> = buildMap {
        val adServers = listOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adservice.google.com", "pagead2.googlesyndication.com", "tpc.googlesyndication.com",
            "afs.googlesyndication.com", "ade.googlesyndication.com", "adnxs.com", "ib.adnxs.com",
            "amazon-adsystem.com", "aax.amazon-adsystem.com", "c.amazon-adsystem.com",
            "criteo.com", "criteo.net", "rubiconproject.com", "fastlane.rubiconproject.com",
            "pubmatic.com", "ads.pubmatic.com", "openx.net", "casalemedia.com",
            "smartadserver.com", "advertising.com", "adform.net", "track.adform.net",
            "taboola.com", "trc.taboola.com", "cdn.taboola.com", "outbrain.com",
            "widgets.outbrain.com", "odb.outbrain.com", "revcontent.com", "trends.revcontent.com",
            "mgid.com", "jsc.mgid.com", "zergnet.com", "teads.tv", "a.teads.tv",
            "moatads.com", "z.moatads.com", "adsafeprotected.com", "pixel.adsafeprotected.com",
            "doubleverify.com", "tps.doubleverify.com", "2mdn.net", "s0.2mdn.net",
            "adroll.com", "d.adroll.com", "rhythmone.com", "sovrn.com", "ap.lijit.com",
            "lijit.com", "3lift.com", "tlx.3lift.com", "triplelift.com", "indexexchange.com",
            "spotxchange.com", "sharethrough.com", "gumgum.com", "kargo.com", "nativo.com",
            "yieldmo.com", "mediavine.com", "scripts.mediavine.com", "adthrive.com",
            "ezodn.com", "ezoic.net", "go.ezoic.net", "carbonads.com", "srv.carbonads.com",
            "buysellads.com", "s3.buysellads.com", "adzerk.net", "bidswitch.net",
            "contextweb.com", "districtm.io", "fyber.com", "inmobi.com", "smaato.net",
            "unityads.unity3d.com", "vungle.com", "applovin.com", "chartboost.com",
            "ads.yahoo.com", "gemini.yahoo.com", "adtech.de", "adtechus.com", "flashtalking.com",
            "serving-sys.com", "bs.serving-sys.com", "eyeviewdigital.com", "innovid.com",
            "celtra.com", "jivox.com", "undertone.com", "exponential.com", "tribalfusion.com",
            "conversantmedia.com", "cj.com", "linksynergy.com", "impact.com", "impactradius-event.com",
            "skimresources.com", "s.skimresources.com", "viglink.com", "cdn.viglink.com",
            "bidgear.com", "vdo.ai", "vidazoo.com", "connatix.com", "primis.tech",
            "anyclip.com", "playwire.com", "freestar.com", "a.pub.network", "snigelweb.com",
            "valueimpression.com", "monetizemore.com", "optad360.io", "setupad.com",
            "adrecover.com", "blockthrough.com", "pagefair.com", "pagefair.net"
        )
        adServers.forEach { put(it.lowercase(), TrackerCategory.AD_SERVER) }

        val analyticsTrackers = listOf(
            "google-analytics.com", "ssl.google-analytics.com", "www.google-analytics.com",
            "googletagmanager.com", "www.googletagmanager.com", "googletagservices.com",
            "scorecardresearch.com", "sb.scorecardresearch.com", "quantserve.com",
            "pixel.quantserve.com", "quantcount.com", "omtrdc.net", "2o7.net", "demdex.net",
            "everesttech.net", "chartbeat.com", "static.chartbeat.com", "chartbeat.net",
            "parsely.com", "cdn.parsely.com", "newrelic.com", "js-agent.newrelic.com",
            "nr-data.net", "bam.nr-data.net", "segment.io", "cdn.segment.com", "segment.com",
            "mixpanel.com", "cdn.mxpnl.com", "amplitude.com", "api.amplitude.com",
            "heapanalytics.com", "kissmetrics.com", "optimizely.com", "cdn.optimizely.com",
            "vwo.com", "dev.visualwebsiteoptimizer.com", "crazyegg.com", "script.crazyegg.com",
            "mouseflow.com", "luckyorange.com", "fullstory.com", "rs.fullstory.com",
            "logrocket.io", "smartlook.com", "statcounter.com", "c.statcounter.com",
            "histats.com", "s10.histats.com", "mc.yandex.ru", "metrika.yandex.ru",
            "matomo.cloud", "pardot.com", "mktoresp.com", "munchkin.marketo.net",
            "hubspot.com", "js.hs-analytics.net", "hs-analytics.net", "hs-scripts.com",
            "braze.com", "iad-01.braze.com", "clevertap.com", "appsflyer.com",
            "adjust.com", "branch.io", "app.link", "kochava.com", "bugsnag.com",
            "snowplowanalytics.com", "krxd.net", "cdn.krxd.net", "bluekai.com",
            "tags.bluekai.com", "exelator.com", "rlcdn.com", "tapad.com", "lotame.com",
            "crwdcntrl.net", "bcp.crwdcntrl.net", "agkn.com", "mathtag.com", "turn.com",
            "eyeota.net", "liadm.com", "id5-sync.com", "criteo.com"
        )
        analyticsTrackers.forEach { put(it.lowercase(), TrackerCategory.CROSS_SITE_TRACKER) }

        val fingerprinting = listOf(
            "hotjar.com", "static.hotjar.com", "vars.hotjar.com", "script.hotjar.com",
            "clarity.ms", "www.clarity.ms", "fpjs.io", "fingerprintjs.com", "api.fpjs.io",
            "iovation.com", "threatmetrix.com", "online-metrix.net", "perimeterx.net",
            "px-cdn.net", "datadome.co", "sift.com", "siftscience.com",
            "clientmetrics-pa.googleapis.com"
        )
        fingerprinting.forEach { put(it.lowercase(), TrackerCategory.FINGERPRINTING) }

        val socialPixels = listOf(
            "connect.facebook.net", "graph.facebook.com", "pixel.facebook.com",
            "an.facebook.com", "analytics.tiktok.com", "business-api.tiktok.com",
            "ads-twitter.com", "static.ads-twitter.com", "analytics.twitter.com",
            "px.ads.linkedin.com", "snap.licdn.com", "sc-static.net", "tr.snapchat.com",
            "ct.pinterest.com", "trk.pinterest.com", "events.redditmedia.com", "alb.reddit.com"
        )
        socialPixels.forEach { put(it.lowercase(), TrackerCategory.SOCIAL_PIXEL) }

        val popupsAndRedirects = listOf(
            "popads.net", "popcash.net", "propellerads.com", "adcash.com",
            "exoclick.com", "main.exoclick.com", "syndication.exoclick.com",
            "realsrv.com", "syndication.realsrv.com", "tsyndicate.com", "juicyads.com",
            "trafficjunky.net", "hilltopads.net", "clickadu.com", "adsterra.com",
            "highperformanceformat.com", "effectivegatecpm.com", "topcreativeformat.com",
            "profitablecpmrate.com", "plugrush.com", "trafficstars.com", "trafficfactory.biz",
            "admaven.com", "galaksion.com", "richads.com", "mondiad.com", "evadav.com",
            "rollerads.com", "dao.ad", "adspyglass.com", "pushcrew.com", "onesignal.com",
            "cdn.onesignal.com", "pushengage.com", "webpushs.com", "izooto.com",
            "1xbet.com", "mostbet.com", "betwinner.com", "parimatch.com", "melbet.com",
            "4rabet.com", "1win.com", "linebet.com", "dafabet.com", "bet365.com"
        )
        popupsAndRedirects.forEach { put(it.lowercase(), TrackerCategory.POPUP_MALWARE) }
    }

    // High-confidence URL path/query signatures for ad & tracker scripts
    private val URL_SIGNATURE_KEYWORDS = listOf(
        "/pagead/", "/ads/ga-audiences", "/gpt/pubads_impl", "/adserver",
        "/prebid", "/ads.js", "/adframe", "/analytics.js", "/fbevents.js",
        "/gtm.js", "/pixel/track", "/collect?v=2&tid=", "adsbygoogle",
        "doubleclick.net", "googlesyndication", "/banner_ad", "/popunder",
        "/pop.js", "/pu.js", "/ad.php", "/ads.php", "/show_ads", "/adview",
        "/adclick", "/sponsor/", "/sponsored/", "adsystem.com", "taboola",
        "outbrain", "propellerads", "adsterra", "exoclick", "realsrv.com",
        "highperformanceformat", "effectivegatecpm"
    )

    /**
     * Checks if a top-level navigation (e.g. popup or click hijack) targets a known
     * popup, malvertising, or ad-redirect domain.
     */
    fun isMaliciousRedirectOrPopup(targetUri: Uri, currentPageUrl: String?): Boolean {
        val host = targetUri.host?.lowercase()?.removePrefix("www.") ?: return false
        val currentHost = runCatching {
            Uri.parse(currentPageUrl.orEmpty()).host?.lowercase()?.removePrefix("www.")
        }.getOrNull()

        // If the user is already on that exact root domain, allow normal navigation
        if (!currentHost.isNullOrBlank() && isSameRootDomain(host, currentHost)) {
            return false
        }

        var candidate = host
        while (candidate.contains('.')) {
            val cat = DOMAIN_CATEGORY_MAP[candidate]
            if (cat == TrackerCategory.POPUP_MALWARE || cat == TrackerCategory.AD_SERVER) {
                return true
            }
            candidate = candidate.substringAfter('.')
        }

        val fullLower = targetUri.toString().lowercase()
        return fullLower.contains("popunder") ||
            fullLower.contains("adclick") ||
            fullLower.contains("doubleclick.net/pcs/click") ||
            fullLower.contains("googleadservices.com/pagead/aclk") ||
            fullLower.contains("effectivegatecpm") ||
            fullLower.contains("highperformanceformat")
    }

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

        // 2. Check high-confidence URL path/query signatures
        for (keyword in URL_SIGNATURE_KEYWORDS) {
            if (fullUrlLower.contains(keyword)) {
                return createBlockDecision(
                    matchedDomain = reqHost,
                    category = TrackerCategory.AD_SERVER,
                    path = requestUri.path.orEmpty()
                )
            }
        }

        // 3. Cross-origin subdomain heuristics for ad/tracker hosts
        if (!pageUrl.isNullOrBlank()) {
            val pageHost = runCatching { Uri.parse(pageUrl).host?.lowercase()?.removePrefix("www.") }.getOrNull()
            if (!pageHost.isNullOrBlank() && !isSameRootDomain(reqHost, pageHost)) {
                if (reqHost.startsWith("ads.") ||
                    reqHost.startsWith("ad.") ||
                    reqHost.startsWith("adserver.") ||
                    reqHost.startsWith("banner.") ||
                    reqHost.startsWith("banners.") ||
                    reqHost.startsWith("pop.") ||
                    reqHost.startsWith("popads.") ||
                    (strictMode && (
                        reqHost.startsWith("track.") ||
                            reqHost.startsWith("tracker.") ||
                            reqHost.startsWith("telemetry.") ||
                            reqHost.startsWith("metrics.") ||
                            reqHost.startsWith("pixel.") ||
                            reqHost.startsWith("beacon.") ||
                            reqHost.startsWith("stat.") ||
                            reqHost.startsWith("stats.")
                        ))
                ) {
                    return createBlockDecision(
                        matchedDomain = reqHost,
                        category = TrackerCategory.AD_SERVER,
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
            path.endsWith(".webp", true) ||
            path.endsWith(".svg", true)

        val isScript = path.endsWith(".js", true)

        val response = when {
            isImage -> WebResourceResponse(
                "image/gif",
                null,
                200,
                "OK",
                CORS_HEADERS,
                ByteArrayInputStream(TRANSPARENT_GIF_BYTES)
            )
            isScript -> WebResourceResponse(
                "application/javascript",
                "UTF-8",
                200,
                "OK",
                CORS_HEADERS,
                ByteArrayInputStream("/* blocked by Vellum Shield */".toByteArray())
            )
            else -> WebResourceResponse(
                "text/plain",
                "UTF-8",
                200,
                "OK",
                CORS_HEADERS,
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
     * Stage 2 & 3: Live MutationObserver DOM Scrubber + YouTube/Video Ad Skipper + Popup Neutralizer.
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
                    "iframe[src*='doubleclick.net']",
                    "iframe[src*='googlesyndication.com']",
                    "iframe[src*='adnxs.com']",
                    "iframe[src*='taboola.com']",
                    "iframe[src*='outbrain.com']",
                    "iframe[src*='exoclick.com']",
                    "iframe[src*='propellerads']",
                    "iframe[src*='adsterra']",
                    "iframe[id*='google_ads']",
                    ".ad-container",
                    ".ad-slot",
                    ".ad-banner",
                    ".ad-wrapper",
                    ".ad-unit",
                    ".ad-box",
                    ".ad-placement",
                    ".ad-leaderboard",
                    ".sticky-ad",
                    ".bottom-ad",
                    ".floating-ad",
                    ".advertisement",
                    ".sponsored-content",
                    ".taboola-container",
                    "#taboola-below-article-thumbnails",
                    ".OUTBRAIN",
                    "[data-ad-slot]",
                    "[data-ad-client]",
                    "[data-ad-unit]",
                    "[data-testid='ad-slot']",
                    "ytd-ad-slot-renderer",
                    "ytd-in-feed-ad-layout-renderer",
                    "ytd-banner-promo-renderer",
                    "ytm-promoted-sparkles-web-renderer",
                    "ytm-promoted-video-renderer",
                    "ytm-companion-ad-renderer",
                    ".ytp-ad-overlay-container",
                    ".ytp-ad-message-container",
                    "#player-ads",
                    "#masthead-ad",
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
            " { display: none !important; visibility: hidden !important; height: 0 !important; min-height: 0 !important; max-height: 0 !important; margin: 0 !important; padding: 0 !important; opacity: 0 !important; pointer-events: none !important; overflow: hidden !important; }"

        val escapedCss = combinedCss.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")

        return """
            (function() {
                try {
                    // 1. Neutralize window.open popups and pop-unders
                    if ($shieldEnabled && !window.__vellumPopupGuarded) {
                        window.__vellumPopupGuarded = true;
                        window.open = function() { return null; };
                    }

                    // 2. Inject persistent Cosmetic Ad & Cookie Hiding CSS
                    var styleId = 'vellum-cosmetic-shield-style';
                    var existing = document.getElementById(styleId);
                    if (!existing) {
                        existing = document.createElement('style');
                        existing.id = styleId;
                        (document.head || document.documentElement).appendChild(existing);
                    }
                    if (existing.textContent !== '$escapedCss') {
                        existing.textContent = '$escapedCss';
                    }

                    // 3. Active DOM Scrubber + YouTube / Video Ad Auto-Skipper
                    function scrubAdsAndVideoOverlays() {
                        if (!$shieldEnabled) return;
                        try {
                            // Auto-click YouTube & HTML5 Video Ad Skip buttons immediately
                            var skipSelectors = [
                                '.ytp-ad-skip-button',
                                '.ytp-ad-skip-button-modern',
                                '.ytp-skip-ad-button',
                                '.ytp-ad-overlay-close-button',
                                '.videoAdUiSkipButton'
                            ];
                            for (var s = 0; s < skipSelectors.length; s++) {
                                var btn = document.querySelector(skipSelectors[s]);
                                if (btn) { btn.click(); }
                            }

                            // Fast-forward active YouTube pre-roll/mid-roll video ads
                            var adPlayer = document.querySelector('.ad-showing video, .ytp-ad-player-overlay video');
                            if (adPlayer && !isNaN(adPlayer.duration) && adPlayer.duration > 0) {
                                adPlayer.muted = true;
                                adPlayer.currentTime = adPlayer.duration;
                            }

                            // Remove suspicious full-screen transparent click-hijack overlays
                            var fixedNodes = document.querySelectorAll('div[style*="z-index: 2147483647"], div[style*="z-index:2147483647"], a[target="_blank"][style*="position: fixed"]');
                            for (var f = 0; f < fixedNodes.length; f++) {
                                fixedNodes[f].remove();
                            }
                        } catch (innerErr) {}
                    }

                    scrubAdsAndVideoOverlays();

                    if ($shieldEnabled && !window.__vellumObserverActive) {
                        window.__vellumObserverActive = true;
                        setInterval(scrubAdsAndVideoOverlays, 650);
                    }
                } catch (e) {}
            })();
        """.trimIndent()
    }

    /**
     * Interactive "Zap Distraction" Element Picker JS.
     */
    fun buildZapModeActivationScript(enable: Boolean): String {
        if (!enable) {
            return """
                (function() {
                    if (window.__vellumZapHandler) {
                        document.removeEventListener('click', window.__vellumZapHandler, true);
                        window.__vellumZapHandler = null;
                    }
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
                    target.style.transition = 'all 0.2s ease-out';
                    target.style.outline = '2px solid #00E5C3';
                    target.style.transform = 'scale(0.92)';
                    target.style.opacity = '0';
                    setTimeout(function() {
                        target.style.display = 'none';
                        if (window.VellumBridge && selector) {
                            window.VellumBridge.onElementZapped(selector);
                        }
                    }, 200);
                    document.removeEventListener('click', window.__vellumZapHandler, true);
                    window.__vellumZapHandler = null;
                    return false;
                };
                document.addEventListener('click', window.__vellumZapHandler, true);
            })();
        """.trimIndent()
    }

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
     * Interactive Ad Blocker + File Upload + File Download Test Studio (`vellum://shield-test`)
     * Visual, minimal text, interactive buttons to test:
     * 1) Live Ad & Tracker Blocking
     * 2) Real File Upload (<input type="file">) from phone
     * 3) Real File Download to phone
     */
    fun buildShieldTestBenchHtml(shieldEnabled: Boolean): String {
        val statusLabel = if (shieldEnabled) "SHIELD ACTIVE" else "SHIELD PAUSED"
        val statusColor = if (shieldEnabled) "#00E5C3" else "#FB7185"
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Vellum Test Studio</title>
                <style>
                    * { box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        background: linear-gradient(160deg, #0A1128 0%, #132247 50%, #1D3557 100%);
                        color: #F8FAFC;
                        margin: 0;
                        padding: 20px 16px 100px 16px;
                        min-height: 100vh;
                    }
                    .pill {
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        font-size: 12px;
                        font-weight: 700;
                        padding: 6px 14px;
                        border-radius: 99px;
                        background: rgba(0, 229, 195, 0.15);
                        color: $statusColor;
                        border: 1px solid $statusColor;
                        margin-bottom: 16px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: 1fr;
                        gap: 14px;
                    }
                    .card {
                        background: rgba(22, 35, 64, 0.85);
                        border: 1px solid rgba(100, 223, 223, 0.25);
                        border-radius: 18px;
                        padding: 18px;
                    }
                    .card h3 {
                        margin: 0 0 8px 0;
                        font-size: 17px;
                        color: #00E5C3;
                    }
                    .card p {
                        margin: 0 0 14px 0;
                        font-size: 13px;
                        color: #94A3B8;
                    }
                    .btn {
                        display: inline-block;
                        background: #00E5C3;
                        color: #04151F;
                        font-weight: 700;
                        font-size: 14px;
                        padding: 12px 18px;
                        border-radius: 12px;
                        text-decoration: none;
                        border: none;
                        cursor: pointer;
                        width: 100%;
                        text-align: center;
                    }
                    .vellum-demo-ad-banner {
                        background: rgba(251, 113, 133, 0.2);
                        border: 2px dashed #FB7185;
                        color: #FFE4E6;
                        padding: 18px;
                        border-radius: 14px;
                        text-align: center;
                        margin-bottom: 14px;
                    }
                    #upload-status {
                        margin-top: 10px;
                        font-size: 13px;
                        color: #38BDF8;
                        font-weight: 600;
                    }
                </style>
                <!-- Real Ad & Tracker Calls Intercepted by VellumShieldEngine -->
                <script src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js" async></script>
                <script src="https://securepubads.g.doubleclick.net/tag/js/gpt.js" async></script>
                <script src="https://www.google-analytics.com/analytics.js" async></script>
                <script src="https://cdn.taboola.com/libtrc/impl.js" async></script>
                <script src="https://widgets.outbrain.com/outbrain.js" async></script>
                <script src="https://static.hotjar.com/c/hotjar-1000.js" async></script>
                <script src="https://connect.facebook.net/en_US/fbevents.js" async></script>
                <script src="https://sb.scorecardresearch.com/beacon.js" async></script>
                <script src="https://syndication.exoclick.com/splash.js" async></script>
                <script src="https://c.amazon-adsystem.com/aax2/apstag.js" async></script>
            </head>
            <body>
                <div class="pill">🛡 $statusLabel · 10 Ad Scripts Tested</div>

                <div class="vellum-demo-ad-banner">
                    🚫 Banner Ad Slot (Visible only when Ad Blocker is OFF)
                </div>

                <div class="grid">
                    <div class="card">
                        <h3>📤 Test File Upload</h3>
                        <p>Select any photo, video, or document from your phone:</p>
                        <input type="file" id="file-picker" multiple style="display:none" onchange="
                            var files = this.files;
                            if (files && files.length > 0) {
                                var names = [];
                                for (var i = 0; i < files.length; i++) {
                                    names.push(files[i].name + ' (' + Math.round(files[i].size / 1024) + ' KB)');
                                }
                                document.getElementById('upload-status').innerText = '✅ Ready: ' + names.join(', ');
                            }
                        " />
                        <button class="btn" onclick="document.getElementById('file-picker').click()">Choose File from Phone</button>
                        <div id="upload-status"></div>
                    </div>

                    <div class="card">
                        <h3>📥 Test File Download</h3>
                        <p>Download a sample file directly to your phone's Downloads folder:</p>
                        <a class="btn" href="https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf" download="Vellum_Sample_Document.pdf">Download Sample PDF</a>
                    </div>

                    <div class="card" id="zap-demo-box">
                        <h3>⚡ Test Zap Element</h3>
                        <p>Tap Shield → Zap → Tap this card to erase it.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
