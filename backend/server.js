require('dotenv').config();
const express = require('express');
const http = require('http');
const { Server } = require("socket.io");
const cors = require('cors');
const connectDB = require('./src/config/db');
const { connectRedis } = require('./src/config/redis');
const authRoutes = require('./src/routes/authRoutes');
const orderRoutes = require('./src/routes/orderRoutes');
const User = require('./src/models/User');

// Connect to Database
connectDB();
// connectRedis();

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

app.set('socketio', io);

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/orders', orderRoutes);

// Basic Route
app.get('/', (req, res) => {
    res.send('Smart Delivery Backend Running');
});

// Socket.io Connection
io.on('connection', (socket) => {
    console.log('User connected:', socket.id);

    // Join a room based on user ID (for targeted notifications)
    socket.on('join', (userId) => {
        socket.join(userId);
        console.log(`User ${userId} joined room`);
    });

    // Driver updates location
    socket.on('updateLocation', async ({ driverId, coordinates }) => {
        // coordinates: { lat, lng }
        // Update in Redis/Mongo (Optimized: just Redis usually, but for demo maybe Mongo or just emit)

        // Emit to anyone tracking this driver (e.g., active customer)
        // Ideally we should know which order is active to notify the specific customer.
        // For simplicity, we can just emit to a "tracking_driverId" room if we had one,
        // or if the client knows the driverId, they can listen to events.

        io.emit(`driverLocation:${driverId}`, coordinates);
        console.log(`Location update from ${driverId}:`, coordinates);

        try {
            await User.findByIdAndUpdate(driverId, {
                currentLocation: {
                    type: 'Point',
                    coordinates: [coordinates.lng, coordinates.lat]
                }
            });
        } catch (err) {
            console.error('Error updating driver location:', err);
        }
    });

    socket.on('disconnect', () => {
        console.log('User disconnected:', socket.id);
    });
});

const PORT = process.env.PORT || 3000;

server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

