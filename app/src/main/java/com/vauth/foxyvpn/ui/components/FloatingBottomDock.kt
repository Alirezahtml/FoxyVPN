package com.vauth.foxyvpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.vauth.foxyvpn.ui.theme.LuxuryOrange

enum class BottomNavItem(val icon: ImageVector, val label: String) {
    HOME(Icons.Filled.Home, "Home"),
    SERVERS(Icons.Filled.Public, "Servers"),
    SETTINGS(Icons.Filled.Tune, "Settings"),
    ACCOUNT(Icons.Filled.Person, "Account"),
}

/**
 * Pixel-accurate Floating Frosted Glass Bottom Dock from the design screenshots.
 * High-blur dark translucent capsule, illuminated orange active indicator,
 * and fluid spring physics.
 */
@Composable
fun FloatingBottomDock(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xDF12121C),
            border = BorderStroke(1.dp, Color(0x38FFFFFF)),
            shadowElevation = 18.dp,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem.entries.forEach { item ->
                    val isSelected = item == selectedItem
                    val interactionSource = remember { MutableInteractionSource() }

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) LuxuryOrange else Color.Transparent,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                        label = "pill_color",
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0xFF88889C),
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                        label = "icon_tint",
                    )

                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 56.dp else 44.dp,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                        label = "pill_width",
                    )

                    Box(
                        modifier = Modifier
                            .width(pillWidth)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                if (!isSelected) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onItemSelected(item)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
