package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

// AI Strategy result mapped from JSON response
data class AiStrategyResult(
    val profileName: String,
    val score: Int,
    val headlineOriginal: String,
    val headlineOptimized: String,
    val aboutOriginal: String,
    val aboutOptimized: String,
    val experienceOriginal: String,
    val experienceOptimized: String,
    val skillsOriginal: String,
    val skillsOptimized: String,
    
    // Core Scorecard Components out of 100
    val scoreHeadline: Int,
    val scoreAbout: Int,
    val scoreExperience: Int,
    val scoreKeywords: Int,
    val scoreLeadership: Int,
    val scoreRecruiter: Int,
    val scoreAts: Int,
    val scoreCredibility: Int,

    // Critiques & Section Feedback
    val overallCritique: String,
    val sectionCritiqueHeadline: String,
    val sectionCritiqueAbout: String,
    val sectionCritiqueExperience: String,

    // Generated Branding Content
    val genHeadlines: String,
    val genAbouts: String,
    val genExperienceBullets: String,
    val genSkills: String,
    val genFeatured: String,
    val genRecruiterKeywords: String,
    val genTagline: String,
    val genBannerText: String,
    val genContentPlan30Days: String,

    // Truth-Safe Metrics suggestions
    val truthSafeSuggestions: String,

    // Readiness Report
    val top5Fixes: String,
    val bestHeadline: String,
    val bestAbout: String,
    val positioningSummary: String,
    val recruiterImpression: String
)

object LinkedInAiReviewer {
    private const val TAG = "LinkedInAiReviewer"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun fetchUrlContent(url: String): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "HTTP connection error: ${response.code} ${response.message}"
                }
                val body = response.body?.string() ?: ""
                
                // Extract headings, titles, or standard meta tag to pass to Gemini
                val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
                val title = titleRegex.find(body)?.groupValues?.getOrNull(1) ?: "No Title"
                
                val metaDescRegex = "<meta\\s+name=\"description\"\\s+content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
                val metaDesc = metaDescRegex.find(body)?.groupValues?.getOrNull(1) ?: "No description"

                val h1s = "<h1>(.*?)</h1>".toRegex(RegexOption.IGNORE_CASE).findAll(body)
                    .map { it.groupValues.getOrNull(1)?.trim() ?: "" }
                    .filter { it.isNotBlank() }
                    .take(10)
                    .joinToString(", ")

                val h2s = "<h2>(.*?)</h2>".toRegex(RegexOption.IGNORE_CASE).findAll(body)
                    .map { it.groupValues.getOrNull(1)?.trim() ?: "" }
                    .filter { it.isNotBlank() }
                    .take(10)
                    .joinToString(", ")

                """
                    LinkedIn Web Address Scrape Excerpt:
                    Page Title: $title
                    Meta Description: $metaDesc
                    Key Headers: $h1s / $h2s
                    Body Excerpt: ${if (body.length > 2000) body.take(2000) else body}
                """.trimIndent()
            }
        } catch (e: Exception) {
            "Error retrieving profile webpage: ${e.message}"
        }
    }

    suspend fun analyzeProfile(
        inputMethod: String,
        url: String,
        rawText: String,
        targetRole: String,
        selectedGoal: String
    ): AiStrategyResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            val errMsg = "Gemini API Key is missing. Please configure 'GEMINI_API_KEY' in the AI Studio Secrets panel."
            Log.e(TAG, errMsg)
            throw IllegalArgumentException(errMsg)
        }

        val parsedInputProfile = if (inputMethod == "URL" && url.isNotBlank()) {
            val webContent = fetchUrlContent(url)
            "URL: $url\nPage Connection Results:\n$webContent"
        } else {
            rawText
        }

        val prompt = """
            You are LinkOptima Pro's elite Chief Executive Branding Officer, professional LinkedIn copywriter, and Head of Recruiting.
            Perform a production-grade, highly precise, dynamic analysis and rewriting of the following candidate profile.
            
            INPUT DETAILS:
            - Method: $inputMethod
            - URL (If supplied): $url
            - Target Job/Role: $targetRole
            - Selected Career Positioning Goal: $selectedGoal
            - Raw Profile Content:
            $parsedInputProfile

            TASK & STRICT INSTRUCTIONS:
            Create a highly tailored strategy based on their specific background, target role ($targetRole) and career positioning goal ($selectedGoal).
            
            1. SCORES out of 100:
               - Calculate a mathematically reasoned overall score and individual breakout scores based strictly on the quality and content of $parsedInputProfile in comparison to recruiter expectations for $targetRole:
                 - scoreHeadline (Headline strength)
                 - scoreAbout (About section bio strength)
                 - scoreExperience (Experience impact)
                 - scoreKeywords (ATS keyword optimization matching $targetRole)
                 - scoreLeadership (Leadership and authority alignment)
                 - scoreRecruiter (Recruiter clickability and search index visibility)
                 - scoreAts (ATS scan friendliness)
                 - scoreCredibility (Authenticity and metrics proof)
               - Do not just output round numbers like 80, 85 for everything. Reflect their real profile content score.

            2. SECION-WISE CRITIQUE (Headline, About, Experience):
               - For each section, formulate professional critique containing the exact points:
                 - What is weak: specific structural/wording faults.
                 - Why it matters: how it affects recruiter interest/ATS.
                 - Exact proposed rewrite: a single, high-impact optimized draft rewrite.
                 - Keywords to add: specific list of skills/terms for this section.
                 - Mistakes to avoid: custom guidance.
               - Format as clear, readable Markdown bullet strings.

            3. AI CONTENT GENERATION:
               - genHeadlines: Generate exactly 5 real LinkedIn headline options emphasizing value proposition, core keywords, and target role ($targetRole). Make each headline highly creative, professional, and clear.
               - genAbouts: Generate exactly 3 completely different versions of LinkedIn "About" bios:
                 - Version 1: Story-based executive overview.
                 - Version 2: Direct, highlight-focused professional synopsis.
                 - Version 3: Modular, keyword-enriched biography.
               - genExperienceBullets: Polished bullet points for experience based on their actual background. Optimize using action verbs.
               - genSkills: Tailored list of 15 recommended skills matching their industry background and $targetRole.
               - genFeatured: Structural ideas on slides, case studies, or certifications they should feature.
               - genRecruiterKeywords: 10 recruiter search keywords.
               - genTagline: A punchy one-sentence personal branding tagline.
               - genBannerText: Copy to display in a customized LinkedIn background banner.
               - genContentPlan30Days: A brief 30-day tactical LinkedIn content schedule (weeks 1 to 4 focus prompts) to grow authority for $selectedGoal.

            4. TRUTH-SAFE AI SAFEGUARDS (MUST NOT FABRICATE):
               - You MUST NOT fabricate or invent non-existent employers, degrees, certified credentials, specific metrics (like $15M revenue or managed 500 people if not mentioned), or awards.
               - If they do not provide metrics, suggest exactly WHERE we left brackets [e.g., increased revenue by [X]% or speed up release by [Y]%] for them to fill in their own verified, real calculations. State clearly that we have preserved their genuine history with trust.

            5. READINESS REPORT:
               - top5Fixes: Exactly 5 urgent profile updates in bullet format.
               - bestHeadline: Single best primary recommended headline option.
               - bestAbout: Single best primary recommended About bio.
               - positioningSummary: A brief synthesis of how this profile now positions the user for $targetRole and $selectedGoal.
               - recruiterImpression: Calculated description of the immediate impression a recruiter will have when landing on this updated page.

            Return the output STRICTLY in a JSON block matching this exact schema (no text preamble, do not wrap in nesting arrays, do not add arbitrary outer properties, output only valid Moshi-compatible JSON):
            {
              "profileName": "User's real or professional title name",
              "score": overall_integer_score_60_to_100,
              
              "scoreHeadline": integer_score_0_to_100,
              "scoreAbout": integer_score_0_to_100,
              "scoreExperience": integer_score_0_to_100,
              "scoreKeywords": integer_score_0_to_100,
              "scoreLeadership": integer_score_0_to_100,
              "scoreRecruiter": integer_score_0_to_100,
              "scoreAts": integer_score_0_to_100,
              "scoreCredibility": integer_score_0_to_100,

              "headlineOriginal": "current extracted headline or title",
              "headlineOptimized": "primary proposed optimized headline",
              "aboutOriginal": "current extracted introductory sentence or about text",
              "aboutOptimized": "primary complete proposed About bio",
              "experienceOriginal": "current extracted text representation of experience",
              "experienceOptimized": "polished experience description",
              "skillsOriginal": "current parsed list of skills",
              "skillsOptimized": "proposed skills keywords list",

              "overallCritique": "high-touch executive overview of profile strengths and opportunities",
              "sectionCritiqueHeadline": "detailed rewrite suggestion, weak issues, keywords to add, and mistakes to avoid for Headline",
              "sectionCritiqueAbout": "detailed rewrite suggestion, weak issues, keywords to add, and mistakes to avoid for About",
              "sectionCritiqueExperience": "detailed rewrite suggestion, weak issues, keywords to add, and mistakes to avoid for Experience",

              "genHeadlines": "bulleted list of 5 premium LinkedIn headline options",
              "genAbouts": "the 3 rich versions of LinkedIn About sections",
              "genExperienceBullets": "proposed polished bullet points tailored to actual employer lists",
              "genSkills": "recommended 15 skills",
              "genFeatured": "featured ideas",
              "genRecruiterKeywords": "recruiter keyword list",
              "genTagline": "personal branding tagline",
              "genBannerText": "recommended LinkedIn background banner copy",
              "genContentPlan30Days": "30-day weekly content suggestions",

              "truthSafeSuggestions": "explicit lists of empty brackets or metric placeholder cues to avoid fabricating facts, indicating real fields to enter",

              "top5Fixes": "exactly 5 tactical issues to resolve immediately",
              "bestHeadline": "ultimate high-impact headline option",
              "bestAbout": "ultimate high-impact bio",
              "positioningSummary": "how this coordinates with target targetRole",
              "recruiterImpression": "executive statement of recruiter response"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.45
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an elite, certified executive branding specialist. You return 100% valid, raw, unnested JSON matching the requested keys exactly. Do not wrap String properties in internal lists or arrays. Preserve truth-safety strictly and explain placeholders without creating mock achievements."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Gemini returned empty generation text.")

            val cleanJson = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(AiStrategyResponseJson::class.java)
            val jsonResult = adapter.fromJson(cleanJson)
                ?: throw IllegalStateException("Moshi parser returned empty for: $cleanJson")

            val baseName = if (jsonResult.profileName.isNullOrBlank()) "LinkedIn Professional" else jsonResult.profileName

            AiStrategyResult(
                profileName = baseName,
                score = jsonResult.score ?: 75,
                headlineOriginal = jsonResult.headlineOriginal ?: "Original Headline Unavailable",
                headlineOptimized = jsonResult.headlineOptimized ?: "Optimized Headline",
                aboutOriginal = jsonResult.aboutOriginal ?: "Original Summary Unavailable",
                aboutOptimized = jsonResult.aboutOptimized ?: "Optimized Summary",
                experienceOriginal = jsonResult.experienceOriginal ?: "Original Experience Unavailable",
                experienceOptimized = jsonResult.experienceOptimized ?: "Optimized Experience",
                skillsOriginal = jsonResult.skillsOriginal ?: "Original Skills Box",
                skillsOptimized = jsonResult.skillsOptimized ?: "Optimized Keyword Skills",
                
                // Scores
                scoreHeadline = jsonResult.scoreHeadline ?: 70,
                scoreAbout = jsonResult.scoreAbout ?: 70,
                scoreExperience = jsonResult.scoreExperience ?: 70,
                scoreKeywords = jsonResult.scoreKeywords ?: 70,
                scoreLeadership = jsonResult.scoreLeadership ?: 70,
                scoreRecruiter = jsonResult.scoreRecruiter ?: 70,
                scoreAts = jsonResult.scoreAts ?: 70,
                scoreCredibility = jsonResult.scoreCredibility ?: 70,

                // Critiques
                overallCritique = jsonResult.overallCritique ?: "Overall assessment ready.",
                sectionCritiqueHeadline = jsonResult.sectionCritiqueHeadline ?: "Headline feedback updated.",
                sectionCritiqueAbout = jsonResult.sectionCritiqueAbout ?: "About bio feedback updated.",
                sectionCritiqueExperience = jsonResult.sectionCritiqueExperience ?: "Experience timeline feedback updated.",

                // Generator
                genHeadlines = jsonResult.genHeadlines ?: "Headline formulations.",
                genAbouts = jsonResult.genAbouts ?: "Summary bios.",
                genExperienceBullets = jsonResult.genExperienceBullets ?: "Polished bullet formulations.",
                genSkills = jsonResult.genSkills ?: "Required keyword list.",
                genFeatured = jsonResult.genFeatured ?: "Featured column proposals.",
                genRecruiterKeywords = jsonResult.genRecruiterKeywords ?: "ATS tag index keywords.",
                genTagline = jsonResult.genTagline ?: "High-impact brand catchphrase.",
                genBannerText = jsonResult.genBannerText ?: "Banner callout phrases.",
                genContentPlan30Days = jsonResult.genContentPlan30Days ?: "30-day timeline schedule roadmap.",

                // Safe mechanics
                truthSafeSuggestions = jsonResult.truthSafeSuggestions ?: "Measurable metric guides.",

                // Readiness
                top5Fixes = jsonResult.top5Fixes ?: "Top 5 prioritized action items.",
                bestHeadline = jsonResult.bestHeadline ?: "Best recommended headline.",
                bestAbout = jsonResult.bestAbout ?: "Best recommended bio.",
                positioningSummary = jsonResult.positioningSummary ?: "Positioning synthesis for modern jobs.",
                recruiterImpression = jsonResult.recruiterImpression ?: "Recruiter response expectation."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini REST execution failed: ${e.message}", e)
            throw e
        }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class AiStrategyResponseJson(
    val profileName: String? = null,
    val score: Int? = null,
    
    val scoreHeadline: Int? = null,
    val scoreAbout: Int? = null,
    val scoreExperience: Int? = null,
    val scoreKeywords: Int? = null,
    val scoreLeadership: Int? = null,
    val scoreRecruiter: Int? = null,
    val scoreAts: Int? = null,
    val scoreCredibility: Int? = null,

    val headlineOriginal: String? = null,
    val headlineOptimized: String? = null,
    val aboutOriginal: String? = null,
    val aboutOptimized: String? = null,
    val experienceOriginal: String? = null,
    val experienceOptimized: String? = null,
    val skillsOriginal: String? = null,
    val skillsOptimized: String? = null,

    val overallCritique: String? = null,
    val sectionCritiqueHeadline: String? = null,
    val sectionCritiqueAbout: String? = null,
    val sectionCritiqueExperience: String? = null,

    val genHeadlines: String? = null,
    val genAbouts: String? = null,
    val genExperienceBullets: String? = null,
    val genSkills: String? = null,
    val genFeatured: String? = null,
    val genRecruiterKeywords: String? = null,
    val genTagline: String? = null,
    val genBannerText: String? = null,
    val genContentPlan30Days: String? = null,

    val truthSafeSuggestions: String? = null,

    val top5Fixes: String? = null,
    val bestHeadline: String? = null,
    val bestAbout: String? = null,
    val positioningSummary: String? = null,
    val recruiterImpression: String? = null
)
