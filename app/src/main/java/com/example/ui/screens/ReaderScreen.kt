package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReactionType
import com.example.data.ReadingTheme
import com.example.data.Story
import com.example.data.StoryType
import com.example.ui.SwapnobajUiState
import com.example.ui.SwapnobajViewModel
import com.example.ui.theme.NightBackground
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightText
import com.example.ui.theme.SepiaBackground
import com.example.ui.theme.SepiaSurface
import com.example.ui.theme.SepiaText
import com.example.ui.theme.SoftCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    story: Story,
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel,
    modifier: Modifier = Modifier
) {
    var showChapterSheet by remember { mutableStateOf(false) }
    var showSettingsBar by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Dynamic Color Palette for Reading Modes
    val (bgColor, textColor, surfaceColor, accentColor) = when (uiState.readingTheme) {
        ReadingTheme.LIGHT -> listOf(
            SoftCream,
            Color(0xFF1E293B),
            Color(0xFFFFFFFF),
            Color(0xFF1E3A8A)
        )
        ReadingTheme.SEPIA -> listOf(
            SepiaBackground,
            SepiaText,
            SepiaSurface,
            Color(0xFF854D0E)
        )
        ReadingTheme.DARK -> listOf(
            NightBackground,
            NightText,
            NightSurface,
            Color(0xFFF59E0B)
        )
    }

    // Current Content (if Novel, show selected chapter; otherwise fullContent)
    val isNovel = story.type == StoryType.UPONNAS && story.chapters.isNotEmpty()
    val activeChapter = if (isNovel) story.chapters.getOrNull(uiState.currentChapterIndex) else null
    val currentReadingTitle = activeChapter?.title ?: story.title
    val currentReadingBody = activeChapter?.content ?: story.fullContent

    // Reading Progress calculation
    val readingProgress by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems <= 1) 0f
            else {
                val firstVisible = listState.firstVisibleItemIndex.toFloat()
                (firstVisible / (totalItems - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = story.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = "${story.authorName} • ${story.genre}",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.closeReader() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                    },
                    actions = {
                        if (isNovel) {
                            IconButton(onClick = { showChapterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Chapters",
                                    tint = textColor
                                )
                            }
                        }
                        IconButton(onClick = { showSettingsBar = !showSettingsBar }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Reading Settings",
                                tint = textColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )

                // Pinned Reading Progress Bar
                LinearProgressIndicator(
                    progress = { if (readingProgress > 0f) readingProgress else 0.25f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = accentColor,
                    trackColor = surfaceColor.copy(alpha = 0.5f)
                )

                // Expandable Typography & Reading Settings Toolbar
                if (showSettingsBar) {
                    ReadingSettingsToolbar(
                        currentTheme = uiState.readingTheme,
                        fontSizeSp = uiState.readingFontSizeSp,
                        onThemeChange = { viewModel.setReadingTheme(it) },
                        onIncreaseFont = { viewModel.increaseFontSize() },
                        onDecreaseFont = { viewModel.decreaseFontSize() },
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
            }
        },
        bottomBar = {
            // Floating Reading Engagement Dock
            Surface(
                color = surfaceColor,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quick Reactions
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReactionButton(
                            emoji = "👍",
                            count = story.likesCount,
                            isSelected = story.userReaction == ReactionType.LIKE,
                            onClick = { viewModel.toggleReaction(story.id, ReactionType.LIKE) }
                        )
                        ReactionButton(
                            emoji = "❤️",
                            count = story.lovesCount,
                            isSelected = story.userReaction == ReactionType.LOVE,
                            onClick = { viewModel.toggleReaction(story.id, ReactionType.LOVE) }
                        )
                        ReactionButton(
                            emoji = "🔥",
                            count = story.firesCount,
                            isSelected = story.userReaction == ReactionType.FIRE,
                            onClick = { viewModel.toggleReaction(story.id, ReactionType.FIRE) }
                        )
                    }

                    // Comments, Reviews, Bookmark, and Tip Writer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.openReviewsSheet(story) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Reviews and Ratings",
                                tint = Color(0xFFD97706)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openCommentsSheet(story) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = textColor.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleBookmark(story.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (story.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (story.isBookmarked) accentColor else textColor.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { viewModel.openTipDialog(story.authorName) },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("টিপ দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Header: Category, Title, Author Details
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${story.type.banglaLabel} • ${story.genre}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentReadingTitle,
                        fontSize = (uiState.readingFontSizeSp + 8).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (uiState.readingFontSizeSp + 14).sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "লেখক: ${story.authorName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${story.publishedDate}",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = 18.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(textColor.copy(alpha = 0.15f))
                    )
                }
            }

            // Body Paragraphs with adjusted Bangla Line Spacing
            val paragraphs = currentReadingBody.split("\n\n").filter { it.isNotBlank() }
            itemsIndexed(paragraphs) { index, paragraph ->
                Text(
                    text = paragraph,
                    fontSize = uiState.readingFontSizeSp.sp,
                    color = textColor,
                    lineHeight = (uiState.readingFontSizeSp * 1.7f).sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontFamily = FontFamily.Default
                )
            }

            // End of Chapter / Story Note
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "— সমাপ্ত —",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isNovel && uiState.currentChapterIndex < story.chapters.lastIndex) {
                        Button(
                            onClick = { viewModel.selectChapter(uiState.currentChapterIndex + 1) },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("পরবর্তী অধ্যায়ে যান (${uiState.currentChapterIndex + 2})")
                        }
                    } else {
                        // Reader Review and Rating Banner Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.readingTheme == ReadingTheme.DARK) Color(0xFF1E293B) else Color(0xFFFEF3C7).copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⭐ পাঠক রিভিউ ও রেটিং",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.readingTheme == ReadingTheme.DARK) Color(0xFFFDE68A) else Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (story.reviewsCount > 0)
                                        "গড় রেটিং: ${String.format("%.1f", story.averageRating)} ★ (${story.reviewsCount} জন পাঠকের মূল্যায়ন)"
                                    else
                                        "গল্প, কবিতা বা উপন্যাসটি কেমন লেগেছে? আপনি কেমন উপভোগ করেছেন?",
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.openReviewsSheet(story) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("রিভিউ পড়ুন ও রেটিং দিন ⭐", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Chapter Navigation Modal Bottom Sheet
    if (showChapterSheet && isNovel) {
        ModalBottomSheet(
            onDismissRequest = { showChapterSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "উপন্যাসের অধ্যায়সমূহ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                story.chapters.forEachIndexed { index, chapter ->
                    val isCurrent = index == uiState.currentChapterIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                viewModel.selectChapter(index)
                                showChapterSheet = false
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = chapter.title,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) accentColor else Color.Unspecified
                            )
                            Text(
                                text = "${chapter.readTimeMinutes} মিনিট পড়ার সময়",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        if (isCurrent) {
                            Text(
                                text = "বর্তমানে পাঠরত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReadingSettingsToolbar(
    currentTheme: ReadingTheme,
    fontSizeSp: Float,
    onThemeChange: (ReadingTheme) -> Unit,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    surfaceColor: Color,
    textColor: Color,
    accentColor: Color
) {
    Surface(
        color = surfaceColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Theme selection: Light | Sepia | Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReadingThemePill(
                    label = "স্বাভাবিক",
                    previewColor = SoftCream,
                    isSelected = currentTheme == ReadingTheme.LIGHT,
                    onClick = { onThemeChange(ReadingTheme.LIGHT) }
                )
                ReadingThemePill(
                    label = "সেপিয়া",
                    previewColor = SepiaBackground,
                    isSelected = currentTheme == ReadingTheme.SEPIA,
                    onClick = { onThemeChange(ReadingTheme.SEPIA) }
                )
                ReadingThemePill(
                    label = "রাতের মোড",
                    previewColor = NightBackground,
                    isSelected = currentTheme == ReadingTheme.DARK,
                    onClick = { onThemeChange(ReadingTheme.DARK) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Font size controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ফন্ট সাইজ:",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.1f))
                        .clickable { onDecreaseFont() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("A-", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                }

                Text(
                    text = "${fontSizeSp.toInt()} pt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.1f))
                        .clickable { onIncreaseFont() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("A+", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                }
            }
        }
    }
}

@Composable
fun ReadingThemePill(
    label: String,
    previewColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF1D4ED8) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(previewColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (previewColor == NightBackground) Color.White else Color.Black
        )
    }
}
