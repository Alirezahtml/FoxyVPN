package com.vauth.foxyvpn.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.formatBytesPerSecond
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.ProxyCandidate
import com.vauth.foxyvpn.data.model.emojiFlag
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.LocalNeumorphicColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicRaised
import com.vauth.foxyvpn.vpn.FoxyVpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.foundation.layout.heightIn
import java.util.Locale

@Composable
fun DynamicIsland(
    settingsStore: SettingsStore,
    selectedProxy: ProxyCandidate?,
    onDisconnect: () -> Unit,
    onRequestConnect: () -> Unit,
    onOpenServers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configRevision by settingsStore.islandConfigRevision.collectAsState()
    @Suppress("UNUSED_EXPRESSION")
    configRevision

    if (!settingsStore.dynamicIslandEnabled) return

    val state by FoxyVpnService.state.collectAsState()
    val liveStats by FoxyVpnService.vpnLiveStats.collectAsState()
    val connectedAtEpochMs by FoxyVpnService.connectedAtEpochMs.collectAsState()
    val pingLatency by FoxyVpnService.pingLatencyMs.collectAsState()

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val haptics = LocalHapticFeedback.current

    var isExpanded by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

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

    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "island-glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow-alpha",
    )

    val islandTheme = settingsStore.dynamicIslandTheme
    val pillBgColor = when (islandTheme) {
        SettingsStore.DynamicIslandTheme.DARK -> Color(0xFF0A0C10)
        SettingsStore.DynamicIslandTheme.ACCENT -> neu.accent.copy(alpha = 0.95f)
        SettingsStore.DynamicIslandTheme.NEUMORPHIC -> if (neu.isDark) Color(0xFF1A1E25) else Color(0xFFE4E9F1)
    }

    val contentColor = when (islandTheme) {
        SettingsStore.DynamicIslandTheme.DARK -> Color.White
        SettingsStore.DynamicIslandTheme.ACCENT -> Color.White
        SettingsStore.DynamicIslandTheme.NEUMORPHIC -> neu.textPrimary
    }

    val sizeConfig = settingsStore.dynamicIslandSize
    val targetCornerRadius = settingsStore.dynamicIslandCornerRadius.dp
    val pillCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else targetCornerRadius,
        animationSpec = spring(stiffness = 400f),
        label = "island-radius",
    )

    val alignment = when (settingsStore.dynamicIslandPosition) {
        SettingsStore.DynamicIslandPosition.TOP_START -> Alignment.TopStart
        SettingsStore.DynamicIslandPosition.TOP_END -> Alignment.TopEnd
        SettingsStore.DynamicIslandPosition.TOP_CENTER -> Alignment.TopCenter
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = alignment,
    ) {
        val shape = RoundedCornerShape(pillCornerRadius)

        Box(
            modifier = Modifier
                .widthIn(min = sizeConfig.widthDp.dp, max = if (isExpanded) 440.dp else (sizeConfig.widthDp + 76).dp)
                .heightIn(min = if (isExpanded) 142.dp else sizeConfig.heightDp.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                )
                .neumorphicRaised(
                    shape = shape,
                    cornerRadius = pillCornerRadius,
                    elevation = if (isExpanded) 8.dp else 4.dp,
                    lightShadowColor = if (islandTheme == SettingsStore.DynamicIslandTheme.DARK) Color.White.copy(alpha = 0.08f) else neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.85f),
                    darkShadowColor = Color.Black.copy(alpha = if (islandTheme == SettingsStore.DynamicIslandTheme.DARK) 0.65f else 0.35f),
                )
                .clip(shape)
                .background(pillBgColor)
                .border(
                    width = 1.dp,
                    color = when (state) {
                        ConnectionState.CONNECTED -> statusColors.connected.copy(alpha = if (settingsStore.dynamicIslandAnimation) glowAlpha * 0.7f else 0.4f)
                        ConnectionState.CONNECTING -> statusColors.connecting.copy(alpha = if (settingsStore.dynamicIslandAnimation) glowAlpha * 0.7f else 0.4f)
                        ConnectionState.DISCONNECTED -> Color.White.copy(alpha = if (neu.isDark) 0.08f else 0.55f)
                    },
                    shape = shape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                }
                .padding(horizontal = if (isExpanded) 20.dp else 16.dp, vertical = if (isExpanded) 18.dp else 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!isExpanded) {
                // Collapsed Compact Island Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Status dot / Icon
                    when (state) {
                        ConnectionState.CONNECTED -> {
                            com.vauth.foxyvpn.ui.theme.AnimatedTrafficEqualizer(
                                active = settingsStore.dynamicIslandAnimation,
                                color = statusColors.connected,
                                barCount = 3,
                            )
                        }
                        ConnectionState.CONNECTING -> {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(11.dp),
                                color = statusColors.connecting,
                            )
                        }
                        ConnectionState.DISCONNECTED -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.6f)),
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Main info text
                    when (state) {
                        ConnectionState.CONNECTED -> {
                            val flag = selectedProxy?.emojiFlag
                            if (settingsStore.dynamicIslandShowFlag && !flag.isNullOrBlank()) {
                                Text(flag, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                            }
                            if (settingsStore.dynamicIslandShowTimer) {
                                Text(
                                    text = formatIslandDuration(elapsedSeconds),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = contentColor,
                                )
                            } else {
                                Text(
                                    text = selectedProxy?.countryCode ?: s.appName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor,
                                )
                            }

                            if (settingsStore.dynamicIslandShowSpeed && (liveStats.rxRateBytesPerSec > 0 || liveStats.txRateBytesPerSec > 0)) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "↓${formatBytesPerSecond(liveStats.rxRateBytesPerSec)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.85f),
                                )
                            } else if (settingsStore.dynamicIslandShowPing && pingLatency != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${pingLatency}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColors.connected,
                                )
                            }
                        }
                        ConnectionState.CONNECTING -> {
                            Text(
                                text = s.islandConnecting,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = statusColors.connecting,
                            )
                        }
                        ConnectionState.DISCONNECTED -> {
                            Text(
                                text = s.appName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = contentColor.copy(alpha = 0.85f),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.islandReady,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.65f),
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.55f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            } else {
                // Expanded iOS-style Island Sheet
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val flag = selectedProxy?.emojiFlag
                            if (!flag.isNullOrBlank()) {
                                Text(flag, fontSize = 18.sp)
                                Spacer(Modifier.width(10.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = selectedProxy?.let { it.countryName.ifBlank { it.countryCode } } ?: s.islandServerDefault,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = when (state) {
                                        ConnectionState.CONNECTED -> s.islandActiveEncrypted
                                        ConnectionState.CONNECTING -> s.islandSecuringConnection
                                        ConnectionState.DISCONNECTED -> s.islandProtectedReady
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (state) {
                                        ConnectionState.CONNECTED -> statusColors.connected
                                        ConnectionState.CONNECTING -> statusColors.connecting
                                        ConnectionState.DISCONNECTED -> neu.textSecondary
                                    },
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.65f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { isExpanded = false },
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Latency / Ping
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.islandLatency,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.65f),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = pingLatency?.let { "$it ms" } ?: "\u2014",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (pingLatency != null) statusColors.connected else contentColor,
                            )
                        }

                        // Duration
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.islandDuration,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.65f),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (state == ConnectionState.CONNECTED) formatIslandDuration(elapsedSeconds) else "00:00:00",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = contentColor,
                            )
                        }

                        // Speed
                        if (settingsStore.dynamicIslandShowSpeed) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = s.islandLiveSpeed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.65f),
                                )
                                Spacer(Modifier.height(4.dp))
                                val tx = formatBytesPerSecond(liveStats.txRateBytesPerSec)
                                val rx = formatBytesPerSecond(liveStats.rxRateBytesPerSec)
                                Text(
                                    text = "↑ $tx  ↓ $rx",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(contentColor.copy(alpha = 0.12f))
                                .clickable {
                                    isExpanded = false
                                    onOpenServers()
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = s.islandChangeServer,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = contentColor,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (state == ConnectionState.CONNECTED) statusColors.connected.copy(alpha = 0.2f)
                                    else neu.accent.copy(alpha = 0.2f)
                                )
                                .clickable {
                                    isExpanded = false
                                    if (state == ConnectionState.DISCONNECTED) onRequestConnect() else onDisconnect()
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = when (state) {
                                    ConnectionState.CONNECTED -> s.islandDisconnect
                                    ConnectionState.CONNECTING -> s.islandCancel
                                    ConnectionState.DISCONNECTED -> s.islandConnect
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (state == ConnectionState.CONNECTED) statusColors.connected else neu.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatIslandDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.US, "%02d:%02d", m, s)
    }
}
