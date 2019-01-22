package androidx.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

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
                = !mBackStack.contains(destination.id)
    }

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return ContainerKeepAliveNavigator(this, childFragmentManager, id)
    }
}
