package com.example.babyshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.babyshop.data.Category
import com.example.babyshop.data.Product
import com.example.babyshop.ui.ShopViewModel
import com.example.babyshop.ui.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ShopViewModel,
    onProductClick: (Long) -> Unit
) {
    val products by viewModel.products.collectAsState()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val filtered = remember(products, selectedCategory) {
        if (selectedCategory == null) products else products.filter { it.category == selectedCategory }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(viewModel.settings.shopName) })

        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("همه") }
                )
            }
            lazyRowItems(Category.values().toList()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("محصولی در این دسته‌بندی موجود نیست")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (product.imageUri != null) {
                    AsyncImage(
                        model = product.imageUri,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(product.name, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("${formatPrice(product.price)} تومان", style = MaterialTheme.typography.bodyMedium)
                if (!product.inStock) {
                    Text("ناموجود", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onAddToCart,
                    enabled = product.inStock,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("افزودن", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
