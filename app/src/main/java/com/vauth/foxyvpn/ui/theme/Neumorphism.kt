package com.vauth.foxyvpn.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.model.ConnectionState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val shadowLight: Color,
    val shadowDark: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean,
)

val LocalNeumorphicColors = staticCompositionLocalOf {
    NeumorphicColors(
        background = NeuLightBg,
        surface = NeuLightSurface,
        surfaceElevated = NeuLightSurfaceElevated,
        shadowLight = NeuLightShadowLight,
        shadowDark = NeuLightShadowDark,
        accent = md_theme_light_primary,
        textPrimary = md_theme_light_onBackground,
        textSecondary = md_theme_light_onSurfaceVariant,
        isDark = false,
    )
}

val currentNeumorphicColors: NeumorphicColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNeumorphicColors.current

/**
 * Renders a dual-shadow raised Soft Neumorphic effect.
 */
fun Modifier.neumorphicRaised(
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    isPressed: Boolean = false,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
): Modifier = this.then(
    Modifier.drawBehind {
        val lightColor = lightShadowColor ?: if (drawContext.size.width > 0) Color.White.copy(alpha = 0.9f) else Color.White
        val darkColor = darkShadowColor ?: Color.Black.copy(alpha = 0.15f)

        val currentElev = if (isPressed) (elevation * 0.25f) else elevation
        val blurPx = currentElev.toPx()
        val offsetPx = (currentElev * 0.85f).toPx()
        val radiusPx = cornerRadius.toPx()

        drawIntoCanvas { canvas ->
            if (blurPx > 0.5f) {
                // Top-left light highlight
                val paintLight = Paint().asFrameworkPaint().apply {
                    color = lightColor.toArgb()
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRoundRect(
                    -offsetPx,
                    -offsetPx,
                    size.width - offsetPx,
                    size.height - offsetPx,
                    radiusPx,
                    radiusPx,
                    paintLight,
                )

                // Bottom-right dark shadow
                val paintDark = Paint().asFrameworkPaint().apply {
                    color = darkColor.toArgb()
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRoundRect(
                    offsetPx,
                    offsetPx,
                    size.width + offsetPx,
                    size.height + offsetPx,
                    radiusPx,
                    radiusPx,
                    paintDark,
                )
            }
        }
    }
)

/**
 * Renders a sunken / inset neumorphic groove for sliders, textboxes, and tracks.
 */
fun Modifier.neumorphicSunken(
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    depth: Dp = 4.dp,
    surfaceColor: Color = Color.Transparent,
): Modifier = this.then(
    Modifier
        .clip(shape)
        .drawBehind {
            val r = cornerRadius.toPx()
            val d = depth.toPx()
            val w = size.width
            val h = size.height

            // Subtle base fill
            if (surfaceColor != Color.Transparent) {
                drawRoundRect(
                    color = surfaceColor,
                    size = size,
                    cornerRadius = CornerRadius(r, r),
                )
            }

            // Top-left inset dark shadow
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(d * 2.5f, d * 2.5f),
                ),
                size = size,
                cornerRadius = CornerRadius(r, r),
            )

            // Bottom-right inset light highlight
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f)),
                    start = Offset(w - d * 2f, h - d * 2f),
                    end = Offset(w, h),
                ),
                size = size,
                cornerRadius = CornerRadius(r, r),
            )
        }
)

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation * 0.35f else elevation,
        animationSpec = spring(stiffness = 400f),
        label = "neu-card-elevation",
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .neumorphicRaised(
                shape = shape,
                cornerRadius = cornerRadius,
                elevation = animatedElevation,
                isPressed = isPressed,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.3f else 0.85f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.7f else 0.45f),
            )
            .clip(shape)
            .background(neu.surface)
            .border(
                width = 1.dp,
                color = if (neu.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.6f),
                shape = shape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else Modifier
            ),
    ) {
        content()
    }
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 5.dp,
    accentColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.5.dp else elevation,
        label = "btn-elev",
    )
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = accentColor ?: neu.surface

    Box(
        modifier = modifier
            .neumorphicRaised(
                shape = shape,
                cornerRadius = cornerRadius,
                elevation = animatedElevation,
                isPressed = isPressed && enabled,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.9f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.5f),
            )
            .clip(shape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (neu.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.7f),
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun NeumorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    isSelected: Boolean = false,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = CircleShape
    val haptics = LocalHapticFeedback.current

    val elevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 1.dp else 4.dp,
        label = "icon-btn-elev",
    )

    Box(
        modifier = modifier
            .size(size)
            .neumorphicRaised(
                shape = shape,
                cornerRadius = size / 2,
                elevation = elevation,
                isPressed = isPressed || isSelected,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.9f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.5f),
            )
            .clip(shape)
            .background(if (isSelected) neu.accent.copy(alpha = 0.15f) else neu.surface)
            .border(
                width = 1.dp,
                color = if (isSelected) neu.accent else if (neu.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.75f),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Slide-to-Connect Neumorphic Component.
 * - Distinct gesture to Connect (drag thumb to right end).
 * - Distinct gesture to Disconnect (drag thumb to left end / distinct slide).
 * - Independent visual feedback and animations for Ready, Connecting, Connected, Disconnecting, Error.
 */
@Composable
fun NeumorphicSlideToConnect(
    state: ConnectionState,
    lastError: String?,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val trackHeight = 64.dp
    val thumbSize = 54.dp
    val trackPadding = 5.dp
    val thumbRadius = 27.dp
    val trackCornerRadius = 32.dp

    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val maxDragPx = remember(totalWidthPx, density) {
        with(density) {
            (totalWidthPx - (thumbSize + trackPadding * 2).toPx()).coerceAtLeast(0f)
        }
    }

    // Drag offset in px
    val offsetX = remember { Animatable(0f) }

    var isDisconnecting by remember { mutableStateOf(false) }

    // When connection state changes, reset or position thumb
    LaunchedEffect(state, maxDragPx) {
        if (state != ConnectionState.CONNECTED) {
            isDisconnecting = false
        }
        if (maxDragPx > 0f) {
            when (state) {
                ConnectionState.CONNECTED -> {
                    offsetX.animateTo(maxDragPx, spring(stiffness = 300f))
                }
                ConnectionState.CONNECTING -> {
                    offsetX.animateTo(maxDragPx, spring(stiffness = 300f))
                }
                ConnectionState.DISCONNECTED -> {
                    offsetX.animateTo(0f, spring(stiffness = 400f))
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "slide-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    val hasError = state == ConnectionState.DISCONNECTED && !lastError.isNullOrBlank()
    val errorColor = MaterialTheme.colorScheme.error

    val trackBgColor = when {
        isDisconnecting -> statusColors.connecting.copy(alpha = 0.14f)
        state == ConnectionState.CONNECTED -> statusColors.connected.copy(alpha = 0.12f)
        state == ConnectionState.CONNECTING -> statusColors.connecting.copy(alpha = 0.15f)
        hasError -> errorColor.copy(alpha = 0.10f)
        else -> neu.surface
    }

    val thumbAccentColor = when {
        isDisconnecting -> statusColors.connecting
        state == ConnectionState.CONNECTED -> statusColors.connected
        state == ConnectionState.CONNECTING -> statusColors.connecting
        hasError -> errorColor
        else -> neu.accent
    }

    val trackShape = RoundedCornerShape(trackCornerRadius)
    val thumbShape = CircleShape

    val dragProgress = if (maxDragPx > 0f) (offsetX.value / maxDragPx).coerceIn(0f, 1f) else 0f

    val dragModifier = Modifier.draggable(
        orientation = Orientation.Horizontal,
        enabled = state != ConnectionState.CONNECTING && !isDisconnecting,
        state = rememberDraggableState { delta ->
            if (maxDragPx > 0f) {
                coroutineScope.launch {
                    val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragPx)
                    offsetX.snapTo(newOffset)
                }
            }
        },
        onDragStopped = {
            if (maxDragPx > 0f) {
                when (state) {
                    ConnectionState.DISCONNECTED -> {
                        if (offsetX.value >= maxDragPx * 0.55f) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            offsetX.animateTo(maxDragPx, spring(stiffness = 500f))
                            onRequestConnect()
                            kotlinx.coroutines.delay(350)
                            if (state == ConnectionState.DISCONNECTED) {
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                            }
                        } else {
                            offsetX.animateTo(0f, spring(stiffness = 500f))
                        }
                    }
                    ConnectionState.CONNECTED -> {
                        if (offsetX.value <= maxDragPx * 0.45f) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            isDisconnecting = true
                            offsetX.animateTo(0f, spring(stiffness = 500f))
                            onDisconnect()
                            kotlinx.coroutines.delay(400)
                            isDisconnecting = false
                        } else {
                            offsetX.animateTo(maxDragPx, spring(stiffness = 500f))
                        }
                    }
                    ConnectionState.CONNECTING -> {
                        // During connecting, allow tap to cancel
                    }
                }
            }
        },
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .onSizeChanged { totalWidthPx = it.width.toFloat() }
            .then(dragModifier)
            .neumorphicRaised(
                shape = trackShape,
                cornerRadius = trackCornerRadius,
                elevation = 5.dp,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.85f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.45f),
            )
            .clip(trackShape)
            .background(neu.surface)
            .border(
                width = 1.dp,
                color = when {
                    hasError -> errorColor.copy(alpha = 0.45f)
                    neu.isDark -> Color.White.copy(alpha = 0.05f)
                    else -> Color.White.copy(alpha = 0.65f)
                },
                shape = trackShape,
            )
            .neumorphicSunken(
                shape = trackShape,
                cornerRadius = trackCornerRadius,
                depth = 3.dp,
                surfaceColor = trackBgColor,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track text and guidance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isDisconnecting -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp),
                            color = statusColors.connecting,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Disconnecting\u2026",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColors.connecting,
                        )
                    }
                }
                state == ConnectionState.DISCONNECTED -> {
                    val labelAlpha = (1f - dragProgress * 1.4f).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 52.dp)
                            .graphicsLayer { alpha = labelAlpha },
                    ) {
                        Text(
                            text = if (hasError) "Slide to Retry Connection" else "Slide to Connect",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                            ),
                            color = if (hasError) errorColor else neu.textPrimary.copy(alpha = 0.78f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = (if (hasError) errorColor else neu.accent).copy(alpha = pulseAlpha),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                state == ConnectionState.CONNECTING -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 52.dp)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDisconnect()
                            },
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp),
                            color = statusColors.connecting,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Connecting\u2026 (Tap to Cancel)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColors.connecting,
                        )
                    }
                }
                state == ConnectionState.CONNECTED -> {
                    val labelAlpha = (dragProgress * 1.4f - 0.4f).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 52.dp)
                            .graphicsLayer { alpha = labelAlpha },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = statusColors.connected.copy(alpha = pulseAlpha),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Slide left to Disconnect",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                            ),
                            color = statusColors.connected,
                        )
                    }
                }
            }
        }

        // Draggable / Tappable Thumb
        val currentOffsetPx = offsetX.value

        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffsetPx.roundToInt(), 0) }
                .padding(trackPadding)
                .size(thumbSize)
                .neumorphicRaised(
                    shape = thumbShape,
                    cornerRadius = thumbRadius,
                    elevation = 6.dp,
                    lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.4f else 0.95f),
                    darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.8f else 0.55f),
                )
                .clip(thumbShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            thumbAccentColor.copy(alpha = 0.95f),
                            thumbAccentColor,
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.45f),
                    shape = thumbShape,
                )
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    when (state) {
                        ConnectionState.DISCONNECTED -> {
                            coroutineScope.launch {
                                if (maxDragPx > 0f) {
                                    offsetX.animateTo(maxDragPx, spring(stiffness = 400f))
                                }
                                onRequestConnect()
                                kotlinx.coroutines.delay(350)
                                if (state == ConnectionState.DISCONNECTED) {
                                    offsetX.animateTo(0f, spring(stiffness = 400f))
                                }
                            }
                        }
                        ConnectionState.CONNECTED -> {
                            coroutineScope.launch {
                                isDisconnecting = true
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                                onDisconnect()
                                kotlinx.coroutines.delay(400)
                                isDisconnecting = false
                            }
                        }
                        ConnectionState.CONNECTING -> {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                                onDisconnect()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                isDisconnecting || state == ConnectionState.CONNECTING -> {
                    CircularProgressIndicator(
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                    )
                }
                state == ConnectionState.DISCONNECTED -> {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Slide to connect",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                state == ConnectionState.CONNECTED -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Connected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
