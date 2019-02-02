package androidx.navigation

import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.*

fun NavController.navigateBack(host: ContainerHostFragment): Boolean {
    if (mBackStack.size < 2) {
        return false
    }

    val manager = host.childFragmentManager
    val transaction = manager.beginTransaction()
    val topEntry = mBackStack.removeLast()
    val desEntry = mBackStack.peekLast()
    if (desEntry.destination !is FragmentNavigator.Destination) {
        return false
    }
    var destination = manager.findFragmentByTag(desEntry.destination.id.toString())
    if (destination == null) {
        destination = host.instantiateFragment(navigatorProvider, desEntry.destination as FragmentNavigator.Destination, desEntry.arguments)
        transaction.add(destination, desEntry.destination.id.toString())
    } else {
        transaction.show(destination)
    }
    transaction.setPrimaryNavigationFragment(destination)
    val fragment = manager.findFragmentByTag(topEntry.destination.id.toString())
    fragment?.let {
        transaction.hide(it)
    }

    transaction.setReorderingAllowed(true)
    transaction.commit()

    dispatchOnDestinationChanged()

    return true
}

/**
 * When navigation is ContainerHostFragment, use this method to
 * back to previous container fragment if back key pressed.
 *
 * This method will be find a top-level container recursively,
 * if the container is ContainerHostFragment then hide top fragment in it.
 */
fun NavController.navigateBack(@IdRes containerId: Int): Boolean {
    if (mContext is AppCompatActivity) {
        val container = (mContext as AppCompatActivity).supportFragmentManager.findFragmentById(containerId)
        if (container is ContainerHostFragment) {
            return container.popBackStack()
        }
    }
    return false
}

/**
 * Porting from mOnBackPressListener of NavController
 */
fun NavController.onPopBackStack(@NonNull navigator: BaseNavigator) {
    // Find what destination just got popped
    var lastFromNavigator: NavDestination? = null
    val iterator = mBackStack.descendingIterator()
    while (iterator.hasNext()) {
        val destination = iterator.next().destination
        val currentNavigator = navigatorProvider.getNavigator<BaseNavigator>(destination.navigatorName)
        if (currentNavigator == navigator) {
            lastFromNavigator = destination
            break
        }
    }
    if (lastFromNavigator == null) {
        return
    }
    // Pop all intervening destinations from other Navigators off the
    // back stack
    popBackStackInternal(lastFromNavigator.id, false)
    dispatchOnDestinationChanged()
}

fun NavController.onBackStackChanged(fragmentManager: FragmentManager, pending: Boolean): Boolean {
    // If we have pending operations made by us then consume this change, otherwise
    // detect a pop in the back stack to dispatch callback.
    if (pending) {
        return !isBackStackEqual(fragmentManager)
    }

    // The initial Fragment and NavGraph won't be on the back stack, so the
    // real count of destinations is the back stack entry count + 2
    val newCount = fragmentManager.backStackEntryCount + 2
    if (newCount < mBackStack.size) {
        // Handle cases where the user hit the system back button
        while (mBackStack.size > newCount) {
            mBackStack.removeLast()
        }

        val navigator = getCurrentFragmentNavigator() as? BaseNavigator ?: return pending
        onPopBackStack(navigator)
    }

    return pending
}

fun NavController.isDestinationExists(): Boolean {
    // first item is NavGraph
    return mBackStack.isNotEmpty() && mBackStack.size > 1
}

fun NavController.getDestinationCount(): Int {
    return if (isDestinationExists()) mBackStack.size - 1 else 0
}

fun NavController.getDestinations(): List<Int> {
    return mBackStack.filter {
        it.destination is FragmentNavigator.Destination
    }.map {
        it.destination.id
    }
}

fun NavController.getCurrentFragmentNavigator(): FragmentNavigator {
    return navigatorProvider.getNavigator(currentDestination!!.navigatorName)
}

/**
 * Porting from isBackStackEqual of FragmentNavigator
 */
internal fun NavController.isBackStackEqual(fragmentManager: FragmentManager): Boolean {
    val fragmentBackStackCount = fragmentManager.backStackEntryCount
    // Initial fragment and NavGraph won't be on the FragmentManager's back stack so +2 its count.
    if (mBackStack.size != fragmentBackStackCount + 2) {
        return false
    }

    // From top to bottom verify destination ids match in both back stacks/
    val backStackIterator = mBackStack.descendingIterator()
    var fragmentBackStackIndex = fragmentBackStackCount - 1
    while (backStackIterator.hasNext() && fragmentBackStackIndex >= 0) {
        val destId = backStackIterator.next().destination.id
        val fragmentDestId = fragmentManager
                .getBackStackEntryAt(fragmentBackStackIndex--)
                .name
        if (fragmentDestId != null && destId != fragmentDestId.toInt()) {
            return false
        }
    }

    return true
}

fun Fragment.findNavigator(): FragmentNavigator? {
    val navController = findNavController()
    val navigators = navController.navigatorProvider.navigators.map {
        it.value
    }.filter {
        it is BaseNavigator
    }.map {
        it as BaseNavigator
    }.filter {
        it.isContains(this)
    }
    return if (navigators.isNotEmpty()) navigators.first() else null
}

fun Fragment.isKeepAlive(): Boolean {
    val navigator = findNavigator()
    return navigator is KeepAliveNavigator
}