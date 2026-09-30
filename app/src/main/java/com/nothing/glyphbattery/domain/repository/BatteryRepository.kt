package com.nothing.glyphbattery.domain.repository

import com.nothing.glyphbattery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for observing and reading system battery state.
 */
interface BatteryRepository {
    /**
     * Observes real-time battery status changes as a cold Flow.
     */
    fun observeBatteryInfo(): Flow<BatteryInfo>

    /**
     * Retrieves immediate battery snapshot synchronously/cached.
     */
    fun getBatteryInfoSnapshot(): BatteryInfo
}
