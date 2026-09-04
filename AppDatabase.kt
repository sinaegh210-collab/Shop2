package com.example.babyshop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "babyshop.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // چند محصول نمونه برای شروع
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).productDao()
                            dao.upsert(
                                Product(
                                    name = "پیراهن پسرانه طرح دار",
                                    price = 250000,
                                    category = Category.BOYS,
                                    imageUri = null,
                                    description = "سایز ۲ تا ۶ سال، جنس نخی"
                                )
                            )
                            dao.upsert(
                                Product(
                                    name = "دامن دخترانه پرنسسی",
                                    price = 320000,
                                    category = Category.GIRLS,
                                    imageUri = null,
                                    description = "سایز ۳ تا ۷ سال"
                                )
                            )
                            dao.upsert(
                                Product(
                                    name = "سرهمی نوزادی",
                                    price = 180000,
                                    category = Category.NEWBORN,
                                    imageUri = null,
                                    description = "۰ تا ۶ ماه"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
