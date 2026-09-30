# ⚡ Mastering the Glyph Interface: Building a Clean Architecture Battery & Charging Monitor for Nothing Phones

*By **MJ-BH** | Published for Android Developers, Nothing Community (#Co-Creation), and Tech Enthusiasts*

---

![Banner](https://raw.githubusercontent.com/MJ-BH/glyph-battery-monitor/main/art/banner.png)

## 💡 Introduction: The Art of Hardware-Software Harmony

When Nothing introduced the **Glyph Interface**, it fundamentally transformed the back of the smartphone into a functional, ambient medium for notifications and visual progress indicators. 

As developers, the **Nothing Glyph Developer Kit (GDK)** gives us direct programmatic control over the individual LED channels and progress strips of the device.

In this article, I will walk you through how I built **Glyph Battery Monitor**—a modern Android application engineered with **Clean Architecture**, **Koin Dependency Injection**, **Jetpack Compose (Material 3 + Nothing Dot-Matrix Design)**, and the official **Nothing Glyph Developer Kit (GDK)**, supporting **Nothing Phone (2a)**, **Phone (2a) Plus**, **Phone (2)**, **Phone (1)**, and **future models (4a/3a)**.

We will explore:
1. Structuring the codebase with the **Clean Architecture pattern** from [`MJ-BH/android-basic-clean-architecture`](https://github.com/MJ-BH/android-basic-clean-architecture).
2. Implementing **Koin DI**, the functional **Result pattern (`Result<T, E>`)**, and sealed **UiState (`UiState<T>`)**.
3. Connecting to the Nothing Glyph Developer Kit via IPC/AIDL and managing LED sessions.
4. Building a reactive battery and charging data stream with Kotlin Coroutines `callbackFlow`.
5. Crafting a high-performance **interactive on-screen Glyph Visualizer** in Jetpack Compose Canvas.
6. Implementing a companion **Home Screen App Widget** and **Launcher App Shortcuts** for instant one-tap Glyph battery flashing.

---

## 🏗️ Architectural Foundations: Clean Architecture Blueprint

To ensure scalability, testability, and clear separation of concerns, the app is organized strictly according to the reference repository [`MJ-BH/android-basic-clean-architecture`](https://github.com/MJ-BH/android-basic-clean-architecture):

```
app/src/main/java/com/nothing/glyphbattery/
├── BaseApplication.kt                 # Koin DI initialization
├── core/
│   └── result/
│       └── Result.kt                  # Sealed functional Result<T, E> with fold() and getOrNull()
├── data/
│   ├── dto/
│   │   ├── BatteryInfoDto.kt          # Raw system battery metrics DTO
│   │   └── GlyphStateDto.kt           # Hardware bridge state DTO
│   ├── mapper/
│   │   ├── BatteryMapper.kt           # DTO -> Pure Domain BatteryInfo mapper
│   │   └── GlyphMapper.kt             # DTO -> Pure Domain GlyphState mapper
│   ├── receiver/
│   │   └── PowerConnectionReceiver.kt # BroadcastReceiver for power plug-in pulse (KoinComponent)
│   ├── repository/
│   │   ├── BatteryRepositoryImpl.kt   # Repository implementation with Dispatchers.IO
│   │   └── GlyphRepositoryImpl.kt     # Glyph hardware repository implementation
│   ├── service/
│   │   └── GlyphBatteryForegroundService.kt # Foreground sync service (KoinComponent)
│   └── source/
│       ├── BatteryDataSource.kt       # System BatteryManager & BroadcastReceiver flow
│       ├── GlyphManagerBridge.kt      # GDK Driver + reflection fallback + hardware detection
│       └── NothingGlyphConstants.kt   # Model codes, channels, and intent actions
├── di/
│   └── AppModule.kt                   # Koin Dependency Injection definitions
├── domain/
│   ├── model/
│   │   ├── BatteryHealth.kt           # Battery health enum
│   │   ├── BatteryInfo.kt             # Pure immutable domain model
│   │   ├── GlyphAnimationMode.kt      # Glyph animation modes
│   │   ├── GlyphState.kt              # Glyph UI & hardware state
│   │   ├── NothingDeviceModel.kt      # Nothing phone hardware models
│   │   └── PluggedType.kt             # Power connection type enum
│   ├── repository/
│   │   ├── BatteryRepository.kt       # Domain repository contracts returning Result & Flow
│   │   └── GlyphRepository.kt         # Glyph controller repository contract
│   └── usecase/
│       ├── ControlGlyphUseCase.kt     # Mode and model switching use case
│       ├── GetBatteryInfoUseCase.kt   # One-shot battery query returning Result
│       ├── GetNothingDeviceUseCase.kt # Device hardware query use case
│       ├── ObserveBatteryInfoUseCase.kt # Reactive battery Flow use case
│       └── TriggerGlyphBatteryFlashUseCase.kt # Momentary LED pulse use case
├── presentation/
│   ├── MainActivity.kt                # Jetpack Compose Activity using koinViewModel()
│   ├── shortcuts/
│   │   └── AppShortcutsHandler.kt     # Dynamic Launcher App Shortcuts
│   └── widget/
│       └── NothingBatteryWidgetProvider.kt # Dot-Matrix Home Screen Widget (KoinComponent)
└── ui/
    ├── battery/
    │   ├── BatteryScreen.kt           # Feature Screen handling UiState.Loading/Success/Error/Empty
    │   ├── BatteryUiEvent.kt          # MVI UI Events
    │   ├── BatteryUiModel.kt          # UI Presentation state holder
    │   ├── BatteryViewModel.kt        # ViewModel exposing StateFlow<UiState<BatteryUiModel>>
    │   └── components/
    │       ├── BatteryStatusMetricsGrid.kt # Diagnostics cards (temp, voltage, health)
    │       ├── DotMatrixBatteryLevel.kt    # Large dot-matrix battery meter
    │       ├── GlyphDeviceVisualizer.kt    # Animated hardware Canvas preview
    │       └── NothingHeader.kt            # Signature Nothing top app bar & model picker
    ├── state/
    │   └── UiState.kt                 # Generic sealed UI State interface
    └── theme/
        ├── Color.kt                   # OLED Black, Dot-Matrix White, Nothing Red tokens
        ├── Shapes.kt                  # Material 3 rounded curves
        ├── Theme.kt                   # Nothing Dark OLED Compose Theme
        └── Type.kt                    # Monospace typographic hierarchy
```

---

## ⚡ 1. Interfacing with the Nothing Glyph Developer Kit

To control physical Glyph LEDs, we declare the Ketchum permission and developer key in `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.nothing.ketchum.permission.ENABLE" />

<application ...>
    <meta-data android:name="NothingKey" android:value="test" />
</application>
```

### Initializing the Glyph Service & Managing Sessions
The `GlyphManagerBridge` interacts with Nothing's Ketchum system service with dynamic reflection safety and automated fallback simulation:

```kotlin
class GlyphManagerBridge(private val context: Context) {
    private var glyphManagerInstance: Any? = null

    fun init() {
        try {
            val glyphManagerClass = Class.forName("com.nothing.ketchum.GlyphManager")
            val callbackClass = Class.forName("com.nothing.ketchum.GlyphManager\$Callback")
            val getInstance = glyphManagerClass.getMethod("getInstance", Context::class.java)
            glyphManagerInstance = getInstance.invoke(null, context.applicationContext)
            // Session initialization and hardware binding...
        } catch (e: Exception) {
            Log.d("GlyphManagerBridge", "Using on-screen virtual simulator")
        }
    }
}
```

---

## 🔋 2. Reactive Battery Stream with Kotlin Coroutines

Using `callbackFlow`, we transform Android's `Intent.ACTION_BATTERY_CHANGED` broadcasts into a cold, lifecycle-safe Kotlin `Flow<BatteryInfoDto>` which is mapped into immutable domain models:

```kotlin
fun observeBatteryDto(): Flow<BatteryInfoDto> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { trySend(extractBatteryDto(it)) }
        }
    }

    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }

    context.registerReceiver(receiver, filter)
    awaitClose { context.unregisterReceiver(receiver) }
}
```

---

## 🎨 3. The UI: Jetpack Compose & Nothing Dot-Matrix Aesthetics

The user interface captures Nothing's signature industrial design:
- **Pure AMOLED Black (`#000000`)** background for OLED battery efficiency.
- **Dot-Matrix Typography** and segmented progress indicators.
- **Interactive Glyph Schematic Canvas**: Renders a live visual representation of the Nothing Phone backplate, lighting up the exact LED channels in sync with the physical hardware.

```kotlin
@Composable
fun DotMatrixBatteryLevel(level: Int, isCharging: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCharging) "CHARGING" else "DISCHARGING",
                fontFamily = FontFamily.Monospace,
                color = if (isCharging) NothingRed else NothingWhiteMuted,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$level%",
                fontFamily = FontFamily.Monospace,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = NothingWhite
            )
            DottedProgressBar(progress = level / 100f)
        }
    }
}
```

---

## 🧩 4. App Widget & App Shortcuts

- **Home Screen Widget** (`NothingBatteryWidgetProvider`): Displays live battery %, charging status, and provides a one-tap **"Flash Glyph"** button.
- **App Shortcuts**: Long-press launcher shortcuts for:
  - *Flash Battery on Glyph*
  - *Toggle Charging Glow*

---

## 🚀 Open Source on GitHub

The complete project is open source and available on GitHub:

🔗 **GitHub Repository**: [https://github.com/MJ-BH/glyph-battery-monitor](https://github.com/MJ-BH/glyph-battery-monitor)

### Testing on Your Device:
1. Enable Glyph Debugging:
   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
2. Build and install:
   ```bash
   ./gradlew installDebug
   ```

---

*Feel free to star the repo, fork, and contribute! Tag your builds on the Nothing Community forums with #Co-Creation.*
