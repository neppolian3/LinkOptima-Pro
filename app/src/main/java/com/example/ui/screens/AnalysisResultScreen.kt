package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileReview
import com.example.viewmodel.ProfileViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOptimized: () -> Unit
) {
    val review by viewModel.selectedReview.collectAsState()
    var resultSelectedTab by remember { mutableStateOf(0) } // 0: Scorecard, 1: Before vs After, 2: Readiness, 3: Truth Check

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Profile Strategy Audit", color = ExecutiveGold, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ExecutiveGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        }
    ) { innerPadding ->
        
        if (review == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No review selected. Please run an analysis first.", color = TextGray)
            }
        } else {
            val finalReview = review!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Profile summary title
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = finalReview.profileName,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(ExecutiveGold.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Score: ${finalReview.score} / 100",
                                color = ExecutiveGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Target: ${finalReview.targetRole} • Goal: ${finalReview.selectedGoal}",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Custom luxury navigation tabs for multi-category result
                ScrollableTabRow(
                    selectedTabIndex = resultSelectedTab,
                    containerColor = SlateSurface,
                    contentColor = ExecutiveGold,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    indicator = { tabPositions ->
                        if (resultSelectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[resultSelectedTab]),
                                color = ExecutiveGold
                            )
                        }
                    },
                    divider = @Composable { Divider(color = Color.Transparent) }
                ) {
                    val auditTabs = listOf("Scorecard", "Before vs After", "Readiness Report", "Truth Check")
                    auditTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = resultSelectedTab == index,
                            onClick = { resultSelectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (resultSelectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            selectedContentColor = ExecutiveGold,
                            unselectedContentColor = TextGray
                        )
                    }
                }

                // Content render based on selection
                Box(modifier = Modifier.weight(1f)) {
                    when (resultSelectedTab) {
                        0 -> ScorecardTab(finalReview)
                        1 -> BeforeAfterTab(finalReview)
                        2 -> ReadinessReportTab(finalReview, onNavigateToOptimized)
                        3 -> TruthCheckTab(finalReview)
                    }
                }
            }
        }
    }
}

// ---------------- SCORECARD TAB COMPONENT ----------------
@Composable
fun ScorecardTab(review: ProfileReview) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "OVERALL AUDIT SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(DeepSlateBg)
                        .border(3.dp, ExecutiveGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${review.score}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = ExecutiveGold
                        )
                        Text("/ 100", fontSize = 10.sp, color = TextGray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = review.overallCritique,
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            "SCORE BREAKDOWN",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ExecutiveGold,
            letterSpacing = 0.5.sp
        )

        val breakdownList = listOf(
            "Headline Strength" to review.scoreHeadline,
            "About Section Strength" to review.scoreAbout,
            "Experience Impact" to review.scoreExperience,
            "Keyword Optimization" to review.scoreKeywords,
            "Leadership Positioning" to review.scoreLeadership,
            "Recruiter Searchability" to review.scoreRecruiter,
            "ATS Friendliness" to review.scoreAts,
            "Credibility & Authenticity" to review.scoreCredibility
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                breakdownList.forEach { (label, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("$value / 100", color = ExecutiveGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = value / 100f,
                            color = if (value >= 85) EmeraldSuccess else ExecutiveGold,
                            trackColor = DeepSlateBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------- BEFORE VS AFTER TAB COMPONENT ----------------
@Composable
fun BeforeAfterTab(review: ProfileReview) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "SECTION-WISE OPTIMIZATION",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ExecutiveGold,
            letterSpacing = 0.5.sp
        )

        // Sections List
        val comparisonSections = listOf(
            Triple("Headline Section", review.headlineOriginal, review.headlineOptimized),
            Triple("About Summary Section", review.aboutOriginal, review.aboutOptimized),
            Triple("Experience Highlights", review.experienceOriginal, review.experienceOptimized)
        )

        comparisonSections.forEach { (sectionTitle, original, optimized) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(sectionTitle, color = ExecutiveGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    // Side by side or stacked based on content length
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Current (Before)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DeepSlateBg.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, RedWarning.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("BEFORE (Original Profile)", color = RedWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (original.isNullOrBlank()) "No original content provided or detected." else original,
                                color = TextWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        // Proposed (After)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExecutiveGold.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, ExecutiveGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("AFTER (Optimized Rewriting)", color = ExecutiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = optimized,
                                color = TextWhite,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Section-specific Critique feedback
                    val feedbackText = when(sectionTitle) {
                        "Headline Section" -> review.sectionCritiqueHeadline
                        "About Summary Section" -> review.sectionCritiqueAbout
                        else -> review.sectionCritiqueExperience
                    }

                    if (feedbackText.isNotBlank()) {
                        Divider(color = TextGray.copy(alpha = 0.1f))
                        Text("Critique Breakdown:", color = ExecutiveGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = feedbackText,
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------- READINESS REPORT TAB COMPONENT ----------------
@Composable
fun ReadinessReportTab(review: ProfileReview, onNavigateToGenerator: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📋", fontSize = 20.sp)
                    Text("LinkedIn Readiness Report", color = ExecutiveGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Divider(color = TextGray.copy(alpha = 0.12f))

                // Top 5 Urgent Fixes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Top 5 Urgent Fixes", color = ExecutiveGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSlateBg.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = review.top5Fixes,
                            color = TextWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Best Headline Suggestion
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Top Recommended Headline", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(review.bestHeadline, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Best About Suggestion
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Top Recommended Bio Summary", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(review.bestAbout, color = TextGray, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }

                // Positioning Summary
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Profile Positioning Summary", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(review.positioningSummary, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                }

                // Recruiter Impression Summary
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recruiter Impression Summary", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(review.recruiterImpression, color = TextGray, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        Button(
            onClick = onNavigateToGenerator,
            colors = ButtonDefaults.buttonColors(containerColor = ExecutiveGold, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Launch AI Brand Content Generator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------- TRUTH-SAFE AI TAB COMPONENT ----------------
@Composable
fun TruthCheckTab(review: ProfileReview) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️", fontSize = 22.sp)
                    Text("Truth-Safe AI SafeGuard", color = ExecutiveGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = TextGray.copy(alpha = 0.12f))

                Text(
                    text = "LinkOptima Pro enforces strict authenticity compliance. The AI does not fabricate achievements, scale size numbers, certificates, awards, degrees, or employers. Our engine polishes your genuine experience with recruiter-level articulation, and presents structural placeholders for metrics where you can add validated business impact.",
                    fontSize = 12.sp,
                    color = TextGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Where to Add Measurable Metrics:", color = ExecutiveGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlateBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = review.truthSafeSuggestions,
                        color = TextWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
