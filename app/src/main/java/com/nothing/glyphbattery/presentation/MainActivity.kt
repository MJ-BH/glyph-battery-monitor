package com.nothing.glyphbattery.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothing.glyphbattery.GlyphBatteryApp
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.presentation.shortcuts.AppShortcutsHandler
import com.nothing.glyphbattery.presentation.ui.dashboard.BeaconCompanionEvent
import com.nothing.glyphbattery.presentation.ui.dashboard.BeaconCompanionScreen
import com.nothing.glyphbattery.presentation.ui.dashboard.BeaconCompanionViewModel
import com.nothing.glyphbattery.presentation.ui.theme.GlyphBatteryTheme
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack

class MainActivity : ComponentActivity() {

    private val viewModel: BeaconCompanionViewModel by viewModels {
        val app = application as GlyphBatteryApp
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BeaconCompanionViewModel(
                    observeBeaconMetricsUseCase = app.observeBeaconMetricsUseCase,
                    observeCmfHeartRateUseCase = app.observeCmfHeartRateUseCase,
                    controlGlyphBeaconUseCase = app.controlGlyphBeaconUseCase,
                    transmitMorseSosUseCase = app.transmitMorseSosUseCase,
                    triggerFindPhoneStrobeUseCase = app.triggerFindPhoneStrobeUseCase
                ) as T
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        AppShortcutsHandler.publishDynamicShortcuts(this)

        handleShortcutIntent(intent)

        setContent {
            GlyphBatteryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NothingBlack
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    BeaconCompanionScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        when (intent?.action) {
            "ACTION_START_BIKE_BEACON" -> {
                viewModel.onEvent(BeaconCompanionEvent.SelectBeaconMode(GlyphBeaconMode.BIKE_SMART_STROBE))
            }
            "ACTION_START_HEART_SYNC" -> {
                viewModel.onEvent(BeaconCompanionEvent.SelectBeaconMode(GlyphBeaconMode.HEART_BIO_PULSE))
            }
            "ACTION_TRIGGER_SOS" -> {
                viewModel.onEvent(BeaconCompanionEvent.TransmitSos)
            }
        }
    }
}
