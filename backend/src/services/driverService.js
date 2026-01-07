const User = require('../models/User');

const findNearestDriver = async (pickupCoordinates) => {
    // pickupCoordinates: [longitude, latitude]
    try {
        const drivers = await User.find({
            role: 'driver',
            isOnline: true,
            currentLocation: {
                $near: {
                    $geometry: {
                        type: 'Point',
                        coordinates: pickupCoordinates
                    },
                    $maxDistance: 5000 // 5km search radius
                }
            }
        }).limit(1);

        if (drivers.length > 0) {
            return drivers[0];
        } else {
            return null;
        }
    } catch (error) {
        console.error('Error finding driver:', error);
        return null;
    }
};

module.exports = {
    findNearestDriver
};
