package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.screens.*

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val USER_PROFILE = "user_profile"
    const val CREATE_POST = "create_post"
    const val POST_DETAIL = "post_detail"


}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
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

        composable(Routes.PROFILE) {
            ProfileScreen(
                onCreatePost = { navController.navigate(Routes.CREATE_POST) },
                onPostClick  = { postId -> navController.navigate("${Routes.POST_DETAIL}/$postId") }
            )
        }

        composable(
            route = "${Routes.POST_DETAIL}/{postId}",
            arguments = listOf(navArgument("postId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val postId = backStack.arguments!!.getString("postId")!!
            PostDetailScreen(postId = postId)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CREATE_POST) {
            CreatePostScreen(onPostCreated = {
                navController.popBackStack()
            })
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
                nullable = false
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments!!.getString("userId")!!
            UserProfileScreen(
                userId = userId,
                onPostClick = { postId ->
                    navController.navigate("${Routes.POST_DETAIL}/$postId")
                }
            )
        }
    }
}
