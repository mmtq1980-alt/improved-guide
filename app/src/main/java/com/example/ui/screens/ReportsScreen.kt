package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.TripEntity
import com.example.data.UserEntity

@Composable
fun ReportsScreen(
    members: List<UserEntity>,
    allTrips: List<TripEntity>
) {
    var reportRange by remember { mutableStateOf("WEEK") } // "DAY", "WEEK", "MONTH"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "تقارير الحركة والإحصائيات 📊",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Time Filter Selector
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = reportRange == "DAY",
                    onClick = { reportRange = "DAY" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("تقرير اليوم")
                }
                SegmentedButton(
                    selected = reportRange == "WEEK",
                    onClick = { reportRange = "WEEK" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("تقرير الأسبوع")
                }
                SegmentedButton(
                    selected = reportRange == "MONTH",
                    onClick = { reportRange = "MONTH" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("تقرير الشهر")
                }
            }
        }

        // Overview Summary Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "إجمالي الرحلات",
                    value = "${if (reportRange == "DAY") 4 else if (reportRange == "WEEK") 26 else 114}",
                    subtitle = "رحلة موثقة",
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "الوقت داخل المنزل",
                    value = "${if (reportRange == "DAY") "78%" else if (reportRange == "WEEK") "68%" else "72%"}",
                    subtitle = "معدل الأمان الأسري",
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Speed & Safety Insights Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        Text(
                            text = "معدل السرعة والسلامة والمرور 🚦",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "ممتاز 🟢",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { 0.35f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Text(
                        text = "متوسط سرعة تنقل أفراد الأسرة أثناء القيادة: 38 كم/س (ضمن حدود السرعة الآمنة الموصى بها).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Top Visited Spots Header
        item {
            Text(
                text = "الأماكن الأكثر زيارة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TopSpotRow(name = "المنزل الرئيسي", visits = "38 زيارة", percent = 0.85f)
                    HorizontalDivider()
                    TopSpotRow(name = "جامعة الملك سعود", visits = "14 زيارة", percent = 0.55f)
                    HorizontalDivider()
                    TopSpotRow(name = "مدرسة النموذجية", visits = "12 زيارة", percent = 0.45f)
                    HorizontalDivider()
                    TopSpotRow(name = "مقر العمل", visits = "10 زيارات", percent = 0.35f)
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TopSpotRow(name: String, visits: String, percent: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = visits, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
