package com.example.babyshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.babyshop.ui.AppNav
import com.example.babyshop.ui.ShopViewModel
import com.example.babyshop.ui.theme.BabyShopTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyShopTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNav(viewModel = viewModel)
                }
            }
        }
    }
}
