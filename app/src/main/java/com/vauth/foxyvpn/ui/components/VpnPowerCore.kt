package com.vauth.foxyvpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.ui.theme.LuxuryAmber
import com.vauth.foxyvpn.ui.theme.LuxuryOrange
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeGlow
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pixel-precise VPN Power Connection Core from Screenshots 1, 2, and 3.
 * Features multi-tier brushed metallic bezels, dynamic radar orbital wave particles,
 * iOS-grade tactile spring feedback, and radiant orange glass bloom.
 */
@Composable
fun VpnPowerCore(
    state: ConnectionState,
    onPowerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    // Rotation of ambient particle ring
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == ConnectionState.CONNECTED) 8000 else 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    // Breathing pulse for outer aura
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == ConnectionState.CONNECTED) 1400 else 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // iOS spring tactile button press scale feedback
    val buttonScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            state == ConnectionState.CONNECTING -> 0.96f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "button_scale",
    )

    val coreBgBrush = when (state) {
        ConnectionState.CONNECTED -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF7A45),
                Color(0xFFFE592A),
                Color(0xFFD63B07),
            ),
        )
        ConnectionState.CONNECTING -> Brush.verticalGradient(
            colors = listOf(
                LuxuryAmber,
                Color(0xFFE65100),
                Color(0xFF381404),
            ),
        )
        ConnectionState.DISCONNECTED -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF222230),
                Color(0xFF161622),
                Color(0xFF0F0F16),
            ),
        )
    }

    val glowColor by animateColorAsState(
        targetValue = when (state) {
            ConnectionState.CONNECTED -> LuxuryOrangeGlow.copy(alpha = 0.55f)
            ConnectionState.CONNECTING -> LuxuryAmber.copy(alpha = 0.4f)
            ConnectionState.DISCONNECTED -> Color(0x18FE592A)
        },
        label = "glow_color",
    )

    Box(
        modifier = modifier.size(310.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer Radar & Wave Particle Mesh Canvas
        Canvas(
            modifier = Modifier
                .size(310.dp)
                .scale(wavePulse),
        ) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.36f
            val isConnected = state == ConnectionState.CONNECTED
            val isConnecting = state == ConnectionState.CONNECTING

            val primaryColor = when {
                isConnected -> Color(0xFFFE592A)
                isConnecting -> LuxuryAmber
                else -> Color(0xFF4C2A1E)
            }

            val secondaryColor = when {
                isConnected -> Color(0xFFFF8555)
                isConnecting -> Color(0xFFFFD54F)
                else -> Color(0xFF321A12)
            }

            // Radial bloom behind the core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = centerOffset,
                    radius = size.width * 0.5f,
                ),
                radius = size.width * 0.5f,
                center = centerOffset,
            )

            // Draw concentric dashed orbit ring
            drawCircle(
                color = primaryColor.copy(alpha = if (isConnected) 0.35f else 0.15f),
                radius = baseRadius + 22f,
                center = centerOffset,
                style = Stroke(
                    width = 1f,
                ),
            )

            // Draw organic wavy mesh lines and radiating particles
            val layers = if (isConnected) 5 else 3
            val pointsPerLayer = if (isConnected) 72 else 48

            for (layer in 0 until layers) {
                val layerRadius = baseRadius + (layer - 2) * 12f
                val rot = (waveRotation * (if (layer % 2 == 0) 1 else -1) * PI / 180f).toFloat()

                val path = Path()
                var startP = Offset.Zero

                for (i in 0..pointsPerLayer) {
                    val angle = (i.toFloat() / pointsPerLayer) * 2f * PI.toFloat() + rot
                    val harmonic1 = sin(angle * 6f + rot * 1.5f) * if (isConnected) 9f else 3.5f
                    val harmonic2 = cos(angle * 4f - rot) * if (isConnected) 6f else 2f
                    val r = layerRadius + harmonic1 + harmonic2

                    val x = centerOffset.x + r * cos(angle)
                    val y = centerOffset.y + r * sin(angle)
                    val pt = Offset(x, y)

                    if (i == 0) {
                        startP = pt
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Radiating fine particle nodes
                    if (i % 2 == 0) {
                        val pRadius = if (isConnected) (1.2f + (i % 3) * 0.6f) else 1.0f
                        val alpha = if (isConnected) {
                            (0.85f - layer * 0.12f).coerceIn(0.2f, 1f)
                        } else 0.3f

                        drawCircle(
                            color = if (i % 4 == 0) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha * 0.8f),
                            radius = pRadius,
                            center = pt,
                        )
                    }
                }
                path.lineTo(startP.x, startP.y)

                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = if (isConnected) 0.28f else 0.12f),
                    style = Stroke(width = if (isConnected) 1.2f else 0.8f, cap = StrokeCap.Round),
                )
            }
        }

        // Concentric Layer 1: Outer Dark Bezel
        Box(
            modifier = Modifier
                .size(186.dp)
                .clip(CircleShape)
                .background(Color(0xFF13131B))
                .border(BorderStroke(1.dp, Color(0xFF262636)), CircleShape)
                .shadow(elevation = 18.dp, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Concentric Layer 2: Intermediate Metallic Groove
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF20202C), Color(0xFF12121A)),
                        ),
                    )
                    .border(BorderStroke(1.dp, Color(0xFF2E2E40)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                // Concentric Layer 3: Inner Recessed Well
                Box(
                    modifier = Modifier
                        .size(138.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F0F16))
                        .border(
                            BorderStroke(
                                1.dp,
                                if (state == ConnectionState.CONNECTED) Color(0x44FE592A) else Color(0x18FFFFFF),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Center Power Button
                    Box(
                        modifier = Modifier
                            .size(122.dp)
                            .scale(buttonScale)
                            .clip(CircleShape)
                            .background(coreBgBrush)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (state == ConnectionState.CONNECTED) {
                                        Color(0x99FFFFFF)
                                    } else {
                                        Color(0x3DFE592A)
                                    },
                                ),
                                CircleShape,
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPowerClick()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        // Subtle top glass glare specular highlight
                        Canvas(modifier = Modifier.size(122.dp)) {
                            drawArc(
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                ),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(10f, 6f),
                                size = Size(size.width - 20f, size.height * 0.45f),
                                style = Stroke(width = 1.5f),
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PowerSettingsNew,
                                contentDescription = if (state == ConnectionState.CONNECTED) "Disconnect" else "Connect",
                                tint = if (state == ConnectionState.DISCONNECTED) Color(0xFFE2E2F0) else Color.White,
                                modifier = Modifier.size(38.dp),
                            )

                            Spacer(Modifier.height(5.dp))

                            Text(
                                text = when (state) {
                                    ConnectionState.CONNECTED -> "Disconnect"
                                    ConnectionState.CONNECTING -> "Connecting\u2026"
                                    ConnectionState.DISCONNECTED -> "Tap to connect"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = if (state == ConnectionState.DISCONNECTED) {
                                    Color(0xFF9898B0)
                                } else {
                                    Color.White.copy(alpha = 0.95f)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
