@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.AppThemeColors

/* ---------- Bottom Tabs ---------- */
private sealed class Tab(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home   : Tab("home_tab",   "Ana Sayfa", { Icon(Icons.Default.Home, null) })
    object Search : Tab("search_tab", "Ara",       { Icon(Icons.Default.Search, null) })
    object Add    : Tab("add_tab",    "Ekle",      { Icon(Icons.Default.Add, null) })
    object Profile: Tab("profile_tab","Profil",    { Icon(Icons.Default.Person, null) })
}

/* ---------- Entry from Routes.HOME ---------- */
@Composable
fun MainTabs(
    onOpenPost: (String) -> Unit,
    onOpenUser: (String) -> Unit
) {
    val nav = rememberNavController()
    val tabs = listOf(Tab.Home, Tab.Search, Tab.Add, Tab.Profile)
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    AppBackground {
        Scaffold(
            // ✅ TopBar sadece Home tabındayken
            topBar = {
                if (currentRoute == Tab.Home.route) {
                    TopAppBar(
                        title = {
                            Text(
                                "Gezgin",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        actions = {
                            IconButton(onClick = { nav.navigate(Tab.Search.route) }) {
                                Icon(Icons.Default.Search, contentDescription = "Ara")
                            }
                            IconButton(onClick = { nav.navigate(Tab.Profile.route) }) {
                                Icon(Icons.Default.Person, contentDescription = "Profil")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AppThemeColors.extra.topBar,
                            titleContentColor = AppThemeColors.extra.onTopBar,
                            actionIconContentColor = AppThemeColors.extra.onTopBar,
                            navigationIconContentColor = AppThemeColors.extra.onTopBar
                        )
                    )
                }
            },
            bottomBar = { BottomBar(nav = nav, items = tabs) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Tab.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Tab.Home.route) {
                    HomeFeed(
                        onUserClick = onOpenUser,
                        onPostClick = onOpenPost
                    )
                }
                composable(Tab.Search.route) {
                    // Bu ekran kendi içinde sadece "Ara" başlığını ve arama kutusunu çiziyor
                    SearchTabScreen(
                        onUserSelected = { userId -> onOpenUser(userId) }
                    )
                }
                composable(Tab.Add.route) {
                    // CreatePost kendi üst barını ("Yeni Gönderi") çiziyor; "Gezgin" görünmez
                    CreatePostScreen(
                        onPostCreated = {
                            nav.navigate(Tab.Home.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onBack = {
                            nav.navigate(Tab.Home.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Tab.Profile.route) {
                    val me = AuthService.getCurrentUser()?.uid
                    if (me == null) {
                        PlaceholderScreen("Giriş yapmalısın")
                    } else {
                        UserProfileScreen(
                            userId = me,
                            onCreatePost = {                      // Profildeki "Gönderi Paylaş" → Add tab
                                nav.navigate(Tab.Add.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onPostClick = { postId -> onOpenPost(postId) },   // Gönderi detayına git
                            onFollowersClick = { /* dış grafikteki route burada yok; isteğe göre ekleyebilirsin */ _ -> },
                            onFollowingClick = { /* aynı şekilde */ _ -> },
                            onLogout = { nav.navigate(Tab.Home.route) },      // İstersen başka davranış ekle
                            onEditProfile = { /* EditProfile route’u iç grafiğe eklemediğimiz için boş */ },
                            onBack = null
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, items: List<Tab>) {
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = tab.icon,
                label = { Text(tab.label) }
            )
        }
    }
}

// -------------------------------
// Home Feed — tema-duyarlı kartlar
// -------------------------------
@Composable
private fun HomeFeed(
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val currentUid = AuthService.getCurrentUser()?.uid
    var feedPosts by remember { mutableStateOf(emptyList<Post>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val authorCache = remember { mutableStateMapOf<String, UserProfile?>() }

    LaunchedEffect(currentUid) {
        if (currentUid == null) {
            isLoading = false
            errorMsg = "Oturum açılmamış."
            return@LaunchedEffect
        }
        FirestoreService.getFollowingIds(currentUid) { followingIds ->
            FirestoreService.getPostsByUserIds(followingIds) { posts ->
                feedPosts = posts
                isLoading = false
                posts.map { it.uid }.distinct().forEach { uid ->
                    if (!authorCache.containsKey(uid)) {
                        FirestoreService.getUserProfile(uid) { p -> authorCache[uid] = p }
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = cs.primary
            )

            errorMsg != null -> Text(
                text = errorMsg!!,
                color = cs.onBackground,
                modifier = Modifier.align(Alignment.Center),
                fontFamily = FontFamily.Monospace
            )

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.background),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(feedPosts, key = { it.postId }) { post ->
                        val author = authorCache[post.uid]
                        PostCard(
                            post = post,
                            author = author,
                            onUserClick = onUserClick,
                            onPostClick = onPostClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    author: UserProfile?,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Tema-duyarlı renkler
    val cardColor     = if (isDark) Color(0xFF111B2A) else Color.White
    val borderColor   = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE5E7EB)
    val textPrimary   = if (isDark) Color(0xFFEFF4FF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFF9AA3B2) else Color(0xFF6B7280)
    val placeholderBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0x14111827) // AA RR GG BB
    val starGold      = Color(0xFFFFD54F)

    val ctx = LocalContext.current

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.postId) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.padding(14.dp)) {

            // Üst: avatar + kullanıcı adı
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (author?.profileImageUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(author.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onUserClick(author.uid) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(textSecondary.copy(alpha = 0.25f))
                            .clickable { author?.uid?.let(onUserClick) }
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = author?.username ?: "Kullanıcı",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Başlık
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textPrimary,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            // ⭐ puan (ikon) + konum (ikon)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Puan",
                    tint = starGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${post.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textPrimary,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.width(14.dp))

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Konum",
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = post.location?.takeIf { it.isNotBlank() } ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Görsel
            post.photoUrl?.let { url ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    loading = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(placeholderBg)
                        )
                    },
                    error = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(placeholderBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Görsel yok", color = textSecondary)
                        }
                    }
                )
            }
        }
    }
}

// -------------------------------
// Yer tutucu ekranlar (diğer tablar için)
// -------------------------------
@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}
