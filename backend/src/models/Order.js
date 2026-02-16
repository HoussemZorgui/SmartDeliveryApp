const mongoose = require('mongoose');

const OrderSchema = new mongoose.Schema({
    customer: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    driver: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User'
    },
    pickupLocation: {
        type: {
            type: String,
            enum: ['Point'],
            default: 'Point'
        },
        coordinates: {
            type: [Number], // [lon, lat]
            required: true
        },
        address: String
    },
    dropoffLocation: {
        type: {
            type: String,
            enum: ['Point'],
            default: 'Point'
        },
        coordinates: {
            type: [Number], // [lon, lat]
            required: true
        },
        address: String
    },
    price: {
        type: Number,
        required: true
    },
    eta: {
        type: Number, // in minutes
    },
    status: {
        type: String,
        enum: ['PENDING', 'ASSIGNED', 'PICKED_UP', 'DELIVERED', 'CANCELLED'],
        default: 'PENDING'
    }
}, { timestamps: true });

OrderSchema.index({ pickupLocation: '2dsphere' });

module.exports = mongoose.model('Order', OrderSchema);
