package com.vauth.foxyvpn.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. Staggered Entrance Modifier (Fade + Slide Up + Subtle Scale)
 */
fun Modifier.staggeredEntrance(
    index: Int,
    delayPerItemMs: Int = 55,
    durationMs: Int = 420,
): Modifier = composed {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtLeast(0) * delayPerItemMs).toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        )
    }
    this.graphicsLayer {
        alpha = anim.value
        translationY = (1f - anim.value) * 32f
        val s = 0.96f + (0.04f * anim.value)
        scaleX = s
        scaleY = s
    }
}

/**
 * 2. Tactile Neumorphic Press-Bounce Modifier
 */
fun Modifier.neumorphicTouchBounce(
    pressedScale: Float = 0.965f,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "touch-bounce-scale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

/**
 * 3. Shake-on-Error Horizontal Keyframe Modifier
 */
fun Modifier.shakeOnTrigger(triggerKey: Any?): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(triggerKey) {
        if (triggerKey != null) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 460
                    0f at 0
                    (-18f) at 60
                    18f at 120
                    (-14f) at 180
                    14f at 240
                    (-8f) at 310
                    8f at 380
                    0f at 460
                },
            )
        }
    }
    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * 4. Floating / Levitating Idle Animation Modifier
 */
fun Modifier.subtleFloatAnimation(
    amplitudePx: Float = 8f,
    durationMs: Int = 2200,
): Modifier = composed {
    val infinite = rememberInfiniteTransition(label = "float-idle")
    val dy by infinite.animateFloat(
        initialValue = -amplitudePx,
        targetValue = amplitudePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float-dy",
    )
    this.graphicsLayer {
        translationY = dy
    }
}

/**
 * 5. Concentric Pulsing Radar Rings (Hero Status Aura)
 */
@Composable
fun NeumorphicRadarPulse(
    color: Color,
    isActive: Boolean,
    isConnecting: Boolean = false,
    size: Dp = 150.dp,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {},
) {
    val infinite = rememberInfiniteTransition(label = "radar-pulse")
    val phase1 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 1300 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "radar-p1",
    )
    val sweepAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 1600 else 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "radar-sweep",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (isActive || isConnecting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val maxRadius = this.size.minDimension / 2f
                val phases = listOf(
                    phase1,
                    (phase1 + 0.33f) % 1f,
                    (phase1 + 0.66f) % 1f,
                )
                phases.forEach { p ->
                    val r = maxRadius * (0.35f + 0.65f * p)
                    val alpha = (1f - p).coerceIn(0f, 1f) * 0.38f
                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = r,
                        center = center,
                        style = Stroke(width = 2.2.dp.toPx()),
                    )
                }
                if (isConnecting) {
                    rotate(sweepAngle, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to Color.Transparent,
                                0.7f to color.copy(alpha = 0.05f),
                                1f to color.copy(alpha = 0.36f),
                                center = center,
                            ),
                            startAngle = 0f,
                            sweepAngle = 95f,
                            useCenter = true,
                            topLeft = Offset(center.x - maxRadius * 0.85f, center.y - maxRadius * 0.85f),
                            size = Size(maxRadius * 1.7f, maxRadius * 1.7f),
                        )
                    }
                }
            }
        }
        centerContent()
    }
}

/**
 * 6. Odometer-style Animated Text for Timers & Counters
 */
@Composable
fun AnimatedOdometerText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn(tween(180)))
                        .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(180)))
                        .using(SizeTransform(clip = false))
                },
                label = "odometer-char-$index",
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = style,
                    color = color,
                )
            }
        }
    }
}

/**
 * 7. Dual-Line Live Traffic Waveform Sparkline Canvas
 */
@Composable
fun LiveTrafficSparkline(
    rxHistory: List<Long>,
    txHistory: List<Long>,
    rxColor: Color,
    txColor: Color,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "wave-phase")
    val waveShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave-shift",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val maxVal = maxOf(
            rxHistory.maxOrNull() ?: 1024L,
            txHistory.maxOrNull() ?: 1024L,
            4096L,
        ).toFloat()

        fun buildSmoothPath(points: List<Long>, phaseOffset: Float): Path {
            val path = Path()
            val count = points.size.coerceAtLeast(2)
            val stepX = w / (count - 1).toFloat()
            for (i in 0 until count) {
                val sample = points.getOrElse(i) { 0L }.toFloat()
                val normalized = (sample / maxVal).coerceIn(0f, 1f)
                val idleBreath = sin(waveShift + phaseOffset + i * 0.45f) * 0.04f
                val yFactor = (normalized * 0.82f + 0.08f + idleBreath).coerceIn(0.05f, 0.95f)
                val x = i * stepX
                val y = h - (yFactor * h)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevSample = points.getOrElse(i - 1) { 0L }.toFloat()
                    val prevNorm = (prevSample / maxVal).coerceIn(0f, 1f)
                    val prevBreath = sin(waveShift + phaseOffset + (i - 1) * 0.45f) * 0.04f
                    val prevY = h - ((prevNorm * 0.82f + 0.08f + prevBreath).coerceIn(0.05f, 0.95f) * h)
                    val ctrlX = (prevX + x) / 2f
                    path.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                }
            }
            return path
        }

        val rxPath = buildSmoothPath(rxHistory, 0f)
        val txPath = buildSmoothPath(txHistory, 1.6f)

        // Fill under RX curve
        val fillPath = Path().apply {
            addPath(rxPath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(rxColor.copy(alpha = 0.28f), Color.Transparent),
                startY = 0f,
                endY = h,
            ),
        )

        drawPath(
            path = txPath,
            color = txColor.copy(alpha = 0.75f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawPath(
            path = rxPath,
            color = rxColor,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * 8. Animated Equalizer Activity Bars (For Dynamic Island & Audio/Traffic Pulse)
 */
@Composable
fun AnimatedTrafficEqualizer(
    active: Boolean,
    color: Color,
    barCount: Int = 4,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "eq-bars")
    val anims = (0 until barCount).map { idx ->
        infinite.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 360 + idx * 110,
                    easing = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "eq-bar-$idx",
        )
    }

    Row(
        modifier = modifier.height(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        anims.forEach { state ->
            val fraction = if (active) state.value else 0.25f
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((14f * fraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .graphicsLayer { alpha = if (active) 0.95f else 0.4f },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = color)
                }
            }
        }
    }
}

/**
 * 9. Neumorphic Speedometer Analog + Digital Gauge Canvas
 */
@Composable
fun NeumorphicSpeedometerGauge(
    speedMbps: Float,
    maxMbps: Float = 100f,
    accentColor: Color,
    trackColor: Color,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedMbps.coerceIn(0f, maxMbps),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "speed-gauge-needle",
    )

    val infinite = rememberInfiniteTransition(label = "gauge-glow")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gauge-glow-val",
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension - strokeWidth * 2f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = diameter / 2f

        // Background track arc (135 deg to 405 deg -> 270 deg sweep)
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Tick marks
        val tickCount = 28
        for (i in 0..tickCount) {
            val fraction = i.toFloat() / tickCount
            val angleDeg = 135f + fraction * 270f
            val angleRad = angleDeg * (PI / 180.0)
            val isMajor = i % 4 == 0
            val innerR = radius - strokeWidth * (if (isMajor) 1.4f else 1.1f)
            val outerR = radius - strokeWidth * 0.7f
            val start = Offset(
                x = center.x + (innerR * cos(angleRad)).toFloat(),
                y = center.y + (innerR * sin(angleRad)).toFloat(),
            )
            val end = Offset(
                x = center.x + (outerR * cos(angleRad)).toFloat(),
                y = center.y + (outerR * sin(angleRad)).toFloat(),
            )
            drawLine(
                color = trackColor.copy(alpha = if (isMajor) 0.9f else 0.45f),
                start = start,
                end = end,
                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val sweepFraction = (animatedSpeed / maxMbps).coerceIn(0f, 1f)
        val activeSweep = sweepFraction * 270f

        if (activeSweep > 0.5f) {
            drawArc(
                color = accentColor.copy(alpha = if (isRunning) glowPulse else 0.9f),
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        // Needle
        val needleAngleRad = (135f + activeSweep) * (PI / 180.0)
        val needleLen = radius * 0.72f
        val needleTip = Offset(
            x = center.x + (needleLen * cos(needleAngleRad)).toFloat(),
            y = center.y + (needleLen * sin(needleAngleRad)).toFloat(),
        )
        drawLine(
            color = accentColor,
            start = center,
            end = needleTip,
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = accentColor,
            radius = 7.dp.toPx(),
            center = center,
        )
    }
}
