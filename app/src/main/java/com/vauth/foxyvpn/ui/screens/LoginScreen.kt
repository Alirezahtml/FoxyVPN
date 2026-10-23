package com.vauth.foxyvpn.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.ui.theme.NeumorphicButton
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.shakeOnTrigger
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import com.vauth.foxyvpn.ui.theme.subtleFloatAnimation
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: FxaAuthRepository,
    onSignedIn: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var awaitingTwoFactor by rememberSaveable { mutableStateOf(authRepository.pendingSessionToken != null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val neu = currentNeumorphicColors

    BackHandler(enabled = awaitingTwoFactor) {
        authRepository.clearPendingSession()
        awaitingTwoFactor = false
        errorMessage = null
        code = ""
    }

    fun doSubmitLogin() {
        val cleanEmail = FxaAuthRepository.normalizeEmail(email)
        if (cleanEmail.isBlank() || password.isBlank() || isLoading) return
        errorMessage = null
        isLoading = true
        scope.launch {
            val result = authRepository.startLogin(cleanEmail, password)
            isLoading = false
            result.onSuccess { needsVerification ->
                if (needsVerification) awaitingTwoFactor = true else onSignedIn()
            }.onFailure { errorMessage = it.message ?: "Sign-in failed" }
        }
    }

    fun doSubmitTwoFactor() {
        val cleanCode = FxaAuthRepository.sanitizeCode(code)
        if (cleanCode.isBlank() || isLoading) return
        errorMessage = null
        isLoading = true
        scope.launch {
            val result = authRepository.submitTwoFactorCode(cleanCode)
            isLoading = false
            result.onSuccess { onSignedIn() }
                .onFailure {
                    errorMessage = it.message ?: "Verification failed"
                    if (authRepository.pendingSessionToken == null) {
                        awaitingTwoFactor = false
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neu.background)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Neumorphic Logo Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .subtleFloatAnimation()
                    .staggeredEntrance(0)
                    .clip(CircleShape)
                    .background(neu.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = neu.accent,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Sign in to FoxyVPN",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Connect with your Firefox account to utilize 50 GB of fast, encrypted monthly VPN traffic.",
                style = MaterialTheme.typography.bodyMedium,
                color = neu.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            Spacer(Modifier.height(28.dp))

            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1)
                    .shakeOnTrigger(errorMessage),
                cornerRadius = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    if (!awaitingTwoFactor) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it.replace(Regex("[\\p{Z}\\u200B-\\u200D\\uFEFF\\u00A0\\s]+"), "").lowercase()
                            },
                            label = { Text("Firefox Account Email") },
                            leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null, tint = neu.textSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                autoCorrectEnabled = false,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it.replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "")
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = neu.textSecondary) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    doSubmitLogin()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(24.dp))

                        NeumorphicButton(
                            onClick = {
                                focusManager.clearFocus()
                                doSubmitLogin()
                            },
                            accentColor = neu.accent,
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(
                                    "Continue",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                        }
                    } else {
                        Text(
                            "Enter the confirmation code sent to your email",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = neu.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                code = it.replace(Regex("[\\p{Z}\\u200B-\\u200D\\uFEFF\\u00A0\\s]+"), "")
                            },
                            label = { Text("6-Digit Code") },
                            leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null, tint = neu.textSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    doSubmitTwoFactor()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(24.dp))

                        NeumorphicButton(
                            onClick = {
                                focusManager.clearFocus()
                                doSubmitTwoFactor()
                            },
                            accentColor = neu.accent,
                            enabled = !isLoading && code.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(
                                    "Verify & Sign In",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    errorMessage?.let {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
