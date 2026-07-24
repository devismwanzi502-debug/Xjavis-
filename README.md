# PhonePilot AI Agent 🤖📱

**PhonePilot** is a modern, cyberpunk-styled Android AI Agent application built with Kotlin and Jetpack Compose. It allows users to control phone settings, trigger smart routines, auto-reply to notifications using Gemini AI or custom text, and speak commands via voice.

---

## ⚡ GitHub Deployment & Automatic APK Generation

This repository is fully configured for automated GitHub CI/CD build pipelines using **GitHub Actions**.

### How to Get Your APK File:

1. **Push to GitHub**:
   - Push this codebase to your GitHub repository (`main` or `master` branch).
2. **Automated Build**:
   - GitHub Actions will automatically start building your APK via the `.github/workflows/build-apk.yml` workflow.
3. **Download your APK**:
   - Navigate to the **Actions** tab in your GitHub repository.
   - Click on the latest workflow run (**Build Android APK**).
   - Under the **Artifacts** section at the bottom of the summary page, download **PhonePilot-APK** (`app-debug.apk`).
   - Install the APK directly on your Android smartphone!

4. **Release Tags (Optional)**:
   - Create a Git tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`).
   - GitHub Actions will automatically create a GitHub Release with the APK attached.

---

## 🔑 Gemini AI API Key Setup

PhonePilot offers **dual runtime fallback** for Gemini AI integration:

### Option 1: In-App Settings Screen (Recommended for APK users)
- Open the app, go to **Settings & Security**.
- Paste your Gemini API key (`AIzaSy...`) into the **Gemini AI API Key** input box and tap **Save Key**.
- The key is securely saved locally in encrypted app preferences.

### Option 2: GitHub Repository Secrets (For CI automated build)
- Go to your GitHub repository -> **Settings** -> **Secrets and variables** -> **Actions**.
- Add a Repository Secret named `GEMINI_API_KEY` with your API key value.
- When GitHub Actions builds the APK, it will automatically inject this key into `BuildConfig`.

---

## 🚀 Key Features

- 🤖 **Chatbot AI Auto-Replies**: Intelligently process incoming app notifications and reply with context-aware Gemini AI responses.
- 💬 **Specific Text Auto-Replies**: Set fixed custom auto-replies for specific contacts or messaging apps.
- 🎙️ **Voice Assistant HUD**: Real-time speech transcription and Text-To-Speech execution.
- ⚡ **Automations Engine**: Define automated `WHEN [Trigger] -> THEN [Action]` rules for notifications, charger plugging, or voice phrases.
- 🔐 **Guardrails & Security**: Dynamic safety switches to confirm calls or messages before execution.

---

## 🛠️ Local Development & Build

- **IDE**: Android Studio Ladybug / Jellyfish (or newer)
- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 36 (Android 15+)
- **Build System**: Gradle with Kotlin DSL

```bash
# Build Debug APK locally
./gradlew assembleDebug

# Run Unit & Compose Tests
./gradlew testDebugUnitTest
```
