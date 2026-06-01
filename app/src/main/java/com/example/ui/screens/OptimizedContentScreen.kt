package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ProfileViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedContentScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val review by viewModel.selectedReview.collectAsState()
    
    // 0: Headlines, 1: About Bios, 2: Experience, 3: Skills & Keywords, 4: Brand Tools, 5: 30-Day Content Plan
    var selectedTab by remember { mutableStateOf(0) } 

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Brand Generator", color = ExecutiveGold, fontFamily = FontFamily.Serif) },
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
                Text("No data to optimize. Please run a review first.", color = TextGray)
            }
        } else {
            val finalReview = review!!

            val tabTitles = listOf("5 Headlines", "3 About Bios", "Work Bullets", "Keywords & SEO", "Branding Tools", "30-Day Content")
            
            val activeContentText = remember(selectedTab, finalReview) {
                when (selectedTab) {
                    0 -> finalReview.genHeadlines
                    1 -> finalReview.genAbouts
                    2 -> finalReview.genExperienceBullets
                    3 -> """
                        [RECOMMENDED STRATEGIC SKILLS]
                        ${finalReview.genSkills}
                        
                        [RECRUITER SEARCH KEYWORDS]
                        ${finalReview.genRecruiterKeywords}
                    """.trimIndent()
                    4 -> """
                        [PERSONAL BRAND TAGLINE]
                        ${finalReview.genTagline}
                        
                        [LINKEDIN BACKGROUND BANNER TEXT]
                        ${finalReview.genBannerText}
                        
                        [FEATURED PORTFOLIO SECTION IDEAS]
                        ${finalReview.genFeatured}
                    """.trimIndent()
                    else -> finalReview.genContentPlan30Days
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI Optimized Branding Copy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Copy these strategic variations directly into LinkedIn to secure recruiter attraction and ATS visibility.",
                    fontSize = 12.sp,
                    color = TextGray,
                    lineHeight = 16.sp
                )

                // Scrollable tab row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateSurface,
                    contentColor = ExecutiveGold,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ExecutiveGold
                            )
                        }
                    },
                    divider = @Composable { Divider(color = Color.Transparent) }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            selectedContentColor = ExecutiveGold,
                            unselectedContentColor = TextGray
                        )
                    }
                }

                // Main Output Display Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateCard)
                        .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeContentText,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth().testTag("active_copy_text")
                        )
                    }
                }

                // Copy Action Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Optimized Profile Copy", activeContentText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "${tabTitles[selectedTab]} copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("copy_details_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋", fontSize = 18.sp)
                        Text(
                            text = "Copy ${tabTitles[selectedTab]}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
