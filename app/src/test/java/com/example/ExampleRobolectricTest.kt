package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ReactionType
import com.example.data.ReadingTheme
import com.example.data.SessionManager
import com.example.data.Story
import com.example.data.StoryType
import com.example.ui.SwapnobajViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("স্বপ্নবাজ", appName)
    }

    @Test
    fun `session manager saves and restores user session`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)

        sessionManager.saveUserSession(
            isLoggedIn = true,
            userName = "কাজী নজরুল",
            userHandle = "@nazrul",
            userBio = "বিদ্রোহী কবি"
        )

        val session = sessionManager.getUserSession()
        assertTrue(session.isLoggedIn)
        assertEquals("কাজী নজরুল", session.userName)
        assertEquals("@nazrul", session.userHandle)
        assertEquals("বিদ্রোহী কবি", session.userBio)

        sessionManager.clearUserSession()
        val clearedSession = sessionManager.getUserSession()
        assertFalse(clearedSession.isLoggedIn)
        assertEquals("", clearedSession.userName)
    }

    @Test
    fun `session manager registers and verifies login`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)

        val registered = sessionManager.registerAccount(
            name = "রবীন্দ্রনাথ ঠাকুর",
            handle = "@tagore",
            bio = "বিশ্বকবি",
            password = "pass"
        )
        assertTrue(registered)

        // Verify with name
        val accByName = sessionManager.verifyLogin("রবীন্দ্রনাথ ঠাকুর", "pass")
        assertNotNull(accByName)
        assertEquals("রবীন্দ্রনাথ ঠাকুর", accByName?.name)

        // Verify with handle
        val accByHandle = sessionManager.verifyLogin("@tagore", "pass")
        assertNotNull(accByHandle)
        assertEquals("@tagore", accByHandle?.handle)
    }

    @Test
    fun `session manager toggles and preserves bookmarks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)

        val storyId = "story-test-101"
        val added = sessionManager.toggleBookmark(storyId)
        assertTrue(added)
        assertTrue(sessionManager.getBookmarks().contains(storyId))

        val removed = sessionManager.toggleBookmark(storyId)
        assertFalse(removed)
        assertFalse(sessionManager.getBookmarks().contains(storyId))
    }

    @Test
    fun `session manager saves and loads local stories`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)

        val testStory = Story(
            id = "test-unique-id",
            title = "একটি নতুন সকাল",
            excerpt = "সকালের মিষ্টি রোদে...",
            fullContent = "সকালের মিষ্টি রোদে পাখির গান শুনে ঘুম ভাঙল।",
            authorName = "সালমা জাহান",
            authorHandle = "@salma_jahan",
            type = StoryType.GOLPO,
            genre = "জীবনবোধ",
            publishedDate = "এইমাত্র"
        )

        sessionManager.saveLocalStory(testStory)
        val loaded = sessionManager.getLocalStories()
        assertTrue(loaded.any { it.id == "test-unique-id" && it.title == "একটি নতুন সকাল" })
    }

    @Test
    fun `viewmodel handles login publish and bookmark`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SwapnobajViewModel(app)

        // Initial state must be completely empty
        val initialCount = viewModel.uiState.value.stories.size
        assertEquals(0, initialCount)

        // Login
        viewModel.loginAuthor("সালমা জাহান", "salma", "গল্পকার", "1234")
        assertTrue(viewModel.uiState.value.isUserLoggedIn)
        assertEquals("সালমা জাহান", viewModel.uiState.value.loggedInUserName)

        // Publish Story
        viewModel.publishNewStory(
            title = "বৃষ্টির গান",
            content = "বৃষ্টিভেজা বিকেলে এক কাপ চা হাতে বসে ছিলাম।",
            type = StoryType.KOBITA,
            genre = "রোমান্টিক",
            chapters = emptyList()
        )
        val afterCount = viewModel.uiState.value.stories.size
        assertEquals(1, afterCount)

        // Check new story author matches logged in user
        val published = viewModel.uiState.value.stories.first()
        assertEquals("বৃষ্টির গান", published.title)
        assertEquals("সালমা জাহান", published.authorName)

        // Reaction
        viewModel.toggleReaction(published.id, ReactionType.LOVE)
        val updatedStory = viewModel.uiState.value.stories.find { it.id == published.id }
        assertEquals(ReactionType.LOVE, updatedStory?.userReaction)

        // Bookmark
        viewModel.toggleBookmark(published.id)
        val bookmarkedStory = viewModel.uiState.value.stories.find { it.id == published.id }
        assertTrue(bookmarkedStory?.isBookmarked == true)
    }

    @Test
    fun `category filtering and story detail view reader works correctly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SwapnobajViewModel(app)

        // Publish a poem
        viewModel.publishNewStory(
            title = "সন্ধ্যার বাঁশি",
            content = "নদীর কূলে একা বসে বাঁশির সুর শুনি...",
            type = StoryType.KOBITA,
            genre = "কবিতা",
            chapters = emptyList()
        )

        // Publish a novel
        viewModel.publishNewStory(
            title = "মেঘের ডানা",
            content = "প্রথম পরিচ্ছেদ: যাত্রা শুরু হলো।",
            type = StoryType.UPONNAS,
            genre = "উপন্যাস",
            chapters = listOf(
                com.example.data.Chapter("ch-1", 1, "প্রথম অধ্যায়", "যাত্রা শুরু হলো।", 5)
            )
        )

        val allStories = viewModel.uiState.value.stories
        assertTrue(allStories.size >= 2)

        // Verify category filtering logic
        val poems = allStories.filter { it.type == StoryType.KOBITA || it.genre.contains("কবিতা") }
        assertTrue(poems.any { it.title == "সন্ধ্যার বাঁশি" })

        val novels = allStories.filter { it.type == StoryType.UPONNAS || it.genre.contains("উপন্যাস") }
        assertTrue(novels.any { it.title == "মেঘের ডানা" })

        // Verify Reader Screen / Story Detail View opening
        val storyToRead = poems.first { it.title == "সন্ধ্যার বাঁশি" }
        viewModel.openReader(storyToRead, chapterIndex = 0)

        val readerState = viewModel.uiState.value
        assertEquals("সন্ধ্যার বাঁশি", readerState.currentReaderStory?.title)
        assertEquals(0, readerState.currentChapterIndex)
        assertTrue(readerState.currentReaderStory?.fullContent?.contains("নদীর কূলে একা বসে") == true)

        // Adjust reader settings
        val initialSize = viewModel.uiState.value.readingFontSizeSp
        viewModel.increaseFontSize()
        assertEquals(initialSize + 2f, viewModel.uiState.value.readingFontSizeSp)

        viewModel.setReadingTheme(ReadingTheme.SEPIA)
        assertEquals(ReadingTheme.SEPIA, viewModel.uiState.value.readingTheme)

        // Close reader
        viewModel.closeReader()
        assertEquals(null, viewModel.uiState.value.currentReaderStory)
    }
}
