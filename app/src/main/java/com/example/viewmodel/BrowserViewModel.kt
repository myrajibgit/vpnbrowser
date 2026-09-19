package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adblock.YouTubeAdBlocker
import com.example.data.BrowserRepository
import com.example.model.Bookmark
import com.example.model.BrowserTab
import com.example.model.HistoryItem
import com.example.vpn.VpnCountry
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder

enum class ActiveSheet {
    NONE,
    VPN,
    SECURITY,
    TABS,
    BOOKMARKS_HISTORY,
    SETTINGS
}

enum class SearchEngine(val id: String, val displayName: String, val searchBaseUrl: String) {
    GOOGLE("google", "Google", "https://www.google.com/search?q="),
    DUCKDUCKGO("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q="),
    BING("bing", "Bing", "https://www.bing.com/search?q=")
}

class BrowserViewModel(
    private val repository: BrowserRepository,
    val vpnManager: VpnManager,
    val adBlocker: YouTubeAdBlocker
) : ViewModel() {

    private val initialTab = BrowserTab(
        url = "about:home",
        title = "Home"
    )

    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _activeSheet = MutableStateFlow(ActiveSheet.NONE)
    val activeSheet: StateFlow<ActiveSheet> = _activeSheet.asStateFlow()

    private val _searchEngine = MutableStateFlow(SearchEngine.GOOGLE)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    private val _isIncognitoMode = MutableStateFlow(false)
    val isIncognitoMode: StateFlow<Boolean> = _isIncognitoMode.asStateFlow()

    // Navigation action triggers for WebView
    private val _navigationTrigger = MutableStateFlow<String?>(null)
    val navigationTrigger: StateFlow<String?> = _navigationTrigger.asStateFlow()

    private val _backTrigger = MutableStateFlow(0)
    val backTrigger: StateFlow<Int> = _backTrigger.asStateFlow()

    private val _forwardTrigger = MutableStateFlow(0)
    val forwardTrigger: StateFlow<Int> = _forwardTrigger.asStateFlow()

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    // Bookmarks and history from Room
    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryItem>> = repository.recentHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentTab: StateFlow<BrowserTab> = combine(_tabs, _activeTabId) { tabList, activeId ->
        tabList.find { it.id == activeId } ?: tabList.firstOrNull() ?: initialTab
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = initialTab
    )

    val activeTab: BrowserTab
        get() = currentTab.value

    init {
        // Pre-seed popular secure bookmarks if database is empty
        viewModelScope.launch {
            repository.allBookmarks.collect { list ->
                if (list.isEmpty()) {
                    repository.addBookmark("YouTube (Ad-Free)", "https://m.youtube.com")
                    repository.addBookmark("DuckDuckGo Secure Search", "https://duckduckgo.com")
                    repository.addBookmark("IP & Location Checker", "https://ipinfo.io")
                    repository.addBookmark("Wikipedia", "https://en.m.wikipedia.org")
                    repository.addBookmark("Reddit", "https://reddit.com")
                }
            }
        }
    }

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.NONE
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
    }

    fun setUrlInput(input: String) {
        _urlInput.value = input
    }

    fun getSearchUrl(query: String): String {
        val vpn = vpnManager.vpnState.value
        val country = if (vpn.status == VpnStatus.CONNECTED) vpn.activeCountry else null
        val encoded = URLEncoder.encode(query, "UTF-8")

        return when (_searchEngine.value) {
            SearchEngine.GOOGLE -> {
                if (country != null) {
                    val domain = when (country.code) {
                        "IN" -> "www.google.co.in"
                        "JP" -> "www.google.co.jp"
                        "GB" -> "www.google.co.uk"
                        "DE" -> "www.google.de"
                        "CA" -> "www.google.ca"
                        "SG" -> "www.google.com.sg"
                        "AU" -> "www.google.com.au"
                        "FR" -> "www.google.fr"
                        "NL" -> "www.google.nl"
                        "BR" -> "www.google.com.br"
                        "KR" -> "www.google.co.kr"
                        "CH" -> "www.google.ch"
                        else -> "www.google.com"
                    }
                    val gl = country.code.lowercase()
                    val primaryLang = country.locale.substringBefore(',')
                    "https://$domain/search?q=$encoded&gl=$gl&hl=$primaryLang&pws=0"
                } else {
                    "https://www.google.com/search?q=$encoded"
                }
            }
            SearchEngine.DUCKDUCKGO -> {
                if (country != null) {
                    val kl = "${country.code.lowercase()}-${country.code.lowercase()}"
                    "https://duckduckgo.com/?q=$encoded&kl=$kl"
                } else {
                    "https://duckduckgo.com/?q=$encoded"
                }
            }
            SearchEngine.BING -> {
                if (country != null) {
                    "https://www.bing.com/search?q=$encoded&cc=${country.code.uppercase()}"
                } else {
                    "https://www.bing.com/search?q=$encoded"
                }
            }
        }
    }

    fun loadUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        val vpn = vpnManager.vpnState.value
        val country = if (vpn.status == VpnStatus.CONNECTED) vpn.activeCountry else null

        val finalUrl = when {
            trimmed.equals("about:home", ignoreCase = true) -> "about:home"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("about:") -> {
                // If it's a Google search URL without 'gl=' and VPN is connected, inject gl parameter
                if (country != null && (trimmed.contains("google.com/search") || trimmed.contains("google.co.in/search")) && !trimmed.contains("gl=")) {
                    val sep = if (trimmed.contains("?")) "&" else "?"
                    "$trimmed${sep}gl=${country.code.lowercase()}&pws=0"
                } else {
                    trimmed
                }
            }
            trimmed.contains(".") && !trimmed.contains(" ") -> {
                val withScheme = "https://$trimmed"
                if (country != null && (trimmed.equals("google.com", ignoreCase = true) || trimmed.equals("www.google.com", ignoreCase = true))) {
                    val domain = when (country.code) {
                        "IN" -> "www.google.co.in"
                        "JP" -> "www.google.co.jp"
                        "GB" -> "www.google.co.uk"
                        "DE" -> "www.google.de"
                        "CA" -> "www.google.ca"
                        "SG" -> "www.google.com.sg"
                        "AU" -> "www.google.com.au"
                        "FR" -> "www.google.fr"
                        "NL" -> "www.google.nl"
                        "BR" -> "www.google.com.br"
                        "KR" -> "www.google.co.kr"
                        "CH" -> "www.google.ch"
                        else -> "www.google.com"
                    }
                    "https://$domain/?gl=${country.code.lowercase()}&pws=0"
                } else {
                    withScheme
                }
            }
            else -> {
                getSearchUrl(trimmed)
            }
        }

        updateActiveTab { it.copy(url = finalUrl, isLoading = true, isSecure = finalUrl.startsWith("https://")) }
        _navigationTrigger.value = finalUrl
        _urlInput.value = if (finalUrl == "about:home") "" else finalUrl

        if (!_isIncognitoMode.value && finalUrl != "about:home") {
            viewModelScope.launch {
                repository.addHistory(trimmed, finalUrl)
            }
        }
    }

    /**
     * Directly loads any URL or search through the user's selected Chrome Free VPN Extension Proxy
     * (Proxyium, CroxyProxy, BlockAway, etc.) ensuring traffic is always unblocked through an external node.
     */
    fun loadViaExtensionProxy(target: String) {
        val engine = vpnManager.vpnState.value.extensionProxyEngine
        val unblockedUrl = engine.buildProxiedUrl(target)
        loadUrl(unblockedUrl)
    }


    fun onPageStarted(url: String, canGoBack: Boolean = false, canGoForward: Boolean = false) {
        updateActiveTab {
            it.copy(
                url = url,
                isLoading = true,
                progress = 0.1f,
                canGoBack = canGoBack || (url != "about:home"),
                canGoForward = canGoForward,
                isSecure = url.startsWith("https://")
            )
        }
        _urlInput.value = if (url == "about:home") "" else url
    }

    fun onPageFinished(url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        val finalTitle = when {
            url == "about:home" -> "Home"
            !title.isNullOrBlank() && !title.contains("http") -> title
            else -> Uri.parse(url).host ?: "Web Page"
        }

        updateActiveTab {
            it.copy(
                url = url,
                title = finalTitle,
                isLoading = false,
                progress = 1.0f,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                isSecure = url.startsWith("https://")
            )
        }
        _urlInput.value = if (url == "about:home") "" else url

        if (!_isIncognitoMode.value && url != "about:home" && !url.startsWith("about:")) {
            viewModelScope.launch {
                repository.addHistory(finalTitle, url)
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        updateActiveTab {
            it.copy(
                progress = progress / 100f,
                isLoading = progress < 100
            )
        }
    }

    fun onNavigationTriggerConsumed() {
        _navigationTrigger.value = null
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        updateActiveTab { tab ->
            tab.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun goBack() {
        if (activeTab.url != "about:home") {
            _backTrigger.value += 1
        }
    }

    fun goForward() {
        _forwardTrigger.value += 1
    }

    fun reload() {
        _reloadTrigger.value += 1
    }

    fun goHome() {
        updateActiveTab {
            it.copy(
                url = "about:home",
                title = "Home",
                isLoading = false,
                progress = 1.0f,
                canGoBack = false,
                canGoForward = false,
                isSecure = true
            )
        }
        _urlInput.value = ""
        _navigationTrigger.value = null
    }

    fun openNewTab(url: String = "about:home") {
        val newTab = BrowserTab(
            url = url,
            title = if (url == "about:home") "Home" else "New Tab",
            isIncognito = _isIncognitoMode.value
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        _urlInput.value = if (url == "about:home") "" else url
        if (url != "about:home") {
            _navigationTrigger.value = url
        }
    }

    fun switchTab(tabId: String) {
        val target = _tabs.value.find { it.id == tabId } ?: return
        _activeTabId.value = target.id
        _urlInput.value = if (target.url == "about:home") "" else target.url
        _navigationTrigger.value = target.url
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // Keep one home tab
            val freshTab = BrowserTab(url = "about:home", title = "Home")
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            _urlInput.value = ""
            return
        }

        val remaining = currentTabs.filter { it.id != tabId }
        _tabs.value = remaining
        if (_activeTabId.value == tabId) {
            val nextTab = remaining.last()
            _activeTabId.value = nextTab.id
            _urlInput.value = if (nextTab.url == "about:home") "" else nextTab.url
            _navigationTrigger.value = nextTab.url
        }
    }

    fun toggleDesktopMode() {
        _isDesktopMode.value = !_isDesktopMode.value
        reload()
    }

    fun toggleIncognito() {
        _isIncognitoMode.value = !_isIncognitoMode.value
        openNewTab("about:home")
    }

    fun toggleBookmarkCurrentPage() {
        val tab = activeTab
        if (tab.url.isBlank() || tab.url.startsWith("about:")) return

        viewModelScope.launch {
            val title = tab.title.ifBlank { tab.url }
            repository.addBookmark(title, tab.url)
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearBrowsingData(context: Context) {
        viewModelScope.launch {
            repository.clearHistory()
            try {
                WebStorage.getInstance().deleteAllData()
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } catch (e: Exception) {
                // Ignore web storage clear errors
            }
        }
    }

    fun selectVpnCountry(country: VpnCountry) {
        vpnManager.selectCountry(country)
        // If current page is open, refresh so geo/locale headers apply
        if (activeTab.url != "about:home") {
            reload()
        }
    }

    fun toggleCountryProxy(country: VpnCountry) {
        vpnManager.toggleCountry(country)
        if (activeTab.url != "about:home") {
            reload()
        }
    }

    fun toggleVpn() {
        vpnManager.toggleVpn()
        if (activeTab.url != "about:home") {
            reload()
        }
    }

    fun openWebProxy(targetUrl: String? = null) {
        val proxyUrl = if (!targetUrl.isNullOrBlank() && targetUrl != "about:home") {
            "https://proxyium.com"
        } else {
            "https://proxyium.com"
        }
        loadUrl(proxyUrl)
    }

    private fun updateActiveTab(transform: (BrowserTab) -> BrowserTab) {
        val id = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id) transform(tab) else tab
        }
    }
}

class BrowserViewModelFactory(
    private val repository: BrowserRepository,
    private val vpnManager: VpnManager,
    private val adBlocker: YouTubeAdBlocker
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowserViewModel::class.java)) {
            return BrowserViewModel(repository, vpnManager, adBlocker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
