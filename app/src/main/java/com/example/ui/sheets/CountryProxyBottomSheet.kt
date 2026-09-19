package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vpn.ExtensionProxyEngine
import com.example.vpn.VpnCountry
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnState
import com.example.vpn.VpnStatus

/**
 * Bottom Sheet UI that allows users to toggle between different country proxy settings,
 * updating the current connection status dynamically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryProxyBottomSheet(
    vpnState: VpnState,
    onToggleVpn: () -> Unit,
    onSelectCountry: (VpnCountry) -> Unit,
    onToggleCountry: (VpnCountry) -> Unit = onSelectCountry,
    onToggleGpsSpoof: (Boolean) -> Unit = {},
    onToggleSecureDns: (Boolean) -> Unit = {},
    onApplyProxy: (String) -> Unit = {},
    onDisableProxy: () -> Unit = {},
    onRefreshIp: () -> Unit = {},
    onOpenWebProxy: () -> Unit = {},
    onCheckIp: () -> Unit = {},
    onSelectExtensionProxyEngine: (ExtensionProxyEngine) -> Unit = {},
    onToggleAutoProxyUnblock: (Boolean) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var proxyInput by remember { mutableStateOf(vpnState.proxyTunnel.hostPort) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf("ALL") }

    val filteredCountries = remember(searchQuery, selectedRegionFilter) {
        VpnCountryCatalog.COUNTRIES.filter { country ->
            val matchesQuery = country.name.contains(searchQuery, ignoreCase = true) ||
                    country.city.contains(searchQuery, ignoreCase = true) ||
                    country.code.contains(searchQuery, ignoreCase = true) ||
                    country.proxyProtocol.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedRegionFilter) {
                "RECOMMENDED" -> country.isRecommended
                "ASIA" -> listOf("JP", "IN", "SG", "KR").contains(country.code)
                "AMERICAS" -> listOf("US", "CA", "BR").contains(country.code)
                "EUROPE" -> listOf("GB", "DE", "FR", "NL", "CH").contains(country.code)
                "TOR" -> country.proxyProtocol.contains("Tor", ignoreCase = true) || country.code == "NL"
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("country_proxy_bottom_sheet")
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (vpnState.status) {
                            VpnStatus.CONNECTED -> Color(0xFF064E3B)
                            VpnStatus.CONNECTING -> Color(0xFF78350F)
                            VpnStatus.DISCONNECTED -> Color(0xFF1E293B)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = when (vpnState.status) {
                                    VpnStatus.CONNECTED -> Color(0xFF34D399)
                                    VpnStatus.CONNECTING -> Color(0xFFFBBF24)
                                    VpnStatus.DISCONNECTED -> Color(0xFF94A3B8)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Country Proxy Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Dynamic connection subtitle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_alpha"
                            )

                            when (vpnState.status) {
                                VpnStatus.CONNECTED -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .alpha(pulseAlpha)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active: ${vpnState.activeCountry.flag} ${vpnState.activeCountry.name} • ${vpnState.activeCountry.basePing}ms",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                VpnStatus.CONNECTING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Connecting to ${vpnState.activeCountry.flag} ${vpnState.activeCountry.name}...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                                VpnStatus.DISCONNECTED -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Direct Connection (Proxy Inactive)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_country_proxy_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Live Connection Status Hero Card
            val cardBgColor by animateColorAsState(
                targetValue = when (vpnState.status) {
                    VpnStatus.CONNECTED -> Color(0xFF042F2E)
                    VpnStatus.CONNECTING -> Color(0xFF451A03)
                    VpnStatus.DISCONNECTED -> Color(0xFF0F172A)
                },
                animationSpec = tween(400),
                label = "card_bg"
            )

            val cardBorderColor by animateColorAsState(
                targetValue = when (vpnState.status) {
                    VpnStatus.CONNECTED -> Color(0xFF10B981)
                    VpnStatus.CONNECTING -> Color(0xFFF59E0B)
                    VpnStatus.DISCONNECTED -> Color(0xFF334155)
                },
                animationSpec = tween(400),
                label = "card_border"
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.5.dp, cardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dynamic_connection_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Top row: Flag, Name, Server, and Main Power Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vpnState.activeCountry.flag,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = vpnState.activeCountry.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (vpnState.status) {
                                            VpnStatus.CONNECTED -> Color(0xFF10B981).copy(alpha = 0.25f)
                                            VpnStatus.CONNECTING -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                            VpnStatus.DISCONNECTED -> Color(0xFF64748B).copy(alpha = 0.25f)
                                        }
                                    ) {
                                        Text(
                                            text = when (vpnState.status) {
                                                VpnStatus.CONNECTED -> "CONNECTED"
                                                VpnStatus.CONNECTING -> "CONNECTING"
                                                VpnStatus.DISCONNECTED -> "OFFLINE"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (vpnState.status) {
                                                VpnStatus.CONNECTED -> Color(0xFF34D399)
                                                VpnStatus.CONNECTING -> Color(0xFFFBBF24)
                                                VpnStatus.DISCONNECTED -> Color(0xFF94A3B8)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${vpnState.activeCountry.city} • ${vpnState.activeCountry.serverName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Prominent 1-tap Main Power Switch
                        Surface(
                            shape = CircleShape,
                            color = when (vpnState.status) {
                                VpnStatus.CONNECTED -> Color(0xFF10B981)
                                VpnStatus.CONNECTING -> Color(0xFFF59E0B)
                                VpnStatus.DISCONNECTED -> Color(0xFF475569)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onToggleVpn() }
                                .testTag("main_proxy_power_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (vpnState.status == VpnStatus.CONNECTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Toggle Proxy Connection",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Column Live Metrics Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Metric 1: Latency Ping
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LATENCY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(
                                text = if (vpnState.status == VpnStatus.CONNECTED) "${vpnState.activeCountry.basePing} ms" else "-- ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vpnState.status == VpnStatus.CONNECTED) Color(0xFF34D399) else Color(0xFF94A3B8)
                            )
                        }

                        // Metric 2: Protocol
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PROTOCOL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(
                                text = vpnState.activeCountry.proxyProtocol.substringBefore(" "),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Metric 3: Virtual IP
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PROXY IP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(
                                text = if (vpnState.status == VpnStatus.CONNECTED) vpnState.activeCountry.virtualIp else "Direct",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vpnState.status == VpnStatus.CONNECTED) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }

                        // Metric 4: Uptime
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UPTIME", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(
                                text = if (vpnState.status == VpnStatus.CONNECTED) vpnState.formattedUptime() else "00:00",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vpnState.status == VpnStatus.CONNECTED) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wide Action Button
                    Button(
                        onClick = onToggleVpn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("proxy_action_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (vpnState.status) {
                                VpnStatus.CONNECTED -> Color(0xFFDC2626)
                                VpnStatus.CONNECTING -> Color(0xFFD97706)
                                VpnStatus.DISCONNECTED -> Color(0xFF059669)
                            }
                        )
                    ) {
                        Text(
                            text = when (vpnState.status) {
                                VpnStatus.CONNECTED -> "Disconnect Proxy (${vpnState.activeCountry.code})"
                                VpnStatus.CONNECTING -> "Connecting to ${vpnState.activeCountry.name}..."
                                VpnStatus.DISCONNECTED -> "Connect to ${vpnState.activeCountry.flag} ${vpnState.activeCountry.name} Proxy"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs: Country Nodes vs Chrome VPN Extensions vs Custom Tunnel vs Shield
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Countries", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.testTag("tab_country_nodes")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Chrome VPN", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.testTag("tab_chrome_vpn_extension")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tunnel", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.testTag("tab_custom_tunnel")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Shield/GPS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.testTag("tab_shield_gps")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: COUNTRY PROXY NODES (Dynamic Toggles)
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_country_proxy_input"),
                        placeholder = { Text("Search country, city, or protocol (e.g. Japan, US, Tor)...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Region & Protocol Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "🌐 All",
                            "RECOMMENDED" to "⚡ Fast",
                            "AMERICAS" to "Americas",
                            "EUROPE" to "Europe",
                            "ASIA" to "Asia",
                            "TOR" to "🧅 Tor"
                        ).forEach { (filterKey, label) ->
                            FilterChip(
                                selected = selectedRegionFilter == filterKey,
                                onClick = { selectedRegionFilter = filterKey },
                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("filter_chip_$filterKey")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dynamic Country Proxy List with interactive switches
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("country_proxy_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCountries, key = { it.code }) { country ->
                            val isThisCountryActive = country.code == vpnState.activeCountry.code
                            val isConnected = isThisCountryActive && vpnState.status == VpnStatus.CONNECTED
                            val isConnecting = isThisCountryActive && vpnState.status == VpnStatus.CONNECTING

                            val itemBgColor by animateColorAsState(
                                targetValue = when {
                                    isConnected -> Color(0xFF064E3B)
                                    isConnecting -> Color(0xFF78350F).copy(alpha = 0.5f)
                                    isThisCountryActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                animationSpec = tween(300),
                                label = "item_bg"
                            )

                            val itemBorderColor by animateColorAsState(
                                targetValue = when {
                                    isConnected -> Color(0xFF10B981)
                                    isConnecting -> Color(0xFFF59E0B)
                                    isThisCountryActive -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                },
                                animationSpec = tween(300),
                                label = "item_border"
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = itemBgColor,
                                border = BorderStroke(if (isThisCountryActive) 1.5.dp else 1.dp, itemBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onToggleCountry(country) }
                                    .testTag("country_proxy_item_${country.code}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Flag, Country, City, Protocol
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = country.flag, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = country.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (country.isRecommended) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "FAST",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFF10B981),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "${country.city} • ${country.serverName}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            // Sub-badges: Protocol & Latency
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = country.proxyProtocol,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }

                                                Text(
                                                    text = "📶 ${country.basePing}ms",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (country.basePing < 50) Color(0xFF10B981) else Color(0xFFF59E0B)
                                                )
                                            }
                                        }
                                    }

                                    // Right: Status indicator & Interactive Toggle Switch
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isConnecting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFF59E0B)
                                            )
                                        }

                                        // Switch that allows users to toggle between different country proxy settings
                                        Switch(
                                            checked = isConnected,
                                            onCheckedChange = { onToggleCountry(country) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF10B981),
                                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.testTag("switch_country_${country.code}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CHROME FREE VPN EXTENSIONS ENGINE (Always Works Everywhere)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Extension Banner
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2563EB).copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🧩", fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Chrome Free VPN Extensions",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF34D399),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Embedded unblocking engine inspired by popular Chrome VPN extensions. Traffic routes through free encrypted web tunnels so it always works.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Launch Active Extension Web Proxy
                                Button(
                                    onClick = onOpenWebProxy,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Launch ${vpnState.extensionProxyEngine.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Selectable Extension Engines
                        Text(
                            text = "CHOOSE EXTENSION ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.6.sp
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(ExtensionProxyEngine.entries.size) { index ->
                                val engine = ExtensionProxyEngine.entries[index]
                                val isSelected = vpnState.extensionProxyEngine == engine

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            Color(0xFF1E293B)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectExtensionProxyEngine(engine) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (engine) {
                                                ExtensionProxyEngine.PROXYIUM -> "⚡"
                                                ExtensionProxyEngine.CROXY_PROXY -> "▶️"
                                                ExtensionProxyEngine.BLOCKAWAY -> "🛡️"
                                                ExtensionProxyEngine.PLAIN_PROXIES -> "🕶️"
                                            },
                                            fontSize = 22.sp
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = engine.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF38BDF8).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = engine.badge,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF38BDF8),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = engine.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (isSelected) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF0284C7),
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: CUSTOM PROXY TUNNEL & DIRECT WEB GATEWAY
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Instant Web Proxy Unblocker Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌐", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Instant Web Proxy Gateway",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Browse any restricted or firewalled site through an encrypted online proxy tunnel.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onOpenWebProxy,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ Launch Web Proxy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Custom AndroidX Proxy Tunnel
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Custom Proxy Tunnel (AndroidX)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Routes all WebView HTTP and HTTPS traffic through a custom SOCKS5 or HTTP proxy.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = proxyInput,
                                    onValueChange = { proxyInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. socks5://127.0.0.1:9050 or proxy.com:8080", fontSize = 12.sp) },
                                    label = { Text("Proxy Address (host:port)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick presets
                                Text(text = "Quick Presets:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { proxyInput = "socks5://127.0.0.1:9050" }
                                    ) {
                                        Text(
                                            text = "Tor Orbot (:9050)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { proxyInput = "socks5://127.0.0.1:1080" }
                                    ) {
                                        Text(
                                            text = "Local Shadowsocks (:1080)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onApplyProxy(proxyInput) },
                                        enabled = proxyInput.isNotBlank(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Apply Tunnel", fontSize = 12.sp)
                                    }

                                    if (vpnState.proxyTunnel.isEnabled) {
                                        OutlinedButton(
                                            onClick = onDisableProxy,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Disconnect", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = vpnState.proxyTunnel.statusMessage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (vpnState.proxyTunnel.isEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: PRIVACY SHIELD & GPS / IP CHECKER
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Real Public IP Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PUBLIC DETECTED IP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = if (vpnState.realIp.isChecking) "Detecting..." else vpnState.realIp.ip,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (vpnState.realIp.country.isNotBlank()) {
                                        Text(
                                            text = "${vpnState.realIp.city.takeIf { it.isNotBlank() }?.let { "$it, " } ?: ""}${vpnState.realIp.country} • ${vpnState.realIp.isp}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = onRefreshIp,
                                        enabled = !vpnState.realIp.isChecking,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        if (vpnState.realIp.isChecking) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Refresh, contentDescription = "Refresh IP", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onDismiss()
                                                onCheckIp()
                                            }
                                    ) {
                                        Text(
                                            text = "ipinfo.io",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // GPS Spoof Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Spoof HTML5 Geolocation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Overrides browser GPS to match ${vpnState.activeCountry.city} coordinates",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = vpnState.spoofGpsEnabled,
                                onCheckedChange = onToggleGpsSpoof
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Secure DNS Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DNS Leak Protection (DoH)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Encrypts all DNS queries with Cloudflare & Google 1.1.1.1",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = vpnState.secureDnsEnabled,
                                onCheckedChange = onToggleSecureDns
                            )
                        }
                    }
                }
            }
        }
    }
}
