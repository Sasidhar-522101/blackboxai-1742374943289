# Grocery Delivery App Backend

A Spring Boot-based backend application for a grocery delivery platform.

## Features

- User authentication and authorization with JWT
- Email verification with OTP
- Product management
- Order processing and tracking
- Real-time order updates using WebSocket
- Payment processing
- Email notifications
- Swagger API documentation

## Tech Stack

- Java 17
- Spring Boot 2.7.0
- Spring Security
- Spring Data JPA
- Spring WebSocket
- H2 Database
- JWT Authentication
- OpenAPI/Swagger Documentation
- Maven

## Prerequisites

- JDK 17
- Maven
- IDE (IntelliJ IDEA/Eclipse/VS Code)

## Setup & Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/grocery-delivery-app.git
cd grocery-delivery-app/backend
```

2. Configure application.properties:
   - Update email configuration
   - Update JWT secret
   - Configure other properties as needed

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://localhost:8080/api/swagger-ui.html
```

## API Endpoints

### Authentication
- POST `/api/auth/register` - Register new user
- POST `/api/auth/login` - User login
- POST `/api/auth/verify-otp` - Verify email OTP
- POST `/api/auth/resend-otp` - Resend OTP

### Products
- GET `/api/products` - Get all products
- GET `/api/products/{id}` - Get product by ID
- POST `/api/products` - Create new product (Admin)
- PUT `/api/products/{id}` - Update product (Admin)
- DELETE `/api/products/{id}` - Delete product (Admin)

### Orders
- POST `/api/orders` - Create new order
- GET `/api/orders/{id}` - Get order by ID
- GET `/api/orders/user` - Get user's orders
- PUT `/api/orders/{id}/status` - Update order status (Admin)
- POST `/api/orders/{id}/cancel` - Cancel order

### Payments
- POST `/api/payments/process/{orderId}` - Process payment
- GET `/api/payments/order/{orderId}/bill` - Get digital bill

## WebSocket Endpoints

- `/ws` - WebSocket endpoint
- `/topic/order/{orderId}` - Order status updates
- `/topic/order/{orderId}/location` - Delivery location updates

## Database

The application uses H2 in-memory database by default. The H2 console is available at:
```
http://localhost:8080/api/h2-console
```

Database credentials (default):
- JDBC URL: `jdbc:h2:mem:grocerydb`
- Username: `sa`
- Password: ` ` (empty)

## Testing

Run tests using:
```bash
mvn test
```

## Security

- JWT-based authentication
- Role-based authorization (USER, ADMIN)
- Password encryption using BCrypt
- Email verification required for new accounts
- CORS configuration for frontend integration

## Error Handling

The application includes global exception handling for:
- Invalid authentication
- Resource not found
- Validation errors
- Payment processing errors
- Out of stock errors
- General application errors

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
