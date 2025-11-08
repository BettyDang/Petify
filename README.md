## 🐾 Petify - Android E-Commerce App

**Petify** is a modern **Android e-commerce application** built with **Java**, **XML**, and **Firebase**.
It provides a complete pet-supply shopping experience with two panels, one for **users** and one for **admins**, all connected through **Firebase Authentication** and **Cloud Firestore**.

---

### 🧭 Project Overview

| Panel                 | Description                                                                                   |
| --------------------- | --------------------------------------------------------------------------------------------- |
| 👤 **User Panel**     | Users can create accounts, log in, browse pet products, and (in future updates) place orders. |
| 🧑‍💼 **Admin Panel** | Admins can add, edit, and delete products, view user orders, and check payment information.   |

Both panels share the same **Firebase Firestore database**, ensuring real-time updates and seamless synchronization.

---

### ⚙️ Tech Stack

| Category           | Tools                                    |
| ------------------ | ---------------------------------------- |
| **Language**       | Java                                     |
| **UI Design**      | XML (LinearLayout, ScrollView, CardView) |
| **Database**       | Firebase Cloud Firestore                 |
| **Authentication** | Firebase Authentication                  |
| **IDE**            | Android Studio                           |
| **Build System**   | Gradle                                   |
| **Target SDK**     | 34                                       |
| **Minimum SDK**    | 24                                       |

---

### 🧩 Main Features

#### 🔐 Authentication

* Firebase Authentication (Email + Password)
* Shared **Login** and **Sign Up** screens
* Role selection (User / Admin)

#### 🧑‍💼 Admin Panel

* Dashboard with quick actions
* Add new products (name, price, category, image)
* Edit and delete existing products
* View all user orders
* Track payments and transactions

#### 👤 User Panel

* Sign up or log in with Firebase
* Browse pet products
* View details and add to cart *(upcoming)*
* Place orders *(upcoming)*

---

### 🔥 Firebase Integration

| Feature                | Firebase Service Used   |
| ---------------------- | ----------------------- |
| User Authentication    | Firebase Authentication |
| Real-Time Data Storage | Cloud Firestore         |
| Product Management     | Firestore Collections   |
| Role-Based Access      | Firestore user roles    |
| Future Payments        | Stripe API (planned)    |

> Example Firestore collections:
>
> ```
> users/
> ├── userID/
> │    ├── name: "John Doe"
> │    ├── role: "user"
> │    └── email: "john@example.com"
>
> products/
> ├── productID/
> │    ├── name: "Dog Toy"
> │    ├── price: 9.99
> │    ├── category: "Toys"
> │    └── imageUrl: "..."
> ```

---

### 🏗️ Folder Structure

```
Petify/
 ├── app/
 │   ├── java/com/example/petify/
 │   │    ├── MainActivity.java
 │   │    ├── LoginActivity.java
 │   │    ├── SignUpActivity.java
 │   │    ├── Admin/
 │   │    │    ├── DashboardActivity.java
 │   │    │    ├── ProductsActivity.java
 │   │    │    ├── OrdersActivity.java
 │   │    │    └── PaymentsActivity.java
 │   │    └── Models/
 │   ├── res/layout/
 │   │    ├── activity_login.xml
 │   │    ├── activity_sign_up.xml
 │   │    ├── activity_admin_dashboard.xml
 │   │    └── other layout files...
 │   ├── res/values/
 │   │    ├── colors.xml
 │   │    ├── strings.xml
 │   │    └── themes.xml
 │   └── AndroidManifest.xml
 ├── google-services.json
 ├── build.gradle
 ├── README.md
 └── .gitignore
```

---

### 🚀 How to Run

1. Clone the repository

   ```bash
   git clone https://github.com/YOUR_USERNAME/Petify.git
   ```
2. Open the project in **Android Studio**
3. Add your `google-services.json` file inside `/app`
4. Make sure Firebase is connected (`Tools → Firebase → Authentication / Firestore`)
5. Sync Gradle and run the app

---

### 💡 Future Improvements

* Stripe API for payment processing
* Image upload to Firebase Storage
* Product search and filtering
* Real-time delivery tracking via Google Maps API

---

### 👩‍💻 Authors

**Developed by:** Negar Pirasteh - Betty Dang
**College:** LaSalle College, Montréal
