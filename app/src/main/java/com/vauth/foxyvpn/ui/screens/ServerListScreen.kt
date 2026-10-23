package com.vauth.foxyvpn.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.ProxyStateStore
import com.vauth.foxyvpn.data.RECOMMENDED_COUNTRY_CODE
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.model.VpnCountry
import com.vauth.foxyvpn.data.model.emojiFlag
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import com.vauth.foxyvpn.vpn.PingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed class LocationRow {
    data class RecommendedRow(val country: VpnCountry) : LocationRow()
    data class CountryHeader(val country: VpnCountry) : LocationRow()
    data class CityRow(val country: VpnCountry, val cityIndex: Int) : LocationRow()
}

private enum class PingState { PENDING, DONE, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    serverListClient: ServerListClient,
    proxyStateStore: ProxyStateStore,
    onServerSelected: () -> Unit,
    onBack: () -> Unit,
) {
    var countries by remember { mutableStateOf<List<VpnCountry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedState by proxyStateStore.selectedProxyFlow.collectAsState()
    val pingResults = remember { mutableStateMapOf<String, Int>() }
    val pingStates = remember { mutableStateMapOf<String, PingState>() }

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val haptics = LocalHapticFeedback.current
    val s = currentFoxyStrings

    LaunchedEffect(Unit) {
        val cached = withContext(Dispatchers.IO) { serverListClient.loadCachedCountries() }
        if (cached.isNotEmpty()) {
            countries = cached
            isLoading = false
        }
        runCatching { serverListClient.fetchCountries() }
            .onSuccess { fresh ->
                countries = fresh
                errorMessage = null
            }
            .onFailure {
                if (countries.isEmpty()) {
                    errorMessage = it.message ?: s.noActiveServers
                }
            }
        isLoading = false
    }

    val ordered = remember(countries) {
        countries.sortedWith(compareBy({ it.code != RECOMMENDED_COUNTRY_CODE }, { it.name }))
    }

    val filteredCountries = remember(ordered, searchQuery) {
        if (searchQuery.isBlank()) ordered
        else ordered.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.cities.any { c -> c.code.contains(searchQuery, ignoreCase = true) }
        }
    }

    val rows = remember(filteredCountries) {
        buildList {
            for (country in filteredCountries) {
                if (country.code == RECOMMENDED_COUNTRY_CODE && searchQuery.isBlank()) {
                    add(LocationRow.RecommendedRow(country))
                    continue
                }
                add(LocationRow.CountryHeader(country))
                country.cities.indices.forEach { index -> add(LocationRow.CityRow(country, index)) }
            }
        }
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s.locationsTitle,
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
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Sunken Neumorphic Search Bar
            val searchShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(vertical = 8.dp)
                    .neumorphicSunken(
                        shape = searchShape,
                        cornerRadius = 18.dp,
                        depth = 3.dp,
                        surfaceColor = neu.surfaceElevated,
                    )
                    .border(
                        width = 1.dp,
                        color = if (neu.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
                        shape = searchShape,
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = neu.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                s.searchLocationsPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = neu.textSecondary.copy(alpha = 0.7f),
                            )
                        },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = neu.textPrimary,
                            unfocusedTextColor = neu.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .weight(1f),
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = neu.accent,
                    )
                    errorMessage != null && countries.isEmpty() -> Text(
                        text = errorMessage.orEmpty(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 6.dp,
                            vertical = 10.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            rows,
                            key = { row ->
                                when (row) {
                                    is LocationRow.RecommendedRow -> "recommended:${row.country.code}"
                                    is LocationRow.CountryHeader -> "country:${row.country.code}"
                                    is LocationRow.CityRow ->
                                        "city:${row.country.code}:${row.country.cities[row.cityIndex].code}"
                                }
                            },
                        ) { row ->
                            val selected = selectedState
                            when (row) {
                                is LocationRow.RecommendedRow -> {
                                    val isSelected = row.country.code == selected?.countryCode
                                    NeumorphicCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 18.dp,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCountry(countries, row.country.code)
                                            val chosen = candidates.randomOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            } else {
                                                errorMessage = s.noActiveServers
                                            }
                                        },
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(neu.accent.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    Icons.Filled.Public,
                                                    contentDescription = null,
                                                    tint = neu.accent,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    s.recommendedFastest,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = neu.textPrimary,
                                                )
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    s.recommendedSubtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = neu.textSecondary,
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = statusColors.connected,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                is LocationRow.CountryHeader -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp, bottom = 4.dp, start = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = row.country.emojiFlag,
                                            fontSize = 20.sp,
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = row.country.name,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp,
                                            ),
                                            color = neu.textSecondary,
                                        )
                                    }
                                }
                                is LocationRow.CityRow -> {
                                    val city = row.country.cities[row.cityIndex]
                                    val serverCount = city.servers.size
                                    val isSelected = row.country.code == selected?.countryCode && city.code == selected?.cityCode

                                    val pingKey = "${row.country.code}:${city.code}"
                                    val pingMs = pingResults[pingKey]
                                    val pingState = pingStates[pingKey] ?: PingState.PENDING

                                    LaunchedEffect(pingKey) {
                                        if (pingStates.containsKey(pingKey)) return@LaunchedEffect
                                        val target = city.servers.firstOrNull { !it.quarantined }
                                            ?.let { ServerListClient.defaultConnectTarget(it) }
                                        if (target == null) {
                                            pingStates[pingKey] = PingState.FAILED
                                            return@LaunchedEffect
                                        }
                                        val result = PingUtil.measureTcpLatencyMs(target.first, target.second)
                                        if (result != null) {
                                            pingResults[pingKey] = result
                                            pingStates[pingKey] = PingState.DONE
                                        } else {
                                            pingStates[pingKey] = PingState.FAILED
                                        }
                                    }

                                    NeumorphicCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp)
                                            .staggeredEntrance(row.cityIndex % 8, delayPerItemMs = 35)
                                            .neumorphicTouchBounce(),
                                        cornerRadius = 16.dp,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCity(countries, row.country.code, city.code)
                                            val chosen = candidates.randomOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            } else {
                                                errorMessage = s.noActiveServers
                                            }
                                        },
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 18.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = city.code,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = neu.textPrimary,
                                                )
                                                val primaryHostname = city.servers.firstOrNull()?.hostname
                                                val serverLabel = when {
                                                    primaryHostname == null -> s.noActiveServers
                                                    serverCount > 1 -> "$primaryHostname (${s.extraEdgesSuffix(serverCount - 1)})"
                                                    else -> primaryHostname
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = serverLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = neu.textSecondary,
                                                )
                                            }

                                            // Ping indicator badge
                                            val pingText = when {
                                                pingMs != null -> "$pingMs ms"
                                                pingState == PingState.FAILED -> "n/a"
                                                else -> "\u2022\u2022\u2022"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(neu.surfaceElevated)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                            ) {
                                                Text(
                                                    text = pingText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (pingMs != null) statusColors.connected else neu.textSecondary,
                                                )
                                            }

                                            if (isSelected) {
                                                Spacer(Modifier.width(10.dp))
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = statusColors.connected,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
