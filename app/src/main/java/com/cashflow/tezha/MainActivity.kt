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
        filter == "Semua" || (filter == "Masuk" && it.income) || (filter == "Keluar" && !it.income)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Tambah transaksi", fontWeight = FontWeight.Bold) }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(Color(0x2210B981), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Ikon Uang",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "C A S H F L O W",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Keuanganmu, lebih teratur.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu Opsi")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pengaturan") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = {
                                showMenu = false
                                showSettings = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tentang") },
                            leadingIcon = { Icon(Icons.Default.Info, null) },
                            onClick = {
                                showMenu = false
                                showAbout = true
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF17634E))
            ) {
                Column(Modifier.fillMaxWidth().padding(21.dp)) {
                    Text(
                        "TOTAL SALDO",
                        color = Color.White.copy(alpha = .75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(13.dp))
                    Text(
                        money(income - expense, currency),
                        color = Color.White,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ringkasan seluruh transaksi  •  $currency",
                        color = Color.White.copy(alpha = .8f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Summary("Pemasukan", income, currency, true, Modifier.weight(1f))
                Summary("Pengeluaran", expense, currency, false, Modifier.weight(1f))
            }
            Spacer(Modifier.height(23.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Transaksi",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${transactions.size} catatan",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f),
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Masuk", "Keluar").forEach { f ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) })
                }
            }
            Spacer(Modifier.height(8.dp))
            if (filtered.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada transaksi", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tekan tombol tambah untuk mencatat uang masuk atau keluar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)
                        )
                    }
                }
            } else {
                filtered.forEach { tx ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Row(
                            Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(42.dp)
                                    .background(
                                        if (tx.income) Color(0x226BE0AE) else Color(0x22FF9A78),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (tx.income) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    null,
                                    tint = if (tx.income) Color(0xFF31B987) else Color(0xFFE58A68)
                                )
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tx.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(
                                    "${tx.category} · ${
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                                            Date(tx.date)
                                        )
                                    }",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f)
                                )
                            }
                            Text(
                                "${if (tx.income) "+" else "−"}${money(tx.amount, currency)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.income) Color(0xFF31B987) else Color(0xFFE58A68)
                            )
                            IconButton(onClick = { transactions.remove(tx); persist() }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    "Hapus",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(90.dp))
        }
    }

    if (showAdd) AddTransactionDialog(currency, onDismiss = { showAdd = false }) { tx ->
        transactions.add(0, tx)
        persist()
        showAdd = false
    }

    if (showSettings) AlertDialog(
        onDismissRequest = { showSettings = false },
        title = { Text("Pengaturan tampilan & mata uang") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tema aplikasi", fontWeight = FontWeight.SemiBold)
                listOf(
                    "system" to "Ikuti sistem",
                    "light" to "Terang",
                    "dark" to "Gelap"
                ).forEach { (v, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = themeMode == v, onClick = { onThemeChange(v) })
                        Text(label, modifier = Modifier.clickable { onThemeChange(v) })
                    }
                }
                Text("Mata uang", fontWeight = FontWeight.SemiBold)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(currency)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(
                            "JPY", "IDR", "USD", "EUR", "GBP", "AUD", "SGD",
                            "MYR", "KRW", "CNY", "THB", "PHP", "INR", "CAD", "CHF"
                        ).forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    currency = c
                                    prefs.edit().putString("currency", c).apply()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Perubahan mata uang hanya mengubah format tampilan, bukan mengonversi nilai transaksi.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { showSettings = false }) { Text("Selesai") }
        }
    )

    if (showAbout) AlertDialog(
        onDismissRequest = { showAbout = false },
        icon = {
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color(0x2210B981), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text("Tentang aplikasi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "CashFlow · Personal Finance Manager",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Aplikasi pencatatan keuangan pribadi yang simpel dan modern untuk memantau pemasukan, pengeluaran, serta menjaga arus kas harian Anda tetap teratur.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    "Dibuat oleh Tesa Adi Wijayanto",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Versi 1.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { showAbout = false }) { Text("Tutup") }
        }
    )
}

@Composable
fun Summary(
    title: String,
    amount: Double,
    currency: String,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(19.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                money(amount, currency),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (positive) Color(0xFF31B987) else Color(0xFFE58A68),
                maxLines = 1
            )
        }
    }
}

fun money(value: Double, currency: String): String = runCatching {
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        this.currency = Currency.getInstance(currency)
    }.format(value)
}.getOrDefault("$currency ${"%.2f".format(value)}")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(currency: String, onDismiss: () -> Unit, onSave: (Tx) -> Unit) {
    var income by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var note by remember { mutableStateOf("") }
    val categories = if (income) listOf("Gaji", "Bonus", "Hadiah", "Lainnya") else listOf(
        "Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Lainnya"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaksi baru") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = income,
                        onClick = { income = true; category = "Gaji" },
                        label = { Text("Pemasukan") }
                    )
                    FilterChip(
                        selected = !income,
                        onClick = { income = false; category = "Makanan" },
                        label = { Text("Pengeluaran") }
                    )
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Nominal ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama transaksi") },
                    singleLine = true
                )
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { category = c; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (opsional)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && (amount.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0,
                onClick = {
                    onSave(
                        Tx(
                            System.currentTimeMillis(),
                            title.trim(),
                            amount.replace(",", ".").toDouble(),
                            income,
                            category,
                            System.currentTimeMillis(),
                            note
                        )
                    )
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
