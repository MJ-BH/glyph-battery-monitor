package com.nothing.glyphbattery.data.source

import android.content.ComponentName
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import com.nothing.glyphbattery.data.dto.GlyphStateDto
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlyphManagerBridge(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val tag = "GlyphManagerBridge"

    private var glyphManager: GlyphManager? = null
    private var isBound = false
    private var isSessionActive = false
    private var cameraManager: CameraManager? = null
    private var primaryCameraId: String? = null

    private val isRealNothingPhone: Boolean = detectIsGenuineNothingHardware()
    private val detectedDeviceModel: NothingDeviceModel = detectInitialModel()

    private val _glyphDtoState = MutableStateFlow(
        GlyphStateDto(
            isConnected = false,
            isSessionOpen = false,
            activeModeName = "PROGRESS_BAR",
            currentProgress = 0,
            activeChannels = emptyList(),
            detectedModelCode = detectedDeviceModel.modelCode,
            syncWithCharging = true,
            flashOnPlugIn = true,
            lastFlashedTimestamp = 0L,
            isGenuineHardware = isRealNothingPhone,
            hardwareModelName = "${Build.MANUFACTURER} ${Build.MODEL} (${detectedDeviceModel.displayName})"
        )
    )
    val glyphDtoState: StateFlow<GlyphStateDto> = _glyphDtoState.asStateFlow()

    init {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            primaryCameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (_: Exception) {}
    }

    fun init() {
        if (!isRealNothingPhone) {
            Log.i(tag, "Host device is not Nothing hardware (${Build.MANUFACTURER} ${Build.MODEL}). Simulator & Torch fallback active.")
            _glyphDtoState.update {
                it.copy(
                    isConnected = true,
                    isGenuineHardware = false,
                    detectedModelCode = detectedDeviceModel.modelCode
                )
            }
            return
        }

        try {
            glyphManager = GlyphManager.getInstance(context.applicationContext)
            glyphManager?.init(object : GlyphManager.Callback {
                override fun onServiceConnected(componentName: ComponentName) {
                    Log.i(tag, "Nothing Ketchum Glyph Service Connected: $componentName")
                    isBound = true

                    val deviceKey = detectDeviceKey()
                    try {
                        glyphManager?.register(deviceKey)
                        Log.i(tag, "Registered Glyph device with key: $deviceKey")
                    } catch (e: Exception) {
                        Log.w(tag, "Register error: ${e.message}")
                    }

                    openSession()
                    detectHardwareModelFromGdk()

                    _glyphDtoState.update {
                        it.copy(isConnected = true, isGenuineHardware = true, isSessionOpen = isSessionActive)
                    }
                }

                override fun onServiceDisconnected(componentName: ComponentName) {
                    Log.w(tag, "Nothing Ketchum Glyph Service Disconnected: $componentName")
                    isBound = false
                    isSessionActive = false
                    _glyphDtoState.update {
                        it.copy(isConnected = false, isSessionOpen = false, activeChannels = emptyList())
                    }
                }
            })
            Log.i(tag, "Initialized Nothing Ketchum GDK Manager")
        } catch (e: Exception) {
            Log.w(tag, "Native GDK initialization error: ${e.message}")
            _glyphDtoState.update { it.copy(isConnected = true) }
        }
    }

    private fun detectDeviceKey(): String {
        return when {
            Common.is23113() -> "23113"
            Common.is23111() -> "23111"
            Common.is22111() -> "22111"
            Common.is20111() -> "20111"
            Common.is24111() -> "24111"
            Build.MODEL.contains("A142P", ignoreCase = true) -> "23113"
            Build.MODEL.contains("A142", ignoreCase = true) -> "23111"
            Build.MODEL.contains("A065", ignoreCase = true) -> "22111"
            Build.MODEL.contains("A063", ignoreCase = true) -> "20111"
            else -> "23111"
        }
    }

    private fun detectIsGenuineNothingHardware(): Boolean {
        val mfg = Build.MANUFACTURER.uppercase()
        val brand = Build.BRAND.uppercase()
        val model = Build.MODEL.uppercase()
        val device = Build.DEVICE.uppercase()

        return mfg.contains("NOTHING") || brand.contains("NOTHING") ||
                model.startsWith("A063") || model.startsWith("A065") ||
                model.startsWith("A142") || model.startsWith("A145") ||
                device.contains("PACMAN") || device.contains("PONG") ||
                device.contains("SPACEWAR") || device.contains("TETRIS")
    }

    private fun detectInitialModel(): NothingDeviceModel {
        val model = Build.MODEL.uppercase()
        val device = Build.DEVICE.uppercase()

        return when {
            model.contains("A142P") || device.contains("PACMANPRO") -> NothingDeviceModel.PHONE_2A_PLUS
            model.contains("A142") || device.contains("PACMAN") -> NothingDeviceModel.PHONE_2A
            model.contains("A065") || device.contains("PONG") -> NothingDeviceModel.PHONE_2
            model.contains("A063") || device.contains("SPACEWAR") -> NothingDeviceModel.PHONE_1
            model.contains("A145") || device.contains("TETRIS") || model.contains("3A") || model.contains("4A") -> NothingDeviceModel.PHONE_3A_SERIES
            isRealNothingPhone -> NothingDeviceModel.PHONE_2A
            else -> NothingDeviceModel.PHONE_2A
        }
    }

    private fun detectHardwareModelFromGdk() {
        val detected = when {
            Common.is23113() -> NothingDeviceModel.PHONE_2A_PLUS
            Common.is23111() -> NothingDeviceModel.PHONE_2A
            Common.is22111() -> NothingDeviceModel.PHONE_2
            Common.is20111() -> NothingDeviceModel.PHONE_1
            Common.is24111() -> NothingDeviceModel.PHONE_3A_SERIES
            else -> detectInitialModel()
        }
        _glyphDtoState.update {
            it.copy(
                detectedModelCode = detected.modelCode,
                hardwareModelName = "${Build.MANUFACTURER} ${Build.MODEL} (${detected.displayName})"
            )
        }
    }

    fun openSession(): Boolean {
        val gm = glyphManager
        if (gm != null && isBound) {
            return try {
                gm.openSession()
                isSessionActive = true
                _glyphDtoState.update { it.copy(isSessionOpen = true) }
                true
            } catch (e: Exception) {
                Log.w(tag, "Failed to open Glyph session: ${e.message}")
                false
            }
        }
        isSessionActive = true
        _glyphDtoState.update { it.copy(isSessionOpen = true) }
        return true
    }

    fun closeSession() {
        val gm = glyphManager
        if (gm != null && isSessionActive) {
            try {
                gm.closeSession()
            } catch (_: Exception) {}
        }
        isSessionActive = false
        _glyphDtoState.update { it.copy(isSessionOpen = false, activeChannels = emptyList()) }
    }

    fun displayProgress(progress: Int, reverse: Boolean = false) {
        val clamped = progress.coerceIn(0, 100)
        val channels = calculateActiveChannels(clamped)
        _glyphDtoState.update { it.copy(currentProgress = clamped, activeChannels = channels) }

        val gm = glyphManager
        if (gm != null && isSessionActive) {
            try {
                val builder = gm.glyphFrameBuilder ?: return

                when (_glyphDtoState.value.detectedModelCode) {
                    NothingGlyphConstants.MODEL_CODE_PHONE_1 -> {
                        builder.buildChannelD()
                    }
                    NothingGlyphConstants.MODEL_CODE_PHONE_2 -> {
                        builder.buildChannelC()
                    }
                    else -> {
                        // Phone (2a), Phone (2a)+, Phone (3a/4a)
                        if (clamped > 0) builder.buildChannelA()
                        if (clamped > 33) builder.buildChannelB()
                        if (clamped > 66) builder.buildChannelC()
                    }
                }
                val frame = builder.build()
                gm.displayProgress(frame, clamped, reverse)
            } catch (e: Exception) {
                Log.w(tag, "Error executing displayProgress on physical Glyph: ${e.message}")
            }
        }
    }

    private fun calculateActiveChannels(progress: Int): List<String> {
        val code = _glyphDtoState.value.detectedModelCode
        return when (code) {
            NothingGlyphConstants.MODEL_CODE_PHONE_2A, NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS, NothingGlyphConstants.MODEL_CODE_PHONE_3A -> {
                val list = mutableListOf<String>()
                if (progress > 5) list.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 35) list.add(NothingGlyphConstants.CHANNEL_B)
                if (progress > 70) list.add(NothingGlyphConstants.CHANNEL_C)
                list
            }
            NothingGlyphConstants.MODEL_CODE_PHONE_2 -> {
                val list = mutableListOf(NothingGlyphConstants.CHANNEL_C1_PROGRESS)
                if (progress > 20) list.add(NothingGlyphConstants.CHANNEL_D1_PROGRESS)
                if (progress > 50) list.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 80) list.add(NothingGlyphConstants.CHANNEL_B)
                list
            }
            else -> {
                val list = mutableListOf(NothingGlyphConstants.CHANNEL_D)
                if (progress > 30) list.add(NothingGlyphConstants.CHANNEL_C)
                if (progress > 60) list.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 90) list.add(NothingGlyphConstants.CHANNEL_B)
                list
            }
        }
    }

    fun animateBreathing() {
        val code = _glyphDtoState.value.detectedModelCode
        val active = when (code) {
            NothingGlyphConstants.MODEL_CODE_PHONE_2A, NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS ->
                listOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_B, NothingGlyphConstants.CHANNEL_C)
            NothingGlyphConstants.MODEL_CODE_PHONE_2 ->
                listOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_C1_PROGRESS, NothingGlyphConstants.CHANNEL_D)
            else ->
                listOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_D, NothingGlyphConstants.CHANNEL_E)
        }
        _glyphDtoState.update { it.copy(activeChannels = active) }

        val gm = glyphManager
        if (gm != null && isSessionActive) {
            try {
                val builder = gm.glyphFrameBuilder ?: return
                when (code) {
                    NothingGlyphConstants.MODEL_CODE_PHONE_2A, NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS -> {
                        builder.buildChannelA()
                        builder.buildChannelB()
                        builder.buildChannelC()
                    }
                    NothingGlyphConstants.MODEL_CODE_PHONE_2 -> {
                        builder.buildChannelA()
                        builder.buildChannelC()
                    }
                    else -> {
                        builder.buildChannelA()
                        builder.buildChannelD()
                    }
                }
                gm.animate(builder.build())
            } catch (_: Exception) {}
        }
    }

    fun triggerQuickBatteryPulse(batteryLevel: Int) {
        scope.launch(Dispatchers.Default) {
            if (!isSessionActive) openSession()

            // 1. Initial Strobe Burst on all physical channels
            _glyphDtoState.update {
                it.copy(
                    activeChannels = listOf("ALL", NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_B, NothingGlyphConstants.CHANNEL_C),
                    lastFlashedTimestamp = System.currentTimeMillis()
                )
            }

            val gm = glyphManager
            if (gm != null && isSessionActive) {
                try {
                    val builder = gm.glyphFrameBuilder
                    if (builder != null) {
                        builder.buildChannelA()
                        builder.buildChannelB()
                        builder.buildChannelC()
                        builder.buildChannelD()
                        builder.buildChannelE()
                        val frame = builder.build()
                        gm.animate(frame)
                    }
                } catch (e: Exception) {
                    Log.w(tag, "animate error: ${e.message}")
                }
            } else if (!isRealNothingPhone) {
                flashCameraTorchFallback()
            }

            delay(200)

            // 2. Clear momentarily
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
            if (gm != null && isSessionActive) {
                try {
                    gm.turnOff()
                } catch (_: Exception) {}
            }
            delay(100)

            // 3. Display Exact Battery Percentage on Physical Glyph LEDs
            displayProgress(batteryLevel)

            // 4. Hold the battery level indicator on the physical backplate for 2.5 seconds
            delay(2500)

            // 5. Clean up
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
            if (gm != null && isSessionActive) {
                try {
                    gm.turnOff()
                } catch (_: Exception) {}
            }
        }
    }

    private fun flashCameraTorchFallback() {
        val cm = cameraManager
        val camId = primaryCameraId
        if (cm != null && camId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cm.setTorchMode(camId, true)
                Thread.sleep(150)
                cm.setTorchMode(camId, false)
            } catch (_: Exception) {}
        }
    }

    fun setModel(model: NothingDeviceModel) {
        _glyphDtoState.update {
            it.copy(
                detectedModelCode = model.modelCode,
                hardwareModelName = if (it.isGenuineHardware) "${Build.MANUFACTURER} ${Build.MODEL} (${model.displayName})" else "SIMULATOR (${model.displayName})"
            )
        }
    }

    fun setSyncCharging(enabled: Boolean) {
        _glyphDtoState.update { it.copy(syncWithCharging = enabled) }
    }

    fun setFlashOnPlugIn(enabled: Boolean) {
        _glyphDtoState.update { it.copy(flashOnPlugIn = enabled) }
    }

    fun turnOff() {
        _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
        val gm = glyphManager
        if (gm != null && isSessionActive) {
            try {
                gm.turnOff()
            } catch (_: Exception) {}
        }
    }
}
