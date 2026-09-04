package com.example.babyshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Category(val label: String) {
    BOYS("پسرانه"),
    GIRLS("دخترانه"),
    NEWBORN("نوزاد"),
    TOYS("اسباب‌بازی"),
    OTHER("سایر")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Long,           // تومان
    val category: Category,
    val imageUri: String?,     // content:// uri یا null برای بدون عکس
    val description: String = "",
    val inStock: Boolean = true
)
