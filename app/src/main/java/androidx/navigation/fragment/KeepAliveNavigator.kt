package androidx.navigation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.Navigator

@Navigator.Name("keep_alive_fragment") // `keep_alive_fragment` is used in navigation xml
open class KeepAliveNavigator(
        private val host: Fragment,
        private val manager: FragmentManager, // Should pass childFragmentManager.
        private val containerId: Int
) : BaseNavigator(host, manager, containerId) {

    override fun getValidateHostClassName(): String = (HybridHostFragment::class.java).name

    override fun showDestination(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment {
        val tag = destination.id.toString()
        val currentFragment = manager.primaryNavigationFragment
        if (currentFragment != null) {
            if (isKeepAliveFragment(currentFragment)) {
                transaction.hide(currentFragment)
            } else {
                transaction.remove(currentFragment)
            }
        }

        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = instantiateFragment(host.requireContext(), manager, getDestinationClassName(destination), args)
            fragment.arguments = args
            setKeepAliveFlag(fragment)

            transaction.add(containerId, fragment, tag)
        } else {
            transaction.show(fragment)
        }

        return fragment
    }

    override fun handleBackStack(transaction: FragmentTransaction, destination: Destination, navOptions: NavOptions?): Boolean {
        if (mBackStack.peekLast() != destination.id) {
            transaction.addToBackStack(Integer.toString(destination.id))
            mIsPendingBackStackOperation = true
            return true
        }
        return false
    }

    override fun isKeepAliveNavigator() = true

    override fun onRestoreState(savedState: Bundle?) {
        super.onRestoreState(savedState)

        savedState?.let {
            val activity = host.requireActivity()
            val navController = Navigation.findNavController(activity, containerId)
            val top = navController.currentDestination
            val transaction = manager.beginTransaction()
            mBackStack.forEach { tag ->
                if (tag != top!!.id) {
                    val fragment = manager.findFragmentByTag(it.toString())
                    if (fragment != null && isKeepAliveFragment(fragment)) {
                        transaction.hide(fragment)
                    }
                }
            }
            transaction.commit()
        }
    }
}