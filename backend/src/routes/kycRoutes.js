const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/authMiddleware');
const upload = require('../middleware/uploadMiddleware');
const {
    uploadDocuments,
    getKycStatus,
    getAllKyc,
    reviewKyc
} = require('../controllers/kycController');

// Driver routes
router.get('/status', protect, getKycStatus);
router.post(
    '/upload',
    protect,
    upload.fields([
        { name: 'cin', maxCount: 1 },
        { name: 'permis', maxCount: 1 },
        { name: 'selfie', maxCount: 1 }
    ]),
    uploadDocuments
);

// Admin routes
router.get('/all', protect, getAllKyc);
router.put('/:driverId/review', protect, reviewKyc);

module.exports = router;
