import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 100,
    duration: '5s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:9087';

export default function () {
    const payload = JSON.stringify({
        productId: '7edd93cf-5287-4728-9400-ea87d2380e6c',
        quantity: 1,
        price: 5000

    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(`${BASE_URL}/order/create`, payload, params);

    check(response, {
        'success OR out of stock': (r) => r.status === 200 || r.status === 409,
        'response body is valid': (res) =>
            res.status === 200 ? res.body.includes('Order created successfully') :
            res.body.includes('Insufficient stock for product ID'),
    });
}
