'use client';

import { create } from 'zustand';
import { authApi } from './api';

interface User {
    id: number;
    email: string;
    name: string;
    role: string;
    tenant: {
        id: number;
        name: string;
        shopifyDomain: string | null;
        shopifyConnected: boolean;
        syncStatus: string;
    };
}

interface AuthState {
    token: string | null;
    user: User | null;
    isLoading: boolean;
    isAuthenticated: boolean;
    initialized: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (name: string, email: string, password: string, companyName: string) => Promise<void>;
    logout: () => void;
    initialize: () => void;
}

export const useAuthStore = create<AuthState>()((set, get) => ({
    token: null,
    user: null,
    isLoading: false,
    isAuthenticated: false,
    initialized: false,

    initialize: () => {
        if (typeof window === 'undefined') return;

        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');

        if (token && userStr) {
            try {
                const user = JSON.parse(userStr);
                set({ token, user, isAuthenticated: true, initialized: true });
            } catch {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                set({ initialized: true });
            }
        } else {
            set({ initialized: true });
        }
    },

    login: async (email: string, password: string) => {
        set({ isLoading: true });
        try {
            const response = await authApi.login({ email, password });
            if (response.success) {
                const { token, user } = response.data;
                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(user));
                set({
                    token,
                    user,
                    isAuthenticated: true,
                    isLoading: false,
                });
            } else {
                set({ isLoading: false });
                throw new Error(response.message || 'Login failed');
            }
        } catch (error: any) {
            set({ isLoading: false });
            throw new Error(error.response?.data?.message || error.message || 'Login failed');
        }
    },

    register: async (name: string, email: string, password: string, companyName: string) => {
        set({ isLoading: true });
        try {
            const response = await authApi.register({ name, email, password, companyName });
            if (response.success) {
                const { token, user } = response.data;
                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(user));
                set({
                    token,
                    user,
                    isAuthenticated: true,
                    isLoading: false,
                });
            } else {
                set({ isLoading: false });
                throw new Error(response.message || 'Registration failed');
            }
        } catch (error: any) {
            set({ isLoading: false });
            throw new Error(error.response?.data?.message || error.message || 'Registration failed');
        }
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        set({
            token: null,
            user: null,
            isAuthenticated: false,
            isLoading: false,
        });
    },
}));
