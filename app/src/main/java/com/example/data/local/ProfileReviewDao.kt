package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileReviewDao {
    @Query("SELECT * FROM profile_reviews ORDER BY date DESC")
    fun getAllReviews(): Flow<List<ProfileReview>>

    @Query("SELECT * FROM profile_reviews WHERE id = :id")
    suspend fun getReviewById(id: Int): ProfileReview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ProfileReview): Long

    @Delete
    suspend fun deleteReview(review: ProfileReview)

    @Query("DELETE FROM profile_reviews")
    suspend fun deleteAllReviews()
}
