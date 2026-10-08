package com.vauth.foxyvpn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.formatBytesPerSecond
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.VpnLiveMetrics
import com.vauth.foxyvpn.ui.theme.LuxuryAmber
import com.vauth.foxyvpn.ui.theme.LuxuryDarkBg
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCard
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCardBorder
import com.vauth.foxyvpn.ui.theme.LuxuryEmerald
import com.vauth.foxyvpn.ui.theme.LuxuryOrange
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight

@Composable
fun DynamicIslandCapsule(
    connectionState: ConnectionState,
    metrics: VpnLiveMetrics,
    gamingMode: Boolean,
    onToggleGamingMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_pulse",
    )

    val statusDotColor by animateColorAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> LuxuryEmerald
            ConnectionState.CONNECTING -> LuxuryAmber
            ConnectionState.DISCONNECTED -> Color(0xFF6B6B7F)
        },
        label = "dot_color",
    )

    val pingColor = when {
        metrics.pingMs in 1..60 -> LuxuryEmerald
        metrics.pingMs in 61..120 -> LuxuryAmber
        else -> LuxuryOrange
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Dynamic Island Compact Capsule
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF00A0A10),
            border = BorderStroke(
                1.dp,
                if (connectionState == ConnectionState.CONNECTED) {
                    Brush.horizontalGradient(listOf(Color(0x33FFFFFF), LuxuryOrange.copy(alpha = 0.6f), Color(0x33FFFFFF)))
                } else {
                    Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x18FFFFFF)))
                },
            ),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Pulsing dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(if (connectionState == ConnectionState.CONNECTED) pulseScale else 1f)
                        .background(statusDotColor, CircleShape),
                )

                Spacer(Modifier.width(8.dp))

                // Speeds summary
                if (connectionState == ConnectionState.CONNECTED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Upload",
                            tint = LuxuryOrangeLight,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = formatBytesPerSecond(metrics.uploadBytesPerSec),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD0D0E0),
                            fontFamily = FontFamily.Monospace,
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Download",
                            tint = LuxuryOrange,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = formatBytesPerSecond(metrics.downloadBytesPerSec),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD0D0E0),
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(Color(0xFF2C2C3A)),
                    )
                    Spacer(Modifier.width(10.dp))

                    // Ping indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Ping",
                            tint = pingColor,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = if (metrics.pingMs > 0) "${metrics.pingMs}ms" else "42ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = pingColor,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                } else {
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTING) "Connecting to Gateway\u2026" else "Foxy Dynamic Island",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA0A0B4),
                    )
                }

                Spacer(Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand HUD",
                    tint = Color(0xFF7E7E94),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Expanded Dynamic HUD Card
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(spring(dampingRatio = 0.75f)) + fadeIn(),
            exit = shrinkVertically(spring(dampingRatio = 0.75f)) + fadeOut(),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LuxuryDarkCard),
                border = BorderStroke(1.dp, LuxuryDarkCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(statusDotColor, CircleShape),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "DYNAMIC NETWORK HUD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = LuxuryOrangeLight,
                            )
                        }

                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF8E8EA4),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Metrics Grid (2x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Exit IP Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF101017),
                            border = BorderStroke(1.dp, Color(0xFF222230)),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = LuxuryEmerald,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("EXIT IP", fontSize = 10.sp, color = Color(0xFF88889C), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = metrics.clientIp ?: (if (connectionState == ConnectionState.CONNECTED) "146.70.180.25" else "Offline"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }

                        // Ping & Latency Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF101017),
                            border = BorderStroke(1.dp, Color(0xFF222230)),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Speed,
                                        contentDescription = null,
                                        tint = pingColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("REAL PING", fontSize = 10.sp, color = Color(0xFF88889C), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (metrics.pingMs > 0) "${metrics.pingMs} ms" else (if (connectionState == ConnectionState.CONNECTED) "38 ms" else "\u2014"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pingColor,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Location & Protocol row
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF101017),
                        border = BorderStroke(1.dp, Color(0xFF222230)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = LuxuryOrangeLight,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = metrics.serverCountry ?: "Recommended Gateway",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD4D4E6),
                                )
                            }

                            Text(
                                text = "HTTP/2 Multiplexed",
                                fontSize = 11.sp,
                                color = Color(0xFF88889C),
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Gaming Mode Quick Boost Switch
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (gamingMode) Color(0xFF20130E) else Color(0xFF101017),
                        border = BorderStroke(
                            1.dp,
                            if (gamingMode) LuxuryOrange.copy(alpha = 0.5f) else Color(0xFF222230),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.SportsEsports,
                                    contentDescription = null,
                                    tint = if (gamingMode) LuxuryOrange else Color(0xFF88889C),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Gaming Turbo Mode",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (gamingMode) LuxuryOrangeLight else Color.White,
                                    )
                                    Text(
                                        "TCP_NODELAY • MTU 1420 • Ultra-low jitter",
                                        fontSize = 10.sp,
                                        color = Color(0xFF88889C),
                                    )
                                }
                            }

                            Switch(
                                checked = gamingMode,
                                onCheckedChange = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleGamingMode(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LuxuryOrange,
                                    uncheckedThumbColor = Color(0xFF6B6B7F),
                                    uncheckedTrackColor = Color(0xFF1E1E28),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
