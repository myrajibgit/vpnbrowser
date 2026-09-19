package com.example.adblock

import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream

data class AdBlockState(
    val isEnabled: Boolean = true,
    val totalBlockedCount: Int = 0,
    val videoAdsSkipped: Int = 0,
    val bannersRemoved: Int = 0
)

class YouTubeAdBlocker {
    private val _state = MutableStateFlow(AdBlockState())
    val state: StateFlow<AdBlockState> = _state.asStateFlow()

    fun toggleEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)
    }

    fun recordVideoAdSkipped() {
        if (!_state.value.isEnabled) return
        _state.value = _state.value.copy(
            totalBlockedCount = _state.value.totalBlockedCount + 1,
            videoAdsSkipped = _state.value.videoAdsSkipped + 1
        )
    }

    fun recordBannerRemoved() {
        if (!_state.value.isEnabled) return
        _state.value = _state.value.copy(
            totalBlockedCount = _state.value.totalBlockedCount + 1,
            bannersRemoved = _state.value.bannersRemoved + 1
        )
    }

    fun isYouTubeUrl(url: String?): Boolean {
        if (url == null) return false
        val lower = url.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    /**
     * Intercepts network ad requests ONLY for YouTube.
     * Guaranteed never to interfere with search engines or regular websites.
     */
    fun shouldIntercept(request: WebResourceRequest?, currentTabUrl: String? = null): WebResourceResponse? {
        if (!_state.value.isEnabled || request == null) return null

        // Strictly ONLY active when user is actually visiting a YouTube page
        if (!isYouTubeUrl(currentTabUrl)) {
            return null
        }

        val requestUrl = request.url?.toString()?.lowercase() ?: return null
        val isAdRequest = YOUTUBE_AD_PATTERNS.any { pattern -> requestUrl.contains(pattern) }
        if (isAdRequest) {
            return WebResourceResponse("text/plain", "UTF-8", 200, "OK", emptyMap(), ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }

    /**
     * JavaScript interface exposed to WebView
     */
    inner class AndroidAdBlockInterface {
        @JavascriptInterface
        fun onVideoAdSkipped() {
            recordVideoAdSkipped()
        }

        @JavascriptInterface
        fun onBannerAdRemoved() {
            recordBannerRemoved()
        }
    }

    companion object {
        private val YOUTUBE_AD_PATTERNS = listOf(
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "static.doubleclick.net",
            "ad.doubleclick.net",
            "adservice.google.",
            "/api/stats/ads",
            "/pagead/",
            "/get_midroll_info",
            "googlesyndication.com/pagead",
            "securepubads.g.doubleclick.net",
            "stats.g.doubleclick.net"
        )

        /**
         * Real-time client-side ad skipper & cleanup script for mobile YouTube
         */
        val YOUTUBE_ADBLOCK_INJECTION_JS = """
            (function() {
                if (window._ytAdBlockerInjected) return;
                window._ytAdBlockerInjected = true;

                function skipAd() {
                    try {
                        const video = document.querySelector('video');
                        const moviePlayer = document.querySelector('#movie_player') || document.querySelector('.html5-video-player');
                        const isAdShowing = moviePlayer && (
                            moviePlayer.classList.contains('ad-showing') ||
                            moviePlayer.classList.contains('ad-interrupting')
                        );

                        // If an in-stream video ad is playing
                        if (isAdShowing && video) {
                            video.muted = true;
                            video.playbackRate = 16.0;
                            if (video.duration && !isNaN(video.duration)) {
                                video.currentTime = video.duration;
                            }
                            if (window.AndroidAdBlock && window.AndroidAdBlock.onVideoAdSkipped) {
                                window.AndroidAdBlock.onVideoAdSkipped();
                            }
                        }

                        // Auto-click any skip button
                        const skipButtons = [
                            '.ytp-ad-skip-button',
                            '.ytp-ad-skip-button-modern',
                            '.ytp-skip-ad-button',
                            '.ytp-ad-skip-button-slot',
                            '.videoAdUiSkipButton',
                            'button.ytp-ad-skip-button'
                        ];
                        for (let sel of skipButtons) {
                            const btn = document.querySelector(sel);
                            if (btn && typeof btn.click === 'function') {
                                btn.click();
                                break;
                            }
                        }

                        // Hide banner / promoted tiles
                        const adSelectors = [
                            '.ytp-ad-overlay-container',
                            '.ytp-ad-message-container',
                            'ytm-promoted-sparkles-web-renderer',
                            'ytd-promoted-video-renderer',
                            'ytm-promoted-video-renderer',
                            'ytd-banner-promo-renderer',
                            'ytd-in-feed-ad-layout-renderer',
                            '#player-ads',
                            '.ad-container',
                            'ytd-ad-slot-renderer'
                        ];
                        for (let sel of adSelectors) {
                            const elements = document.querySelectorAll(sel);
                            elements.forEach(el => {
                                if (el && el.style.display !== 'none') {
                                    el.style.display = 'none';
                                    if (window.AndroidAdBlock && window.AndroidAdBlock.onBannerAdRemoved) {
                                        window.AndroidAdBlock.onBannerAdRemoved();
                                    }
                                }
                            });
                        }
                    } catch(e) {
                        // Silent catch
                    }
                }

                // Run frequently to catch instant prerolls
                setInterval(skipAd, 300);

                // Observe DOM changes for dynamically loaded ads
                const observer = new MutationObserver(function() {
                    skipAd();
                });
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        observer.observe(document.body, { childList: true, subtree: true });
                    });
                }
            })();
        """.trimIndent()
    }
}
