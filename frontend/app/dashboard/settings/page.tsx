'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { useAuthStore } from '@/lib/auth-store';
import { shopifyApi } from '@/lib/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import {
    Settings,
    Store,
    RefreshCw,
    Check,
    X,
    Loader2,
    Link as LinkIcon,
    Database,
    Clock,
} from 'lucide-react';

interface ConnectionStatus {
    connected: boolean;
    shopDomain: string | null;
    syncStatus: string;
    lastSyncAt: string | null;
    syncMessage: string | null;
    stats: {
        customersCount: number;
        productsCount: number;
        ordersCount: number;
    } | null;
}

export default function SettingsPage() {
    const { user } = useAuthStore();
    const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus | null>(null);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [connecting, setConnecting] = useState(false);
    const [shopDomain, setShopDomain] = useState('');
    const [accessToken, setAccessToken] = useState('');

    useEffect(() => {
        fetchStatus();
    }, []);

    const fetchStatus = async () => {
        try {
            const response = await shopifyApi.getStatus();
            if (response.success) {
                setConnectionStatus(response.data);
            }
        } catch (error) {
            console.error('Failed to fetch status:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleConnect = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!shopDomain || !accessToken) {
            toast.error('Please fill in all fields');
            return;
        }

        setConnecting(true);
        try {
            const response = await shopifyApi.connect({ shopDomain, accessToken });
            if (response.success) {
                toast.success('Shopify store connected successfully!');
                setShopDomain('');
                setAccessToken('');
                fetchStatus();
            } else {
                toast.error(response.message || 'Failed to connect');
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to connect to Shopify');
        } finally {
            setConnecting(false);
        }
    };

    const handleSync = async () => {
        setSyncing(true);
        try {
            const response = await shopifyApi.sync();
            if (response.success) {
                const data = response.data;
                if (data.success) {
                    toast.success(`Sync complete! Imported ${data.customersImported} customers, ${data.productsImported} products, ${data.ordersImported} orders.`);
                } else {
                    toast.error(data.message || 'Sync failed');
                }
                fetchStatus();
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to sync data');
        } finally {
            setSyncing(false);
        }
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6 max-w-4xl"
        >
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-3">
                    <Settings className="w-8 h-8 text-blue-600 dark:text-blue-400" />
                    Settings
                </h1>
                <p className="text-gray-600 dark:text-gray-300 mt-1">Manage your account and integrations</p>
            </div>

            {/* Account Info */}
            <Card>
                <CardHeader>
                    <CardTitle>Account Information</CardTitle>
                    <CardDescription>Your account details</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <Label className="text-gray-500 dark:text-gray-400 text-sm">Name</Label>
                            <p className="text-gray-900 dark:text-white font-medium mt-1">{user?.name}</p>
                        </div>
                        <div>
                            <Label className="text-gray-500 dark:text-gray-400 text-sm">Email</Label>
                            <p className="text-gray-900 dark:text-white font-medium mt-1">{user?.email}</p>
                        </div>
                        <div>
                            <Label className="text-gray-500 dark:text-gray-400 text-sm">Company</Label>
                            <p className="text-gray-900 dark:text-white font-medium mt-1">{user?.tenant?.name}</p>
                        </div>
                        <div>
                            <Label className="text-gray-500 dark:text-gray-400 text-sm">Role</Label>
                            <Badge variant="secondary" className="mt-1">{user?.role}</Badge>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Shopify Connection */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Store className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                        Shopify Integration
                    </CardTitle>
                    <CardDescription>Connect your Shopify store to sync data</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                    {/* Connection Status */}
                    <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
                        <div className="flex items-center justify-between mb-4">
                            <h4 className="font-medium text-gray-900 dark:text-white">Connection Status</h4>
                            {connectionStatus?.connected ? (
                                <Badge variant="success" className="flex items-center gap-1">
                                    <Check className="w-3 h-3" />
                                    Connected
                                </Badge>
                            ) : (
                                <Badge variant="destructive" className="flex items-center gap-1">
                                    <X className="w-3 h-3" />
                                    Not Connected
                                </Badge>
                            )}
                        </div>

                        {connectionStatus?.connected && (
                            <div className="space-y-3">
                                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                                    <LinkIcon className="w-4 h-4" />
                                    <span>{connectionStatus.shopDomain}</span>
                                </div>
                                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                                    <Database className="w-4 h-4" />
                                    <span>
                                        {connectionStatus.stats?.customersCount || 0} customers, {' '}
                                        {connectionStatus.stats?.productsCount || 0} products, {' '}
                                        {connectionStatus.stats?.ordersCount || 0} orders
                                    </span>
                                </div>
                                {connectionStatus.lastSyncAt && (
                                    <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                                        <Clock className="w-4 h-4" />
                                        <span>Last synced: {new Date(connectionStatus.lastSyncAt).toLocaleString()}</span>
                                    </div>
                                )}

                                <Button
                                    onClick={handleSync}
                                    disabled={syncing}
                                    className="mt-4"
                                >
                                    {syncing ? (
                                        <>
                                            <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                            Syncing...
                                        </>
                                    ) : (
                                        <>
                                            <RefreshCw className="w-4 h-4 mr-2" />
                                            Sync Now
                                        </>
                                    )}
                                </Button>
                            </div>
                        )}
                    </div>

                    {/* Connect Form */}
                    {!connectionStatus?.connected && (
                        <form onSubmit={handleConnect} className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="shopDomain">Shop Domain</Label>
                                <Input
                                    id="shopDomain"
                                    placeholder="your-store.myshopify.com"
                                    value={shopDomain}
                                    onChange={(e) => setShopDomain(e.target.value)}
                                    disabled={connecting}
                                />
                                <p className="text-xs text-gray-500 dark:text-gray-400">Enter your Shopify store domain</p>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="accessToken">Access Token</Label>
                                <Input
                                    id="accessToken"
                                    type="password"
                                    placeholder="shpat_xxxxxxxxxx"
                                    value={accessToken}
                                    onChange={(e) => setAccessToken(e.target.value)}
                                    disabled={connecting}
                                />
                                <p className="text-xs text-gray-500 dark:text-gray-400">
                                    Generate an access token from your Shopify Admin → Settings → Apps → Develop apps
                                </p>
                            </div>
                            <Button type="submit" disabled={connecting}>
                                {connecting ? (
                                    <>
                                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                        Connecting...
                                    </>
                                ) : (
                                    <>
                                        <Store className="w-4 h-4 mr-2" />
                                        Connect Store
                                    </>
                                )}
                            </Button>
                        </form>
                    )}
                </CardContent>
            </Card>
        </motion.div>
    );
}
