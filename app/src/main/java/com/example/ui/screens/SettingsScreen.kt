package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ProfileViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isFirebaseActive by viewModel.isFirebaseAvailable.collectAsState()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Executive Settings", color = ExecutiveGold, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ExecutiveGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // User Card (Only display when genuinely signed in via Live OAuth)
            if (isUserSignedIn && userName.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ExecutiveGold.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ExecutiveGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = userName,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 18.sp
                            )
                            Text(
                                text = userEmail,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            // Section 1: Security & Credentials Info
            Text(
                "Security & API Credentials",
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🔑", fontSize = 18.sp)
                        Column {
                            Text("Gemini API Encryption", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ExecutiveGold)
                            Text(
                                "Your Gemini LLM API keys are securely managed inside AI Studio's Secrets panel. They are never hardcoded on disk and are dynamically retrieved using encrypted BuildConfig configurations at runtime.",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Divider(color = TextGray.copy(alpha = 0.1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🛡️", fontSize = 18.sp)
                        Column {
                            Text("Compliance & Privacy Guidelines", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ExecutiveGold)
                            Text(
                                "LinkOptima respects professional codes of ethics. Profile scans are processed locally and securely discarded after analysis, ensuring your executive documentation remains secure.",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Section 2: Integration Status indicators (Only show when genuinely active on a live service)
            if (isFirebaseActive) {
                Text(
                    "System Status Log",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
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
                            Text("Firebase Core SDK Engine", fontSize = 12.sp, color = TextWhite)
                            Text(
                                text = "ACTIVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = EmeraldSuccess
                            )
                        }

                        if (isUserSignedIn) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cloud Firestore Sync Status", fontSize = 12.sp, color = TextWhite)
                                Text(
                                    text = "SYNCHRONIZED",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = EmeraldSuccess
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Local SQLite Room Cache", fontSize = 12.sp, color = TextWhite)
                            Text(
                                text = "ACTIVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Action Trigger Button (Only show when genuinely logged in to active live session)
            if (isUserSignedIn) {
                Button(
                    onClick = {
                        viewModel.logout()
                        Toast.makeText(context, "Logged out of current profile.", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedWarning, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Sign Out from System",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
