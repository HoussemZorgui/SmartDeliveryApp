# 🚚 Smart Delivery App

<p align="center">
  <img src="assets/hero.png" width="400" alt="Smart Delivery App Hero">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Active-brightgreen" alt="Status">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Backend-NodeJS-339933?logo=nodedotjs" alt="Backend">
  <img src="https://img.shields.io/badge/Database-MongoDB-47A248?logo=mongodb" alt="Database">
  <img src="https://img.shields.io/badge/RealTime-Socket.io-010101?logo=socketdotio" alt="RealTime">
</p>

---

## ✨ Overview

**Smart Delivery** is a high-performance, full-stack logistics solution designed for the modern era. Combining a robust **Node.js** backend with a cutting-edge **Kotlin** Android application, it provides a seamless real-time tracking experience for both customers and drivers.

Whether you're managing thousands of deliveries or tracking a single package, Smart Delivery offers the precision, speed, and elegance required in today's on-demand economy.

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **🛰️ Real-Time Tracking** | Live driver location updates powered by Socket.io and background services. |
| **🗺️ Intelligent Mapping** | High-performance map integration using **OSMDroid** (OpenStreetMap) – no Google Maps API keys required! |
| **🔐 Enterprise Security** | Stateless **JWT Authentication** and high-entropy password hashing with **Bcrypt**. |
| **📦 Order Lifecycle** | Full CRUD capabilities for order management with real-time status transitions. |
| **🎨 Premium UX/UI** | A stunning Dark Mode aesthetic with **Lottie** micro-animations and Material Design 3 components. |
| **⚡ Scalable Architecture** | Redis-ready backend and Hilt-powered dependency injection on Android. |

---

## 🏗️ Deep Dive Architecture

### 🛡️ Backend (The Engine)
Built with scalability in mind, the backend follows a strict **MVC/Service** pattern:
- **Runtime:** Node.js v20+ with Express 5.0.
- **Data Layer:** MongoDB via Mongoose with optimized indexing for location queries.
- **Real-time Hub:** Socket.io managing bi-directional communication channels for instant delivery pings.
- **State Management:** Redis integration (configurable) for high-speed session and location caching.

### 📱 Android (The Interface)
A masterpiece of modern Android development (MAD):
- **Language:** 100% Kotlin.
- **Architecture:** MVVM (Model-ViewModel-View) for clean separation of logic.
- **Networking:** Retrofit + OkHttp with custom Interceptors for seamless API interaction.
- **Location:** Integrated Foreground Tracking Service ensuring location updates even when the app is minimized.
- **Dependency Injection:** Dagger Hilt for modular and testable code.

---

## 🛠️ Tech Stack

### Frontend
- **Framework:** Jetpack (Navigation, Lifecycle, LiveData)
- **Maps:** OSMDroid (OpenStreetMap)
- **Real-time:** Socket.io-client
- **DI:** Hilt
- **Animations:** Lottie

### Backend
- **Framework:** Express.js
- **Database:** MongoDB
- **Real-time:** Socket.io
- **Auth:** JWT / Bcrypt
- **Caching:** Redis

---

## ⚙️ Getting Started

### 📦 Backend Setup
1. **Navigate to backend:**
   ```bash
   cd backend
   ```
2. **Install dependencies:**
   ```bash
   npm install
   ```
3. **Configure Environment:**
   Create a `.env` file based on `.env.example`:
   ```env
   PORT=3000
   MONGO_URI=your_mongodb_connection_string
   JWT_SECRET=your_super_secret_key
   ```
4. **Launch:**
   ```bash
   npm start
   ```

### 📱 Android Setup
1. **Open the project** in Android Studio.
2. **Sync Gradle** and ensure all dependencies are downloaded.
3. **Configure API Endpoint:**
   Update the `BASE_URL` in `src/main/java/com/example/smartdeliveryapp/data/api/RetrofitClient.kt`.
4. **Run:** Connect your physical device or emulator and hit **Run**.

---

## 📄 License

This project is licensed under the **ISC License**. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Developed with ❤️ by the Smart Delivery Team
</p>
