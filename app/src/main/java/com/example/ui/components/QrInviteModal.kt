package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyEntity

@Composable
fun QrInviteModal(
    family: FamilyEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val inviteCode = family?.inviteCode ?: "FG-8942"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "دعوة العائلة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "رمز دعوة العائلة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "دعوة أفراد العائلة للانضمام ومشاركة الموقع بحماية كاملة",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vector Canvas QR Code simulation
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val tileSize = size.width / 9f
                        val darkColor = Color(0xFF0F382C)

                        // Outer position markers
                        drawRect(darkColor, topLeft = Offset(0f, 0f), size = Size(tileSize * 3, tileSize * 3))
                        drawRect(Color.White, topLeft = Offset(tileSize, tileSize), size = Size(tileSize, tileSize))

                        drawRect(darkColor, topLeft = Offset(tileSize * 6, 0f), size = Size(tileSize * 3, tileSize * 3))
                        drawRect(Color.White, topLeft = Offset(tileSize * 7, tileSize), size = Size(tileSize, tileSize))

                        drawRect(darkColor, topLeft = Offset(0f, tileSize * 6), size = Size(tileSize * 3, tileSize * 3))
                        drawRect(Color.White, topLeft = Offset(tileSize, tileSize * 7), size = Size(tileSize, tileSize))

                        // Random tiles pattern representing code
                        val tiles = listOf(
                            Pair(4, 1), Pair(4, 2), Pair(4, 4), Pair(4, 6),
                            Pair(1, 4), Pair(2, 4), Pair(6, 4), Pair(7, 4),
                            Pair(3, 3), Pair(5, 5), Pair(6, 6), Pair(8, 8),
                            Pair(3, 7), Pair(7, 3), Pair(5, 2), Pair(2, 5)
                        )
                        tiles.forEach { (col, row) ->
                            drawRect(darkColor, topLeft = Offset(col * tileSize, row * tileSize), size = Size(tileSize, tileSize))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Invitation Code Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "رمز الانضمام 6 أرقام:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = inviteCode,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 2.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Family Invite Code", inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ رمز الدعوة", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرمز")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "إغلاق")
            }
        }
    )
}
