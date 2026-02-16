package com.radarlabs.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdFreeStatusCard() {
    var adFreeUntil by remember { mutableStateOf<Date?>(null) }
    var supportTier by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        Firebase.firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val timestamp = snapshot.getTimestamp("adFreeUntil")
                    adFreeUntil = timestamp?.toDate()
                    supportTier = snapshot.getString("supportTier")
                }
                isLoading = false
            }
    }

    if (isLoading) return

    val now = Date()
    val isAdFree = adFreeUntil != null && adFreeUntil!!.after(now)

    if (isAdFree) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.3f),
                            Color(0xFF34D399).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B263B),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "✨ Ad-Free Active",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))

                    val isLifetime = supportTier == "lifetime"
                    if (isLifetime) {
                        Text(
                            "Forever • Thank you for your support! 💚",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    } else {
                        Text(
                            "Until ${formatDate(adFreeUntil!!)}",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}
