package com.example.ui.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.example.vpn.VpnCountry
import com.example.vpn.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnBottomSheet(
    vpnState: VpnState,
    onToggleVpn: () -> Unit,
    onSelectCountry: (VpnCountry) -> Unit,
    onToggleCountry: (VpnCountry) -> Unit = onSelectCountry,
    onToggleGpsSpoof: (Boolean) -> Unit,
    onToggleSecureDns: (Boolean) -> Unit = {},
    onApplyProxy: (String) -> Unit = {},
    onDisableProxy: () -> Unit = {},
    onRefreshIp: () -> Unit = {},
    onOpenWebProxy: () -> Unit = {},
    onCheckIp: () -> Unit = {},
    onDismiss: () -> Unit
) {
    CountryProxyBottomSheet(
        vpnState = vpnState,
        onToggleVpn = onToggleVpn,
        onSelectCountry = onSelectCountry,
        onToggleCountry = onToggleCountry,
        onToggleGpsSpoof = onToggleGpsSpoof,
        onToggleSecureDns = onToggleSecureDns,
        onApplyProxy = onApplyProxy,
        onDisableProxy = onDisableProxy,
        onRefreshIp = onRefreshIp,
        onOpenWebProxy = onOpenWebProxy,
        onCheckIp = onCheckIp,
        onDismiss = onDismiss
    )
}
