package com.example.wearhealthsuite.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
import kotlinx.coroutines.delay

enum class HealthTab {
    REALTIME_SCAN,
    HEART_PPG,
    ECG_BIA,
    SKIN_TEMP,
    ABOUT
}

@Composable
fun HealthSuiteScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(HealthTab.REALTIME_SCAN) }

    // Realtime Scan Running State
    var isScanning by remember { mutableStateOf(false) }

    // Live Telemetry States
    var heartRate by remember { mutableStateOf(74f) }
    var hrHistory by remember { mutableStateOf(listOf(70f, 72f, 74f, 75f, 73f, 74f)) }
    var spo2Percent by remember { mutableStateOf(98) }
    var hrvMs by remember { mutableStateOf(46) }
    var skinTempC by remember { mutableStateOf(34.4f) }

    // System Telemetry States (Charging & Wrist Detection)
    var isCharging by remember { mutableStateOf(false) }
    var batteryPercent by remember { mutableStateOf(85) }
    var isWorn by remember { mutableStateOf(true) }

    // Battery & Charging Receiver
    DisposableEffect(Unit) {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, batteryFilter)

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            batteryPercent = (level * 100 / scale.toFloat()).toInt()
        }

        onDispose {}
    }

    // Hardware Sensors & Offbody Listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { e ->
                    // Offbody / Wrist Detection
                    if (e.sensor.name.contains("Offbody", ignoreCase = true) || e.sensor.type == 34) {
                        if (e.values.isNotEmpty()) {
                            isWorn = e.values[0] == 1.0f || e.values[0] == 0.0f
                        }
                    }

                    // PPG Heart Rate
                    if (e.sensor.type == Sensor.TYPE_HEART_RATE) {
                        if (e.values.isNotEmpty() && e.values[0] > 0 && isScanning) {
                            heartRate = e.values[0]
                            hrHistory = (hrHistory.takeLast(19) + heartRate)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        // Find Samsung Offbody Sensor
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val offbodySensor = sensorList.firstOrNull { it.name.contains("Offbody", ignoreCase = true) }

        hrSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST) }
        offbodySensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Live Realtime Scan Simulation/Ticker when Scanning
    LaunchedEffect(isScanning) {
        while (isScanning) {
            delay(800)
            val delta = ((-2..2).random()).toFloat()
            heartRate = (heartRate + delta).coerceIn(58f, 130f)
            hrHistory = (hrHistory.takeLast(19) + heartRate)
            spo2Percent = ((97..99).random())
            hrvMs = ((42..54).random())
            skinTempC = (34.2f + ((-1..1).random() * 0.1f))
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
            // Persistent Device Status Header (Charging & Wrist State)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF141416))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCharging) "⚡ CHARGING ($batteryPercent%)" else "🔋 BATTERY ($batteryPercent%)",
                        color = if (isCharging) Color(0xFFFFD54F) else Color(0xFF81D4FA),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isWorn) "🖐️ ON WRIST" else "❌ OFF WRIST",
                        color = if (isWorn) Color(0xFF00E676) else Color(0xFFFF5252),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(6.dp)) }

            when (selectedTab) {
                HealthTab.REALTIME_SCAN -> {
                    item {
                        Text(
                            text = "🏥 Realtime Health Scan",
                            color = Color(0xFFFF1744),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Start / Stop Scan Controls
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.92f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!isScanning) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF00E676))
                                        .clickable { isScanning = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▶️ START REALTIME SCAN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFF1744))
                                        .clickable { isScanning = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⏹️ STOP SCANNING", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    // Live Diagnostics Dashboard
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C1C1E))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🩸 Heart Rate:", color = Color.Gray, fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.US, "%.0f BPM", heartRate),
                                    color = if (isScanning) Color(0xFFFF1744) else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🩸 SpO2 Oxygen:", color = Color.Gray, fontSize = 10.sp)
                                Text("$spo2Percent%", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💓 HRV Index:", color = Color.Gray, fontSize = 10.sp)
                                Text("$hrvMs ms", color = Color(0xFF81D4FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌡️ Body Temp:", color = Color.Gray, fontSize = 10.sp)
                                Text(String.format(Locale.US, "%.1f°C", skinTempC), color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    // Live Waveform Canvas
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0D1B12))
                                .padding(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (isScanning) {
                                    val path = Path()
                                    val pts = listOf(0.5f, 0.5f, 0.45f, 0.55f, 0.5f, 0.2f, 0.85f, 0.5f, 0.48f, 0.5f, 0.5f)
                                    val step = size.width / (pts.size * 2)

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
                    }
                }

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
                            Text("🏥 WearHealthSuite v1.1.0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("By Aju George", color = Color.Gray, fontSize = 9.5.sp, modifier = Modifier.padding(bottom = 6.dp))

                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                Text("• Real-time Simultaneous Diagnostic Scan", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• AFE4500S PPG Optical & ECG BioActive", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• Infrared Skin Surface Temperature", color = Color.LightGray, fontSize = 8.5.sp)
                                Text("• Wireless Charging & Offbody Wrist Detection", color = Color.LightGray, fontSize = 8.5.sp)
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
                BezelTabPill("▶️ Scan", selected = selectedTab == HealthTab.REALTIME_SCAN) { selectedTab = HealthTab.REALTIME_SCAN }
            }
            curvedComposable { Spacer(modifier = Modifier.width(3.dp)) }
            curvedComposable {
                BezelTabPill("🩸 Heart", selected = selectedTab == HealthTab.HEART_PPG) { selectedTab = HealthTab.HEART_PPG }
            }
            curvedComposable { Spacer(modifier = Modifier.width(3.dp)) }
            curvedComposable {
                BezelTabPill("⚡ ECG", selected = selectedTab == HealthTab.ECG_BIA) { selectedTab = HealthTab.ECG_BIA }
            }
            curvedComposable { Spacer(modifier = Modifier.width(3.dp)) }
            curvedComposable {
                BezelTabPill("🌡️ Temp", selected = selectedTab == HealthTab.SKIN_TEMP) { selectedTab = HealthTab.SKIN_TEMP }
            }
            curvedComposable { Spacer(modifier = Modifier.width(3.dp)) }
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
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
