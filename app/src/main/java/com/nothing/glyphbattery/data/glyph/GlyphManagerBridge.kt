package com.nothing.glyphbattery.data.glyph

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Robust bridge to the Nothing Glyph Developer Kit.
 * Dynamically binds with com.nothing.ketchum.GlyphManager if available on device,
 * detects device model (Phone 1, Phone 2, Phone 2a, Phone 2a Plus, Phone 3a/4a),
 * and provides full simulation support for local UI testing.
 */
class GlyphManagerBridge(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "GlyphManagerBridge"

    private var glyphManagerInstance: Any? = null
    private var isBound = false
    private var isSessionActive = false

    private val _connectedFlow = MutableStateFlow(false)
    val connectedFlow = _connectedFlow.asStateFlow()

    private val _activeChannelsFlow = MutableStateFlow<Set<String>>(emptySet())
    val activeChannelsFlow = _activeChannelsFlow.asStateFlow()

    private val _detectedModel = MutableStateFlow(detectDeviceModel())
    val detectedModel = _detectedModel.asStateFlow()

    fun init() {
        try {
            val glyphManagerClass = Class.forName("com.nothing.ketchum.GlyphManager")
            val callbackClass = Class.forName("com.nothing.ketchum.GlyphManager\$Callback")

            val getInstanceMethod = glyphManagerClass.getMethod("getInstance", Context::class.java)
            glyphManagerInstance = getInstanceMethod.invoke(null, context.applicationContext)

            // Dynamic proxy for GlyphManager.Callback
            val callbackProxy = Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
                object : InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                        when (method.name) {
                            "onServiceConnected" -> {
                                Log.d(tag, "GlyphService connected successfully")
                                isBound = true
                                _connectedFlow.value = true
                                detectHardwareModelFromGdk()
                            }
                            "onServiceDisconnected" -> {
                                Log.d(tag, "GlyphService disconnected")
                                isBound = false
                                isSessionActive = false
                                _connectedFlow.value = false
                            }
                        }
                        return null
                    }
                }
            )

            val initMethod = glyphManagerClass.getMethod("init", callbackClass)
            initMethod.invoke(glyphManagerInstance, callbackProxy)
            Log.d(tag, "GlyphManager initialized with dynamic proxy")
        } catch (e: Exception) {
            Log.w(tag, "Native Nothing Glyph SDK not found or failed to initialize (Running in Simulator/Fallback mode): ${e.message}")
            _connectedFlow.value = true // Enable simulator mode
        }
    }

    private fun detectHardwareModelFromGdk() {
        val gm = glyphManagerInstance ?: return
        try {
            val cls = gm.javaClass

            // Check is23111 (Phone 2a)
            val is2a = runCatching { cls.getMethod("is23111").invoke(gm) as? Boolean }.getOrNull() == true
            // Check is23113 (Phone 2a Plus)
            val is2aPlus = runCatching { cls.getMethod("is23113").invoke(gm) as? Boolean }.getOrNull() == true
            // Check is22111 (Phone 2)
            val isPhone2 = runCatching { cls.getMethod("is22111").invoke(gm) as? Boolean }.getOrNull() == true
            // Check is20111 (Phone 1)
            val isPhone1 = runCatching { cls.getMethod("is20111").invoke(gm) as? Boolean }.getOrNull() == true
            // Check is24111 (Phone 3a/4a)
            val is3a = runCatching { cls.getMethod("is24111").invoke(gm) as? Boolean }.getOrNull() == true

            val model = when {
                is2a -> NothingDeviceModel.PHONE_2A
                is2aPlus -> NothingDeviceModel.PHONE_2A_PLUS
                isPhone2 -> NothingDeviceModel.PHONE_2
                isPhone1 -> NothingDeviceModel.PHONE_1
                is3a -> NothingDeviceModel.PHONE_3A_SERIES
                else -> detectDeviceModel()
            }
            _detectedModel.value = model
            Log.i(tag, "Detected Nothing Device from GDK: ${model.displayName}")
        } catch (e: Exception) {
            Log.w(tag, "Error detecting model from GDK: ${e.message}")
        }
    }

    private fun detectDeviceModel(): NothingDeviceModel {
        val model = Build.MODEL.uppercase()
        val device = Build.DEVICE.uppercase()
        val manufacturer = Build.MANUFACTURER.uppercase()

        if (!manufacturer.contains("NOTHING")) {
            return NothingDeviceModel.VIRTUAL_SIMULATOR
        }

        return when {
            model.contains("A142P") || device.contains("PACMANPRO") || model.contains("2A PLUS") -> NothingDeviceModel.PHONE_2A_PLUS
            model.contains("A142") || device.contains("PACMAN") || model.contains("2A") -> NothingDeviceModel.PHONE_2A
            model.contains("A065") || device.contains("PONG") || model.contains("PHONE 2") || model.contains("PHONE(2)") -> NothingDeviceModel.PHONE_2
            model.contains("A063") || device.contains("SPACELORD") || model.contains("PHONE 1") || model.contains("PHONE(1)") -> NothingDeviceModel.PHONE_1
            model.contains("3A") || model.contains("4A") -> NothingDeviceModel.PHONE_3A_SERIES
            else -> NothingDeviceModel.PHONE_2A
        }
    }

    fun setManualModel(model: NothingDeviceModel) {
        _detectedModel.value = model
    }

    fun openSession(): Boolean {
        val gm = glyphManagerInstance
        if (gm != null && isBound) {
            return try {
                val openSessionMethod = gm.javaClass.getMethod("openSession")
                openSessionMethod.invoke(gm)
                isSessionActive = true
                Log.d(tag, "GlyphManager session opened")
                true
            } catch (e: Exception) {
                Log.e(tag, "Failed to open Glyph session: ${e.message}")
                false
            }
        }
        isSessionActive = true // Simulator active
        return true
    }

    fun closeSession() {
        val gm = glyphManagerInstance
        if (gm != null && isSessionActive) {
            try {
                val closeSessionMethod = gm.javaClass.getMethod("closeSession")
                closeSessionMethod.invoke(gm)
                Log.d(tag, "GlyphManager session closed")
            } catch (e: Exception) {
                Log.e(tag, "Failed to close Glyph session: ${e.message}")
            }
        }
        isSessionActive = false
        _activeChannelsFlow.value = emptySet()
    }

    fun displayProgress(progress: Int, reverse: Boolean = false) {
        val clampedProgress = progress.coerceIn(0, 100)
        val gm = glyphManagerInstance

        // Update active simulated channels based on model
        updateSimulatedProgressChannels(clampedProgress)

        if (gm != null && isSessionActive) {
            try {
                val builder = getGlyphFrameBuilder()
                val frame = buildFrameWithProgress(builder, clampedProgress)

                val displayProgressMethod = gm.javaClass.methods.firstOrNull {
                    it.name == "displayProgress" && it.parameterTypes.size >= 2
                }

                if (displayProgressMethod != null) {
                    if (displayProgressMethod.parameterTypes.size == 3) {
                        displayProgressMethod.invoke(gm, frame, clampedProgress, reverse)
                    } else {
                        displayProgressMethod.invoke(gm, frame, clampedProgress)
                    }
                } else {
                    // Fallback to animate/toggle
                    val animateMethod = gm.javaClass.getMethod("animate", frame.javaClass)
                    animateMethod.invoke(gm, frame)
                }
            } catch (e: Exception) {
                Log.w(tag, "Error calling displayProgress on GDK: ${e.message}")
            }
        }
    }

    private fun getGlyphFrameBuilder(): Any? {
        val gm = glyphManagerInstance ?: return null
        return try {
            val getBuilderMethod = gm.javaClass.getMethod("getGlyphFrameBuilder")
            getBuilderMethod.invoke(gm)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildFrameWithProgress(builder: Any?, progress: Int): Any? {
        if (builder == null) return null
        val bCls = builder.javaClass

        val model = _detectedModel.value
        when (model) {
            NothingDeviceModel.PHONE_1 -> {
                runCatching { bCls.getMethod("buildChannelD").invoke(builder) }
            }
            NothingDeviceModel.PHONE_2 -> {
                runCatching { bCls.getMethod("buildChannelC1").invoke(builder) }
            }
            NothingDeviceModel.PHONE_2A, NothingDeviceModel.PHONE_2A_PLUS, NothingDeviceModel.PHONE_3A_SERIES -> {
                if (progress > 0) runCatching { bCls.getMethod("buildChannelA").invoke(builder) }
                if (progress > 33) runCatching { bCls.getMethod("buildChannelB").invoke(builder) }
                if (progress > 66) runCatching { bCls.getMethod("buildChannelC").invoke(builder) }
            }
            else -> {
                runCatching { bCls.getMethod("buildChannelA").invoke(builder) }
            }
        }

        return try {
            bCls.getMethod("build").invoke(builder)
        } catch (e: Exception) {
            null
        }
    }

    fun animateBreathingCycle() {
        val model = _detectedModel.value
        val gm = glyphManagerInstance

        // Simulator animation step
        _activeChannelsFlow.value = when (model) {
            NothingDeviceModel.PHONE_2A, NothingDeviceModel.PHONE_2A_PLUS, NothingDeviceModel.PHONE_3A_SERIES ->
                setOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_B, NothingGlyphConstants.CHANNEL_C)
            NothingDeviceModel.PHONE_2 ->
                setOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_B, NothingGlyphConstants.CHANNEL_C1_PROGRESS, NothingGlyphConstants.CHANNEL_D)
            else ->
                setOf(NothingGlyphConstants.CHANNEL_A, NothingGlyphConstants.CHANNEL_D, NothingGlyphConstants.CHANNEL_E)
        }

        if (gm != null && isSessionActive) {
            try {
                val builder = getGlyphFrameBuilder() ?: return
                val bCls = builder.javaClass
                bCls.getMethod("buildChannelA").invoke(builder)
                runCatching { bCls.getMethod("buildPeriod", Int::class.javaPrimitiveType).invoke(builder, 1500) }
                runCatching { bCls.getMethod("buildCycles", Int::class.javaPrimitiveType).invoke(builder, 1) }
                runCatching { bCls.getMethod("buildInterval", Int::class.javaPrimitiveType).invoke(builder, 200) }
                val frame = bCls.getMethod("build").invoke(builder)

                val animateMethod = gm.javaClass.getMethod("animate", frame.javaClass)
                animateMethod.invoke(gm, frame)
            } catch (e: Exception) {
                Log.w(tag, "Breathing animation failed: ${e.message}")
            }
        }
    }

    fun triggerQuickBatteryPulse(batteryLevel: Int) {
        coroutineScope.launch(Dispatchers.Default) {
            if (!isSessionActive) {
                openSession()
            }

            // Step 1: Flash all LEDs briefly
            _activeChannelsFlow.value = setOf("ALL")
            delay(150)
            _activeChannelsFlow.value = emptySet()
            delay(100)

            // Step 2: Show Battery Progress Meter
            displayProgress(batteryLevel)
            delay(2500)

            // Step 3: Fade to rest
            _activeChannelsFlow.value = emptySet()
        }
    }

    private fun updateSimulatedProgressChannels(progress: Int) {
        val model = _detectedModel.value
        _activeChannelsFlow.value = when (model) {
            NothingDeviceModel.PHONE_2A, NothingDeviceModel.PHONE_2A_PLUS, NothingDeviceModel.PHONE_3A_SERIES -> {
                val channels = mutableSetOf<String>()
                if (progress > 10) channels.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 45) channels.add(NothingGlyphConstants.CHANNEL_B)
                if (progress > 80) channels.add(NothingGlyphConstants.CHANNEL_C)
                channels
            }
            NothingDeviceModel.PHONE_2 -> {
                val channels = mutableSetOf<String>()
                channels.add(NothingGlyphConstants.CHANNEL_C1_PROGRESS)
                if (progress > 20) channels.add(NothingGlyphConstants.CHANNEL_D1_PROGRESS)
                if (progress > 50) channels.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 80) channels.add(NothingGlyphConstants.CHANNEL_B)
                channels
            }
            NothingDeviceModel.PHONE_1 -> {
                val channels = mutableSetOf<String>()
                channels.add(NothingGlyphConstants.CHANNEL_D)
                if (progress > 30) channels.add(NothingGlyphConstants.CHANNEL_C)
                if (progress > 60) channels.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 90) channels.add(NothingGlyphConstants.CHANNEL_B)
                channels
            }
            NothingDeviceModel.VIRTUAL_SIMULATOR -> {
                val channels = mutableSetOf<String>()
                if (progress > 15) channels.add(NothingGlyphConstants.CHANNEL_A)
                if (progress > 50) channels.add(NothingGlyphConstants.CHANNEL_B)
                if (progress > 80) channels.add(NothingGlyphConstants.CHANNEL_C)
                channels
            }
        }
    }

    fun turnOff() {
        _activeChannelsFlow.value = emptySet()
    }
}
