package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
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
import com.example.util.PdfExtractor
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.ProfileReviewState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReviewScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Inputs
    var targetRole by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("Job search") }
    var inputMethod by remember { mutableStateOf("PDF") } // "PDF", "TEXT", "URL"
    
    var linkedinUrl by remember { mutableStateOf("") }
    var pastedProfileText by remember { mutableStateOf("") }
    
    var pickedPdfUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pickedPdfName by remember { mutableStateOf("") }
    var pdfExtractedText by remember { mutableStateOf("") }

    val reviewState by viewModel.reviewState.collectAsState()
    var showUrlBlockedDialog by remember { mutableStateOf(false) }

    val goalsList = listOf(
        "Job search",
        "Promotion",
        "Personal branding",
        "Leadership branding",
        "Business founder branding",
        "Healthcare professional branding",
        "UAE job market positioning"
    )

    // PDF File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pickedPdfUri = uri
            pickedPdfName = uri.path?.substringAfterLast("/") ?: "linkedin_profile.pdf"
            
            // Extract text immediately to preview
            val text = PdfExtractor.extractTextFromPdf(context, uri)
            if (text.isNotBlank()) {
                pdfExtractedText = text
                Toast.makeText(context, "PDF text extracted successfully!", Toast.LENGTH_SHORT).show()
            } else {
                pdfExtractedText = ""
                Toast.makeText(context, "Unable to extract text from PDF. Ensure it is readable.", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(reviewState) {
        if (reviewState is ProfileReviewState.Success) {
            viewModel.clearState()
            onNavigateToResult()
        }
    }

    Scaffold(
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Profile Strategist", color = ExecutiveGold, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ExecutiveGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (reviewState is ProfileReviewState.Loading) {
                // Polished Loading Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepSlateBg.copy(alpha = 0.95f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(color = ExecutiveGold, strokeWidth = 4.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analyzing Profile...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExecutiveGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extracting details, running score engine checks and writing suggestions...",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Real Profile Optimization",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    // Error Box
                    if (reviewState is ProfileReviewState.Error) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RedWarning.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RedWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = (reviewState as ProfileReviewState.Error).message,
                                color = RedWarning,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // 1. Target Role Text Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Target Role / Job Title",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = targetRole,
                            onValueChange = { targetRole = it },
                            placeholder = { Text("e.g. Senior Product Manager or Medical Director", color = TextGray.copy(alpha = 0.6f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ExecutiveGold,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("target_role_field")
                        )
                    }

                    // 2. Career Positioning Mode Dropdown Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Career Positioning Goal",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        
                        var expandedGoals by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedGoal,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expandedGoals = true }) {
                                        Text("▼", color = ExecutiveGold, fontSize = 12.sp)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ExecutiveGold,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedGoals = true }
                            )
                            DropdownMenu(
                                expanded = expandedGoals,
                                onDismissRequest = { expandedGoals = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(SlateCard)
                            ) {
                                goalsList.forEach { goal ->
                                    DropdownMenuItem(
                                        text = { Text(goal, color = TextWhite) },
                                        onClick = {
                                            selectedGoal = goal
                                            expandedGoals = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Three-Tab Input Segment Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SlateCard)
                            .padding(4.dp)
                    ) {
                        val methods = listOf("PDF" to "Upload PDF", "TEXT" to "Paste Text", "URL" to "LinkedIn URL")
                        methods.forEach { (m, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (inputMethod == m) ExecutiveGold else Color.Transparent)
                                    .clickable { inputMethod = m }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (inputMethod == m) Color.Black else TextGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 4. Input Fields conditional renderer
                    when (inputMethod) {
                        "PDF" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SlateCard)
                                        .border(1.dp, if (pickedPdfUri != null) ExecutiveGold else TextGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                        .clickable { filePickerLauncher.launch("application/pdf") }
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (pickedPdfUri != null) "📄" else "📤", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (pickedPdfUri != null) pickedPdfName else "Upload LinkedIn PDF",
                                            fontWeight = FontWeight.Bold,
                                            color = if (pickedPdfUri != null) ExecutiveGold else TextWhite
                                        )
                                        Text(
                                            "Exported PDF directly from your LinkedIn profile",
                                            fontSize = 11.sp,
                                            color = TextGray,
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }

                                // Show Profile Preview box if PDF is extracted
                                if (pdfExtractedText.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Extracted Profile Preview", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(100.dp, 160.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SlateSurface)
                                                .border(0.5.dp, TextGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = pdfExtractedText,
                                                color = TextWhite.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.verticalScroll(rememberScrollState())
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "TEXT" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Paste LinkedIn Profile Text Details", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                OutlinedTextField(
                                    value = pastedProfileText,
                                    onValueChange = { pastedProfileText = it },
                                    placeholder = { Text("Paste your headline, about bio and resume history from LinkedIn directly here...", color = TextGray.copy(alpha = 0.5f)) },
                                    minLines = 6,
                                    maxLines = 10,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ExecutiveGold,
                                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("paste_text_field")
                                )

                                // Live text profile preview
                                if (pastedProfileText.trim().isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                                        Text("Entered Profile Preview", color = ExecutiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(80.dp, 140.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SlateSurface)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                pastedProfileText,
                                                color = TextWhite.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.verticalScroll(rememberScrollState())
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "URL" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = linkedinUrl,
                                    onValueChange = { linkedinUrl = it },
                                    placeholder = { Text("e.g. https://www.linkedin.com/in/username", color = TextGray.copy(alpha = 0.6f)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ExecutiveGold,
                                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("url_text_field")
                                )

                                // WARNING BOX: Clearly ask the user to upload PDF or paste profile text if URL scanner experiences private walls.
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.8f)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(0.5.dp, ExecutiveGold.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Alert",
                                            tint = ExecutiveGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                "LinkedIn URL Access Restrictive",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ExecutiveGold
                                            )
                                            Text(
                                                "LinkedIn enforces aggressive login sign-in walls, blocking automated URL crawlers. For a pristine, highly authentic analysis, we strongly recommend that you upload your Profile PDF or paste your Profile copy text above.",
                                                fontSize = 11.sp,
                                                color = TextGray,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary ACTION BUTTON: "Generate Optimized Content"
                    Button(
                        onClick = {
                            if (targetRole.isBlank()) {
                                Toast.makeText(context, "Please write a Target Role first.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            when (inputMethod) {
                                "PDF" -> {
                                    if (pickedPdfUri == null || pdfExtractedText.isBlank()) {
                                        Toast.makeText(context, "Please upload a readable LinkedIn profile PDF.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.startPdfAnalysis(pdfExtractedText, targetRole, selectedGoal) {}
                                    }
                                }
                                "TEXT" -> {
                                    if (pastedProfileText.trim().length < 15) {
                                        Toast.makeText(context, "Please paste profile text (minimum 15 characters).", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.startTextAnalysis(pastedProfileText, targetRole, selectedGoal) {}
                                    }
                                }
                                "URL" -> {
                                    showUrlBlockedDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_button")
                    ) {
                        Text(
                            text = "Generate Optimized Content",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showUrlBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showUrlBlockedDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚠️", fontSize = 24.sp)
                    Text("URL Access Restricted", color = ExecutiveGold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                }
            },
            text = {
                Text(
                    text = "LinkedIn implements secure sign-in firewalls which block automated URL profile crawlers. To ensure an authentic, highly accurate Gemini analysis and protect your brand strategy, please upload your exported LinkedIn Profile PDF or copy-paste your profile text instead.",
                    color = TextWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            containerColor = SlateCard,
            confirmButton = {
                Button(
                    onClick = {
                        showUrlBlockedDialog = false
                        inputMethod = "PDF" // Seamlessly direct user to the PDF option
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Use PDF Option", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUrlBlockedDialog = false }
                ) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}
