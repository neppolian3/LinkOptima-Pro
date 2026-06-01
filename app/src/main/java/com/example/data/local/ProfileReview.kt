package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_reviews")
data class ProfileReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileName: String,
    val profileUrl: String = "",
    val inputMethod: String, // "URL", "PDF" or "TEXT"
    val targetRole: String = "", // e.g. Senior Software Engineer
    val selectedGoal: String = "", // e.g. UAE job market positioning
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val rawExtractedText: String = "",
    
    // Original Content
    val headlineOriginal: String = "",
    val aboutOriginal: String = "",
    val experienceOriginal: String = "",
    val skillsOriginal: String = "",
    
    // Optimized Content Properties
    val headlineOptimized: String = "",
    val aboutOptimized: String = "",
    val experienceOptimized: String = "",
    val skillsOptimized: String = "",
    
    // Score Breakdown out of 100
    val scoreHeadline: Int = 70,
    val scoreAbout: Int = 70,
    val scoreExperience: Int = 70,
    val scoreKeywords: Int = 70,
    val scoreLeadership: Int = 70,
    val scoreRecruiter: Int = 70,
    val scoreAts: Int = 70,
    val scoreCredibility: Int = 70,
    
    // Critique & Suggestions
    val overallCritique: String = "",
    val sectionCritiqueHeadline: String = "",
    val sectionCritiqueAbout: String = "",
    val sectionCritiqueExperience: String = "",
    
    // AI Content Generator:
    val genHeadlines: String = "", // 5 options
    val genAbouts: String = "", // 3 versions
    val genExperienceBullets: String = "",
    val genSkills: String = "",
    val genFeatured: String = "",
    val genRecruiterKeywords: String = "",
    val genTagline: String = "",
    val genBannerText: String = "",
    val genContentPlan30Days: String = "",
    
    // Truth-Safe Metrics / Suggestions where user can add real measurements
    val truthSafeSuggestions: String = "",
    
    // Readiness Report
    val top5Fixes: String = "",
    val bestHeadline: String = "",
    val bestAbout: String = "",
    val positioningSummary: String = "",
    val recruiterImpression: String = ""
)
