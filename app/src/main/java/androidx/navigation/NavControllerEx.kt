package androidx.navigation

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.ContainerHostFragment
import androidx.navigation.fragment.FragmentNavigator

fun NavController.popBackStackContainer(host: ContainerHostFragment): Boolean {
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

    return true
}

fun NavController.popBackStackContainer(@IdRes containerId: Int): Boolean {
    if (mContext is AppCompatActivity) {
        val container = (mContext as AppCompatActivity).supportFragmentManager.findFragmentById(containerId)
        if (container is ContainerHostFragment) {
            return container.popBackStack()
        }
    }
    return false
}