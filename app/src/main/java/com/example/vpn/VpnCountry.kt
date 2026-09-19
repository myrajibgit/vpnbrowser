package com.example.vpn

data class VpnCountry(
    val code: String,
    val name: String,
    val flag: String,
    val city: String,
    val serverName: String,
    val virtualIp: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val locale: String,
    val basePing: Int,
    val isRecommended: Boolean = false,
    val proxyHostPort: String = "",
    val proxyProtocol: String = "HTTPS"
)

object VpnCountryCatalog {
    val COUNTRIES = listOf(
        VpnCountry(
            code = "US",
            name = "United States",
            flag = "🇺🇸",
            city = "New York",
            serverName = "US-East-Ultra #108",
            virtualIp = "104.28.19.42",
            latitude = 40.7128,
            longitude = -74.0060,
            timezone = "America/New_York",
            locale = "en-US,en;q=0.9",
            basePing = 24,
            isRecommended = true,
            proxyHostPort = "us-east.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "JP",
            name = "Japan",
            flag = "🇯🇵",
            city = "Tokyo",
            serverName = "JP-Tokyo-Speed #04",
            virtualIp = "133.242.18.91",
            latitude = 35.6762,
            longitude = 139.6503,
            timezone = "Asia/Tokyo",
            locale = "ja-JP,ja;q=0.9,en-US;q=0.8",
            basePing = 62,
            isRecommended = true,
            proxyHostPort = "jp-tokyo.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "IN",
            name = "India",
            flag = "🇮🇳",
            city = "Mumbai",
            serverName = "IN-Mumbai-Fast #12",
            virtualIp = "103.21.244.15",
            latitude = 19.0760,
            longitude = 72.8777,
            timezone = "Asia/Kolkata",
            locale = "hi-IN,en-IN;q=0.9,en;q=0.8",
            basePing = 45,
            isRecommended = true,
            proxyHostPort = "in-mumbai.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "GB",
            name = "United Kingdom",
            flag = "🇬🇧",
            city = "London",
            serverName = "UK-London-Shield #21",
            virtualIp = "185.220.101.5",
            latitude = 51.5074,
            longitude = -0.1278,
            timezone = "Europe/London",
            locale = "en-GB,en;q=0.9",
            basePing = 35,
            isRecommended = true,
            proxyHostPort = "uk-london.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "DE",
            name = "Germany",
            flag = "🇩🇪",
            city = "Frankfurt",
            serverName = "DE-Frankfurt-Direct #09",
            virtualIp = "194.26.29.112",
            latitude = 50.1109,
            longitude = 8.6821,
            timezone = "Europe/Berlin",
            locale = "de-DE,de;q=0.9,en;q=0.8",
            basePing = 38,
            proxyHostPort = "de-frankfurt.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "CA",
            name = "Canada",
            flag = "🇨🇦",
            city = "Toronto",
            serverName = "CA-Toronto-Secure #03",
            virtualIp = "198.54.130.22",
            latitude = 43.6532,
            longitude = -79.3832,
            timezone = "America/Toronto",
            locale = "en-CA,en;q=0.9,fr-CA;q=0.7",
            basePing = 32,
            proxyHostPort = "ca-toronto.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "SG",
            name = "Singapore",
            flag = "🇸🇬",
            city = "Singapore",
            serverName = "SG-Jurong-Turbo #16",
            virtualIp = "128.199.200.45",
            latitude = 1.3521,
            longitude = 103.8198,
            timezone = "Asia/Singapore",
            locale = "en-SG,en;q=0.9,zh-SG;q=0.8",
            basePing = 48,
            isRecommended = true,
            proxyHostPort = "sg-jurong.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "AU",
            name = "Australia",
            flag = "🇦🇺",
            city = "Sydney",
            serverName = "AU-Sydney-Express #07",
            virtualIp = "139.130.4.5",
            latitude = -33.8688,
            longitude = 151.2093,
            timezone = "Australia/Sydney",
            locale = "en-AU,en;q=0.9",
            basePing = 88,
            proxyHostPort = "au-sydney.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "FR",
            name = "France",
            flag = "🇫🇷",
            city = "Paris",
            serverName = "FR-Paris-Vault #15",
            virtualIp = "51.15.24.89",
            latitude = 48.8566,
            longitude = 2.3522,
            timezone = "Europe/Paris",
            locale = "fr-FR,fr;q=0.9,en;q=0.8",
            basePing = 41,
            proxyHostPort = "fr-paris.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "KR",
            name = "South Korea",
            flag = "🇰🇷",
            city = "Seoul",
            serverName = "KR-Seoul-Fiber #28",
            virtualIp = "211.233.72.10",
            latitude = 37.5665,
            longitude = 126.9780,
            timezone = "Asia/Seoul",
            locale = "ko-KR,ko;q=0.9,en;q=0.8",
            basePing = 72,
            proxyHostPort = "kr-seoul.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "BR",
            name = "Brazil",
            flag = "🇧🇷",
            city = "São Paulo",
            serverName = "BR-SaoPaulo-Hub #11",
            virtualIp = "177.18.200.12",
            latitude = -23.5505,
            longitude = -46.6333,
            timezone = "America/Sao_Paulo",
            locale = "pt-BR,pt;q=0.9,en;q=0.8",
            basePing = 95,
            proxyHostPort = "br-saopaulo.secure-relay.net:8443",
            proxyProtocol = "HTTPS"
        ),
        VpnCountry(
            code = "NL",
            name = "Netherlands",
            flag = "🇳🇱",
            city = "Amsterdam",
            serverName = "NL-Amsterdam-Privacy #01",
            virtualIp = "185.107.56.230",
            latitude = 52.3676,
            longitude = 4.9041,
            timezone = "Europe/Amsterdam",
            locale = "nl-NL,nl;q=0.9,en;q=0.8",
            basePing = 33,
            proxyHostPort = "socks5://127.0.0.1:9050",
            proxyProtocol = "SOCKS5 (Tor)"
        ),
        VpnCountry(
            code = "CH",
            name = "Switzerland",
            flag = "🇨🇭",
            city = "Zurich",
            serverName = "CH-Zurich-Fortress #05",
            virtualIp = "178.209.51.10",
            latitude = 47.3769,
            longitude = 8.5417,
            timezone = "Europe/Zurich",
            locale = "de-CH,de;q=0.9,en;q=0.8",
            basePing = 42,
            proxyHostPort = "ch-zurich.vault-relay.net:8443",
            proxyProtocol = "HTTPS"
        )
    )

    fun defaultCountry(): VpnCountry = COUNTRIES.first()
    fun findByCode(code: String): VpnCountry = COUNTRIES.find { it.code.equals(code, ignoreCase = true) } ?: defaultCountry()
}

/**
 * Free VPN Web Proxy services that emulate popular Chrome VPN extensions (like Proxyium, CroxyProxy, PlainProxies, BlockAway)
 */
enum class ExtensionProxyEngine(
    val title: String,
    val description: String,
    val badge: String,
    val proxyBaseUrl: String
) {
    PROXYIUM(
        title = "Proxyium VPN Extension",
        description = "High-speed encrypted SSL proxy tunnel with multi-region routing",
        badge = "CHROME EXTENSION",
        proxyBaseUrl = "https://proxyium.com"
    ),
    CROXY_PROXY(
        title = "CroxyProxy Free VPN",
        description = "Advanced web proxy for unblocking YouTube, streaming & social media",
        badge = "YOUTUBE READY",
        proxyBaseUrl = "https://www.croxyproxy.com"
    ),
    BLOCKAWAY(
        title = "BlockAway Secure Proxy",
        description = "Zero-configuration proxy tunnel for school, workplace & ISP firewalls",
        badge = "FIREWALL BYPASS",
        proxyBaseUrl = "https://www.blockaway.net"
    ),
    PLAIN_PROXIES(
        title = "PlainProxies Web Unblocker",
        description = "Instant HTTPS anonymizer that shields your real IP & ISP traces",
        badge = "ANONYMOUS",
        proxyBaseUrl = "https://plainproxies.com/resources/free-web-proxy"
    );

    /**
     * Converts a target URL or search term into an unblocked proxy destination
     */
    fun buildProxiedUrl(targetUrl: String): String {
        val clean = targetUrl.trim()
        if (clean.isBlank() || clean == "about:home") return proxyBaseUrl
        val urlToUnblock = if (clean.startsWith("http://") || clean.startsWith("https://")) {
            clean
        } else if (clean.contains(".") && !clean.contains(" ")) {
            "https://$clean"
        } else {
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(clean, "UTF-8")
        }

        return when (this) {
            PROXYIUM -> "$proxyBaseUrl/?url=${java.net.URLEncoder.encode(urlToUnblock, "UTF-8")}"
            CROXY_PROXY -> "$proxyBaseUrl/"
            BLOCKAWAY -> "$proxyBaseUrl/"
            PLAIN_PROXIES -> "$proxyBaseUrl"
        }
    }
}

