package com.ajimsjames.wearhealthsuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ajimsjames.wearhealthsuite.ui.HealthSuiteScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthSuiteScreen()
        }
    }
}
