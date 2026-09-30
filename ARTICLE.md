# ⚡ Bridging the Glyph: Building an Intelligent Safety Beacon & Bio-Pulse Companion for Nothing Phone & CMF Watch Pro 2

*By **MJ-BH** | Published for Android Developers, Nothing Community (#Co-Creation), and Tech Enthusiasts*

---

![Cover Art](https://raw.githubusercontent.com/MJ-BH/glyph-beacon-cmf/main/art/banner.png)

## 💡 The Inspiration: Unlocking the Untapped Power of the Glyph

When Carl Pei introduced the **Glyph Interface**, it redefined smartphone hardware by transforming the rear glass into an expressive, functional canvas of light. But while built-in features like the battery charging meter and timer progress bars are great, the true potential of the Nothing ecosystem lies in **hardware synergy**—specifically when you pair a Nothing Phone with wearable tech like the **CMF Watch Pro 2**.

What if your phone wasn't just a screen in your pocket, but an **automotive-grade smart cycling brake light**, a **live bio-pulse visualizer reflecting your real-time heart rate**, and an **optical Morse code emergency transmitter**?

Today, I am open-sourcing **Glyph Beacon & CMF Companion**—a clean architecture Android application built with Jetpack Compose that connects Nothing Phone (2a, 2, 1, 3a) with the CMF Watch Pro 2 over Bluetooth Low Energy.

---

## 🌟 What the App Does

```
 ┌───────────────────────────┐         Bluetooth LE         ┌───────────────────────────┐
 │     CMF Watch Pro 2       │ ───────────────────────────► │    Nothing Phone (2a)     │
 │  • Real-time Wrist BPM    │                              │  • 3 Ribbon LED Strips    │
 │  • Functional Crown / SOS │                              │  • Automotive Brake Light │
 │  • Workout Telemetry      │                              │  • Optical Morse SOS      │
 └───────────────────────────┘                              └───────────────────────────┘
```

### 1. 🚴 Smart Bike Safety Beacon with Real-Time Brake Light
When you mount your Nothing Phone on your bike handle, backpack, or running armband:
- **Speed-Adaptive Cadence Strobe**: The Glyph LEDs flash at a tempo that dynamically scales with your riding speed (faster strobe when sprinting or riding in traffic).
- **Automotive-Grade Brake Light**: Using accelerometer and GPS sensor fusion, the app instantly detects deceleration (braking force) and ramps all rear Glyph LEDs to **100% solid bright glow**, warning motorists behind you before returning to cadence strobing.

### 2. 🫀 CMF Watch Pro 2 Heart Bio-Pulse Mirroring
- Streams your live wrist pulse from the CMF Watch Pro 2 over BLE GATT.
- Converts your heart rate into a rhythmic systolic *'lub-dub'* pulse across the physical Glyph LEDs.
- **Training Intensity Zones**: Dynamically visualizes whether you are in *Warm-Up*, *Aerobic / Fat Burn*, *Cardio*, or *Peak Stride* training zones.

### 3. 🚨 Optical Morse Code SOS & Text Transmitter
- One-tap distress beacon that encodes text into international **Morse Code `... --- ...` (SOS)**.
- Flashes optical Morse sequences on the Glyph LEDs for high-visibility outdoor rescue, blackout signaling, or covert communications.

### 4. 👏 Acoustic Clap & Watch "Find Phone" Supercharger
- Uses low-latency audio thresholding to detect acoustic **double-claps** in a dark room.
- Fires a 12-burst ultra-bright high-frequency strobe, instantly illuminating the room to reveal your phone without turning on the screen.

---

## 🏗️ Technical Architecture: Clean Architecture & Modern Android

The project strictly follows **Clean Architecture** principles to separate hardware sensor drivers, domain rules, and presentation UI:

```
app/src/main/java/com/nothing/glyphbattery/
├── domain/                      # 100% Pure Kotlin Domain Layer
│   ├── model/                  # BeaconSession, CmfWatchMetrics, MorseMessage, GlyphBeaconMode
│   ├── repository/             # BeaconRepository, CmfWatchRepository
│   └── usecase/                # Single-responsibility interactors
├── data/                        # Data & Hardware Layer
│   ├── ble/                    # CmfBleManager (BLE GATT Heart Rate Client)
│   ├── motion/                 # SpeedAndBrakeDetector (Sensor Fusion)
│   ├── audio/                  # ClapDetectorSource (AudioRecord PCM thresholding)
│   ├── glyph/                  # GlyphManagerBridge (Nothing Ketchum SDK Bridge)
│   └── service/                # GlyphBeaconForegroundService (Background execution)
└── presentation/                # Jetpack Compose UI
    ├── ui/theme/               # Nothing Dark Theme + CMF Orange (#FF5722)
    ├── ui/components/          # CmfWatchDialVisualizer, GlyphBeaconVisualizer
    └── ui/dashboard/           # BeaconCompanionViewModel (MVI StateFlow)
```

---

## 🔧 Deep Dive: Key Code Snippets

### 1. Interfacing with Nothing Glyph Developer Kit (GDK)
```kotlin
val gm = GlyphManager.getInstance(context)
gm.init(object : GlyphManager.Callback {
    override fun onServiceConnected(componentName: ComponentName) {
        gm.openSession()
        // Hardware Detection: Supports Phone (2a), Phone (2), Phone (1)
        val is2a = gm.is23111()
    }
    override fun onServiceDisconnected(componentName: ComponentName) {
        gm.closeSession()
    }
})
```

### 2. Optical Morse Timing Sequence
```kotlin
fun toSignalSequence(text: String): List<MorseSignal> {
    val signals = mutableListOf<MorseSignal>()
    for (char in text.uppercase()) {
        val morse = MORSE_MAP[char] ?: continue
        for (symbol in morse) {
            when (symbol) {
                '.' -> signals.add(MorseSignal.LightOn(150L))
                '-' -> signals.add(MorseSignal.LightOn(450L))
            }
            signals.add(MorseSignal.LightOff(150L)) // Element gap
        }
        signals.add(MorseSignal.LightOff(350L)) // Letter gap
    }
    return signals
}
```

---

## 🎨 Design Language: Nothing Dot-Matrix meets CMF Industrial Design

The UI is built with **Jetpack Compose (Material 3)**, fusing Nothing OS's minimalist dot-matrix monochrome aesthetics with **CMF's signature vibrant orange (`#FF5722`)**.

The screen features an interactive **CMF Watch Pro 2 circular dial Canvas** that visually mimics the aluminum case and functional crown, updating live with the BPM gauge alongside an interactive **Nothing Phone (2a) backplate schematic**.

---

## 🚀 Open Source on GitHub

The complete project is open-source and available on GitHub:

🔗 **GitHub Repository**: [https://github.com/MJ-BH/glyph-beacon-cmf](https://github.com/MJ-BH/glyph-beacon-cmf)

### Try it yourself:
1. Clone the repository: `git clone https://github.com/MJ-BH/glyph-beacon-cmf.git`
2. Enable Glyph Debugging on your Nothing Phone:
   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
3. Run on your Nothing Phone (2a) and pair with your CMF Watch Pro 2!

---

*What other creative hardware integrations would you love to see with the Nothing Glyph Interface? Let's discuss in the comments or on the Nothing Community forums with #Co-Creation!*
