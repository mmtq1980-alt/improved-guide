package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.PlaceEntity
import com.example.data.PlaceType

@Composable
fun PlacesScreen(
    places: List<PlaceEntity>,
    onOpenAddPlace: () -> Unit,
    onDeletePlace: (PlaceEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الأماكن المهمة (Geofencing) 📍",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "احصل على تنبيهات فورية تلقائية عند وصول أو مغادرة أي فرد من أفراد عائلتك لهذه المناطق.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onOpenAddPlace,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة")
                    }
                }
            }
        }

        items(places, key = { it.id }) { place ->
            PlaceCard(
                place = place,
                onDelete = { onDeletePlace(place) }
            )
        }
    }
}

@Composable
fun PlaceCard(
    place: PlaceEntity,
    onDelete: () -> Unit
) {
    var enterNotify by remember { mutableStateOf(place.enterNotify) }
    var exitNotify by remember { mutableStateOf(place.exitNotify) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (place.placeType) {
                                    PlaceType.HOME -> Icons.Default.Home
                                    PlaceType.SCHOOL -> Icons.Default.School
                                    PlaceType.UNIVERSITY -> Icons.Default.AccountBalance
                                    PlaceType.WORK -> Icons.Default.Work
                                    PlaceType.CLUB -> Icons.Default.FitnessCenter
                                    PlaceType.HOSPITAL -> Icons.Default.LocalHospital
                                    else -> Icons.Default.Place
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = place.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${place.placeType.labelAr} • قطر النطاق: ${place.radiusMeters}m",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Gray)
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("تنبيه الوصول:", fontSize = 12.sp)
                    Switch(
                        checked = enterNotify,
                        onCheckedChange = { enterNotify = it },
                        modifier = Modifier.height(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("تنبيه المغادرة:", fontSize = 12.sp)
                    Switch(
                        checked = exitNotify,
                        onCheckedChange = { exitNotify = it },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}
