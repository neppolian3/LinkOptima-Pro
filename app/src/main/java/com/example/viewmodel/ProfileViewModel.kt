package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ProfileReview
import com.example.data.repository.ProfileRepository
import com.example.data.firebase.FirebaseManager
import com.example.data.remote.LinkedInAiReviewer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProfileReviewState {
    object Idle : ProfileReviewState()
    object Loading : ProfileReviewState()
    data class Success(val review: ProfileReview) : ProfileReviewState()
    data class Error(val message: String) : ProfileReviewState()
}

class ProfileViewModel(
    application: Application,
    private val repository: ProfileRepository
) : AndroidViewModel(application) {

    private val TAG = "ProfileViewModel"

    // Observed local SQLite list reactively updated via Flow
    val allReviews: StateFlow<List<ProfileReview>> = repository.allReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _reviewState = MutableStateFlow<ProfileReviewState>(ProfileReviewState.Idle)
    val reviewState: StateFlow<ProfileReviewState> = _reviewState.asStateFlow()

    private val _selectedReview = MutableStateFlow<ProfileReview?>(null)
    val selectedReview: StateFlow<ProfileReview?> = _selectedReview.asStateFlow()

    // Firebase state tracking
    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    init {
        // Initialize Firebase on start
        FirebaseManager.initialize(application)
        _isFirebaseAvailable.value = FirebaseManager.isFirebaseAvailable
        syncAuthState()
    }

    fun syncAuthState() {
        if (FirebaseManager.isFirebaseAvailable) {
            val signedIn = FirebaseManager.isUserSignedIn()
            _isUserSignedIn.value = signedIn
            if (signedIn) {
                _userEmail.value = FirebaseManager.getCurrentUserEmail()
                _userName.value = FirebaseManager.getCurrentUserName()
            } else {
                _userEmail.value = ""
                _userName.value = ""
            }
        } else {
            _isUserSignedIn.value = false
            _userEmail.value = ""
            _userName.value = ""
        }
    }

    fun selectReview(review: ProfileReview?) {
        _selectedReview.value = review
    }

    fun startUrlAnalysis(url: String, targetRole: String, selectedGoal: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _reviewState.value = ProfileReviewState.Loading
            
            val trimmedUrl = url.trim()
            if (trimmedUrl.isEmpty() || !trimmedUrl.startsWith("http")) {
                _reviewState.value = ProfileReviewState.Error("Please enter a valid LinkedIn profile URL.")
                return@launch
            }

            try {
                // Call Gemini via standard REST API
                val aiOutput = LinkedInAiReviewer.analyzeProfile(
                    inputMethod = "URL",
                    url = trimmedUrl,
                    rawText = "Original Link Reference: $trimmedUrl",
                    targetRole = targetRole,
                    selectedGoal = selectedGoal
                )

                val newReview = ProfileReview(
                    profileName = aiOutput.profileName,
                    profileUrl = trimmedUrl,
                    inputMethod = "URL",
                    targetRole = targetRole,
                    selectedGoal = selectedGoal,
                    score = aiOutput.score,
                    rawExtractedText = "$trimmedUrl (Link Reference Profile)",
                    
                    // Scores
                    scoreHeadline = aiOutput.scoreHeadline,
                    scoreAbout = aiOutput.scoreAbout,
                    scoreExperience = aiOutput.scoreExperience,
                    scoreKeywords = aiOutput.scoreKeywords,
                    scoreLeadership = aiOutput.scoreLeadership,
                    scoreRecruiter = aiOutput.scoreRecruiter,
                    scoreAts = aiOutput.scoreAts,
                    scoreCredibility = aiOutput.scoreCredibility,

                    // Original
                    headlineOriginal = aiOutput.headlineOriginal,
                    aboutOriginal = aiOutput.aboutOriginal,
                    experienceOriginal = aiOutput.experienceOriginal,
                    skillsOriginal = aiOutput.skillsOriginal,

                    // Optimized
                    headlineOptimized = aiOutput.headlineOptimized,
                    aboutOptimized = aiOutput.aboutOptimized,
                    experienceOptimized = aiOutput.experienceOptimized,
                    skillsOptimized = aiOutput.skillsOptimized,

                    // Feedbacks
                    overallCritique = aiOutput.overallCritique,
                    sectionCritiqueHeadline = aiOutput.sectionCritiqueHeadline,
                    sectionCritiqueAbout = aiOutput.sectionCritiqueAbout,
                    sectionCritiqueExperience = aiOutput.sectionCritiqueExperience,

                    // Generated content
                    genHeadlines = aiOutput.genHeadlines,
                    genAbouts = aiOutput.genAbouts,
                    genExperienceBullets = aiOutput.genExperienceBullets,
                    genSkills = aiOutput.genSkills,
                    genFeatured = aiOutput.genFeatured,
                    genRecruiterKeywords = aiOutput.genRecruiterKeywords,
                    genTagline = aiOutput.genTagline,
                    genBannerText = aiOutput.genBannerText,
                    genContentPlan30Days = aiOutput.genContentPlan30Days,

                    truthSafeSuggestions = aiOutput.truthSafeSuggestions,

                    // Readiness Report
                    top5Fixes = aiOutput.top5Fixes,
                    bestHeadline = aiOutput.bestHeadline,
                    bestAbout = aiOutput.bestAbout,
                    positioningSummary = aiOutput.positioningSummary,
                    recruiterImpression = aiOutput.recruiterImpression
                )

                val id = repository.saveReview(newReview)
                val finalReview = newReview.copy(id = id.toInt())
                
                _selectedReview.value = finalReview
                _reviewState.value = ProfileReviewState.Success(finalReview)
                onComplete()
            } catch (e: Exception) {
                _reviewState.value = ProfileReviewState.Error("Profile URL scanning failed: ${e.message}")
            }
        }
    }

    fun startPdfAnalysis(rawPdfText: String, targetRole: String, selectedGoal: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _reviewState.value = ProfileReviewState.Loading

            if (rawPdfText.trim().length < 15) {
                _reviewState.value = ProfileReviewState.Error("Extracted PDF content too small to analyze. Please ensure a valid LinkedIn PDF is uploaded.")
                return@launch
            }

            try {
                // Call Gemini via REST
                val aiOutput = LinkedInAiReviewer.analyzeProfile(
                    inputMethod = "PDF",
                    url = "",
                    rawText = rawPdfText,
                    targetRole = targetRole,
                    selectedGoal = selectedGoal
                )

                val newReview = ProfileReview(
                    profileName = aiOutput.profileName,
                    profileUrl = "Uploaded PDF Document",
                    inputMethod = "PDF",
                    targetRole = targetRole,
                    selectedGoal = selectedGoal,
                    score = aiOutput.score,
                    rawExtractedText = rawPdfText,
                    
                    // Scores
                    scoreHeadline = aiOutput.scoreHeadline,
                    scoreAbout = aiOutput.scoreAbout,
                    scoreExperience = aiOutput.scoreExperience,
                    scoreKeywords = aiOutput.scoreKeywords,
                    scoreLeadership = aiOutput.scoreLeadership,
                    scoreRecruiter = aiOutput.scoreRecruiter,
                    scoreAts = aiOutput.scoreAts,
                    scoreCredibility = aiOutput.scoreCredibility,

                    // Original
                    headlineOriginal = aiOutput.headlineOriginal,
                    aboutOriginal = aiOutput.aboutOriginal,
                    experienceOriginal = aiOutput.experienceOriginal,
                    skillsOriginal = aiOutput.skillsOriginal,

                    // Optimized
                    headlineOptimized = aiOutput.headlineOptimized,
                    aboutOptimized = aiOutput.aboutOptimized,
                    experienceOptimized = aiOutput.experienceOptimized,
                    skillsOptimized = aiOutput.skillsOptimized,

                    // Feedbacks
                    overallCritique = aiOutput.overallCritique,
                    sectionCritiqueHeadline = aiOutput.sectionCritiqueHeadline,
                    sectionCritiqueAbout = aiOutput.sectionCritiqueAbout,
                    sectionCritiqueExperience = aiOutput.sectionCritiqueExperience,

                    // Generated content
                    genHeadlines = aiOutput.genHeadlines,
                    genAbouts = aiOutput.genAbouts,
                    genExperienceBullets = aiOutput.genExperienceBullets,
                    genSkills = aiOutput.genSkills,
                    genFeatured = aiOutput.genFeatured,
                    genRecruiterKeywords = aiOutput.genRecruiterKeywords,
                    genTagline = aiOutput.genTagline,
                    genBannerText = aiOutput.genBannerText,
                    genContentPlan30Days = aiOutput.genContentPlan30Days,

                    truthSafeSuggestions = aiOutput.truthSafeSuggestions,

                    // Readiness Report
                    top5Fixes = aiOutput.top5Fixes,
                    bestHeadline = aiOutput.bestHeadline,
                    bestAbout = aiOutput.bestAbout,
                    positioningSummary = aiOutput.positioningSummary,
                    recruiterImpression = aiOutput.recruiterImpression
                )

                val id = repository.saveReview(newReview)
                val finalReview = newReview.copy(id = id.toInt())

                _selectedReview.value = finalReview
                _reviewState.value = ProfileReviewState.Success(finalReview)
                onComplete()
            } catch (e: Exception) {
                _reviewState.value = ProfileReviewState.Error("PDF Analysis failed: ${e.message}")
            }
        }
    }

    fun startTextAnalysis(rawText: String, targetRole: String, selectedGoal: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _reviewState.value = ProfileReviewState.Loading

            val trimmedText = rawText.trim()
            if (trimmedText.length < 15) {
                _reviewState.value = ProfileReviewState.Error("Pasted profile text details must be at least 15 characters.")
                return@launch
            }

            try {
                // Call Gemini via REST
                val aiOutput = LinkedInAiReviewer.analyzeProfile(
                    inputMethod = "TEXT",
                    url = "",
                    rawText = trimmedText,
                    targetRole = targetRole,
                    selectedGoal = selectedGoal
                )

                val newReview = ProfileReview(
                    profileName = aiOutput.profileName,
                    profileUrl = "Pasted Profile Copy",
                    inputMethod = "TEXT",
                    targetRole = targetRole,
                    selectedGoal = selectedGoal,
                    score = aiOutput.score,
                    rawExtractedText = trimmedText,
                    
                    // Scores
                    scoreHeadline = aiOutput.scoreHeadline,
                    scoreAbout = aiOutput.scoreAbout,
                    scoreExperience = aiOutput.scoreExperience,
                    scoreKeywords = aiOutput.scoreKeywords,
                    scoreLeadership = aiOutput.scoreLeadership,
                    scoreRecruiter = aiOutput.scoreRecruiter,
                    scoreAts = aiOutput.scoreAts,
                    scoreCredibility = aiOutput.scoreCredibility,

                    // Original
                    headlineOriginal = aiOutput.headlineOriginal,
                    aboutOriginal = aiOutput.aboutOriginal,
                    experienceOriginal = aiOutput.experienceOriginal,
                    skillsOriginal = aiOutput.skillsOriginal,

                    // Optimized
                    headlineOptimized = aiOutput.headlineOptimized,
                    aboutOptimized = aiOutput.aboutOptimized,
                    experienceOptimized = aiOutput.experienceOptimized,
                    skillsOptimized = aiOutput.skillsOptimized,

                    // Feedbacks
                    overallCritique = aiOutput.overallCritique,
                    sectionCritiqueHeadline = aiOutput.sectionCritiqueHeadline,
                    sectionCritiqueAbout = aiOutput.sectionCritiqueAbout,
                    sectionCritiqueExperience = aiOutput.sectionCritiqueExperience,

                    // Generated content
                    genHeadlines = aiOutput.genHeadlines,
                    genAbouts = aiOutput.genAbouts,
                    genExperienceBullets = aiOutput.genExperienceBullets,
                    genSkills = aiOutput.genSkills,
                    genFeatured = aiOutput.genFeatured,
                    genRecruiterKeywords = aiOutput.genRecruiterKeywords,
                    genTagline = aiOutput.genTagline,
                    genBannerText = aiOutput.genBannerText,
                    genContentPlan30Days = aiOutput.genContentPlan30Days,

                    truthSafeSuggestions = aiOutput.truthSafeSuggestions,

                    // Readiness Report
                    top5Fixes = aiOutput.top5Fixes,
                    bestHeadline = aiOutput.bestHeadline,
                    bestAbout = aiOutput.bestAbout,
                    positioningSummary = aiOutput.positioningSummary,
                    recruiterImpression = aiOutput.recruiterImpression
                )

                val id = repository.saveReview(newReview)
                val finalReview = newReview.copy(id = id.toInt())

                _selectedReview.value = finalReview
                _reviewState.value = ProfileReviewState.Success(finalReview)
                onComplete()
            } catch (e: Exception) {
                _reviewState.value = ProfileReviewState.Error("Profile Text analysis failed: ${e.message}")
            }
        }
    }

    // Official Google Sign-In processor with Firebase Authentication
    fun signInWithGoogleCredential(idToken: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (FirebaseManager.isFirebaseAvailable) {
                val mAuth = FirebaseManager.firebaseAuthInstance()
                if (mAuth != null) {
                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            syncAuthState()
                            onComplete(task.isSuccessful)
                        }
                } else {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun logout() {
        if (FirebaseManager.isFirebaseAvailable) {
            FirebaseManager.firebaseAuthInstance()?.signOut()
        }
        _isUserSignedIn.value = false
        _userEmail.value = ""
        _userName.value = ""
        syncAuthState()
    }

    fun deleteReview(review: ProfileReview) {
        viewModelScope.launch {
            repository.deleteReview(review)
            if (_selectedReview.value?.id == review.id) {
                _selectedReview.value = null
            }
        }
    }

    fun clearState() {
        _reviewState.value = ProfileReviewState.Idle
    }
}

class ProfileViewModelFactory(
    private val application: Application,
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
