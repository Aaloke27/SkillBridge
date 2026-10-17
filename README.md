# 🌉 SkillBridge

### Connecting Local Workers with Opportunities

SkillBridge is a native Android application designed to connect local service professionals with customers. It helps workers discover job opportunities based on their location and availability while allowing customers to find and book suitable service professionals.

Developed as a solution to **SIH Problem Statement SIH26089** during **Solvathon 2026**, the internal hackathon at Swami Vivekananda Institute of Modern Science (SVIMS).

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Problem Statement](#-problem-statement)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Application Workflow](#-application-workflow)
- [Getting Started](#-getting-started)
- [Firebase Configuration](#-firebase-configuration)
- [Project Structure](#-project-structure)
- [Future Improvements](#-future-improvements)
- [Team](#-team)
- [Author](#-author)

---

## 💡 Overview

Finding reliable local service professionals can be difficult, while skilled workers may struggle to find suitable work opportunities in their nearby areas.

SkillBridge aims to bridge this gap through a location-aware Android application that connects customers with local workers.

The application provides separate experiences for customers and workers, supporting service discovery, booking management, and job status tracking.

## 🎯 Problem Statement

**SIH26089**

SkillBridge was developed as a proposed solution to the selected Smart India Hackathon problem statement during Solvathon 2026.

The project explores how mobile technology can help connect local service professionals with customers seeking their services.

> Note: This project was developed during a college internal hackathon. This does not imply that it was an official Smart India Hackathon finalist or winner.

---

## ✨ Key Features

### 👤 Customer

- Register and log in using email and password.
- Discover available local service professionals.
- Find workers based on service area and profession.
- Submit service booking requests.
- View booking history and booking statuses.
- Manage profile information.

### 🛠️ Worker

- Register with a worker profile and profession.
- Set availability for service requests.
- Manage service-area information.
- Receive customer booking requests.
- Accept or reject incoming requests.
- View assigned bookings and their statuses.
- Complete accepted jobs.

### 🔥 Firebase Integration

- Firebase Authentication for user authentication.
- Cloud Firestore for user profiles, worker profiles, and booking data.
- Firestore security rules for access control.
- Separate booking-location documents to support controlled access to customer location information.

Location access restrictions depend on the deployed Firestore rules and application implementation. Review these rules before using the application with real customer data.

---

## 🧰 Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Jetpack Compose | Declarative native Android UI |
| Material 3 | UI components and visual design |
| Firebase Authentication | Email/password authentication |
| Cloud Firestore | Cloud database and booking management |
| Google Play Services Location | Device location detection |
| Gradle Kotlin DSL | Build configuration |
| Git & GitHub | Version control and collaboration |
| Android Studio | Development environment |

---

## 🔄 Application Workflow

### Customer Workflow

1. Register or log in.
2. Explore available workers in the relevant service area.
3. Select a suitable worker.
4. Submit a booking request with a description of the required service.
5. Track the booking status.

### Worker Workflow

1. Register or log in as a worker.
2. Set a service area and availability.
3. Review incoming customer requests.
4. Accept or reject a request.
5. Manage accepted jobs and mark them as completed.

---

## 🚀 Getting Started

Follow these steps to run SkillBridge locally.

### Prerequisites

Install the following tools:

- [Android Studio](https://developer.android.com/studio)
- [Git](https://git-scm.com/)
- Android SDK compatible with the project configuration
- A Firebase account and Firebase project

### 1. Clone the Repository

```bash
git clone https://github.com/Aaloke27/SkillBridge.git
