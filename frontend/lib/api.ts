import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add auth token
api.interceptors.request.use(
    (config) => {
        if (typeof window !== 'undefined') {
            const token = localStorage.getItem('token');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor to handle errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Clear token and redirect to login
            if (typeof window !== 'undefined') {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

// Auth API
export const authApi = {
    register: async (data: { name: string; email: string; password: string; companyName: string }) => {
        const response = await api.post('/auth/register', data);
        return response.data;
    },

    login: async (data: { email: string; password: string }) => {
        const response = await api.post('/auth/login', data);
        return response.data;
    },

    me: async () => {
        const response = await api.get('/auth/me');
        return response.data;
    },
};

// Shopify API
export const shopifyApi = {
    connect: async (data: { shopDomain: string; accessToken: string }) => {
        const response = await api.put('/shopify/connect', data);
        return response.data;
    },

    getStatus: async () => {
        const response = await api.get('/shopify/status');
        return response.data;
    },

    sync: async () => {
        const response = await api.post('/shopify/sync');
        return response.data;
    },
};

// Dashboard API
export const dashboardApi = {
    getData: async () => {
        const response = await api.get('/dashboard');
        return response.data;
    },

    getStats: async () => {
        const response = await api.get('/dashboard/stats');
        return response.data;
    },

    getOrdersByDate: async (startDate: string, endDate: string) => {
        const response = await api.get('/dashboard/orders-by-date', {
            params: { startDate, endDate },
        });
        return response.data;
    },

    getTopCustomers: async (limit = 5) => {
        const response = await api.get('/dashboard/top-customers', {
            params: { limit },
        });
        return response.data;
    },

    getRevenueTrends: async (months = 12) => {
        const response = await api.get('/dashboard/revenue-trends', {
            params: { months },
        });
        return response.data;
    },

    getTopProducts: async (limit = 10) => {
        const response = await api.get('/dashboard/top-products', {
            params: { limit },
        });
        return response.data;
    },
};

// Customers API
export const customersApi = {
    getAll: async (params?: { page?: number; size?: number; search?: string }) => {
        const response = await api.get('/customers', { params });
        return response.data;
    },

    getById: async (id: number) => {
        const response = await api.get(`/customers/${id}`);
        return response.data;
    },
};

// Orders API
export const ordersApi = {
    getAll: async (params?: { page?: number; size?: number; startDate?: string; endDate?: string }) => {
        const response = await api.get('/orders', { params });
        return response.data;
    },

    getById: async (id: number) => {
        const response = await api.get(`/orders/${id}`);
        return response.data;
    },
};

// Products API
export const productsApi = {
    getAll: async (params?: { page?: number; size?: number; search?: string }) => {
        const response = await api.get('/products', { params });
        return response.data;
    },

    getById: async (id: number) => {
        const response = await api.get(`/products/${id}`);
        return response.data;
    },

    getVendors: async () => {
        const response = await api.get('/products/vendors');
        return response.data;
    },

    getTypes: async () => {
        const response = await api.get('/products/types');
        return response.data;
    },
};

export default api;
