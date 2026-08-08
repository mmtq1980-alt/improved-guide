package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.FamilyGuardianTheme
import com.example.viewmodel.FamilyGuardianViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FamilyGuardianViewModel = viewModel()
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
            val isDarkMode = userSettings?.isDarkMode ?: true
            val language = userSettings?.language ?: "ar"

            val layoutDir = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                FamilyGuardianTheme(darkTheme = isDarkMode) {
                    FamilyGuardianApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGuardianApp(viewModel: FamilyGuardianViewModel) {
    var isAuthenticated by remember { mutableStateOf(true) }

    val members by viewModel.familyMembers.collectAsStateWithLifecycle()
    val family by viewModel.currentFamily.collectAsStateWithLifecycle()
    val places by viewModel.places.collectAsStateWithLifecycle()
    val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()
    val allTrips by viewModel.allTrips.collectAsStateWithLifecycle()
    val eventLogs by viewModel.eventLogs.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val selectedMemberHistory by viewModel.selectedMemberHistory.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isSosActive by viewModel.isSosActive.collectAsStateWithLifecycle()
    val showSosDialog by viewModel.showSosDialog.collectAsStateWithLifecycle()
    val showQrDialog by viewModel.showQrDialog.collectAsStateWithLifecycle()
    val showAddPlaceDialog by viewModel.showAddPlaceDialog.collectAsStateWithLifecycle()
    val showStartTripDialog by viewModel.showStartTripDialog.collectAsStateWithLifecycle()
    val selectedHistoryFilter by viewModel.selectedHistoryFilter.collectAsStateWithLifecycle()
    val isLiveSimulating by viewModel.isLiveSimulationRunning.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    if (!isAuthenticated) {
        AuthScreen(onLoginSuccess = { isAuthenticated = true })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "حامي العائلة",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = family?.name ?: "Family Guardian",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSosDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleQrDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Invite",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "الخريطة") },
                    label = { Text("الخريطة", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.Place, contentDescription = "الأماكن") },
                    label = { Text("الأماكن", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "السجل") },
                    label = { Text("السجل", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    members = members,
                    family = family,
                    places = places,
                    activeTrip = activeTrip,
                    eventLogs = eventLogs,
                    selectedMemberId = selectedMemberId,
                    onSelectMember = { viewModel.selectMember(it) },
                    onNavigateToMap = { viewModel.setTab(1) },
                    onTriggerSos = { viewModel.triggerSosAlert() },
                    onOpenQrDialog = { viewModel.toggleQrDialog(true) },
                    onOpenStartTrip = { viewModel.toggleStartTripDialog(true) },
                    onOpenAddPlace = { viewModel.toggleAddPlaceDialog(true) }
                )
                1 -> MapScreen(
                    members = members,
                    places = places,
                    activeTrip = activeTrip,
                    selectedMemberId = selectedMemberId,
                    isLiveSimulationRunning = isLiveSimulating,
                    onSelectMember = { viewModel.selectMember(it) },
                    onToggleLiveSimulation = { viewModel.toggleLiveSimulation() },
                    onOpenStartTrip = { viewModel.toggleStartTripDialog(true) },
                    onOpenAddPlace = { viewModel.toggleAddPlaceDialog(true) },
                    onTriggerSos = { viewModel.triggerSosAlert() }
                )
                2 -> PlacesScreen(
                    places = places,
                    onOpenAddPlace = { viewModel.toggleAddPlaceDialog(true) },
                    onDeletePlace = { viewModel.deletePlace(it) }
                )
                3 -> HistoryScreen(
                    members = members,
                    selectedMemberId = selectedMemberId,
                    locationHistory = selectedMemberHistory,
                    selectedFilter = selectedHistoryFilter,
                    onSelectMember = { viewModel.selectMember(it) },
                    onSelectFilter = { viewModel.setHistoryFilter(it) }
                )
                4 -> SettingsScreen(
                    userSettings = userSettings,
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onToggleLanguage = { viewModel.toggleLanguage(it) },
                    onToggleLocationSharing = { viewModel.toggleLocationSharing(it) },
                    onUpdateInterval = { viewModel.updateUpdateInterval(it) },
                    onLogout = { isAuthenticated = false }
                )
            }

            // Dialog Overlays
            if (showSosDialog) {
                SOSDialog(
                    user = currentUser,
                    onCancel = { viewModel.cancelSosAlert() }
                )
            }

            if (showQrDialog) {
                QrInviteModal(
                    family = family,
                    onDismiss = { viewModel.toggleQrDialog(false) }
                )
            }

            if (showAddPlaceDialog) {
                AddPlaceModal(
                    onDismiss = { viewModel.toggleAddPlaceDialog(false) },
                    onAddPlace = { name, type, lat, lng, radius ->
                        viewModel.addPlace(name, type, lat, lng, radius)
                    }
                )
            }

            if (showStartTripDialog) {
                StartTripModal(
                    onDismiss = { viewModel.toggleStartTripDialog(false) },
                    onStartTrip = { name, lat, lng ->
                        viewModel.startTrip(name, lat, lng)
                    }
                )
            }
        }
    }
}
