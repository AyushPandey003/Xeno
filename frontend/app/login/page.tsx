'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/lib/auth-store';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, Mail, Lock, Sparkles } from 'lucide-react';

export default function LoginPage() {
    const router = useRouter();
    const { login, isLoading, isAuthenticated, initialize, initialized } = useAuthStore();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {
        initialize();
    }, [initialize]);

    useEffect(() => {
        if (initialized && isAuthenticated) {
            router.replace('/dashboard');
        }
    }, [initialized, isAuthenticated, router]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email || !password) {
            toast.error('Please fill in all fields');
            return;
        }

        try {
            await login(email, password);
            toast.success('Login successful!');
            router.replace('/dashboard');
        } catch (error: any) {
            toast.error(error.message || 'Login failed');
        }
    };

    if (!initialized) {
        return (
            <div className="min-h-screen flex items-center justify-center gradient-bg">
                <Loader2 className="h-10 w-10 animate-spin text-blue-600 dark:text-blue-400" />
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center gradient-bg p-4">
            {/* Decorative elements */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-20 left-10 w-72 h-72 bg-blue-200/30 dark:bg-blue-500/20 rounded-full blur-3xl animate-float" />
                <div className="absolute bottom-20 right-10 w-96 h-96 bg-blue-300/20 dark:bg-blue-400/10 rounded-full blur-3xl animate-float" style={{ animationDelay: '-3s' }} />
            </div>

            <div className="w-full max-w-md relative z-10">
                <Card className="border-gray-200 dark:border-gray-700 bg-white/90 dark:bg-gray-800/90 backdrop-blur-md shadow-2xl">
                    <CardHeader className="space-y-4 text-center px-8 pt-10 pb-8">
                        <div className="mx-auto w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-600 to-blue-400 flex items-center justify-center shadow-lg shadow-blue-500/30 dark-mode-glow">
                            <Sparkles className="w-8 h-8 text-white" />
                        </div>
                        <CardTitle className="text-3xl font-bold text-gray-900 dark:text-white">Welcome back</CardTitle>
                        <CardDescription className="text-gray-600 dark:text-gray-300 text-base">
                            Sign in to your Xeno account
                        </CardDescription>
                    </CardHeader>
                    <form onSubmit={handleSubmit}>
                        <CardContent className="space-y-6 px-8">
                            <div className="space-y-2">
                                <Label htmlFor="email" className="text-sm font-medium text-gray-700 dark:text-gray-200">Email</Label>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400 dark:text-gray-500" />
                                    <Input
                                        id="email"
                                        type="email"
                                        placeholder="name@company.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="pl-10 h-11 border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder:text-gray-500 dark:placeholder:text-gray-400 focus:border-blue-500 focus:ring-blue-500"
                                        disabled={isLoading}
                                    />
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="password" className="text-sm font-medium text-gray-700 dark:text-gray-200">Password</Label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400 dark:text-gray-500" />
                                    <Input
                                        id="password"
                                        type="password"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="pl-10 h-11 border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder:text-gray-500 dark:placeholder:text-gray-400 focus:border-blue-500 focus:ring-blue-500"
                                        disabled={isLoading}
                                    />
                                </div>
                            </div>
                        </CardContent>
                        <CardContent className="px-8 pb-10 pt-2 space-y-4">
                            <Button
                                type="submit"
                                className="w-full h-11 bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 text-white font-medium shadow-lg shadow-blue-500/30"
                                disabled={isLoading}
                            >
                                {isLoading ? (
                                    <>
                                        <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                                        Signing in...
                                    </>
                                ) : (
                                    'Sign in'
                                )}
                            </Button>
                            <p className="text-sm text-gray-600 dark:text-gray-300 text-center">
                                Don&apos;t have an account?{' '}
                                <Link href="/register" className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium transition-colors">
                                    Create one
                                </Link>
                            </p>
                        </CardContent>
                    </form>
                </Card>

                {/* Brand */}
                <p className="text-center mt-8 text-gray-500 dark:text-gray-400 text-sm">
                    Powered by <span className="gradient-text font-semibold">Xeno</span>
                </p>
            </div>
        </div>
    );
}
