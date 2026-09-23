package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.ui.SwapnobajUiState
import com.example.ui.SwapnobajViewModel

data class GenreCategory(
    val title: String,
    val iconEmoji: String,
    val color1: Long,
    val color2: Long
)

@Composable
fun SearchScreen(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel,
    modifier: Modifier = Modifier
) {
    val genreCategories = listOf(
        GenreCategory("গল্প", "📖", 0xFF1E3A8A, 0xFF0F172A),
        GenreCategory("কবিতা", "✍️", 0xFF92400E, 0xFFB45309),
        GenreCategory("উপন্যাস", "📚", 0xFF6D28D9, 0xFF4C1D95),
        GenreCategory("রোমান্স", "🌹", 0xFFE11D48, 0xFFBE123C),
        GenreCategory("থ্রিলার ও রহস্য", "🔍", 0xFF1E293B, 0xFF0F172A),
        GenreCategory("ভৌতিক", "👻", 0xFF4A044E, 0xFF2E1065),
        GenreCategory("সামাজিক", "🍃", 0xFF065F46, 0xFF047857),
        GenreCategory("বিজ্ঞান কল্পকাহিনী", "🚀", 0xFF1E40AF, 0xFF1E3A8A)
    )

    // Quick filter topics based on actual genres
    val quickTopics = SampleData.genres.filter { it != "সব" }

    val searchResults = if (uiState.searchQuery.isNotBlank()) {
        uiState.stories.filter {
            it.title.contains(uiState.searchQuery, ignoreCase = true) ||
            it.authorName.contains(uiState.searchQuery, ignoreCase = true) ||
            it.genre.contains(uiState.searchQuery, ignoreCase = true)
        }
    } else emptyList()

    // Real authors who have published stories on the platform
    val realAuthors = uiState.stories
        .groupBy { it.authorHandle }
        .values
        .mapNotNull { it.firstOrNull() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Search Header & Input
        item {
            Text(
                text = "অনুসন্ধান ও আবিষ্কার",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "শিরোনাম, লেখক অথবা সাহিত্যের ধরণ দিয়ে খুঁজে নিন",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("গল্প, কবিতা বা লেখকের নাম লিখুন...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
        }

        // Search Results Section if query is active
        if (uiState.searchQuery.isNotBlank()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "ফলাফল (${searchResults.size}টি পাওয়া গেছে)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (searchResults.isEmpty()) {
                item {
                    Text(
                        text = "\"${uiState.searchQuery}\"-এর জন্য কোনো লেখা পাওয়া যায়নি।",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(searchResults, key = { it.id }) { story ->
                    StoryFeedCard(
                        story = story,
                        onCardClick = { viewModel.openReader(story) },
                        onReactionClick = { reaction -> viewModel.toggleReaction(story.id, reaction) },
                        onBookmarkClick = { viewModel.toggleBookmark(story.id) },
                        onCommentsClick = { viewModel.openCommentsSheet(story) },
                        onReviewsClick = { viewModel.openReviewsSheet(story) },
                        onFollowClick = { viewModel.toggleFollow(story.authorHandle) }
                    )
                }
            }
        } else {
            // Quick Topic Search Chips
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "জনপ্রিয় বিষয়সমূহ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTopics) { topic ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .clickable { viewModel.updateSearchQuery(topic) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Genre Exploration Grid with Dynamic Counts
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "সাহিত্যের শাখা ও ধারা",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(genreCategories.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { cat ->
                        val storyCount = uiState.stories.count {
                            it.genre.contains(cat.title, ignoreCase = true) ||
                            it.type.banglaLabel.contains(cat.title, ignoreCase = true)
                        }
                        GenreBannerCard(
                            category = cat,
                            storyCount = storyCount,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateSearchQuery(cat.title) }
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Real Writers Spotlight
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "লেখক ও কবি",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (realAuthors.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "এখনো কোনো লেখক গল্প প্রকাশ করেননি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "স্বপ্নবাজে আপনার গল্প বা কবিতা প্রকাশ করে প্রথম লেখক হোন!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(realAuthors, key = { it.authorHandle }) { authorStory ->
                    val authorStoryCount = uiState.stories.count { it.authorHandle == authorStory.authorHandle }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = authorStory.authorName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = authorStory.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${authorStory.authorHandle} • $authorStoryCount টি প্রকাশিত সৃষ্টি",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.toggleFollow(authorStory.authorHandle) },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (authorStory.isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                    contentColor = if (authorStory.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (authorStory.isFollowing) "অনুসৃত" else "অনুসরণ", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreBannerCard(
    category: GenreCategory,
    storyCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(category.color1), Color(category.color2))
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(text = category.iconEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (storyCount > 0) "$storyCount টি সৃষ্টি" else "ব্রাউজ করুন",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
