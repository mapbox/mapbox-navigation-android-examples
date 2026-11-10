package com.mapbox.navigation.examples.androidauto.car

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.OnCheckedChangeDelegateImpl
import androidx.car.app.model.Row
import androidx.car.app.model.Toggle
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.voice.api.MapboxAudioGuidance
import com.mapbox.navigation.voice.api.MapboxAudioGuidanceState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@SuppressLint("RestrictedApi")
class CarSettingsTemplateTest {

    private val screen = mock(Screen::class.java)
    private val carContext = mock(CarContext::class.java)
    private val audioGuidance = mock(MapboxAudioGuidance::class.java)
    private val state = mock(MapboxAudioGuidanceState::class.java)
    private lateinit var textUtils: MockedStatic<TextUtils>
    private lateinit var toggleDelegate: MockedConstruction<OnCheckedChangeDelegateImpl>
    private lateinit var onCheckedChange: Toggle.OnCheckedChangeListener

    @Before
    fun setUp() {
        `when`(screen.carContext).thenReturn(carContext)
        `when`(carContext.getString(anyInt())).thenAnswer { "String ${it.arguments[0]}" }
        `when`(audioGuidance.stateFlow()).thenReturn(MutableStateFlow(state))
        textUtils = mockStatic(TextUtils::class.java)
        textUtils.`when`<Boolean> { TextUtils.isEmpty(any()) }
            .thenAnswer { it.getArgument<CharSequence?>(0).isNullOrEmpty() }
        toggleDelegate = mockConstruction(OnCheckedChangeDelegateImpl::class.java) { _, context ->
            onCheckedChange = context.arguments().single() as Toggle.OnCheckedChangeListener
        }
    }

    @After
    fun tearDown() {
        toggleDelegate.close()
        textUtils.close()
    }

    @Test
    fun `settings list supplies back and voice guidance content`() {
        val template = buildCarSettingsTemplate(screen, audioGuidance)
        val row = template.singleList!!.items.single() as Row

        assertEquals(Action.BACK, template.headerAction)
        assertEquals(carContext.getString(R.string.car_settings_title), template.title.toString())
        assertEquals(carContext.getString(R.string.car_settings_voice_guidance), row.title.toString())
        assertEquals(
            carContext.getString(R.string.car_settings_voice_guidance_description),
            row.texts.single().toString(),
        )
    }

    @Test
    fun `voice guidance is checked when audio is unmuted`() {
        assertTrue(voiceGuidanceToggle().isChecked)
    }

    @Test
    fun `template reads latest shared mute state`() {
        assertTrue(voiceGuidanceToggle().isChecked)

        `when`(state.isMuted).thenReturn(true)

        assertFalse(voiceGuidanceToggle().isChecked)
    }

    @Test
    fun `enabling voice guidance unmutes audio and refreshes owning screen`() {
        voiceGuidanceToggle()

        onCheckedChange.onCheckedChange(true)

        verify(audioGuidance).unmute()
        verify(screen).invalidate()
    }

    @Test
    fun `disabling voice guidance mutes audio and refreshes owning screen`() {
        voiceGuidanceToggle()

        onCheckedChange.onCheckedChange(false)

        verify(audioGuidance).mute()
        verify(screen).invalidate()
    }

    private fun voiceGuidanceToggle(): Toggle {
        val template = buildCarSettingsTemplate(screen, audioGuidance)
        return (template.singleList!!.items.single() as Row).toggle!!
    }
}
