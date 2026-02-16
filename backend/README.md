# 🛠️ Smart Delivery Backend

The backbone of the Smart Delivery ecosystem, providing a high-performance REST API and real-time communication hub.

## 🚀 Technologies

- **Node.js & Express:** Modern web framework for high-concurrency.
- **Socket.IO:** Powers the real-time driver tracking and status updates.
- **MongoDB & Mongoose:** Scalable document database for orders and users.
- **JWT:** Secure, stateless session management.
- **Redis:** (Optional) Ready-to-use caching layer for ultra-low latency.

## 📁 Project Structure

```text
src/
├── config/       # DB and Redis configurations
├── controllers/  # Business logic for Auth and Orders
├── middleware/   # JWT verification and role-based access
├── models/       # Mongoose schemas (User, Order)
├── routes/       # API endpoint definitions
└── services/     # External integrations or shared logic
```

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Create a new user (Customer/Driver) |
| `POST` | `/api/auth/login` | Authenticate and receive JWT |

### Orders
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/orders` | Fetch orders (filtered by role) |
| `POST` | `/api/orders` | Place a new delivery order |
| `PATCH` | `/api/orders/:id` | Update order status (Driver only) |

## 🔌 Socket.io Events

The backend listens and emits specific events to facilitate real-time tracking:

### Inbound Events
- `join`: User joins a private room using their `userId`.
- `updateLocation`: Drivers emit their `{ lat, lng }` to this event.

### Outbound Events
- `driverLocation:{driverId}`: Backend broadcasts the location to tracking clients.
- `orderUpdate`: Emitted when an order changes status.

## 🛠️ Performance Tuning

The backend is configured to support Redis for location caching. To enable, uncomment `connectRedis()` in `server.js` and provide the `REDIS_URL` in your environment variables.

---
*Smart Delivery Backend - Built for Speed.*
