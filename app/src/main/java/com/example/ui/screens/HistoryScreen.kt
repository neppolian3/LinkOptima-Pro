package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileReview
import com.example.viewmodel.ProfileViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    val reviews by viewModel.allReviews.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredReviews = remember(reviews, searchQuery) {
        if (searchQuery.isBlank()) {
            reviews
        } else {
            reviews.filter {
                it.profileName.contains(searchQuery, ignoreCase = true) ||
                it.inputMethod.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Review History", color = ExecutiveGold, fontFamily = FontFamily.Serif) },
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Professional Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, URL, or method...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ExecutiveGold) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ExecutiveGold,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.2f),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (filteredReviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No reviews recorded yet" else "No matching reviews found",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analyze your first LinkedIn URL or PDF profile above.",
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredReviews) { review ->
                        HistoryCardRow(
                            review = review,
                            onChoose = {
                                viewModel.selectReview(review)
                                onNavigateToDetail()
                            },
                            onDelete = {
                                viewModel.deleteReview(review)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCardRow(
    review: ProfileReview,
    onChoose: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val timeLabel = formatter.format(Date(review.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChoose() }
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (review.inputMethod == "URL") "🔗" else "📄",
                        fontSize = 14.sp
                    )
                    Text(
                        text = review.profileName,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (review.score >= 85) EmeraldSuccess.copy(alpha = 0.15f)
                            else ExecutiveGold.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = review.score.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (review.score >= 85) EmeraldSuccess else ExecutiveGold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (review.profileUrl.isNotBlank() && review.profileUrl != "PDF Upload") {
                    Text(
                        text = review.profileUrl,
                        fontSize = 11.sp,
                        color = ExecutiveGold.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = RedWarning.copy(alpha = 0.7f)
                )
            }
        }
    }
}
