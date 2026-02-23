const express = require('express');
const router = express.Router();
const { protect, admin } = require('../middleware/authMiddleware');
const {
    getAllUsers,
    updateUser,
    deleteUser,
    getAllOrders,
    createRestaurant
} = require('../controllers/adminController');

// Toutes les routes ici nécessitent d'être connecté ET admin
router.use(protect);
router.use(admin);

// Users
router.route('/users')
    .get(getAllUsers);

router.route('/users/:id')
    .put(updateUser)
    .delete(deleteUser);

// Orders
router.get('/orders', getAllOrders);

// Restaurants
router.post('/restaurants', createRestaurant);

module.exports = router;
