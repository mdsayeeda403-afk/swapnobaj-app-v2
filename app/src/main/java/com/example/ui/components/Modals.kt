package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Comment
import com.example.data.StoryReview
import com.example.data.UserRole
import com.example.ui.SwapnobajUiState
import com.example.ui.SwapnobajViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel
) {
    var newCommentText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeCommentsSheet() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "পাঠকের প্রতিক্রিয়া (${uiState.currentComments.size}টি মন্তব্য)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    uiState.activeCommentsStory?.let { s ->
                        Text(
                            text = s.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = { viewModel.closeCommentsSheet() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comments List
            if (uiState.currentComments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "এখনো কোনো মন্তব্য করা হয়নি।",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রথম পাঠক হিসেবে আপনার অনুভূতি জানান! ✍️",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.currentComments, key = { it.id }) { comment ->
                        CommentItemView(comment = comment)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Comment Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("আপনার মূল্যবান মতামত লিখুন...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            viewModel.addComment(newCommentText)
                            newCommentText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItemView(comment: Comment) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.authorName.take(1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = comment.timeAgo,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = comment.text,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        // Nested Replies
        if (comment.replies.isNotEmpty()) {
            comment.replies.forEach { reply ->
                Row(
                    modifier = Modifier
                        .padding(start = 28.dp, top = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.SubdirectoryArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reply.authorName.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = reply.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0284C7)
                        )
                        Text(
                            text = reply.text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TipWriterDialog(
    authorName: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(100) }
    var note by remember { mutableStateOf("") }
    val amounts = listOf(50, 100, 250, 500)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "লেখককে টিপ / উপহার দিন", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "$authorName-এর সাহিত্যকর্মের প্রতি কৃতজ্ঞতা ও ভালোবাসা প্রকাশ করুন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amounts.forEach { amt ->
                        val isSelected = selectedAmount == amt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0xFF1D4ED8)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedAmount = amt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "৳$amt",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("লেখকের উদ্দেশ্যে ছোট বার্তা (ঐচ্ছিক)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "পেমেন্ট মেথড: bKash / Nagad গেটওয়ে",
                    fontSize = 11.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedAmount) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("৳$selectedAmount প্রদান করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun AuthModalDialog(
    isLoggedIn: Boolean,
    userName: String,
    userRole: UserRole = UserRole.READER,
    onDismiss: () -> Unit,
    onLoginAuthor: (name: String, penName: String, bio: String, role: UserRole) -> Unit,
    onSwitchRole: (UserRole) -> Unit = {},
    onLogoutAuthor: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WRITER) }
    var authorName by remember { mutableStateOf("") }
    var penName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authorBio by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isLoggedIn) "প্রোফাইল ও সেটিংস" 
                           else if (isRegisterMode) "নতুন অ্যাকাউন্ট তৈরি (Sign Up)" 
                           else "অ্যাকাউন্টে প্রবেশ (লগইন)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLoggedIn) {
                    // Logged In Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "বর্তমানে লগইন আছেন:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (userRole == UserRole.WRITER) Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (userRole == UserRole.WRITER) "✍️ ভেরিফাইড লেখক (গল্প লেখার অধিকারপ্রাপ্ত)" else "📖 সম্মানিত পাঠক (পড়া ও রিভিউ দেওয়ার অধিকারপ্রাপ্ত)",
                                    fontSize = 11.sp,
                                    color = if (userRole == UserRole.WRITER) Color(0xFFB45309) else Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            val target = if (userRole == UserRole.WRITER) UserRole.READER else UserRole.WRITER
                            onSwitchRole(target)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (userRole == UserRole.WRITER) "পাঠক মোডে স্যুইচ করুন 📖" else "লেখক মোডে পরিবর্তন করুন (লেখক হন ✍️)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onLogoutAuthor()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("লগআউট করুন", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Role Selector Tabs
                    Text(
                        text = "আপনার ভূমিকা নির্বাচন করুন:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Writer Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selectedRole == UserRole.WRITER) 2.dp else 1.dp,
                                    color = if (selectedRole == UserRole.WRITER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(if (selectedRole == UserRole.WRITER) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .clickable { selectedRole = UserRole.WRITER }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✍️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("লেখক", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == UserRole.WRITER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text("গল্প-কবিতা লিখুন", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Reader Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selectedRole == UserRole.READER) 2.dp else 1.dp,
                                    color = if (selectedRole == UserRole.READER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(if (selectedRole == UserRole.READER) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .clickable { selectedRole = UserRole.READER }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📖", fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("পাঠক", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == UserRole.READER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text("পড়ুন ও রিভিউ দিন", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google One-Tap Sign In
                    Button(
                        onClick = {
                            val defaultName = if (selectedRole == UserRole.WRITER) "আহমেদ তানভীর" else "তানভীর আহমেদ"
                            val defaultHandle = if (selectedRole == UserRole.WRITER) "tanveer_writes" else "tanveer_reader"
                            val defaultBio = if (selectedRole == UserRole.WRITER) "গল্প ও কবিতা লিখি। স্বপ্নবাজের গর্বিত লেখক।" else "স্বপ্নবাজের একনিষ্ঠ সাহিত্যপ্রেমী পাঠক।"
                            onLoginAuthor(defaultName, defaultHandle, defaultBio, selectedRole)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA4335))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google দিয়ে ${if (selectedRole == UserRole.WRITER) "লেখক" else "পাঠক"} লগইন", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Text(
                            text = " অথবা নাম ও পাসওয়ার্ড দিয়ে ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name Field
                    OutlinedTextField(
                        value = authorName,
                        onValueChange = {
                            authorName = it
                            errorMessage = null
                        },
                        label = { Text(if (selectedRole == UserRole.WRITER) "লেখকের নাম (Author Name)" else "পাঠকের নাম (Reader Name)") },
                        placeholder = { Text(if (selectedRole == UserRole.WRITER) "যেমন: কাজী নজরুল ইসলাম" else "যেমন: আরিফুল ইসলাম") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isRegisterMode) {
                        // Pen Name / Handle Field
                        OutlinedTextField(
                            value = penName,
                            onValueChange = { penName = it },
                            label = { Text("কলম নাম বা ইউজারনেম (ঐচ্ছিক)") },
                            placeholder = { Text("যেমন: swapno_kobi") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Short Bio Field
                        OutlinedTextField(
                            value = authorBio,
                            onValueChange = { authorBio = it },
                            label = { Text("ছোট লেখক পরিচিতি / বায়ো") },
                            placeholder = { Text("যেমন: গল্প এবং উপন্যাসের মুগ্ধ কারিগর...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 2
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
                        label = { Text("পাসওয়ার্ড (Password)") },
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
                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (authorName.isBlank()) {
                                errorMessage = "অনুগ্রহ করে আপনার নাম লিখুন"
                            } else if (password.length < 4) {
                                errorMessage = "পাসওয়ার্ড অন্তত ৪ অক্ষরের হতে হবে"
                            } else {
                                onLoginAuthor(authorName, penName, authorBio, selectedRole)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (isRegisterMode) "${selectedRole.banglaLabel} হিসেবে অ্যাকাউন্ট খুলুন" else "${selectedRole.banglaLabel} অ্যাকাউন্টে প্রবেশ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle between Login & Register
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isRegisterMode) "ইতিমধ্যে অ্যাকাউন্ট আছে? লগইন করুন" 
                                   else "নতুন লেখক? এখানে ফ্রি অ্যাকাউন্ট খুলুন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsBottomSheet(
    uiState: SwapnobajUiState,
    viewModel: SwapnobajViewModel
) {
    val story = uiState.activeReviewStory ?: uiState.currentReaderStory
    var selectedRating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeReviewsSheet() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header with Story details & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "পাঠক রিভিউ ও রেটিং (${uiState.currentReviews.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    story?.let { s ->
                        Text(
                            text = "“${s.title}” — ${s.authorName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = { viewModel.closeReviewsSheet() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rating Overview Bar
            story?.let { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (s.reviewsCount > 0) String.format("%.1f", s.averageRating) else "০.০",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row {
                                    repeat(5) { starIndex ->
                                        val avgRatingInt = if (s.reviewsCount > 0) s.averageRating.toInt() else 0
                                        val isFilled = starIndex < avgRatingInt
                                        Text(
                                            text = if (isFilled) "★" else "☆",
                                            color = Color(0xFFD97706),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Text(
                                    text = if (s.reviewsCount > 0) "${s.reviewsCount} জন পাঠকের সম্মিলিত মূল্যায়ন" else "এখনো কোনো রিভিউ দেওয়া হয়নি",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD97706))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (s.reviewsCount > 0) "জনপ্রিয়" else "নতুন",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reviews List + Write Review Form inside scrollable section
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Write a review card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "গল্প, কবিতা বা উপন্যাসটি কেমন লেগেছে? রিভিউ দিন:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive 5-Star Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..5).forEach { star ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (star <= selectedRating) Color(0xFFFEF3C7) else Color.Transparent)
                                            .clickable { selectedRating = star },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (star <= selectedRating) "★" else "☆",
                                            fontSize = 22.sp,
                                            color = if (star <= selectedRating) Color(0xFFD97706) else Color.Gray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                val ratingText = when (selectedRating) {
                                    5 -> "অসাধারণ সাহিত্য! 🌟"
                                    4 -> "খুব সুন্দর লেগেছে 👍"
                                    3 -> "ভালো হয়েছে 😊"
                                    2 -> "মোটামুটি 😐"
                                    else -> "উন্নতি প্রয়োজন 🤔"
                                }
                                Text(
                                    text = ratingText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick tag chips
                            val quickTags = listOf(
                                "গল্পের প্লট চমৎকার 👏",
                                "ভাষাশৈলী অসাধারণ ✍️",
                                "চরিত্রায়ন নিখুঁত 🎭",
                                "হৃদয়স্পর্শী সমাপ্তি 💖",
                                "রহস্যে টানটান 🔍"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickTags.take(3).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                reviewText = if (reviewText.isBlank()) tag else "$reviewText $tag"
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                placeholder = {
                                    Text("গল্পটি আপনার কেমন লেগেছে? অনুভূতির বিস্তারিত রিভিউ লিখুন...", fontSize = 12.sp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (reviewText.isNotBlank() && story != null) {
                                        viewModel.addReview(
                                            storyId = story.id,
                                            rating = selectedRating,
                                            reviewText = reviewText
                                        )
                                        reviewText = ""
                                    }
                                },
                                enabled = reviewText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রিভিউ ও রেটিং জমা দিন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Reviews List Header
                item {
                    Text(
                        text = "পাঠকদের প্রকাশিত রিভিউ (${uiState.currentReviews.size}টি):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.currentReviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨ এখনো কোনো রিভিউ জমা পড়েনি", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("আপনিই প্রথম পাঠক হিসেবে ওপরের বক্সে লিখে রিভিউ দিন!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else {
                    items(uiState.currentReviews, key = { it.id }) { review ->
                        StoryReviewItemView(review = review)
                    }
                }
            }
        }
    }
}

@Composable
fun StoryReviewItemView(review: StoryReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.reviewerName.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.reviewerName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.1f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = review.reviewerRole,
                                    fontSize = 9.sp,
                                    color = Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = review.timeAgo,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stars display
                Row {
                    repeat(5) { index ->
                        Text(
                            text = if (index < review.rating) "★" else "☆",
                            color = Color(0xFFD97706),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.reviewText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}
