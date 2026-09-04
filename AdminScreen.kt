package com.example.babyshop.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.babyshop.data.Category
import com.example.babyshop.data.Product
import com.example.babyshop.ui.ShopViewModel
import com.example.babyshop.ui.formatPrice

@Composable
fun AdminScreen(viewModel: ShopViewModel) {
    var unlocked by rememberSaveable { mutableStateOf(false) }

    if (!unlocked) {
        AdminLoginGate(viewModel = viewModel, onUnlocked = { unlocked = true })
    } else {
        AdminPanel(viewModel = viewModel)
    }
}

@Composable
private fun AdminLoginGate(viewModel: ShopViewModel, onUnlocked: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ورود به بخش مدیریت", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; error = false },
            label = { Text("رمز عبور") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            isError = error,
            modifier = Modifier.fillMaxWidth()
        )
        if (error) {
            Text("رمز اشتباه است", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (input == viewModel.settings.adminPassword) onUnlocked() else error = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ورود")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "رمز پیش‌فرض: 1234 (از تنظیمات قابل تغییر است)",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPanel(viewModel: ShopViewModel) {
    val products by viewModel.products.collectAsState()
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("مدیریت فروشگاه") },
            actions = {
                TextButton(onClick = { showSettings = true }) { Text("تنظیمات") }
            }
        )

        Row(Modifier.padding(12.dp)) {
            Button(onClick = { editingProduct = null; showForm = true }) {
                Text("+ افزودن محصول جدید")
            }
        }

        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            items(products, key = { it.id }) { product ->
                AdminProductRow(
                    product = product,
                    onEdit = { editingProduct = product; showForm = true },
                    onDelete = { viewModel.deleteProduct(product) }
                )
                Divider()
            }
        }
    }

    if (showForm) {
        ProductFormDialog(
            initial = editingProduct,
            onDismiss = { showForm = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showForm = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
    }
}

@Composable
private fun AdminProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.titleSmall)
            Text(
                "${formatPrice(product.price)} تومان · ${product.category.label}" +
                    if (!product.inStock) " · ناموجود" else "",
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "حذف") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.OTHER) }
    var inStock by remember { mutableStateOf(initial?.inStock ?: true) }
    var imageUri by remember { mutableStateOf(initial?.imageUri) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* برخی منابع اجازه دائمی نمی‌دهند، مشکلی نیست */ }
            imageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "افزودن محصول" else "ویرایش محصول") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { imagePicker.launch(arrayOf("image/*")) },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text("انتخاب عکس محصول", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("نام محصول") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = { Text("قیمت (تومان)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        Category.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = { category = cat; categoryMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("توضیحات (سایز، جنس و ...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = inStock, onCheckedChange = { inStock = it })
                    Text("موجود است")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val priceValue = price.toLongOrNull() ?: 0L
                if (name.isNotBlank() && priceValue > 0) {
                    onSave(
                        Product(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            price = priceValue,
                            category = category,
                            imageUri = imageUri,
                            description = description.trim(),
                            inStock = inStock
                        )
                    )
                }
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun SettingsDialog(viewModel: ShopViewModel, onDismiss: () -> Unit) {
    var shopName by remember { mutableStateOf(viewModel.settings.shopName) }
    var whatsapp by remember { mutableStateOf(viewModel.settings.whatsappNumber) }
    var password by remember { mutableStateOf(viewModel.settings.adminPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیمات فروشگاه") },
        text = {
            Column {
                OutlinedTextField(
                    value = shopName, onValueChange = { shopName = it },
                    label = { Text("نام فروشگاه") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = whatsapp, onValueChange = { whatsapp = it.filter { c -> c.isDigit() } },
                    label = { Text("شماره واتساپ (با کد کشور، بدون + مثل 989121234567)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("رمز بخش مدیریت") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.settings.shopName = shopName.ifBlank { viewModel.settings.shopName }
                viewModel.settings.whatsappNumber = whatsapp.ifBlank { viewModel.settings.whatsappNumber }
                viewModel.settings.adminPassword = password.ifBlank { viewModel.settings.adminPassword }
                onDismiss()
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
