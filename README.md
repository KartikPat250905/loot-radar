# FreeGameRadar 🎮

**Kotlin Multiplatform (KMP) app for Android and Desktop**

FreeGameRadar is a **Kotlin Multiplatform** application built with **Compose Multiplatform**, sharing 70-80% of its code between **Android** and **Desktop** while providing native-feeling UIs on both targets.

---

## 🚀 Targets & Platforms

This project is a single KMP module with multiple targets:

- **Android:**
  - Target: `composeApp`
  - UI: Compose Multiplatform on Android
  - Auth: Firebase Android SDK
  - Features: Bottom navigation, native notifications

- **Desktop (JVM):**
  - Target: `composeApp[desktop]`
  - UI: Compose Multiplatform for Desktop
  - Auth: Firebase via REST
  - Features: Sidebar navigation, system tray integration

Both targets share the same business logic, data layer, and most UI code through the common KMP module, with platform-specific implementations provided via expect/actual patterns.

---

## 💡 What is FreeGameRadar?

FreeGameRadar consolidates free game deals from **Epic Games, Steam, GOG, and itch.io** into one unified app with real-time notifications. Major platforms collectively give away $6,000-$8,000 worth of games annually, but these time-limited offers are scattered and easy to miss.

---

## ✨ Features

### Core Functionality

#### 🎮 Multi-Platform Game Tracking
- Track free game giveaways from Epic Games Store, Steam, GOG, and itch.io in one unified interface
- Real-time updates when new free games become available
- Automatic deal expiration tracking with countdown timers
- Filter and sort games by platform, release date, or expiration time

#### 🔐 Cross-Platform Authentication
- Secure Firebase authentication supporting email/password and social login
- Synchronized user profile across Android and Desktop platforms
- Persistent login sessions with automatic token refresh
- Password reset and account management capabilities

#### 🔔 Smart Notifications
- Customizable push notifications for new free game alerts
- Platform-specific filters (get notified only for Epic or Steam deals)
- Content-type filtering (games, DLC, add-ons)
- Notification scheduling to avoid off-hours alerts
- Badge indicators showing unread deal counts

#### 📊 Personal Savings Dashboard
- Track all games you've claimed with automatic value calculation
- Visual charts displaying your total savings over time
- Monthly and yearly savings breakdown
- Game library statistics (total games claimed, platform distribution)

### Platform-Specific Features

#### Android 🤖
- **Bottom Navigation:** Intuitive tab-based navigation between Home, Browse, Favorites, and Profile
- **Material Design 3:** Modern Android UI with dynamic color theming and adaptive layouts
- **Native Notifications:** Android system notifications with rich media and action buttons
- **Swipe Gestures:** Swipe to mark games as claimed or add to favorites
- **Widget Support:** Home screen widget showing current free games at a glance
- **Tablet Optimization:** Responsive layouts for tablets and foldable devices

#### Desktop 🖥️
- **Sidebar Navigation:** Persistent sidebar for easy access to all sections
- **System Tray Integration:** Minimize to system tray with quick access menu
- **Multi-Window Support:** Open multiple windows to compare game deals side-by-side
- **Resizable Windows:** Flexible window sizing with saved layout preferences

### Technical Highlights

#### ⚡ Kotlin Multiplatform Architecture
- **70-80% Code Sharing:** Business logic, data layer, and UI components shared between platforms
- **Compose Multiplatform:** Single UI codebase with platform-specific optimizations
- **Expect/Actual Pattern:** Platform-specific implementations for authentication, notifications, and storage
- **Shared ViewModels:** Unified state management across Android and Desktop

#### 🔄 Data Synchronization
- Real-time Firebase Firestore sync for user data
- Offline-first architecture with local caching
- Conflict resolution for multi-device usage
- Background sync with WorkManager (Android) and scheduled tasks (Desktop)

#### 🚀 Performance Optimizations
- Lazy loading for large game lists
- Image caching and compression
- Efficient API polling with exponential backoff
- Memory-optimized data structures for smooth scrolling

---

## 🧰 Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17+**
- **Kotlin 1.9.20+**
- Internet connection (for Firebase and game deal APIs)

**✅ All API keys and Firebase configuration files are pre-configured. No additional setup required.**

---

## ▶️ Running the App in Android Studio

### 1. Clone the Repository

```bash
git clone https://github.com/KartikPat250905/loot-radar.git
cd loot-radar
```

### 2. Open in Android Studio
- Start Android Studio

- Select File → Open… and choose the root loot-radar folder

- Wait for Gradle sync to complete automatically (1-2 minutes on first open)

- If sync doesn't start automatically: File → Sync Project with Gradle Files

### 3. Run on Android
- Click the Run/Debug configuration dropdown in the top toolbar (left of the green Run ▶️ button)

- Select the Android configuration (shows composeApp with Android icon 🤖)

- Choose your target device from the device dropdown:

- Physical device: Enable USB debugging in Developer Options and connect via USB

- Emulator: Select an existing emulator or create one (API 24+ recommended)

- Click the Run ▶️ button (green play icon)

The Android app will build, install, and launch automatically on your selected device.

### 4. Run on Desktop
- Click the Run/Debug configuration dropdown in the top toolbar

- Select the Desktop configuration (shows composeApp Desktop with desktop icon 🖥️)

- Click the Run ▶️ button

A desktop window will open running the app natively on your current OS (Windows, macOS, or Linux).

### 🎯 Switching Between Targets
- To easily switch between Android and Desktop: run composeApp and composeApp[desktop] simultaneously.

- Use the Run configuration dropdown in the top toolbar

- Select your desired target: composeApp or composeApp[desktop]
