# Implementation Plan: Cloud Synchronization

This document outlines the strategic plan for implementing cross-device cloud synchronization for the AutoCare app using Firebase.

## 1. Overview
The goal is to enable users to access their vehicle data across multiple devices seamlessly. The architecture follows an **offline-first** approach, using Room as the local source of truth and Firebase Firestore as the cloud sync provider.

*   **Firebase Project:** `autocare-3f144f19`
*   **Primary Technologies:** Firebase Auth, Cloud Firestore, Room, WorkManager.

## 2. Infrastructure & Authentication
Before synchronization can begin, a secure user context must be established.

*   **Firebase Setup:** Integrate the Firebase SDK and `google-services.json`.
*   **Authentication:** Implement **Firebase Authentication** (Google Sign-In or Anonymous Auth).
*   **User Scoping:** All data in Firestore will be stored under a `/users/{userId}/` root to ensure data isolation and privacy.

## 3. Data Model Enhancements
To facilitate synchronization and conflict resolution, the Room entities need additional metadata.

*   **Sync Fields:** Add the following fields to all syncable entities (`Vehicle`, `FuelLoad`, `Maintenance`, `Expense`, `Reminder`):
    *   `lastUpdated: Long`: Timestamp of the last local or remote modification.
    *   `isDeleted: Boolean`: Soft-delete flag to sync deletions across devices.
    *   `uuid: String`: (Already present) Used as the primary key for cloud mapping.
*   **Firestore Mapping:** Create a mapper layer to convert Room entities to Firestore documents.

## 4. The Synchronization Engine (Bidirectional)
A two-way synchronization strategy ensures consistency while maintaining offline functionality.

### Outgoing Sync (Local → Cloud)
*   **Trigger:** Any local write (Insert/Update/Delete) triggers a `SyncWorker` via **WorkManager**.
*   **Process:** The worker identifies records where `lastUpdated` is greater than the `lastSyncTimestamp`. It pushes these changes to Firestore using the `uuid` as the document ID.
*   **Reliability:** WorkManager handles retries and ensures sync completes even if the app is closed or network is intermittent.

### Incoming Sync (Cloud → Local)
*   **Strategy:** Use Firestore **Snapshot Listeners** (Real-time) or a manual "Pull" on app start.
*   **Process:** When remote changes are detected, the app compares the remote `lastUpdated` with the local version.
*   **Conflict Resolution:** "Last Write Wins" strategy based on the `lastUpdated` timestamp. If `isDeleted` is true in the cloud, the local record is removed.

## 5. Security Rules
Enforce data privacy at the database level using Firebase Security Rules:

```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{collection}/{document} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 6. Implementation Roadmap
1.  **Phase 1:** Firebase SDK integration and Auth implementation.
2.  **Phase 2:** Database migration to add `lastUpdated` and `isDeleted` fields.
3.  **Phase 3:** Implementation of `SyncWorker` for outgoing changes (starting with `Vehicle`).
4.  **Phase 4:** Implementation of real-time listeners for incoming changes.
5.  **Phase 5:** Multi-device testing and edge-case handling (e.g., rapid offline edits).
