# Chatly – Modern Android Chat Application

**Chatly** is a production-ready Android chat application demonstrating modern development practices using **Jetpack Compose**, **MVVM architecture**, and **Firebase**. This project serves as a comprehensive example of building a scalable, real-time communication platform with a unified design system.

---

## 🚀 Key Features

- **Real-time Messaging**: One-on-one and Group chats powered by Cloud Firestore.
- **AI-Powered Chat**: Integrated Gemini AI for intelligent assistance.
- **Schedule Management**: Track study sessions and exam dates with a built-in calendar system.
- **Unified UI/UX**: A consistent design language across User and Admin modules using shared components.
- **Admin Dashboard**: Comprehensive tools for managing users, system data, and chatbot configurations.
- **Modern Tech Stack**: Fully built with Jetpack Compose, Coroutines, and StateFlow.

---

## 🏗️ Architecture & Tech Stack

Chatly follows the **MVVM (Model-View-ViewModel)** architecture and Clean Architecture principles to ensure maintainability and testability.

### Tech Stack
| Layer | Technology |
|---|---|
| **UI Framework** | Jetpack Compose (100%) |
| **Navigation** | Compose Navigation with Type-safe arguments |
| **Asynchronous** | Kotlin Coroutines & StateFlow |
| **Local Database** | Room (Offline caching & Schedule storage) |
| **Cloud Services** | Firebase (Auth, Firestore, Vertex AI for Gemini) |
| **Image Handling** | Coil & Cloudinary |
| **Dependency Injection** | ViewModel Factories (Standardized) |
| **Networking** | OkHttp & Retrofit |

### Repository Layer
Standardized naming and responsibility:
- `ChatRepository`: Manages 1-on-1 messaging logic.
- `GroupChatRepository`: Handles group creation and multi-user messaging.
- `FirebaseAiChatRepository`: Integration with Gemini AI.
- `ProfileRepository`: Manages user profiles and Cloudinary uploads.
- `ScheduleRepository`: Handles local and remote schedule data.
- `AdminRepository`: Centralized logic for administrative tasks.

---

## 🎨 Design System

We utilize a centralized UI library in `ui/components/CommonComponents.kt` to ensure a cohesive look and feel:
- **ChatlyTopAppBar**: Standardized header with Auto-Mirrored back icons for RTL support.
- **ChatlyButton & ChatlyTextField**: Thematic input and action elements.
- **ChatBubble**: Unified message visualization across all chat types.
- **ProfileInfoItem**: Consistent data display for user profiles.

---

## 📂 Project Structure

```text
app/src/main/java/com/example/chatly/
├── data/
│   ├── model/          # Domain data classes
│   ├── repository/     # Data source abstraction (Local & Remote)
│   └── local/          # Room DB, DAOs, and Type Converters
├── ui/
│   ├── components/     # Shared UI library (CommonComponents.kt)
│   ├── navigation/     # NavHost and Route definitions
│   ├── screen/         # User module screens (Compose)
│   ├── viewmodel/      # User module ViewModels
│   └── admin/          # Admin-specific UI and Logic
└── utils/              # Extensions, Formatters, and Constants
```

---

## 🛠️ Getting Started

### 1. Prerequisites
- Android Studio Ladybug or newer.
- A Firebase project.
- A Cloudinary account for image hosting.
- A Gemini API Key (Vertex AI).

### 2. Configuration
Create a `local.properties` file in the root directory:
```properties
GEMINI_API_KEY="your_api_key_here"
CLOUDINARY_CLOUD_NAME="your_cloud_name"
CLOUDINARY_UNSIGNED_PRESET="your_preset"
```

### 3. Firebase Setup
1. Add `google-services.json` to the `app/` directory.
2. Enable Email/Password Auth in Firebase Console.
3. Enable Cloud Firestore and Storage.

---

## 🛡️ Firestore Security & Data Flow
- **Messages**: Encrypted at rest, restricted by user-specific rules.
- **Groups**: Access controlled via membership lists.
- **Sync**: Room acts as a single source of truth for the UI, with Firestore providing real-time synchronization.

---

## 📄 License
Licensed under the MIT License. See `LICENSE` for details.

**Developed with ❤️ by the Chatly Team**
