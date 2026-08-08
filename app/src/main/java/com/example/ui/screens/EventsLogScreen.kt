package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventLogEntity
import com.example.data.EventSeverity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventsLogScreen(
    eventLogs: List<EventLogEntity>
) {
    var selectedSeverity by remember { mutableStateOf<EventSeverity?>(null) }

    val filteredLogs = remember(eventLogs, selectedSeverity) {
        if (selectedSeverity == null) eventLogs else eventLogs.filter { it.severity == selectedSeverity }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "سجل الأنشطة والأحداث (Audit Logs) 📋",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Severity Filter Chips Row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedSeverity == null,
                    onClick = { selectedSeverity = null },
                    label = { Text("الكل") }
                )
                FilterChip(
                    selected = selectedSeverity == EventSeverity.EMERGENCY,
                    onClick = { selectedSeverity = EventSeverity.EMERGENCY },
                    label = { Text("طوارئ SOS") }
                )
                FilterChip(
                    selected = selectedSeverity == EventSeverity.WARNING,
                    onClick = { selectedSeverity = EventSeverity.WARNING },
                    label = { Text("تحذيرات") }
                )
                FilterChip(
                    selected = selectedSeverity == EventSeverity.INFO,
                    onClick = { selectedSeverity = EventSeverity.INFO },
                    label = { Text("عام") }
                )
            }
        }

        items(filteredLogs, key = { it.id }) { log ->
            EventLogDetailCard(log = log)
        }
    }
}

@Composable
fun EventLogDetailCard(log: EventLogEntity) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.forLanguageTag("ar")) }
    val timeStr = formatter.format(Date(log.timestamp))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.severity) {
                EventSeverity.EMERGENCY -> Color(0xFFFFEBEE)
                EventSeverity.WARNING -> Color(0xFFFFF8E1)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = when (log.severity) {
                            EventSeverity.EMERGENCY -> Icons.Default.Warning
                            EventSeverity.WARNING -> Icons.Default.BatteryAlert
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (log.severity) {
                            EventSeverity.EMERGENCY -> Color(0xFFD32F2F)
                            EventSeverity.WARNING -> Color(0xFFF57F17)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = log.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text(text = timeStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = log.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "العضو: ${log.userName}",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
