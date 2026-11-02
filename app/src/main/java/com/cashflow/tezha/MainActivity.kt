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
import androidx.compose.ui.text.style.TextAlign
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
        primary = Color(0xFF6BE0AE),
        background = Color(0xFF0E171B),
        surface = Color(0xFF192529),
        onSurface = Color(0xFFF3F7F5)
    ) else lightColorScheme(
        primary = Color(0xFF087D5B),
        background = Color(0xFFF4F7F5),
        surface = Color.White,
        onSurface = Color(0xFF17211E)
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun CashFlowApp(
    prefs: android.content.SharedPreferences,
    themeMode: String,
    onThemeChange: (String) -> Unit
) {
    val transactions = remember {
        mutableStateListOf<Tx>().apply {
            val arr = runCatching { JSONArray(prefs.getString("transactions", "[]")) }.getOrNull()
            if (arr != null) for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    Tx(
                        o.optLong("id"),
                        o.optString("title"),
                        o.optDouble("amount"),
                        o.optBoolean("income"),
                        o.optString("category"),
                        o.optLong("date"),
                        o.optString("note")
                    )
                )
            }
        }
    }
    var currency by remember { mutableStateOf(prefs.getString("currency", "JPY") ?: "JPY") }
    var filter by remember { mutableStateOf("Semua") }
    var showAdd by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    fun persist() {
        val a = JSONArray()
        transactions.forEach { t ->
            a.put(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("amount", t.amount)
                    .put("income", t.income)
                    .put("category", t.category)
                    .put("date", t.date)
                    .put("note", t.note)
            )
        }
        prefs.edit().putString("transactions", a.toString()).apply()
    }

    val income = transactions.filter { it.income }.sumOf { it.amount }
    val expense = transactions.filter { !it.income }.sumOf { it.amount }
    val filtered = transactions.filter {
        filter == "Semua" || (filter == "Masuk" && it.income) || (filter == "Keluar" &&
