const Order = require('../models/Order');
const { findNearestDriver } = require('../services/driverService');

// @desc    Create new order
// @route   POST /api/orders
// @access  Private (Client)
const createOrder = async (req, res) => {
    const { pickupLocation, dropoffLocation, price, eta } = req.body;

    try {
        const order = await Order.create({
            customer: req.user._id,
            pickupLocation,
            dropoffLocation,
            price,
            eta,
            status: 'PENDING'
        });

        // Attempt to find a driver
        const driver = await findNearestDriver(pickupLocation.coordinates);

        if (driver) {
            order.driver = driver._id;
            order.status = 'ASSIGNED';
            await order.save();

            // Notify Driver via Socket
            const io = req.app.get('socketio');
            if (io) {
                io.to(driver._id.toString()).emit('newOrder', order);
            }

            // Notify Customer
            const io2 = req.app.get('socketio');
            if (io2) {
                io2.to(req.user._id.toString()).emit('orderStatus', order);
            }

            res.status(201).json({ message: 'Order created and driver assigned', order });
        } else {
            res.status(201).json({ message: 'Order created, looking for drivers...', order });
        }

    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @desc    Get my orders
// @route   GET /api/orders/myorders
// @access  Private
const getMyOrders = async (req, res) => {
    try {
        let orders;
        if (req.user.role === 'driver') {
            orders = await Order.find({ driver: req.user._id }).sort({ createdAt: -1 });
        } else {
            orders = await Order.find({ customer: req.user._id }).sort({ createdAt: -1 });
        }
        res.json(orders);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @desc    Update order status
// @route   PUT /api/orders/:id/status
// @access  Private (Driver)
const updateOrderStatus = async (req, res) => {
    const { status } = req.body;
    try {
        const order = await Order.findById(req.params.id);
        if (!order) return res.status(404).json({ message: 'Order not found' });

        if (order.driver.toString() !== req.user._id.toString()) {
            return res.status(401).json({ message: 'Not authorized' });
        }

        order.status = status;
        await order.save();

        // Notify Customer
        const io = req.app.get('socketio');
        if (io) {
            io.to(order.customer.toString()).emit('orderStatus', {
                orderId: order._id,
                status: order.status,
                driverId: req.user._id
            });
        }

        res.json(order);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
}

module.exports = {
    createOrder,
    getMyOrders,
    updateOrderStatus
};
