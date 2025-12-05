'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { customersApi } from '@/lib/api';
import { formatCurrency, formatDate, getInitials } from '@/lib/utils';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Users, Search, ChevronLeft, ChevronRight, Mail, Phone, MapPin } from 'lucide-react';

interface Customer {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    phone: string;
    city: string;
    country: string;
    totalSpent: number;
    ordersCount: number;
    acceptsMarketing: boolean;
    createdAt: string;
}

interface Pagination {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export default function CustomersPage() {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [pagination, setPagination] = useState<Pagination | null>(null);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);

    useEffect(() => {
        fetchCustomers();
    }, [page, search]);

    const fetchCustomers = async () => {
        setLoading(true);
        try {
            const response = await customersApi.getAll({ page, size: 10, search: search || undefined });
            if (response.success) {
                setCustomers(response.data || []);
                setPagination(response.pagination);
            }
        } catch (error) {
            console.error('Failed to fetch customers:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
        fetchCustomers();
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
        >
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-white flex items-center gap-3">
                        <Users className="w-8 h-8 text-violet-400" />
                        Customers
                    </h1>
                    <p className="text-white/60 mt-1">Manage your customer base</p>
                </div>
                <form onSubmit={handleSearch} className="flex gap-2">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/40" />
                        <Input
                            placeholder="Search customers..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="pl-10 w-64"
                        />
                    </div>
                    <Button type="submit" variant="secondary">Search</Button>
                </form>
            </div>

            {/* Customers Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Customers</CardTitle>
                    <CardDescription>
                        {pagination ? `${pagination.totalElements} total customers` : 'Loading...'}
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-4">
                            {[...Array(5)].map((_, i) => (
                                <div key={i} className="flex items-center gap-4">
                                    <Skeleton className="w-10 h-10 rounded-full" />
                                    <Skeleton className="h-4 w-48" />
                                    <Skeleton className="h-4 w-32 ml-auto" />
                                </div>
                            ))}
                        </div>
                    ) : customers.length === 0 ? (
                        <div className="text-center py-12">
                            <Users className="w-12 h-12 text-white/20 mx-auto mb-4" />
                            <p className="text-white/60">No customers found</p>
                            <p className="text-white/40 text-sm mt-1">Connect your Shopify store to sync customers</p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Customer</TableHead>
                                    <TableHead>Contact</TableHead>
                                    <TableHead>Location</TableHead>
                                    <TableHead className="text-right">Spent</TableHead>
                                    <TableHead className="text-right">Orders</TableHead>
                                    <TableHead>Status</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {customers.map((customer) => (
                                    <TableRow key={customer.id}>
                                        <TableCell>
                                            <div className="flex items-center gap-3">
                                                <Avatar>
                                                    <AvatarFallback>{getInitials(customer.fullName || customer.email)}</AvatarFallback>
                                                </Avatar>
                                                <div>
                                                    <p className="font-medium">{customer.fullName || 'Unknown'}</p>
                                                    <p className="text-white/40 text-sm">{formatDate(customer.createdAt)}</p>
                                                </div>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            <div className="space-y-1">
                                                <div className="flex items-center gap-2 text-sm text-white/60">
                                                    <Mail className="w-3 h-3" />
                                                    {customer.email}
                                                </div>
                                                {customer.phone && (
                                                    <div className="flex items-center gap-2 text-sm text-white/40">
                                                        <Phone className="w-3 h-3" />
                                                        {customer.phone}
                                                    </div>
                                                )}
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {customer.city || customer.country ? (
                                                <div className="flex items-center gap-2 text-sm text-white/60">
                                                    <MapPin className="w-3 h-3" />
                                                    {[customer.city, customer.country].filter(Boolean).join(', ')}
                                                </div>
                                            ) : (
                                                <span className="text-white/40">—</span>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-right font-medium">
                                            {formatCurrency(customer.totalSpent)}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {customer.ordersCount}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={customer.acceptsMarketing ? 'success' : 'secondary'}>
                                                {customer.acceptsMarketing ? 'Subscribed' : 'Not Subscribed'}
                                            </Badge>
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
