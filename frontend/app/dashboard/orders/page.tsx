'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { ordersApi } from '@/lib/api';
import { formatCurrency, formatDate } from '@/lib/utils';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { ShoppingCart, ChevronLeft, ChevronRight, Package, DollarSign } from 'lucide-react';

interface Order {
    id: number;
    shopifyOrderId: number;
    orderNumber: string;
    customer: { id: number; name: string; email: string } | null;
    totalPrice: number;
    currency: string;
    financialStatus: string;
    fulfillmentStatus: string;
    itemCount: number;
    processedAt: string;
}

interface Pagination {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export default function OrdersPage() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [pagination, setPagination] = useState<Pagination | null>(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);

    useEffect(() => {
        fetchOrders();
    }, [page]);

    const fetchOrders = async () => {
        setLoading(true);
        try {
            const response = await ordersApi.getAll({ page, size: 10 });
            if (response.success) {
                setOrders(response.data || []);
                setPagination(response.pagination);
            }
        } catch (error) {
            console.error('Failed to fetch orders:', error);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (status: string | null) => {
        if (!status) return <Badge variant="secondary">Unknown</Badge>;

        const statusLower = status.toLowerCase();
        if (statusLower === 'paid') return <Badge variant="success">Paid</Badge>;
        if (statusLower === 'pending') return <Badge variant="warning">Pending</Badge>;
        if (statusLower === 'refunded') return <Badge variant="destructive">Refunded</Badge>;
        return <Badge variant="secondary">{status}</Badge>;
    };

    const getFulfillmentBadge = (status: string | null) => {
        if (!status) return <Badge variant="outline">Unfulfilled</Badge>;

        const statusLower = status.toLowerCase();
        if (statusLower === 'fulfilled') return <Badge variant="success">Fulfilled</Badge>;
        if (statusLower === 'partial') return <Badge variant="warning">Partial</Badge>;
        return <Badge variant="outline">{status}</Badge>;
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
        >
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-white flex items-center gap-3">
                    <ShoppingCart className="w-8 h-8 text-violet-400" />
                    Orders
                </h1>
                <p className="text-white/60 mt-1">View and manage your orders</p>
            </div>

            {/* Orders Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Orders</CardTitle>
                    <CardDescription>
                        {pagination ? `${pagination.totalElements} total orders` : 'Loading...'}
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-4">
                            {[...Array(5)].map((_, i) => (
                                <div key={i} className="flex items-center gap-4">
                                    <Skeleton className="h-4 w-20" />
                                    <Skeleton className="h-4 w-48" />
                                    <Skeleton className="h-4 w-24 ml-auto" />
                                </div>
                            ))}
                        </div>
                    ) : orders.length === 0 ? (
                        <div className="text-center py-12">
                            <ShoppingCart className="w-12 h-12 text-white/20 mx-auto mb-4" />
                            <p className="text-white/60">No orders found</p>
                            <p className="text-white/40 text-sm mt-1">Connect your Shopify store to sync orders</p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Order</TableHead>
                                    <TableHead>Customer</TableHead>
                                    <TableHead>Date</TableHead>
                                    <TableHead>Items</TableHead>
                                    <TableHead>Payment</TableHead>
                                    <TableHead>Fulfillment</TableHead>
                                    <TableHead className="text-right">Total</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.map((order) => (
                                    <TableRow key={order.id}>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <div className="w-8 h-8 rounded-lg bg-violet-500/20 flex items-center justify-center">
                                                    <DollarSign className="w-4 h-4 text-violet-400" />
                                                </div>
                                                <span className="font-medium">#{order.orderNumber || order.shopifyOrderId}</span>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {order.customer ? (
                                                <div>
                                                    <p className="font-medium">{order.customer.name || 'Unknown'}</p>
                                                    <p className="text-white/40 text-sm">{order.customer.email}</p>
                                                </div>
                                            ) : (
                                                <span className="text-white/40">Guest</span>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-white/60">
                                            {order.processedAt ? formatDate(order.processedAt) : '—'}
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <Package className="w-4 h-4 text-white/40" />
                                                {order.itemCount} items
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {getStatusBadge(order.financialStatus)}
                                        </TableCell>
                                        <TableCell>
                                            {getFulfillmentBadge(order.fulfillmentStatus)}
                                        </TableCell>
                                        <TableCell className="text-right font-semibold">
                                            {formatCurrency(order.totalPrice, order.currency)}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}

                    {/* Pagination */}
                    {pagination && pagination.totalPages > 1 && (
                        <div className="flex items-center justify-between mt-6 pt-6 border-t border-white/10">
                            <p className="text-sm text-white/60">
                                Page {pagination.page + 1} of {pagination.totalPages}
                            </p>
                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage(p => Math.max(0, p - 1))}
                                    disabled={pagination.page === 0}
                                >
                                    <ChevronLeft className="w-4 h-4 mr-1" />
                                    Previous
                                </Button>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage(p => p + 1)}
                                    disabled={pagination.page >= pagination.totalPages - 1}
                                >
                                    Next
                                    <ChevronRight className="w-4 h-4 ml-1" />
                                </Button>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>
        </motion.div>
    );
}
