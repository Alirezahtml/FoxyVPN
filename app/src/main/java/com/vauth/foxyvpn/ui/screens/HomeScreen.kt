package com.vauth.foxyvpn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.data.GUARDIAN_ENDPOINT_DEFAULT
import com.vauth.foxyvpn.data.RECOMMENDED_COUNTRY_CODE
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.formatBytes
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.emojiFlag
import com.vauth.foxyvpn.ui.components.DynamicIsland
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.NeumorphicSlideToConnect
import com.vauth.foxyvpn.ui.theme.ThemeController
import com.vauth.foxyvpn.ui.theme.ThemeMode
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.vpn.ExitCheck
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.vpn.IpLocationInfo
import com.vauth.foxyvpn.vpn.PingUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    app: FoxyVpnApp,
    themeController: ThemeController,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by FoxyVpnService.state.collectAsState()
    val lastError by FoxyVpnService.lastError.collectAsState()
    val liveStats by FoxyVpnService.vpnLiveStats.collectAsState()
    val connectedAtEpochMs by FoxyVpnService.connectedAtEpochMs.collectAsState()
    val pingLatency by FoxyVpnService.pingLatencyMs.collectAsState()
    val vpnEdgeIp by FoxyVpnService.vpnEdgeIp.collectAsState()

    val selectedProxy by app.proxyStateStore.selectedProxyFlow.collectAsState()

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var realIp by remember {
        mutableStateOf<IpLocationInfo?>(
            app.settingsStore.cachedRealIp?.let {
                IpLocationInfo(it, app.settingsStore.cachedRealCountry.orEmpty())
            },
        )
    }
    var isPinging by remember { mutableStateOf(false) }
    var disconnectedPingLatency by remember { mutableStateOf<Int?>(null) }
    var quotaRemaining by remember { mutableStateOf(app.settingsStore.cachedQuotaRemaining) }
    var quotaMax by remember { mutableStateOf(app.settingsStore.cachedQuotaMax) }
    var isUnlimited by remember { mutableStateOf(app.settingsStore.cachedIsUnlimited) }

    LaunchedEffect(state) {
        if (state == ConnectionState.CONNECTED) {
            quotaRemaining = app.settingsStore.cachedQuotaRemaining
            quotaMax = app.settingsStore.cachedQuotaMax
            isUnlimited = app.settingsStore.cachedIsUnlimited
        }
    }

    LaunchedEffect(state, connectedAtEpochMs) {
        if (state == ConnectionState.CONNECTED && connectedAtEpochMs != null) {
            while (isActive) {
                val now = System.currentTimeMillis()
                elapsedSeconds = ((now - connectedAtEpochMs!!) / 1000L).coerceAtLeast(0L)
                delay(1000L)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    LaunchedEffect(Unit) {
        launch {
            ExitCheck().fetchDirectIpInfo().onSuccess { info ->
                realIp = info
                app.settingsStore.cachedRealIp = info.ip
                app.settingsStore.cachedRealCountry = info.countryCode
            }
        }
        launch {
            val token = app.authRepository.currentAccessToken()
            if (token != null) {
                runCatching { app.guardianClient.fetchUserInfo(GUARDIAN_ENDPOINT_DEFAULT, token) }
                    .onSuccess { entitlement ->
                        val unlimited = !entitlement.limitedBandwidth
                        isUnlimited = unlimited
                        app.settingsStore.cachedIsUnlimited = unlimited
                        entitlement.quotaRemaining?.let {
                            quotaRemaining = it
                            app.settingsStore.cachedQuotaRemaining = it
                        }
                        entitlement.maxBytes?.let {
                            quotaMax = it
                            app.settingsStore.cachedQuotaMax = it
                        }
                    }
            }
        }
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(neu.accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = neu.accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "FoxyVPN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                            ),
                            color = neu.textPrimary,
                        )
                    }
                },
                actions = {
                    val mode = themeController.mode
                    val showingDark = themeController.resolveDark(systemInDarkTheme)
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = { themeController.toggle(systemInDarkTheme) },
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                            },
                            contentDescription = "Toggle Theme",
                            tint = neu.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = onOpenSettings,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = neu.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = neu.background,
                ),
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Dynamic Island header pill
                DynamicIsland(
                    settingsStore = app.settingsStore,
                    selectedProxy = selectedProxy,
                    onDisconnect = onDisconnect,
                    onRequestConnect = onRequestConnect,
                    onOpenServers = onOpenServers,
                )

                Spacer(Modifier.height(18.dp))

                // Connection Status Hero
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp),
                ) {
                    val statusText = when (state) {
                        ConnectionState.CONNECTED -> "SECURE & CONNECTED"
                        ConnectionState.CONNECTING -> "ESTABLISHING TUNNEL\u2026"
                        ConnectionState.DISCONNECTED -> "NOT PROTECTED"
                    }
                    val statusColor = when (state) {
                        ConnectionState.CONNECTED -> statusColors.connected
                        ConnectionState.CONNECTING -> statusColors.connecting
                        ConnectionState.DISCONNECTED -> neu.textSecondary
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                        ),
                        color = statusColor,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (state == ConnectionState.CONNECTED) formatDuration(elapsedSeconds) else "00:00:00",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = neu.textPrimary,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // SLIDE-TO-CONNECT PRIMARY CONTROLLER
                NeumorphicSlideToConnect(
                    state = state,
                    lastError = lastError,
                    onRequestConnect = onRequestConnect,
                    onDisconnect = onDisconnect,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                val error = lastError
                if (error != null && state != ConnectionState.CONNECTING) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Location Card (Raised Neumorphic)
                NeumorphicCard(
                    onClick = onOpenServers,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val flag = selectedProxy?.emojiFlag
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(neu.surfaceElevated),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!flag.isNullOrBlank()) {
                                Text(flag, fontSize = 22.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = neu.accent,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "SERVER LOCATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = neu.textSecondary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                selectedProxy?.let { it.countryName.ifBlank { it.countryCode } } ?: "Recommended (Fastest)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary,
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Change Server",
                            tint = neu.textSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Current IP Card (Raised Neumorphic)
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                ) {
                    val isConnected = state == ConnectionState.CONNECTED
                    val ipDisplay = if (isConnected) {
                        vpnEdgeIp?.let {
                            if (it.countryCode.isNotEmpty()) "${it.ip} • ${it.countryCode}" else it.ip
                        } ?: "Verifying\u2026"
                    } else {
                        realIp?.let {
                            if (it.countryCode.isNotEmpty()) "${it.ip} • ${it.countryCode}" else it.ip
                        } ?: app.settingsStore.cachedRealIp?.let { cachedIp ->
                            val cachedCountry = app.settingsStore.cachedRealCountry
                            if (!cachedCountry.isNullOrEmpty()) "$cachedIp • $cachedCountry" else cachedIp
                        } ?: "Checking\u2026"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) statusColors.connected.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Filled.Security else Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (isConnected) statusColors.connected else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "CURRENT IP ADDRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = neu.textSecondary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                ipDisplay,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = neu.textPrimary,
                            )
                        }

                        val badgeColor = if (isConnected) statusColors.connected else MaterialTheme.colorScheme.error
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(
                                if (isConnected) "Protected" else "Exposed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = badgeColor,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Telemetry & Quota Card (Raised Neumorphic)
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                ) {
                    Column(Modifier.padding(18.dp)) {
                        val isConnected = state == ConnectionState.CONNECTED
                        val remaining = quotaRemaining
                        val max = quotaMax

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Data Used
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "DATA USED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                    ),
                                    color = neu.textSecondary,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (isConnected) formatBytes(liveStats.sessionBytes) else "0.0 MB",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = neu.textPrimary,
                                )
                            }

                            // Latency / Ping
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(role = Role.Button) {
                                        if (!isPinging) {
                                            scope.launch {
                                                isPinging = true
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (state == ConnectionState.CONNECTED) {
                                                    FoxyVpnService.pingNow()
                                                } else {
                                                    val target = selectedProxy ?: app.proxyStateStore.load() ?: run {
                                                        val cached = app.serverListClient.loadCachedCountries()
                                                        ServerListClient.candidatesForCountry(cached, RECOMMENDED_COUNTRY_CODE).firstOrNull()
                                                            ?: cached.firstOrNull { it.code.isNotBlank() }?.let { country ->
                                                                ServerListClient.candidatesForCountry(cached, country.code).firstOrNull()
                                                            }
                                                    }
                                                    if (target != null) {
                                                        disconnectedPingLatency = PingUtil.measureTcpLatencyMs(target.host, target.port)
                                                    }
                                                }
                                                isPinging = false
                                            }
                                        }
                                    },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "LATENCY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                        ),
                                        color = neu.textSecondary,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.NetworkCheck,
                                        contentDescription = "Test Ping",
                                        tint = neu.textSecondary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                val currentPing = if (isConnected) pingLatency else disconnectedPingLatency
                                Text(
                                    text = when {
                                        isPinging -> "Testing\u2026"
                                        currentPing != null -> "$currentPing ms"
                                        else -> "\u2014 ms"
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentPing != null) statusColors.connected else neu.textPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // Quota Details & Sunken Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Monthly Quota",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            val quotaText = when {
                                isUnlimited -> "Unlimited"
                                remaining != null && max != null -> "${formatBytes(remaining)} / ${formatBytes(max)}"
                                remaining != null -> formatBytes(remaining)
                                max != null -> "Limit: ${formatBytes(max)}"
                                else -> "\u2014"
                            }
                            Text(
                                quotaText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = neu.textPrimary,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        val quotaFraction = when {
                            isUnlimited -> 1f
                            max != null && max > 0L && remaining != null ->
                                (remaining.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                            else -> 0f
                        }

                        // Sunken Neumorphic Progress Track
                        val progressShape = RoundedCornerShape(6.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .neumorphicSunken(
                                    shape = progressShape,
                                    cornerRadius = 6.dp,
                                    depth = 2.dp,
                                    surfaceColor = neu.surfaceElevated,
                                ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(quotaFraction)
                                    .fillMaxSize()
                                    .clip(progressShape)
                                    .background(
                                        if (isConnected) statusColors.connected
                                        else neu.accent.copy(alpha = 0.8f)
                                    ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
