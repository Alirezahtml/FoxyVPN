package com.vauth.foxyvpn.ui.screens

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.BuildConfig
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.quicksettings.DynamicIslandOverlayService
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GITHUB_URL = "https://github.com/vauth/foxyvpn"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    onOpenLogs: () -> Unit,
    onOpenTools: (Int) -> Unit = {},
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val neu = currentNeumorphicColors

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
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = neu.textPrimary,
                    )
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 38.dp,
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.widthIn(max = 600.dp)) {
                Column {
                    // SMART NETWORK & SECURITY TOOLKIT (5 OPTIONS)
                    SectionHeading("SMART NETWORK & SECURITY TOOLKIT (۵ ابزار ویژه)")
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(0),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            SettingClickableRow(
                                title = "1. Real Speed Test (تست سرعت دانلود و آپلود)",
                                value = "Measure live Download, Upload, Ping & Jitter",
                                onClick = { onOpenTools(0) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "2. IP, DNS & Leak Inspector (تست نشتی و ایپی)",
                                value = "Compare ISP IP vs VPN Exit IP, DoH DNS & IPv6 leak guard",
                                onClick = { onOpenTools(1) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "3. Smart Fastest Server Scanner (یافتن سریع‌ترین سرور)",
                                value = "Benchmark all edge locations and auto-connect to #1",
                                onClick = { onOpenTools(2) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "4. Keep-Alive & Auto-Reconnect Watchdog (ضدقطع‌شدن)",
                                value = "Configure anti-idle heartbeat pulses and auto-redial",
                                onClick = { onOpenTools(3) },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "5. Smart MTU & Anti-DPI Optimizer (بهینه‌ساز MTU و فرگمنت)",
                                value = "Probe optimal MTU size and TLS SNI fragmentation",
                                onClick = { onOpenTools(4) },
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // DYNAMIC ISLAND SECTION
                    SectionHeading("DYNAMIC ISLAND (iOS STYLE)")
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(1),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            SettingToggleRow(
                                title = "Enable Dynamic Island",
                                subtitle = "Shows an interactive morphing status island at top",
                                checked = dynamicIslandEnabled,
                                onCheckedChange = {
                                    dynamicIslandEnabled = it
                                    settingsStore.dynamicIslandEnabled = it
                                },
                            )

                            if (dynamicIslandEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
                                SettingToggleRow(
                                    title = "Display Over Other Apps (Overlay)",
                                    subtitle = if (hasOverlayPermission) "Floating pill visible when switching to other apps"
                                    else "Requires Android 'Display over other apps' permission. Tap to grant.",
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

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingClickableRow(
                                    title = "Island Size",
                                    value = dynamicIslandSize.label,
                                    onClick = { showIslandSizeDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingClickableRow(
                                    title = "Corner Roundness",
                                    value = "${dynamicIslandCornerRadius} dp",
                                    onClick = { showIslandCornerRadiusDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingClickableRow(
                                    title = "Alignment & Position",
                                    value = dynamicIslandPosition.label,
                                    onClick = { showIslandPositionDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingClickableRow(
                                    title = "Visual Theme",
                                    value = dynamicIslandTheme.label,
                                    onClick = { showIslandThemeDialog = true },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingToggleRow(
                                    title = "Show Live Transfer Speed",
                                    subtitle = "Displays current upload and download rates",
                                    checked = dynamicIslandShowSpeed,
                                    onCheckedChange = {
                                        dynamicIslandShowSpeed = it
                                        settingsStore.dynamicIslandShowSpeed = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingToggleRow(
                                    title = "Show Ping Latency",
                                    subtitle = "Displays measured latency to server",
                                    checked = dynamicIslandShowPing,
                                    onCheckedChange = {
                                        dynamicIslandShowPing = it
                                        settingsStore.dynamicIslandShowPing = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingToggleRow(
                                    title = "Show Country Flag",
                                    subtitle = "Displays emoji flag of the active location",
                                    checked = dynamicIslandShowFlag,
                                    onCheckedChange = {
                                        dynamicIslandShowFlag = it
                                        settingsStore.dynamicIslandShowFlag = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingToggleRow(
                                    title = "Show Elapsed Timer",
                                    subtitle = "Displays connection duration on pill",
                                    checked = dynamicIslandShowTimer,
                                    onCheckedChange = {
                                        dynamicIslandShowTimer = it
                                        settingsStore.dynamicIslandShowTimer = it
                                    },
                                )

                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                SettingToggleRow(
                                    title = "Glow & Pulse Animations",
                                    subtitle = "Soft breathing aura while connected or dialing",
                                    checked = dynamicIslandAnimation,
                                    onCheckedChange = {
                                        dynamicIslandAnimation = it
                                        settingsStore.dynamicIslandAnimation = it
                                    },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // CONNECTION
                    SectionHeading("CONNECTION")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingToggleRow(
                                title = "Verify exit location",
                                subtitle = "Check that tunnel exits in selected country",
                                checked = exitCheckEnabled,
                                onCheckedChange = {
                                    exitCheckEnabled = it
                                    settingsStore.exitCheckEnabled = it
                                },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "Tunnel MTU",
                                value = mtu.toString(),
                                onClick = { showMtuDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // DNS
                    SectionHeading("DNS")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingClickableRow(
                                title = "Tunnel DNS",
                                value = when (dnsProvider) {
                                    SettingsStore.DnsProvider.SYSTEM_DEFAULT -> "Default"
                                    SettingsStore.DnsProvider.CUSTOM -> customDohUrl.ifBlank { "Custom DoH" }
                                    else -> dnsProvider.label
                                },
                                onClick = { showDnsProviderDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "Resolver DNS",
                                value = when (bootstrapDnsProvider) {
                                    SettingsStore.BootstrapDnsProvider.CUSTOM -> customBootstrapIp.ifBlank { "Custom IP" }
                                    SettingsStore.BootstrapDnsProvider.SYSTEM -> "System DNS"
                                    else -> bootstrapDnsProvider.label
                                },
                                onClick = { showBootstrapDnsDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // LOCAL PROXY
                    SectionHeading("LOCAL PROXY")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingToggleRow(
                                title = "Proxy-only mode",
                                subtitle = "Run local proxy servers without a VPN interface",
                                checked = proxyOnlyMode,
                                onCheckedChange = {
                                    proxyOnlyMode = it
                                    settingsStore.proxyOnlyMode = it
                                },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "Local address",
                                value = socksBindAddress,
                                onClick = { showSocksBindDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "SOCKS5 port",
                                value = socksPort.toString(),
                                onClick = { showSocksPortDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingToggleRow(
                                title = "Enable HTTP proxy",
                                subtitle = "Run local HTTP proxy alongside SOCKS5",
                                checked = httpProxyEnabled,
                                onCheckedChange = {
                                    httpProxyEnabled = it
                                    settingsStore.httpProxyEnabled = it
                                },
                            )
                            if (httpProxyEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                SettingClickableRow(
                                    title = "HTTP port",
                                    value = httpProxyPort.toString(),
                                    onClick = { showHttpPortDialog = true },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // SPLIT TUNNELING
                    SectionHeading("SPLIT TUNNELING")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingClickableRow(
                                title = "Excluded apps",
                                value = if (excludedApps.isEmpty()) "None" else "${excludedApps.size} app(s) bypass VPN",
                                onClick = { showSplitTunnelDialog = true },
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ADVANCED
                    SectionHeading("ADVANCED NETWORKING")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingClickableRow(
                                title = "Custom edge address",
                                value = customEdgeAddress.ifBlank { "Not set" },
                                onClick = { showEdgeAddressDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "TLS fragmentation",
                                value = tlsSplitMode.label,
                                onClick = { showTlsSplitDialog = true },
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingToggleRow(
                                title = "Chain through upstream proxy",
                                subtitle = "Connect to VPN server through another proxy first",
                                checked = upstreamProxyEnabled,
                                onCheckedChange = {
                                    upstreamProxyEnabled = it
                                    settingsStore.upstreamProxyEnabled = it
                                },
                            )
                            if (upstreamProxyEnabled) {
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                SettingClickableRow(
                                    title = "Proxy type",
                                    value = upstreamProxyType.name,
                                    onClick = { showUpstreamProxyTypeDialog = true },
                                )
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                SettingClickableRow(
                                    title = "Proxy address",
                                    value = if (upstreamProxyHost.isBlank()) "Not set" else "$upstreamProxyHost:$upstreamProxyPort",
                                    onClick = { showUpstreamProxyAddressDialog = true },
                                )
                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                SettingClickableRow(
                                    title = "Proxy credentials",
                                    value = if (upstreamProxyUsername.isBlank()) "None" else upstreamProxyUsername,
                                    onClick = { showUpstreamProxyCredentialsDialog = true },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ABOUT & ACTIONS
                    SectionHeading("ABOUT & LOGS")
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            SettingClickableRow(
                                title = "View connection logs",
                                value = "Live diagnostics and trace",
                                onClick = onOpenLogs,
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "Version",
                                value = BuildConfig.VERSION_NAME,
                                onClick = {},
                            )
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                            SettingClickableRow(
                                title = "GitHub Repository",
                                value = GITHUB_URL,
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Sign Out Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .clickable(onClick = onSignOut)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sign Out",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
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
            letterSpacing = 1.2.sp,
        ),
        color = neu.textSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 4.dp),
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = neu.textSecondary,
            )
        }
        Spacer(Modifier.width(12.dp))
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
            .clickable(onClick = onClick)
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
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = neu.textSecondary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = neu.textSecondary,
            modifier = Modifier.size(20.dp),
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
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dynamic Island Size") },
        text = {
            Column {
                SettingsStore.DynamicIslandSize.entries.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == size, onClick = { selected = size })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == size, onClick = { selected = size })
                        Spacer(Modifier.width(10.dp))
                        Text(size.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun DynamicIslandCornerRadiusDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val presets = listOf(
        14 to "Soft Rounded (14 dp)",
        20 to "Balanced Pill (20 dp)",
        24 to "Default iOS Capsule (24 dp)",
        30 to "Deep Capsule (30 dp)",
        36 to "Full Stadium (36 dp)",
    )
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Corner Roundness") },
        text = {
            Column {
                presets.forEach { (radius, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == radius, onClick = { selected = radius })
                            .padding(vertical = 6.dp),
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
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun DynamicIslandPositionDialog(
    current: SettingsStore.DynamicIslandPosition,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandPosition) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dynamic Island Position") },
        text = {
            Column {
                SettingsStore.DynamicIslandPosition.entries.forEach { pos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == pos, onClick = { selected = pos })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == pos, onClick = { selected = pos })
                        Spacer(Modifier.width(10.dp))
                        Text(pos.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun DynamicIslandThemeDialog(
    current: SettingsStore.DynamicIslandTheme,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandTheme) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dynamic Island Theme") },
        text = {
            Column {
                SettingsStore.DynamicIslandTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == theme, onClick = { selected = theme })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == theme, onClick = { selected = theme })
                        Spacer(Modifier.width(10.dp))
                        Text(theme.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var urlText by remember { mutableStateOf(currentCustomUrl) }
    val isCustom = selectedProvider == SettingsStore.DnsProvider.CUSTOM
    val isValidCustomUrl = SettingsStore.isValidDohUrl(urlText)
    val canSave = !isCustom || isValidCustomUrl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tunnel DNS") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "DNS used inside the VPN tunnel for all app and browsing traffic.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SettingsStore.DnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.label)
                            val detail = when (provider) {
                                SettingsStore.DnsProvider.SYSTEM_DEFAULT -> "Resolved at the VPN edge"
                                SettingsStore.DnsProvider.CUSTOM -> "Specify an HTTPS DoH endpoint URL"
                                else -> provider.dohUrl ?: ""
                            }
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var ipText by remember { mutableStateOf(currentCustomIp) }
    val isCustom = selectedProvider == SettingsStore.BootstrapDnsProvider.CUSTOM
    val isValidCustomIp = SettingsStore.isValidIpAddress(ipText.trim())
    val canSave = !isCustom || isValidCustomIp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resolver DNS") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "DNS used to resolve the VPN server address before the tunnel connects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SettingsStore.BootstrapDnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.label)
                            Text(
                                provider.description,
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
                        label = { Text("Resolver IP Address") },
                        placeholder = { Text("e.g. 1.1.1.1") },
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SocksBindAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SOCKS5 Bind Address") },
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
                            .padding(vertical = 4.dp),
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
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SocksPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SOCKS5 Port") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text("Port (1\u201365535)") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_SOCKS_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Reset to default (${SettingsStore.DEFAULT_SOCKS_PORT})")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun HttpPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("HTTP Proxy Port") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text("Port (1\u201365535)") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_HTTP_PROXY_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Reset to default (${SettingsStore.DEFAULT_HTTP_PROXY_PORT})")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun EdgeAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(current) }
    val trimmed = text.trim()
    val isValid = trimmed.isEmpty() || SettingsStore.isValidEdgeHost(trimmed)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Edge Address") },
        text = {
            Column {
                Text(
                    "Override the DNS resolution of the VPN server with a specific hostname or IP address.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.trim() },
                    label = { Text("IP or Hostname") },
                    placeholder = { Text("Leave empty for default") },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trimmed) }, enabled = isValid) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun UpstreamProxyTypeDialog(
    current: SettingsStore.UpstreamProxyType,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.UpstreamProxyType) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upstream Proxy Type") },
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
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = selected == type, onClick = { selected = type })
                        Text(type.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
    var hostText by remember { mutableStateOf(currentHost) }
    var portText by remember { mutableStateOf(currentPort.toString()) }
    val port = portText.toIntOrNull()
    val isValidHost = hostText.isBlank() || SettingsStore.isValidEdgeHost(hostText)
    val isValidPort = port != null && port in 1..65_535
    val canSave = isValidHost && isValidPort

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upstream Proxy Address") },
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
                    label = { Text("Port (1\u201365535)") },
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf(currentPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proxy Authentication") },
        text = {
            Column {
                Text(
                    "Optional username and password if your proxy requires authentication.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
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
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(username, password) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun TlsSplitModePickerDialog(
    current: SettingsStore.TlsSplitMode,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.TlsSplitMode) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TLS Fragmentation") },
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
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = selected == mode, onClick = { selected = mode })
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(mode.label)
                            Text(
                                mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text("Bypass VPN (Excluded Apps)") },
        text = {
            Column(Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search apps") },
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
            TextButton(onClick = { onConfirm(selected) }) { Text("Save (${selected.size})") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun MtuPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var mtuText by remember { mutableStateOf(current.toString()) }
    val parsedMtu = mtuText.toIntOrNull()
    val isValid = parsedMtu != null && parsedMtu in SettingsStore.MIN_MTU..SettingsStore.MAX_MTU

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tunnel MTU") },
        text = {
            Column {
                Text(
                    "Maximum Transmission Unit for the VPN tunnel. Default is ${SettingsStore.DEFAULT_MTU}. Lower values (e.g. 1280\u20131400) prevent packet fragmentation on mobile networks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                OutlinedTextField(
                    value = mtuText,
                    onValueChange = { mtuText = it.filter(Char::isDigit).take(4) },
                    label = { Text("MTU (${SettingsStore.MIN_MTU}\u2013${SettingsStore.MAX_MTU})") },
                    singleLine = true,
                    isError = mtuText.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { mtuText = SettingsStore.DEFAULT_MTU.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Reset to default (${SettingsStore.DEFAULT_MTU})")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedMtu?.let { onConfirm(it) } }, enabled = isValid) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
