# 📱 Smart Delivery Android App

A premium Kotlin-based mobile application built with the latest Android architecture components.

## 🌟 Features

- **MVVM Pure Implementation:** Clean separation of UI logic and data.
- **OpenStreetMap (OSM):** Efficient, offline-capable mapping without vendor lock-in.
- **Real-Time Tracking Service:** Foreground service that ensures delivery accuracy.
- **Dagger Hilt:** Robust dependency injection for a scalable codebase.
- **Lottie Animations:** High-quality micro-interactions for an engaging UX.

## 🏗️ Architecture Layers

### 1. Presentation Layer (`ui`)
- **Activities/Fragments:** ViewBinding and DataBinding for declarative UI.
- **ViewModels:** Managing UI state using Kotlin Flows and LiveData.
- **Navigation:** Deeply integrated Jetpack Navigation for smooth transitions.

### 2. Data Layer (`data`)
- **Remote:** Retrofit interfaces for the REST API.
- **Socket:** Managed Socket.IO connection for real-time coordinates.
- **Repository:** The single source of truth coordinating network and local data.

### 3. Dependency Injection (`di`)
- Hilt modules providing API clients, database instances, and socket connections.

## 🗺️ Mapping System

The app utilizes **OSMDroid** for map rendering. This allows for:
- Custom tile providers.
- Faster map interactions.
- Completely free usage with no API quotas.

## 🛰️ Tracking Service

The `TrackingService.kt` is a specialized Foreground Service that:
1. Requests high-accuracy GPS updates.
2. Batches location points for network efficiency.
3. Communicates directly with the backend via Socket.IO.
4. Includes notification persistence to prevent OS-level service termination.

## 🚀 Build Instructions

1. Ensure you have **Android Studio Iguana** or newer.
2. Sync the project with Gradle.
3. Use the `debug` build variant for development to see detailed logs in Logcat.
4. To test real-time features, run the companion **Smart Delivery Backend**.

---
*Smart Delivery Mobile - Reliability in your pocket.*
