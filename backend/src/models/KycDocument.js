const mongoose = require('mongoose');

const KycDocumentSchema = new mongoose.Schema({
    driver: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true
    },
    cin: {
        filename: { type: String },
        path: { type: String },
        uploadedAt: { type: Date }
    },
    permis: {
        filename: { type: String },
        path: { type: String },
        uploadedAt: { type: Date }
    },
    selfie: {
        filename: { type: String },
        path: { type: String },
        uploadedAt: { type: Date }
    },
    status: {
        type: String,
        enum: ['PENDING', 'APPROVED', 'REJECTED'],
        default: 'PENDING'
    },
    rejectionReason: {
        type: String
    }
}, { timestamps: true });

module.exports = mongoose.model('KycDocument', KycDocumentSchema);
