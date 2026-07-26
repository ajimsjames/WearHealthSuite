# 🏥 WearHealthSuite (v1.0.0)

[![Wear OS](https://img.shields.io/badge/Wear%20OS-5.0-blue.svg)](https://developer.android.com/wear)
[![Device](https://img.shields.io/badge/Target-Samsung%20Galaxy%20Watch%206-black.svg)](https://www.samsung.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**WearHealthSuite** is a comprehensive, hardware-level health, bio-telemetry, and environmental sensor diagnostic suite built specifically for **Samsung Wear OS Smartwatches** (optimized for **Galaxy Watch 6**). It interfaces with all onboard physical, optical, electrical, and thermal hardware sensors to present real-time telemetry, sparkline graphs, and diagnostic readouts.

Developed by **Aju George** ([@ajimsjames](https://github.com/ajimsjames)).

---

## ✨ Features & Hardware Sensor Breakdown

### 🩸 1. Optical PPG Sensor Suite (`AFE4500S PPG`)
- **Heart Rate Monitor**: Real-time BPM calculation with live 20-sample sparkline trend canvas graph.
- **Heart Rate Variability (HRV)**: Live HRV index calculation in milliseconds.
- **Pulse Oximetry (SpO2)**: Blood Oxygen saturation percentage monitoring.

### ⚡ 2. BioActive Electrical Sensors (`AFE4500S ECG & BIA`)
- **ECG Bio-Voltage Visualizer**: Real-time 60fps ECG Lead-I bio-electrical voltage waveform trace.
- **Bioelectrical Impedance Analysis (BIA)**: Skeletal Muscle %, Body Fat %, and Hydration Body Water % metrics.

### 🌡️ 3. Thermal Sensor Cluster (`Samsung Skin Temp & Thermistor`)
- **Infrared Contact Skin Temperature**: Real-time surface skin temperature reading in Celsius (`°C`) & Fahrenheit (`°F`).
- **Board Thermistor**: Internal hardware temperature tracking with baseline status alerts.

### 🎈 4. Environmental & Altitude Sensors (`LPS28DFW Barometer & Light`)
- **Barometric Pressure**: High-precision atmospheric pressure sensor (`hPa` / `mbar`).
- **Barometric Altimeter**: Real-time sea-level elevation altimeter calculation (`m` / `ft`).
- **STK31E15 Ambient Light Sensor**: Illumination intensity metering (`Lux`).
- **Wrist Offbody Sensor**: Low-power capacitive & optical contact detector (`ON WRIST` vs `OFF WRIST`).

### 📐 5. Motion & Magnetic Sensors (`LSM6DSV IMU & AK09918C Magnetometer`)
- **6-Axis Motion IMU**: 3-Axis Accelerometer (`m/s²`, total G-Force) & 3-Axis Gyroscope (`°/s` angular velocity).
- **3-Axis Digital Magnetometer**: Geomagnetic field strength (`μT`) and 360° heading direction.
- **Pedestrian Step Counter**: Hardware step counter & tilt gesture engine.

### ⭕ 6. Bezel-Aligned Top Navigation (`CurvedLayout`)
- Curved top navigation tabs (`🩸 Heart`, `⚡ ECG`, `🌡️ Temp`, `🎈 Baro`, `📐 IMU`, `⚙️ About`) aligned with the Galaxy Watch 6 display bezel.
- Extra top padding (`40.dp`) ensuring zero cutoff by top curved bezel pills.

---

## 🛠️ Technology Stack

* **Platform**: Android Wear OS (Min SDK 30 / Target SDK 33 / Wear OS 5)
* **Language**: Kotlin 1.9
* **UI Framework**: Jetpack Compose for Wear OS + Wear Material & `CurvedLayout`
* **Sensor Architecture**: Android `SensorManager` + Low-level HAL Event Listeners
* **Graphics**: Jetpack Compose Canvas 2D Vector Path Rendering for ECG & PPG Sparklines

---

## 🚀 Installation via Wireless ADB

```bash
# Connect to Galaxy Watch 6 via Wireless ADB
adb connect <WATCH_IP_ADDRESS>:<PORT>

# Install Release APK directly onto the smartwatch
adb install -r WearHealthSuite-v1.0.0.apk
```

---

## 👨‍💻 Author & Credits

Developed by **Aju George**  
GitHub: [@ajimsjames](https://github.com/ajimsjames)
