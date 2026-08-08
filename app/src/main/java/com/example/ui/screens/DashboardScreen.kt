package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.InteractiveMapCanvas
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    members: List<UserEntity>,
    family: FamilyEntity?,
    places: List<PlaceEntity>,
    activeTrip: TripEntity?,
    eventLogs: List<EventLogEntity>,
    selectedMemberId: String?,
    onSelectMember: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onTriggerSos: () -> Unit,
    onOpenQrDialog: () -> Unit,
    onOpenStartTrip: () -> Unit,
    onOpenAddPlace: () -> Unit
) {
    val totalMembers = members.size
    val insideHomeCount = members.count { it.isInsideHome }
    val outsideHomeCount = members.count { !it.isInsideHome }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Family Banner Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Diversity3, contentDescription = null, tint = Color.White)
                            }
                            Column {
                                Text(
                                    text = family?.name ?: "عائلة الأحمد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "كود الدعوة: ${family?.inviteCode ?: "FG-8942"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = onOpenQrDialog) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(title = "إجمالي الأفراد", value = "$totalMembers", icon = Icons.Default.People)
                        StatPill(title = "داخل المنزل 🏠", value = "$insideHomeCount", icon = Icons.Default.Home)
                        StatPill(title = "خارج المنزل 🚗", value = "$outsideHomeCount", icon = Icons.Default.DirectionsCar)
                    }
                }
            }
        }

        // 2. Quick Action Buttons (SOS / Trip Share / Add Place)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Emergency SOS Button
                Button(
                    onClick = onTriggerSos,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🚨 طوارئ SOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Share Trip Button
                OutlinedButton(
                    onClick = onOpenStartTrip,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "رحلة", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مشاركة رحلة", fontSize = 12.sp)
                }
            }
        }

        // 3. Live Interactive Map Preview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    InteractiveMapCanvas(
                        members = members,
                        places = places,
                        activeTrip = activeTrip,
                        selectedMemberId = selectedMemberId,
                        onMemberSelect = onSelectMember
                    )

                    // Expand Map Floating Chip
                    Surface(
                        onClick = onNavigateToMap,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "خريطة كاملة", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "فتح الخريطة الكاملة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Family Members Status Cards Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حالة أفراد العائلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تحديث حي كل 5 ثوانٍ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Family Members List
        items(members, key = { it.id }) { member ->
            MemberCard(
                user = member,
                isSelected = member.id == selectedMemberId,
                onSelect = { onSelectMember(member.id) }
            )
        }

        // 5. Recent Alerts / Audit Log Header & Items
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل التنبيهات والأحداث الأخيرة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(eventLogs.take(3)) { log ->
            EventLogCard(log = log)
        }
    }
}

@Composable
fun StatPill(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MemberCard(
    user: UserEntity,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            when (user.role) {
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
                        text = user.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = user.role.labelAr,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Text(
                        text = "المكان: ${user.currentPlaceName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "السرعة: ${user.speedKmh.roundToInt()} كم/س",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "الاتجاه: ${user.movementDirection}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Battery Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (user.batteryLevel <= 20) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (user.batteryLevel <= 20) Icons.Default.BatteryAlert else Icons.Default.BatteryStd,
                            contentDescription = "بطارية",
                            tint = if (user.batteryLevel <= 20) Color.Red else Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${user.batteryLevel}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.batteryLevel <= 20) Color.Red else Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (user.isInsideHome) "داخل المنزل" else "خارج المنزل",
                    fontSize = 11.sp,
                    color = if (user.isInsideHome) Color(0xFF2E7D32) else Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EventLogCard(log: EventLogEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.severity) {
                EventSeverity.EMERGENCY -> Color(0xFFFFEBEE)
                EventSeverity.WARNING -> Color(0xFFFFF8E1)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when (log.severity) {
                    EventSeverity.EMERGENCY -> Icons.Default.Warning
                    EventSeverity.WARNING -> Icons.Default.BatteryAlert
                    else -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = when (log.severity) {
                    EventSeverity.EMERGENCY -> Color(0xFFD32F2F)
                    EventSeverity.WARNING -> Color(0xFFF57F17)
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = log.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
