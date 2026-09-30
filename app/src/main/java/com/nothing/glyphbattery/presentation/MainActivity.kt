package com.nothing.glyphbattery.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothing.glyphbattery.GlyphBatteryApp
import com.nothing.glyphbattery.data.glyph.NothingGlyphConstants
import com.nothing.glyphbattery.presentation.shortcuts.AppShortcutsHandler
import com.nothing.glyphbattery.presentation.ui.dashboard.BatteryDashboardEvent
import com.nothing.glyphbattery.presentation.ui.dashboard.BatteryDashboardScreen
import com.nothing.glyphbattery.presentation.ui.dashboard.BatteryDashboardViewModel
import com.nothing.glyphbattery.presentation.ui.theme.GlyphBatteryTheme
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack

class MainActivity : ComponentActivity() {

    private val viewModel: BatteryDashboardViewModel by viewModels {
        val app = application as GlyphBatteryApp
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BatteryDashboardViewModel(
                    observeBatteryInfoUseCase = app.observeBatteryInfoUseCase,
                    controlGlyphUseCase = app.controlGlyphUseCase,
                    triggerGlyphBatteryFlashUseCase = app.triggerGlyphBatteryFlashUseCase,
                    glyphRepository = app.glyphRepository
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppShortcutsHandler.publishDynamicShortcuts(this)

        handleShortcutIntent(intent)

        setContent {
            GlyphBatteryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NothingBlack
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    BatteryDashboardScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        when (intent?.action) {
            NothingGlyphConstants.ACTION_FLASH_BATTERY -> {
                viewModel.onEvent(BatteryDashboardEvent.FlashGlyphBattery)
            }
            NothingGlyphConstants.ACTION_TOGGLE_CHARGING_GLOW -> {
                val current = viewModel.uiState.value.glyphState.syncWithCharging
                viewModel.onEvent(BatteryDashboardEvent.ToggleChargingGlow(!current))
            }
        }
    }
}
