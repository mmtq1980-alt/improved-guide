package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.LocationHistoryEntity
import com.example.data.UserEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    members: List<UserEntity>,
    selectedMemberId: String?,
    locationHistory: List<LocationHistoryEntity>,
    selectedFilter: String,
    onSelectMember: (String) -> Unit,
    onSelectFilter: (String) -> Unit
) {
    var isPlayingReplay by remember { mutableStateOf(false) }
    var replaySpeed by remember { mutableIntStateOf(1) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isPlayingReplay, currentStepIndex) {
        if (isPlayingReplay && locationHistory.isNotEmpty()) {
            kotlinx.coroutines.delay(1200L / replaySpeed)
            if (currentStepIndex < locationHistory.size - 1) {
                currentStepIndex++
            } else {
                isPlayingReplay = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Member Selector Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(members, key = { it.id }) { member ->
                    FilterChip(
                        selected = member.id == selectedMemberId,
                        onClick = { onSelectMember(member.id) },
                        label = { Text(member.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Time Range Filter Row (Day / Week / Month)
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedFilter == "DAY",
                    onClick = { onSelectFilter("DAY") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("اليوم")
                }
                SegmentedButton(
                    selected = selectedFilter == "WEEK",
                    onClick = { onSelectFilter("WEEK") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("الأسبوع")
                }
                SegmentedButton(
                    selected = selectedFilter == "MONTH",
                    onClick = { onSelectFilter("MONTH") },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("الشهر")
                }
            }
        }

        // Route Replay Playback Controller Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إعادة عرض المسار (Playback) 🎬",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "سرعة ${replaySpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (locationHistory.isNotEmpty()) {
                        Slider(
                            value = currentStepIndex.toFloat(),
                            onValueChange = {
                                currentStepIndex = it.roundToInt()
                                isPlayingReplay = false
                            },
                            valueRange = 0f..(locationHistory.size - 1).toFloat(),
                            steps = (locationHistory.size - 2).coerceAtLeast(0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                isPlayingReplay = !isPlayingReplay
                            }) {
                                Icon(
                                    imageVector = if (isPlayingReplay) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 4).forEach { spd ->
                                    FilterChip(
                                        selected = replaySpeed == spd,
                                        onClick = { replaySpeed = spd },
                                        label = { Text("${spd}x") }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("لا يوجد سجل حركة مسجل لهذه الفترة.", fontSize = 12.sp)
                    }
                }
            }
        }

        // Timeline Step Points List Header
        item {
            Text(
                text = "تفاصيل المسار والجولات الزمانية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(locationHistory) { point ->
            HistoryPointCard(point = point)
        }
    }
}

@Composable
fun HistoryPointCard(point: LocationHistoryEntity) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")) }
    val timeStr = formatter.format(Date(point.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (point.speedKmh > 0) Color(0xFF006C4C) else Color.Gray)
                )

                Column {
                    Text(
                        text = if (point.placeName.isNotBlank()) point.placeName else "موقع محدد: ${point.latitude.toString().take(6)}, ${point.longitude.toString().take(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "الوقت: $timeStr • السرعة: ${point.speedKmh.roundToInt()} كم/س",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${point.batteryLevel}% 🔋",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
