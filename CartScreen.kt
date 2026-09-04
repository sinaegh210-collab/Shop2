package com.example.babyshop.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.babyshop.data.CartItem
import com.example.babyshop.ui.ShopViewModel
import com.example.babyshop.ui.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: ShopViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("سبد خرید") })

        if (cartItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("سبد خرید شما خالیه")
            }
            return@Column
        }

        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            items(cartItems, key = { it.product.id }) { item ->
                CartRow(
                    item = item,
                    onIncrease = { viewModel.addToCart(item.product) },
                    onDecrease = { viewModel.decreaseFromCart(item.product) },
                    onRemove = { viewModel.removeFromCart(item.product) }
                )
                Divider()
            }
        }

        Column(Modifier.padding(16.dp)) {
            Text("جمع کل: ${formatPrice(total)} تومان", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("نام شما (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = customerAddress,
                onValueChange = { customerAddress = it },
                label = { Text("آدرس / محل تحویل (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val message = viewModel.buildOrderMessage(customerName, customerAddress)
                        sendViaWhatsApp(context, viewModel.settings.whatsappNumber, message)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ارسال با واتساپ")
                }
                OutlinedButton(
                    onClick = {
                        val message = viewModel.buildOrderMessage(customerName, customerAddress)
                        sendViaShare(context, message)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ارسال با تلگرام")
                }
            }
        }
    }
}

@Composable
fun CartRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.titleSmall)
            Text("${formatPrice(item.product.price)} تومان × ${item.quantity}", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDecrease) { Icon(Icons.Filled.Remove, contentDescription = "کم کردن") }
        Text("${item.quantity}")
        IconButton(onClick = onIncrease) { Icon(Icons.Filled.Add, contentDescription = "زیاد کردن") }
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "حذف") }
    }
}

private fun sendViaWhatsApp(context: Context, phoneNumber: String, message: String) {
    try {
        val encoded = Uri.encode(message)
        val uri = Uri.parse("https://wa.me/$phoneNumber?text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "واتساپ نصب نیست", Toast.LENGTH_SHORT).show()
    }
}

private fun sendViaShare(context: Context, message: String) {
    // برای تلگرام: چون ارسال مستقیم به یک مخاطب مشخص بدون ربات ممکن نیست،
    // از منوی اشتراک‌گذاری استاندارد اندروید استفاده می‌کنیم تا کاربر تلگرام (یا هر اپ دیگری) را انتخاب کند.
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        setPackage("org.telegram.messenger")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // اگر تلگرام نصب نبود، منوی عمومی اشتراک‌گذاری را نشان بده
        val chooser = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(chooser, "ارسال سفارش با..."))
    }
}
