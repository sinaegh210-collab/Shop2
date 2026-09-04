package com.example.babyshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.babyshop.data.Product
import com.example.babyshop.ui.ShopViewModel
import com.example.babyshop.ui.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: ShopViewModel, productId: Long, onBack: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val product: Product? = products.find { it.id == productId }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(product?.name ?: "") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            }
        )

        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("محصول یافت نشد") }
            return@Column
        }

        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (product.imageUri != null) {
                    AsyncImage(
                        model = product.imageUri,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(64.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(product.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("${formatPrice(product.price)} تومان", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("دسته‌بندی: ${product.category.label}", style = MaterialTheme.typography.bodyMedium)
            if (!product.inStock) {
                Text("ناموجود", color = MaterialTheme.colorScheme.error)
            }
            if (product.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(product.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.addToCart(product) },
                enabled = product.inStock,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("افزودن به سبد خرید")
            }
        }
    }
}
