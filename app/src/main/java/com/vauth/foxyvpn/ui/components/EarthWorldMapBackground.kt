package com.vauth.foxyvpn.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.ui.theme.LuxuryAmber
import com.vauth.foxyvpn.ui.theme.LuxuryEmerald
import com.vauth.foxyvpn.ui.theme.LuxuryOrange
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight
import kotlin.math.sin

/**
 * Procedural Earth World Map Canvas with stylized cyber continental dots,
 * radiating planetary glow, server location nodes, and pulsing flight arcs.
 * Exactly matches the Earth world map background shown in Screenshots 1, 2, and 3.
 */
@Composable
fun EarthWorldMapBackground(
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_anim")

    // Pulse animation for server nodes and flight arcs
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_anim",
    )

    // Moving pulse along flight arcs
    val arcProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "arc_progress",
    )

    // Geographical continent dot clusters normalized to (0..1, 0..1)
    // Normalized lon (0 = 180W, 1 = 180E), lat (0 = 85N, 1 = 60S)
    val continentPoints = remember {
        generateWorldMapPoints()
    }

    // Key Global Server Hubs (Coordinates normalized)
    val serverHubs = remember {
        listOf(
            Offset(0.25f, 0.35f), // North America (US East / New York)
            Offset(0.18f, 0.38f), // US West (California)
            Offset(0.50f, 0.30f), // Europe (Frankfurt / London)
            Offset(0.53f, 0.28f), // Europe (Amsterdam / Stockholm)
            Offset(0.78f, 0.40f), // Asia (Tokyo)
            Offset(0.72f, 0.54f), // Southeast Asia (Singapore)
            Offset(0.85f, 0.72f), // Oceania (Sydney)
            Offset(0.32f, 0.65f), // South America (São Paulo)
            Offset(0.55f, 0.52f), // Middle East (Dubai)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val isConnected = state == ConnectionState.CONNECTED
        val isConnecting = state == ConnectionState.CONNECTING

        // Warm radial bloom radiating from behind the center power button
        val centerBloomOffset = Offset(w / 2f, h * 0.54f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isConnected) LuxuryOrange.copy(alpha = 0.32f)
                    else if (isConnecting) LuxuryAmber.copy(alpha = 0.22f)
                    else Color(0x18FE592A),
                    Color(0x08FE592A),
                    Color.Transparent,
                ),
                center = centerBloomOffset,
                radius = w * 0.75f,
            ),
            radius = w * 0.75f,
            center = centerBloomOffset,
        )

        // Subtle planetary horizon arc below the centerpiece
        val horizonCenter = Offset(w / 2f, h * 0.88f)
        val horizonRadius = w * 0.95f
        drawArc(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    (if (isConnected) LuxuryOrange else Color(0xFF38231C)).copy(alpha = 0.35f),
                    (if (isConnected) LuxuryOrangeLight else Color(0xFF4C3026)).copy(alpha = 0.65f),
                    (if (isConnected) LuxuryOrange else Color(0xFF38231C)).copy(alpha = 0.35f),
                    Color.Transparent,
                ),
            ),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(horizonCenter.x - horizonRadius, horizonCenter.y - horizonRadius),
            size = androidx.compose.ui.geometry.Size(horizonRadius * 2f, horizonRadius * 2f),
            style = Stroke(width = if (isConnected) 2.5f else 1.5f),
        )

        // Map projection bounding box on screen
        val mapTop = h * 0.18f
        val mapHeight = h * 0.58f
        val mapLeft = 16f
        val mapWidth = w - 32f

        // Draw continental dots
        val baseDotColor = if (isConnected) Color(0xFF3E3E54) else Color(0xFF242432)
        val highlightedDotColor = if (isConnected) LuxuryOrange.copy(alpha = 0.75f) else Color(0xFF5A453C)

        continentPoints.forEach { pt ->
            val px = mapLeft + pt.x * mapWidth
            val py = mapTop + pt.y * mapHeight

            // Distance to center to give gentle wave highlight
            val distToCenter = (Offset(px, py) - centerBloomOffset).getDistance()
            val isNearCenter = distToCenter < w * 0.38f

            val dotColor = if (isNearCenter && isConnected) highlightedDotColor else baseDotColor
            val radius = if (isNearCenter && isConnected) 1.6f else 1.25f

            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(px, py),
            )
        }

        // Draw curved flight arcs / connection lines between key hubs
        val hubPairs = listOf(
            Pair(0, 2), // US East -> Europe
            Pair(1, 4), // US West -> Tokyo
            Pair(2, 4), // Europe -> Tokyo
            Pair(2, 5), // Europe -> Singapore
            Pair(5, 6), // Singapore -> Sydney
            Pair(0, 7), // US East -> South America
            Pair(2, 8), // Europe -> Dubai
        )

        val arcStrokeColor = if (isConnected) {
            LuxuryOrange.copy(alpha = 0.45f)
        } else if (isConnecting) {
            LuxuryAmber.copy(alpha = 0.3f)
        } else {
            Color(0xFF3E2D24).copy(alpha = 0.35f)
        }

        hubPairs.forEachIndexed { index, pair ->
            val p1 = serverHubs[pair.first]
            val p2 = serverHubs[pair.second]

            val start = Offset(mapLeft + p1.x * mapWidth, mapTop + p1.y * mapHeight)
            val end = Offset(mapLeft + p2.x * mapWidth, mapTop + p2.y * mapHeight)

            // Curved quadratic bezier arc
            val control = Offset(
                (start.x + end.x) / 2f,
                (start.y + end.y) / 2f - 35f - (index % 3) * 12f,
            )

            val path = Path().apply {
                moveTo(start.x, start.y)
                quadraticTo(control.x, control.y, end.x, end.y)
            }

            drawPath(
                path = path,
                color = arcStrokeColor,
                style = Stroke(
                    width = if (isConnected) 1.4f else 0.8f,
                    cap = StrokeCap.Round,
                ),
            )

            // Draw animated traveling pulse along arc
            if (isConnected) {
                val t = (arcProgress + (index * 0.17f)) % 1f
                val u = 1f - t
                // Quadratic bezier point: B(t) = (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
                val bx = u * u * start.x + 2f * u * t * control.x + t * t * end.x
                val by = u * u * start.y + 2f * u * t * control.y + t * t * end.y

                drawCircle(
                    color = LuxuryOrangeLight,
                    radius = 2.2f,
                    center = Offset(bx, by),
                )
            }
        }

        // Draw Server Hubs
        serverHubs.forEachIndexed { i, hub ->
            val hx = mapLeft + hub.x * mapWidth
            val hy = mapTop + hub.y * mapHeight
            val hubPos = Offset(hx, hy)

            val isPrimaryNode = i == 0 || i == 2 || i == 4

            // Pulsing halo
            if (isConnected && isPrimaryNode) {
                drawCircle(
                    color = LuxuryOrange.copy(alpha = 0.35f * (2f - pulseAnim).coerceIn(0.1f, 1f)),
                    radius = 6f * pulseAnim,
                    center = hubPos,
                )
            }

            // Core hub point
            val hubColor = when {
                isConnected && isPrimaryNode -> LuxuryEmerald
                isConnected -> LuxuryOrange
                isConnecting -> LuxuryAmber
                else -> Color(0xFF6B584E)
            }

            drawCircle(
                color = hubColor,
                radius = if (isPrimaryNode) 3.2f else 2.4f,
                center = hubPos,
            )
        }
    }
}

/**
 * Generates coordinate points representing continents in a simplified,
 * aesthetic cyber dot-matrix projection.
 */
private fun generateWorldMapPoints(): List<Offset> {
    val list = mutableListOf<Offset>()

    fun addRegion(xRange: ClosedFloatingPointRange<Float>, yRange: ClosedFloatingPointRange<Float>, density: Int, filter: (Float, Float) -> Boolean) {
        val xStep = (xRange.endInclusive - xRange.start) / density
        val yStep = (yRange.endInclusive - yRange.start) / density
        var y = yRange.start
        while (y <= yRange.endInclusive) {
            var x = xRange.start
            while (x <= xRange.endInclusive) {
                if (filter(x, y)) {
                    list.add(Offset(x, y))
                }
                x += xStep
            }
            y += yStep
        }
    }

    // North America
    addRegion(0.12f..0.36f, 0.18f..0.45f, 16) { x, y ->
        val inAlaska = x < 0.20f && y < 0.28f
        val inCanada = x in 0.18f..0.34f && y in 0.18f..0.30f
        val inUs = x in 0.16f..0.35f && y in 0.30f..0.42f
        val inMex = x in 0.20f..0.28f && y in 0.42f..0.48f
        inAlaska || inCanada || inUs || inMex
    }

    // South America
    addRegion(0.24f..0.38f, 0.48f..0.78f, 14) { x, y ->
        val widthAtY = when {
            y < 0.58f -> 0.12f
            y < 0.68f -> 0.08f
            else -> 0.04f
        }
        val centerX = 0.31f
        x in (centerX - widthAtY)..(centerX + widthAtY)
    }

    // Europe
    addRegion(0.44f..0.60f, 0.18f..0.38f, 14) { x, y ->
        val inScandinavia = x in 0.49f..0.55f && y in 0.16f..0.24f
        val inWestEurope = x in 0.44f..0.54f && y in 0.24f..0.36f
        val inEastEurope = x in 0.54f..0.62f && y in 0.22f..0.35f
        inScandinavia || inWestEurope || inEastEurope
    }

    // Africa
    addRegion(0.44f..0.62f, 0.36f..0.72f, 16) { x, y ->
        val inNorth = x in 0.44f..0.62f && y in 0.36f..0.48f
        val inCentral = x in 0.48f..0.60f && y in 0.48f..0.60f
        val inSouth = x in 0.50f..0.57f && y in 0.60f..0.72f
        inNorth || inCentral || inSouth
    }

    // Asia
    addRegion(0.58f..0.88f, 0.18f..0.52f, 20) { x, y ->
        val inRussia = x in 0.58f..0.86f && y in 0.16f..0.30f
        val inCentralAsia = x in 0.58f..0.72f && y in 0.30f..0.42f
        val inChinaEastAsia = x in 0.70f..0.86f && y in 0.30f..0.48f
        val inIndia = x in 0.64f..0.72f && y in 0.40f..0.54f
        val inJapan = x in 0.84f..0.88f && y in 0.32f..0.42f
        inRussia || inCentralAsia || inChinaEastAsia || inIndia || inJapan
    }

    // Southeast Asia & Indonesia
    addRegion(0.72f..0.86f, 0.52f..0.62f, 8) { x, y ->
        sin(x * 40f) * sin(y * 40f) > 0.1f
    }

    // Australia & New Zealand
    addRegion(0.78f..0.92f, 0.64f..0.80f, 12) { x, y ->
        val inOz = x in 0.78f..0.89f && y in 0.64f..0.78f
        val inNz = x in 0.90f..0.93f && y in 0.74f..0.82f
        inOz || inNz
    }

    return list
}
