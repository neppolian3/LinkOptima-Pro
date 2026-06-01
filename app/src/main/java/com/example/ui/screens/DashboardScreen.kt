package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileReview
import com.example.ui.theme.*
import com.example.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ProfileViewModel,
    onNavigateToNewReview: () -> Unit,
    onNavigateToReviewDetail: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val reviews by viewModel.allReviews.collectAsState()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()

    // Calculate average score dynamically
    val avgScore = remember(reviews) {
        if (reviews.isEmpty()) 0 else reviews.map { it.score }.average().toInt()
    }

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🔗", fontSize = 24.sp)
                        Text(
                            text = "LinkOptima Pro",
                            color = ExecutiveGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ExecutiveGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        },
        bottomBar = {
            Surface(
                color = SlateSurface,
                border = BorderStroke(width = 0.5.dp, color = TextGray.copy(alpha = 0.15f)),
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Text("🏠", fontSize = 20.sp)
                        Text("Dashboard", fontSize = 11.sp, color = ExecutiveGold, fontWeight = FontWeight.Bold)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToNewReview() }
                    ) {
                        Text("➕", fontSize = 20.sp)
                        Text("New Review", fontSize = 11.sp, color = TextGray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToHistory() }
                    ) {
                        Text("🕒", fontSize = 20.sp)
                        Text("History", fontSize = 11.sp, color = TextGray)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val greeting = if (isUserSignedIn) "Welcome," else "Offline Mode,"
                        Text(
                            text = greeting,
                            fontSize = 14.sp,
                            color = ExecutiveGold,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = userName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    if (reviews.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ExecutiveGold.copy(alpha = 0.12f))
                                .border(2.dp, ExecutiveGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = avgScore.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExecutiveGold
                                )
                                Text("Avg Score", fontSize = 8.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Quick Banner Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToNewReview() }
                        .border(1.dp, ExecutiveGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SlateCard, SlateSurface)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .background(ExecutiveGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("AI STRATEGIST", color = ExecutiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Analyze LinkedIn Profile",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                "Paste your portfolio link or upload a profile PDF to unlock structured AI reviews & optimizations in seconds.",
                                fontSize = 12.sp,
                                color = TextGray,
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = onNavigateToNewReview,
                                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveGold, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Start Analysis", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Recent Reviews
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Technical Reviews",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View History", color = ExecutiveGold, fontSize = 12.sp)
                    }
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📊", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No profile reviews yet", color = TextWhite, fontWeight = FontWeight.Bold)
                            Text(
                                "Your submitted report history will be listed here. Run your first review now!",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            } else {
                items(reviews.take(3)) { review ->
                    ReviewRowItem(
                        review = review,
                        onClick = {
                            viewModel.selectReview(review)
                            onNavigateToReviewDetail()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewRowItem(
    review: ProfileReview,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(review.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.5.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (review.score >= 85) EmeraldSuccess.copy(alpha = 0.12f)
                            else ExecutiveGold.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (review.inputMethod == "URL") "🔗" else "📄",
                        fontSize = 20.sp
                    )
                }
                Column {
                    Text(
                        text = review.profileName,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$formattedDate • ${review.inputMethod} Source",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = review.score.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (review.score >= 85) EmeraldSuccess else ExecutiveGold
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextGray
                )
            }
        }
    }
}
