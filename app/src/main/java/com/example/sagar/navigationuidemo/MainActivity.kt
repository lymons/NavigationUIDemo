package com.example.sagar.navigationuidemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.popBackStackContainer
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setting keep alvie navigator for avoiding recreated fragment
        navController = Navigation.findNavController(this, R.id.mainNavFragment)

        // Set up navigation menu
        navigationView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.bottomNavFragment, R.id.infoFragment),
                drawerLayout)

        // Set up ActionBar
        setSupportActionBar(toolbar)
        setupNavController(R.id.mainNavFragment, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp(R.id.mainNavFragment, appBarConfiguration)
    }

    override fun finishAfterTransition() {
        if (!navController.popBackStackContainer(R.id.mainNavFragment)) {
            super.finishAfterTransition()
        }
    }
}
