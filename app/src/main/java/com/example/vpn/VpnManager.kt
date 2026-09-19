package com.example.vpn

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class RealIpInfo(
    val ip: String = "Detecting...",
    val country: String = "",
    val city: String = "",
    val isp: String = "",
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)

data class ProxyTunnelConfig(
    val isEnabled: Boolean = false,
    val hostPort: String = "",
    val statusMessage: String = "Proxy tunnel inactive",
    val isSuccess: Boolean = false
)

data class VpnState(
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val activeCountry: VpnCountry = VpnCountryCatalog.defaultCountry(),
    val connectionStartTime: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val killSwitchEnabled: Boolean = true,
    val secureDnsEnabled: Boolean = true,
    val spoofGpsEnabled: Boolean = true,
    val proxyTunnel: ProxyTunnelConfig = ProxyTunnelConfig(),
    val realIp: RealIpInfo = RealIpInfo(),
    val extensionProxyEngine: ExtensionProxyEngine = ExtensionProxyEngine.PROXYIUM,
    val autoProxyUnblockAlways: Boolean = true
) {
    fun formattedUptime(): String {
        val hrs = uptimeSeconds / 3600
        val mins = (uptimeSeconds % 3600) / 60
        val secs = uptimeSeconds % 60
        return if (hrs > 0) {
            String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
        }
    }
}

class VpnManager(private val scope: CoroutineScope) {
    private val _vpnState = MutableStateFlow(VpnState())
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private var timerJob: Job? = null

    init {
        refreshRealIp()
    }

    /**
     * Checks the actual detected public IP and network location via ip-api.com
     */
    fun refreshRealIp() {
        scope.launch(Dispatchers.IO) {
            _vpnState.value = _vpnState.value.copy(
                realIp = _vpnState.value.realIp.copy(isChecking = true, errorMessage = null)
            )
            try {
                val url = URL("http://ip-api.com/json")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.optString("status") == "success") {
                    val ip = json.optString("query", "Unknown")
                    val country = json.optString("country", "Unknown")
                    val city = json.optString("city", "")
                    val isp = json.optString("isp", "")
                    _vpnState.value = _vpnState.value.copy(
                        realIp = RealIpInfo(
                            ip = ip,
                            country = country,
                            city = city,
                            isp = isp,
                            isChecking = false
                        )
                    )
                } else {
                    _vpnState.value = _vpnState.value.copy(
                        realIp = RealIpInfo(
                            ip = "Check failed",
                            isChecking = false,
                            errorMessage = json.optString("message", "Could not resolve IP")
                        )
                    )
                }
            } catch (e: Exception) {
                _vpnState.value = _vpnState.value.copy(
                    realIp = _vpnState.value.realIp.copy(
                        isChecking = false,
                        errorMessage = e.localizedMessage ?: "Network error checking IP"
                    )
                )
            }
        }
    }

    /**
     * Applies a real network proxy rule to WebView using AndroidX ProxyController
     */
    fun applyProxyTunnel(hostPort: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val trimmed = hostPort.trim()
        if (trimmed.isBlank()) {
            val msg = "Please enter host:port (e.g. socks5://127.0.0.1:9050 or proxy.com:8080)"
            _vpnState.value = _vpnState.value.copy(
                proxyTunnel = ProxyTunnelConfig(
                    isEnabled = false,
                    hostPort = "",
                    statusMessage = msg,
                    isSuccess = false
                )
            )
            onComplete(false, msg)
            return
        }

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val msg = "Proxy override is not supported on this device's WebView"
            _vpnState.value = _vpnState.value.copy(
                proxyTunnel = ProxyTunnelConfig(
                    isEnabled = false,
                    hostPort = trimmed,
                    statusMessage = msg,
                    isSuccess = false
                )
            )
            onComplete(false, msg)
            return
        }

        try {
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule(trimmed)
                .build()

            ProxyController.getInstance().setProxyOverride(
                proxyConfig,
                { it.run() },
                {
                    val successMsg = "Proxy active: $trimmed"
                    _vpnState.value = _vpnState.value.copy(
                        proxyTunnel = ProxyTunnelConfig(
                            isEnabled = true,
                            hostPort = trimmed,
                            statusMessage = successMsg,
                            isSuccess = true
                        )
                    )
                    refreshRealIp()
                    onComplete(true, successMsg)
                }
            )
        } catch (e: Throwable) {
            val err = e.localizedMessage ?: "Failed to set proxy"
            _vpnState.value = _vpnState.value.copy(
                proxyTunnel = ProxyTunnelConfig(
                    isEnabled = false,
                    hostPort = trimmed,
                    statusMessage = err,
                    isSuccess = false
                )
            )
            onComplete(false, err)
        }
    }

    /**
     * Clears proxy override, returning WebView to direct connection
     */
    fun disableProxyTunnel(onComplete: () -> Unit = {}) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                ProxyController.getInstance().clearProxyOverride(
                    { it.run() },
                    {
                        _vpnState.value = _vpnState.value.copy(
                            proxyTunnel = ProxyTunnelConfig(
                                isEnabled = false,
                                hostPort = "",
                                statusMessage = "Proxy tunnel disconnected",
                                isSuccess = false
                            )
                        )
                        refreshRealIp()
                        onComplete()
                    }
                )
                return
            } catch (e: Throwable) {
                // ignore
            }
        }
        _vpnState.value = _vpnState.value.copy(
            proxyTunnel = ProxyTunnelConfig(
                isEnabled = false,
                hostPort = "",
                statusMessage = "Proxy tunnel disconnected",
                isSuccess = false
            )
        )
        refreshRealIp()
        onComplete()
    }

    fun toggleVpn() {
        if (_vpnState.value.status == VpnStatus.CONNECTED) {
            disconnect()
        } else {
            connect(_vpnState.value.activeCountry)
        }
    }

    fun connect(country: VpnCountry) {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.CONNECTED,
            activeCountry = country,
            connectionStartTime = startTime,
            uptimeSeconds = 0L
        )
        try {
            val cookieManager = android.webkit.CookieManager.getInstance()
            val gl = country.code.lowercase()
            cookieManager.setCookie(".google.com", "gl=$gl; path=/; domain=.google.com")
            cookieManager.flush()
        } catch (e: Throwable) {
            // ignore
        }
        startUptimeTimer()
    }

    fun disconnect() {
        timerJob?.cancel()
        timerJob = null
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.DISCONNECTED,
            connectionStartTime = 0L,
            uptimeSeconds = 0L
        )
        try {
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setCookie(".google.com", "gl=; Max-Age=0; path=/; domain=.google.com")
            cookieManager.flush()
        } catch (e: Throwable) {
            // ignore
        }
    }

    fun selectCountry(country: VpnCountry, withTransition: Boolean = true) {
        if (_vpnState.value.status == VpnStatus.CONNECTED && _vpnState.value.activeCountry.code == country.code) {
            return
        }
        if (withTransition) {
            scope.launch {
                _vpnState.value = _vpnState.value.copy(
                    status = VpnStatus.CONNECTING,
                    activeCountry = country
                )
                delay(350)
                connect(country)
            }
        } else {
            connect(country)
        }
    }

    fun toggleCountry(country: VpnCountry) {
        if (_vpnState.value.status == VpnStatus.CONNECTED && _vpnState.value.activeCountry.code == country.code) {
            disconnect()
        } else {
            selectCountry(country, withTransition = true)
        }
    }

    fun toggleKillSwitch(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(killSwitchEnabled = enabled)
    }

    fun toggleSecureDns(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(secureDnsEnabled = enabled)
    }

    fun toggleGpsSpoof(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(spoofGpsEnabled = enabled)
    }

    fun setExtensionProxyEngine(engine: ExtensionProxyEngine) {
        _vpnState.value = _vpnState.value.copy(extensionProxyEngine = engine)
    }

    fun toggleAutoProxyUnblock(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(autoProxyUnblockAlways = enabled)
    }

    private fun startUptimeTimer() {
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            while (_vpnState.value.status == VpnStatus.CONNECTED) {
                delay(1000)
                val current = _vpnState.value
                val elapsed = (System.currentTimeMillis() - current.connectionStartTime) / 1000
                _vpnState.value = current.copy(uptimeSeconds = elapsed)
            }
        }
    }

    /**
     * JavaScript to spoof Geolocation, Timezone, and Locale in WebView
     */
    fun getSpoofingJavascript(country: VpnCountry): String {
        val tz = java.util.TimeZone.getTimeZone(country.timezone)
        val offsetMinutes = tz.getOffset(System.currentTimeMillis()) / (60 * 1000)
        val jsTimezoneOffset = -offsetMinutes // In JS, getTimezoneOffset() is negative of UTC offset

        return """
            (function() {
                try {
                    // Prevent duplicate execution on same page
                    if (window.__vpn_spoof_applied_country === '${country.code}') return;
                    window.__vpn_spoof_applied_country = '${country.code}';

                    // 1. Safe Geolocation spoofing with standard async callback
                    if (navigator.geolocation) {
                        const fakePosition = {
                            coords: {
                                latitude: ${country.latitude},
                                longitude: ${country.longitude},
                                altitude: 25.0,
                                accuracy: 15.0,
                                altitudeAccuracy: 10.0,
                                heading: null,
                                speed: null
                            },
                            timestamp: Date.now()
                        };
                        navigator.geolocation.getCurrentPosition = function(success, error, options) {
                            if (typeof success === 'function') {
                                setTimeout(function() {
                                    try { success(fakePosition); } catch(e) {}
                                }, 5);
                            }
                        };
                        navigator.geolocation.watchPosition = function(success, error, options) {
                            if (typeof success === 'function') {
                                setTimeout(function() {
                                    try { success(fakePosition); } catch(e) {}
                                }, 5);
                            }
                            return 12345;
                        };
                    }

                    // Permissions API spoofing for Geolocation
                    if (navigator.permissions && navigator.permissions.query) {
                        const origQuery = navigator.permissions.query.bind(navigator.permissions);
                        navigator.permissions.query = function(param) {
                            if (param && param.name === 'geolocation') {
                                return Promise.resolve({
                                    state: 'granted',
                                    name: 'geolocation',
                                    onchange: null
                                });
                            }
                            return origQuery(param);
                        };
                    }

                    // 2. Safe Timezone Spoofing via prototype resolvedOptions and getTimezoneOffset
                    try {
                        if (typeof Intl !== 'undefined' && Intl.DateTimeFormat && Intl.DateTimeFormat.prototype) {
                            if (!window.__orig_resolvedOptions) {
                                window.__orig_resolvedOptions = Intl.DateTimeFormat.prototype.resolvedOptions;
                            }
                            const origResolved = window.__orig_resolvedOptions;
                            Intl.DateTimeFormat.prototype.resolvedOptions = function() {
                                const res = origResolved.apply(this, arguments);
                                try {
                                    return Object.assign({}, res, { timeZone: '${country.timezone}' });
                                } catch(e) {
                                    return res;
                                }
                            };
                        }
                        Date.prototype.getTimezoneOffset = function() { return $jsTimezoneOffset; };
                    } catch(e) {}

                    // 3. Safe Locale override
                    try {
                        const primaryLang = '${country.locale.split(',').first()}';
                        Object.defineProperty(navigator, 'language', {
                            get: function() { return primaryLang; },
                            configurable: true
                        });
                        Object.defineProperty(navigator, 'languages', {
                            get: function() { return [primaryLang, 'en-US', 'en']; },
                            configurable: true
                        });
                    } catch(e) {}
                } catch(err) {
                    console.log('Location spoof err', err);
                }
            })();
        """.trimIndent()
    }
}
