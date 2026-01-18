# Technical Context

## Technologies Used
-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose (Material3)
-   **Architecture:** MVVM (Model-View-ViewModel)
-   **Asynchronous Processing:** Kotlin Coroutines & Flow
-   **Background Processing:** Android Foreground Service
-   **Dependency Injection:** Manual (via ViewModel factory/instantiation) - *Note: Could be migrated to Hilt if complexity grows.*
-   **Build System:** Gradle (Kotlin DSL)

## Development Setup
-   **Min SDK:** 24 (Android 7.0 Nougat)
-   **Target SDK:** 34 (Android 14)
-   **Compile SDK:** 34
-   **Compose Compiler:** 1.5.1
-   **AGP Version:** 8.6.0
-   **Kotlin Version:** 1.9.0

## Technical Constraints
-   **Battery Optimization:** Foreground services are subject to OS restrictions; proper permissions and notification handling are crucial.
-   **Screen Wake Lock:** The app must keep the screen on during countdown, which impacts battery life.
-   **Gesture Handling:** Accurate conversion of touch coordinates to angles and time values is critical for UX.
-   **State persistence:** Handling configuration changes (rotation) and process death requires saving state (currently handled via Repository/ViewModel survival and Service rebinding).

## Dependencies
-   `androidx.core:core-ktx`
-   `androidx.lifecycle:lifecycle-runtime-ktx`
-   `androidx.activity:activity-compose`
-   `androidx.compose:compose-bom`
-   `androidx.compose.ui:ui`
-   `androidx.compose.material3:material3`
-   `androidx.lifecycle:lifecycle-viewmodel-compose`
-   `androidx.lifecycle:lifecycle-runtime-compose`
-   `androidx.work:work-runtime-ktx`
