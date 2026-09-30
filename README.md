# ⚡ Nothing Glyph Battery & Charging Monitor

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.12.1-02303A.svg?style=flat&logo=gradle)](https://gradle.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Nothing GDK](https://img.shields.io/badge/Nothing-Glyph_Developer_Kit-black.svg?style=flat)](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-blue.svg?style=flat)](https://developer.android.com/topic/architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> A modern, open-source Android application engineered with **Clean Architecture**, **Jetpack Compose (Material 3 + Nothing Dot-Matrix Design)**, and the official **Nothing Glyph Developer Kit (GDK)**. It brings real-time battery level monitoring, dynamic charging animations, home screen app widgets, launcher app shortcuts, and interactive LED backplate visualization to all Nothing phones.

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

## 🏗️ Architecture Blueprint (Clean Architecture)

```
                            ┌────────────────────────────────────────┐
                            │           Presentation Layer           │
                            │      Jetpack Compose (Material 3)      │
                            │  MVI StateFlow • Widgets • Shortcuts   │
                            └───────────────────┬────────────────────┘
                                                │
                                                ▼
                            ┌────────────────────────────────────────┐
                            │              Domain Layer              │
                            │  100% Pure Kotlin • Single-Resp UseCases│
                            │  BatteryInfo • NothingDevice • Glyph   │
                            └───────────────────┬────────────────────┘
                                                │
                        ┌───────────────────────┴───────────────────────┐
                        ▼                                               ▼
            ┌───────────────────────┐                       ┌───────────────────────┐
            │       Data Layer      │                       │     Hardware Layer    │
            │ BatteryManager Flow   │                       │   Nothing GDK Bridge  │
            │ Foreground Service    │                       │ Phone 1, 2, 2a, 2a+, 4a│
            └───────────────────────┘                       └───────────────────────┘
```

---

## 🛠️ Hardware Setup (For Physical Devices)

To allow third-party apps to control the physical Glyph LEDs during development, enable Glyph Debugging via ADB:

```bash
# Enable Glyph Developer Mode on your device (valid for 48 hours)
adb shell settings put global nt_glyph_interface_debug_enable 1
```

---

## 🚀 Building & Installing

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 21+
- Android SDK 35

```bash
# Clone the repository
git clone https://github.com/MJ-BH/glyph-battery-monitor.git
cd glyph-battery-monitor

# Run Unit Tests
./gradlew testDebugUnitTest

# Build & Install Debug APK
./gradlew installDebug
```

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for details.
