# ⚡ Nothing Glyph Battery & Charging Monitor

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.5-02303A.svg?style=flat&logo=gradle)](https://gradle.org)
[![AGP](https://img.shields.io/badge/AGP-8.8.2-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![JVM Target](https://img.shields.io/badge/JVM-21-orange.svg?style=flat&logo=openjdk)](https://openjdk.org)
[![Koin](https://img.shields.io/badge/Koin-4.0.2-blue.svg?style=flat)](https://insert-koin.io/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-brightgreen.svg?style=flat)](https://github.com/MJ-BH/android-basic-clean-architecture)
[![Nothing GDK](https://img.shields.io/badge/Nothing-Glyph_Developer_Kit-black.svg?style=flat)](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> A modern, open-source Android application engineered strictly following the **Clean Architecture** patterns from [`MJ-BH/android-basic-clean-architecture`](https://github.com/MJ-BH/android-basic-clean-architecture) using **Koin DI**, **Jetpack Compose (Material 3 + Nothing Dot-Matrix Design)**, and the official **Nothing Glyph Developer Kit (GDK)**. It brings real-time battery level monitoring, dynamic charging animations, home screen app widgets, launcher app shortcuts, and interactive LED backplate visualization to all Nothing phones.

---

## 📱 Screenshots & Visual Design

```
   ┌────────────────────────────────────────┐
   │  ● NOTHING      [ PHONE (2a) ▾ ]       │
   │    GLYPH BATTERY MONITOR               │
   ├────────────────────────────────────────┤
   │  ┌──────────────────────────────────┐  │
   │  │        [● CHARGING]              │  │
   │  │                                  │  │
   │  │              87%                 │  │
   │  │     ■ ■ ■ ■ ■ ■ ■ ■ ■ □ □        │  │
   │  └──────────────────────────────────┘  │
   │                                        │
   │  [ ⚡ FLASH BATTERY ON GLYPH ]         │
   │                                        │
   │  ┌──────────────────────────────────┐  │
   │  │  GLYPH INTERFACE    [LEDS ACTIVE]│  │
   │  │        ╭─────────╮               │  │
   │  │       ( ( ◎   ◎ ) )              │  │
   │  │        ╰─────────╯   ┃           │  │
   │  │             │        ┃ (Ribbon C)│  │
   │  │             │                    │  │
   │  └──────────────────────────────────┘  │
   │                                        │
   │  [HEALTH: GOOD]    [TEMP: 28.5 °C]     │
   │  [VOLTAGE: 4.15V]  [POWER: AC FAST]    │
   └────────────────────────────────────────┘
```

---

## ✨ Key Features

- 🔋 **Live Battery & Charging Diagnostics**: Real-time battery percentage, charging velocity/wattage, voltage, battery health, and temperature.
- 💡 **Nothing Glyph Developer Kit (GDK) Integration**:
  - **Battery Meter**: Maps charge percentage (0-100%) to physical LED channels.
  - **Breathing Pulse**: Smooth breathing glow while phone is charging.
  - **Cable Connect Auto-Pulse**: Automatically flashes the exact battery level on the physical Glyph LEDs when you plug in your charging cable.
- 📱 **Hardware Compatibility Across All Nothing Phones**:
  - **Nothing Phone (2a)** (`is23111`): 3 camera ribbon LED strips.
  - **Nothing Phone (2a) Plus** (`is23113`): Metallic accented ribbon strips.
  - **Nothing Phone (2)** (`is22111`): 33 LED zones (C1-C16 segmented progress bar & D1 indicator).
  - **Nothing Phone (1)** (`is20111`): 5 Glyph zones with D1 battery meter.
  - **Nothing Phone (3a / 4a Series)** (`is24111`): Next-gen Glyph interface support.
  - **Interactive Simulator**: On-screen Canvas visualizer for testing and emulators.
- 🧩 **Nothing Battery App Widget**: Clean, dot-matrix styled home screen widget with one-tap "Flash Glyph" action button.
- ⚡ **Launcher App Shortcuts**: Long-press app icon to instantly *Flash Glyph* or *Toggle Charging Glow*.

---

## 🏗️ Architecture Blueprint

The codebase strictly follows the Clean Architecture layers from [`MJ-BH/android-basic-clean-architecture`](https://github.com/MJ-BH/android-basic-clean-architecture):

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

## 🛠️ Hardware Setup (For Physical Devices)

To allow third-party apps to control the physical Glyph LEDs during development, enable Glyph Debugging via ADB:

```bash
# Enable Glyph Developer Mode on your device (valid for 48 hours)
adb shell settings put global nt_glyph_interface_debug_enable 1
```

---

## 🚀 Building & Testing

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 21+
- Android SDK 35

```bash
# Clone the repository
git clone https://github.com/MJ-BH/glyph-battery-monitor.git
cd glyph-battery-monitor

# Run Unit Tests
./gradlew test

# Build Debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for details.
