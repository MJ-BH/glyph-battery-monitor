# ⚡ Glyph Beacon & CMF Watch Pro 2 Companion

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.12.1-02303A.svg?style=flat&logo=gradle)](https://gradle.org)
[![Android](https://img.shields.io/badge/Platform-Android_10+-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Nothing GDK](https://img.shields.io/badge/Nothing-Glyph_Developer_Kit-black.svg?style=flat)](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit)
[![CMF by Nothing](https://img.shields.io/badge/CMF_by_Nothing-Watch_Pro_2-FF5722.svg?style=flat)](https://intl.cmf.tech)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> An innovative outdoor safety, biometric bio-pulse, and emergency optical signaling application seamlessly connecting **Nothing Phone Glyph LEDs** (Phone 2a, Phone 2, Phone 1, 3a series) with the **CMF Watch Pro 2** smartwatch over Bluetooth Low Energy (BLE) and sensor fusion.

---

## 🌟 Key Innovations & Features

### 1. 🚴 Smart Cycling & Running Rear Safety Beacon
* **Automotive-Grade Brake Light**: Uses phone accelerometer and GPS sensor fusion to automatically detect deceleration and ramp the rear Glyph LEDs to solid 100% brightness.
* **Speed-Adaptive Cadence Strobe**: Strobe flashing tempo dynamically speeds up as your riding/running pace increases.

### 2. 🫀 CMF Watch Pro 2 Heart Bio-Pulse
* **Live Wrist Heart Rate Mirroring**: Connects to the CMF Watch Pro 2 over BLE GATT and pulses the physical Glyph LEDs in real-time tempo with your heartbeat.
* **Training Zone Visualization**: Dynamically color-codes and sequences LED channels across **Warm-Up**, **Aerobic / Fat Burn**, **Cardio**, and **Peak Stride** intensity zones.

### 3. 🚨 Optical Morse Code SOS & Text Transmitter
* **Emergency Optical SOS**: One-tap international Morse code (`... --- ...`) distress signal flashing across all rear Glyph LED channels.
* **Custom Text Encoder**: Type any text message and transmit it as optical Morse light sequences.

### 4. 👏 Acoustic Clap & Watch "Find Phone" Supercharger
* **Acoustic Double-Clap Trigger**: Detects acoustic double-clap spikes with low-latency audio processing to fire high-frequency finding strobe bursts in pitch-dark rooms.
* **CMF Watch Remote Trigger**: Intercepts CMF Watch Pro 2 gestures or "Find Phone" commands to unleash a 12-pulse ultra-bright strobe burst.

---

## 🏗️ Architecture Blueprint (Clean Architecture)

```
                            ┌────────────────────────────────────────┐
                            │           Presentation Layer           │
                            │   Jetpack Compose (M3 + Dot Matrix)    │
                            │      CMF Dial & Glyph Visualizer       │
                            └───────────────────┬────────────────────┘
                                                │
                                                ▼
                            ┌────────────────────────────────────────┐
                            │              Domain Layer              │
                            │  UseCases • BeaconSession • MorseModel │
                            └───────────────────┬────────────────────┘
                                                │
                        ┌───────────────────────┴───────────────────────┐
                        ▼                                               ▼
            ┌───────────────────────┐                       ┌───────────────────────┐
            │       Data Layer      │                       │     Hardware Layer    │
            │ BLE GATT • GPS Motion │                       │   Nothing GDK Bridge  │
            │   Audio Clap Detector │                       │ Phone 2a, 2, 1, 3a / 4a│
            └───────────────────────┘                       └───────────────────────┘
```

---

## 🛠️ Hardware Setup & Permissions

### Enable Glyph Debugging on Nothing Phones:
```bash
# Required on physical Nothing devices during development (valid 48h)
adb shell settings put global nt_glyph_interface_debug_enable 1
```

### Bluetooth & Sensors:
- **Bluetooth LE**: Automatically scans and pairs with `CMF Watch Pro 2`.
- **Sensors**: Location & Accelerometer for speed & braking detection; Microphone for acoustic clap detection.

---

## 🚀 Building & Running

```bash
# Clone the repository
git clone https://github.com/MJ-BH/glyph-beacon-cmf.git
cd glyph-beacon-cmf

# Run Unit Tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug
```

---

## 📱 Supported Devices

| Device Model | GDK Method | Glyph LED Configuration |
| :--- | :--- | :--- |
| **Nothing Phone (2a)** | `is23111()` | 3 Camera Ribbon LED Strips |
| **Nothing Phone (2a) Plus** | `is23113()` | 3 Metallic Accented Ribbon Strips |
| **Nothing Phone (2)** | `is22111()` | 33 LED Zones (C1-C16 Coil + D1 Progress) |
| **Nothing Phone (1)** | `is20111()` | 5 Glyph Zones (A-E) |
| **Nothing Phone (3a / 4a)** | `is24111()` | Next-Gen Matrix / Ribbon Array |
| **CMF Watch Pro 2** | BLE GATT | 1.32" Round AMOLED Dial + Functional Crown |

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
