package com.example.babyshop.data

import android.content.Context

class ShopSettings(context: Context) {
    private val prefs = context.getSharedPreferences("shop_settings", Context.MODE_PRIVATE)

    var shopName: String
        get() = prefs.getString("shop_name", "فروشگاه کودک") ?: "فروشگاه کودک"
        set(value) = prefs.edit().putString("shop_name", value).apply()

    // شماره واتساپ فروشنده با کد کشور و بدون + یا صفر ابتدایی، مثل 989121234567
    var whatsappNumber: String
        get() = prefs.getString("whatsapp_number", "989120000000") ?: ""
        set(value) = prefs.edit().putString("whatsapp_number", value).apply()

    // رمز ساده برای ورود به بخش مدیریت
    var adminPassword: String
        get() = prefs.getString("admin_password", "1234") ?: "1234"
        set(value) = prefs.edit().putString("admin_password", value).apply()
}
