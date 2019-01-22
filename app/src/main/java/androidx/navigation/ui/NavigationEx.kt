package androidx.navigation.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.ui.NavigationUI.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import java.lang.ref.WeakReference

/**
 * Porting from setupWithNavController of NavigationUI,
 * The difference is using onNavKeepAliveDestinationSelected(item, navController)
 * to prevent fragment be destroy in menu when this menu item clicked
 * instead of using onNavDestinationSelected(item, navController).
 */
fun NavigationView.setupKeepAliveNavController(@NonNull navController: NavController) {
    setNavigationItemSelectedListener { item ->
        val handled = onNavKeepAliveDestinationSelected(item, navController)
        if (handled) {
            if (parent is DrawerLayout) {
                (parent as DrawerLayout).closeDrawer(this)
            } else {
                val bottomSheetBehavior = findBottomSheetBehavior(this)
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        handled
    }
    val weakReference = WeakReference<NavigationView>(this)
    navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(@NonNull controller: NavController,
                                                  @NonNull destination: NavDestination, @Nullable arguments: Bundle?) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    val menu = view.menu
                    var h = 0
                    val size = menu.size()
                    while (h < size) {
                        val item = menu.getItem(h)
                        item.isChecked = matchDestination(destination, item.itemId)
                        h++
                    }
                }
            })
}

/**
 * Porting from setupWithNavController of NavigationUI,
 * The difference is using onNavKeepAliveDestinationSelected(item, navController)
 * to prevent fragment be destroy in Tab when this Tab item clicked
 * instead of using onNavDestinationSelected(item, navController).
 */
fun BottomNavigationView.setupKeepAliveNavController(@NonNull navController: NavController) {
    setOnNavigationItemSelectedListener { item -> onNavKeepAliveDestinationSelected(item, navController) }
    val weakReference = WeakReference<BottomNavigationView>(this)
    navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(@NonNull controller: NavController,
                                                  @NonNull destination: NavDestination, @Nullable arguments: Bundle?) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    val menu = view.menu
                    var h = 0
                    val size = menu.size()
                    while (h < size) {
                        val item = menu.getItem(h)
                        if (matchDestination(destination, item.itemId)) {
                            item.isChecked = true
                        }
                        h++
                    }
                }
            })

}

/**
 * Porting from onNavDestinationSelected of NavigationUI,
 * the difference is that fragment do not destroy in menu
 * when this menu item clicked.
 */
internal fun onNavKeepAliveDestinationSelected(@NonNull item: MenuItem,
                                      @NonNull navController: NavController): Boolean {
    val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)

    val options = builder.build()

    return try {
        //TODO provide proper API instead of using Exceptions as Control-Flow.
        navController.navigate(item.itemId, null, options)
        true
    } catch (e: IllegalArgumentException) {
        false
    }

}