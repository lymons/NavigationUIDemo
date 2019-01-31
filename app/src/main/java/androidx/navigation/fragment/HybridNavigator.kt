package androidx.navigation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigator
import androidx.navigation.isKeepAlive

@Navigator.Name("fragment")
class HybridNavigator(
        private val host: Fragment,
        private val manager: FragmentManager,
        private val containerId: Int
) : BaseNavigator(host, manager, containerId) {

    override fun showDestination(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment {
        val tag = destination.id.toString()

        val fragment = instantiateFragment(host.requireContext(), manager, getDestinationClassName(destination), args)
        fragment.arguments = args
        store(fragment)

        val currentFragment = manager.primaryNavigationFragment
        if (currentFragment != null) {
            if (currentFragment.isKeepAlive()) {
                transaction.hide(currentFragment)
                transaction.add(containerId, fragment, tag)
            } else {
                transaction.replace(containerId, fragment)
            }
        } else {
            transaction.replace(containerId, fragment)
        }

        return fragment
    }

    override fun setPendingOperation(pending: Boolean) {
        (host as HybridHostFragment).setPendingOperation(pending)
    }

    override fun getValidateHostClassName(): String = (HybridHostFragment::class.java).name
}