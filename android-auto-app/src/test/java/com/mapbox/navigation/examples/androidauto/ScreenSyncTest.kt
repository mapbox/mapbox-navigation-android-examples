package com.mapbox.navigation.examples.androidauto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenSyncTest {

    @Test
    fun `replace top is needed when the target screen is not already on top`() {
        assertTrue(shouldReplaceTop(currentKey = "FREE_DRIVE", targetKey = "ACTIVE_GUIDANCE"))
    }

    @Test
    fun `replace top is needed when there is no current screen yet`() {
        assertTrue(shouldReplaceTop(currentKey = null, targetKey = "FREE_DRIVE"))
    }

    @Test
    fun `replace top is not needed when the target screen is already on top`() {
        // Regression test: a phone-driven notify(...) call replaces the car's top screen, which
        // is observed back on the phone via MapboxScreenManager.screenEvent and would otherwise
        // call notify(...) again for the same screen, bouncing back into the car a second time.
        assertFalse(shouldReplaceTop(currentKey = "ACTIVE_GUIDANCE", targetKey = "ACTIVE_GUIDANCE"))
    }
}
