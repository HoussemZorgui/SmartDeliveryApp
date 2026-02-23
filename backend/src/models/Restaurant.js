const mongoose = require('mongoose');

const ReviewSchema = new mongoose.Schema({
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    username: String,
    rating: { type: Number, required: true, min: 1, max: 5 },
    comment: String,
    createdAt: { type: Date, default: Date.now }
});

const MenuItemSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: String,
    price: { type: Number, required: true },
    image: String,
    category: String, // Entrée, Plat, Dessert, Boisson
    isAvailable: { type: Boolean, default: true }
});

const RestaurantSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String },
    address: { type: String, required: true },
    location: {
        type: { type: String, default: 'Point' },
        coordinates: { type: [Number], required: true } // [lng, lat]
    },
    images: [{ type: String }], // URLs des images
    logo: { type: String },
    phone: { type: String },
    cuisineType: [String], // Italien, Tunisien, Fast Food...
    rating: { type: Number, default: 0 },
    numReviews: { type: Number, default: 0 },
    reviews: [ReviewSchema],
    menu: [MenuItemSchema],
    openingHours: {
        open: String, // ex: "09:00"
        close: String // ex: "23:00"
    },
    isFeatured: { type: Boolean, default: false },
    deliveryFee: { type: Number, default: 0 }
}, { timestamps: true });

RestaurantSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('Restaurant', RestaurantSchema);
