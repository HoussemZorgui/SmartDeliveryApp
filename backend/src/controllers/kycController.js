const KycDocument = require('../models/KycDocument');
const path = require('path');

// @desc    Upload KYC documents (CIN, Permis, Selfie)
// @route   POST /api/kyc/upload
// @access  Private (Driver)
const uploadDocuments = async (req, res) => {
    try {
        if (req.user.role !== 'driver') {
            return res.status(403).json({ message: 'Only drivers can submit KYC documents' });
        }

        let kyc = await KycDocument.findOne({ driver: req.user._id });

        if (!kyc) {
            kyc = new KycDocument({ driver: req.user._id });
        }

        // If status was REJECTED, allow re-upload
        if (kyc.status === 'APPROVED') {
            return res.status(400).json({ message: 'KYC already approved. Cannot re-upload.' });
        }

        const files = req.files;

        if (files.cin && files.cin[0]) {
            kyc.cin = {
                filename: files.cin[0].filename,
                path: files.cin[0].path,
                uploadedAt: new Date()
            };
        }
        if (files.permis && files.permis[0]) {
            kyc.permis = {
                filename: files.permis[0].filename,
                path: files.permis[0].path,
                uploadedAt: new Date()
            };
        }
        if (files.selfie && files.selfie[0]) {
            kyc.selfie = {
                filename: files.selfie[0].filename,
                path: files.selfie[0].path,
                uploadedAt: new Date()
            };
        }

        kyc.status = 'PENDING';
        await kyc.save();

        res.status(201).json({
            message: 'Documents uploaded successfully. Under review.',
            status: kyc.status
        });

    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @desc    Get my KYC status
// @route   GET /api/kyc/status
// @access  Private (Driver)
const getKycStatus = async (req, res) => {
    try {
        const kyc = await KycDocument.findOne({ driver: req.user._id });
        if (!kyc) {
            return res.json({ status: 'NOT_SUBMITTED', documents: {} });
        }
        res.json({
            status: kyc.status,
            rejectionReason: kyc.rejectionReason || null,
            documents: {
                cin: kyc.cin ? { uploaded: true, uploadedAt: kyc.cin.uploadedAt } : { uploaded: false },
                permis: kyc.permis ? { uploaded: true, uploadedAt: kyc.permis.uploadedAt } : { uploaded: false },
                selfie: kyc.selfie ? { uploaded: true, uploadedAt: kyc.selfie.uploadedAt } : { uploaded: false },
            }
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @desc    Admin: Get all pending KYC submissions
// @route   GET /api/kyc/all
// @access  Private (Admin) - for future admin panel
const getAllKyc = async (req, res) => {
    try {
        const kycList = await KycDocument.find({}).populate('driver', 'username email');
        res.json(kycList);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @desc    Admin: Approve or Reject a KYC submission
// @route   PUT /api/kyc/:driverId/review
// @access  Private (Admin) - for future admin panel
const reviewKyc = async (req, res) => {
    const { status, rejectionReason } = req.body;
    try {
        const kyc = await KycDocument.findOne({ driver: req.params.driverId });
        if (!kyc) return res.status(404).json({ message: 'KYC not found' });

        kyc.status = status; // 'APPROVED' or 'REJECTED'
        if (status === 'REJECTED') {
            kyc.rejectionReason = rejectionReason;
        }
        await kyc.save();
        res.json({ message: `KYC ${status}`, kyc });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

module.exports = { uploadDocuments, getKycStatus, getAllKyc, reviewKyc };
