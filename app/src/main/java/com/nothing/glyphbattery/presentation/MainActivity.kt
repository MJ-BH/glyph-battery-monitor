package com.nothing.glyphbattery.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothing.glyphbattery.data.source.NothingGlyphConstants
import com.nothing.glyphbattery.presentation.shortcuts.AppShortcutsHandler
import com.nothing.glyphbattery.ui.battery.BatteryScreen
import com.nothing.glyphbattery.ui.battery.BatteryUiEvent
import com.nothing.glyphbattery.ui.battery.BatteryViewModel
import com.nothing.glyphbattery.ui.state.UiState
import com.nothing.glyphbattery.ui.theme.GlyphBatteryTheme
import com.nothing.glyphbattery.ui.theme.NothingBlack
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private var activeViewModel: BatteryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppShortcutsHandler.publishDynamicShortcuts(this)

        setContent {
            val viewModel: BatteryViewModel = koinViewModel()
            activeViewModel = viewModel

            GlyphBatteryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NothingBlack
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    BatteryScreen(
                        uiState = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }

        handleShortcutIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        when (intent?.action) {
            NothingGlyphConstants.ACTION_FLASH_BATTERY -> {
                activeViewModel?.onEvent(BatteryUiEvent.FlashGlyphBattery)
            }
            NothingGlyphConstants.ACTION_TOGGLE_CHARGING_GLOW -> {
                val current = (activeViewModel?.uiState?.value as? UiState.Success)?.data?.glyphState?.syncWithCharging ?: false
                activeViewModel?.onEvent(BatteryUiEvent.ToggleChargingGlow(!current))
            }
        }
    }
}
