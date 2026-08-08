package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyRole
import com.example.data.PlaceEntity
import com.example.data.TripEntity
import com.example.data.UserEntity
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveMapCanvas(
    members: List<UserEntity>,
    places: List<PlaceEntity>,
    activeTrip: TripEntity?,
    selectedMemberId: String?,
    onMemberSelect: (String) -> Unit,
    showTraffic: Boolean = true,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isSatelliteMode by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()

    val mapBgColor = if (isSatelliteMode) Color(0xFF1B231F) else Color(0xFFE8EFEA)
    val roadColor = if (isSatelliteMode) Color(0xFF2C3932) else Color(0xFFFFFFFF)
    val mainRoadColor = if (isSatelliteMode) Color(0xFF3F4E45) else Color(0xFFD6E3DB)
    val trafficColor = Color(0xFFE53935)
    val geofenceBorderColor = Color(0xFF006C4C)

    val baseLat = 24.7136
    val baseLng = 46.6753

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(mapBgColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f + panOffset.x, height / 2f + panOffset.y)

            // 1. Draw Map Grid / Roads
            val gridStep = 80.dp.toPx() * scale
            val startX = (center.x % gridStep) - gridStep
            val startY = (center.y % gridStep) - gridStep

            var x = startX
            while (x < width + gridStep) {
                drawLine(
                    color = roadColor.copy(alpha = 0.7f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 14.dp.toPx() * scale
                )
                x += gridStep
            }

            var y = startY
            while (y < height + gridStep) {
                drawLine(
                    color = roadColor.copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 14.dp.toPx() * scale
                )
                y += gridStep
            }

            // Diagonal Main Highways
            drawLine(
                color = mainRoadColor,
                start = Offset(center.x - 600 * scale, center.y - 400 * scale),
                end = Offset(center.x + 600 * scale, center.y + 400 * scale),
                strokeWidth = 24.dp.toPx() * scale
            )

            // Traffic layer line if enabled
            if (showTraffic) {
                drawLine(
                    color = trafficColor.copy(alpha = 0.6f),
                    start = Offset(center.x + 50 * scale, center.y - 120 * scale),
                    end = Offset(center.x + 220 * scale, center.y + 180 * scale),
                    strokeWidth = 8.dp.toPx() * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
            }

            // Function to convert Lat/Lng to Canvas Coordinates
            fun latLngToOffset(lat: Double, lng: Double): Offset {
                val dx = ((lng - baseLng) * 18000).toFloat() * scale
                val dy = ((baseLat - lat) * 18000).toFloat() * scale
                return Offset(center.x + dx, center.y + dy)
            }

            // 2. Draw Geofence Places
            places.forEach { place ->
                val placePos = latLngToOffset(place.latitude, place.longitude)
                val radiusPx = (place.radiusMeters / 2.5f) * scale

                // Filled area
                drawCircle(
                    color = geofenceBorderColor.copy(alpha = 0.12f),
                    radius = radiusPx,
                    center = placePos
                )
                // Border
                drawCircle(
                    color = geofenceBorderColor.copy(alpha = pulseAlpha),
                    radius = radiusPx,
                    center = placePos,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                )

                // Label
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString(place.name),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D36)
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(placePos.x - textLayout.size.width / 2f, placePos.y - radiusPx - 20)
                )
            }

            // 3. Draw Active Trip Polyline if exists
            if (activeTrip != null) {
                val startPos = latLngToOffset(activeTrip.startLatitude, activeTrip.startLongitude)
                val endPos = latLngToOffset(activeTrip.destLatitude, activeTrip.destLongitude)
                val currPos = latLngToOffset(activeTrip.currentLatitude, activeTrip.currentLongitude)

                val routePath = Path().apply {
                    moveTo(startPos.x, startPos.y)
                    lineTo(currPos.x, currPos.y)
                    lineTo(endPos.x, endPos.y)
                }

                drawPath(
                    path = routePath,
                    color = Color(0xFFFF9800),
                    style = Stroke(width = 5.dp.toPx() * scale, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f))
                )

                // Start Marker
                drawCircle(color = Color(0xFF4CAF50), radius = 8.dp.toPx(), center = startPos)
                // Destination Marker
                drawCircle(color = Color(0xFFE53935), radius = 10.dp.toPx(), center = endPos)
            }

            // 4. Draw Family Member Markers
            members.forEach { member ->
                val pos = latLngToOffset(member.latitude, member.longitude)
                val isSelected = member.id == selectedMemberId

                // Selection pulse
                if (isSelected) {
                    drawCircle(
                        color = Color(0xFF2CE1A0).copy(alpha = pulseAlpha),
                        radius = 28.dp.toPx() * scale,
                        center = pos
                    )
                }

                // Marker background bubble
                val markerBg = when (member.role) {
                    FamilyRole.FATHER -> Color(0xFF006C4C)
                    FamilyRole.MOTHER -> Color(0xFF9C27B0)
                    FamilyRole.SON -> Color(0xFF0288D1)
                    FamilyRole.DAUGHTER -> Color(0xFFE91E63)
                    else -> Color(0xFF607D8B)
                }

                drawCircle(
                    color = Color.White,
                    radius = 20.dp.toPx() * scale,
                    center = pos
                )
                drawCircle(
                    color = markerBg,
                    radius = 17.dp.toPx() * scale,
                    center = pos
                )

                // Role Initial Text
                val roleInitial = member.name.take(1)
                val initialLayout = textMeasurer.measure(
                    text = AnnotatedString(roleInitial),
                    style = TextStyle(
                        fontSize = (14 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                drawText(
                    textLayoutResult = initialLayout,
                    topLeft = Offset(pos.x - initialLayout.size.width / 2f, pos.y - initialLayout.size.height / 2f)
                )

                // Battery Badge
                val batteryBg = if (member.batteryLevel <= 20) Color(0xFFE53935) else Color(0xFF4CAF50)
                drawCircle(
                    color = batteryBg,
                    radius = 6.dp.toPx() * scale,
                    center = Offset(pos.x + 14.dp.toPx() * scale, pos.y - 14.dp.toPx() * scale)
                )

                // Name Tag Card below marker
                val nameTag = "${member.name} (${member.batteryLevel}%)"
                val tagLayout = textMeasurer.measure(
                    text = AnnotatedString(nameTag),
                    style = TextStyle(
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSatelliteMode) Color.White else Color(0xFF101714)
                    )
                )
                drawText(
                    textLayoutResult = tagLayout,
                    topLeft = Offset(pos.x - tagLayout.size.width / 2f, pos.y + 22.dp.toPx() * scale)
                )
            }
        }

        // Map Control Floating Buttons (Layer toggle, Zoom +, Zoom -, Reset)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { isSatelliteMode = !isSatelliteMode },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isSatelliteMode) Icons.Default.Map else Icons.Default.Layers,
                    contentDescription = "الطبقات"
                )
            }

            SmallFloatingActionButton(
                onClick = { scale = (scale + 0.3f).coerceAtMost(3.5f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير")
            }

            SmallFloatingActionButton(
                onClick = { scale = (scale - 0.3f).coerceAtLeast(0.6f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير")
            }

            SmallFloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    scale = 1f
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "إعادة الضبط")
            }
        }

        // Selected Member Details Overlay Banner at bottom
        val selectedUser = members.find { it.id == selectedMemberId }
        if (selectedUser != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 6.dp
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedUser.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Column {
                            Text(
                                text = selectedUser.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = selectedUser.currentPlaceName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${selectedUser.speedKmh.roundToInt()} كم/س",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedUser.batteryLevel <= 20) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selectedUser.batteryLevel <= 20) Icons.Default.BatteryAlert else Icons.Default.BatteryStd,
                                    contentDescription = "البطارية",
                                    tint = if (selectedUser.batteryLevel <= 20) Color.Red else Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedUser.batteryLevel}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedUser.batteryLevel <= 20) Color.Red else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
