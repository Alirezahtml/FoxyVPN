package com.vauth.foxyvpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.LogEntry
import com.vauth.foxyvpn.data.LogLevel
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

@Composable
fun LogsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries by AppLogger.entries.collectAsState()
    var filter by remember { mutableStateOf(LogFilter.ALL) }

    val visible: List<LogEntry> = remember(entries, filter) {
        if (filter == LogFilter.ALL) entries else entries.filter { it.level in filter.levels }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFF6B6B7F), CircleShape),
                )
                TopAppBar(
                    title = {
                        Text(
                            if (visible.size == entries.size) "Logs (${entries.size})"
                            else "Logs (${visible.size}/${entries.size})",
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (entries.isEmpty()) {
                                Toast.makeText(context, "No log entries yet", Toast.LENGTH_SHORT).show()
                                return@IconButton
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
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Save/share logs as a file")
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FoxyVPN logs", AppLogger.exportAsText()))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy logs")
                        }
                        IconButton(onClick = { AppLogger.clear() }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear logs")
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LogFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                    )
                }
            }

            if (visible.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (entries.isEmpty()) "No log entries recorded in this session"
                        else "No logs match the selected filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                ) {
                    items(visible) { entry ->
                        val color = when (entry.level) {
                            LogLevel.ERROR -> MaterialTheme.colorScheme.error
                            LogLevel.WARN -> Color(0xFFFFB74D)
                            LogLevel.INFO -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(
                            text = "[${entry.tag}] ${entry.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
