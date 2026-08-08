package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlaceType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceModal(
    onDismiss: () -> Unit,
    onAddPlace: (name: String, type: PlaceType, lat: Double, lng: Double, radius: Int) -> Unit
) {
    var placeName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PlaceType.HOME) }
    var radiusSlider by remember { mutableFloatStateOf(150f) }

    val defaultLat = 24.7136 + (Math.random() * 0.005)
    val defaultLng = 46.6753 + (Math.random() * 0.005)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddLocation, contentDescription = "إضافة مكان", tint = MaterialTheme.colorScheme.primary)
                Text(text = "إضافة مكان مهم (Geofence)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("اسم المكان (مثلاً: بيت الجدة، النادي)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(text = "نوع المكان:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(PlaceType.HOME, PlaceType.SCHOOL, PlaceType.WORK, PlaceType.CLUB).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.labelAr, fontSize = 12.sp) }
                        )
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "نطاق التنبيه الجغرافي:", fontSize = 13.sp)
                        Text(text = "${radiusSlider.roundToInt()} متر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = radiusSlider,
                        onValueChange = { radiusSlider = it },
                        valueRange = 50f..1000f,
                        steps = 19
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (placeName.isNotBlank()) {
                        onAddPlace(placeName, selectedType, defaultLat, defaultLng, radiusSlider.roundToInt())
                    }
                },
                enabled = placeName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ المكان")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
