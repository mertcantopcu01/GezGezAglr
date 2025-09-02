package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.screens.*

object Routes {
    const val LOGIN          = "login"
    const val REGISTER       = "register"
    const val HOME           = "home"
    const val USER_PROFILE   = "user_profile"
    const val CREATE_POST    = "create_post"
    const val POST_DETAIL    = "post_detail"
    const val FOLLOWERS_LIST = "followers_list"
    const val FOLLOWING_LIST = "following_list"
    const val EDIT_PROFILE   = "edit_profile"
}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        // HOME (bottom tab'ler içeride)
        composable(Routes.HOME) {
            MainTabs(
                onOpenPost = { postId ->
                    navController.navigate("${Routes.POST_DETAIL}/$postId")
                },
                onOpenUser = { userId ->
                    navController.navigate("${Routes.USER_PROFILE}/$userId")
                }
            )
        }

        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        // REGISTER
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // CREATE POST
        composable(Routes.CREATE_POST) {
            CreatePostScreen(
                onPostCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // USER PROFILE
        composable(
            route = "${Routes.USER_PROFILE}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val userId = backStack.arguments!!.getString("userId")!!
            UserProfileScreen(
                userId           = userId,
                onPostClick      = { postId -> navController.navigate("${Routes.POST_DETAIL}/$postId") },
                onFollowersClick = { uid -> navController.navigate("${Routes.FOLLOWERS_LIST}/$uid") },
                onFollowingClick = { uid -> navController.navigate("${Routes.FOLLOWING_LIST}/$uid") },
                onCreatePost     = { navController.navigate(Routes.CREATE_POST) },
                onLogout         = {
                    AuthService.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onEditProfile    = { navController.navigate(Routes.EDIT_PROFILE) },
                onBack           = { navController.popBackStack() }
            )
        }

        // EDIT PROFILE
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        // POST DETAIL
        composable(
            route = "${Routes.POST_DETAIL}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStack ->
            val postId = backStack.arguments!!.getString("postId")!!
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }

        // FOLLOWERS
        composable(
            route = "${Routes.FOLLOWERS_LIST}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val userId = back.arguments!!.getString("userId")!!
            FollowersScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onUserClick = { clickedUid ->
                    navController.navigate("${Routes.USER_PROFILE}/$clickedUid")
                }
            )
        }

        // FOLLOWING
        composable(
            route = "${Routes.FOLLOWING_LIST}/{userId}",
            arguments = listOf(navArgument("userId"){ type = NavType.StringType })
        ) { back ->
            val userId = back.arguments!!.getString("userId")!!
            FollowingScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onUserClick = { clickedUid ->
                    navController.navigate("${Routes.USER_PROFILE}/$clickedUid")
                }
            )
        }
    }
}
