package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartTripModal(
    onDismiss: () -> Unit,
    onStartTrip: (destName: String, destLat: Double, destLng: Double) -> Unit
) {
    var destName by remember { mutableStateOf("") }

    val presetDestinations = listOf(
        Triple("جامعة الملك سعود", 24.7286, 46.6873),
        Triple("مطار الملك خالد الدولي", 24.9576, 46.6988),
        Triple("الرياض بارك مول", 24.7554, 46.6321),
        Triple("مستشفى التخصصي", 24.6712, 46.6782)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Navigation, contentDescription = "مشاركة رحلة", tint = MaterialTheme.colorScheme.primary)
                Text(text = "مشاركة رحلة مباشرة مع العائلة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "سيشاهد أفراد عائلتك خط السير والوقت المتوقع للوصول وموقعك المباشر دقيقة بدقيقة.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = destName,
                    onValueChange = { destName = it },
                    label = { Text("الوجهة (أدخل اسم المكان)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(text = "أو اختر وجهة سريعة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetDestinations.forEach { (name, lat, lng) ->
                        ElevatedCard(
                            onClick = {
                                destName = name
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (destName.isNotBlank()) {
                        val preset = presetDestinations.find { it.first == destName }
                        val lat = preset?.second ?: (24.7136 + 0.015)
                        val lng = preset?.third ?: (46.6753 + 0.012)
                        onStartTrip(destName, lat, lng)
                    }
                },
                enabled = destName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بدء ومشاركة الرحلة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
