package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.CctvRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Thai CCTV Live", appName)
  }

  @Test
  fun `verify genuine Koh Samui live CCTV cameras`() {
    val cameras = CctvRepository.cameras
    assertEquals(4, cameras.size)

    val crystalBay = CctvRepository.getCameraById("crystal_bay")
    assertNotNull(crystalBay)
    assertEquals("Fw9hgttWzIg", crystalBay?.youtubeVideoId)

    val baobab = CctvRepository.getCameraById("baobab_lamai")
    assertNotNull(baobab)
    assertEquals("Tpj0cmMVOd0", baobab?.youtubeVideoId)

    val bondi = CctvRepository.getCameraById("bondi_aussie")
    assertNotNull(bondi)
    assertEquals("-lI2AH2WkEA", bondi?.youtubeVideoId)

    val elGaucho = CctvRepository.getCameraById("el_gaucho_bophut")
    assertNotNull(elGaucho)
    assertEquals("CSp55hSd_6A", elGaucho?.youtubeVideoId)
  }

  @Test
  fun `verify YouTube player HTML generation and safe data extraction`() {
    val html = com.example.ui.components.generateYouTubePlayerHtml("Fw9hgttWzIg", autoPlay = true)
    org.junit.Assert.assertTrue(html.contains("Fw9hgttWzIg"))
    org.junit.Assert.assertTrue(html.contains("extractEventData"))
    org.junit.Assert.assertTrue(html.contains("onYouTubeIframeAPIReady"))
    org.junit.Assert.assertTrue(html.contains("AndroidBridge"))
  }
}
