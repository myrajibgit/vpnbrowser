package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.adblock.YouTubeAdBlocker
import com.example.vpn.VpnCountryCatalog
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Web Browser", appName)
    }

    @Test
    fun `vpn country catalog has all major requested countries`() {
        val usa = VpnCountryCatalog.findByCode("US")
        val japan = VpnCountryCatalog.findByCode("JP")
        val india = VpnCountryCatalog.findByCode("IN")
        val uk = VpnCountryCatalog.findByCode("GB")
        val germany = VpnCountryCatalog.findByCode("DE")

        assertEquals("United States", usa.name)
        assertEquals("Japan", japan.name)
        assertEquals("India", india.name)
        assertEquals("United Kingdom", uk.name)
        assertEquals("Germany", germany.name)

        assertTrue(VpnCountryCatalog.COUNTRIES.size >= 12)
    }

    @Test
    fun `youtube adblocker accurately identifies youtube urls`() {
        val adBlocker = YouTubeAdBlocker()

        assertTrue(adBlocker.isYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(adBlocker.isYouTubeUrl("https://m.youtube.com"))
        assertTrue(adBlocker.isYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        assertFalse(adBlocker.isYouTubeUrl("https://wikipedia.org"))
        assertFalse(adBlocker.isYouTubeUrl("https://google.com"))
    }

    @Test
    fun `vpn manager generates valid spoofing script for selected country`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val vpnManager = VpnManager(scope)

        val japan = VpnCountryCatalog.findByCode("JP")
        val jsJapan = vpnManager.getSpoofingJavascript(japan)

        assertTrue(jsJapan.contains("Asia/Tokyo"))
        assertTrue(jsJapan.contains("ja-JP"))
        assertTrue(jsJapan.contains("35.6762")) // Tokyo latitude

        val india = VpnCountryCatalog.findByCode("IN")
        val jsIndia = vpnManager.getSpoofingJavascript(india)

        assertTrue(jsIndia.contains("Asia/Kolkata"))
        assertTrue(jsIndia.contains("hi-IN"))
        assertTrue(jsIndia.contains("19.076")) // Mumbai latitude
    }

    @Test
    fun `ad blocker strictly ignores non-youtube search requests`() {
        val adBlocker = YouTubeAdBlocker()
        // Non-YouTube context should never be intercepted
        val blocked = adBlocker.shouldIntercept(null, "https://www.google.com/search?q=india")
        org.junit.Assert.assertNull(blocked)
    }
}
