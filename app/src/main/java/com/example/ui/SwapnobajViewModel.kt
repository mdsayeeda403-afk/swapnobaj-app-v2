package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Chapter
import com.example.data.CloudRepository
import com.example.data.Comment
import com.example.data.CommentReply
import com.example.data.ReactionType
import com.example.data.ReadingTheme
import com.example.data.SampleData
import com.example.data.SessionManager
import com.example.data.Story
import com.example.data.StoryReview
import com.example.data.StoryType
import com.example.data.UserRole
import com.example.data.WriterStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab(val label: String) {
    FOR_YOU("আপনার জন্য"),
    TRENDING("ট্রেন্ডিং"),
    FOLLOWING("অনুসরণ")
}

data class SwapnobajUiState(
    val currentTab: HomeTab = HomeTab.FOR_YOU,
    val selectedGenre: String = "সব",
    val searchQuery: String = "",
    val stories: List<Story> = emptyList(),
    val currentReaderStory: Story? = null,
    val currentChapterIndex: Int = 0,
    val readingTheme: ReadingTheme = ReadingTheme.SEPIA,
    val readingFontSizeSp: Float = 18f,
    val isCommentsSheetOpen: Boolean = false,
    val activeCommentsStory: Story? = null,
    val commentsByStoryId: Map<String, List<Comment>> = emptyMap(),
    val currentComments: List<Comment> = emptyList(),
    val isReviewModalOpen: Boolean = false,
    val activeReviewStory: Story? = null,
    val reviewsByStoryId: Map<String, List<StoryReview>> = emptyMap(),
    val currentReviews: List<StoryReview> = emptyList(),
    val isTipDialogOpen: Boolean = false,
    val tipTargetAuthor: String? = null,
    val isAuthModalOpen: Boolean = false,
    val isBookshelfGridView: Boolean = true,
    val bookshelfFilter: String = "সব",
    val writerStats: WriterStats = WriterStats(),
    val isUserLoggedIn: Boolean = false,
    val userRole: UserRole = UserRole.READER,
    val loggedInUserName: String = "",
    val loggedInUserHandle: String = "",
    val loggedInUserBio: String = "",
    val successSnackbarMessage: String? = null
)

class SwapnobajViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application.applicationContext)
    private val cloudRepository = CloudRepository()
    private val _uiState = MutableStateFlow(SwapnobajUiState())
    val uiState: StateFlow<SwapnobajUiState> = _uiState.asStateFlow()

    init {
        // Restore user session, bookmarks, and locally saved user stories (excluding any old seeds)
        val session = sessionManager.getUserSession()
        val savedBookmarks = sessionManager.getBookmarks()
        val localStories = sessionManager.getLocalStories().filter { !it.id.startsWith("story-seed-") }

        val allInitialStories = localStories.map { story ->
            if (savedBookmarks.contains(story.id)) story.copy(isBookmarked = true) else story
        }

        _uiState.update { state ->
            state.copy(
                isUserLoggedIn = session.isLoggedIn,
                loggedInUserName = session.userName,
                loggedInUserHandle = session.userHandle,
                loggedInUserBio = session.userBio,
                userRole = session.role,
                stories = allInitialStories
            )
        }

        // Purge any old seed stories from cloud so Firestore has only real user-submitted stories
        viewModelScope.launch {
            try {
                if (cloudRepository.isCloudAvailable) {
                    cloudRepository.purgeSeedStories()
                }
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            try {
                cloudRepository.getStoriesFlow().collect { cloudStories ->
                    val cleanCloud = cloudStories.filter { !it.id.startsWith("story-seed-") }
                    _uiState.update { state ->
                        val currentBookmarks = sessionManager.getBookmarks()
                        val cloudIds = cleanCloud.map { it.id }.toSet()
                        val localOnly = state.stories.filter { it.id !in cloudIds && !it.id.startsWith("story-seed-") }
                        val combined = (cleanCloud + localOnly).map { story ->
                            if (currentBookmarks.contains(story.id)) story.copy(isBookmarked = true) else story
                        }
                        state.copy(stories = combined)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun selectHomeTab(tab: HomeTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectGenre(genre: String) {
        _uiState.update { it.copy(selectedGenre = genre) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openReader(story: Story, chapterIndex: Int = 0) {
        _uiState.update {
            it.copy(
                currentReaderStory = story,
                currentChapterIndex = chapterIndex
            )
        }
    }

    fun closeReader() {
        _uiState.update { it.copy(currentReaderStory = null) }
    }

    fun selectChapter(index: Int) {
        _uiState.update { it.copy(currentChapterIndex = index) }
    }

    fun setReadingTheme(theme: ReadingTheme) {
        _uiState.update { it.copy(readingTheme = theme) }
    }

    fun increaseFontSize() {
        _uiState.update {
            if (it.readingFontSizeSp < 26f) it.copy(readingFontSizeSp = it.readingFontSizeSp + 2f) else it
        }
    }

    fun decreaseFontSize() {
        _uiState.update {
            if (it.readingFontSizeSp > 14f) it.copy(readingFontSizeSp = it.readingFontSizeSp - 2f) else it
        }
    }

    fun toggleReaction(storyId: String, reaction: ReactionType) {
        _uiState.update { state ->
            val updated = state.stories.map { story ->
                if (story.id == storyId) {
                    if (story.userReaction == reaction) {
                        // Undo reaction
                        val (l, lo, f, d) = when (reaction) {
                            ReactionType.LIKE -> listOf(story.likesCount - 1, story.lovesCount, story.firesCount, story.dislikesCount)
                            ReactionType.LOVE -> listOf(story.likesCount, story.lovesCount - 1, story.firesCount, story.dislikesCount)
                            ReactionType.FIRE -> listOf(story.likesCount, story.lovesCount, story.firesCount - 1, story.dislikesCount)
                            ReactionType.DISLIKE -> listOf(story.likesCount, story.lovesCount, story.firesCount, story.dislikesCount - 1)
                        }
                        story.copy(
                            userReaction = null,
                            likesCount = maxOf(0, l),
                            lovesCount = maxOf(0, lo),
                            firesCount = maxOf(0, f),
                            dislikesCount = maxOf(0, d)
                        )
                    } else {
                        // Switch or new reaction
                        var newL = story.likesCount
                        var newLo = story.lovesCount
                        var newF = story.firesCount
                        var newD = story.dislikesCount

                        // Decrement previous if existed
                        story.userReaction?.let { prev ->
                            when (prev) {
                                ReactionType.LIKE -> newL--
                                ReactionType.LOVE -> newLo--
                                ReactionType.FIRE -> newF--
                                ReactionType.DISLIKE -> newD--
                            }
                        }

                        // Increment new
                        when (reaction) {
                            ReactionType.LIKE -> newL++
                            ReactionType.LOVE -> newLo++
                            ReactionType.FIRE -> newF++
                            ReactionType.DISLIKE -> newD++
                        }

                        story.copy(
                            userReaction = reaction,
                            likesCount = maxOf(0, newL),
                            lovesCount = maxOf(0, newLo),
                            firesCount = maxOf(0, newF),
                            dislikesCount = maxOf(0, newD)
                        )
                    }
                } else {
                    story
                }
            }

            val updatedCurrent = if (state.currentReaderStory?.id == storyId) {
                updated.find { it.id == storyId }
            } else state.currentReaderStory

            state.copy(stories = updated, currentReaderStory = updatedCurrent)
        }
    }

    fun toggleBookmark(storyId: String) {
        val isNowBookmarked = sessionManager.toggleBookmark(storyId)
        _uiState.update { state ->
            val updated = state.stories.map { story ->
                if (story.id == storyId) story.copy(isBookmarked = isNowBookmarked) else story
            }
            val current = if (state.currentReaderStory?.id == storyId) {
                updated.find { it.id == storyId }
            } else state.currentReaderStory
            val msg = if (isNowBookmarked) "বইয়ের তাকে সংরক্ষণ করা হয়েছে" else "সংরক্ষণ বাতিল করা হয়েছে"
            state.copy(stories = updated, currentReaderStory = current, successSnackbarMessage = msg)
        }
    }

    fun toggleFollow(authorHandle: String) {
        _uiState.update { state ->
            val isNowFollowing = state.stories.find { it.authorHandle == authorHandle }?.isFollowing == false
            val updated = state.stories.map { story ->
                if (story.authorHandle == authorHandle) story.copy(isFollowing = !story.isFollowing) else story
            }
            val msg = if (isNowFollowing) "$authorHandle কে অনুসরণ করা হচ্ছে" else "অনুসরণ বাতিল করা হয়েছে"
            state.copy(stories = updated, successSnackbarMessage = msg)
        }
    }

    fun openCommentsSheet(story: Story) {
        val existingComments = _uiState.value.commentsByStoryId[story.id] ?: emptyList()
        _uiState.update {
            it.copy(
                isCommentsSheetOpen = true,
                activeCommentsStory = story,
                currentComments = existingComments
            )
        }
        // Listen to live comments for this story from cloud
        viewModelScope.launch {
            try {
                cloudRepository.getCommentsFlow(story.id).collect { cloudComments ->
                    _uiState.update { state ->
                        val updatedMap = state.commentsByStoryId.toMutableMap()
                        updatedMap[story.id] = cloudComments
                        val updatedStories = state.stories.map { s ->
                            if (s.id == story.id) s.copy(commentsCount = cloudComments.size) else s
                        }
                        val updatedReader = if (state.currentReaderStory?.id == story.id) {
                            state.currentReaderStory?.copy(commentsCount = cloudComments.size)
                        } else state.currentReaderStory

                        state.copy(
                            commentsByStoryId = updatedMap,
                            currentComments = if (state.activeCommentsStory?.id == story.id) cloudComments else state.currentComments,
                            stories = updatedStories,
                            currentReaderStory = updatedReader
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun closeCommentsSheet() {
        _uiState.update { it.copy(isCommentsSheetOpen = false, activeCommentsStory = null) }
    }

    fun addComment(text: String) {
        if (text.isBlank()) return
        val story = _uiState.value.activeCommentsStory ?: _uiState.value.currentReaderStory
        val storyId = story?.id ?: "general"
        val commenterName = if (_uiState.value.isUserLoggedIn && _uiState.value.loggedInUserName.isNotBlank()) {
            _uiState.value.loggedInUserName
        } else {
            "স্বপ্নবাজ পাঠক"
        }
        val newComment = Comment(
            id = "comment-${System.currentTimeMillis()}",
            storyId = storyId,
            authorName = commenterName,
            authorAvatar = "",
            text = text.trim(),
            timeAgo = "এইমাত্র",
            likesCount = 0
        )
        _uiState.update { state ->
            val updatedMap = state.commentsByStoryId.toMutableMap()
            val list = listOf(newComment) + (updatedMap[storyId] ?: emptyList())
            updatedMap[storyId] = list

            val updatedStories = state.stories.map { s ->
                if (s.id == storyId) s.copy(commentsCount = s.commentsCount + 1) else s
            }

            val updatedReaderStory = if (state.currentReaderStory?.id == storyId) {
                state.currentReaderStory?.copy(commentsCount = (state.currentReaderStory?.commentsCount ?: 0) + 1)
            } else state.currentReaderStory

            state.copy(
                commentsByStoryId = updatedMap,
                currentComments = list,
                stories = updatedStories,
                currentReaderStory = updatedReaderStory,
                successSnackbarMessage = "আপনার মন্তব্যটি সফলভাবে প্রকাশিত হয়েছে ✨"
            )
        }

        // Upload comment to cloud asynchronously
        viewModelScope.launch {
            try {
                cloudRepository.addCommentToCloud(storyId, newComment)
            } catch (_: Exception) {}
        }
    }

    fun openReviewsSheet(story: Story) {
        val existingReviews = _uiState.value.reviewsByStoryId[story.id] ?: emptyList()
        _uiState.update {
            it.copy(
                isReviewModalOpen = true,
                activeReviewStory = story,
                currentReviews = existingReviews
            )
        }
        // Listen to live reviews from cloud
        viewModelScope.launch {
            try {
                cloudRepository.getReviewsFlow(story.id).collect { cloudReviews ->
                    _uiState.update { state ->
                        val updatedMap = state.reviewsByStoryId.toMutableMap()
                        updatedMap[story.id] = cloudReviews
                        val avg = if (cloudReviews.isNotEmpty()) cloudReviews.map { r -> r.rating }.average() else 0.0
                        val updatedStories = state.stories.map { s ->
                            if (s.id == story.id) s.copy(reviewsCount = cloudReviews.size, averageRating = avg) else s
                        }
                        val updatedReader = if (state.currentReaderStory?.id == story.id) {
                            state.currentReaderStory?.copy(reviewsCount = cloudReviews.size, averageRating = avg)
                        } else state.currentReaderStory

                        state.copy(
                            reviewsByStoryId = updatedMap,
                            currentReviews = if (state.activeReviewStory?.id == story.id) cloudReviews else state.currentReviews,
                            stories = updatedStories,
                            currentReaderStory = updatedReader
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun closeReviewsSheet() {
        _uiState.update { it.copy(isReviewModalOpen = false, activeReviewStory = null) }
    }

    fun addReview(storyId: String, rating: Int, reviewText: String) {
        if (reviewText.isBlank()) return
        val reviewerName = if (_uiState.value.isUserLoggedIn && _uiState.value.loggedInUserName.isNotBlank()) {
            _uiState.value.loggedInUserName
        } else {
            "স্বপ্নবাজ পাঠক"
        }
        val newReview = StoryReview(
            id = "review-${System.currentTimeMillis()}",
            storyId = storyId,
            reviewerName = reviewerName,
            reviewerRole = _uiState.value.userRole.banglaLabel,
            rating = rating.coerceIn(1, 5),
            reviewText = reviewText.trim(),
            timeAgo = "এইমাত্র"
        )
        _uiState.update { state ->
            val updatedMap = state.reviewsByStoryId.toMutableMap()
            val list = listOf(newReview) + (updatedMap[storyId] ?: emptyList())
            updatedMap[storyId] = list
            val newAvg = list.map { it.rating }.average()
            val updatedStories = state.stories.map { s ->
                if (s.id == storyId) s.copy(
                    reviewsCount = list.size,
                    averageRating = newAvg
                ) else s
            }
            val updatedReaderStory = if (state.currentReaderStory?.id == storyId) {
                state.currentReaderStory?.copy(
                    reviewsCount = list.size,
                    averageRating = newAvg
                )
            } else state.currentReaderStory

            state.copy(
                reviewsByStoryId = updatedMap,
                currentReviews = list,
                stories = updatedStories,
                currentReaderStory = updatedReaderStory,
                successSnackbarMessage = "আপনার মূল্যবান পাঠক রিভিউ ও রেটিং সফলভাবে জমা হয়েছে ⭐"
            )
        }

        // Upload review to cloud
        viewModelScope.launch {
            try {
                cloudRepository.addReviewToCloud(storyId, newReview)
            } catch (_: Exception) {}
        }
    }

    fun switchRole(role: UserRole) {
        sessionManager.setUserRole(role)
        _uiState.update {
            it.copy(
                userRole = role,
                successSnackbarMessage = if (role == UserRole.WRITER) "আপনি লেখক মোডে যুক্ত হলেন ✍️" else "আপনি পাঠক মোডে আছেন 📖"
            )
        }
    }

    fun openTipDialog(authorName: String) {
        _uiState.update { it.copy(isTipDialogOpen = true, tipTargetAuthor = authorName) }
    }

    fun closeTipDialog() {
        _uiState.update { it.copy(isTipDialogOpen = false, tipTargetAuthor = null) }
    }

    fun confirmTip(amount: Int) {
        val author = _uiState.value.tipTargetAuthor ?: "লেখক"
        _uiState.update {
            it.copy(
                isTipDialogOpen = false,
                tipTargetAuthor = null,
                successSnackbarMessage = "ধন্যবাদ! $author কে ৳$amount সহায়তা পাঠানো হয়েছে 🎉"
            )
        }
    }

    fun toggleBookshelfLayout() {
        _uiState.update { it.copy(isBookshelfGridView = !it.isBookshelfGridView) }
    }

    fun setBookshelfFilter(filter: String) {
        _uiState.update { it.copy(bookshelfFilter = filter) }
    }

    fun publishNewStory(
        title: String,
        content: String,
        type: StoryType,
        genre: String,
        chapters: List<Chapter>
    ) {
        if (_uiState.value.userRole != UserRole.WRITER) {
            _uiState.update {
                it.copy(successSnackbarMessage = "শুধুমাত্র নিবন্ধিত লেখকরা গল্প, কবিতা ও উপন্যাস লিখতে পারেন। পাঠকরা শুধু পড়তে ও রিভিউ দিতে পারবেন।")
            }
            return
        }

        val writerName = if (_uiState.value.isUserLoggedIn && _uiState.value.loggedInUserName.isNotBlank()) {
            _uiState.value.loggedInUserName
        } else {
            "স্বপ্নবাজ স্বাধীন লেখক"
        }
        val writerHandle = if (_uiState.value.isUserLoggedIn && _uiState.value.loggedInUserHandle.isNotBlank()) {
            _uiState.value.loggedInUserHandle
        } else {
            "@independent_writer"
        }
        val writerBio = if (_uiState.value.isUserLoggedIn && _uiState.value.loggedInUserBio.isNotBlank()) {
            _uiState.value.loggedInUserBio
        } else {
            "স্বপ্নবাজ পরিবারের নতুন কলমসেনানী।"
        }

        val newStory = Story(
            id = "story-${System.currentTimeMillis()}",
            title = title,
            excerpt = if (content.length > 120) content.take(120) + "..." else content,
            fullContent = content,
            authorName = writerName,
            authorHandle = writerHandle,
            authorBio = writerBio,
            genre = genre,
            type = type,
            coverGradientStart = 0xFF1E3A8A,
            coverGradientEnd = 0xFF0D9488,
            likesCount = 1,
            lovesCount = 1,
            firesCount = 0,
            dislikesCount = 0,
            commentsCount = 0,
            readTimeMinutes = maxOf(1, content.split(" ").size / 150),
            publishedDate = "এইমাত্র",
            chapters = chapters
        )

        _uiState.update {
            it.copy(
                stories = listOf(newStory) + it.stories,
                successSnackbarMessage = "অভিনন্দন! আপনার লেখাটি সফলভাবে প্রকাশিত হয়েছে ✨"
            )
        }

        // Save locally so the story is never lost across app restarts
        sessionManager.saveLocalStory(newStory)

        // Upload new story to cloud for all readers
        viewModelScope.launch {
            try {
                cloudRepository.publishStoryToCloud(newStory)
            } catch (_: Exception) {}
        }
    }

    fun loginAuthor(
        name: String,
        penName: String = "",
        bio: String = "",
        password: String = "",
        role: UserRole = UserRole.WRITER
    ) {
        val cleanName = if (name.isNotBlank()) name.trim() else if (role == UserRole.WRITER) "স্বপ্নবাজ লেখক" else "স্বপ্নবাজ পাঠক"
        val handle = if (penName.isNotBlank()) {
            "@" + penName.trim().lowercase().replace(" ", "_").removePrefix("@")
        } else {
            "@" + cleanName.lowercase().replace(" ", "_").removePrefix("@")
        }
        val authorBio = if (bio.isNotBlank()) bio.trim() else if (role == UserRole.WRITER) {
            "বাংলা সাহিত্যের অনুরাগী লেখক ও স্বপ্নবাজের গর্বিত সদস্য।"
        } else {
            "বাংলা সাহিত্যের অনুরাগী একজন একনিষ্ঠ পাঠক।"
        }

        // Save account credentials and active session to SharedPreferences
        sessionManager.registerAccount(cleanName, handle, authorBio, password, role)
        sessionManager.saveUserSession(
            isLoggedIn = true,
            userName = cleanName,
            userHandle = handle,
            userBio = authorBio,
            role = role
        )

        _uiState.update {
            it.copy(
                isUserLoggedIn = true,
                loggedInUserName = cleanName,
                loggedInUserHandle = handle,
                loggedInUserBio = authorBio,
                userRole = role,
                isAuthModalOpen = false,
                successSnackbarMessage = "স্বাগতম, $cleanName! ${role.banglaLabel} হিসেবে সফলভাবে লগইন হয়েছে ✨"
            )
        }
    }

    fun verifyAndLogin(nameOrHandle: String, password: String): Boolean {
        val account = sessionManager.verifyLogin(nameOrHandle, password)
        return if (account != null) {
            sessionManager.saveUserSession(
                isLoggedIn = true,
                userName = account.name,
                userHandle = account.handle,
                userBio = account.bio,
                role = account.role
            )
            _uiState.update {
                it.copy(
                    isUserLoggedIn = true,
                    loggedInUserName = account.name,
                    loggedInUserHandle = account.handle,
                    loggedInUserBio = account.bio,
                    userRole = account.role,
                    isAuthModalOpen = false,
                    successSnackbarMessage = "স্বাগতম ফিরে আসার জন্য, ${account.name}! (${account.role.banglaLabel}) ✨"
                )
            }
            true
        } else {
            false
        }
    }

    fun logoutAuthor() {
        sessionManager.clearUserSession()
        _uiState.update {
            it.copy(
                isUserLoggedIn = false,
                loggedInUserName = "",
                loggedInUserHandle = "",
                loggedInUserBio = "",
                userRole = UserRole.READER,
                successSnackbarMessage = "লগআউট সম্পন্ন হয়েছে। আপনি এখন সাধারণ পাঠক মোডে আছেন।"
            )
        }
    }

    fun openAuthModal() {
        _uiState.update { it.copy(isAuthModalOpen = true) }
    }

    fun closeAuthModal() {
        _uiState.update { it.copy(isAuthModalOpen = false) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(successSnackbarMessage = null) }
    }
}
