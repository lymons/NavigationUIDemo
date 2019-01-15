package com.example.sagar.navigationuidemo

import android.app.Activity
import androidx.annotation.IdRes
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController


fun Activity.navigateUp(@IdRes viewId: Int, @Nullable drawerLayout: DrawerLayout): Boolean = navigateUp(Navigation.findNavController(this, viewId), drawerLayout)

fun AppCompatActivity.setupNavController(@IdRes viewId: Int, @Nullable drawerLayout: DrawerLayout) = setupActionBarWithNavController(this, Navigation.findNavController(this, viewId), drawerLayout)