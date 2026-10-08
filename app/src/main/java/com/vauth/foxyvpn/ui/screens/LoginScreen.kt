package com.vauth.foxyvpn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.ui.theme.LuxuryDarkBg
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCard
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCardBorder
import com.vauth.foxyvpn.ui.theme.LuxuryOrange
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(
    authRepository: FxaAuthRepository,
    onSignedIn: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var awaitingTwoFactor by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAuthModal by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBg),
    ) {
        // World Map & Planet Horizon Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glowing Planet Curve at bottom
            val planetCenter = Offset(w / 2f, h * 0.96f)
            val planetRadius = w * 0.85f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LuxuryOrange.copy(alpha = 0.45f),
                        LuxuryOrangeLight.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                    center = planetCenter,
                    radius = planetRadius * 1.2f,
                ),
                radius = planetRadius * 1.2f,
                center = planetCenter,
            )

            // Sharp glowing horizon arc of planet (from Screenshot 2)
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFF8A55),
                        Color(0xFFFE592A),
                        Color(0xFFFF8A55),
                        Color.Transparent,
                    ),
                ),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(planetCenter.x - planetRadius, planetCenter.y - planetRadius),
                size = androidx.compose.ui.geometry.Size(planetRadius * 2f, planetRadius * 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f),
            )

            // Dotted World Map Stylized Matrix
            val rows = 35
            val cols = 45
            val startY = h * 0.22f
            val endY = h * 0.65f
            val rowSpacing = (endY - startY) / rows
            val colSpacing = w / cols

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val x = c * colSpacing
                    val y = startY + r * rowSpacing
                    // Pseudo-continents shape density
                    val wave = sin(x / w * 5f) * cos(y / h * 8f)
                    if (wave > 0.15f) {
                        drawCircle(
                            color = Color(0xFF262638).copy(alpha = (0.25f + wave * 0.4f).coerceIn(0.1f, 0.6f)),
                            radius = 1.3f,
                            center = Offset(x, y),
                        )
                    }
                }
            }
        }

        // Onboarding Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header Content
            Column(modifier = Modifier.padding(top = 40.dp)) {
                Text(
                    text = "TRUSTED BY",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "MILLIONS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = LuxuryOrangeLight,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "USERS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "You can access 100+ country and 4000+ city on just one-click. You can change your locations anytime from anywhere.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF8E8EA4),
                )
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Sign Up / Continue Pill Button
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAuthModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryOrange),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                // Login Glass Pill Button
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAuthModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF161622),
                    border = BorderStroke(1.dp, Color(0xFF2C2C3E)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }

                // Quick Guest / Instant Access Button
                TextButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSignedIn()
                    },
                ) {
                    Text(
                        text = "Continue as Guest \u2192",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = LuxuryOrangeLight,
                    )
                }
            }
        }

        // Sliding Authentication Modal Sheet
        AnimatedVisibility(
            visible = showAuthModal,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showAuthModal = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {} // consume clicks
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF214141E)),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                    ) {
                        // iOS drag handle bar
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFF6B6B7F), CircleShape)
                                .align(Alignment.CenterHorizontally),
                        )
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (awaitingTwoFactor) "Two-Factor Auth" else "Sign In to FoxyVPN",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            IconButton(onClick = { showAuthModal = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF88889C))
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (awaitingTwoFactor) {
                                "Enter the confirmation code sent to your email"
                            } else {
                                "Use your Firefox or FoxyVPN credentials for 50GB high-speed monthly VPN traffic."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF88889C),
                        )

                        Spacer(Modifier.height(18.dp))

                        if (!awaitingTwoFactor) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Account email") },
                                leadingIcon = { Icon(Icons.Filled.Mail, null, tint = LuxuryOrange) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryOrange,
                                    unfocusedBorderColor = Color(0xFF2E2E3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                ),
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = LuxuryOrange) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryOrange,
                                    unfocusedBorderColor = Color(0xFF2E2E3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                ),
                            )

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch {
                                        val result = authRepository.startLogin(email, password)
                                        isLoading = false
                                        result.onSuccess { needsVerification ->
                                            if (needsVerification) awaitingTwoFactor = true else onSignedIn()
                                        }.onFailure {
                                            errorMessage = it.message ?: "Sign-in failed. Please verify credentials."
                                        }
                                    }
                                },
                                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryOrange),
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = { Text("Confirmation Code") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxuryOrange,
                                    unfocusedBorderColor = Color(0xFF2E2E3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                ),
                            )

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch {
                                        val result = authRepository.submitTwoFactorCode(code)
                                        isLoading = false
                                        result.onSuccess { onSignedIn() }
                                            .onFailure { errorMessage = it.message ?: "Verification failed" }
                                    }
                                },
                                enabled = !isLoading && code.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryOrange),
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        errorMessage?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it, color = Color(0xFFFF5252), fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Fast Demo Skip inside modal
                        TextButton(
                            onClick = { onSignedIn() },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text("Skip & Try Free Demo \u2192", color = LuxuryOrangeLight, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
