package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: ProfileViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isFirebaseActive by viewModel.isFirebaseAvailable.collectAsState()

    // Activity Launcher for official Google OAuth flow
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogleCredential(idToken) { success ->
                    if (success) {
                        Toast.makeText(context, "Welcome to LinkOptima Pro!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Firebase authentication failed.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Google Sign-In failed: Client ID/Token unconfigured.", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google authentication request failed: Code ${e.statusCode}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Authentication process error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSlateBg, SlateSurface)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Stylized Logo Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ExecutiveGold, GoldAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔗",
                    fontSize = 40.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Luxury Typography Header
            Text(
                text = "LinkOptima Pro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ExecutiveGold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = "Elevate Professional Branding via Expert AI Review",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Credentials Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ExecutiveGold.copy(alpha = 0.25f), RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Executive Authentication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    if (isFirebaseActive) {
                        Text(
                            text = "A secure connection is active. Sign in with your real Google Account to authenticate and save reports securely in the cloud.",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Official Google Sign-In Trigger Button
                        Button(
                            onClick = {
                                val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(if (webClientId.isNotEmpty() && webClientId != "YOUR_GOOGLE_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE") webClientId else "")
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExecutiveGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("submit_button")
                        ) {
                            Text(
                                text = "Sign In with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Unconfigured Live Firebase State Visual Card (Honest representation of connection status)
                        Text(
                            text = "Cloud Integration is unconfigured. Google OAuth and Firestore sync are disabled as no valid server setup is present.",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SlateSurface,
                                contentColor = ExecutiveGold
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("submit_button")
                        ) {
                            Text(
                                text = "Explore App (Local Sandbox)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By accessing LinkOptima, you agree to our terms of professional ethics and standard data processing.",
                fontSize = 11.sp,
                color = TextGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
