// API Service
class ApiService {
    constructor() {
        this.baseUrl = '/api/v1';
    }

    async get(endpoint) {
        return fetch(`${this.baseUrl}${endpoint}`).then(r => r.json());
    }

    async post(endpoint, data) {
        return fetch(`${this.baseUrl}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(r => r.json());
    }
}

const apiService = new ApiService();
