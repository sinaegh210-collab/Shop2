package com.example.babyshop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyshop.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).productDao()
    val settings = ShopSettings(application)

    val products: StateFlow<List<Product>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap()) // productId -> quantity
    val cartItems: StateFlow<List<CartItem>> = combine(products, _cart) { productList, cartMap ->
        productList.filter { cartMap.containsKey(it.id) }
            .map { CartItem(it, cartMap[it.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Long> = cartItems
        .map { items -> items.sumOf { it.totalPrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val cartCount: StateFlow<Int> = cartItems
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product) {
        _cart.update { current ->
            val newQty = (current[product.id] ?: 0) + 1
            current + (product.id to newQty)
        }
    }

    fun decreaseFromCart(product: Product) {
        _cart.update { current ->
            val newQty = (current[product.id] ?: 0) - 1
            if (newQty <= 0) current - product.id else current + (product.id to newQty)
        }
    }

    fun removeFromCart(product: Product) {
        _cart.update { it - product.id }
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch { dao.upsert(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { dao.delete(product) }
        _cart.update { it - product.id }
    }

    // متن سفارش برای ارسال به واتساپ/تلگرام
    fun buildOrderMessage(customerName: String = "", customerAddress: String = ""): String {
        val items = cartItems.value
        val sb = StringBuilder()
        sb.append("سلام، سفارش جدید از ${settings.shopName}:\n\n")
        items.forEach { item ->
            sb.append("- ${item.product.name} × ${item.quantity} = ${formatPrice(item.totalPrice)} تومان\n")
        }
        sb.append("\nجمع کل: ${formatPrice(cartTotal.value)} تومان\n")
        if (customerName.isNotBlank()) sb.append("\nنام: $customerName")
        if (customerAddress.isNotBlank()) sb.append("\nآدرس: $customerAddress")
        return sb.toString()
    }
}

fun formatPrice(price: Long): String {
    return "%,d".format(price)
}
