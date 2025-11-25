# 🐾 **Petify, Android E-Commerce App for Pet Supplies**

![Android](https://img.shields.io/badge/Android-Java-brightgreen)
![Firebase](https://img.shields.io/badge/Firebase-Full%20Suite-orange)
![Firestore](https://img.shields.io/badge/Firestore-NoSQL%20DB-blue)
![Storage](https://img.shields.io/badge/Firebase%20Storage-Images-blue)
![Stripe](https://img.shields.io/badge/Stripe-API%20Integration%20-purple)
![Gradle](https://img.shields.io/badge/Build-Gradle-yellow)
![UI](https://img.shields.io/badge/UI-XML-lightgrey)

**Petify** is a complete **Android e-commerce app** built with
**Java + XML + Firebase Auth + Cloud Firestore + Firebase Storage**, featuring real-time synchronization and two fully separated panels: **User** and **Admin**.

It includes **Stripe API integration** for real online payment processing.

---

# 📸 Screenshots

### 🏠 Home Screen

![](/Demo/Home.png)

### 🛒 Shopping Cart

![](/Demo/Cart.png)

### 🐶 Product Detail

![](/Demo/Product.png)

### 👤 User Profile

![](/Demo/Profile.png)

### 🛠 Admin Dashboard

![](/Demo/adminDashboard.png)


---

# 🧭 Project Overview

Petify includes **two synchronized environments** powered by Firestore:

| Panel                 | Capabilities                                                                                 |
| --------------------- | -------------------------------------------------------------------------------------------- |
| 👤 **User Panel**     | Browse products, search, add to cart, manage favorites, view product details, manage profile |
| 🧑‍💼 **Admin Panel**   | Add/edit/delete products, upload product images, view user orders, manage payments           |

Both panels communicate with:

* **Cloud Firestore** for real-time database
* **Firebase Storage** for hosting images
* **Firebase Authentication** for secure login
* **Stripe API** for payments

---

# ⚙️ Tech Stack

### **Languages & Tools**

* Java
* XML UI
* Android Studio
* Gradle

### **Firebase Services**

| Service                | Usage                                    |
| ---------------------- | ---------------------------------------- |
| **Authentication**     | Role-based login (User/Admin)            |
| **Firestore NoSQL DB** | Products, users, orders, cart, favorites |
| **Firestore Indexes**  | Optimized search & queries               |
| **Firebase Storage**   | Product images + Profile images          |
| **Firestore Rules**    | Secure per-user access control           |

### **External APIs**

| API                      | Purpose                                                                                 |
| ------------------------ | --------------------------------------------------------------------------------------- |
| **Stripe API** | Secure online payment processing (credit cards and debit cards payment intents) |

Stripe will handle:

* Payment intents
* Payment confirmation
* Server-side verification
* Firestore update on payment success

---

# 🧩 Key Features

## 🔐 Authentication

* Email/Password login via Firebase
* Admin/User role mapping
* Auto redirect based on role

---

## 👤 User Features

### 🏠 Home Page

* Firestore-powered product list
* Search + instant filter
* Real-time image loading via URL & Storage

### ❤️ Favorites System

* Toggle heart icon
* Stored in `users/{uid}/favorites`
* Favorites screen includes:

  * Product title, category, price
  * Add to Cart button
  * Remove favorite

### 🛒 Shopping Cart

* Add/remove products
* Modify quantity
* Real-time syncing to Firestore

### 🧾 Product Details

* Large image
* Description
* Favorite toggle
* Add to Cart

### 👤 Profile Section

* Edit profile
* Change password
* Logout
* View order history

---

## 🧑‍💼 Admin Features

### 📦 Product Management

* Create new products
* Edit product details
* Delete products
* Upload product images → Firebase Storage

### 🛒 Order & Payment Overview

* See all user orders
* Inspect payment details
* Verify completed payments
* Support for **Stripe API**

---

# 🔥 Firebase Architecture (Technical Deep Dive)

## **Firestore Structure**

```
users/
   uid/
      name: "John"
      email: "john@gmail.com"
      role: "user"

products/
   productId/
      title: "Dog Toy"
      category: "Toys"
      price: 12.99
      description: "..."
      imageUrl: "..."
      createdAt: timestamp

users/{uid}/favorites/
   productId/
      productId: "..."
      title: "..."
      imageUrl: "..."
      price: 12.99
      createdAt: timestamp

users/{uid}/cartItems/
   productId/
      title: "..."
      quantity: 2
      price: 12.99
      imageUrl: "..."

orders/
   orderId/
      userId: "uid"
      items: [...]
      totalPrice: ...
      status: "pending" | "paid"
```

---

## **Firebase Storage Structure**

```
product_images/
    productId.jpg

profile_images/
    uid.jpg
```

Used by:

* Admin product creation
* User profile updates

---

# 💳 Stripe API Integration 

Stripe integration will introduce:

### **Client-Side (Android)**

* Initiate payment intents
* Display Stripe’s payment sheet
* Secure tokenization of card details

### **Server-Side**

* Cloud Function or backend service will:

  * Create payment intents
  * Verify success
  * Update Firestore:

    ```
    orders/orderId/status: "paid"
    ```

### **Benefits**

* PCI-compliant card handling
* Strong authentication
* Support for credit/debit
* Automatic fraud detection

---

# 🧱 Architecture

* **Models** → represent Firestore documents
* **Adapters** → efficient RecyclerView bindings
* **FirebaseUtils** → shared Firestore/Auth instances
* **Activities** → UI + user interactions, no business logic inside
* **Subcollections** → for favorites, cart, payments

---

# 🗂️ Folder Structure

```
Petify/
 ├── app/
 │   ├── java/com/example/petify/
 │   │    ├── User/
 │   │    ├── Admin/
 │   │    ├── Adapters/
 │   │    ├── Models/
 │   │    └── FirebaseUtils.java
 │   ├── res/layout/
 │   ├── res/drawable/
 │   ├── res/values/
 │   └── AndroidManifest.xml
 ├── google-services.json
 ├── build.gradle
 ├── README.md
 └── .gitignore
```

---

# 🚀 Running the Project

1. Clone:

```bash
git clone https://github.com/negarprh/Petify.git
```

2. Open in Android Studio
3. Add your `google-services.json` under `/app`
4. Connect Firebase
5. Sync + Run


---

# 👩‍💻 Authors

**Negar Pirasteh** ,
**Betty Dang**
