# FreeGameRadar 🎮

**Kotlin Multiplatform (KMP) app for Android and Desktop**

FreeGameRadar is a **Kotlin Multiplatform** application built with **Compose Multiplatform**, sharing 70-80% of its code between **Android** and **Desktop** while providing native-feeling UIs on both targets.

---

## 🚀 Targets & Platforms

This project is a single KMP module with multiple targets:

- **Android:**
    - Target: composeApp
    - UI: Compose Multiplatform on Android
    - Auth: Firebase Android SDK
    - Features: Bottom navigation, native notifications

- **Desktop (JVM):**
    - Target: composeApp[desktop]
    - UI: Compose Multiplatform for Desktop
    - Auth: Firebase via REST
    - Features: Sidebar navigation, system tray integration

Both targets share the same business logic, data layer, and most UI code through the common KMP module, with platform-specific implementations provided via expect/actual patterns [web:6].

---

## 💡 What is FreeGameRadar?

FreeGameRadar consolidates free game deals from **Epic Games, Steam, GOG, and itch.io** into one unified app with real-time notifications. Major platforms collectively give away $6,000-$8,000 worth of games annually, but these time-limited offers are scattered and easy to miss.

**Key Features:**

- 🔐 Firebase Authentication (cross-platform)
- 🔔 Real-time notifications (platform & content-type filtered)
- 🎮 Multi-platform tracking (Epic/Steam/GOG/itch.io)
- 📊 Savings dashboard (track claimed games & total value)
- 🖥️ True cross-platform (native Android + Desktop from one codebase)
- ⚡ 70% code sharing via Kotlin Multiplatform

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
- To easily switch between Android and Desktop:

- Use the Run configuration dropdown in the top toolbar

- Select your desired target: composeApp or composeApp[desktop]