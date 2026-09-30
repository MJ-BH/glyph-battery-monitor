package com.nothing.glyphbattery.data.source

import android.content.Context
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

    private val _glyphDtoState = MutableStateFlow(
        GlyphStateDto(
            isConnected = false,
            isSessionOpen = false,
            activeModeName = "PROGRESS_BAR",
            currentProgress = 0,
            activeChannels = emptyList(),
            detectedModelCode = detectInitialModelCode(),
            syncWithCharging = true,
            flashOnPlugIn = true,
            lastFlashedTimestamp = 0L
        )
    )
    val glyphDtoState: StateFlow<GlyphStateDto> = _glyphDtoState.asStateFlow()

    fun init() {
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
                                Log.d(tag, "GlyphService connected")
                                isBound = true
                                _glyphDtoState.update { it.copy(isConnected = true) }
                                detectHardwareModelFromGdk()
                            }
                            "onServiceDisconnected" -> {
                                Log.d(tag, "GlyphService disconnected")
                                isBound = false
                                isSessionActive = false
                                _glyphDtoState.update { it.copy(isConnected = false, isSessionOpen = false, activeChannels = emptyList()) }
                            }
                        }
                        return null
                    }
                }
            )

            val initMethod = glyphManagerClass.getMethod("init", callbackClass)
            initMethod.invoke(glyphManagerInstance, callbackProxy)
        } catch (e: Exception) {
            Log.w(tag, "Native GDK not found (Running in Simulator Mode): ${e.message}")
            _glyphDtoState.update { it.copy(isConnected = true) }
        }
    }

    private fun detectInitialModelCode(): String {
        val model = Build.MODEL.uppercase()
        val device = Build.DEVICE.uppercase()
        return when {
            model.contains("A142P") || device.contains("PACMANPRO") -> NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS
            model.contains("A142") || device.contains("PACMAN") -> NothingGlyphConstants.MODEL_CODE_PHONE_2A
            model.contains("A065") || device.contains("PONG") -> NothingGlyphConstants.MODEL_CODE_PHONE_2
            model.contains("A063") || device.contains("SPACELORD") -> NothingGlyphConstants.MODEL_CODE_PHONE_1
            model.contains("3A") || model.contains("4A") -> NothingGlyphConstants.MODEL_CODE_PHONE_3A
            else -> NothingGlyphConstants.MODEL_CODE_PHONE_2A
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

            val code = when {
                is2aPlus -> NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS
                is2a -> NothingGlyphConstants.MODEL_CODE_PHONE_2A
                isPhone2 -> NothingGlyphConstants.MODEL_CODE_PHONE_2
                isPhone1 -> NothingGlyphConstants.MODEL_CODE_PHONE_1
                else -> detectInitialModelCode()
            }
            _glyphDtoState.update { it.copy(detectedModelCode = code) }
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
            } catch (_: Exception) {}
        }
    }

    private fun calculateActiveChannels(progress: Int): List<String> {
        val code = _glyphDtoState.value.detectedModelCode
        return when (code) {
            NothingGlyphConstants.MODEL_CODE_PHONE_2A, NothingGlyphConstants.MODEL_CODE_PHONE_2A_PLUS, NothingGlyphConstants.MODEL_CODE_PHONE_3A -> {
                val list = mutableListOf<String>()
                if (progress > 10) list.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 45) list.add(NothingGlyphConstants.CHANNEL_B)
                if (progress > 80) list.add(NothingGlyphConstants.CHANNEL_C)
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
            _glyphDtoState.update { it.copy(activeChannels = listOf("ALL"), lastFlashedTimestamp = System.currentTimeMillis()) }
            delay(150)
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
            delay(100)
            displayProgress(batteryLevel)
            delay(2500)
            _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
        }
    }

    fun setModel(model: NothingDeviceModel) {
        _glyphDtoState.update { it.copy(detectedModelCode = model.modelCode) }
    }

    fun setSyncCharging(enabled: Boolean) {
        _glyphDtoState.update { it.copy(syncWithCharging = enabled) }
    }

    fun setFlashOnPlugIn(enabled: Boolean) {
        _glyphDtoState.update { it.copy(flashOnPlugIn = enabled) }
    }

    fun turnOff() {
        _glyphDtoState.update { it.copy(activeChannels = emptyList()) }
    }
}
