package com.cashflow.tezha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class Tx(
    val id: Long,
    val title: String,
    val amount: Double,
    val income: Boolean,
    val category: String,
    val date: Long,
    val note: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("cashflow", MODE_PRIVATE)
        setContent {
            var dark by remember { mutableStateOf(prefs.getString("theme", "system") ?: "system") }
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDark = when (dark) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            CashFlowTheme(useDark) {
                CashFlowApp(
                    prefs = prefs,
                    themeMode = dark,
                    onThemeChange = {
                        dark = it
                        prefs.edit().putString("theme", it).apply()
                    }
                )
            }
        }
    }
}

@Composable
fun CashFlowTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark) darkColorScheme(
        primary = Color(0xFF6
