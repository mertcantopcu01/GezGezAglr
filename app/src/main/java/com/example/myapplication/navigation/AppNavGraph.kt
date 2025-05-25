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
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val HOME          = "home"
    const val USER_PROFILE  = "user_profile"
    const val SEARCH        = "search"
    const val CREATE_POST   = "create_post"
    const val POST_DETAIL   = "post_detail"
    const val FOLLOWERS_LIST  = "followers_list"
    const val FOLLOWING_LIST  = "following_list"
}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        // — Home
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    AuthService.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    AuthService.getCurrentUser()?.uid
                        ?.let { navController.navigate("${Routes.USER_PROFILE}/$it") }
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onUserClick = { userId ->
                    navController.navigate("${Routes.USER_PROFILE}/$userId")
                },
                onPostClick = { postId ->
                    navController.navigate("${Routes.POST_DETAIL}/$postId")
                }
            )
        }


        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }


        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }


        composable(Routes.CREATE_POST) {
            CreatePostScreen(onPostCreated = { navController.popBackStack() })
        }


        composable(Routes.SEARCH) {
            SearchScreen(onUserSelected = { userId ->
                navController.navigate("${Routes.USER_PROFILE}/$userId")
            })
        }


        composable(
            route = "${Routes.USER_PROFILE}/{userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val userId = backStack.arguments!!.getString("userId")!!
            UserProfileScreen(
                userId            = userId,
                onPostClick       = { postId -> navController.navigate("${Routes.POST_DETAIL}/$postId") },
                onFollowersClick  = { navController.navigate("${Routes.FOLLOWERS_LIST}/$userId") },
                onFollowingClick  = { navController.navigate("${Routes.FOLLOWING_LIST}/$userId") },
                onCreatePost     = { navController.navigate(Routes.CREATE_POST) },
            )
        }

        // — Post Detail
        composable(
            route = "${Routes.POST_DETAIL}/{postId}",
            arguments = listOf(navArgument("postId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val postId = backStack.arguments!!.getString("postId")!!
            PostDetailScreen(postId = postId)
        }

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
