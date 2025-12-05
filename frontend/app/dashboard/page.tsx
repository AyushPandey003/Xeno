'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { dashboardApi } from '@/lib/api';
import { formatCurrency, formatCompactNumber, formatPercentage } from '@/lib/utils';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import {
    Users,
    ShoppingCart,
    DollarSign,
    Package,
    TrendingUp,
    TrendingDown,
    ArrowUpRight,
    Crown,
} from 'lucide-react';
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
} from 'recharts';

interface DashboardData {
    overview: {
        totalCustomers: number;
        totalOrders: number;
        totalRevenue: number;
        totalProducts: number;
        averageOrderValue: number;
        newCustomersThisMonth: number;
        ordersThisMonth: number;
        revenueThisMonth: number;
        customersChange: number;
        ordersChange: number;
        revenueChange: number;
    };
    ordersByDate: Array<{ date: string; orderCount: number; revenue: number }>;
    topCustomers: Array<{ id: number; name: string; email: string; totalSpent: number; ordersCount: number }>;
    revenueTrends: Array<{ period: string; revenue: number; orderCount: number }>;
    topProducts: Array<{ productTitle: string; quantitySold: number; revenue: number }>;
    orderStatusBreakdown: Array<{ status: string; count: number; percentage: number }>;
}

const COLORS = ['#8b5cf6', '#6366f1', '#a855f7', '#ec4899', '#f43f5e'];

export default function DashboardPage() {
    const [data, setData] = useState<DashboardData | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await dashboardApi.getData();
                if (response.success) {
                    setData(response.data);
                }
            } catch (error) {
                console.error('Failed to fetch dashboard data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const container = {
        hidden: { opacity: 0 },
        show: {
            opacity: 1,
            transition: { staggerChildren: 0.1 },
        },
    };

    const item = {
        hidden: { opacity: 0, y: 20 },
        show: { opacity: 1, y: 0 },
    };

    if (loading) {
        return (
            <div className="space-y-8">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {[...Array(4)].map((_, i) => (
                        <Card key={i}>
                            <CardHeader className="pb-2">
                                <Skeleton className="h-4 w-24" />
                            </CardHeader>
                            <CardContent>
                                <Skeleton className="h-8 w-32 mb-2" />
                                <Skeleton className="h-4 w-20" />
                            </CardContent>
                        </Card>
                    ))}
                </div>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <Card>
                        <CardHeader>
                            <Skeleton className="h-6 w-32" />
                        </CardHeader>
                        <CardContent>
                            <Skeleton className="h-64 w-full" />
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader>
                            <Skeleton className="h-6 w-32" />
                        </CardHeader>
                        <CardContent>
                            <Skeleton className="h-64 w-full" />
                        </CardContent>
                    </Card>
                </div>
            </div>
        );
    }

    const stats = [
        {
            title: 'Total Customers',
            value: formatCompactNumber(data?.overview?.totalCustomers || 0),
            change: data?.overview?.customersChange,
            icon: Users,
            gradient: 'from-blue-500 to-cyan-500',
            shadowColor: 'shadow-blue-500/30',
        },
        {
            title: 'Total Orders',
            value: formatCompactNumber(data?.overview?.totalOrders || 0),
            change: data?.overview?.ordersChange,
            icon: ShoppingCart,
            gradient: 'from-violet-500 to-purple-500',
            shadowColor: 'shadow-violet-500/30',
        },
        {
            title: 'Total Revenue',
            value: formatCurrency(data?.overview?.totalRevenue || 0),
            change: data?.overview?.revenueChange,
            icon: DollarSign,
            gradient: 'from-emerald-500 to-teal-500',
            shadowColor: 'shadow-emerald-500/30',
        },
        {
            title: 'Total Products',
            value: formatCompactNumber(data?.overview?.totalProducts || 0),
            icon: Package,
            gradient: 'from-orange-500 to-amber-500',
            shadowColor: 'shadow-orange-500/30',
        },
    ];

    return (
        <motion.div
            variants={container}
            initial="hidden"
            animate="show"
            className="space-y-8"
        >
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-white">Dashboard</h1>
                <p className="text-white/60 mt-1">Welcome back! Here&apos;s your store overview.</p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {stats.map((stat, index) => (
                    <motion.div key={stat.title} variants={item}>
                        <Card className="relative overflow-hidden group hover:scale-[1.02] transition-transform duration-300">
                            <div className={`absolute inset-0 bg-gradient-to-br ${stat.gradient} opacity-0 group-hover:opacity-5 transition-opacity`} />
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                                <CardDescription className="text-white/60 font-medium">{stat.title}</CardDescription>
                                <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${stat.gradient} flex items-center justify-center shadow-lg ${stat.shadowColor}`}>
                                    <stat.icon className="w-5 h-5 text-white" />
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="text-3xl font-bold text-white mb-1">{stat.value}</div>
                                {stat.change !== undefined && (
                                    <div className={`flex items-center gap-1 text-sm ${stat.change >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                                        {stat.change >= 0 ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                                        {formatPercentage(stat.change)} from last month
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    </motion.div>
                ))}
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Revenue Trends */}
                <motion.div variants={item}>
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <TrendingUp className="w-5 h-5 text-violet-400" />
                                Revenue Trends
                            </CardTitle>
                            <CardDescription>Monthly revenue over time</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="h-72">
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={data?.revenueTrends || []}>
                                        <defs>
                                            <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.4} />
                                                <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                                        <XAxis dataKey="period" stroke="rgba(255,255,255,0.4)" fontSize={12} />
                                        <YAxis stroke="rgba(255,255,255,0.4)" fontSize={12} tickFormatter={(v) => `$${formatCompactNumber(v)}`} />
                                        <Tooltip
                                            contentStyle={{
                                                backgroundColor: 'rgba(17, 17, 27, 0.95)',
                                                border: '1px solid rgba(255,255,255,0.1)',
                                                borderRadius: '12px',
                                                color: 'white',
                                            }}
                                            formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="revenue"
                                            stroke="#8b5cf6"
                                            strokeWidth={2}
                                            fillOpacity={1}
                                            fill="url(#colorRevenue)"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </CardContent>
                    </Card>
                </motion.div>

                {/* Top Products */}
                <motion.div variants={item}>
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Package className="w-5 h-5 text-violet-400" />
                                Top Products
                            </CardTitle>
                            <CardDescription>Best selling products by revenue</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="h-72">
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={data?.topProducts?.slice(0, 5) || []} layout="vertical">
                                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                                        <XAxis type="number" stroke="rgba(255,255,255,0.4)" fontSize={12} tickFormatter={(v) => `$${formatCompactNumber(v)}`} />
                                        <YAxis dataKey="productTitle" type="category" stroke="rgba(255,255,255,0.4)" fontSize={11} width={120} tick={{ fill: 'rgba(255,255,255,0.6)' }} />
                                        <Tooltip
                                            contentStyle={{
                                                backgroundColor: 'rgba(17, 17, 27, 0.95)',
                                                border: '1px solid rgba(255,255,255,0.1)',
                                                borderRadius: '12px',
                                                color: 'white',
                                            }}
                                            formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                                        />
                                        <Bar dataKey="revenue" fill="#8b5cf6" radius={[0, 4, 4, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </CardContent>
                    </Card>
                </motion.div>
            </div>

            {/* Bottom Row */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Top Customers */}
                <motion.div variants={item} className="lg:col-span-2">
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Crown className="w-5 h-5 text-amber-400" />
                                Top Customers
                            </CardTitle>
                            <CardDescription>Customers with highest spend</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-4">
                                {data?.topCustomers?.map((customer, index) => (
                                    <div
                                        key={customer.id}
                                        className="flex items-center gap-4 p-4 rounded-xl bg-white/5 hover:bg-white/10 transition-colors"
                                    >
                                        <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${index === 0 ? 'bg-gradient-to-br from-amber-400 to-orange-500' :
                                                index === 1 ? 'bg-gradient-to-br from-slate-300 to-slate-400' :
                                                    index === 2 ? 'bg-gradient-to-br from-amber-600 to-amber-700' :
                                                        'bg-white/10'
                                            } text-white`}>
                                            {index + 1}
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className="text-white font-medium truncate">{customer.name || 'Unknown'}</p>
                                            <p className="text-white/40 text-sm truncate">{customer.email}</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="text-white font-semibold">{formatCurrency(customer.totalSpent)}</p>
                                            <p className="text-white/40 text-sm">{customer.ordersCount} orders</p>
                                        </div>
                                    </div>
                                ))}
                                {(!data?.topCustomers || data.topCustomers.length === 0) && (
                                    <p className="text-center text-white/40 py-8">No customer data available</p>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </motion.div>

                {/* Order Status */}
                <motion.div variants={item}>
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <ShoppingCart className="w-5 h-5 text-violet-400" />
                                Order Status
                            </CardTitle>
                            <CardDescription>Breakdown by status</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="h-48 mb-4">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={data?.orderStatusBreakdown || []}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={50}
                                            outerRadius={70}
                                            paddingAngle={5}
                                            dataKey="count"
                                        >
                                            {data?.orderStatusBreakdown?.map((_, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip
                                            contentStyle={{
                                                backgroundColor: 'rgba(17, 17, 27, 0.95)',
                                                border: '1px solid rgba(255,255,255,0.1)',
                                                borderRadius: '12px',
                                                color: 'white',
                                            }}
                                        />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <div className="space-y-2">
                                {data?.orderStatusBreakdown?.map((status, index) => (
                                    <div key={status.status} className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <div
                                                className="w-3 h-3 rounded-full"
                                                style={{ backgroundColor: COLORS[index % COLORS.length] }}
                                            />
                                            <span className="text-sm text-white/60 capitalize">{status.status.toLowerCase().replace('_', ' ')}</span>
                                        </div>
                                        <Badge variant="secondary">{status.count}</Badge>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>
                </motion.div>
            </div>

            {/* Average Order Value */}
            <motion.div variants={item}>
                <Card className="bg-gradient-to-r from-violet-600/20 to-indigo-600/20 border-violet-500/20">
                    <CardContent className="flex items-center justify-between p-6">
                        <div>
                            <p className="text-white/60 text-sm font-medium">Average Order Value</p>
                            <p className="text-4xl font-bold text-white mt-1">
                                {formatCurrency(data?.overview?.averageOrderValue || 0)}
                            </p>
                        </div>
                        <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-violet-500/30">
                            <ArrowUpRight className="w-8 h-8 text-white" />
                        </div>
                    </CardContent>
                </Card>
            </motion.div>
        </motion.div>
    );
}
