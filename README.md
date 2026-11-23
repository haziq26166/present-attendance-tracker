# Present! — Multi-Factor Attendance Tracker

> An Android app that makes attendance hard to fake: attendees check in to an event by **scanning a QR code**, **proving they are physically at the venue via GPS geofencing**, and **taking a live selfie**, while organizers create events, monitor check-ins in real time, and export attendance records.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Java-007396?logo=openjdk&logoColor=white)
![Backend](https://img.shields.io/badge/backend-Firebase-FFCA28?logo=firebase&logoColor=black)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [How Check-In Works](#how-check-in-works)
- [Tech Stack](#tech-stack)
- [Project Architecture](#project-architecture)
- [Data Model](#data-model)
- [Getting Started](#getting-started)
- [Known Limitations & Roadmap](#known-limitations--roadmap)
- [Contributors / Team](#contributors--team)

---

## Overview

Traditional attendance methods (paper sign-in sheets, shared links, a single QR code) are easy to abuse. Someone can sign in for a friend, or share a code with people who aren't in the room.

**Present!** solves this by requiring three independent proofs of presence before an attendance record is accepted:

1. **QR code**: identifies the exact event.
2. **GPS geofence**: confirms the device is within the organizer-defined radius of the venue.
3. **Live selfie**: attaches photographic evidence to the record for organizer review.

The app has two roles, **Admin (organizer)** and **User (attendee)**, each with its own navigation flow and dashboard. The role is stored in Firestore and the user is routed automatically at launch.

---

## Key Features

### For Attendees
- **Email/password registration & login** with role selection
- **Guided 3-step check-in wizard** (Scan QR → GPS verification → Selfie) with a live step indicator and custom scanner/oval-guide overlays
- **Real-time distance feedback** to the venue while the GPS check runs
- **Attendance history** with pull-to-refresh, showing event, venue, time, and captured photo
- **Home dashboard** with total events attended (live count) and recent activity
- **Profile screen** and persistent login via splash-screen session check

### For Organizers (Admins)
- **Two-step event creation**: details (name, description, date/time pickers, venue, capacity) followed by an **interactive Google Map** to drop a venue pin and set the **geofence radius** (enforced minimum of 20 m for GPS reliability)
- **Auto-generated QR code** per event, with a full-screen display mode that keeps the screen awake for projecting at the venue
- **Event management**: list, edit (bottom sheet), and delete (with confirmation) events
- **Live attendee list** per event with real-time Firestore updates
- **CSV export** of attendance records saved directly to the device's Downloads folder
- **Live dashboard** showing total events, today's check-ins, and a feed of the 10 most recent check-ins

### Engineering Highlights
- Duplicate-proof records: attendance documents are keyed as `{eventId}_{userId}`, so each user has at most one record per event
- Photos are resized and iteratively JPEG-compressed to ≤150 KB before being stored with the record
- Location updates and camera resources are released on pause/stop/destroy to avoid leaks and battery drain
- Firestore snapshot listeners are removed in `ViewModel.onCleared()`

---

## How Check-In Works

```
 ┌────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌────────────┐
 │  QR_SCAN   │ ──▶ │    GPS_CHECK     │ ──▶ │  PHOTO_CAPTURE   │ ──▶ │    DONE    │
 │ ML Kit     │     │ Fused Location   │     │ CameraX selfie   │     │ Record     │
 │ decodes    │     │ distance ≤ event │     │ compress ≤150 KB │     │ saved to   │
 │ event JSON │     │ radiusMeters     │     │                  │     │ Firestore  │
 └────────────┘     └──────────────────┘     └──────────────────┘     └────────────┘
```

The flow is driven by a `QRViewModel` state machine (`QRStep` enum) observed by `QRFragment`. Each saved record is tagged with `verificationMethods: ["qr", "gps", "photo"]` and `status: "verified"`.

The QR payload is a small JSON object containing `eventId`, `eventName`, `venueName`, `venueLat`, `venueLng`, and `radiusMeters`, generated when the organizer finalizes the event.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Language** | Java 11 |
| **Platform** | Android (minSdk 24, targetSdk 35, compileSdk 36) |
| **Architecture** | MVVM + Repository pattern, LiveData, ViewModel |
| **UI** | XML layouts, ViewBinding, Material Components, RecyclerView, SwipeRefreshLayout, custom Views |
| **Navigation** | Jetpack Navigation Component (separate graphs for Admin and User) |
| **Backend / BaaS** | Firebase Authentication, Cloud Firestore |
| **Camera & Vision** | CameraX (Camera2, Lifecycle, View), Google ML Kit Barcode Scanning |
| **Maps & Location** | Google Maps SDK, Google Play Services Location (Fused Location Provider) |
| **QR Generation** | ZXing (`zxing-android-embedded`) |
| **Serialization** | Gson |
| **Build** | Gradle (Groovy DSL), Version Catalog (`libs.versions.toml`), Android Gradle Plugin 8.10.1 |
| **Testing** | JUnit 4, AndroidX Test, Espresso (boilerplate scaffolding) |

---

## Project Architecture

The app follows **MVVM** with a thin repository layer over Firebase.

```
Present!/
├── app/
│   ├── build.gradle                 # App module config & dependencies
│   ├── google-services.json         # Firebase config (replace with your own)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml  # Permissions, Maps API key, activities
│       │   ├── java/com/example/present/
│       │   │   ├── SplashActivity.java        # Session check + role-based routing
│       │   │   ├── LoginActivity.java
│       │   │   ├── RegisterActivity.java
│       │   │   ├── AdminMainActivity.java     # Host for admin nav graph
│       │   │   ├── UserMainActivity.java      # Host for user nav graph
│       │   │   ├── QRDisplayActivity.java     # Full-screen event QR
│       │   │   │
│       │   │   ├── data/
│       │   │   │   ├── model/                 # User, Event, AttendanceRecord
│       │   │   │   └── repository/            # Auth, Event, Attendance, User repositories
│       │   │   │
│       │   │   ├── ui/
│       │   │   │   ├── admin/
│       │   │   │   │   ├── home/              # Dashboard stats & recent activity
│       │   │   │   │   ├── create/            # Event creation + map geofence picker
│       │   │   │   │   ├── events/            # Event list, edit sheet, delete
│       │   │   │   │   ├── attendees/         # Attendee list + CSV export
│       │   │   │   │   └── profile/
│       │   │   │   └── user/
│       │   │   │       ├── home/              # Attendance count & recent activity
│       │   │   │       ├── qr/                # 3-step check-in flow + custom overlay views
│       │   │   │       ├── history/           # Attendance history
│       │   │   │       └── profile/
│       │   │   │
│       │   │   └── util/                      # BitmapUtils, DateTimeUtils,
│       │   │                                  # PermissionUtils, QRCodeGenerator
│       │   └── res/
│       │       ├── layout/                    # Activity, fragment & list-item layouts
│       │       ├── navigation/                # nav_graph_admin.xml, nav_graph_user.xml
│       │       ├── menu/                      # Bottom navigation & overflow menus
│       │       ├── drawable/                  # Vector icons & shapes
│       │       └── values/ values-night/      # Colors, strings, light/dark themes
│       ├── test/                              # Unit test scaffold
│       └── androidTest/                       # Instrumented test scaffold
├── gradle/
│   └── libs.versions.toml                     # Centralized dependency versions
├── build.gradle
├── settings.gradle
└── gradle.properties
```

**Layering**

| Layer | Responsibility |
|---|---|
| **Activities / Fragments** | Render UI, handle user input, observe LiveData |
| **ViewModels** | Hold UI state (e.g. the `QRStep` and `CreateStep` state machines), expose LiveData, manage Firestore listeners |
| **Repositories** | Wrap Firebase Auth / Firestore calls behind simple callback interfaces |
| **Models** | Plain Java objects mapped directly to Firestore documents |

---

## Data Model

Cloud Firestore uses three top-level collections:

| Collection | Document ID | Key fields |
|---|---|---|
| `users` | Firebase Auth UID | `uid`, `displayName`, `email`, `role` (`admin` \| `user`), `createdAt` |
| `events` | Auto-generated ID (also embedded in the QR) | `eventName`, `description`, `date`, `startTime`, `endTime`, `venueName`, `venueLat`, `venueLng`, `radiusMeters`, `maxCapacity`, `organizerId`, `qrPayload`, `createdAt` |
| `attendance` | `{eventId}_{userId}` | `eventId`, `eventName`, `venueName`, `userId`, `displayName`, `email`, `checkedInAt`, `photoUrl` (Base64 data URL), `status`, `verificationMethods[]` |

---

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable recommended)
- **JDK 17** for Gradle (the project compiles to Java 11 bytecode)
- An Android device or emulator running **Android 7.0 (API 24)+**. A physical device is strongly recommended, since the check-in flow uses the camera and GPS.
- A Google account for Firebase and Google Cloud

### 1. Clone the repository

```bash
git clone https://github.com/haziq26166/present-attendance-tracker.git
cd present-attendance-tracker
```

### 2. Set up Firebase

This project has no `.env` file. Configuration lives in `google-services.json` and the Android manifest.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an **Android app** with the package name **`com.example.present`**.
3. Download the generated **`google-services.json`** and place it at **`app/google-services.json`** (replacing the existing file).
4. In **Authentication → Sign-in method**, enable **Email/Password**.
5. In **Firestore Database**, create a database (start in test mode for development, then lock it down with proper security rules before any real use).
6. Create the required **composite index** (Firestore will also print a one-click link in Logcat the first time the History screen runs without it):

   | Collection | Fields |
   |---|---|
   | `attendance` | `userId` (Ascending), `checkedInAt` (Descending) |

### 3. Add a Google Maps API key

The organizer's geofence picker uses the Maps SDK.

1. In [Google Cloud Console](https://console.cloud.google.com/), enable **Maps SDK for Android** for your project.
2. Create an API key (restrict it to your app's package name and SHA-1 fingerprint).
3. Open `app/src/main/AndroidManifest.xml` and replace the placeholder value:

   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_MAPS_API_KEY" />
   ```

> **Tip:** To keep the key out of version control, move it to `local.properties` and inject it with `manifestPlaceholders` in `app/build.gradle`.

### 4. Build and run

**Android Studio:** open the project, wait for the Gradle sync, select a device, and press **Run ▶**.

**Command line:**

```bash
./gradlew assembleDebug        # build the APK
./gradlew installDebug         # install on a connected device
```

### 5. Try it out

1. Register one account as an **Admin** and another as a **User** (the sign-up screen includes a role toggle).
2. As Admin: **Create Event → pick a location and radius → Confirm**, then open the event's QR code from the Events tab.
3. As User: open the **Scan** tab, scan the QR, stand within the radius, and take the selfie.
4. As Admin: open the event's attendee list to see the live check-in and **export to CSV**.

---

## Known Limitations & Roadmap

Honest notes on the current state of the project and where it could go next:

- [ ] **Server-side validation.** GPS and distance checks currently run on the client; moving verification into Cloud Functions plus Firestore Security Rules would harden against spoofed locations.
- [ ] **Enforce event time window and capacity** (`startTime`, `endTime`, `maxCapacity` are stored but not yet checked at check-in).
- [ ] **Move selfies to Firebase Storage** instead of Base64 strings inside Firestore documents.
- [ ] **Face/liveness detection** on the selfie step.
- [ ] **Secrets hygiene.** Externalize the Maps key and rotate Firebase keys if a config file was ever committed publicly.
- [ ] **Automated tests.** Replace the Android Studio test scaffolding with ViewModel and repository tests.
- [ ] Remove unused dependencies (Realtime Database, Credential Manager / Google ID) or implement Google Sign-In.
