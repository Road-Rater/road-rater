package com.roadrater.database.repository

import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Comment
import com.roadrater.database.entities.CommentVote
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.domain.DatabaseRepository
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class DatabaseRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : DatabaseRepository {
    override suspend fun getCarByPlate(numberPlate: String): Car? {
        return supabaseClient.from("cars").select { filter { eq("number_plate", numberPlate) } }.decodeSingleOrNull<Car>()
    }

    override suspend fun insertCar(numberPlate: String) {
        supabaseClient.from("cars").insert(GetCarInfo.getCarInfo(numberPlate))
    }

    override suspend fun upsertCar(car: Car) {
        supabaseClient.from("car").insert(car)
    }

    override suspend fun watchCar(uid: String, numberPlate: String) {
        // Redundant checks for invalid format and already watching are removed as they are handled in ViewModel

        // Ensure car exists in database (or upsert if not)
        val count = supabaseClient
            .from("cars")
            .select {
                filter {
                    eq("number_plate", numberPlate)
                }
                limit(1)
            }.countOrNull()

        if (count == null || count >= 0) {
            supabaseClient.from("cars").upsert(GetCarInfo.getCarInfo(numberPlate))
        }

        // Add to watched cars
        supabaseClient.from("watched_cars").upsert(
            WatchedCar(
                number_plate = numberPlate,
                uid = uid,
            ),
        )
    }

    override suspend fun unwatchCar(uid: String, numberPlate: String) {
        // Redundant check for not watching is removed as it should be handled in ViewModel
        supabaseClient.from("watched_cars").delete {
            filter {
                eq("number_plate", numberPlate)
                eq("uid", uid)
            }
        }
    }

    override suspend fun getWatchedCars(uid: String): List<Car> {
        return supabaseClient
            .from("watched_cars")
            .select {
                filter {
                    eq("uid", uid)
                }
            }
            .decodeList<WatchedCar>()
            .map { watchedCar ->
                getCarByPlate(watchedCar.number_plate)
            }
            .filterNotNull()
    }

    override suspend fun isWatchingCar(uid: String, numberPlate: String): Boolean {
        val count = supabaseClient
            .from("watched_cars")
            .select {
                filter {
                    eq("uid", uid)
                    eq("number_plate", numberPlate)
                }
            }
            .countOrNull()

        return count != null && count > 0
    }

    override suspend fun insertReview(review: Review) {
        supabaseClient.from("reviews").insert(review)
    }

    override suspend fun getReviewsByPlate(numberPlate: String): List<Review?> {
        return supabaseClient.from("watched_cars").select {
            filter {
                eq("number_plate", numberPlate)
            }
        }.decodeList<Review>()
    }

    override suspend fun getReviewsByUser(uid: String): List<Review?> {
        return supabaseClient.from("watched_cars").select {
            filter {
                eq("created_by", uid)
            }
        }.decodeList<Review>()
    }

    override suspend fun getUser(uid: String): TableUser? {
        return supabaseClient.from("users").select {
            filter {
                eq("uid", uid)
            }
        }.decodeSingleOrNull<TableUser>()
    }

    override suspend fun insertUser(user: TableUser) {
        supabaseClient.from("users").insert(user)
    }

    override suspend fun updateNickname(uid: String, nickname: String) {
        supabaseClient.from("users").update(
            {
                set("nickname", nickname)
            },
        ) {
            filter {
                eq("uid", uid)
            }
        }
    }

    override suspend fun nicknameAvailable(nickname: String): Boolean {
        return try {
            val response = supabaseClient.from("users").select { filter { eq("nickname", nickname) } }.decodeList<TableUser>()
            response.isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getCommentsForReview(reviewId: Long): List<Comment> {
        return supabaseClient
            .from("comments")
            .select {
                filter {
                    eq("review_id", reviewId)
                }
            }
            .decodeList<Comment>()
    }

    override suspend fun postComment(
        reviewId: Long,
        userId: String,
        content: String,
        parentId: Long? // nullable for top-level comments
    ): Comment {
        return supabaseClient.from("comments")
            .insert(
                mapOf(
                    "review_id" to reviewId,
                    "user_id" to userId,
                    "content" to content,
                    "parent_id" to parentId
                )
            )
            .decodeSingle()
    }

    override suspend fun voteOnComment(commentId: Long, userId: String, vote: Int) {
        val existing = supabaseClient.from("comment_votes")
            .select {
                filter {
                    eq("comment_id", commentId)
                    eq("user_id", userId)
                }
                limit(1)
            }
            .decodeSingleOrNull<CommentVote>()

        if (existing != null) {
            supabaseClient.from("comment_votes")
                .update(
                    {
                        set("vote", vote)
                    }
                ) {
                    filter {
                        eq("id", existing.id)
                    }
                }
        } else {
            supabaseClient.from("comment_votes")
                .insert(
                    mapOf(
                        "comment_id" to commentId,
                        "user_id" to userId,
                        "vote" to vote
                    )
                )
        }
    }




    override suspend fun getVoteCount(commentId: Long): Int {
        val votes = supabaseClient.from("comment_votes")
            .select {
                filter {
                    eq("comment_id", commentId)
                }
            }
            .decodeList<CommentVote>()

        return votes.sumOf { it.vote }
    }



}
