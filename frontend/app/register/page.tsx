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
import { Loader2, Mail, Lock, User, Building2, Sparkles } from 'lucide-react';

export default function RegisterPage() {
    const router = useRouter();
    const { register, isLoading, isAuthenticated, initialize, initialized } = useAuthStore();
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [companyName, setCompanyName] = useState('');

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

        if (!name || !email || !password || !companyName) {
            toast.error('Please fill in all fields');
            return;
        }

        if (password.length < 6) {
            toast.error('Password must be at least 6 characters');
            return;
        }

        try {
            await register(name, email, password, companyName);
            toast.success('Account created successfully!');
            router.replace('/dashboard');
        } catch (error: any) {
            toast.error(error.message || 'Registration failed');
        }
    };

    if (!initialized) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-50">
                <Loader2 className="h-10 w-10 animate-spin text-blue-600" />
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4">
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-20 right-10 w-72 h-72 bg-blue-200/30 rounded-full blur-3xl animate-float" />
                <div className="absolute bottom-20 left-10 w-96 h-96 bg-blue-300/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '-3s' }} />
            </div>

            <div className="w-full max-w-lg relative z-10">
                <Card className="border-gray-200 bg-white/80 backdrop-blur-sm shadow-2xl">
                    <CardHeader className="space-y-4 text-center px-8 pt-10 pb-8">
                        <div className="mx-auto w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-600 to-blue-400 flex items-center justify-center shadow-lg shadow-blue-500/30">
                            <Sparkles className="w-8 h-8 text-white" />
                        </div>
                        <CardTitle className="text-3xl font-bold text-gray-900">Get started</CardTitle>
                        <CardDescription className="text-gray-600 text-base">
                            Create your Xeno account
                        </CardDescription>
                    </CardHeader>
                    <form onSubmit={handleSubmit}>
                        <CardContent className="space-y-5 px-8">
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <div className="space-y-2">
                                    <Label htmlFor="name" className="text-sm font-medium text-gray-700">Full Name</Label>
                                    <div className="relative">
                                        <User className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                        <Input
                                            id="name"
                                            type="text"
                                            placeholder="John Doe"
                                            value={name}
                                            onChange={(e) => setName(e.target.value)}
                                            className="pl-10 h-11 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                                            disabled={isLoading}
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="company" className="text-sm font-medium text-gray-700">Company Name</Label>
                                    <div className="relative">
                                        <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                        <Input
                                            id="company"
                                            type="text"
                                            placeholder="Acme Inc"
                                            value={companyName}
                                            onChange={(e) => setCompanyName(e.target.value)}
                                            className="pl-10 h-11 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                                            disabled={isLoading}
                                        />
                                    </div>
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="email" className="text-sm font-medium text-gray-700">Email</Label>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                    <Input
                                        id="email"
                                        type="email"
                                        placeholder="name@company.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="pl-10 h-11 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                                        disabled={isLoading}
                                    />
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="password" className="text-sm font-medium text-gray-700">Password</Label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                    <Input
                                        id="password"
                                        type="password"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="pl-10 h-11 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                                        disabled={isLoading}
                                    />
                                </div>
                                <p className="text-xs text-gray-500 pl-1">Must be at least 6 characters</p>
                            </div>
                        </CardContent>
                        <CardContent className="px-8 pb-10 pt-2 space-y-4">
                            <Button
                                type="submit"
                                className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-medium shadow-lg shadow-blue-500/30"
                                disabled={isLoading}
                            >
                                {isLoading ? (
                                    <>
                                        <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                                        Creating account...
                                    </>
                                ) : (
                                    'Create account'
                                )}
                            </Button>
                            <p className="text-sm text-gray-600 text-center">
                                Already have an account?{' '}
                                <Link href="/login" className="text-blue-600 hover:text-blue-700 font-medium transition-colors">
                                    Sign in
                                </Link>
                            </p>
                        </CardContent>
                    </form>
                </Card>

                <p className="text-center mt-8 text-gray-500 text-sm">
                    Powered by <span className="gradient-text font-semibold">Xeno</span>
                </p>
            </div>
        </div>
    );
}
