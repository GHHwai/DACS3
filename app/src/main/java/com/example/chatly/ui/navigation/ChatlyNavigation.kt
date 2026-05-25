package com.example.chatly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.ui.screen.*
import com.example.chatly.ui.admin.screen.*
import com.example.chatly.ui.admin.viewmodel.*
import com.example.chatly.ui.chat.AiChatViewModel
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.example.chatly.data.repository.admin.AdminRepository

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
    
    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AdminUsers : Screen("admin_users")
    object AdminSystemData : Screen("admin_system_data")
    object AdminDocuments : Screen("admin_documents")
    object AdminChatbot : Screen("admin_chatbot")
    object AdminChat : Screen("admin_chat")
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
        // Splash
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

        // Greeting
        composable(Screen.Greeting.route) {
            GreetingScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        // Login
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

        // Register
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

        // Main
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

        // Chat
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

        // User Detail
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

        // AI Chat (Firebase AI)
        composable(Screen.AiChat.route) {
            val aiViewModel: AiChatViewModel = viewModel(
                factory = AiChatViewModel.Factory(FirebaseAiChatRepository())
            )
            AiChatScreen(
                viewModel = aiViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onAdminClick = { navController.navigate(Screen.AdminDashboard.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Admin
        val adminRepository = AdminRepository()

        composable(Screen.AdminDashboard.route) {
            val adminDashboardViewModel: AdminDashboardViewModel = viewModel(
                factory = AdminDashboardViewModel.Factory(adminRepository)
            )
            AdminDashboardScreen(
                viewModel = adminDashboardViewModel,
                onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                onNavigateToSystemData = { navController.navigate(Screen.AdminSystemData.route) },
                onNavigateToDocuments = { navController.navigate(Screen.AdminDocuments.route) },
                onNavigateToChatbot = { navController.navigate(Screen.AdminChatbot.route) },
                onNavigateToChatSystem = { navController.navigate(Screen.AdminChat.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminUsers.route) {
            val adminUsersViewModel: AdminUsersViewModel = viewModel(
                factory = AdminUsersViewModel.Factory(adminRepository)
            )
            AdminUsersScreen(
                viewModel = adminUsersViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminSystemData.route) {
            val adminSystemDataViewModel: AdminSystemDataViewModel = viewModel(
                factory = AdminSystemDataViewModel.Factory(adminRepository)
            )
            AdminSystemDataScreen(
                viewModel = adminSystemDataViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDocuments.route) {
            val adminDocumentsViewModel: AdminDocumentsViewModel = viewModel(
                factory = AdminDocumentsViewModel.Factory(adminRepository)
            )
            AdminDocumentsScreen(
                viewModel = adminDocumentsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminChatbot.route) {
            val adminChatbotViewModel: AdminChatbotViewModel = viewModel(
                factory = AdminChatbotViewModel.Factory(adminRepository)
            )
            AdminChatbotScreen(
                viewModel = adminChatbotViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminChat.route) {
            val adminChatViewModel: AdminChatViewModel = viewModel(
                factory = AdminChatViewModel.Factory(adminRepository)
            )
            AdminChatScreen(
                viewModel = adminChatViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}