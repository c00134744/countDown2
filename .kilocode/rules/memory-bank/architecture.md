# System Architecture

## Overview
The app follows a standard MVVM (Model-View-ViewModel) architecture, leveraging Jetpack Compose for UI and Android Architecture Components for state management and background processing.

## Core Components

### UI Layer (View)
-   **`MainActivity`**: The entry point, setting up the theme and hosting the main screen.
-   **`TimerScreen`**: The primary composable screen, coordinating sub-components.
-   **`CircularTimerView`**: A custom composable drawing the circular track and handling drag gestures for time setting.
-   **`TimerDisplay`**: Displays the digital time in the center.
-   **`ControlButton`**: The main action button (Start/Pause/Stop).

### ViewModel Layer
-   **`TimerViewModel`**:
    -   Holds the `TimerState`.
    -   Exposes state via `StateFlow`.
    -   Handles user actions (`START`, `PAUSE`, `STOP`, `SET_TIME`).
    -   Manages the connection to `TimerForegroundService`.

### Data Layer (Model)
-   **`TimerRepository`**:
    -   Source of truth for `TimerState`.
    -   Manages local data and state updates.
-   **`TimerState`**: Data class representing the UI state (status, time remaining, angle, progress).

### Service Layer
-   **`TimerForegroundService`**:
    -   Runs the actual countdown timer (`CountDownTimer`) in a foreground service.
    -   Ensures the timer keeps running even if the app is killed or backgrounded.
    -   Manages the persistent notification.
    -   Communicates updates back to the UI via listeners/callbacks.

## Key Technical Decisions
-   **Foreground Service:** chosen over `WorkManager` or simple coroutines to guarantee precise timing execution and user visibility (via notification) even when the app is not active.
-   **Jetpack Compose:** Used for declarative UI building, simplifying the complex circular drawing and animation logic.
-   **StateFlow:** Used for reactive state management, allowing the UI to update instantly upon state changes from the ViewModel or Repository.

## Component Relationships
`TimerScreen` -> observes -> `TimerViewModel` -> manages -> `TimerRepository`
`TimerViewModel` -> binds -> `TimerForegroundService`
`TimerForegroundService` -> updates -> `TimerNotificationManager`
