package com.example.data.repository

import android.util.Log
import com.example.data.local.ProfileReview
import com.example.data.local.ProfileReviewDao
import com.example.data.firebase.FirebaseManager
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import java.io.File

class ProfileRepository(private val profileReviewDao: ProfileReviewDao) {

    private val TAG = "ProfileRepository"

    val allReviews: Flow<List<ProfileReview>> = profileReviewDao.getAllReviews()

    suspend fun getReviewById(id: Int): ProfileReview? {
        return profileReviewDao.getReviewById(id)
    }

    suspend fun saveReview(review: ProfileReview): Long {
        // 1. Save to Room Local DB
        val insertedId = profileReviewDao.insertReview(review)
        val finalReview = review.copy(id = insertedId.toInt())

        // 2. Save to Cloud Firestore if Firebase is active
        if (FirebaseManager.isFirebaseAvailable && FirebaseManager.isUserSignedIn()) {
            try {
                val db = FirebaseManager.firestoreInstance()
                val userId = FirebaseManager.getCurrentUserId()
                
                if (db != null) {
                    val firestoreDoc = mapOf(
                        "id" to insertedId,
                        "createdAt" to finalReview.date,
                        "profileName" to finalReview.profileName,
                        "profileUrl" to finalReview.profileUrl,
                        "inputMethod" to finalReview.inputMethod,
                        "targetRole" to finalReview.targetRole,
                        "selectedGoal" to finalReview.selectedGoal,
                        "score" to finalReview.score,
                        "date" to finalReview.date,
                        "extractedProfileText" to finalReview.rawExtractedText,
                        
                        // Score breakdown
                        "scoreBreakdown" to mapOf(
                            "headline" to finalReview.scoreHeadline,
                            "about" to finalReview.scoreAbout,
                            "experience" to finalReview.scoreExperience,
                            "keywords" to finalReview.scoreKeywords,
                            "leadership" to finalReview.scoreLeadership,
                            "recruiter" to finalReview.scoreRecruiter,
                            "ats" to finalReview.scoreAts,
                            "credibility" to finalReview.scoreCredibility
                        ),
                        
                        // Original
                        "headlineOriginal" to finalReview.headlineOriginal,
                        "aboutOriginal" to finalReview.aboutOriginal,
                        "experienceOriginal" to finalReview.experienceOriginal,
                        "skillsOriginal" to finalReview.skillsOriginal,

                        // Critique section-wise
                        "sectionFeedback" to mapOf(
                            "headline" to finalReview.sectionCritiqueHeadline,
                            "about" to finalReview.sectionCritiqueAbout,
                            "experience" to finalReview.sectionCritiqueExperience
                        ),
                        "overallCritique" to finalReview.overallCritique,

                        // AI Optimized Content
                        "optimizedContent" to mapOf(
                            "headlineOptimized" to finalReview.headlineOptimized,
                            "aboutOptimized" to finalReview.aboutOptimized,
                            "experienceOptimized" to finalReview.experienceOptimized,
                            "skillsOptimized" to finalReview.skillsOptimized,
                            
                            // Generated elements
                            "genHeadlines" to finalReview.genHeadlines,
                            "genAbouts" to finalReview.genAbouts,
                            "genExperienceBullets" to finalReview.genExperienceBullets,
                            "genSkills" to finalReview.genSkills,
                            "genFeatured" to finalReview.genFeatured,
                            "genRecruiterKeywords" to finalReview.genRecruiterKeywords,
                            "genTagline" to finalReview.genTagline,
                            "genBannerText" to finalReview.genBannerText,
                            "genContentPlan30Days" to finalReview.genContentPlan30Days
                        ),
                        
                        // Safe and quality metrics
                        "truthSafeSuggestions" to finalReview.truthSafeSuggestions,
                        "top5Fixes" to finalReview.top5Fixes,
                        "bestHeadline" to finalReview.bestHeadline,
                        "bestAbout" to finalReview.bestAbout,
                        "positioningSummary" to finalReview.positioningSummary,
                        "recruiterImpression" to finalReview.recruiterImpression
                    )
                    
                    db.collection("users")
                        .document(userId)
                        .collection("reviews")
                        .document(insertedId.toString())
                        .set(firestoreDoc, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Review synced successfully to Cloud Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to sync to Firestore: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firestore sync skipped/failed: ${e.message}")
            }
        }
        return insertedId
    }

    suspend fun deleteReview(review: ProfileReview) {
        profileReviewDao.deleteReview(review)
        
        if (FirebaseManager.isFirebaseAvailable && FirebaseManager.isUserSignedIn()) {
            try {
                val db = FirebaseManager.firestoreInstance()
                val userId = FirebaseManager.getCurrentUserId()
                db?.collection("users")
                    ?.document(userId)
                    ?.collection("reviews")
                    ?.document(review.id.toString())
                    ?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete from Firestore: ${e.message}")
            }
        }
    }

    suspend fun uploadPdfToFirebaseStorage(file: File, onComplete: (String?) -> Unit) {
        if (FirebaseManager.isFirebaseAvailable && FirebaseManager.isUserSignedIn()) {
            try {
                val storageRef = FirebaseManager.storageInstance()?.reference
                val userId = FirebaseManager.getCurrentUserId()
                val pdfRef = storageRef?.child("users/$userId/pdfs/${file.name}")
                
                pdfRef?.putFile(android.net.Uri.fromFile(file))
                    ?.addOnSuccessListener {
                        pdfRef.downloadUrl.addOnSuccessListener { uri ->
                            onComplete(uri.toString())
                        }.addOnFailureListener {
                            onComplete(null)
                        }
                    }
                    ?.addOnFailureListener {
                        onComplete(null)
                    }
            } catch (e: Exception) {
                onComplete(null)
            }
        } else {
            onComplete(null) // Local fallback
        }
    }
}
