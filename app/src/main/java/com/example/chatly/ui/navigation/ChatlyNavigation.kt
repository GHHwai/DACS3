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
import com.example.chatly.ui.chat.GroupsScreen
import com.example.chatly.ui.chat.GroupChatScreen
import com.example.chatly.data.repository.GroupChatRepository
import com.example.chatly.ui.chat.GroupChatViewModel
import com.example.chatly.data.repository.admin.AdminRepository

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Schedule : Screen("schedule")

    object AddSchedule : Screen("add_schedule")

    object EditSchedule : Screen("edit_schedule/{scheduleId}") {
        fun createRoute(scheduleId: String): String {
            return "edit_schedule/$scheduleId"
        }
    }
    object AddExam : Screen("add_exam")

    object EditExam : Screen("edit_exam/{examId}") {
        fun createRoute(examId: String) = "edit_exam/$examId"
    }
    object Chat : Screen("chat/{userId}/{userName}/{userPhotoUrl}") {
        fun createRoute(userId: String, userName: String, userPhotoUrl: String): String {
            val encodedUrl = java.net.URLEncoder.encode(userPhotoUrl, "UTF-8")
            return "chat/$userId/$userName/$encodedUrl"
        }
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
    object Groups : Screen("groups")

    object GroupChat : Screen("group_chat/{groupId}/{groupName}") {
        fun createRoute(groupId: String, groupName: String) =
            "group_chat/$groupId/$groupName"
    }
}

@Composable
fun ChatlyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
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
                },
                onNavigationAdmin = {
                    navController.navigate(Screen.AdminDashboard.route) {
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
                onLoginSuccess = { role ->
                    val dest = if (role == "admin") Screen.AdminDashboard.route else Screen.Main.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    val dest = if (role == "admin") Screen.AdminDashboard.route else Screen.Main.route
                    navController.navigate(dest) {
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
                // --- ĐÃ ĐƯỢC GỘP ĐẦY ĐỦ CẢ NHÓM CHAT VÀ LỊCH HỌC Ở ĐÂY SẠCH LỖI ---
                onAiChatClick = { navController.navigate(Screen.AiChat.route) },
                onGroupChatClick = { navController.navigate(Screen.Groups.route) },
                onScheduleClick = { navController.navigate(Screen.Schedule.route) },
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
            val userPhotoUrl = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("userPhotoUrl") ?: "none",
                "UTF-8"
            )
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

        // Schedule
        composable(Screen.Schedule.route) {
            ScheduleScreen(
                onAddStudyClick = { navController.navigate(Screen.AddSchedule.route) },
                onAddExamClick = { navController.navigate(Screen.AddExam.route) },
                onEditStudyClick = { id -> navController.navigate(Screen.EditSchedule.createRoute(id)) },
                onEditExamClick = { id -> navController.navigate(Screen.EditExam.createRoute(id)) }
            )
        }

        composable(Screen.AddSchedule.route) {
            AddScheduleScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditSchedule.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
            EditScheduleScreen(
                scheduleId = scheduleId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddExam.route) {
            AddExamScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditExam.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            EditExamScreen(
                examId = examId,
                onBackClick = { navController.popBackStack() }
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
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
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
                viewModel = groupChatViewModel,
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
                viewModel = groupChatViewModel,
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
                onLogout = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
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