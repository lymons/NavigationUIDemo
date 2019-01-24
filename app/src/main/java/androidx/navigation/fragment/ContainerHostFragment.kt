package androidx.navigation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.*

class ContainerHostFragment: NavHostFragment() {

    @Navigator.Name("fragment")
    private class ContainerKeepAliveNavigator constructor(host: Fragment,
                                                         manager: FragmentManager,
                                                         containerId: Int
    ) : KeepAliveNavigator(host, manager, containerId) {
        override fun getValidateHostClassName(): String = (ContainerHostFragment::class.java).name

        /**
         * Should not put container fragment into backStack to avoid destroy when tab was clicked
         */
        override fun handleBackStack(transaction: FragmentTransaction, destination: Destination, navOptions: NavOptions?): Boolean
                = true
    }

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return ContainerKeepAliveNavigator(this, childFragmentManager, id)
    }

    fun popBackStack(): Boolean {
        val fragment = childFragmentManager.primaryNavigationFragment
        val tops = fragment?.childFragmentManager?.fragments?.filterNotNull()?.filter {
            it.isVisible
        }

        var resumed = false
        if (tops != null && tops.isNotEmpty()) {
            if (tops[0] is ContainerHostFragment) {
                val host = tops[0] as ContainerHostFragment
                resumed = host.popBackStack()
            }
        }

        return if (resumed) resumed else navController.popBackStackContainer(this)
    }

    fun instantiateFragment(provider: NavigatorProvider, destination: FragmentNavigator.Destination, args: Bundle?): Fragment {
        val navigator = provider.getNavigator(ContainerKeepAliveNavigator::class.java)
        val fragment = navigator.instantiateFragment(requireContext(), childFragmentManager, navigator.getDestinationClassName(destination), args)
        fragment.arguments = args
        return fragment
    }
}
