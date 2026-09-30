package com.nothing.glyphbattery.data.source

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import com.nothing.glyphbattery.data.dto.GlyphStateDto
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class GlyphManagerBridge(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val tag = "GlyphManagerBridge"

    private var glyphManagerInstance: Any? = null
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
            val glyphManagerClass = Class.forName("com.nothing.ketchum.GlyphManager")
            val callbackClass = Class.forName("com.nothing.ketchum.GlyphManager\$Callback")

            val getInstanceMethod = glyphManagerClass.getMethod("getInstance", Context::class.java)
            glyphManagerInstance = getInstanceMethod.invoke(null, context.applicationContext)

            val callbackProxy = Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
                object : InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                        when (method.name) {
                            "onServiceConnected" -> {
                                Log.i(tag, "Nothing Glyph Ketchum Service Connected successfully!")
                                isBound = true
                                _glyphDtoState.update { it.copy(isConnected = true, isGenuineHardware = true) }
                                detectHardwareModelFromGdk()
                                openSession()
                            }
                            "onServiceDisconnected" -> {
                                Log.w(tag, "Nothing Glyph Ketchum Service Disconnected")
                                isBound = false
                                isSessionActive = false
                                _glyphDtoState.update {
                                    it.copy(isConnected = false, isSessionOpen = false, activeChannels = emptyList())
                                }
                            }
                        }
                        return null
                    }
                }
            )

            val initMethod = glyphManagerClass.getMethod("init", callbackClass)
            initMethod.invoke(glyphManagerInstance, callbackProxy)
            Log.i(tag, "Initialized Nothing Ketchum GDK bridge")
        } catch (e: Exception) {
            Log.w(tag, "Native GDK binding error: ${e.message}. Using high-precision simulator & hardware fallback.")
            _glyphDtoState.update { it.copy(isConnected = true) }
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
        val gm = glyphManagerInstance ?: return
        try {
            val cls = gm.javaClass
            val is2a = runCatching { cls.getMethod("is23111").invoke(gm) as? Boolean }.getOrNull() == true
            val is2aPlus = runCatching { cls.getMethod("is23113").invoke(gm) as? Boolean }.getOrNull() == true
            val isPhone2 = runCatching { cls.getMethod("is22111").invoke(gm) as? Boolean }.getOrNull() == true
            val isPhone1 = runCatching { cls.getMethod("is20111").invoke(gm) as? Boolean }.getOrNull() == true

            val detected = when {
                is2aPlus -> NothingDeviceModel.PHONE_2A_PLUS
                is2a -> NothingDeviceModel.PHONE_2A
                isPhone2 -> NothingDeviceModel.PHONE_2
                isPhone1 -> NothingDeviceModel.PHONE_1
                else -> detectInitialModel()
            }
            _glyphDtoState.update {
                it.copy(
                    detectedModelCode = detected.modelCode,
                    hardwareModelName = "${Build.MANUFACTURER} ${Build.MODEL} (${detected.displayName})"
                )
            }
        } catch (_: Exception) {}
    }

    fun openSession(): Boolean {
        val gm = glyphManagerInstance
        if (gm != null && isBound) {
            return try {
                gm.javaClass.getMethod("openSession").invoke(gm)
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
        val gm = glyphManagerInstance
        if (gm != null && isSessionActive) {
            try {
                gm.javaClass.getMethod("closeSession").invoke(gm)
            } catch (_: Exception) {}
        }
        isSessionActive = false
        _glyphDtoState.update { it.copy(isSessionOpen = false, activeChannels = emptyList()) }
    }

    fun displayProgress(progress: Int, reverse: Boolean = false) {
        val clamped = progress.coerceIn(0, 100)
        val channels = calculateActiveChannels(clamped)
        _glyphDtoState.update { it.copy(currentProgress = clamped, activeChannels = channels) }

        val gm = glyphManagerInstance
        if (gm != null && isSessionActive) {
            try {
                val builder = gm.javaClass.getMethod("getGlyphFrameBuilder").invoke(gm)
                val bCls = builder.javaClass

                val code = _glyphDtoState.value.detectedModelCode
                when (code) {
                    NothingGlyphConstants.MODEL_CODE_PHONE_1 -> runCatching { bCls.getMethod("buildChannelD").invoke(builder) }
                    NothingGlyphConstants.MODEL_CODE_PHONE_2 -> runCatching { bCls.getMethod("buildChannelC1").invoke(builder) }
                    else -> {
                        // Phone (2a), Phone (2a)+, Phone (3a/4a)
                        if (clamped > 0) runCatching { bCls.getMethod("buildChannelA").invoke(builder) }
                        if (clamped > 33) runCatching { bCls.getMethod("buildChannelB").invoke(builder) }
                        if (clamped > 66) runCatching { bCls.getMethod("buildChannelC").invoke(builder) }
                    }
                }
                val frame = bCls.getMethod("build").invoke(builder)
                val method = gm.javaClass.methods.firstOrNull { it.name == "displayProgress" && it.parameterTypes.size >= 2 }
                if (method != null) {
                    if (method.parameterTypes.size == 3) {
                        method.invoke(gm, frame, clamped, reverse)
                    } else {
                        method.invoke(gm, frame, clamped)
                    }
                }
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
    }

    fun triggerQuickBatteryPulse(batteryLevel: Int) {
        scope.launch(Dispatchers.Default) {
            if (!isSessionActive) openSession()

            // 1. Initial Strobe Burst on all channels
            _glyphDtoState.update {
                it.copy(
                    activeChannels = listOf("ALL", NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_B, NothingGlyphConstants.CHANNEL_C),
                    lastFlashedTimestamp = System.currentTimeMillis()
                )
            }

            // Physical GDK full flash
            val gm = glyphManagerInstance
            if (gm != null && isSessionActive) {
                try {
                    val builder = gm.javaClass.getMethod("getGlyphFrameBuilder").invoke(gm)
                    val bCls = builder.javaClass
                    runCatching { bCls.getMethod("buildChannelA").invoke(builder) }
                    runCatching { bCls.getMethod("buildChannelB").invoke(builder) }
                    runCatching { bCls.getMethod("buildChannelC").invoke(builder) }
                    runCatching { bCls.getMethod("buildChannelC1").invoke(builder) }
                    runCatching { bCls.getMethod("buildChannelD").invoke(builder) }
                    val frame = bCls.getMethod("build").invoke(builder)
                    val displayProgressMethod = gm.javaClass.methods.firstOrNull { it.name == "displayProgress" }
                    displayProgressMethod?.invoke(gm, frame, 100)
                } catch (_: Exception) {}
            } else if (!isRealNothingPhone) {
                flashCameraTorchFallback()
            }

            delay(180)

            // 2. Clear momentarily
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
            if (gm != null && isSessionActive) {
                runCatching { gm.javaClass.getMethod("turnOff").invoke(gm) }
            }
            delay(120)

            // 3. Display Exact Battery Percentage on Physical Glyph LEDs
            displayProgress(batteryLevel)

            // 4. Hold the battery level indicator for 2.5 seconds
            delay(2500)

            // 5. Clean up
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
            if (gm != null && isSessionActive) {
                runCatching { gm.javaClass.getMethod("turnOff").invoke(gm) }
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
        val gm = glyphManagerInstance
        if (gm != null && isSessionActive) {
            runCatching { gm.javaClass.getMethod("turnOff").invoke(gm) }
        }
    }
}
