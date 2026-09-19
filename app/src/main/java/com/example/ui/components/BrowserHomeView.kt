package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adblock.AdBlockState
import com.example.viewmodel.SearchEngine
import com.example.vpn.VpnCountry
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnState
import com.example.vpn.VpnStatus

data class QuickShortcut(
    val title: String,
    val url: String,
    val iconEmoji: String,
    val badge: String? = null,
    val accentColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrowserHomeView(
    vpnState: VpnState,
    adBlockState: AdBlockState,
    onNavigate: (String) -> Unit,
    onOpenVpnSheet: () -> Unit,
    onOpenSecuritySheet: () -> Unit,
    modifier: Modifier = Modifier,
    searchEngine: SearchEngine = SearchEngine.GOOGLE,
    onSelectSearchEngine: (SearchEngine) -> Unit = {},
    onSelectCountry: (VpnCountry) -> Unit = {},
    onToggleVpn: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val executeSearch: () -> Unit = {
        val query = searchQuery.trim()
        try {
            keyboardController?.hide()
            focusManager.clearFocus()
        } catch (_: Exception) {}
        if (query.isNotEmpty()) {
            onNavigate(query)
        } else {
            // If empty, navigate directly to selected search engine home
            onNavigate(searchEngine.searchBaseUrl)
        }
    }

    val shortcuts = remember {
        listOf(
            QuickShortcut(
                title = "Google",
                url = "https://www.google.com",
                iconEmoji = "🔍",
                accentColor = Color(0xFF4285F4)
            ),
            QuickShortcut(
                title = "YouTube",
                url = "https://m.youtube.com",
                iconEmoji = "▶️",
                badge = "Ad-Free",
                accentColor = Color(0xFFEF4444)
            ),
            QuickShortcut(
                title = "Web Proxy",
                url = "https://proxyium.com",
                iconEmoji = "⚡",
                badge = "Unblock",
                accentColor = Color(0xFF0284C7)
            ),
            QuickShortcut(
                title = "Check My IP",
                url = "https://ipinfo.io",
                iconEmoji = "🌐",
                accentColor = Color(0xFF10B981)
            ),
            QuickShortcut(
                title = "DuckDuckGo",
                url = "https://duckduckgo.com",
                iconEmoji = "🦆",
                accentColor = Color(0xFFDE5833)
            ),
            QuickShortcut(
                title = "Wikipedia",
                url = "https://en.m.wikipedia.org",
                iconEmoji = "📚",
                accentColor = Color(0xFF6B7280)
            ),
            QuickShortcut(
                title = "Reddit",
                url = "https://reddit.com",
                iconEmoji = "🤖",
                accentColor = Color(0xFFFF4500)
            ),
            QuickShortcut(
                title = "GitHub",
                url = "https://github.com",
                iconEmoji = "🐙",
                accentColor = Color(0xFF374151)
            ),
            QuickShortcut(
                title = "BBC News",
                url = "https://www.bbc.com/news",
                iconEmoji = "📰",
                accentColor = Color(0xFFB91C1C)
            ),
            QuickShortcut(
                title = "Whoer Privacy",
                url = "https://whoer.net",
                iconEmoji = "🔍",
                badge = "Audit",
                accentColor = Color(0xFF6366F1)
            )
        )
    }

    val popularCountries = remember {
        listOf(
            VpnCountryCatalog.findByCode("US"),
            VpnCountryCatalog.findByCode("JP"),
            VpnCountryCatalog.findByCode("IN"),
            VpnCountryCatalog.findByCode("GB"),
            VpnCountryCatalog.findByCode("DE"),
            VpnCountryCatalog.findByCode("SG")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0F2A4A),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Secure Web Browser",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "YouTube AdBlocker + Inbuilt Free VPN",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Inbuilt VPN Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (vpnState.status) {
                    VpnStatus.CONNECTED -> Color(0xFF064E3B)
                    VpnStatus.CONNECTING -> Color(0xFF78350F)
                    VpnStatus.DISCONNECTED -> Color(0xFF1E293B)
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = when (vpnState.status) {
                    VpnStatus.CONNECTED -> Color(0xFF34D399)
                    VpnStatus.CONNECTING -> Color(0xFFFBBF24)
                    VpnStatus.DISCONNECTED -> Color(0xFF334155)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenVpnSheet() }
                .testTag("home_vpn_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (vpnState.status == VpnStatus.CONNECTED) vpnState.activeCountry.flag else "🌐",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "INBUILT VPN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = when (vpnState.status) {
                                    VpnStatus.CONNECTED -> Color(0xFF34D399)
                                    VpnStatus.CONNECTING -> Color(0xFFFBBF24)
                                    VpnStatus.DISCONNECTED -> Color(0xFF94A3B8)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when {
                                    vpnState.proxyTunnel.isEnabled -> Color(0xFF0284C7)
                                    vpnState.status == VpnStatus.CONNECTED -> Color(0xFF10B981)
                                    else -> Color(0xFF64748B)
                                }
                            ) {
                                Text(
                                    text = when {
                                        vpnState.proxyTunnel.isEnabled -> "PROXY"
                                        vpnState.status == VpnStatus.CONNECTED -> "PROTECTED"
                                        else -> "STANDBY"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = when {
                                vpnState.proxyTunnel.isEnabled -> "Proxy: ${vpnState.proxyTunnel.hostPort}"
                                vpnState.status == VpnStatus.CONNECTED -> "${vpnState.activeCountry.flag} ${vpnState.activeCountry.name} Location Shield"
                                else -> "Virtual Location & Free VPN"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                vpnState.proxyTunnel.isEnabled -> "HTTP/SOCKS proxy tunnel active"
                                vpnState.status == VpnStatus.CONNECTED -> "GPS, Timezone & Search: ${vpnState.activeCountry.timezone} • To bypass ISP bans, use Web Proxy below"
                                else -> "Real IP: ${vpnState.realIp.ip} • Unprotected"
                            },
                            fontSize = 11.sp,
                            color = if (vpnState.status == VpnStatus.CONNECTED) Color(0xFFA7F3D0) else Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Direct quick toggle power button
                    Surface(
                        shape = CircleShape,
                        color = when (vpnState.status) {
                            VpnStatus.CONNECTED -> Color(0xFF10B981)
                            VpnStatus.CONNECTING -> Color(0xFFF59E0B)
                            VpnStatus.DISCONNECTED -> Color(0xFF475569)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onToggleVpn() }
                            .testTag("home_vpn_quick_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = if (vpnState.status == VpnStatus.CONNECTED) "Disconnect VPN" else "Connect VPN",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenVpnSheet() }
                    ) {
                        Text(
                            text = if (vpnState.status == VpnStatus.CONNECTED || vpnState.proxyTunnel.isEnabled) "Config" else "Set Up",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (vpnState.status == VpnStatus.CONNECTED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7),
                        modifier = Modifier
                            .clickable { onNavigate(vpnState.extensionProxyEngine.proxyBaseUrl) }
                            .testTag("home_web_proxy_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ ${vpnState.extensionProxyEngine.title} (Always Unblocked)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clickable { onNavigate("https://ipinfo.io") }
                            .testTag("home_check_ip_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍 Check IP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Country Chips (USA, Japan, India, UK, Germany, etc.)
        Text(
            text = "FAST COUNTRY SWITCH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            popularCountries.take(4).forEach { country ->
                val isCurrent = vpnState.activeCountry.code == country.code && vpnState.status == VpnStatus.CONNECTED
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrent) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isCurrent) Color(0xFF2DD4BF) else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectCountry(country)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = country.flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = country.code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Engine Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Engine:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SearchEngine.values().forEach { engine ->
                val isSelected = engine == searchEngine
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectSearchEngine(engine) }
                ) {
                    Text(
                        text = engine.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Search Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Search or type URL...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp).testTag("home_search_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { executeSearch() },
                    onDone = { executeSearch() },
                    onGo = { executeSearch() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dedicated prominent Search / Go button
            Button(
                onClick = { executeSearch() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("home_search_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Go",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shortcuts Title
        Text(
            text = "POPULAR WEBSITES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 12.dp)
        )

        // Shortcuts Grid (FlowRow)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 4,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shortcuts.forEach { shortcut ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(shortcut.url) }
                        .padding(vertical = 6.dp)
                        .testTag("shortcut_${shortcut.title.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, shortcut.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = shortcut.iconEmoji,
                                    fontSize = 24.sp
                                )
                            }
                        }

                        shortcut.badge?.let { badge ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = shortcut.accentColor,
                                modifier = Modifier.padding(1.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = shortcut.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Security & YouTube AdBlock Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSecuritySheet() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active Protections",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Details >",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "YouTube AdBlock",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (adBlockState.isEnabled) "⚡ Active (${adBlockState.totalBlockedCount} blocked)" else "Disabled",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adBlockState.isEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "HTTPS Enforcement",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "🔒 Strict SSL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
