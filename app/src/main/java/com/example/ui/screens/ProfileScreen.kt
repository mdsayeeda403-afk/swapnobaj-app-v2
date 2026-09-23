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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReactionType
import com.example.data.UserRole
import com.example.ui.SwapnobajUiState
import com.example.ui.SwapnobajViewModel

@Composable
fun ProfileScreen(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel,
    modifier: Modifier = Modifier
) {
    var selectedProfileTab by remember { mutableIntStateOf(0) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    val profileTabs = listOf("প্রকাশিত সৃষ্টি", "লেখক ড্যাশবোর্ড", "ড্রাফট")

    val userStories = uiState.stories.filter { it.authorHandle == uiState.loggedInUserHandle }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text("লগআউট নিশ্চিতকরণ", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("আপনি কি লেখক অ্যাকাউন্ট থেকে লগআউট করতে চান? লগআউট করলেও আপনার প্রকাশিত সৃষ্টিগুলো সুরক্ষিত থাকবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logoutAuthor()
                        showLogoutConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, লগআউট করুন", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        if (!uiState.isUserLoggedIn) {
            // DEDICATED IN-PAGE LOGIN & REGISTRATION INTERFACE
            item {
                DedicatedAuthSection(viewModel = viewModel)
            }
        } else {
            // LOGGED-IN AUTHOR PROFILE VIEW
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1D4ED8), Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.loggedInUserName.take(1),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.loggedInUserName,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Verified Writer",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = uiState.loggedInUserHandle,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.loggedInUserBio.ifBlank { "বাংলা সাহিত্যের চিরন্তন মুগ্ধ পাঠক ও স্বপ্নবাজ পরিবারের গর্বিত লেখক।" },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Follower & Following Stats
                        val followingCount = uiState.stories.filter { it.isFollowing }.map { it.authorHandle }.distinct().size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem(count = "${userStories.size}", label = "প্রকাশিত কাজ")
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            ProfileStatItem(count = uiState.writerStats.followersCount.ifBlank { "০" }, label = "অনুসারী (Followers)")
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            ProfileStatItem(count = "$followingCount", label = "অনুসরণ (Following)")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Current Role Badge and Switcher
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.userRole == UserRole.WRITER) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (uiState.userRole == UserRole.WRITER) "✍️ ভূমিকা: লেখক (লেখা ও প্রকাশ)" else "📖 ভূমিকা: পাঠক (পড়া ও রিভিউ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.userRole == UserRole.WRITER) Color(0xFF1E40AF) else Color(0xFF166534)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val nextRole = if (uiState.userRole == UserRole.WRITER) UserRole.READER else UserRole.WRITER
                                        viewModel.switchRole(nextRole)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (uiState.userRole == UserRole.WRITER) "পাঠক হন" else "লেখক হন ✍️",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action buttons: Tip Writer & Logout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openTipDialog(uiState.loggedInUserName) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("টিপ / সহায়তা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showLogoutConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("লগআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tab Row: Published | Writer Dashboard | Drafts
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ScrollableTabRow(
                    selectedTabIndex = selectedProfileTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    profileTabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedProfileTab == index,
                            onClick = { selectedProfileTab = index },
                            text = {
                                Text(
                                    text = tab,
                                    fontWeight = if (selectedProfileTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tab Content
            when (selectedProfileTab) {
                1 -> {
                    // Writer Dashboard
                    item {
                        WriterDashboardSection(uiState = uiState)
                    }
                }
                2 -> {
                    // Drafts
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "কোনো সংরক্ষিত খসড়া নেই",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "আপনার লেখার অসম্পূর্ণ খসড়াগুলো এখানে সংরক্ষিত থাকবে।",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Published Stories
                    if (userStories.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "এখনো কোনো লেখা প্রকাশিত হয়নি",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "'লিখুন' অপশন থেকে আপনার গল্প বা কবিতা প্রকাশ করুন।",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(userStories, key = { it.id }) { story ->
                            StoryFeedCard(
                                story = story,
                                onCardClick = { viewModel.openReader(story) },
                                onReactionClick = { reaction -> viewModel.toggleReaction(story.id, reaction) },
                                onBookmarkClick = { viewModel.toggleBookmark(story.id) },
                                onCommentsClick = { viewModel.openCommentsSheet(story) },
                                onFollowClick = { viewModel.toggleFollow(story.authorHandle) }
                            )
                        }
                    }
                }
            }

            // App footer
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "স্বপ্নবাজ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DedicatedAuthSection(viewModel: SwapnobajViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WRITER) }
    var authorName by remember { mutableStateOf("") }
    var penName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authorBio by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon & Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1D4ED8), Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isRegisterMode) "লেখক নিবন্ধন" else "লেখক অ্যাকাউন্ট লগইন",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isRegisterMode) "স্বপ্নবাজ পরিবারে যোগ দিন এবং আপনার সৃষ্টি সবার সাথে শেয়ার করুন।"
                       else "আপনার অ্যাকাউন্টে প্রবেশ করে গল্প-কবিতা প্রকাশ ও পরিচালনা করুন।",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Switch Tabs: Login | Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            isRegisterMode = false
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "লগইন",
                        fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            isRegisterMode = true
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "নতুন নিবন্ধন",
                        fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Role Selector Tab
            Text(
                text = "আপনার ভূমিকা (Role):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp)
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedRole = role }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (role == UserRole.WRITER) "✍️ ${role.banglaLabel} (লেখা ও প্রকাশ)" else "📖 ${role.banglaLabel} (পড়া ও রিভিউ)",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // One-Tap Quick Sign In with Google
            Button(
                onClick = {
                    viewModel.loginAuthor(
                        name = "সালমা জাহান",
                        penName = "salma_jahan",
                        bio = "বাংলা সাহিত্যের অনুরাগী লেখক ও স্বপ্নবাজের সম্মানিত সদস্য।"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA4335))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google দিয়ে এক ক্লিকে লগইন", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Text(
                    text = " অথবা নাম ও পাসওয়ার্ড ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Name Field
            OutlinedTextField(
                value = authorName,
                onValueChange = {
                    authorName = it
                    errorMessage = null
                },
                label = { Text(if (isRegisterMode) "লেখকের আসল নাম (Author Name)" else "লেখক নাম বা ইউজারনেম") },
                placeholder = { Text(if (isRegisterMode) "যেমন: কাজী নজরুল ইসলাম" else "আপনার নাম লিখুন") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isRegisterMode) {
                // Pen Name
                OutlinedTextField(
                    value = penName,
                    onValueChange = { penName = it },
                    label = { Text("কলম নাম / ইউজারনেম (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: swapno_kobi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Short Bio
                OutlinedTextField(
                    value = authorBio,
                    onValueChange = { authorBio = it },
                    label = { Text("ছোট লেখক পরিচিতি / বায়ো") },
                    placeholder = { Text("যেমন: কবিতা ও উপন্যাসের অনুরাগী কারিগর...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("গোপন পাসওয়ার্ড (Password)") },
                placeholder = { Text("কমপক্ষে ৪ অক্ষরের পাসওয়ার্ড") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Keep Logged In Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "স্থায়ীভাবে লগইন থাকবে — অ্যাপ বন্ধ করলেও লগইন মুছে যাবে না",
                    fontSize = 11.sp,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (authorName.isBlank()) {
                        errorMessage = "অনুগ্রহ করে আপনার নাম লিখুন"
                    } else if (password.length < 4) {
                        errorMessage = "পাসওয়ার্ড অন্তত ৪ অক্ষরের হতে হবে"
                    } else {
                        if (isRegisterMode) {
                            viewModel.loginAuthor(authorName, penName, authorBio, password, selectedRole)
                        } else {
                            val success = viewModel.verifyAndLogin(authorName, password)
                            if (!success) {
                                // If not previously saved, log in and save as new account automatically!
                                viewModel.loginAuthor(authorName, penName, authorBio, password, selectedRole)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isRegisterMode) "${selectedRole.banglaLabel} অ্যাকাউন্ট তৈরি ও লগইন করুন" else "${selectedRole.banglaLabel} অ্যাকাউন্টে প্রবেশ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reader Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            ) {
                Text(
                    text = "💡 সাধারণ পাঠকদের কোনো লগইন ছাড়াই সব গল্প, কবিতা ও উপন্যাস পড়ার উন্মুক্ত সুবিধা রয়েছে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WriterDashboardSection(uiState: SwapnobajUiState) {
    val userStories = uiState.stories.filter { it.authorHandle == uiState.loggedInUserHandle }
    val realReactions = userStories.sumOf { it.likesCount + it.lovesCount + it.firesCount }
    val realComments = userStories.sumOf { it.commentsCount }
    val realViews = if (userStories.isNotEmpty()) "${userStories.size * 3}" else "০"

    Column {
        Text(
            text = "লেখক অ্যানালিটিক্স ও পরিসংখ্যান",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 2x2 Analytics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsCard(
                icon = Icons.Default.Visibility,
                title = "মোট ভিউ",
                value = realViews,
                subtitle = "আপনার লেখার পাঠক",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                icon = Icons.Default.FavoriteBorder,
                title = "মোট রিঅ্যাকশন",
                value = "$realReactions",
                subtitle = "পাঠকের ভালোবাসা",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsCard(
                icon = Icons.Default.ChatBubbleOutline,
                title = "পাঠকের মন্তব্য",
                value = "$realComments",
                subtitle = "পাঠকের প্রতিক্রিয়া",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                icon = Icons.Default.MonetizationOn,
                title = "অর্জিত টিপস",
                value = uiState.writerStats.totalTips.ifBlank { "৳ ০" },
                subtitle = "সরাসরি লেখক সম্মাননা",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AnalyticsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF0D9488)
            )
        }
    }
}
