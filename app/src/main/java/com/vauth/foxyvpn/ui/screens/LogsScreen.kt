package com.vauth.foxyvpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.LogEntry
import com.vauth.foxyvpn.data.LogLevel
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LogFilter(val label: String, val levels: Set<LogLevel>) {
    ALL("All", setOf(LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR)),
    INFO("Info", setOf(LogLevel.INFO)),
    PROBLEMS("Problems", setOf(LogLevel.WARN, LogLevel.ERROR)),
}

private suspend fun writeLogFile(context: Context): File = withContext(Dispatchers.IO) {
    val dir = File(context.cacheDir, "logs").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val file = File(dir, "foxyvpn-logs-$stamp.txt")
    file.writeText(AppLogger.exportAsText())
    file
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(onBack: () -> Unit) {
    val entries by AppLogger.entries.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    var filter by remember { mutableStateOf(LogFilter.ALL) }

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current

    val countsByFilter: Map<LogFilter, Int> = remember(entries) {
        LogFilter.entries.associateWith { option -> entries.count { it.level in option.levels } }
    }
    val visible: List<LogEntry> = remember(entries, filter) {
        if (filter == LogFilter.ALL) entries else entries.filter { it.level in filter.levels }
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (visible.size == entries.size) "Logs (${entries.size})"
                        else "Logs (${visible.size}/${entries.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = neu.textPrimary,
                    )
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 38.dp,
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                actions = {
                    NeumorphicIconButton(
                        size = 38.dp,
                        onClick = {
                            if (entries.isEmpty()) {
                                Toast.makeText(context, "No log entries yet", Toast.LENGTH_SHORT).show()
                                return@NeumorphicIconButton
                            }
                            scope.launch {
                                val file = writeLogFile(context)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Save or send FoxyVPN logs").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = neu.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    NeumorphicIconButton(
                        size = 38.dp,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FoxyVPN logs", AppLogger.exportAsText()))
                            Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = neu.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    NeumorphicIconButton(
                        size = 38.dp,
                        onClick = { AppLogger.clear() },
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Neumorphic filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (option in LogFilter.entries) {
                    val count = countsByFilter[option] ?: 0
                    val isSelected = filter == option
                    val pillShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(if (isSelected) neu.accent else neu.surfaceElevated)
                            .clickable { filter = option }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${option.label} ($count)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) Color.White else neu.textPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sunken Neumorphic Terminal Console
            val consoleShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp)
                    .neumorphicSunken(
                        shape = consoleShape,
                        cornerRadius = 16.dp,
                        depth = 4.dp,
                        surfaceColor = neu.surfaceElevated,
                    )
                    .border(
                        width = 1.dp,
                        color = if (neu.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
                        shape = consoleShape,
                    )
                    .padding(12.dp),
            ) {
                if (visible.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            when {
                                entries.isEmpty() -> "No log entries yet."
                                filter == LogFilter.PROBLEMS -> "No warnings or errors logged."
                                else -> "Nothing matches this filter."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = neu.textSecondary,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(visible.asReversed(), key = { it.id }) { entry ->
                            val levelColor = when (entry.level) {
                                LogLevel.ERROR -> MaterialTheme.colorScheme.error
                                LogLevel.WARN -> statusColors.connecting
                                LogLevel.INFO -> neu.textSecondary
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = timeFormat.format(Date(entry.timestampMillis)),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = neu.textSecondary.copy(alpha = 0.7f),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = entry.level.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = levelColor,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "[${entry.tag}]",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = neu.textSecondary,
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = entry.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp,
                                    ),
                                    color = neu.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
