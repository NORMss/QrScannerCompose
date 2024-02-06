package com.norm.myqrscannercompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE numberQr = :qr")
    fun getProductByQr(qr: String): Product?
}