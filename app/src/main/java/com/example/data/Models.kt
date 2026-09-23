package com.example.data

enum class StoryType(val banglaLabel: String) {
    GOLPO("গল্প"),
    KOBITA("কবিতা"),
    UPONNAS("উপন্যাস")
}

enum class ReactionType(val banglaLabel: String, val emoji: String) {
    LIKE("লাইক", "👍"),
    LOVE("ভালোবাসা", "❤️"),
    FIRE("আগুন", "🔥"),
    DISLIKE("অপছন্দ", "👎")
}

enum class ReadingTheme {
    LIGHT,
    SEPIA,
    DARK
}

enum class UserRole(val banglaLabel: String) {
    WRITER("লেখক"),
    READER("পাঠক")
}

data class StoryReview(
    val id: String,
    val storyId: String = "",
    val reviewerName: String,
    val reviewerRole: String = "পাঠক",
    val rating: Int = 5, // 1 to 5 stars
    val reviewText: String,
    val timeAgo: String = "এইমাত্র",
    val createdAt: Long = System.currentTimeMillis()
)

data class Chapter(
    val id: String,
    val chapterNumber: Int,
    val title: String,
    val content: String,
    val readTimeMinutes: Int
)

data class CommentReply(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int = 0
)

data class Comment(
    val id: String,
    val storyId: String = "",
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int = 0,
    val replies: List<CommentReply> = emptyList()
)

data class Story(
    val id: String,
    val title: String,
    val excerpt: String,
    val fullContent: String,
    val authorName: String,
    val authorHandle: String,
    val authorBio: String = "",
    val authorAvatar: String = "",
    val genre: String,
    val type: StoryType,
    val coverGradientStart: Long = 0xFF1E3A8A,
    val coverGradientEnd: Long = 0xFF0F172A,
    val likesCount: Int = 0,
    val lovesCount: Int = 0,
    val firesCount: Int = 0,
    val dislikesCount: Int = 0,
    val commentsCount: Int = 0,
    val readTimeMinutes: Int = 4,
    val publishedDate: String = "এইমাত্র",
    val isBookmarked: Boolean = false,
    val userReaction: ReactionType? = null,
    val isFollowing: Boolean = false,
    val readingProgressPercent: Int = 0,
    val chapters: List<Chapter> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewsCount: Int = 0
)

data class WriterStats(
    val totalViews: String = "০",
    val totalReads: String = "০",
    val totalReactions: String = "০",
    val totalComments: String = "০",
    val followersCount: String = "০",
    val totalTips: String = "৳ ০"
)
