package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.adblock.AdBlockState
import com.example.ui.components.BrowserHomeView
import com.example.ui.theme.MyApplicationTheme
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnState
import com.example.vpn.VpnStatus
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun browser_home_screenshot() {
        val testVpnState = VpnState(
            status = VpnStatus.CONNECTED,
            activeCountry = VpnCountryCatalog.findByCode("US"),
            connectionStartTime = 1000L,
            uptimeSeconds = 125L
        )
        val testAdBlockState = AdBlockState(
            isEnabled = true,
            totalBlockedCount = 18,
            videoAdsSkipped = 12,
            bannersRemoved = 6
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                BrowserHomeView(
                    vpnState = testVpnState,
                    adBlockState = testAdBlockState,
                    onNavigate = {},
                    onOpenVpnSheet = {},
                    onOpenSecuritySheet = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/browser_home.png")
    }
}
