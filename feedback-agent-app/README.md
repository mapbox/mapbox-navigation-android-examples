# Feedback Agent App

This module demonstrates the Mapbox Navigation SDK's voice feedback capabilities. It provides a simple interface for users to provide voice feedback during navigation sessions.

## Features

- Voice feedback recording and processing
- Real-time speech recognition display
- Feedback submission to Mapbox Navigation SDK
- Clean Material 3 UI with Jetpack Compose

## Usage

### Setup

This module's MapGPT Maven artifacts require a secret Mapbox token with **Downloads:Read**
scope. Add it to your global `~/.gradle/gradle.properties`:

```properties
MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_MAPBOX_TOKEN
```

Alternatively, set the `MAPBOX_DOWNLOADS_TOKEN` environment variable. The Gradle property takes
precedence if both are set. Authentication is configured only for this module's Mapbox Maven
repository. Keep the secret out of source control and app resources.

For CircleCI builds that include this module, set `MAPBOX_DOWNLOADS_TOKEN` in
**Project Settings > Environment Variables** to a secret token with **Downloads:Read** scope.

### Using the app

1. Tap "Connect" to establish connection with the voice feedback service
2. Tap "Start Listening" to begin voice input
3. Speak your feedback
4. Tap "Stop Listening" to end recording and submit feedback
5. View feedback submission results

## Architecture

The app follows a MVVM architecture pattern with:

- `VoiceFeedbackView` - Compose UI for the feedback interface
- `VoiceFeedbackViewModel` - Business logic and state management
- `AutomaticSpeechRecognitionEngine` - ASR integration layer
- Domain models for speech recognition states and feedback DTOs

## Dependencies

- Mapbox Navigation SDK v3.11.0
- Jetpack Compose for UI
- Kotlin Coroutines for async operations
- Android microphone middleware for audio capture

## Permissions

The app requires microphone permissions to capture voice feedback. The app will prompt for permissions on first launch.

## Technical Implementation

- Uses MapGPT experimental APIs for speech recognition infrastructure
- AudioLiteMicrophoneMiddleware for microphone access
- Integration with Mapbox Navigation SDK telemetry for feedback submission