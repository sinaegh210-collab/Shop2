package com.example.babyshop.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.babyshop.ui.screens.*

sealed class Screen(val route: String, val label: String) {
    data object Shop : Screen("shop", "فروشگاه")
    data object Cart : Screen("cart", "سبد خرید")
    data object Admin : Screen("admin", "مدیریت")
    data object Detail : Screen("detail/{productId}", "جزئیات") {
        fun createRoute(id: Long) = "detail/$id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(viewModel: ShopViewModel) {
    val navController = rememberNavController()
    val cartCount by viewModel.cartCount.collectAsState()

    val bottomItems = listOf(
        Triple(Screen.Shop, Icons.Filled.Storefront, false),
        Triple(Screen.Cart, Icons.Filled.ShoppingCart, true),
        Triple(Screen.Admin, Icons.Filled.AdminPanelSettings, false)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomItems.forEach { (screen, icon, showBadge) ->
                    NavigationBarItem(
                        icon = {
                            if (showBadge && cartCount > 0) {
                                BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                    Icon(icon, contentDescription = screen.label)
                                }
                            } else {
                                Icon(icon, contentDescription = screen.label)
                            }
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Shop.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Screen.Shop.route) {
                ProductListScreen(
                    viewModel = viewModel,
                    onProductClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(viewModel = viewModel)
            }
            composable(Screen.Admin.route) {
                AdminScreen(viewModel = viewModel)
            }
            composable(Screen.Detail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductDetailScreen(
                    viewModel = viewModel,
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
