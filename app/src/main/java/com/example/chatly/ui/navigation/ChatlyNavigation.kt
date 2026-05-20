package com.example.chatly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chatly.ui.screen.*
import com.example.chatly.viewmodel.AiChatViewModel
import com.example.chatly.ui.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.remote.GeminiApiService
import com.example.chatly.data.repository.AiChatRepository

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Chat : Screen("chat/{userId}/{userName}/{userPhotoUrl}") {
        fun createRoute(userId: String, userName: String, userPhotoUrl: String) = 
            "chat/$userId/$userName/$userPhotoUrl"
    }
    object AiChat : Screen("ai_chat")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
}

@Composable
fun ChatlyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigationNext = {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigationMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Greeting.route) {
            GreetingScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onUserClick = { user ->
                    navController.navigate(
                        Screen.Chat.createRoute(user.uid, user.displayName ?: "", user.photoUrl ?: "none")
                    )
                },
                onAiChatClick = { navController.navigate(Screen.AiChat.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("userPhotoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userPhotoUrl = backStackEntry.arguments?.getString("userPhotoUrl") ?: ""
            ChatScreen(
                userId = userId,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                onBackClick = { navController.popBackStack() },
                onUserClick = { 
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AiChat.route) {
            val aiViewModel: AiChatViewModel = viewModel(
                factory = AiChatViewModel.Factory(AiChatRepository(GeminiApiService.create()))
            )
            AiChatScreen(
                viewModel = aiViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
