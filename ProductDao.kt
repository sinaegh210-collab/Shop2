package com.example.babyshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAll(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?
}
