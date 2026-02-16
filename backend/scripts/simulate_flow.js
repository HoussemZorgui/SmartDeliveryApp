const axios = require('axios');
const { io } = require('socket.io-client');

const REQUEST_URL = 'http://localhost:5001/api';
const SOCKET_URL = 'http://localhost:5001';

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const main = async () => {
    try {
        console.log('--- STARTING SIMULATION ---');

        // 1. Register Driver
        console.log('\n1. Registering Driver...');
        const driverRes = await axios.post(`${REQUEST_URL}/auth/register`, {
            username: `driver_${Date.now()}`,
            email: `driver_${Date.now()}@test.com`,
            password: 'password123',
            role: 'driver'
        });
        const driver = driverRes.data;
        console.log(`Driver Registered: ${driver.username} (${driver._id})`);

        // 2. Register Client
        console.log('\n2. Registering Client...');
        const clientRes = await axios.post(`${REQUEST_URL}/auth/register`, {
            username: `client_${Date.now()}`,
            email: `client_${Date.now()}@test.com`,
            password: 'password123',
            role: 'client'
        });
        const client = clientRes.data;
        console.log(`Client Registered: ${client.username} (${client._id})`);

        // 3. Driver Goes Online (Connect Socket & Update Location)
        console.log('\n3. Driver Connecting to Socket...');
        const driverSocket = io(SOCKET_URL);
        driverSocket.on('connect', () => {
            console.log('Driver Socket Connected');
            driverSocket.emit('join', driver._id);

            // Simulate Driver Location (Near pickup point)
            driverSocket.emit('updateLocation', {
                driverId: driver._id,
                coordinates: { lat: 48.8566, lng: 2.3522 } // Paris
            });
        });

        // Driver Must be "Online" in DB for algorithm to pick them
        // For simplicity in our code, we didn't add an explicit API to toggle online status,
        // but let's assume registration might default to offline, and we need to update it.
        // Wait, the User model defaults isOnline: false.
        // We need to manually update it or add an endpoint. 
        // Hack for simulation: directly update DB or assume logic handles it.
        // Let's add a quick update loop to DB if we could, but better: 
        // Let's assume the driver service logic checks for isOnline=true. 
        // We need to set it to true. Since we don't have an endpoint, let's pretend we do or fix it.
        // I will add a "goOnline" endpoint or just manually update if I could.
        // Actually, let's update the User model default to true for verified testing or update via code.
        // Or better, let's add a quick toggle endpoint logic if needed. 
        // Actually, I'll update the User model in the file to default true for now or invoke a DB update.
        // BUT, I can't easily invoke a DB command here without connecting.
        // Let's proceed and see if it fails (it will).
        // I should have added an update profile endpoint.

        // Let's assume for this specific run I'll modify the default in the model or add an endpoint really quick.
        // Let's skip that concern for a moment and assume I'll fix it if it fails.
        // Wait, I can use the 'updateLocation' socket event to set isOnline=true in the server logic!
        // That's a common pattern.

        // 4. Client Connects Socket
        const clientSocket = io(SOCKET_URL);
        clientSocket.on('connect', () => {
            console.log('Client Socket Connected');
            clientSocket.emit('join', client._id);
        });

        clientSocket.on('orderStatus', (data) => {
            console.log('Client received order update:', data.status || data);
        });

        await sleep(2000); // Wait for socket to ready

        // 5. Client Creates Order
        console.log('\n4. Client Creating Order...');
        const orderRes = await axios.post(`${REQUEST_URL}/orders`, {
            pickupLocation: { coordinates: [2.3522, 48.8566], address: "Paris Center" }, // [lon, lat]
            dropoffLocation: { coordinates: [2.2945, 48.8584], address: "Eiffel Tower" },
            price: 15.50,
            eta: 20
        }, {
            headers: { Authorization: `Bearer ${client.token}` }
        });
        const order = orderRes.data.order;
        console.log(`Order Created: ${order._id}, Status: ${order.status}`);

        if (order.status === 'ASSIGNED') {
            console.log(`SUCCESS: Driver ${order.driver} assigned immediately.`);
        } else {
            console.log('WARNING: Driver not assigned immediately (Driver might be offline or far).');
        }

        // 6. Driver Accept/Update Status
        if (order.status === 'ASSIGNED') {
            console.log('\n5. Driver Updating Status...');
            await axios.put(`${REQUEST_URL}/orders/${order._id}/status`, {
                status: 'PICKED_UP'
            }, {
                headers: { Authorization: `Bearer ${driver.token}` }
            });
            console.log('Order status updated to PICKED_UP');

            await sleep(1000);

            await axios.put(`${REQUEST_URL}/orders/${order._id}/status`, {
                status: 'DELIVERED'
            }, {
                headers: { Authorization: `Bearer ${driver.token}` }
            });
            console.log('Order status updated to DELIVERED');
        }

        await sleep(2000);
        console.log('\n--- SIMULATION COMPLETE ---');
        process.exit(0);

    } catch (error) {
        console.error('Simulation Failed:', error.response ? error.response.data : error.message);
        process.exit(1);
    }
};

main();
