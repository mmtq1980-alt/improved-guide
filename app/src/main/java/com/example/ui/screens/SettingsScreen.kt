package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettingsEntity

@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity?,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleLanguage: (String) -> Unit,
    onToggleLocationSharing: (Boolean) -> Unit,
    onUpdateInterval: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var isSharingActive by remember { mutableStateOf(true) }
    var batterySaver by remember { mutableStateOf(userSettings?.batterySaverEnabled ?: false) }
    var autoBackup by remember { mutableStateOf(userSettings?.autoBackupEnabled ?: true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "إعدادات التطبيق والخصوصية ⚙️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Appearance & Language Group
        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "المظهر واللغة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DarkMode, contentDescription = null)
                            Text("الوضع الليلي (Dark Mode)", fontSize = 13.sp)
                        }
                        Switch(
                            checked = userSettings?.isDarkMode ?: true,
                            onCheckedChange = { onToggleDarkMode(it) }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Text("لغة الواجهة (Language)", fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = (userSettings?.language ?: "ar") == "ar",
                                onClick = { onToggleLanguage("ar") },
                                label = { Text("العربية") }
                            )
                            FilterChip(
                                selected = (userSettings?.language ?: "ar") == "en",
                                onClick = { onToggleLanguage("en") },
                                label = { Text("English") }
                            )
                        }
                    }
                }
            }
        }

        // Privacy & Battery Optimization Group
        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "الخصوصية وتوفير الطاقة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Text("مشاركة موقعي الجغرافي", fontSize = 13.sp)
                        }
                        Switch(
                            checked = isSharingActive,
                            onCheckedChange = {
                                isSharingActive = it
                                onToggleLocationSharing(it)
                            }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.BatterySaver, contentDescription = null)
                            Text("معدل التحديث الذكي لتوفير البطارية", fontSize = 13.sp)
                        }
                        Switch(
                            checked = batterySaver,
                            onCheckedChange = { batterySaver = it }
                        )
                    }
                }
            }
        }

        // Web Admin Dashboard Preview Button
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "لوحة تحكم الويب للمشرف (Web Admin Panel)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "يمكنك إدارة أفراد العائلة والإعدادات المتقدمة من خلال المتصفح مباشرة.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "رابط لوحة التحكم: https://admin.familyguardian.app", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح لوحة التحكم عبر الويب")
                    }
                }
            }
        }

        // Logout Button
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل الخروج من جميع الأجهزة")
            }
        }
    }
}
