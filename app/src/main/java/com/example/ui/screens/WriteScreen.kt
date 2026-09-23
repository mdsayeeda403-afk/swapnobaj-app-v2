package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Chapter
import com.example.data.StoryType
import com.example.data.UserRole
import com.example.ui.SwapnobajUiState
import com.example.ui.SwapnobajViewModel

data class CategoryOption(
    val label: String,
    val type: StoryType,
    val genreName: String,
    val description: String
)

val defaultCategoryOptions = listOf(
    CategoryOption("ছোটগল্প (Story)", StoryType.GOLPO, "গল্প", "বাস্তব বা কল্পিত ছোটগল্প"),
    CategoryOption("কবিতা (Poem)", StoryType.KOBITA, "কবিতা", "ছন্দ, অন্ত্যমিল বা গদ্যকবিতা"),
    CategoryOption("উপন্যাস (Novel)", StoryType.UPONNAS, "উপন্যাস", "ধারাবাহিক অধ্যায়ভিত্তিক দীর্ঘ সাহিত্য"),
    CategoryOption("অনুগল্প (Micro-story)", StoryType.GOLPO, "অনুগল্প", "খুব সংক্ষিপ্ত হৃদয়স্পর্শী গল্প"),
    CategoryOption("রোমান্স ও প্রেম (Romance)", StoryType.GOLPO, "রোমান্স", "অনুভূতি ও ভালোবাসার কথকতা"),
    CategoryOption("থ্রিলার ও রহস্য (Thriller)", StoryType.GOLPO, "থ্রিলার ও রহস্য", "রোমাঞ্চকর গোয়েন্দা ও রহস্য কাহিনী"),
    CategoryOption("ভৌতিক (Horror)", StoryType.GOLPO, "ভৌতিক", "ভয় ও অতিলৌকিক ঘটনার গল্প"),
    CategoryOption("সামাজিক ও জীবনবোধ (Social)", StoryType.GOLPO, "সামাজিক", "সমাজ ও জীবনের বাস্তব চিত্র"),
    CategoryOption("বিজ্ঞান কল্পকাহিনী (Sci-Fi)", StoryType.GOLPO, "বিজ্ঞান কল্পকাহিনী", "মহাকাশ, ভবিষ্যৎ ও বিজ্ঞানের গল্প")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel,
    modifier: Modifier = Modifier,
    onPublishSuccess: () -> Unit = {}
) {
    if (uiState.userRole == UserRole.READER) {
        ReaderRestrictedWriteView(
            uiState = uiState,
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var tagsInput by remember { mutableStateOf("#বাংলা_সাহিত্য #স্বপ্নবাজ") }
    var isPublishing by remember { mutableStateOf(false) }

    // Validation error states
    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }

    val currentCategory = defaultCategoryOptions.getOrElse(selectedCategoryIndex) { defaultCategoryOptions[0] }
    val selectedType = currentCategory.type
    val selectedGenre = currentCategory.genreName

    // Novel Chapters state
    val chapters = remember {
        mutableStateListOf(
            Chapter(id = "ch-1", chapterNumber = 1, title = "প্রথম অধ্যায়: সূচনা", content = "", readTimeMinutes = 5)
        )
    }
    var activeChapterIndex by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        // Creative Studio Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFEFF6FF),
                                Color(0xFFDBEAFE).copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_writer_art),
                    contentDescription = "Writer Quill Art",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "গল্প ও কবিতা প্রকাশনা",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "শিরোনাম, ক্যাটাগরি ও বিষয়বস্তু লিখে সরাসরি স্বপ্নবাজে প্রকাশ করুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Author login status indicator
        if (!uiState.isUserLoggedIn) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আপনি বেনামী লেখক হিসেবে আছেন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "আপনার স্থায়ী নামে প্রকাশ করতে প্রোফাইল থেকে লগইন করুন।",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.openAuthModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("লগইন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "লেখক: ${uiState.loggedInUserName} (${uiState.loggedInUserHandle})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.openAuthModal() }) {
                        Text("পরিবর্তন", fontSize = 12.sp, color = Color(0xFF047857))
                    }
                }
            }
        }

        // Feature 1 Requirement: 1. Story Publishing Screen (Title, Category dropdown, and Content input)

        // 1. Title Input with Validation
        Text(
            text = "১. লেখার শিরোনাম (Story Title) *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (it.isNotBlank()) titleError = null
            },
            placeholder = { Text("যেমন: বৃষ্টির দিনের এক টুকরো স্মৃতি...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            isError = titleError != null,
            supportingText = {
                if (titleError != null) {
                    Text(text = titleError!!, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = "${title.length}/100 অক্ষর", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("story_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Category Dropdown with Bengali literature options
        Text(
            text = "২. ক্যাটাগরি / বিভাগ নির্বাচন করুন (Category Dropdown) *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = isCategoryDropdownExpanded,
            onExpandedChange = { isCategoryDropdownExpanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("story_category_dropdown")
        ) {
            OutlinedTextField(
                value = currentCategory.label,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                supportingText = {
                    Text(
                        text = "ধরন: ${currentCategory.type.banglaLabel} • ${currentCategory.description}",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            ExposedDropdownMenu(
                expanded = isCategoryDropdownExpanded,
                onDismissRequest = { isCategoryDropdownExpanded = false }
            ) {
                defaultCategoryOptions.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = option.label,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategoryIndex == index) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                                Text(
                                    text = option.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            selectedCategoryIndex = index
                            isCategoryDropdownExpanded = false
                        },
                        leadingIcon = {
                            if (selectedCategoryIndex == index) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Novel Specific: Chapter-by-chapter tabs & add chapter button
        if (selectedType == StoryType.UPONNAS) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "উপন্যাসের অধ্যায়সমূহ (${chapters.size}টি অধ্যায়)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = {
                                val nextNum = chapters.size + 1
                                chapters.add(
                                    Chapter(
                                        id = "ch-$nextNum",
                                        chapterNumber = nextNum,
                                        title = "অধ্যায় $nextNum",
                                        content = "",
                                        readTimeMinutes = 5
                                    )
                                )
                                activeChapterIndex = chapters.lastIndex
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন অধ্যায়", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ScrollableTabRow(
                        selectedTabIndex = activeChapterIndex,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        chapters.forEachIndexed { idx, _ ->
                            Tab(
                                selected = activeChapterIndex == idx,
                                onClick = { activeChapterIndex = idx },
                                text = { Text("অধ্যায় ${idx + 1}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Content Input with Formatting and Word Counter
        val activeContent = if (selectedType == StoryType.UPONNAS && chapters.isNotEmpty()) {
            chapters[activeChapterIndex].content
        } else content

        val wordCount = if (activeContent.isBlank()) 0 else activeContent.trim().split("\\s+".toRegex()).size

        Text(
            text = "৩. গল্প / লেখার মূল বিষয়বস্তু (Story Content) *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Formatting toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val newText = if (activeContent.isNotBlank()) activeContent + " **গুরুত্বপূর্ণ কথা**" else "**গুরুত্বপূর্ণ কথা**"
                            if (selectedType == StoryType.UPONNAS && chapters.isNotEmpty()) {
                                chapters[activeChapterIndex] = chapters[activeChapterIndex].copy(content = newText)
                            } else {
                                content = newText
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            val newText = if (activeContent.isNotBlank()) activeContent + " *বাঁকা লেখা*" else "*বাঁকা লেখা*"
                            if (selectedType == StoryType.UPONNAS && chapters.isNotEmpty()) {
                                chapters[activeChapterIndex] = chapters[activeChapterIndex].copy(content = newText)
                            } else {
                                content = newText
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            val newText = if (activeContent.isNotBlank()) activeContent + "\n\n> উদ্ধৃতি বাক্য...\n\n" else "> উদ্ধৃতি বাক্য...\n\n"
                            if (selectedType == StoryType.UPONNAS && chapters.isNotEmpty()) {
                                chapters[activeChapterIndex] = chapters[activeChapterIndex].copy(content = newText)
                            } else {
                                content = newText
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = "$wordCount শব্দ • ${(wordCount / 120).coerceAtLeast(1)} মিনিট পাঠ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = activeContent,
            onValueChange = { newText ->
                if (selectedType == StoryType.UPONNAS && chapters.isNotEmpty()) {
                    val current = chapters[activeChapterIndex]
                    chapters[activeChapterIndex] = current.copy(content = newText)
                } else {
                    content = newText
                }
                if (newText.isNotBlank()) contentError = null
            },
            placeholder = {
                Text(
                    if (selectedType == StoryType.KOBITA) "এখানে আপনার কবিতার ছন্দ ও শব্দগুলো লিখুন..."
                    else if (selectedType == StoryType.UPONNAS) "অধ্যায় ${activeChapterIndex + 1}-এর উপন্যাস এখানে লিখুন..."
                    else "এখানে আপনার গল্পটির বিস্তারিত বিষয়বস্তু লিখুন..."
                )
            },
            isError = contentError != null,
            supportingText = {
                if (contentError != null) {
                    Text(text = contentError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .testTag("story_content_input"),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tags Input
        OutlinedTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            label = { Text("ট্যাগসমূহ (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Save Draft & Publish to Firestore Real-Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val draftTitle = if (title.isBlank()) "নামহীন খসড়া" else title
                    val draftContent = if (activeContent.isBlank()) "খসড়া সংরক্ষিত রয়েছে।" else activeContent
                    viewModel.publishNewStory(
                        title = draftTitle,
                        content = draftContent,
                        type = selectedType,
                        genre = selectedGenre,
                        chapters = if (selectedType == StoryType.UPONNAS) chapters else emptyList()
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("draft_story_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ড্রাফট রাখুন", fontSize = 14.sp)
            }

            Button(
                onClick = {
                    var hasError = false
                    if (title.isBlank()) {
                        titleError = "অনুগ্রহ করে গল্পের একটি আকর্ষণীয় শিরোনাম লিখুন"
                        hasError = true
                    }
                    if (activeContent.isBlank()) {
                        contentError = "অনুগ্রহ করে আপনার লেখার মূল বিষয়বস্তু লিখুন"
                        hasError = true
                    }

                    if (!hasError) {
                        isPublishing = true
                        viewModel.publishNewStory(
                            title = title.trim(),
                            content = activeContent.trim(),
                            type = selectedType,
                            genre = selectedGenre,
                            chapters = if (selectedType == StoryType.UPONNAS) chapters else emptyList()
                        )
                        title = ""
                        content = ""
                        isPublishing = false
                        onPublishSuccess()
                    }
                },
                enabled = !isPublishing,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("publish_story_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রকাশ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReaderRestrictedWriteView(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_writer_art),
                    contentDescription = "Writer Only",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📖 বর্তমান মোড: সম্মানিত পাঠক",
                        color = Color(0xFF1D4ED8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "লেখা শুধুমাত্র লেখকদের জন্য ✍️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "স্বপ্নবাজে গল্প, কবিতা ও উপন্যাস শুধুমাত্র লেখকরাই প্রকাশ করতে পারেন। পাঠকরা সকল লেখা পড়তে এবং তারকা রেটিং ও রিভিউ দিতে পারবেন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "✨ পাঠক হিসেবে আপনার সুবিধাসমূহ:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• সকল লেখকের প্রকাশিত গল্প ও কবিতা বিনামূল্যে পড়া", fontSize = 12.sp, color = Color(0xFF78350F))
                        Text("• ১ থেকে ৫ তারকা রেটিং দিয়ে গল্প মূল্যায়ন করা", fontSize = 12.sp, color = Color(0xFF78350F))
                        Text("• প্রতিটি গল্পে আপনার অনুভূতি ও বিস্তারিত রিভিউ দেওয়া", fontSize = 12.sp, color = Color(0xFF78350F))
                        Text("• প্রিয় গল্প ও কবিতা বুকমার্কে সংরক্ষণ করা", fontSize = 12.sp, color = Color(0xFF78350F))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "আপনিও কি গল্প বা কবিতা লিখতে চান?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.switchRole(UserRole.WRITER) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("লেখক মোডে পরিবর্তন করুন (লেখক হন ✍️)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.openAuthModal() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("লেখক হিসেবে অ্যাকাউন্ট খুলুন বা লগইন করুন", fontSize = 13.sp)
                }
            }
        }
    }
}
