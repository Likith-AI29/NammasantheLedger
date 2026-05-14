#  Namma Santhe Ledger (Android)

**Namma Santhe Ledger** is an Android application built with **Jetpack Compose** that helps users manage customer ledger records digitally.
The app allows tracking of **UDARI (credit)** and **RECEIVED (payment)** transactions, viewing customer history, and handling authentication with a local Room database.

---

##  Features

###  Authentication

* Splash Screen flow
* User Login & Signup
* Persistent login session using **DataStore**
* Local user management with **Room Database**

###  Ledger Management

* Add **UDARI (Credit)** entries
* Add **RECEIVED (Payment)** entries
* Track outstanding balances
* View total paid and pending amounts
* Customer-wise summary cards

###  Transaction History

* Search transactions easily
* Separate tabs for:

  * **Udari (Credit)**
  * **Paid (Received)**

###  Additional Features

* WhatsApp reminder integration for pending dues
* Profile screen with:

  * Logged-in user details
  * Logout functionality

---

##  Tech Stack

* **Kotlin**
* **Jetpack Compose (Material 3)**
* **Navigation Compose**
* **Room Database**
* **KSP (Kotlin Symbol Processing)**
* **DataStore Preferences**

---

##  Project Structure

```text
app/src/main/java/com/example/nammasantheledger/
│
├── MainActivity.kt
│   └── App navigation and UI scaffolding
│
├── SplashScreen.kt
│   └── Splash screen UI
│
├── LoginScreen.kt
├── SignupScreen.kt
│   └── Authentication screens
│
├── SessionManager.kt
│   └── DataStore session management
│
├── AppDatabase.kt
│   └── Room database setup
│
├── TransactionDao.kt
│   └── DAO methods for transactions and users
│
├── LedgerViewModel.kt
│   └── Business logic and state handling
│
├── HistoryScreen.kt
├── ProfileScreen.kt
│   └── History and profile UI
```

---

##  How It Works

### Database Structure

The app uses **Room Database** to store:

* **Users**

  * User authentication data
  * `email` acts as the primary key

* **Customers**

  * Customer information
  * Balance and payment summaries

* **Transactions**

  * Credit (Udari) entries
  * Payment (Received) entries

---

### App Flow

1. App launches with the **Splash Screen**
2. Session status is checked using **DataStore**
3. User is redirected to:

   * Login/Signup screen (if not logged in)
   * Main Ledger screen (if session exists)
4. After successful authentication:

   ```kotlin
   SessionManager.setLoggedIn(email, name)
   ```
5. User gains access to the ledger dashboard

---

##  Build & Run

### Prerequisites

* Android Studio
* Minimum SDK: **24**

### Steps

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

> Dependency versions are managed using:

```text
gradle/libs.versions.toml
```

---

## Notes / Limitations

* Passwords are currently stored directly in Room Database
  ⚠️ This is acceptable for learning/testing purposes but **not recommended for production apps**.

* The database currently uses:

```kotlin
fallbackToDestructiveMigration(dropAllTables = true)
```

which resets data on schema changes.

---

## Future Improvements

* Password hashing & secure authentication
* Cloud backup / sync support
* Export ledger reports (PDF/Excel)
* Dark mode improvements
* Multi-device support

---

## License

Add your preferred license here.
Example: MIT License, Apache 2.0, etc.

---

## Author

Developed using **Kotlin + Jetpack Compose** for modern Android development.
