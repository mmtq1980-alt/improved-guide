package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.InteractiveMapCanvas

@Composable
fun MapScreen(
    members: List<UserEntity>,
    places: List<PlaceEntity>,
    activeTrip: TripEntity?,
    selectedMemberId: String?,
    isLiveSimulationRunning: Boolean,
    onSelectMember: (String) -> Unit,
    onToggleLiveSimulation: () -> Unit,
    onOpenStartTrip: () -> Unit,
    onOpenAddPlace: () -> Unit,
    onTriggerSos: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fullscreen Map Canvas
        InteractiveMapCanvas(
            members = members,
            places = places,
            activeTrip = activeTrip,
            selectedMemberId = selectedMemberId,
            onMemberSelect = onSelectMember
        )

        // 2. Member Selector Pills Overlay at top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = isLiveSimulationRunning,
                            onClick = onToggleLiveSimulation,
                            label = { Text(if (isLiveSimulationRunning) "التتبع مباشر 🟢" else "إيقاف التتبع ⏸️", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isLiveSimulationRunning) Icons.Default.Sensors else Icons.Default.PauseCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    items(members, key = { it.id }) { member ->
                        val isSelected = member.id == selectedMemberId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMember(member.id) },
                            label = { Text(member.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (member.role) {
                                                FamilyRole.FATHER -> Color(0xFF006C4C)
                                                FamilyRole.MOTHER -> Color(0xFF9C27B0)
                                                FamilyRole.SON -> Color(0xFF0288D1)
                                                FamilyRole.DAUGHTER -> Color(0xFFE91E63)
                                                else -> Color(0xFF607D8B)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(1),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // 3. Floating Actions (SOS + Trip + Add Place)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = onTriggerSos,
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Warning, contentDescription = "SOS")
            }

            FloatingActionButton(
                onClick = onOpenStartTrip,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Navigation, contentDescription = "رحلة جديدة")
            }

            FloatingActionButton(
                onClick = onOpenAddPlace,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = "إضافة مكان")
            }
        }
    }
}
