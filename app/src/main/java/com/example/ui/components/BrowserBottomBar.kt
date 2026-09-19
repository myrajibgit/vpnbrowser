package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.vpn.VpnState
import com.example.vpn.VpnStatus

@Composable
fun BrowserBottomBar(
    tab: BrowserTab,
    tabCount: Int,
    vpnState: VpnState,
    isDesktopMode: Boolean,
    isIncognito: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onOpenVpnSheet: () -> Unit,
    onOpenTabsSheet: () -> Unit,
    onOpenSecuritySheet: () -> Unit,
    onOpenBookmarksHistory: () -> Unit,
    onBookmarkCurrentPage: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleIncognito: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Pulsing animation for active VPN connection
    val infiniteTransition = rememberInfiniteTransition(label = "vpnPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState.status == VpnStatus.CONNECTED) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                enabled = tab.canGoBack || tab.url != "about:home",
                modifier = Modifier
                    .size(44.dp)
                    .testTag("bottombar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (tab.canGoBack || tab.url != "about:home") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Forward button
            IconButton(
                onClick = onForward,
                enabled = tab.canGoForward,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("bottombar_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (tab.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Home button - direct one-tap navigation to Home
            IconButton(
                onClick = onHome,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("bottombar_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (tab.url == "about:home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Sleek, unobtrusive VPN Badge Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    vpnState.proxyTunnel.isEnabled -> Color(0xFF0C4A6E)
                    vpnState.status == VpnStatus.CONNECTED -> Color(0xFF064E3B)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        vpnState.proxyTunnel.isEnabled -> Color(0xFF38BDF8)
                        vpnState.status == VpnStatus.CONNECTED -> Color(0xFF10B981)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenVpnSheet() }
                    .testTag("inbuilt_vpn_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            vpnState.proxyTunnel.isEnabled -> "⚡"
                            vpnState.status == VpnStatus.CONNECTED -> vpnState.activeCountry.flag
                            else -> "🛡️"
                        },
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            vpnState.proxyTunnel.isEnabled -> "PROXY"
                            vpnState.status == VpnStatus.CONNECTED -> vpnState.activeCountry.code
                            else -> "VPN"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            vpnState.proxyTunnel.isEnabled -> Color(0xFF38BDF8)
                            vpnState.status == VpnStatus.CONNECTED -> Color(0xFF34D399)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Tabs button
            Surface(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                color = Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenTabsSheet() }
                    .padding(2.dp)
                    .testTag("tabs_button")
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Menu button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Home") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onHome()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Bookmarks") },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onBookmarkCurrentPage()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bookmarks & History") },
                        leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenBookmarksHistory()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Inbuilt VPN") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenVpnSheet()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Security & AdBlock") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenSecuritySheet()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (isIncognito) "Exit Incognito" else "Incognito Mode") },
                        leadingIcon = {
                            Icon(
                                if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleIncognito()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isDesktopMode) "Mobile Site" else "Desktop Site") },
                        leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onToggleDesktopMode()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Clear Browsing Data", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onClearData()
                        }
                    )
                }
            }
        }
    }
}
