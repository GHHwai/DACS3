package com.example.chatly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.ui.screen.*
import com.example.chatly.viewmodel.AiChatViewModel
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.example.chatly.ui.chat.GroupsScreen
import com.example.chatly.ui.chat.GroupChatScreen
import com.example.chatly.data.repository.GroupChatRepository
import com.example.chatly.ui.chat.GroupChatViewModel

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
    object Groups : Screen("groups")

    object GroupChat :
        Screen("group_chat/{groupId}/{groupName}") {

        fun createRoute(
            groupId: String,
            groupName: String
        ) =
            "group_chat/$groupId/$groupName"
    }
}

@Composable
fun ChatlyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    // Khởi tạo repository chuẩn từ Class bạn vừa viết
    val groupRepository = GroupChatRepository()

    val groupChatViewModel: GroupChatViewModel = viewModel(
        factory = GroupChatViewModel.Factory(groupRepository)
    )
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
                        Screen.Chat.createRoute(
                            user.uid,
                            user.displayName ?: "",
                            user.photoUrl ?: "none"
                        )
                    )
                },
                onAiChatClick = { navController.navigate(Screen.AiChat.route) },
                onGroupChatClick = { navController.navigate(Screen.Groups.route) },
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
                onBackClick = { navController.popBackStack() }
            )
        }

        // Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Groups
        composable(Screen.Groups.route) {
            GroupsScreen(
                viewModel = groupChatViewModel, // Truyền viewModel vào
                onGroupClick = { group ->
                    navController.navigate(
                        Screen.GroupChat.createRoute(group.id, group.groupName)
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Group Chat
        composable(
            route = Screen.GroupChat.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""

            GroupChatScreen(
                groupId = groupId,
                groupName = groupName,
                viewModel = groupChatViewModel, // Truyền viewModel vào
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
