# Order History API Documentation

## Overview
This document outlines the implementation of Order History APIs following the patterns from `AlbumMngApi.java` and `UserInfoApi.java`. Two main APIs have been created:

1. **OrderApi.java** - User-facing order history API (updated)
2. **OrderMngApi.java** - Admin order management API (new)

## 1. User Order History API (`/api/v1/orders`)

### Features
- JWT token validation with proper user access control
- Users can only access their own orders (unless admin)
- Follows the validation patterns from `UserInfoApi.java`

### Endpoints

#### `GET /api/v1/orders`
- **Description**: Get current user's order history
- **Authentication**: Bearer token required
- **Authorization**: User can access their own orders
- **Response**: List of user's orders

#### `GET /api/v1/orders/{id}`
- **Description**: Get specific order by ID
- **Authentication**: Bearer token required
- **Authorization**: User can only access their own orders (or admin can access any)
- **Response**: Order details

#### `GET /api/v1/orders/user/{userId}`
- **Description**: Get orders by user ID
- **Authentication**: Bearer token required
- **Authorization**: User can access their own orders or admin can access any
- **Response**: List of orders for the specified user

#### `POST /api/v1/orders`
- **Description**: Create a new order
- **Authentication**: Bearer token required
- **Authorization**: User can only create orders for themselves (unless admin)
- **Request Body**: OrderRequest
- **Response**: Created order

#### `POST /api/v1/orders/{id}/payment`
- **Description**: Generate payment URL for an order
- **Authentication**: Bearer token required
- **Authorization**: User can only generate payment for their own orders
- **Parameters**: `baseUrl` (query parameter)
- **Response**: Payment URL

### Security Features
- `validateAndGetUser(token)` - Validates token and returns authenticated user
- `validateUserAccess(token, userId)` - Ensures user can only access their own data or admin can access any
- `hasAdminRole(user)` - Checks if user has admin privileges

## 2. Admin Order Management API (`/api/admin/orders`)

### Features
- Admin-only access with proper JWT validation
- Comprehensive order management capabilities
- Search and filtering functionality
- Order statistics and reporting
- Follows the patterns from `AlbumMngApi.java`

### Endpoints

#### `GET /api/admin/orders`
- **Description**: Get all orders
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Response**: List of all orders

#### `GET /api/admin/orders/{id}`
- **Description**: Get order by ID
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Response**: Order details

#### `GET /api/admin/orders/user/{userId}`
- **Description**: Get orders by user ID
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Response**: List of orders for the specified user

#### `GET /api/admin/orders/status/{status}`
- **Description**: Get orders by status
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Parameters**: `status` (PENDING, PAID, CANCELLED)
- **Response**: List of orders with the specified status

#### `GET /api/admin/orders/date-range`
- **Description**: Get orders by date range
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Parameters**: 
  - `startDate` (YYYY-MM-DD format)
  - `endDate` (YYYY-MM-DD format)
- **Response**: List of orders in the date range

#### `GET /api/admin/orders/code/{orderCode}`
- **Description**: Get order by order code
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Response**: Order details

#### `PUT /api/admin/orders/{id}/status`
- **Description**: Update order status
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Parameters**: 
  - `status` (required)
  - `transactionId` (optional)
- **Response**: Updated order

#### `GET /api/admin/orders/statistics`
- **Description**: Get order statistics
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Response**: Order statistics including:
  - Total orders
  - Pending orders
  - Paid orders
  - Cancelled orders
  - Total revenue

#### `GET /api/admin/orders/search`
- **Description**: Search orders by user name, email, or order code
- **Authentication**: Bearer token required
- **Authorization**: Admin role required
- **Parameters**: `query` (search term)
- **Response**: List of matching orders

#### `GET /api/admin/orders/token-debug`
- **Description**: Debug endpoint for token verification
- **Authentication**: Bearer token required
- **Response**: Token information and authorization status

### Security Features
- `validatePermission(token)` - Validates token and ensures admin role
- `hasAdminRole(user)` - Checks if user has admin privileges

## Response Format

### OrderResponse DTO
```java
{
    "id": 1,
    "orderCode": "abc12345",
    "userId": 123,
    "subscriptionId": 456,
    "subscriptionName": "Premium Plan",
    "originalAmount": 99.99,
    "discountAmount": 10.00,
    "finalAmount": 89.99,
    "status": "PAID",
    "voucherCode": "SAVE10",
    "voucherDiscount": 10.0,
    "createdAt": "2024-01-15T10:30:00",
    "paidAt": "2024-01-15T10:35:00"
}
```

## CORS Configuration
Both APIs are configured with CORS support for:
- `http://localhost:3000`
- `http://localhost:3001`

## Dependencies Used
- Spring Boot REST Controllers
- JWT Token validation (JwtUtil)
- Lombok for boilerplate reduction
- Existing OrderService and UserService
- Repository pattern for data access

## Error Handling
- Proper exception handling with BadRequestException
- Informative error messages
- HTTP status codes following REST conventions

## Implementation Notes
1. The user API ensures users can only access their own order history unless they have admin privileges
2. The admin API provides comprehensive order management capabilities
3. Both APIs follow the established patterns in the codebase
4. Token validation is consistent with other APIs in the project
5. All endpoints include proper documentation with JavaDoc comments

This implementation provides a complete order history management system with proper security, following the established patterns in your Spring Boot application. 