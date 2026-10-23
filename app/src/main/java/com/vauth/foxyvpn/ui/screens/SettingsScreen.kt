package com.vauth.foxyvpn.ui.screens

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.BuildConfig
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.quicksettings.DynamicIslandOverlayService
import com.vauth.foxyvpn.ui.theme.AppLanguage
import com.vauth.foxyvpn.ui.theme.LanguageController
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.ThemeController
import com.vauth.foxyvpn.ui.theme.ThemeMode
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GITHUB_URL = "https://github.com/vauth/foxyvpn"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    themeController: ThemeController,
    languageController: LanguageController,
    onOpenLogs: () -> Unit,
    onOpenTools: (Int) -> Unit = {},
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings

    var exitCheckEnabled by remember { mutableStateOf(settingsStore.exitCheckEnabled) }
    var dnsProvider by remember { mutableStateOf(settingsStore.dnsProvider) }
    var customDohUrl by remember { mutableStateOf(settingsStore.customDohUrl) }
    var bootstrapDnsProvider by remember { mutableStateOf(settingsStore.bootstrapDnsProvider) }
    var customBootstrapIp by remember { mutableStateOf(settingsStore.customBootstrapIp) }
    var socksBindAddress by remember { mutableStateOf(settingsStore.socksBindAddress) }
    var socksPort by remember { mutableStateOf(settingsStore.socksPort) }
    var httpProxyEnabled by remember { mutableStateOf(settingsStore.httpProxyEnabled) }
    var httpProxyPort by remember { mutableStateOf(settingsStore.httpProxyPort) }
    var proxyOnlyMode by remember { mutableStateOf(settingsStore.proxyOnlyMode) }
    var customEdgeAddress by remember { mutableStateOf(settingsStore.customEdgeAddress) }
    var upstreamProxyEnabled by remember { mutableStateOf(settingsStore.upstreamProxyEnabled) }
    var upstreamProxyType by remember { mutableStateOf(settingsStore.upstreamProxyType) }
    var upstreamProxyHost by remember { mutableStateOf(settingsStore.upstreamProxyHost) }
    var upstreamProxyPort by remember { mutableStateOf(settingsStore.upstreamProxyPort) }
    var upstreamProxyUsername by remember { mutableStateOf(settingsStore.upstreamProxyUsername) }
    var upstreamProxyPassword by remember { mutableStateOf(settingsStore.upstreamProxyPassword) }
    var excludedApps by remember { mutableStateOf(settingsStore.excludedApps) }
    var mtu by remember { mutableStateOf(settingsStore.mtu) }
    var tlsSplitMode by remember { mutableStateOf(settingsStore.tlsSplitMode) }

    // Dynamic Island State
    var dynamicIslandEnabled by remember { mutableStateOf(settingsStore.dynamicIslandEnabled) }
    var dynamicIslandOverlayEnabled by remember { mutableStateOf(settingsStore.dynamicIslandOverlayEnabled) }
    var dynamicIslandSize by remember { mutableStateOf(settingsStore.dynamicIslandSize) }
    var dynamicIslandCornerRadius by remember { mutableStateOf(settingsStore.dynamicIslandCornerRadius) }
    var dynamicIslandPosition by remember { mutableStateOf(settingsStore.dynamicIslandPosition) }
    var dynamicIslandTheme by remember { mutableStateOf(settingsStore.dynamicIslandTheme) }
    var dynamicIslandShowSpeed by remember { mutableStateOf(settingsStore.dynamicIslandShowSpeed) }
    var dynamicIslandShowPing by remember { mutableStateOf(settingsStore.dynamicIslandShowPing) }
    var dynamicIslandShowFlag by remember { mutableStateOf(settingsStore.dynamicIslandShowFlag) }
    var dynamicIslandShowTimer by remember { mutableStateOf(settingsStore.dynamicIslandShowTimer) }
    var dynamicIslandAnimation by remember { mutableStateOf(settingsStore.dynamicIslandAnimation) }

    // Dialog toggles
    var showDnsProviderDialog by remember { mutableStateOf(false) }
    var showBootstrapDnsDialog by remember { mutableStateOf(false) }
    var showMtuDialog by remember { mutableStateOf(false) }
    var showSocksBindDialog by remember { mutableStateOf(false) }
    var showSocksPortDialog by remember { mutableStateOf(false) }
    var showHttpPortDialog by remember { mutableStateOf(false) }
    var showEdgeAddressDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyTypeDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyAddressDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyCredentialsDialog by remember { mutableStateOf(false) }
    var showSplitTunnelDialog by remember { mutableStateOf(false) }
    var showTlsSplitDialog by remember { mutableStateOf(false) }
    var showIslandSizeDialog by remember { mutableStateOf(false) }
    var showIslandCornerRadiusDialog by remember { mutableStateOf(false) }
    var showIslandPositionDialog by remember { mutableStateOf(false) }
    var showIslandThemeDialog by remember { mutableStateOf(false) }

    if (showIslandCornerRadiusDialog) {
        DynamicIslandCornerRadiusDialog(
            current = dynamicIslandCornerRadius,
            onDismiss = { showIslandCornerRadiusDialog = false },
            onConfirm = {
                dynamicIslandCornerRadius = it
                settingsStore.dynamicIslandCornerRadius = it
                showIslandCornerRadiusDialog = false
            },
        )
    }

    if (showIslandSizeDialog) {
        DynamicIslandSizeDialog(
            current = dynamicIslandSize,
            onDismiss = { showIslandSizeDialog = false },
            onConfirm = {
                dynamicIslandSize = it
                settingsStore.dynamicIslandSize = it
                showIslandSizeDialog = false
            },
        )
    }

    if (showIslandPositionDialog) {
        DynamicIslandPositionDialog(
            current = dynamicIslandPosition,
            onDismiss = { showIslandPositionDialog = false },
            onConfirm = {
                dynamicIslandPosition = it
                settingsStore.dynamicIslandPosition = it
                showIslandPositionDialog = false
            },
        )
    }

    if (showIslandThemeDialog) {
        DynamicIslandThemeDialog(
            current = dynamicIslandTheme,
            onDismiss = { showIslandThemeDialog = false },
            onConfirm = {
                dynamicIslandTheme = it
                settingsStore.dynamicIslandTheme = it
                showIslandThemeDialog = false
            },
        )
    }

    if (showMtuDialog) {
        MtuPickerDialog(
            current = mtu,
            onDismiss = { showMtuDialog = false },
            onConfirm = {
                mtu = it
                settingsStore.mtu = it
                showMtuDialog = false
            },
        )
    }

    if (showDnsProviderDialog) {
        DnsProviderPickerDialog(
            currentProvider = dnsProvider,
            currentCustomUrl = customDohUrl,
            onDismiss = { showDnsProviderDialog = false },
            onConfirm = { provider, url ->
                dnsProvider = provider
                settingsStore.dnsProvider = provider
                customDohUrl = url
                settingsStore.customDohUrl = url
                showDnsProviderDialog = false
            },
        )
    }
    if (showBootstrapDnsDialog) {
        BootstrapDnsPickerDialog(
            currentProvider = bootstrapDnsProvider,
            currentCustomIp = customBootstrapIp,
            onDismiss = { showBootstrapDnsDialog = false },
            onConfirm = { provider, ip ->
                bootstrapDnsProvider = provider
                settingsStore.bootstrapDnsProvider = provider
                customBootstrapIp = ip
                settingsStore.customBootstrapIp = ip
                showBootstrapDnsDialog = false
            },
        )
    }
    if (showSocksBindDialog) {
        SocksBindAddressPickerDialog(
            current = socksBindAddress,
            onDismiss = { showSocksBindDialog = false },
            onConfirm = {
                socksBindAddress = it
                settingsStore.socksBindAddress = it
                showSocksBindDialog = false
            },
        )
    }
    if (showSocksPortDialog) {
        SocksPortPickerDialog(
            current = socksPort,
            onDismiss = { showSocksPortDialog = false },
            onConfirm = {
                socksPort = it
                settingsStore.socksPort = it
                showSocksPortDialog = false
            },
        )
    }
    if (showHttpPortDialog) {
        HttpPortPickerDialog(
            current = httpProxyPort,
            onDismiss = { showHttpPortDialog = false },
            onConfirm = {
                httpProxyPort = it
                settingsStore.httpProxyPort = it
                showHttpPortDialog = false
            },
        )
    }
    if (showEdgeAddressDialog) {
        EdgeAddressPickerDialog(
            current = customEdgeAddress,
            onDismiss = { showEdgeAddressDialog = false },
            onConfirm = {
                customEdgeAddress = it
                settingsStore.customEdgeAddress = it
                showEdgeAddressDialog = false
            },
        )
    }
    if (showUpstreamProxyTypeDialog) {
        UpstreamProxyTypeDialog(
            current = upstreamProxyType,
            onDismiss = { showUpstreamProxyTypeDialog = false },
            onConfirm = {
                upstreamProxyType = it
                settingsStore.upstreamProxyType = it
                showUpstreamProxyTypeDialog = false
            },
        )
    }
    if (showUpstreamProxyAddressDialog) {
        UpstreamProxyAddressDialog(
            currentHost = upstreamProxyHost,
            currentPort = upstreamProxyPort,
            onDismiss = { showUpstreamProxyAddressDialog = false },
            onConfirm = { host, port ->
                upstreamProxyHost = host
                upstreamProxyPort = port
                settingsStore.upstreamProxyHost = host
                settingsStore.upstreamProxyPort = port
                showUpstreamProxyAddressDialog = false
            },
        )
    }
    if (showUpstreamProxyCredentialsDialog) {
        UpstreamProxyCredentialsDialog(
            currentUsername = upstreamProxyUsername,
            currentPassword = upstreamProxyPassword,
            onDismiss = { showUpstreamProxyCredentialsDialog = false },
            onConfirm = { username, password ->
                upstreamProxyUsername = username
                upstreamProxyPassword = password
                settingsStore.upstreamProxyUsername = username
                settingsStore.upstreamProxyPassword = password
                showUpstreamProxyCredentialsDialog = false
            },
        )
    }
    if (showSplitTunnelDialog) {
        SplitTunnelDialog(
            current = excludedApps,
            onDismiss = { showSplitTunnelDialog = false },
            onConfirm = {
                excludedApps = it
                settingsStore.excludedApps = it
                showSplitTunnelDialog = false
            },
        )
    }
    if (showTlsSplitDialog) {
        TlsSplitModePickerDialog(
            current = tlsSplitMode,
            onDismiss = { showTlsSplitDialog = false },
            onConfirm = {
                tlsSplitMode = it
                settingsStore.tlsSplitMode = it
                showTlsSplitDialog = false
            },
        )
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s.settingsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = neu.textPrimary,
                    )
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 42.dp,
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back,
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.widthIn(max = 600.dp)) {
                Column {
                    // 0. LANGUAGE & APPEARANCE SECTION
                    SectionHeading(s.sectionLanguageAndTheme)
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(0),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                s.appLanguageTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                s.appLanguageSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = neu.textSecondary,
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    val isSelected = languageController.language == lang
                                    val pillShape = RoundedCornerShape(14.dp)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .neumorphicTouchBounce()
                                            .let { mod ->
                                                if (isSelected) {
                                                    mod.clip(pillShape).background(neu.accent)
                                                } else {
                                                    mod.neumorphicSunken(
                                                        shape = pillShape,
                                                        cornerRadius = 14.dp,
                                                        depth = 3.dp,
                                                        surfaceColor = neu.surfaceElevated,
                                                    )
                                                }
                                            }
                                            .clickable { languageController.set(lang) }
                                            .padding(vertical = 12.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = if (isSelected) Color.White else neu.textPrimary,
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = neu.textSecondary.copy(alpha = 0.12f),
                                modifier = Modifier.padding(vertical = 16.dp),
                            )

                            Text(
                                s.appThemeTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary,
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val themeOptions = listOf(
                                    ThemeMode.SYSTEM to s.themeSystem,
                                    ThemeMode.LIGHT to s.themeLight,
                                    ThemeMode.DARK to s.themeDark,
                                )
                                themeOptions.forEach { (mode, label) ->
                                    val isSelected = themeController.mode == mode
                                    val pillShape = RoundedCornerShape(14.dp)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .neumorphicTouchBounce()
                                            .let { mod ->
                                                if (isSelected) {
                                                    mod.clip(pillShape).background(neu.accent)
                                                } else {
                                                    mod.neumorphicSunken(
                                                        shape = pillShape,
                                                        cornerRadius = 14.dp,
                                                        depth = 3.dp,
                                                        surfaceColor = neu.surfaceElevated,
                                                    )
                                                }
                                            }
                                            .clickable { themeController.set(mode) }
                                            .padding(vertical = 11.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = if (isSelected) Color.White else neu.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 1. SMART NETWORK & SECURITY TOOLKIT (5 OPTIONS)
                    SectionHeading(s.sectionSmartToolkit)
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(1),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            SettingClickableRow(
                                title = s.toolSpeedTitle,
                                value = s.toolSpeedSubtitle,
                                onClick = { onOpenTools(0) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.toolLeakTitle,
                                value = s.toolLeakSubtitle,
                                onClick = { onOpenTools(1) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.toolScannerTitle,
                                value = s.toolScannerSubtitle,
                                onClick = { onOpenTools(2) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.toolWatchdogTitle,
                                value = s.toolWatchdogSubtitle,
                                onClick = { onOpenTools(3) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.toolMtuTitle,
                                value = s.toolMtuSubtitle,
                                onClick = { onOpenTools(4) },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 2. DYNAMIC ISLAND SECTION
                    SectionHeading(s.sectionDynamicIsland)
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(2),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            SettingToggleRow(
                                title = s.enableDynamicIslandTitle,
                                subtitle = s.enableDynamicIslandSub,
                                checked = dynamicIslandEnabled,
                                onCheckedChange = {
                                    dynamicIslandEnabled = it
                                    settingsStore.dynamicIslandEnabled = it
                                },
                            )

                            if (dynamicIslandEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
                                SettingToggleRow(
                                    title = s.overlayIslandTitle,
                                    subtitle = if (hasOverlayPermission) s.overlayIslandSubGranted
                                    else s.overlayIslandSubNeedsPerm,
                                    checked = dynamicIslandOverlayEnabled && hasOverlayPermission,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}"),
                                                )
                                                context.startActivity(intent)
                                            } else {
                                                dynamicIslandOverlayEnabled = true
                                                settingsStore.dynamicIslandOverlayEnabled = true
                                                DynamicIslandOverlayService.start(context)
                                            }
                                        } else {
                                            dynamicIslandOverlayEnabled = false
                                            settingsStore.dynamicIslandOverlayEnabled = false
                                            DynamicIslandOverlayService.stop(context)
                                        }
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingClickableRow(
                                    title = s.islandSizeTitle,
                                    value = dynamicIslandSize.localizedLabel(s.isRtl),
                                    onClick = { showIslandSizeDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingClickableRow(
                                    title = s.islandCornerRadiusTitle,
                                    value = "${dynamicIslandCornerRadius} dp",
                                    onClick = { showIslandCornerRadiusDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingClickableRow(
                                    title = s.islandPositionTitle,
                                    value = dynamicIslandPosition.localizedLabel(s.isRtl),
                                    onClick = { showIslandPositionDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingClickableRow(
                                    title = s.islandThemeTitle,
                                    value = dynamicIslandTheme.localizedLabel(s.isRtl),
                                    onClick = { showIslandThemeDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingToggleRow(
                                    title = s.islandShowSpeedTitle,
                                    subtitle = s.islandShowSpeedSub,
                                    checked = dynamicIslandShowSpeed,
                                    onCheckedChange = {
                                        dynamicIslandShowSpeed = it
                                        settingsStore.dynamicIslandShowSpeed = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingToggleRow(
                                    title = s.islandShowPingTitle,
                                    subtitle = s.islandShowPingSub,
                                    checked = dynamicIslandShowPing,
                                    onCheckedChange = {
                                        dynamicIslandShowPing = it
                                        settingsStore.dynamicIslandShowPing = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingToggleRow(
                                    title = s.islandShowFlagTitle,
                                    subtitle = s.islandShowFlagSub,
                                    checked = dynamicIslandShowFlag,
                                    onCheckedChange = {
                                        dynamicIslandShowFlag = it
                                        settingsStore.dynamicIslandShowFlag = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingToggleRow(
                                    title = s.islandShowTimerTitle,
                                    subtitle = s.islandShowTimerSub,
                                    checked = dynamicIslandShowTimer,
                                    onCheckedChange = {
                                        dynamicIslandShowTimer = it
                                        settingsStore.dynamicIslandShowTimer = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                SettingToggleRow(
                                    title = s.islandAnimationTitle,
                                    subtitle = s.islandAnimationSub,
                                    checked = dynamicIslandAnimation,
                                    onCheckedChange = {
                                        dynamicIslandAnimation = it
                                        settingsStore.dynamicIslandAnimation = it
                                    },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 3. CONNECTION
                    SectionHeading(s.sectionConnection)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingToggleRow(
                                title = s.verifyExitTitle,
                                subtitle = s.verifyExitSub,
                                checked = exitCheckEnabled,
                                onCheckedChange = {
                                    exitCheckEnabled = it
                                    settingsStore.exitCheckEnabled = it
                                },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.tunnelMtuTitle,
                                value = s.mtuBytesValue(mtu),
                                onClick = { showMtuDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 4. DNS
                    SectionHeading(s.sectionDns)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingClickableRow(
                                title = s.tunnelDnsTitle,
                                value = when (dnsProvider) {
                                    SettingsStore.DnsProvider.CUSTOM -> customDohUrl.ifBlank { dnsProvider.localizedLabel(s.isRtl) }
                                    else -> dnsProvider.localizedLabel(s.isRtl)
                                },
                                onClick = { showDnsProviderDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.resolverDnsTitle,
                                value = when (bootstrapDnsProvider) {
                                    SettingsStore.BootstrapDnsProvider.CUSTOM -> customBootstrapIp.ifBlank { bootstrapDnsProvider.localizedLabel(s.isRtl) }
                                    else -> bootstrapDnsProvider.localizedLabel(s.isRtl)
                                },
                                onClick = { showBootstrapDnsDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 5. LOCAL PROXY
                    SectionHeading(s.sectionLocalProxy)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingToggleRow(
                                title = s.proxyOnlyModeTitle,
                                subtitle = s.proxyOnlyModeSub,
                                checked = proxyOnlyMode,
                                onCheckedChange = {
                                    proxyOnlyMode = it
                                    settingsStore.proxyOnlyMode = it
                                },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.localBindAddressTitle,
                                value = socksBindAddress,
                                onClick = { showSocksBindDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.socksPortTitle,
                                value = socksPort.toString(),
                                onClick = { showSocksPortDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingToggleRow(
                                title = s.enableHttpProxyTitle,
                                subtitle = s.enableHttpProxySub,
                                checked = httpProxyEnabled,
                                onCheckedChange = {
                                    httpProxyEnabled = it
                                    settingsStore.httpProxyEnabled = it
                                },
                            )
                            if (httpProxyEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                SettingClickableRow(
                                    title = s.httpProxyPortTitle,
                                    value = httpProxyPort.toString(),
                                    onClick = { showHttpPortDialog = true },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 6. SPLIT TUNNELING
                    SectionHeading(s.sectionSplitTunneling)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingClickableRow(
                                title = s.excludedAppsTitle,
                                value = if (excludedApps.isEmpty()) s.none else s.excludedAppsCount(excludedApps.size),
                                onClick = { showSplitTunnelDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 7. ADVANCED NETWORKING
                    SectionHeading(s.sectionAdvancedNetworking)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingClickableRow(
                                title = s.customEdgeAddressTitle,
                                value = customEdgeAddress.ifBlank { s.notSet },
                                onClick = { showEdgeAddressDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.tlsFragmentationTitle,
                                value = tlsSplitMode.localizedLabel(s.isRtl),
                                onClick = { showTlsSplitDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingToggleRow(
                                title = s.chainUpstreamProxyTitle,
                                subtitle = s.chainUpstreamProxySub,
                                checked = upstreamProxyEnabled,
                                onCheckedChange = {
                                    upstreamProxyEnabled = it
                                    settingsStore.upstreamProxyEnabled = it
                                },
                            )
                            if (upstreamProxyEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                SettingClickableRow(
                                    title = s.upstreamProxyTypeTitle,
                                    value = upstreamProxyType.name,
                                    onClick = { showUpstreamProxyTypeDialog = true },
                                )
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                SettingClickableRow(
                                    title = s.upstreamProxyAddressTitle,
                                    value = if (upstreamProxyHost.isBlank()) s.notSet else "$upstreamProxyHost:$upstreamProxyPort",
                                    onClick = { showUpstreamProxyAddressDialog = true },
                                )
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                SettingClickableRow(
                                    title = s.upstreamProxyCredentialsTitle,
                                    value = if (upstreamProxyUsername.isBlank()) s.none else upstreamProxyUsername,
                                    onClick = { showUpstreamProxyCredentialsDialog = true },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // 8. ABOUT & LOGS
                    SectionHeading(s.sectionAboutAndLogs)
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            SettingClickableRow(
                                title = s.viewConnectionLogsTitle,
                                value = s.viewConnectionLogsSub,
                                onClick = onOpenLogs,
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.versionTitle,
                                value = BuildConfig.VERSION_NAME,
                                onClick = {},
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                            SettingClickableRow(
                                title = s.githubRepoTitle,
                                value = GITHUB_URL,
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(26.dp))

                    // Sign Out Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable(onClick = onSignOut)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                s.signOutButton,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    val neu = currentNeumorphicColors
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
        ),
        color = neu.textSecondary,
        modifier = Modifier.padding(start = 6.dp, bottom = 10.dp, top = 4.dp),
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val neu = currentNeumorphicColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = neu.textSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = neu.accent,
                uncheckedThumbColor = neu.textSecondary,
                uncheckedTrackColor = neu.surfaceElevated,
            ),
        )
    }
}

@Composable
private fun SettingClickableRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    val neu = currentNeumorphicColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = neu.textSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = neu.textSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

// Dialogs
@Composable
private fun DynamicIslandSizeDialog(
    current: SettingsStore.DynamicIslandSize,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandSize) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandSizeTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandSize.entries.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == size, onClick = { selected = size })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == size, onClick = { selected = size })
                        Spacer(Modifier.width(10.dp))
                        Text(size.localizedLabel(s.isRtl), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandCornerRadiusDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    val presets = if (s.isRtl) {
        listOf(
            14 to "نرم و ملایم (۱۴ dp)",
            20 to "متعادل (۲۰ dp)",
            24 to "کپسول استاندارد (۲۴ dp)",
            30 to "کپسول عمیق (۳۰ dp)",
            36 to "کاملاً گرد (۳۶ dp)",
        )
    } else {
        listOf(
            14 to "Soft Rounded (14 dp)",
            20 to "Balanced Pill (20 dp)",
            24 to "Default Capsule (24 dp)",
            30 to "Deep Capsule (30 dp)",
            36 to "Full Stadium (36 dp)",
        )
    }
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandCornerRadiusTitle) },
        text = {
            Column {
                presets.forEach { (radius, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == radius, onClick = { selected = radius })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == radius, onClick = { selected = radius })
                        Spacer(Modifier.width(10.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandPositionDialog(
    current: SettingsStore.DynamicIslandPosition,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandPosition) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandPositionTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandPosition.entries.forEach { pos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == pos, onClick = { selected = pos })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == pos, onClick = { selected = pos })
                        Spacer(Modifier.width(10.dp))
                        Text(pos.localizedLabel(s.isRtl), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandThemeDialog(
    current: SettingsStore.DynamicIslandTheme,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandTheme) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandThemeTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == theme, onClick = { selected = theme })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == theme, onClick = { selected = theme })
                        Spacer(Modifier.width(10.dp))
                        Text(theme.localizedLabel(s.isRtl), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DnsProviderPickerDialog(
    currentProvider: SettingsStore.DnsProvider,
    currentCustomUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DnsProvider, String) -> Unit,
) {
    val s = currentFoxyStrings
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var urlText by remember { mutableStateOf(currentCustomUrl) }
    val isCustom = selectedProvider == SettingsStore.DnsProvider.CUSTOM
    val isValidCustomUrl = SettingsStore.isValidDohUrl(urlText)
    val canSave = !isCustom || isValidCustomUrl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tunnelDnsTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsStore.DnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.localizedLabel(s.isRtl))
                            val detail = provider.dohUrl ?: ""
                            if (detail.isNotEmpty()) {
                                Text(
                                    detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (isCustom) {
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it.trim() },
                        label = { Text("DoH URL (https://...)") },
                        placeholder = { Text("https://example.com/dns-query") },
                        singleLine = true,
                        isError = urlText.isNotBlank() && !isValidCustomUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedProvider, urlText) },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun BootstrapDnsPickerDialog(
    currentProvider: SettingsStore.BootstrapDnsProvider,
    currentCustomIp: String,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.BootstrapDnsProvider, String) -> Unit,
) {
    val s = currentFoxyStrings
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var ipText by remember { mutableStateOf(currentCustomIp) }
    val isCustom = selectedProvider == SettingsStore.BootstrapDnsProvider.CUSTOM
    val isValidCustomIp = SettingsStore.isValidIpAddress(ipText.trim())
    val canSave = !isCustom || isValidCustomIp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.resolverDnsTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsStore.BootstrapDnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.localizedLabel(s.isRtl))
                            Text(
                                provider.localizedDescription(s.isRtl),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (isCustom) {
                    OutlinedTextField(
                        value = ipText,
                        onValueChange = { ipText = it.trim() },
                        label = { Text("IP (1.1.1.1)") },
                        placeholder = { Text("1.1.1.1") },
                        singleLine = true,
                        isError = ipText.isNotBlank() && !isValidCustomIp,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedProvider, ipText) },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun SocksBindAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.localBindAddressTitle) },
        text = {
            Column {
                SettingsStore.SOCKS_BIND_ADDRESS_PRESETS.forEach { (address, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == address,
                                onClick = { selected = address },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selected == address,
                            onClick = { selected = address },
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun SocksPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.socksPortTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text("1\u201365535") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_SOCKS_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_SOCKS_PORT.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun HttpPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.httpProxyPortTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text("1\u201365535") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_HTTP_PROXY_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_HTTP_PROXY_PORT.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun EdgeAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current) }
    val trimmed = text.trim()
    val isValid = trimmed.isEmpty() || SettingsStore.isValidEdgeHost(trimmed)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.customEdgeAddressTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.trim() },
                    label = { Text("IP / Host") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trimmed) }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyTypeDialog(
    current: SettingsStore.UpstreamProxyType,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.UpstreamProxyType) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyTypeTitle) },
        text = {
            Column {
                SettingsStore.UpstreamProxyType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == type,
                                onClick = { selected = type },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(selected = selected == type, onClick = { selected = type })
                        Text(type.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyAddressDialog(
    currentHost: String,
    currentPort: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    val s = currentFoxyStrings
    var hostText by remember { mutableStateOf(currentHost) }
    var portText by remember { mutableStateOf(currentPort.toString()) }
    val port = portText.toIntOrNull()
    val isValidHost = hostText.isBlank() || SettingsStore.isValidEdgeHost(hostText)
    val isValidPort = port != null && port in 1..65_535
    val canSave = isValidHost && isValidPort

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyAddressTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = hostText,
                    onValueChange = { hostText = it.trim() },
                    label = { Text("Host / IP") },
                    singleLine = true,
                    isError = hostText.isNotBlank() && !isValidHost,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter(Char::isDigit).take(5) },
                    label = { Text("1\u201365535") },
                    singleLine = true,
                    isError = portText.isNotBlank() && !isValidPort,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { port?.let { onConfirm(hostText, it) } },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyCredentialsDialog(
    currentUsername: String,
    currentPassword: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    val s = currentFoxyStrings
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf(currentPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyCredentialsTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(s.loginPasswordLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(username, password) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun TlsSplitModePickerDialog(
    current: SettingsStore.TlsSplitMode,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.TlsSplitMode) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tlsFragmentationTitle) },
        text = {
            Column {
                SettingsStore.TlsSplitMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == mode,
                                onClick = { selected = mode },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(selected = selected == mode, onClick = { selected = mode })
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(mode.localizedLabel(s.isRtl))
                            Text(
                                mode.localizedDescription(s.isRtl),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

private data class AppItem(
    val packageName: String,
    val label: String,
)

@Composable
private fun SplitTunnelDialog(
    current: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    val context = LocalContext.current
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    var searchQuery by remember { mutableStateOf("") }

    val installedApps by produceState<List<AppItem>>(initialValue = emptyList()) {
        value = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            packages
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.packageName == "com.android.chrome" }
                .map { AppItem(it.packageName, pm.getApplicationLabel(it).toString()) }
                .sortedBy { it.label.lowercase() }
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.excludedAppsTitle) },
        text = {
            Column(Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                if (installedApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isChecked = selected.contains(app.packageName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = if (isChecked) selected - app.packageName
                                        else selected + app.packageName
                                    }
                                    .padding(vertical = 4.dp),
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        selected = if (check) selected + app.packageName
                                        else selected - app.packageName
                                    },
                                )
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(app.label, maxLines = 1)
                                    Text(
                                        app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("${s.save} (${selected.size})") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun MtuPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var mtuText by remember { mutableStateOf(current.toString()) }
    val parsedMtu = mtuText.toIntOrNull()
    val isValid = parsedMtu != null && parsedMtu in SettingsStore.MIN_MTU..SettingsStore.MAX_MTU

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tunnelMtuTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = mtuText,
                    onValueChange = { mtuText = it.filter(Char::isDigit).take(4) },
                    label = { Text("${SettingsStore.MIN_MTU}\u2013${SettingsStore.MAX_MTU}") },
                    singleLine = true,
                    isError = mtuText.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { mtuText = SettingsStore.DEFAULT_MTU.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_MTU.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedMtu?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}
