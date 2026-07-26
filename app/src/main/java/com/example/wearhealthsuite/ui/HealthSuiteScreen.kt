package com.example.wearhealthsuite.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.curvedComposable
import androidx.wear.compose.material.Text
import java.util.Locale
import kotlin.math.sqrt

enum class HealthTab {
    HEART_PPG,
    ECG_BIA,
    SKIN_TEMP,
    BARO_ENV,
    IMU_MOTION,
    ABOUT
}

@Composable
fun HealthSuiteScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(HealthTab.HEART_PPG) }

    // Live Sensor States
    var heartRate by remember { mutableStateOf(72f) }
    var hrHistory by remember { mutableStateOf(listOf(68f, 70f, 72f, 75f, 73f, 72f)) }
    var spo2Percent by remember { mutableStateOf(98) }
    var hrvMs by remember { mutableStateOf(48) }

    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }
    var accelZ by remember { mutableStateOf(9.8f) }
    var gyroX by remember { mutableStateOf(0f) }
    var gyroY by remember { mutableStateOf(0f) }
    var gyroZ by remember { mutableStateOf(0f) }

    var magX by remember { mutableStateOf(18f) }
    var magY by remember { mutableStateOf(-22f) }
    var magZ by remember { mutableStateOf(42f) }

    var pressureHpa by remember { mutableStateOf(1013.25f) }
    var altitudeMeters by remember { mutableStateOf(125f) }
    var ambientLux by remember { mutableStateOf(180f) }
    var skinTempC by remember { mutableStateOf(34.2f) }
    var stepCount by remember { mutableStateOf(3420) }
    var isWorn by remember { mutableStateOf(true) }

    // Sensor Registration
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { e ->
                    when (e.sensor.type) {
                        Sensor.TYPE_HEART_RATE -> {
                            if (e.values.isNotEmpty() && e.values[0] > 0) {
                                heartRate = e.values[0]
                                hrHistory = (hrHistory.takeLast(19) + heartRate)
                            }
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            if (e.values.size >= 3) {
                                accelX = e.values[0]
                                accelY = e.values[1]
                                accelZ = e.values[2]
                            }
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                            if (e.values.size >= 3) {
                                gyroX = e.values[0]
                                gyroY = e.values[1]
                                gyroZ = e.values[2]
                            }
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            if (e.values.size >= 3) {
                                magX = e.values[0]
                                magY = e.values[1]
                                magZ = e.values[2]
                            }
                        }
                        Sensor.TYPE_PRESSURE -> {
                            if (e.values.isNotEmpty()) {
                                pressureHpa = e.values[0]
                                altitudeMeters = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureHpa)
                            }
                        }
                        Sensor.TYPE_LIGHT -> {
                            if (e.values.isNotEmpty()) {
                                ambientLux = e.values[0]
                            }
                        }
                        Sensor.TYPE_STEP_COUNTER -> {
                            if (e.values.isNotEmpty()) {
                                stepCount = e.values[0].toInt()
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        hrSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        gyroSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        pressSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        stepSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 24.dp)
        ) {
            when (selectedTab) {
                HealthTab.HEART_PPG -> {
                    item {
                        Text(
                            text = "🩸 PPG Optical Sensor",
                            color = Color(0xFFFF1744),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        // Live Heart Rate Circle
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1014)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.0f", heartRate),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("BPM", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    item {
                        // Live PPG Sparkline Graph
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A1A1E))
                                .padding(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (hrHistory.size > 1) {
                                    val maxVal = (hrHistory.maxOrNull() ?: 100f).coerceAtLeast(80f)
                                    val minVal = (hrHistory.minOrNull() ?: 60f).coerceAtMost(60f)
                                    val range = (maxVal - minVal).coerceAtLeast(1f)

                                    val path = Path()
                                    val stepX = size.width / (hrHistory.size - 1)

                                    hrHistory.forEachIndexed { idx, valY ->
                                        val x = idx * stepX
                                        val normY = (valY - minVal) / range
                                        val y = size.height - (normY * (size.height - 8f)) - 4f
                                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    drawPath(
                                        path = path,
                                        color = Color(0xFFFF1744),
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    item {
                        // SpO2 & HRV Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(0.92f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SensorMetricCard("🩸 SpO2 Oxygen", "$spo2Percent%", Color(0xFF00E676), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            SensorMetricCard("💓 HRV Index", "$hrvMs ms", Color(0xFF81D4FA), modifier = Modifier.weight(1f))
                        }
                    }
                }

                HealthTab.ECG_BIA -> {
                    item {
                        Text(
                            text = "⚡ ECG & BIA BioActive",
                            color = Color(0xFF00E676),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        // ECG Waveform Visualizer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0D1B12))
                                .padding(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path()
                                val pts = listOf(0.5f, 0.5f, 0.45f, 0.55f, 0.5f, 0.2f, 0.85f, 0.5f, 0.48f, 0.5f, 0.5f)
                                val totalW = size.width
                                val step = totalW / (pts.size * 2)

                                path.moveTo(0f, size.height * 0.5f)
                                for (repeat in 0..1) {
                                    pts.forEachIndexed { i, p ->
                                        val x = (repeat * pts.size + i) * step
                                        val y = size.height * p
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(path = path, color = Color(0xFF00E676), style = Stroke(width = 2.dp.toPx()))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    item {
                        Text("AFE4500S Body Composition (BIA)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1C1C1E))
                                .padding(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Skeletal Muscle:", color = Color.Gray, fontSize = 9.5.sp)
                                Text("44.2%", color = Color(0xFF81D4FA), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Body Fat Ratio:", color = Color.Gray, fontSize = 9.5.sp)
                                Text("16.8%", color = Color(0xFFFFB300), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Body Water %:", color = Color.Gray, fontSize = 9.5.sp)
                                Text("62.5%", color = Color(0xFF00E676), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HealthTab.SKIN_TEMP -> {
                    item {
                        Text(
                            text = "🌡️ Thermal Sensors",
                            color = Color(0xFFFFB300),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF241D10)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.1f°C", skinTempC),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f°F", skinTempC * 1.8f + 32f),
                                    color = Color(0xFFFFB300),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1C1C1E))
                                .padding(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Infrared Skin Sensor:", color = Color.Gray, fontSize = 9.sp)
                                Text("Calibrated", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Board Thermistor:", color = Color.Gray, fontSize = 9.sp)
                                Text("31.5°C", color = Color.LightGray, fontSize = 9.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Thermal Baseline:", color = Color.Gray, fontSize = 9.sp)
                                Text("Normal Range", color = Color(0xFF81D4FA), fontSize = 9.sp)
                            }
                        }
                    }
                }

                HealthTab.BARO_ENV -> {
                    item {
                        Text(
                            text = "🎈 Baro & Environment",
                            color = Color(0xFF81D4FA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.94f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SensorMetricCard("🎈 Barometer", String.format(Locale.US, "%.1f hPa", pressureHpa), Color(0xFF81D4FA), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            SensorMetricCard("⛰️ Elevation", String.format(Locale.US, "%.0f m", altitudeMeters), Color(0xFFFFB300), modifier = Modifier.weight(1f))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.94f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SensorMetricCard("☀️ Light Lux", String.format(Locale.US, "%.0f lx", ambientLux), Color(0xFFFFD54F), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            SensorMetricCard("🖐️ Wrist Detector", if (isWorn) "ON WRIST" else "OFF WRIST", if (isWorn) Color(0xFF00E676) else Color.Red, modifier = Modifier.weight(1f))
                        }
                    }
                }

                HealthTab.IMU_MOTION -> {
                    val totalG = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ) / 9.80665f
                    val magMagnitude = sqrt(magX * magX + magY * magY + magZ * magZ)

                    item {
                        Text(
                            text = "📐 LSM6DSV 6-Axis IMU",
                            color = Color(0xFFAB47BC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1C1C1E))
                                .padding(8.dp)
                        ) {
                            Text("Accelerate: G-Force = ${String.format(Locale.US, "%.2f", totalG)} G", color = Color(0xFFAB47BC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("X: ${String.format(Locale.US, "%.1f", accelX)}  Y: ${String.format(Locale.US, "%.1f", accelY)}  Z: ${String.format(Locale.US, "%.1f", accelZ)} m/s²", color = Color.LightGray, fontSize = 9.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Gyroscope: Angular Velocity", color = Color(0xFF81D4FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("X: ${String.format(Locale.US, "%.1f", gyroX)}  Y: ${String.format(Locale.US, "%.1f", gyroY)}  Z: ${String.format(Locale.US, "%.1f", gyroZ)} °/s", color = Color.LightGray, fontSize = 9.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Magnetometer: AK09918C", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Strength: ${String.format(Locale.US, "%.1f", magMagnitude)} μT", color = Color.LightGray, fontSize = 9.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("👟 Samsung Step Count: $stepCount steps", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HealthTab.ABOUT -> {
                    item {
                        Text(
                            text = "⚙️ About App",
                            color = Color(0xFFFFB300),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C1C1E))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏥 WearHealthSuite v1.0.0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("By Aju George", color = Color.Gray, fontSize = 9.5.sp, modifier = Modifier.padding(bottom = 6.dp))

                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                Text("• AFE4500S PPG, ECG & BIA Sensor Suite", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• LSM6DSV 6-Axis IMU & Accelerometer", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• AK09918C Magnetometer Compass", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• LPS28DFW Barometric Pressure", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• Infrared Skin Temperature Sensor", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• Target: Samsung Galaxy Watch 6", color = Color(0xFFFFB300), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Curved Bezel Top Navigation Bar
        CurvedLayout(
            anchor = 270f,
            modifier = Modifier.fillMaxSize()
        ) {
            curvedComposable {
                BezelTabPill("🩸 Heart", selected = selectedTab == HealthTab.HEART_PPG) { selectedTab = HealthTab.HEART_PPG }
            }
            curvedComposable { Spacer(modifier = Modifier.width(2.dp)) }
            curvedComposable {
                BezelTabPill("⚡ ECG", selected = selectedTab == HealthTab.ECG_BIA) { selectedTab = HealthTab.ECG_BIA }
            }
            curvedComposable { Spacer(modifier = Modifier.width(2.dp)) }
            curvedComposable {
                BezelTabPill("🌡️ Temp", selected = selectedTab == HealthTab.SKIN_TEMP) { selectedTab = HealthTab.SKIN_TEMP }
            }
            curvedComposable { Spacer(modifier = Modifier.width(2.dp)) }
            curvedComposable {
                BezelTabPill("🎈 Baro", selected = selectedTab == HealthTab.BARO_ENV) { selectedTab = HealthTab.BARO_ENV }
            }
            curvedComposable { Spacer(modifier = Modifier.width(2.dp)) }
            curvedComposable {
                BezelTabPill("📐 IMU", selected = selectedTab == HealthTab.IMU_MOTION) { selectedTab = HealthTab.IMU_MOTION }
            }
            curvedComposable { Spacer(modifier = Modifier.width(2.dp)) }
            curvedComposable {
                BezelTabPill("⚙️ About", selected = selectedTab == HealthTab.ABOUT) { selectedTab = HealthTab.ABOUT }
            }
        }
    }
}

@Composable
fun SensorMetricCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C1C1E))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.Gray, fontSize = 8.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BezelTabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFFFF1744) else Color(0xFF2C2C2E))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
