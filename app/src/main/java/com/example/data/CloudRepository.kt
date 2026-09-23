package com.example.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CloudRepository {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            val dbId = try {
                val idRes = app.applicationContext.resources.getIdentifier(
                    "firestore_database_id",
                    "string",
                    app.applicationContext.packageName
                )
                if (idRes != 0) app.applicationContext.getString(idRes) else null
            } catch (_: Exception) {
                null
            }

            if (!dbId.isNullOrBlank()) {
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                FirebaseFirestore.getInstance(app)
            }
        } catch (e: Exception) {
            Log.w("CloudRepository", "Firebase not yet initialized or configured: ${e.message}")
            null
        }
    }

    val isCloudAvailable: Boolean
        get() = firestore != null

    /**
     * Real-time stream of all stories published across users.
     * Orders newest stories to the top in real-time.
     */
    fun getStoriesFlow(): Flow<List<Story>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("stories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CloudRepository", "Error fetching stories: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val stories = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            if (id.startsWith("story-seed-")) {
                                return@mapNotNull null
                            }
                            val title = doc.getString("title") ?: ""
                            val authorName = doc.getString("authorName") ?: ""
                            val authorHandle = doc.getString("authorHandle") ?: "@anonymous"
                            val authorBio = doc.getString("authorBio") ?: ""
                            val typeStr = doc.getString("type") ?: "গল্প"
                            val type = when (typeStr) {
                                "কবিতা" -> StoryType.KOBITA
                                "উপন্যাস" -> StoryType.UPONNAS
                                else -> StoryType.GOLPO
                            }
                            val genre = doc.getString("genre") ?: "সামাজিক"
                            val excerpt = doc.getString("excerpt") ?: ""
                            val fullContent = doc.getString("fullContent") ?: ""
                            val likesCount = (doc.getLong("likesCount") ?: 0L).toInt()
                            val lovesCount = (doc.getLong("lovesCount") ?: 0L).toInt()
                            val firesCount = (doc.getLong("firesCount") ?: 0L).toInt()
                            val commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt()
                            val readTimeMinutes = (doc.getLong("readTimeMinutes") ?: 3L).toInt()
                            val publishedDate = doc.getString("publishedDate") ?: "এইমাত্র"
                            val timestamp = doc.getLong("timestamp") ?: 0L

                            val chapterList = try {
                                val rawChapters = doc.get("chapters") as? List<Map<String, Any>>
                                rawChapters?.mapIndexed { idx, chMap ->
                                    Chapter(
                                        id = chMap["id"] as? String ?: "ch-${idx + 1}",
                                        chapterNumber = (chMap["chapterNumber"] as? Long)?.toInt() ?: (idx + 1),
                                        title = chMap["title"] as? String ?: "অধ্যায় ${idx + 1}",
                                        content = chMap["content"] as? String ?: "",
                                        readTimeMinutes = (chMap["readTimeMinutes"] as? Long)?.toInt() ?: 3
                                    )
                                } ?: emptyList()
                            } catch (_: Exception) {
                                emptyList()
                            }

                            Story(
                                id = id,
                                title = title,
                                excerpt = excerpt,
                                fullContent = fullContent,
                                authorName = authorName,
                                authorHandle = authorHandle,
                                authorBio = authorBio,
                                genre = genre,
                                type = type,
                                likesCount = likesCount,
                                lovesCount = lovesCount,
                                firesCount = firesCount,
                                commentsCount = commentsCount,
                                readTimeMinutes = readTimeMinutes,
                                publishedDate = publishedDate,
                                chapters = chapterList,
                                averageRating = doc.getDouble("averageRating") ?: 0.0,
                                reviewsCount = (doc.getLong("reviewsCount") ?: 0L).toInt()
                            ) to timestamp
                        } catch (e: Exception) {
                            Log.e("CloudRepository", "Error parsing story: ${e.message}")
                            null
                        }
                    }
                    // Sort descending by timestamp (newest on top)
                    val sortedStories = stories.sortedByDescending { it.second }.map { it.first }
                    trySend(sortedStories)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Publish a story to the cloud for all users to read in real-time
     */
    suspend fun publishStoryToCloud(story: Story): Boolean {
        val db = firestore ?: return false
        return try {
            val chaptersData = story.chapters.map { ch ->
                mapOf(
                    "id" to ch.id,
                    "chapterNumber" to ch.chapterNumber,
                    "title" to ch.title,
                    "content" to ch.content,
                    "readTimeMinutes" to ch.readTimeMinutes
                )
            }

            val storyData = hashMapOf(
                "title" to story.title,
                "authorName" to story.authorName,
                "authorHandle" to story.authorHandle,
                "authorBio" to story.authorBio,
                "type" to story.type.banglaLabel,
                "genre" to story.genre,
                "excerpt" to story.excerpt,
                "fullContent" to story.fullContent,
                "likesCount" to story.likesCount,
                "lovesCount" to story.lovesCount,
                "firesCount" to story.firesCount,
                "commentsCount" to story.commentsCount,
                "averageRating" to story.averageRating,
                "reviewsCount" to story.reviewsCount,
                "readTimeMinutes" to story.readTimeMinutes,
                "publishedDate" to story.publishedDate,
                "timestamp" to System.currentTimeMillis(),
                "chapters" to chaptersData
            )
            db.collection("stories").document(story.id).set(storyData).await()
            true
        } catch (e: Exception) {
            Log.e("CloudRepository", "Failed to publish story to cloud: ${e.message}")
            false
        }
    }

    /**
     * Purges any initial mock/seed stories from Firestore so only real user stories exist.
     */
    suspend fun purgeSeedStories() {
        val db = firestore ?: return
        try {
            val snapshot = db.collection("stories").get().await()
            for (doc in snapshot.documents) {
                if (doc.id.startsWith("story-seed-")) {
                    doc.reference.delete().await()
                }
            }
        } catch (e: Exception) {
            Log.w("CloudRepository", "Could not purge seed stories: ${e.message}")
        }
    }

    suspend fun seedInitialStoriesIfEmpty(initialStories: List<Story>) {
        // App starts empty per user requirement
    }

    /**
     * Real-time stream of comments for a specific story
     */
    fun getCommentsFlow(storyId: String): Flow<List<Comment>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("stories")
            .document(storyId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CloudRepository", "Error fetching comments: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            val timestamp = doc.getLong("timestamp") ?: 0L
                            val comment = Comment(
                                id = doc.id,
                                storyId = storyId,
                                authorName = doc.getString("authorName") ?: "বেনামী পাঠক",
                                authorAvatar = doc.getString("authorAvatar") ?: "",
                                text = doc.getString("text") ?: "",
                                timeAgo = doc.getString("timeAgo") ?: "এইমাত্র",
                                likesCount = (doc.getLong("likesCount") ?: 0L).toInt()
                            )
                            comment to timestamp
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val sortedComments = comments.sortedByDescending { it.second }.map { it.first }
                    trySend(sortedComments)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Add a comment to a story in the cloud
     */
    suspend fun addCommentToCloud(storyId: String, comment: Comment): Boolean {
        val db = firestore ?: return false
        return try {
            val commentData = hashMapOf(
                "authorName" to comment.authorName,
                "authorAvatar" to comment.authorAvatar,
                "text" to comment.text,
                "timeAgo" to comment.timeAgo,
                "likesCount" to comment.likesCount,
                "timestamp" to System.currentTimeMillis()
            )
            val docRef = db.collection("stories").document(storyId)
            docRef.collection("comments").document(comment.id).set(commentData).await()
            docRef.update("commentsCount", FieldValue.increment(1)).await()
            true
        } catch (e: Exception) {
            Log.e("CloudRepository", "Failed to add comment to cloud: ${e.message}")
            false
        }
    }

    /**
     * Listen to real-time reviews for a specific story from Firestore
     */
    fun getReviewsFlow(storyId: String): Flow<List<StoryReview>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("stories")
            .document(storyId)
            .collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CloudRepository", "Error fetching reviews: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.documents.mapNotNull { doc ->
                        try {
                            val timestamp = doc.getLong("timestamp") ?: doc.getLong("createdAt") ?: 0L
                            val review = StoryReview(
                                id = doc.id,
                                storyId = storyId,
                                reviewerName = doc.getString("reviewerName") ?: "স্বপ্নবাজ পাঠক",
                                reviewerRole = doc.getString("reviewerRole") ?: "পাঠক",
                                rating = (doc.getLong("rating") ?: 5L).toInt(),
                                reviewText = doc.getString("reviewText") ?: "",
                                timeAgo = doc.getString("timeAgo") ?: "এইমাত্র",
                                createdAt = timestamp
                            )
                            review to timestamp
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val sortedReviews = reviews.sortedByDescending { it.second }.map { it.first }
                    trySend(sortedReviews)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Add a reader review to a story in the cloud and update the story's average rating
     */
    suspend fun addReviewToCloud(storyId: String, review: StoryReview): Boolean {
        val db = firestore ?: return false
        return try {
            val reviewData = hashMapOf(
                "reviewerName" to review.reviewerName,
                "reviewerRole" to review.reviewerRole,
                "rating" to review.rating,
                "reviewText" to review.reviewText,
                "timeAgo" to review.timeAgo,
                "timestamp" to System.currentTimeMillis()
            )
            val docRef = db.collection("stories").document(storyId)
            docRef.collection("reviews").document(review.id).set(reviewData).await()

            // Update average rating and reviews count
            val reviewsSnapshot = docRef.collection("reviews").get().await()
            val totalRatings = reviewsSnapshot.documents.mapNotNull { it.getLong("rating") }
            val count = totalRatings.size
            val avg = if (count > 0) totalRatings.sum().toDouble() / count else review.rating.toDouble()

            docRef.update(
                mapOf(
                    "reviewsCount" to count,
                    "averageRating" to avg
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e("CloudRepository", "Failed to add review to cloud: ${e.message}")
            false
        }
    }
}
