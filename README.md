# EasyShop E-Commerce API

A Spring Boot e-commerce backend API implementing product management, categories, and shopping cart functionality with MySQL database.

## 🚀 Features Implemented

### Phase 1: Categories Management
- View all categories
- View category by ID
- Create new categories (Admin only)
- Update existing categories (Admin only)
- Delete categories (Admin only)

### Phase 2: Bug Fixes
**Bug #1: Price Filter**
- Fixed price range filtering to correctly use `>=` for minPrice and `<=` for maxPrice
- Products now properly filter within the specified price range

**Bug #2: Product Update**
- Fixed product update endpoint that was incorrectly creating duplicates
- Changed from `productDao.create()` to `productDao.update()` in PUT endpoint

### Phase 3: Shopping Cart
- View user's shopping cart
- Add items to cart with smart quantity logic
- Update item quantities
- Clear entire cart
- Cart persists across login sessions

## 🛠️ Technologies Used

- Java 17
- Spring Boot 2.7.3
- Spring Security with JWT authentication
- MySQL Database
- JDBC for database operations

## 🔐 Authentication

### Test Users

| Username | Password | Role |
|----------|----------|------|
| user | password | USER |
| admin | password | ADMIN |
| george | password | USER |

### Login Process

```http
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

Response includes JWT token for authenticated requests.

## 📡 API Endpoints

### Public Endpoints

```http
GET    /categories              # Get all categories
GET    /categories/{id}         # Get category by ID
GET    /products                # Get all products
GET    /products/{id}           # Get product by ID
GET    /products?cat={id}       # Filter by category
GET    /products?minPrice={}&maxPrice={}  # Filter by price range
POST   /login                   # User login
POST   /register                # User registration
```

### Admin Endpoints (Requires ADMIN role)

```http
POST   /categories              # Create category
PUT    /categories/{id}         # Update category
DELETE /categories/{id}         # Delete category
POST   /products                # Create product
PUT    /products/{id}           # Update product
DELETE /products/{id}           # Delete product
```

### User Endpoints (Requires Authentication)

```http
GET    /cart                    # Get user's cart
POST   /cart/products/{id}      # Add item to cart
PUT    /cart/products/{id}      # Update item quantity
DELETE /cart                    # Clear cart
```

## 💡 Interesting Code: Smart Cart Logic

Located in `MySqlShoppingCartDao.java`, the `addItem()` method implements intelligent cart management:

```java
@Override
public void addItem(int userId, int productId)
{
    String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
    String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
    String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";
    
    try (Connection conn = getConnection()) {
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Item exists - increment quantity
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, userId);
                        updateStmt.setInt(2, productId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Item doesn't exist - insert new
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("DB Error adding product to cart");
    }
}
```

**Why it's smart:**
Instead of blindly inserting items, this method:
1. Checks if the product already exists in the user's cart
2. If it exists, increments the quantity by 1
3. If it doesn't exist, inserts a new cart item with quantity 1
4. Prevents duplicate cart entries and provides a better user experience

## 📸 Application Screenshots

### Homepage - Product Browsing
The main page displays all products with filtering options by category and price range.

### Shopping Cart
Users can add items to cart, update quantities, and see real-time totals.

### Admin Features
Administrators can create, update, and delete both categories and products.

## 🔑 Implementation Files

### Controllers
- `CategoriesController.java` - REST endpoints for category management
- `ProductsController.java` - REST endpoints for product management (includes bug fixes)
- `ShoppingCartController.java` - REST endpoints for cart operations

### DAO Layer
- `MySqlCategoryDao.java` - Category database operations
- `MySqlProductDao.java` - Product database operations with fixed price filtering
- `MySqlShoppingCartDao.java` - Shopping cart database operations with smart add logic

### Models
- `Category.java` - Category entity
- `Product.java` - Product entity
- `ShoppingCart.java` - Shopping cart container
- `ShoppingCartItem.java` - Individual cart item

## 🎯 Key Implementation Highlights

1. **RESTful API Design** - All endpoints follow REST conventions
2. **JWT Authentication** - Secure token-based authentication
3. **Role-Based Access Control** - Admin vs User permissions
4. **Smart Cart Logic** - Prevents duplicate items, auto-increments quantities
5. **Bug Fixes** - Corrected price filtering and update operations

## 🔮 Potential Future Enhancements

- Order checkout system
- User profile management
- Product reviews and ratings
- Wishlist functionality
- Advanced search and filtering

## 📝 Backend Project Structure

```
src/main/java/org/yearup/
├── controllers/
│   ├── CategoriesController.java      # Category CRUD endpoints
│   ├── ProductsController.java        # Product CRUD endpoints (Bug fixes)
│   └── ShoppingCartController.java    # Shopping cart endpoints
├── data/
│   ├── CategoryDao.java               # Category DAO interface
│   ├── ProductDao.java                # Product DAO interface
│   └── ShoppingCartDao.java           # Shopping cart DAO interface
├── data/mysql/
│   ├── MySqlCategoryDao.java          # Category database operations
│   ├── MySqlProductDao.java           # Product database operations (Price filter fix)
│   └── MySqlShoppingCartDao.java      # Shopping cart operations (Smart add logic)
└── models/
    ├── Category.java                  # Category entity
    ├── Product.java                   # Product entity
    ├── ShoppingCart.java              # Shopping cart container
    └── ShoppingCartItem.java          # Cart item entity
```

## 👤 Author

**Rich**
- Capstone Project - Year Up Program
- Full-Stack Java Development

## 📄
