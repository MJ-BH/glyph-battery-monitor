package com.nothing.glyphbattery.domain.repository

import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for controlling the physical Nothing Glyph Interface and simulation.
 */
interface GlyphRepository {
    /**
     * Observable state flow of the Glyph status.
     */
    val glyphState: StateFlow<GlyphState>

    /**
     * Initializes the connection to Nothing Glyph Service.
     */
    suspend fun initialize()

    /**
     * Opens a session with the Glyph Manager.
     */
    suspend fun openSession(): Boolean

    /**
     * Closes the active Glyph session to conserve battery.
     */
    suspend fun closeSession()

    /**
     * Displays a progress bar on the appropriate device LED channel (0 - 100).
     */
    suspend fun displayProgress(progress: Int, reverse: Boolean = false)

    /**
     * Sets the active animation mode (Breathing, Progress, Pulse, Steady, Off).
     */
    suspend fun setAnimationMode(mode: GlyphAnimationMode)

    /**
     * Triggers a momentary flash / pulse animation representing the battery percentage.
     */
    suspend fun triggerBatteryFlash(batteryLevel: Int)

    /**
     * Toggles whether the Glyph charging glow is enabled while plugged in.
     */
    suspend fun setSyncWithCharging(enabled: Boolean)

    /**
     * Manually overrides or specifies the active device model (useful for simulation or testing).
     */
    suspend fun setDeviceModel(model: NothingDeviceModel)

    /**
     * Releases all hardware bindings.
     */
    fun release()
}
