package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adblock.YouTubeAdBlocker
import com.example.data.AppDatabase
import com.example.data.BrowserRepository
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserHomeView
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.WebViewContainer
import com.example.ui.sheets.BookmarksHistorySheet
import com.example.ui.sheets.CountryProxyBottomSheet
import com.example.ui.sheets.SecurityBottomSheet
import com.example.ui.sheets.TabsBottomSheet
import com.example.ui.sheets.VpnBottomSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.BrowserViewModelFactory
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: BrowserRepository
    private lateinit var vpnManager: VpnManager
    private lateinit var adBlocker: YouTubeAdBlocker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        repository = BrowserRepository(database.browserDao())
        vpnManager = VpnManager(lifecycleScope)
        adBlocker = YouTubeAdBlocker()

        setContent {
            MyApplicationTheme {
                val factory = remember {
                    BrowserViewModelFactory(repository, vpnManager, adBlocker)
                }
                val viewModel: BrowserViewModel = viewModel(factory = factory)

                BrowserMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrowserMainScreen(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val isDesktopMode by viewModel.isDesktopMode.collectAsStateWithLifecycle()
    val isIncognito by viewModel.isIncognitoMode.collectAsStateWithLifecycle()

    val vpnState by viewModel.vpnManager.vpnState.collectAsStateWithLifecycle()
    val adBlockState by viewModel.adBlocker.state.collectAsStateWithLifecycle()

    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    BackHandler(enabled = currentTab.canGoBack || currentTab.url != "about:home") {
        if (currentTab.canGoBack) {
            viewModel.goBack()
        } else if (currentTab.url != "about:home") {
            viewModel.goHome()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                // Incognito Mode Banner
                if (isIncognito) {
                    Surface(
                        color = Color(0xFF1E1B4B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "Incognito Browsing Active • History Not Saved",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5B4FC)
                            )
                        }
                    }
                }

                BrowserTopBar(
                    tab = currentTab,
                    vpnState = vpnState,
                    adBlockState = adBlockState,
                    onNavigate = { queryOrUrl ->
                        viewModel.loadUrl(queryOrUrl)
                    },
                    onReload = {
                        viewModel.reload()
                    },
                    onOpenVpnSheet = {
                        viewModel.openSheet(ActiveSheet.VPN)
                    },
                    onOpenSecuritySheet = {
                        viewModel.openSheet(ActiveSheet.SECURITY)
                    },
                    onUnblockNavigate = { target ->
                        if (target.isNotBlank()) {
                            viewModel.loadViaExtensionProxy(target)
                        } else {
                            viewModel.loadUrl(vpnState.extensionProxyEngine.proxyBaseUrl)
                        }
                    }
                )
            }
        },
        bottomBar = {
            BrowserBottomBar(
                tab = currentTab,
                tabCount = tabs.size,
                vpnState = vpnState,
                isDesktopMode = isDesktopMode,
                isIncognito = isIncognito,
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onHome = { viewModel.goHome() },
                onOpenVpnSheet = { viewModel.openSheet(ActiveSheet.VPN) },
                onOpenTabsSheet = { viewModel.openSheet(ActiveSheet.TABS) },
                onOpenSecuritySheet = { viewModel.openSheet(ActiveSheet.SECURITY) },
                onOpenBookmarksHistory = { viewModel.openSheet(ActiveSheet.BOOKMARKS_HISTORY) },
                onBookmarkCurrentPage = {
                    viewModel.toggleBookmarkCurrentPage()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Bookmark saved")
                    }
                },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onToggleIncognito = { viewModel.toggleIncognito() },
                onClearData = {
                    viewModel.clearBrowsingData(context)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Browsing cache and data cleared")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentTab.url == "about:home") {
                BrowserHomeView(
                    vpnState = vpnState,
                    adBlockState = adBlockState,
                    searchEngine = searchEngine,
                    onSelectSearchEngine = { engine ->
                        viewModel.setSearchEngine(engine)
                    },
                    onSelectCountry = { country ->
                        viewModel.selectVpnCountry(country)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("VPN connected to ${country.flag} ${country.name}")
                        }
                    },
                    onNavigate = { targetUrl ->
                        viewModel.loadUrl(targetUrl)
                    },
                    onOpenVpnSheet = {
                        viewModel.openSheet(ActiveSheet.VPN)
                    },
                    onOpenSecuritySheet = {
                        viewModel.openSheet(ActiveSheet.SECURITY)
                    },
                    onToggleVpn = {
                        viewModel.toggleVpn()
                        coroutineScope.launch {
                            val isNowConnected = viewModel.vpnManager.vpnState.value.status == com.example.vpn.VpnStatus.CONNECTED
                            val active = viewModel.vpnManager.vpnState.value.activeCountry
                            if (isNowConnected) {
                                snackbarHostState.showSnackbar("VPN Connected to ${active.flag} ${active.name}")
                            } else {
                                snackbarHostState.showSnackbar("VPN Disconnected")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            } else {
                WebViewContainer(
                    viewModel = viewModel,
                    tab = currentTab,
                    isDesktopMode = isDesktopMode,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Modal Bottom Sheets
    when (activeSheet) {
        ActiveSheet.VPN -> {
            CountryProxyBottomSheet(
                vpnState = vpnState,
                onToggleVpn = { viewModel.toggleVpn() },
                onSelectCountry = { country ->
                    viewModel.selectVpnCountry(country)
                    Toast.makeText(
                        context,
                        "Virtual location switched to ${country.flag} ${country.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onToggleCountry = { country ->
                    viewModel.toggleCountryProxy(country)
                    val willBeConnected = viewModel.vpnManager.vpnState.value.status != com.example.vpn.VpnStatus.CONNECTED || viewModel.vpnManager.vpnState.value.activeCountry.code != country.code
                    if (willBeConnected) {
                        Toast.makeText(
                            context,
                            "Connecting to ${country.flag} ${country.name} proxy...",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Proxy disconnected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onToggleGpsSpoof = { enabled ->
                    viewModel.vpnManager.toggleGpsSpoof(enabled)
                },
                onToggleSecureDns = { enabled ->
                    viewModel.vpnManager.toggleSecureDns(enabled)
                },
                onApplyProxy = { hostPort ->
                    viewModel.vpnManager.applyProxyTunnel(hostPort) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onDisableProxy = {
                    viewModel.vpnManager.disableProxyTunnel {
                        Toast.makeText(context, "Proxy tunnel disconnected", Toast.LENGTH_SHORT).show()
                    }
                },
                onRefreshIp = {
                    viewModel.vpnManager.refreshRealIp()
                },
                onOpenWebProxy = {
                    viewModel.closeSheet()
                    viewModel.loadUrl(vpnState.extensionProxyEngine.proxyBaseUrl)
                },
                onCheckIp = {
                    viewModel.closeSheet()
                    viewModel.loadUrl("https://ipinfo.io")
                },
                onSelectExtensionProxyEngine = { engine ->
                    viewModel.vpnManager.setExtensionProxyEngine(engine)
                    Toast.makeText(context, "Switched to ${engine.title}", Toast.LENGTH_SHORT).show()
                },
                onToggleAutoProxyUnblock = { enabled ->
                    viewModel.vpnManager.toggleAutoProxyUnblock(enabled)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.SECURITY -> {
            SecurityBottomSheet(
                tab = currentTab,
                adBlockState = adBlockState,
                isIncognito = isIncognito,
                onToggleAdBlock = { enabled ->
                    viewModel.adBlocker.toggleEnabled(enabled)
                },
                onToggleIncognito = {
                    viewModel.toggleIncognito()
                },
                onClearBrowsingData = {
                    viewModel.clearBrowsingData(context)
                    Toast.makeText(context, "Cache and cookies deleted", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.TABS -> {
            TabsBottomSheet(
                tabs = tabs,
                activeTabId = activeTabId,
                onSelectTab = { tabId -> viewModel.switchTab(tabId) },
                onCloseTab = { tabId -> viewModel.closeTab(tabId) },
                onNewTab = { viewModel.openNewTab() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.BOOKMARKS_HISTORY -> {
            BookmarksHistorySheet(
                bookmarks = bookmarks,
                history = history,
                onSelectUrl = { url -> viewModel.loadUrl(url) },
                onDeleteBookmark = { id -> viewModel.removeBookmark(id) },
                onDeleteHistoryItem = { id -> viewModel.deleteHistoryItem(id) },
                onClearHistory = {
                    viewModel.clearBrowsingData(context)
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        else -> {}
    }
}
