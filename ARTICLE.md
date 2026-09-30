# ⚡ Mastering the Glyph Interface: Building a Clean Architecture Battery & Charging Monitor for Nothing Phones

*By **MJ-BH** | Published for Android Developers, Nothing Community (#Co-Creation), and Tech Enthusiasts*

---

![Banner](https://raw.githubusercontent.com/MJ-BH/glyph-battery-monitor/main/art/banner.png)

## 💡 Introduction: The Art of Hardware-Software Harmony

When Nothing introduced the **Glyph Interface**, it fundamentally transformed the back of the smartphone into a functional, ambient medium for notifications and visual progress indicators. 

As developers, the **Nothing Glyph Developer Kit (GDK)** gives us direct programmatic control over the individual LED channels and progress strips of the device.

In this article, I will walk you through how I built **Glyph Battery Monitor**—a modern Android application created with **Jetpack Compose (Material 3 + Nothing Dot-Matrix Design)** and **Clean Architecture**, supporting **Nothing Phone (2a)**, **Phone (2a) Plus**, **Phone (2)**, **Phone (1)**, and **future models (4a/3a)**.

We will explore:
1. Connecting to the Nothing Glyph Developer Kit via IPC/AIDL and managing LED sessions.
2. Building a reactive battery and charging data stream with Kotlin Coroutines `callbackFlow`.
3. Crafting a high-performance **interactive on-screen Glyph Visualizer** in Jetpack Compose Canvas.
4. Implementing a companion **Home Screen App Widget** and **Launcher App Shortcuts** for instant one-tap Glyph battery flashing.

---

## 🏗️ Architectural Foundations: Clean Architecture Blueprint

To ensure scalability, testability, and clear separation of concerns, the app is organized into three distinct layers:

```
app/src/main/java/com/nothing/glyphbattery/
├── domain/                      # 100% Pure Kotlin Business Logic
│   ├── model/                  # BatteryInfo, NothingDeviceModel, GlyphState
│   ├── repository/             # BatteryRepository, GlyphRepository interfaces
│   └── usecase/                # ObserveBatteryInfoUseCase, ControlGlyphUseCase, TriggerGlyphBatteryFlashUseCase
├── data/                        # Hardware Drivers & Data Sources
│   ├── battery/                # BatteryDataSource (callbackFlow + BroadcastReceiver)
│   ├── glyph/                  # GlyphManagerBridge (GDK dynamic binding + fallback)
│   └── service/                # GlyphBatteryForegroundService (charging glow sync)
└── presentation/                # Jetpack Compose UI
    ├── ui/theme/               # Nothing Dot-Matrix Typography & AMOLED Dark Theme
    ├── ui/components/          # DotMatrixBatteryLevel, GlyphDeviceVisualizer
    ├── ui/dashboard/           # BatteryDashboardViewModel & Screen
    ├── widget/                 # NothingBatteryWidgetProvider (App Widget)
    └── shortcuts/              # AppShortcutsHandler (Dynamic Shortcuts)
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
The `GlyphManager` interacts with Nothing's system service:

```kotlin
class GlyphManagerBridge(private val context: Context) {
    private var glyphManager: GlyphManager? = null

    fun init() {
        glyphManager = GlyphManager.getInstance(context)
        glyphManager?.init(object : GlyphManager.Callback {
            override fun onServiceConnected(componentName: ComponentName) {
                // Open a session before sending LED commands
                glyphManager?.openSession()
                detectDeviceHardware()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                glyphManager?.closeSession()
            }
        })
    }
}
```

### Multi-Device Hardware Detection
Nothing phones feature distinct LED layouts:
- **Phone (1)** (`is20111`): 5 zones (A, B, C, D, E), with `D1` functioning as the battery progress strip.
- **Phone (2)** (`is22111`): 33 zones (A, B, C1-C16 coil, D1_1-D1_8 progress meter).
- **Phone (2a) & (2a) Plus** (`is23111`, `is23113`): 3 camera ribbon LED strips.

```kotlin
fun displayBatteryProgress(progress: Int) {
    val builder = glyphManager?.glyphFrameBuilder ?: return

    when {
        glyphManager?.is23111() == true || glyphManager?.is23113() == true -> {
            // Phone (2a) ribbon strips
            if (progress > 0) builder.buildChannelA()
            if (progress > 33) builder.buildChannelB()
            if (progress > 66) builder.buildChannelC()
        }
        glyphManager?.is22111() == true -> {
            // Phone (2) circular coil & progress channel
            builder.buildChannelC1()
        }
        else -> {
            // Phone (1) D1 channel
            builder.buildChannelD()
        }
    }

    val frame = builder.build()
    glyphManager?.displayProgress(frame, progress)
}
```

---

## 🔋 2. Reactive Battery Stream with Kotlin Coroutines

Using `callbackFlow`, we transform Android's sticky `Intent.ACTION_BATTERY_CHANGED` broadcasts into a cold, lifecycle-safe Kotlin `Flow<BatteryInfo>`:

```kotlin
fun observeBattery(): Flow<BatteryInfo> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { trySend(parseBatteryIntent(it)) }
        }
    }

    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }

    val stickyIntent = context.registerReceiver(receiver, filter)
    stickyIntent?.let { trySend(parseBatteryIntent(it)) }

    awaitClose { context.unregisterReceiver(receiver) }
}
```

---

## 🎨 3. The UI: Jetpack Compose & Nothing Dot-Matrix Aesthetics

The user interface captures Nothing's signature industrial design:
- **Pure AMOLED Black (`#000000`)** background for battery efficiency.
- **Dot-Matrix Typography** and segmented progress dots.
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
