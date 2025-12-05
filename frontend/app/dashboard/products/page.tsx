'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { productsApi } from '@/lib/api';
import { formatCurrency } from '@/lib/utils';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Package, Search, ChevronLeft, ChevronRight, ImageIcon } from 'lucide-react';

interface Product {
    id: number;
    shopifyProductId: number;
    title: string;
    vendor: string;
    productType: string;
    price: number;
    inventoryQuantity: number;
    status: string;
    imageUrl: string;
}

interface Pagination {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export default function ProductsPage() {
    const [products, setProducts] = useState<Product[]>([]);
    const [pagination, setPagination] = useState<Pagination | null>(null);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);

    useEffect(() => {
        fetchProducts();
    }, [page, search]);

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const response = await productsApi.getAll({ page, size: 12, search: search || undefined });
            if (response.success) {
                setProducts(response.data || []);
                setPagination(response.pagination);
            }
        } catch (error) {
            console.error('Failed to fetch products:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
        fetchProducts();
    };

    const getStatusBadge = (status: string | null) => {
        if (!status) return <Badge variant="secondary">Unknown</Badge>;

        const statusLower = status.toLowerCase();
        if (statusLower === 'active') return <Badge variant="success">Active</Badge>;
        if (statusLower === 'draft') return <Badge variant="warning">Draft</Badge>;
        if (statusLower === 'archived') return <Badge variant="secondary">Archived</Badge>;
        return <Badge variant="secondary">{status}</Badge>;
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
                        <Package className="w-8 h-8 text-violet-400" />
                        Products
                    </h1>
                    <p className="text-white/60 mt-1">Browse your product catalog</p>
                </div>
                <form onSubmit={handleSearch} className="flex gap-2">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/40" />
                        <Input
                            placeholder="Search products..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="pl-10 w-64"
                        />
                    </div>
                    <Button type="submit" variant="secondary">Search</Button>
                </form>
            </div>

            {/* Stats */}
            <div className="flex items-center gap-2 text-white/60">
                <p>{pagination ? `${pagination.totalElements} products` : 'Loading...'}</p>
            </div>

            {/* Products Grid */}
            {loading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {[...Array(8)].map((_, i) => (
                        <Card key={i}>
                            <Skeleton className="h-48 w-full rounded-t-2xl" />
                            <CardContent className="pt-4">
                                <Skeleton className="h-4 w-3/4 mb-2" />
                                <Skeleton className="h-4 w-1/2" />
                            </CardContent>
                        </Card>
                    ))}
                </div>
            ) : products.length === 0 ? (
                <Card>
                    <CardContent className="text-center py-12">
                        <Package className="w-12 h-12 text-white/20 mx-auto mb-4" />
                        <p className="text-white/60">No products found</p>
                        <p className="text-white/40 text-sm mt-1">Connect your Shopify store to sync products</p>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {products.map((product) => (
                        <motion.div
                            key={product.id}
                            initial={{ opacity: 0, scale: 0.95 }}
                            animate={{ opacity: 1, scale: 1 }}
                            className="group"
                        >
                            <Card className="overflow-hidden hover:border-violet-500/50 transition-all duration-300 hover:shadow-xl hover:shadow-violet-500/10">
                                {/* Product Image */}
                                <div className="h-48 bg-white/5 relative overflow-hidden">
                                    {product.imageUrl ? (
                                        <img
                                            src={product.imageUrl}
                                            alt={product.title}
                                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center">
                                            <ImageIcon className="w-12 h-12 text-white/20" />
                                        </div>
                                    )}
                                    <div className="absolute top-3 right-3">
                                        {getStatusBadge(product.status)}
                                    </div>
                                </div>

                                {/* Product Info */}
                                <CardContent className="pt-4">
                                    <h3 className="font-semibold text-white truncate mb-1 group-hover:text-violet-300 transition-colors">
                                        {product.title}
                                    </h3>
                                    <p className="text-white/40 text-sm mb-3">{product.vendor || 'No vendor'}</p>

                                    <div className="flex items-center justify-between">
                                        <span className="text-lg font-bold text-white">
                                            {formatCurrency(product.price)}
                                        </span>
                                        <span className={`text-sm ${product.inventoryQuantity <= 10 ? 'text-red-400' : 'text-white/60'}`}>
                                            {product.inventoryQuantity} in stock
                                        </span>
                                    </div>
                                </CardContent>
                            </Card>
                        </motion.div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            {pagination && pagination.totalPages > 1 && (
                <div className="flex items-center justify-between pt-6 border-t border-white/10">
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
        </motion.div>
    );
}
