package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adblock.AdBlockState
import com.example.model.BrowserTab
import com.example.vpn.VpnState
import com.example.vpn.VpnStatus

@Composable
fun BrowserTopBar(
    tab: BrowserTab,
    vpnState: VpnState,
    adBlockState: AdBlockState,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onOpenVpnSheet: () -> Unit,
    onOpenSecuritySheet: () -> Unit,
    modifier: Modifier = Modifier,
    onUnblockNavigate: ((String) -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = tab.progress,
        label = "progressBar"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // VPN Quick Indicator Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (vpnState.status) {
                        VpnStatus.CONNECTED -> Color(0xFF0F392B)
                        VpnStatus.CONNECTING -> Color(0xFF3B2E05)
                        VpnStatus.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenVpnSheet() }
                        .testTag("topbar_vpn_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (vpnState.status == VpnStatus.CONNECTED) vpnState.activeCountry.flag else "🌐",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (vpnState.status) {
                                VpnStatus.CONNECTED -> vpnState.activeCountry.code
                                VpnStatus.CONNECTING -> "..."
                                VpnStatus.DISCONNECTED -> "VPN"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (vpnState.status) {
                                VpnStatus.CONNECTED -> Color(0xFF34D399)
                                VpnStatus.CONNECTING -> Color(0xFFFBBF24)
                                VpnStatus.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Modular URL Address Bar with dedicated input and 'Go' button
                UrlAddressBar(
                    currentUrl = tab.url,
                    isSecure = tab.isSecure,
                    onNavigate = onNavigate,
                    onOpenSecuritySheet = onOpenSecuritySheet,
                    onUnblockNavigate = onUnblockNavigate,
                    modifier = Modifier.weight(1f)
                )

                // YouTube AdBlock Indicator badge if on YouTube
                val isYouTube = tab.url.contains("youtube.com") || tab.url.contains("youtu.be")
                if (isYouTube && adBlockState.isEnabled) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onOpenSecuritySheet() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${adBlockState.totalBlockedCount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                // Reload Action Button (when on active web page)
                if (tab.url != "about:home") {
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Web page load Progress Bar
            AnimatedVisibility(visible = tab.isLoading && animatedProgress < 1f) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
