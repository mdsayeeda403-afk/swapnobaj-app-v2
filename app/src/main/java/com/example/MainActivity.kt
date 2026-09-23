package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SwapnobajViewModel
import com.example.ui.components.AuthModalDialog
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.ReviewsBottomSheet
import com.example.ui.components.TipWriterDialog
import com.example.ui.screens.BookshelfScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.WriteScreen
import com.example.ui.theme.MyApplicationTheme

enum class BottomNavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("হোম", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("অনুসন্ধান", Icons.Filled.Search, Icons.Outlined.Search),
    WRITE("লিখুন", Icons.Filled.EditNote, Icons.Outlined.EditNote),
    BOOKSHELF("বইয়ের তাক", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    PROFILE("প্রোফাইল", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SwapnobajApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapnobajApp(viewModel: SwapnobajViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBottomIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successSnackbarMessage) {
        uiState.successSnackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    // Handle Back Press when in Reader Mode
    if (uiState.currentReaderStory != null) {
        BackHandler {
            viewModel.closeReader()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (selectedBottomIndex == 0) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.swapnobaj_logo),
                                    contentDescription = "Swapnobaj Logo",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "স্বপ্নবাজ",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "বাংলার গল্প, কবিতা ও উপন্যাসের ঘর",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.openAuthModal() }) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Color(0xFF2563EB),
                                            modifier = Modifier.size(7.dp)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { selectedBottomIndex = 4 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.isUserLoggedIn && uiState.loggedInUserName.isNotBlank()) uiState.loggedInUserName.take(1) else "প",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    BottomNavDestination.values().forEachIndexed { index, destination ->
                        val isSelected = selectedBottomIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedBottomIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedBottomIndex) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigateToWrite = { selectedBottomIndex = 2 }
                    )
                    1 -> SearchScreen(uiState = uiState, viewModel = viewModel)
                    2 -> WriteScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onPublishSuccess = { selectedBottomIndex = 0 }
                    )
                    3 -> BookshelfScreen(uiState = uiState, viewModel = viewModel)
                    4 -> ProfileScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }

        // Full Screen Reader Mode Overlay with Smooth Transitions
        AnimatedVisibility(
            visible = uiState.currentReaderStory != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            uiState.currentReaderStory?.let { story ->
                ReaderScreen(
                    story = story,
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Comments Bottom Sheet
        if (uiState.isCommentsSheetOpen) {
            CommentsBottomSheet(uiState = uiState, viewModel = viewModel)
        }

        // Reviews Bottom Sheet
        if (uiState.isReviewModalOpen) {
            ReviewsBottomSheet(uiState = uiState, viewModel = viewModel)
        }

        // Tip Writer Dialog
        if (uiState.isTipDialogOpen && uiState.tipTargetAuthor != null) {
            TipWriterDialog(
                authorName = uiState.tipTargetAuthor!!,
                onDismiss = { viewModel.closeTipDialog() },
                onConfirm = { amount -> viewModel.confirmTip(amount) }
            )
        }

        // Authentication Modal Dialog
        if (uiState.isAuthModalOpen) {
            AuthModalDialog(
                isLoggedIn = uiState.isUserLoggedIn,
                userName = uiState.loggedInUserName,
                userRole = uiState.userRole,
                onDismiss = { viewModel.closeAuthModal() },
                onLoginAuthor = { name, penName, bio, role ->
                    viewModel.loginAuthor(name = name, penName = penName, bio = bio, role = role)
                },
                onSwitchRole = { role ->
                    viewModel.switchRole(role)
                },
                onLogoutAuthor = {
                    viewModel.logoutAuthor()
                }
            )
        }
    }
}
