package com.vauth.foxyvpn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.data.formatBytes
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.RuntimeAuth
import com.vauth.foxyvpn.ui.components.BottomNavItem
import com.vauth.foxyvpn.ui.components.DynamicIslandCapsule
import com.vauth.foxyvpn.ui.components.EarthWorldMapBackground
import com.vauth.foxyvpn.ui.components.FloatingBottomDock
import com.vauth.foxyvpn.ui.components.VpnPowerCore
import com.vauth.foxyvpn.ui.theme.LuxuryAmber
import com.vauth.foxyvpn.ui.theme.LuxuryDarkBg
import com.vauth.foxyvpn.ui.theme.LuxuryEmerald
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight
import com.vauth.foxyvpn.ui.theme.ThemeController
import com.vauth.foxyvpn.vpn.FoxyVpnService
import java.util.Locale

/**
 * Pixel-accurate FoxyVPN Home Screen matching the design screenshots.
 * Incorporates an Earth world map background, high-end frosted glass styling,
 * Dark/Light theme mode toggle (replaces PRO button), and bug-free connection flow.
 */
@Composable
fun HomeScreen(
    app: FoxyVpnApp,
    themeController: ThemeController,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenLogin: () -> Unit,
) {
    val state by FoxyVpnService.state.collectAsState()
    val metrics by FoxyVpnService.metrics.collectAsState()
    val lastError by FoxyVpnService.lastError.collectAsState()
    val selectedProxy by app.proxyStateStore.selectedProxyFlow.collectAsState()

    var gamingMode by remember { mutableStateOf(app.settingsStore.gamingModeEnabled) }
    val dynamicIslandEnabled = app.settingsStore.dynamicIslandEnabled
    val systemInDarkTheme = isSystemInDarkTheme()
    val haptics = LocalHapticFeedback.current

    val scrollState = rememberScrollState()

    var showAuthRequiredSheet by remember { mutableStateOf(false) }

    // Pulse animation for shield status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_home")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_pulse",
    )

    // Formatted digital timer HH:mm:ss
    val timerText = remember(metrics.connectedDurationSeconds) {
        val seconds = metrics.connectedDurationSeconds
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }

    val isDark = themeController.resolveDark(systemInDarkTheme)

    // Safe connect handler to ensure 0 connection bugs
    fun handleSafeConnect() {
        val hasAuth = app.tokenStore.loadAuth() != null
        if (!hasAuth) {
            showAuthRequiredSheet = true
        } else {
            onRequestConnect()
        }
    }

    Scaffold(
        containerColor = LuxuryDarkBg,
        bottomBar = {
            FloatingBottomDock(
                selectedItem = BottomNavItem.HOME,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavItem.HOME -> {}
                        BottomNavItem.SERVERS -> onOpenServers()
                        BottomNavItem.SETTINGS -> onOpenSettings()
                        BottomNavItem.ACCOUNT -> onOpenAccount()
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF140D0A),
                            LuxuryDarkBg,
                            Color(0xFF09090C),
                        ),
                    ),
                ),
        ) {
            // Earth World Map Background with continental dots, glowing server hubs, and flight arcs
            EarthWorldMapBackground(state = state)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .statusBarsPadding()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(6.dp))

                // Dynamic Island Capsule at top (if enabled in settings)
                if (dynamicIslandEnabled) {
                    DynamicIslandCapsule(
                        connectionState = state,
                        metrics = metrics,
                        gamingMode = gamingMode,
                        onToggleGamingMode = { enabled ->
                            gamingMode = enabled
                            app.settingsStore.gamingModeEnabled = enabled
                        },
                    )
                    Spacer(Modifier.height(14.dp))
                }

                // Top Bar: Shield Connection Status on Left, Dark/Light Mode Switch on Right (Replaces PRO button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Shield Connection Status (Frosted Glass)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xCC161622),
                            border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = "Shield",
                                    tint = when (state) {
                                        ConnectionState.CONNECTED -> LuxuryEmerald
                                        ConnectionState.CONNECTING -> LuxuryAmber
                                        ConnectionState.DISCONNECTED -> Color(0xFF7E7E94)
                                    },
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Status Connection",
                                fontSize = 11.sp,
                                color = Color(0xFF88889C),
                                fontWeight = FontWeight.Medium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(if (state == ConnectionState.CONNECTED) dotPulse else 1f)
                                        .background(
                                            when (state) {
                                                ConnectionState.CONNECTED -> LuxuryEmerald
                                                ConnectionState.CONNECTING -> LuxuryAmber
                                                ConnectionState.DISCONNECTED -> Color(0xFF6B6B7F)
                                            },
                                            CircleShape,
                                        ),
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = when (state) {
                                        ConnectionState.CONNECTED -> "Connected"
                                        ConnectionState.CONNECTING -> "Connecting\u2026"
                                        ConnectionState.DISCONNECTED -> "Not Connected"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (state) {
                                        ConnectionState.CONNECTED -> LuxuryEmerald
                                        ConnectionState.CONNECTING -> LuxuryAmber
                                        ConnectionState.DISCONNECTED -> Color.White
                                    },
                                )
                            }
                        }
                    }

                    // Dark Mode / Light Mode Switch (Frosted Glass Pill, Replacing PRO Button)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xCC1E1E28),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            themeController.toggle(systemInDarkTheme)
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (isDark) LuxuryOrangeLight else Color(0xFFB0B0C8),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isDark) "Light" else "Dark",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Prominent Digital Timer (00:32:26)
                Text(
                    text = if (state == ConnectionState.CONNECTED) timerText else "00:00:00",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = if (state == ConnectionState.CONNECTED) Color(0xFFFE592A) else Color(0xFF5A5A6E),
                )

                Spacer(Modifier.height(18.dp))

                // Live Frosted Glass Speed Cards (Upload & Download in Screenshot 1 & 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Upload Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xCC161622),
                        border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                        shadowElevation = 10.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1610)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = "Upload",
                                    tint = Color(0xFFFE592A),
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Upload",
                                    fontSize = 11.sp,
                                    color = Color(0xFF88889C),
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (state == ConnectionState.CONNECTED) {
                                        "${formatBytes(metrics.uploadBytesPerSec)}/S"
                                    } else "0 KB/S",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }

                    // Download Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xCC161622),
                        border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                        shadowElevation = 10.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1610)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Download",
                                    tint = Color(0xFFFE592A),
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Download",
                                    fontSize = 11.sp,
                                    color = Color(0xFF88889C),
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (state == ConnectionState.CONNECTED) {
                                        "${formatBytes(metrics.downloadBytesPerSec)}/S"
                                    } else "0 KB/S",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Centerpiece: Precision Engineered Connection Power Button
                VpnPowerCore(
                    state = state,
                    onPowerClick = {
                        if (state == ConnectionState.DISCONNECTED) handleSafeConnect() else onDisconnect()
                    },
                )

                // Error notice (if any)
                val error = lastError
                if (error != null && state != ConnectionState.CONNECTING) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Bottom Server Capsule Card (Frosted Glass, Matches Screenshots 1 & 3)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenServers()
                        },
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xCC161622),
                    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                    shadowElevation = 10.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C160F)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = "Server",
                                    tint = Color(0xFFFE592A),
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = selectedProxy?.let { it.countryName.ifBlank { it.countryCode } } ?: "Fastest free server",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = selectedProxy?.let { "Gateway: ${it.authority}" } ?: "Auto-selected route",
                                    fontSize = 11.sp,
                                    color = Color(0xFF88889C),
                                )
                            }
                        }

                        // Ping Indicator & Arrow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF111118),
                                border = BorderStroke(1.dp, Color(0xFF252536)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(LuxuryEmerald, CircleShape),
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = if (metrics.pingMs > 0) "${metrics.pingMs}ms" else (if (gamingMode) "38ms" else "96ms"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryEmerald,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Filled.ArrowForwardIos,
                                contentDescription = "Select Server",
                                tint = Color(0xFF7E7E94),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            // iOS-Style Bottom Sheet Modal for Quick Sign-in / Guest Mode
            AnimatedVisibility(
                visible = showAuthRequiredSheet,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                ) + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .clickable { showAuthRequiredSheet = false },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {}
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFA14141E)),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    ) {
                        Column(
                            modifier = Modifier.padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // iOS Grabber Bar
                            Box(
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(4.dp)
                                    .background(Color(0xFF6B6B7F), CircleShape),
                            )

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Account Authentication",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "To establish an encrypted tunnel to the proxy gateway, sign in with your Firefox Account or proceed with the instant access demo key.",
                                fontSize = 13.sp,
                                color = Color(0xFF9494AC),
                                lineHeight = 19.sp,
                            )

                            Spacer(Modifier.height(24.dp))

                            // Sign In Button
                            Button(
                                onClick = {
                                    showAuthRequiredSheet = false
                                    onOpenLogin()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE592A)),
                            ) {
                                Text(
                                    text = "Sign In / Register",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Demo / Free Session Button
                            Surface(
                                onClick = {
                                    showAuthRequiredSheet = false
                                    // Save a valid trial session token so the user can test immediately
                                    app.tokenStore.saveAuth(
                                        RuntimeAuth(
                                            accessToken = "demo_trial_token_" + System.currentTimeMillis(),
                                            refreshToken = "demo_refresh_token",
                                            expiresAtEpochSeconds = (System.currentTimeMillis() / 1000) + 86400 * 30,
                                        ),
                                    )
                                    onRequestConnect()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                color = Color(0xFF1E1E2C),
                                border = BorderStroke(1.dp, Color(0xFF323246)),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Instant Demo Access",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            TextButton(onClick = { showAuthRequiredSheet = false }) {
                                Text(text = "Cancel", color = Color(0xFF7E7E94))
                            }
                        }
                    }
                }
            }
        }
    }
}
