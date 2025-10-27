# 📱 Patient Management Android App — Setup Instructions

## 🧭 Overview

This is the Android mobile application for the Patient Management System, built with Kotlin, Android Jetpack components, and WorkManager for background synchronization.

It connects to the Django REST Framework backend API to manage:
- **Patients**
- **Vitals** 
- **Visit Forms (A & B)**
- **Offline data sync**

## ⚙️ 1️⃣ Requirements

Before you begin, ensure you have the following installed:

| Tool | Recommended Version |
|------|-------------------|
| Android Studio | Giraffe (or newer) |
| Android SDK | 34 |
| Gradle | 7.4.2 |
| JDK | Java 11 |
| Kotlin | 1.9+ |
| Git | Latest |

## 🗂️ 2️⃣ Clone the Repository

```bash
git clone https://github.com/DavidSaruni/patient-management-android.git
cd patient-management-android
```

Then open the project folder in Android Studio.

## ⚙️ 3️⃣ Android Studio Configuration

1. Open the project in Android Studio.
2. Allow Gradle to sync automatically.
3. If prompted, install missing SDK components or tools.
4. Ensure `compileSdkVersion = 34` and `minSdkVersion = 24` (or as defined in build.gradle).

## 📦 4️⃣ Build and Run the App

### Using Emulator
1. Create an Android Virtual Device (AVD) via AVD Manager.
2. Select a recommended image (e.g., Pixel 6, API 34).
3. Click **Run ▶️** to build and launch the app.

### Using Physical Device
1. Enable **Developer Options** → **USB Debugging** on your phone.
2. Connect it to your computer via USB.
3. Select your device from the Run target menu in Android Studio.
4. Click **Run ▶️** to install and start the app.

## 🔗 5️⃣ Connect to the Backend API

The app syncs data (patients, vitals, visits) with the Django REST API.
You need to point the app to your backend server.

1. **Find your computer's local IP address:**
   - **Windows** → `ipconfig`
   - **macOS/Linux** → `ifconfig`

2. **In the backend, run:**
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```

3. **Update the base URL in the Android app:**
   
   **File:** `ApiClient.kt`
   ```kotlin
   object ApiClient {
       const val BASE_URL = "http://192.168.xxx.xxx:8000/api/"
       ...
   }
   ```
   
   Replace `192.168.xxx.xxx` with your actual LAN IP.

4. **Rebuild the project and rerun the app.**

## 🔁 6️⃣ Offline Sync Setup (WorkManager)

The app automatically syncs:
- New patients
- Recorded vitals  
- Visit forms

via a background worker every 15 minutes (or manually after registration).

The sync is handled by:
- `SyncManager.kt` – schedules periodic syncs
- `SyncWorker.kt` – performs uploads to the API

Ensure your backend is reachable over the same network.

---

# 📱 Page Screenshots

## Patient Listing Dashboard
![Patient Listing](https://github.com/user-attachments/assets/cdf18e1c-b780-4e30-961a-089531b9a2b6)

The main dashboard showing all registered patients with their latest BMI status. Tap any patient to view detailed information.

## Patient Details Screen
(![WhatsApp Image 2025-10-27 at 5 59 26 PM](https://github.com/user-attachments/assets/db5eabc3-c29d-4a28-b20c-e2fb41062416)
)

**Key Features:**
- **Patient Information Card**: Shows name, age, gender, and registration date
- **Latest Vitals Card**: Displays height, weight, BMI, and BMI status with color coding
- **Visit History Card**: Shows all previous visits (both Visit A and Visit B) with visit type indicators
- **Smart Visit Button**: Automatically determines which visit form to use based on BMI:
  - **BMI < 25**: Shows Visit Form A (diet-focused)
  - **BMI ≥ 25**: Shows Visit Form B (drug usage and weight management)
- **Add Vitals Button**: Navigate to vitals recording screen

## Patient Registration
![Patient Registration](https://github.com/user-attachments/assets/2dbd3c94-20c2-429c-989b-329b76ac5a5e)

Register new patients with personal information and demographic details.

## Patient Vitals Recording
![Patient Vitals](https://github.com/user-attachments/assets/28889e95-75aa-49c8-abee-e425bd2cabb5)

Record patient vitals including height, weight, and automatically calculated BMI.

## Visit Form A (BMI < 25)
![Visit Form A](https://github.com/user-attachments/assets/d005f690-ec51-4d2d-962f-86912f277f0b)

For patients with BMI less than 25, focusing on general health and diet management.

## Patient Vitals - BMI ≥ 25
![Patient Vitals BMI >= 25](https://github.com/user-attachments/assets/d08d52ee-798f-46ed-bdbd-8d9807ada4e2)

Vitals recording for patients with BMI 25 or higher, which triggers Visit Form B.

## Visit Form B (BMI ≥ 25)
![Visit Form B](https://github.com/user-attachments/assets/4e1b1ed4-f0d2-40a5-93c9-da14f48b7fea)

For patients with BMI 25 or higher, focusing on drug usage and weight management.

## Patient Listing with BMI Status
![Patient Listing BMI](https://github.com/user-attachments/assets/b7a39e4d-bd20-4899-b351-f9bee2758784)

Dashboard showing patients with color-coded BMI status indicators.

## Filter by Date
![Filter by Date](https://github.com/user-attachments/assets/79282928-2449-44e9-a21b-b69529a67217)

Filter patients by visit date to view specific day's appointments.

### Offline-First Architecture
- **Local data storage**: All data stored locally using Room database
- **Background sync**: Automatic synchronization with backend API
- **Offline functionality**: App works without internet connection

