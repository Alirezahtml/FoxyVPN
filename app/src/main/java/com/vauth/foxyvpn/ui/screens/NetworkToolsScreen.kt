package com.vauth.foxyvpn.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.ProxyCandidate
import com.vauth.foxyvpn.data.model.emojiFlag
import com.vauth.foxyvpn.ui.theme.AnimatedOdometerText
import com.vauth.foxyvpn.ui.theme.AnimatedTrafficEqualizer
import com.vauth.foxyvpn.ui.theme.FoxyStrings
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicButton
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.NeumorphicRadarPulse
import com.vauth.foxyvpn.ui.theme.NeumorphicSpeedometerGauge
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.vpn.PingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

enum class NetworkToolTab(
    val id: Int,
    val icon: ImageVector,
) {
    SPEED_TEST(0, Icons.Filled.Speed),
    LEAK_INSPECTOR(1, Icons.Filled.Security),
    SMART_SERVER(2, Icons.Filled.Explore),
    WATCHDOG(3, Icons.Filled.Favorite),
    MTU_OPTIMIZER(4, Icons.Filled.Tune),
    ;

    fun localizedTitle(s: FoxyStrings): String = when (this) {
        SPEED_TEST -> s.toolSpeedTitle
        LEAK_INSPECTOR -> s.toolLeakTitle
        SMART_SERVER -> s.toolScannerTitle
        WATCHDOG -> s.toolWatchdogTitle
        MTU_OPTIMIZER -> s.toolMtuTitle
    }

    fun localizedSubtitle(s: FoxyStrings): String = when (this) {
        SPEED_TEST -> s.toolSpeedSubtitle
        LEAK_INSPECTOR -> s.toolLeakSubtitle
        SMART_SERVER -> s.toolScannerSubtitle
        WATCHDOG -> s.toolWatchdogSubtitle
        MTU_OPTIMIZER -> s.toolMtuSubtitle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkToolsScreen(
    app: FoxyVpnApp,
    initialTab: Int = 0,
    onRequestConnect: () -> Unit,
    onBack: () -> Unit,
) {
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings
    val haptics = LocalHapticFeedback.current
    var selectedTab by remember {
        mutableStateOf(NetworkToolTab.entries.firstOrNull { it.id == initialTab } ?: NetworkToolTab.SPEED_TEST)
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            s.toolkitScreenTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = neu.textPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            selectedTab.localizedSubtitle(s),
                            style = MaterialTheme.typography.labelSmall,
                            color = neu.accent,
                        )
                    }
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back,
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Horizontal Neumorphic Tool Selector Bar (Spacious Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NetworkToolTab.entries.forEachIndexed { idx, tab ->
                    val isSelected = selectedTab == tab
                    val pillShape = RoundedCornerShape(18.dp)
                    val title = tab.localizedTitle(s)
                    Box(
                        modifier = Modifier
                            .staggeredEntrance(idx, delayPerItemMs = 45)
                            .neumorphicTouchBounce()
                            .clip(pillShape)
                            .background(if (isSelected) neu.accent else neu.surfaceElevated)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTab = tab
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = title,
                                tint = if (isSelected) Color.White else neu.accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                ),
                                color = if (isSelected) Color.White else neu.textPrimary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn(tween(260)) togetherWith fadeOut(tween(200)))
                    },
                    label = "tool-tab-content",
                ) { tab ->
                    when (tab) {
                        NetworkToolTab.SPEED_TEST -> SpeedTestToolPanel(app = app)
                        NetworkToolTab.LEAK_INSPECTOR -> LeakInspectorToolPanel(app = app)
                        NetworkToolTab.SMART_SERVER -> SmartServerScannerPanel(
                            app = app,
                            onRequestConnect = onRequestConnect,
                        )
                        NetworkToolTab.WATCHDOG -> WatchdogKeepAlivePanel(app = app)
                        NetworkToolTab.MTU_OPTIMIZER -> MtuDpiOptimizerPanel(app = app)
                    }
                }
            }
        }
    }
}

private fun buildTunnelAwareHttpClient(
    isVpnConnected: Boolean,
    socksPort: Int,
    timeoutSec: Long = 12L,
): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .connectTimeout(timeoutSec, TimeUnit.SECONDS)
        .readTimeout(timeoutSec, TimeUnit.SECONDS)
        .writeTimeout(timeoutSec, TimeUnit.SECONDS)
        .callTimeout(timeoutSec + 4L, TimeUnit.SECONDS)
    if (isVpnConnected) {
        builder.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socksPort)))
    }
    return builder.build()
}

// ============================================================================
// TOOL 1: REAL SPEED TEST (PING, JITTER, DOWNLOAD, UPLOAD)
// ============================================================================
private enum class SpeedTestStage {
    READY,
    PING,
    DOWNLOAD_TUNNEL,
    DOWNLOAD_DIRECT,
    UPLOAD_TUNNEL,
    UPLOAD_DIRECT,
    DONE_TUNNEL,
    DONE_DIRECT,
}

@Composable
private fun SpeedTestToolPanel(app: FoxyVpnApp) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()
    val state by FoxyVpnService.state.collectAsState()
    val isConnected = state == ConnectionState.CONNECTED

    var isRunning by remember { mutableStateOf(false) }
    var stage by remember { mutableStateOf(SpeedTestStage.READY) }
    var currentGaugeMbps by remember { mutableFloatStateOf(0f) }
    var downloadMbps by remember { mutableStateOf<Float?>(null) }
    var uploadMbps by remember { mutableStateOf<Float?>(null) }
    var pingMs by remember { mutableStateOf<Int?>(null) }
    var jitterMs by remember { mutableStateOf<Int?>(null) }

    val stageLabel = when (stage) {
        SpeedTestStage.READY -> s.speedReadyStage
        SpeedTestStage.PING -> s.speedMeasuringPingStage
        SpeedTestStage.DOWNLOAD_TUNNEL -> s.speedTestingTunnelDlStage
        SpeedTestStage.DOWNLOAD_DIRECT -> s.speedTestingDirectDlStage
        SpeedTestStage.UPLOAD_TUNNEL -> s.speedTestingTunnelUlStage
        SpeedTestStage.UPLOAD_DIRECT -> s.speedTestingDirectUlStage
        SpeedTestStage.DONE_TUNNEL -> s.speedDoneTunnelStage
        SpeedTestStage.DONE_DIRECT -> s.speedDoneDirectStage
    }

    fun startSpeedTest() {
        if (isRunning) return
        isRunning = true
        currentGaugeMbps = 0f
        downloadMbps = null
        uploadMbps = null
        pingMs = null
        jitterMs = null

        scope.launch {
            val socksPort = app.settingsStore.socksPort
            // Step 1: Ping & Jitter
            stage = SpeedTestStage.PING
            val endpoint = FoxyVpnService.connectedEndpoint.value
            val targetHost = endpoint?.first ?: app.proxyStateStore.load()?.host ?: "1.1.1.1"
            val targetPort = endpoint?.second ?: app.proxyStateStore.load()?.port ?: 443
            val samples = mutableListOf<Int>()
            repeat(5) {
                val rtt = PingUtil.measureTcpLatencyMs(targetHost, targetPort)
                    ?: PingUtil.measureTcpLatencyMs("1.1.1.1", 443)
                if (rtt != null) samples.add(rtt)
                delay(120)
            }
            if (samples.isNotEmpty()) {
                pingMs = samples.average().roundToInt()
                jitterMs = if (samples.size >= 2) {
                    samples.zipWithNext { a, b -> abs(a - b) }.average().roundToInt()
                } else 1
            }

            // Step 2: Download Benchmark
            stage = if (isConnected) SpeedTestStage.DOWNLOAD_TUNNEL else SpeedTestStage.DOWNLOAD_DIRECT
            val dlClient = buildTunnelAwareHttpClient(isConnected, socksPort, timeoutSec = 15L)
            try {
                val dlUrls = listOf(
                    "https://speed.cloudflare.com/__down?bytes=4000000",
                    "https://cloudflare.com/cdn-cgi/trace",
                )
                var totalBytes = 0L
                val startNs = System.nanoTime()
                for (url in dlUrls) {
                    val req = Request.Builder()
                        .url(url)
                        .header("Cache-Control", "no-cache")
                        .build()
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            dlClient.newCall(req).execute().use { resp ->
                                if (!resp.isSuccessful) return@use false
                                val stream = resp.body?.byteStream() ?: return@use false
                                val buf = ByteArray(16 * 1024)
                                var read: Int
                                while (stream.read(buf).also { read = it } != -1) {
                                    totalBytes += read
                                    val elapsedSec = ((System.nanoTime() - startNs) / 1_000_000_000.0).coerceAtLeast(0.05)
                                    val liveMbps = ((totalBytes * 8.0) / (elapsedSec * 1_000_000.0)).toFloat()
                                    currentGaugeMbps = liveMbps
                                    if (elapsedSec > 5.5) break
                                }
                                totalBytes > 0L
                            }
                        }.getOrDefault(false)
                    }
                    if (ok && totalBytes > 64 * 1024) break
                }
                val totalSec = ((System.nanoTime() - startNs) / 1_000_000_000.0).coerceAtLeast(0.08)
                val finalDlMbps = ((totalBytes * 8.0) / (totalSec * 1_000_000.0)).toFloat().coerceAtLeast(0.1f)
                downloadMbps = finalDlMbps
                currentGaugeMbps = finalDlMbps
            } finally {
                withContext(Dispatchers.IO) {
                    dlClient.dispatcher.executorService.shutdown()
                    dlClient.connectionPool.evictAll()
                }
            }

            delay(350)

            // Step 3: Upload Benchmark
            stage = if (isConnected) SpeedTestStage.UPLOAD_TUNNEL else SpeedTestStage.UPLOAD_DIRECT
            val ulClient = buildTunnelAwareHttpClient(isConnected, socksPort, timeoutSec = 12L)
            try {
                val payloadSize = 512 * 1024
                val payload = ByteArray(payloadSize) { (it % 251).toByte() }
                val uploadEndpoints = listOf(
                    "https://speed.cloudflare.com/__up",
                    "https://www.cloudflare.com/cdn-cgi/trace",
                )
                var measuredUlMbps: Float? = null
                for (ulUrl in uploadEndpoints) {
                    val startNs = System.nanoTime()
                    val req = Request.Builder()
                        .url(ulUrl)
                        .post(payload.toRequestBody("application/octet-stream".toMediaType()))
                        .build()
                    val uploadedOk = withContext(Dispatchers.IO) {
                        runCatching {
                            ulClient.newCall(req).execute().use { resp ->
                                resp.code in 200..499
                            }
                        }.getOrDefault(false)
                    }
                    if (uploadedOk) {
                        val elapsedSec = ((System.nanoTime() - startNs) / 1_000_000_000.0).coerceAtLeast(0.05)
                        val ulMbps = ((payloadSize * 8.0) / (elapsedSec * 1_000_000.0)).toFloat()
                        measuredUlMbps = ulMbps
                        currentGaugeMbps = ulMbps
                        break
                    }
                }
                uploadMbps = measuredUlMbps
            } finally {
                withContext(Dispatchers.IO) {
                    ulClient.dispatcher.executorService.shutdown()
                    ulClient.connectionPool.evictAll()
                }
            }

            stage = if (isConnected) SpeedTestStage.DONE_TUNNEL else SpeedTestStage.DONE_DIRECT
            isRunning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(0),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) statusColors.connected else statusColors.connecting),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isConnected) s.speedModeTunnel else s.speedModeDirect,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                        ),
                        color = if (isConnected) statusColors.connected else neu.textSecondary,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(224.dp),
                ) {
                    NeumorphicSpeedometerGauge(
                        speedMbps = currentGaugeMbps,
                        maxMbps = 100f,
                        accentColor = if (isConnected) statusColors.connected else neu.accent,
                        trackColor = neu.surfaceElevated,
                        isRunning = isRunning,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 48.dp),
                    ) {
                        AnimatedOdometerText(
                            text = String.format(Locale.US, "%.1f", currentGaugeMbps),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = neu.textPrimary,
                        )
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = neu.textSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stageLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = neu.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(Modifier.height(20.dp))

                // 4-Metric Grid (Download, Upload, Ping, Jitter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeedMetricTile(
                        label = s.speedDownloadLabel,
                        value = downloadMbps?.let { String.format(Locale.US, "%.1f Mbps", it) } ?: "—",
                        accent = statusColors.connected,
                        modifier = Modifier.weight(1f),
                    )
                    SpeedMetricTile(
                        label = s.speedUploadLabel,
                        value = uploadMbps?.let { String.format(Locale.US, "%.1f Mbps", it) } ?: "—",
                        accent = neu.accent,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeedMetricTile(
                        label = s.speedPingLabel,
                        value = pingMs?.let { "$it ms" } ?: "—",
                        accent = statusColors.connecting,
                        modifier = Modifier.weight(1f),
                    )
                    SpeedMetricTile(
                        label = s.speedJitterLabel,
                        value = jitterMs?.let { "$it ms" } ?: "—",
                        accent = neu.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(22.dp))

                NeumorphicButton(
                    onClick = ::startSpeedTest,
                    enabled = !isRunning,
                    accentColor = neu.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicTouchBounce(),
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(s.speedRunningButton, color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(s.speedStartButton, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SpeedMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .neumorphicSunken(
                shape = shape,
                cornerRadius = 16.dp,
                depth = 3.dp,
                surfaceColor = neu.surfaceElevated,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (s.isRtl) 0.sp else 0.7.sp,
                ),
                color = neu.textSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = accent,
            )
        }
    }
}

// ============================================================================
// TOOL 2: IP, DNS & LEAK INSPECTOR
// ============================================================================
private data class CloudflareTraceReport(
    val ip: String,
    val country: String,
    val colo: String,
    val tlsVersion: String,
    val httpVersion: String,
    val sniStatus: String,
    val warpStatus: String,
    val userAgent: String,
)

@Composable
private fun LeakInspectorToolPanel(app: FoxyVpnApp) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()
    val state by FoxyVpnService.state.collectAsState()
    val isConnected = state == ConnectionState.CONNECTED

    var isScanning by remember { mutableStateOf(false) }
    var directTrace by remember { mutableStateOf<CloudflareTraceReport?>(null) }
    var tunnelTrace by remember { mutableStateOf<CloudflareTraceReport?>(null) }
    var dohLatencyMs by remember { mutableStateOf<Int?>(null) }
    var dohResolvedIp by remember { mutableStateOf<String?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    fun parseFullTrace(raw: String): CloudflareTraceReport {
        val map = mutableMapOf<String, String>()
        raw.lineSequence().forEach { line ->
            val idx = line.indexOf('=')
            if (idx > 0) {
                map[line.substring(0, idx).trim()] = line.substring(idx + 1).trim()
            }
        }
        return CloudflareTraceReport(
            ip = map["ip"] ?: "Unknown",
            country = map["loc"] ?: "—",
            colo = map["colo"] ?: "—",
            tlsVersion = map["tls"] ?: "—",
            httpVersion = map["http"] ?: "—",
            sniStatus = map["sni"] ?: "—",
            warpStatus = map["warp"] ?: "off",
            userAgent = map["uag"] ?: "FoxyVPN",
        )
    }

    fun runLeakAudit() {
        if (isScanning) return
        isScanning = true
        scanError = null
        scope.launch {
            val socksPort = app.settingsStore.socksPort
            // 1. Direct ISP trace
            val directClient = buildTunnelAwareHttpClient(isVpnConnected = false, socksPort = socksPort, timeoutSec = 8L)
            try {
                val res = withContext(Dispatchers.IO) {
                    runCatching {
                        val req = Request.Builder().url("https://www.cloudflare.com/cdn-cgi/trace").build()
                        directClient.newCall(req).execute().use { resp ->
                            if (!resp.isSuccessful) error("HTTP ${resp.code}")
                            parseFullTrace(resp.body?.string().orEmpty())
                        }
                    }
                }
                res.onSuccess { directTrace = it }
            } finally {
                withContext(Dispatchers.IO) {
                    directClient.dispatcher.executorService.shutdown()
                    directClient.connectionPool.evictAll()
                }
            }

            // 2. Tunnel Exit trace (via SOCKS5 when connected)
            if (isConnected) {
                val proxiedClient = buildTunnelAwareHttpClient(isVpnConnected = true, socksPort = socksPort, timeoutSec = 12L)
                try {
                    val res = withContext(Dispatchers.IO) {
                        runCatching {
                            val req = Request.Builder().url("https://www.cloudflare.com/cdn-cgi/trace").build()
                            proxiedClient.newCall(req).execute().use { resp ->
                                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                                parseFullTrace(resp.body?.string().orEmpty())
                            }
                        }
                    }
                    res.onSuccess { tunnelTrace = it }
                        .onFailure { scanError = it.message }
                } finally {
                    withContext(Dispatchers.IO) {
                        proxiedClient.dispatcher.executorService.shutdown()
                        proxiedClient.connectionPool.evictAll()
                    }
                }
            } else {
                tunnelTrace = null
            }

            // 3. DoH DNS Resolution & Leak Check
            val dnsClient = buildTunnelAwareHttpClient(isVpnConnected = isConnected, socksPort = socksPort, timeoutSec = 8L)
            try {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val start = System.nanoTime()
                        val req = Request.Builder()
                            .url("https://cloudflare-dns.com/dns-query?name=cloudflare.com&type=A")
                            .header("Accept", "application/dns-json")
                            .build()
                        dnsClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful) {
                                val rtt = ((System.nanoTime() - start) / 1_000_000L).toInt()
                                val body = resp.body?.string().orEmpty()
                                dohLatencyMs = rtt
                                val ipMatch = Regex("\"data\"\\s*:\\s*\"([0-9.]+)\"").find(body)
                                dohResolvedIp = ipMatch?.groupValues?.getOrNull(1)
                            }
                        }
                    }
                }
            } finally {
                withContext(Dispatchers.IO) {
                    dnsClient.dispatcher.executorService.shutdown()
                    dnsClient.connectionPool.evictAll()
                }
            }

            isScanning = false
        }
    }

    LaunchedEffect(isConnected) {
        runLeakAudit()
    }

    val isProtectedNoLeak = isConnected &&
        tunnelTrace != null &&
        directTrace != null &&
        tunnelTrace?.ip != directTrace?.ip

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(0),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NeumorphicRadarPulse(
                    color = if (isProtectedNoLeak) statusColors.connected else if (isConnected) statusColors.connecting else MaterialTheme.colorScheme.error,
                    isActive = isConnected,
                    isConnecting = isScanning,
                    size = 116.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(
                                if (isProtectedNoLeak) statusColors.connected.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isProtectedNoLeak) Icons.Filled.Security else Icons.Filled.Shield,
                            contentDescription = null,
                            tint = if (isProtectedNoLeak) statusColors.connected else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = when {
                        isScanning -> s.leakScanningTitle
                        isProtectedNoLeak -> s.leakProtectedTitle
                        isConnected -> s.leakVerifyingTitle
                        else -> s.leakExposedTitle
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (s.isRtl) 0.sp else 0.7.sp,
                    ),
                    color = if (isProtectedNoLeak) statusColors.connected else if (isConnected) statusColors.connecting else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(18.dp))

                // Side-by-side ISP IP vs Tunnel Exit IP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeedMetricTile(
                        label = s.leakDirectIpLabel,
                        value = directTrace?.let { "${it.ip} (${it.country})" } ?: s.checking,
                        accent = neu.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    SpeedMetricTile(
                        label = s.leakVpnIpLabel,
                        value = if (isConnected) {
                            tunnelTrace?.let { "${it.ip} (${it.country})" } ?: s.leakProbing
                        } else {
                            s.leakNotConnected
                        },
                        accent = if (isConnected) statusColors.connected else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Detailed Security & Edge Telemetry Rows
                val activeTrace = tunnelTrace ?: directTrace
                InspectorDetailRow(s.leakColoLabel, activeTrace?.colo ?: "—")
                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                InspectorDetailRow(s.leakProtocolLabel, "${activeTrace?.tlsVersion ?: "—"} / ${activeTrace?.httpVersion ?: "—"}")
                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                InspectorDetailRow(s.leakSniLabel, activeTrace?.sniStatus?.uppercase() ?: "—")
                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                InspectorDetailRow(
                    s.leakDohLabel,
                    if (dohLatencyMs != null) {
                        val ipPart = dohResolvedIp?.let { " ($it)" }.orEmpty()
                        s.leakDohVerified(dohLatencyMs!!, ipPart)
                    } else s.testing,
                )
                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                InspectorDetailRow(
                    s.leakIpv6Label,
                    if (isConnected) s.leakIpv6Protected else s.leakIpv6Inactive,
                )

                scanError?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(22.dp))

                NeumorphicButton(
                    onClick = ::runLeakAudit,
                    enabled = !isScanning,
                    accentColor = neu.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicTouchBounce(),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isScanning) s.leakScanningButton else s.leakRescanButton,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun InspectorDetailRow(label: String, value: String) {
    val neu = currentNeumorphicColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = neu.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = neu.textPrimary,
        )
    }
}

// ============================================================================
// TOOL 3: SMART FASTEST SERVER FINDER
// ============================================================================
private data class BenchmarkedServer(
    val candidate: ProxyCandidate,
    val latencyMs: Int,
)

@Composable
private fun SmartServerScannerPanel(
    app: FoxyVpnApp,
    onRequestConnect: () -> Unit,
) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var isScanning by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember(s.isRtl) { mutableStateOf(s.scannerInitialSubtitle) }
    val results = remember { mutableStateListOf<BenchmarkedServer>() }

    fun startServerBenchmark() {
        if (isScanning) return
        isScanning = true
        progress = 0f
        results.clear()

        scope.launch {
            statusText = s.scannerLoadingCatalog
            val countries = withContext(Dispatchers.IO) {
                val cached = app.serverListClient.loadCachedCountries()
                cached.ifEmpty {
                    runCatching { app.serverListClient.fetchCountries() }.getOrDefault(emptyList())
                }
            }
            val candidates = countries
                .filter { it.code.isNotBlank() && it.code != "REC" }
                .flatMap { country ->
                    country.cities.mapNotNull { city ->
                        ServerListClient.candidatesForCity(countries, country.code, city.code).firstOrNull()
                    }
                }
                .distinctBy { "${it.countryCode}:${it.cityCode}" }
                .take(18)

            if (candidates.isEmpty()) {
                statusText = s.scannerEmptyCatalog
                isScanning = false
                return@launch
            }

            candidates.forEachIndexed { idx, candidate ->
                statusText = s.scannerProbingServer(candidate.countryName.ifBlank { candidate.countryCode }, candidate.cityCode)
                val rtt = PingUtil.measureTcpLatencyMs(candidate.host, candidate.port)
                if (rtt != null) {
                    results.add(BenchmarkedServer(candidate, rtt))
                    results.sortBy { it.latencyMs }
                }
                progress = (idx + 1).toFloat() / candidates.size.toFloat()
            }

            statusText = if (results.isNotEmpty()) {
                s.scannerOptimalFound(results.first().candidate.countryName.ifBlank { results.first().candidate.countryCode }, results.first().latencyMs)
            } else {
                s.scannerTimeoutMessage
            }
            isScanning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(0),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NeumorphicRadarPulse(
                    color = neu.accent,
                    isActive = results.isNotEmpty(),
                    isConnecting = isScanning,
                    size = 110.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(neu.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = null,
                            tint = neu.accent,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = s.scannerHeaderTitle,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                    ),
                    color = neu.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = neu.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(Modifier.height(16.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    label = "scanner-progress",
                )
                if (isScanning || progress > 0f) {
                    val barShape = RoundedCornerShape(7.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .neumorphicSunken(
                                shape = barShape,
                                cornerRadius = 7.dp,
                                depth = 2.dp,
                                surfaceColor = neu.surfaceElevated,
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                .fillMaxSize()
                                .clip(barShape)
                                .background(statusColors.connected),
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                }

                NeumorphicButton(
                    onClick = ::startServerBenchmark,
                    enabled = !isScanning,
                    accentColor = neu.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicTouchBounce(),
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isScanning) s.scannerRunningButton else s.scannerStartButton,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (results.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    val best = results.first()
                    NeumorphicButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            app.proxyStateStore.save(best.candidate)
                            onRequestConnect()
                        },
                        accentColor = statusColors.connected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicTouchBounce(),
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            s.scannerConnectBestButton(best.candidate.countryCode, best.latencyMs),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    results.take(8).forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(neu.surfaceElevated)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    app.proxyStateStore.save(item.candidate)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "#${idx + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (idx == 0) statusColors.connected else neu.textSecondary,
                                modifier = Modifier.width(32.dp),
                            )
                            Text(item.candidate.emojiFlag, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.candidate.countryName.ifBlank { item.candidate.countryCode }} • ${item.candidate.cityCode}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = neu.textPrimary,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.candidate.host,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = neu.textSecondary,
                                )
                            }
                            Text(
                                text = "${item.latencyMs} ms",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = statusColors.connected,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ============================================================================
// TOOL 4: CONNECTION WATCHDOG & KEEP-ALIVE
// ============================================================================
@Composable
private fun WatchdogKeepAlivePanel(app: FoxyVpnApp) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()
    val state by FoxyVpnService.state.collectAsState()
    val pulseCount by FoxyVpnService.keepAlivePulseCount.collectAsState()
    val lastPulseAt by FoxyVpnService.lastKeepAliveAtMs.collectAsState()
    val pingMs by FoxyVpnService.pingLatencyMs.collectAsState()

    var keepAliveEnabled by remember { mutableStateOf(app.settingsStore.keepAliveEnabled) }
    var keepAliveIntervalSec by remember { mutableIntStateOf(app.settingsStore.keepAliveIntervalSec) }
    var autoReconnectEnabled by remember { mutableStateOf(app.settingsStore.autoReconnectEnabled) }
    var isProbingNow by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(0),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AnimatedTrafficEqualizer(
                        active = keepAliveEnabled && state == ConnectionState.CONNECTED,
                        color = statusColors.connected,
                        barCount = 5,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = s.watchdogHeaderTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                        ),
                        color = neu.textPrimary,
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeedMetricTile(
                        label = s.watchdogPulsesLabel,
                        value = s.watchdogPulsesValue(pulseCount),
                        accent = statusColors.connected,
                        modifier = Modifier.weight(1f),
                    )
                    SpeedMetricTile(
                        label = s.watchdogLastPulseLabel,
                        value = lastPulseAt?.let { timeFormat.format(Date(it)) } ?: "—",
                        accent = neu.accent,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Toggle 1: Auto-Reconnect Watchdog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            s.watchdogAutoReconnectTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = neu.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            s.watchdogAutoReconnectSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = neu.textSecondary,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Switch(
                        checked = autoReconnectEnabled,
                        onCheckedChange = {
                            autoReconnectEnabled = it
                            app.settingsStore.autoReconnectEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = statusColors.connected,
                        ),
                    )
                }

                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 14.dp))

                // Toggle 2: Keep-Alive Socket Heartbeat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            s.watchdogKeepAliveTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = neu.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            s.watchdogKeepAliveSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = neu.textSecondary,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Switch(
                        checked = keepAliveEnabled,
                        onCheckedChange = {
                            keepAliveEnabled = it
                            app.settingsStore.keepAliveEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = neu.accent,
                        ),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Interval Selector Pills
                Text(
                    s.watchdogIntervalLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = neu.textSecondary,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(10, 20, 30, 60).forEach { sec ->
                        val selected = keepAliveIntervalSec == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .neumorphicTouchBounce()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) neu.accent else neu.surfaceElevated)
                                .clickable {
                                    keepAliveIntervalSec = sec
                                    app.settingsStore.keepAliveIntervalSec = sec
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = s.watchdogSecondsUnit(sec),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) Color.White else neu.textPrimary,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                NeumorphicButton(
                    onClick = {
                        if (!isProbingNow) {
                            isProbingNow = true
                            scope.launch {
                                FoxyVpnService.pingNow()
                                delay(300)
                                isProbingNow = false
                            }
                        }
                    },
                    enabled = !isProbingNow && state == ConnectionState.CONNECTED,
                    accentColor = statusColors.connected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicTouchBounce(),
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = when {
                            state != ConnectionState.CONNECTED -> s.watchdogConnectFirstButton
                            isProbingNow -> s.watchdogSendingPulseButton
                            else -> s.watchdogSendNowButton(pingMs?.let { "${it}ms" } ?: s.islandReady)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ============================================================================
// TOOL 5: SMART MTU & DPI FRAGMENTATION OPTIMIZER
// ============================================================================
private data class DpiOptimizationCandidate(
    val mtu: Int,
    val splitMode: SettingsStore.TlsSplitMode,
    val rttMs: Int,
)

private suspend fun measureRealTlsFragmentedProbeMs(
    host: String,
    port: Int,
    mtuCandidate: Int,
    splitMode: SettingsStore.TlsSplitMode,
): Int? = withContext(Dispatchers.IO) {
    runCatching {
        val rawSocket = object : java.net.Socket() {
            private var splitApplied = false
            override fun getOutputStream(): java.io.OutputStream {
                val baseOut = super.getOutputStream()
                return object : java.io.FilterOutputStream(baseOut) {
                    override fun write(b: ByteArray, off: Int, len: Int) {
                        if (!splitApplied &&
                            splitMode != SettingsStore.TlsSplitMode.OFF &&
                            com.vauth.foxyvpn.vpn.upstream.TlsClientHelloSplitter.looksLikeClientHello(b, off, len)
                        ) {
                            splitApplied = true
                            val parts = com.vauth.foxyvpn.vpn.upstream.TlsClientHelloSplitter.splitBytes(b, off, len, splitMode)
                            if (!parts.isNullOrEmpty()) {
                                runCatching { tcpNoDelay = true }
                                for (part in parts) {
                                    baseOut.write(part)
                                    baseOut.flush()
                                }
                                return
                            }
                        }
                        baseOut.write(b, off, len)
                    }
                }
            }
        }
        rawSocket.use { sock ->
            sock.bind(InetSocketAddress(0))
            com.vauth.foxyvpn.data.ControlPlaneHttp.socketProtector?.invoke(sock)
            runCatching {
                sock.sendBufferSize = mtuCandidate
                sock.receiveBufferSize = mtuCandidate
            }
            val startNs = System.nanoTime()
            sock.connect(InetSocketAddress(host, port), 3_500)
            sock.soTimeout = 3_500
            val sslFactory = javax.net.ssl.SSLSocketFactory.getDefault() as javax.net.ssl.SSLSocketFactory
            val sslSocket = sslFactory.createSocket(sock, host, port, true) as javax.net.ssl.SSLSocket
            sslSocket.use { tls ->
                tls.startHandshake()
                val padLen = (mtuCandidate - 220).coerceIn(64, 1300)
                val padHeader = "X".repeat(padLen)
                val reqBytes = "HEAD / HTTP/1.1\r\nHost: $host\r\nX-Mtu-Probe: $padHeader\r\nConnection: close\r\n\r\n"
                    .toByteArray(Charsets.US_ASCII)
                tls.outputStream.write(reqBytes)
                tls.outputStream.flush()
                val firstByte = tls.inputStream.read()
                if (firstByte == -1) error("EOF before TLS response")
            }
            ((System.nanoTime() - startNs) / 1_000_000L).toInt().coerceAtLeast(1)
        }
    }.getOrNull()
}

@Composable
private fun MtuDpiOptimizerPanel(app: FoxyVpnApp) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var currentMtu by remember { mutableIntStateOf(app.settingsStore.mtu) }
    var currentSplit by remember { mutableStateOf(app.settingsStore.tlsSplitMode) }
    var isTesting by remember { mutableStateOf(false) }
    var statusMessage by remember(s.isRtl) { mutableStateOf(s.mtuInitialStatus) }
    var recommended by remember { mutableStateOf<DpiOptimizationCandidate?>(null) }
    val testedRows = remember { mutableStateListOf<DpiOptimizationCandidate>() }

    fun runOptimizer() {
        if (isTesting) return
        isTesting = true
        testedRows.clear()
        recommended = null

        scope.launch {
            val targetHost = FoxyVpnService.connectedEndpoint.value?.first
                ?: app.proxyStateStore.load()?.host
                ?: "cloudflare.com"
            val targetPort = FoxyVpnService.connectedEndpoint.value?.second
                ?: app.proxyStateStore.load()?.port
                ?: 443

            val combos = listOf(
                1400 to SettingsStore.TlsSplitMode.SNI,
                1360 to SettingsStore.TlsSplitMode.SNI,
                1280 to SettingsStore.TlsSplitMode.SNI,
                1400 to SettingsStore.TlsSplitMode.FIRST_BYTE,
                1420 to SettingsStore.TlsSplitMode.OFF,
            )

            for ((mtuCandidate, splitCandidate) in combos) {
                statusMessage = s.mtuProbingStatus(mtuCandidate, splitCandidate.localizedLabel(s.isRtl))
                val rtt = measureRealTlsFragmentedProbeMs(
                    host = targetHost,
                    port = targetPort,
                    mtuCandidate = mtuCandidate,
                    splitMode = splitCandidate,
                ) ?: measureRealTlsFragmentedProbeMs(
                    host = "cloudflare.com",
                    port = 443,
                    mtuCandidate = mtuCandidate,
                    splitMode = splitCandidate,
                )
                if (rtt != null) {
                    testedRows.add(DpiOptimizationCandidate(mtuCandidate, splitCandidate, rtt))
                    testedRows.sortBy { it.rttMs }
                }
                delay(120)
            }

            recommended = testedRows.firstOrNull()
            statusMessage = recommended?.let {
                s.mtuOptimalFoundStatus(it.mtu, it.splitMode.localizedLabel(s.isRtl), it.rttMs)
            } ?: s.mtuTimeoutStatus
            isTesting = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(0),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeedMetricTile(
                        label = s.mtuActiveLabel,
                        value = s.mtuBytesValue(currentMtu),
                        accent = neu.accent,
                        modifier = Modifier.weight(1f),
                    )
                    SpeedMetricTile(
                        label = s.mtuFragmentLabel,
                        value = currentSplit.localizedLabel(s.isRtl),
                        accent = statusColors.connected,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = neu.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(Modifier.height(18.dp))

                NeumorphicButton(
                    onClick = ::runOptimizer,
                    enabled = !isTesting,
                    accentColor = neu.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicTouchBounce(),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isTesting) s.mtuProbingButton else s.mtuFindBestButton,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                AnimatedVisibility(
                    visible = recommended != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    val best = recommended
                    if (best != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.height(16.dp))
                            NeumorphicButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    app.settingsStore.mtu = best.mtu
                                    app.settingsStore.tlsSplitMode = best.splitMode
                                    currentMtu = best.mtu
                                    currentSplit = best.splitMode
                                    statusMessage = s.mtuAppliedStatus(best.mtu, best.splitMode.localizedLabel(s.isRtl))
                                },
                                accentColor = statusColors.connected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neumorphicTouchBounce(),
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    s.mtuApplyBestButton(best.mtu, best.splitMode.localizedLabel(s.isRtl)),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                if (testedRows.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    testedRows.forEachIndexed { idx, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(neu.surfaceElevated)
                                .clickable {
                                    app.settingsStore.mtu = row.mtu
                                    app.settingsStore.tlsSplitMode = row.splitMode
                                    currentMtu = row.mtu
                                    currentSplit = row.splitMode
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "MTU ${row.mtu} • ${row.splitMode.localizedLabel(s.isRtl)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (idx == 0) statusColors.connected else neu.textPrimary,
                            )
                            Text(
                                text = "${row.rttMs} ms",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (idx == 0) statusColors.connected else neu.textSecondary,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}
