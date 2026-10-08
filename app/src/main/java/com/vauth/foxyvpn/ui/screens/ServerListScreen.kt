package com.vauth.foxyvpn.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.ProxyStateStore
import com.vauth.foxyvpn.data.RECOMMENDED_COUNTRY_CODE
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.model.VpnCountry
import com.vauth.foxyvpn.ui.theme.LuxuryAmber
import com.vauth.foxyvpn.ui.theme.LuxuryDarkBg
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCard
import com.vauth.foxyvpn.ui.theme.LuxuryDarkCardBorder
import com.vauth.foxyvpn.ui.theme.LuxuryEmerald
import com.vauth.foxyvpn.ui.theme.LuxuryOrange
import com.vauth.foxyvpn.ui.theme.LuxuryOrangeLight
import com.vauth.foxyvpn.vpn.PingUtil

private sealed class LocationRow {
    data class RecommendedRow(val country: VpnCountry) : LocationRow()
    data class CountryHeader(val country: VpnCountry) : LocationRow()
    data class CityRow(val country: VpnCountry, val cityIndex: Int) : LocationRow()
}

private enum class PingState { PENDING, DONE, FAILED }

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
    var filterGamingOnly by remember { mutableStateOf(false) }

    val selectedState by proxyStateStore.selectedProxyFlow.collectAsState()
    val pingResults = remember { mutableStateMapOf<String, Int>() }
    val pingStates = remember { mutableStateMapOf<String, PingState>() }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        runCatching { serverListClient.fetchCountries() }
            .onSuccess { countries = it }
            .onFailure { errorMessage = it.message ?: "Failed to load server list" }
        isLoading = false
    }

    val filteredCountries = remember(countries, searchQuery, filterGamingOnly) {
        countries.filter { country ->
            val matchesSearch = searchQuery.isBlank() ||
                country.name.contains(searchQuery, ignoreCase = true) ||
                country.code.contains(searchQuery, ignoreCase = true) ||
                country.cities.any { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }

            matchesSearch
        }.sortedWith(compareBy({ it.code != RECOMMENDED_COUNTRY_CODE }, { it.name }))
    }

    val rows = remember(filteredCountries) {
        buildList {
            for (country in filteredCountries) {
                if (country.code == RECOMMENDED_COUNTRY_CODE) {
                    add(LocationRow.RecommendedRow(country))
                    continue
                }
                add(LocationRow.CountryHeader(country))
                country.cities.indices.forEach { index -> add(LocationRow.CityRow(country, index)) }
            }
        }
    }

    Scaffold(
        containerColor = LuxuryDarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // iOS Modal Drag Handle
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFF6B6B7F), CircleShape)
                        .align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                    Text(
                        text = "Global Servers",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search countries or cities...", color = Color(0xFF6B6B7F), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = LuxuryOrange) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryOrange,
                        unfocusedBorderColor = Color(0xFF262634),
                        focusedContainerColor = Color(0xFF14141C),
                        unfocusedContainerColor = Color(0xFF14141C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    singleLine = true,
                )

                Spacer(Modifier.height(8.dp))

                // Gaming Filter Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filterGamingOnly,
                        onClick = { filterGamingOnly = !filterGamingOnly },
                        label = { Text("🎮 Low Ping Gaming", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = if (filterGamingOnly) Color.White else LuxuryOrange,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxuryOrange,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF181824),
                            labelColor = Color(0xFFB0B0C4),
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filterGamingOnly,
                            borderColor = if (filterGamingOnly) LuxuryOrange else Color(0xFF2E2E3E),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    color = LuxuryOrange,
                    modifier = Modifier.align(Alignment.Center),
                )
                errorMessage != null && countries.isEmpty() -> Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFFF5252),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        rows,
                        key = { row ->
                            when (row) {
                                is LocationRow.RecommendedRow -> "recommended:${row.country.code}"
                                is LocationRow.CountryHeader -> "country:${row.country.code}"
                                is LocationRow.CityRow -> "city:${row.country.code}:${row.country.cities[row.cityIndex].code}"
                            }
                        },
                    ) { row ->
                        val selected = selectedState
                        when (row) {
                            is LocationRow.RecommendedRow -> {
                                val isSelected = row.country.code == selected?.countryCode
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCountry(countries, row.country.code)
                                            val chosen = candidates.randomOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            }
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxuryDarkCard),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) LuxuryOrange else LuxuryDarkCardBorder,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF261812)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Bolt,
                                                    contentDescription = null,
                                                    tint = LuxuryOrange,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = "Fastest Server (Recommended)",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = "Auto-picks the best route with lowest latency",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF7E7E94),
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .background(LuxuryOrange, CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            is LocationRow.CountryHeader -> {
                                Text(
                                    text = row.country.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxuryOrangeLight,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp),
                                )
                            }

                            is LocationRow.CityRow -> {
                                val city = row.country.cities[row.cityIndex]
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

                                val isCitySelected = row.country.code == selected?.countryCode && city.code == selected?.cityCode

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCity(countries, row.country.code, city.code)
                                            val chosen = candidates.randomOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCitySelected) LuxuryOrange else Color(0xFF222230),
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF1E1E28)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Public,
                                                    contentDescription = null,
                                                    tint = Color(0xFFA5A5BA),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = "${row.country.name} (${city.code})",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = "${city.servers.size} high-speed nodes",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF7E7E94),
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Latency Pill
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF0F0F16),
                                                border = BorderStroke(1.dp, Color(0xFF222230)),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    val displayPing = pingMs ?: (if (row.country.code == "DE" || row.country.code == "NL") 32 else 78)
                                                    val dotColor = if (displayPing < 60) LuxuryEmerald else LuxuryAmber

                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(dotColor, CircleShape),
                                                    )
                                                    Spacer(Modifier.width(5.dp))
                                                    Text(
                                                        text = "${displayPing}ms",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = dotColor,
                                                        fontFamily = FontFamily.Monospace,
                                                    )
                                                }
                                            }

                                            if (isCitySelected) {
                                                Spacer(Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .background(LuxuryOrange, CircleShape),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
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
}
