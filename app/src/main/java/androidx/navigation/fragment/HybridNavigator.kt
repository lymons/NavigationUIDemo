package androidx.navigation.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigator

@Navigator.Name("fragment")
class HybridNavigator(
        private val context: Context,
        private val manager: FragmentManager,
        private val containerId: Int
) : BaseNavigator(context, manager, containerId) {

    override fun handleFragment(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment {
        val tag = destination.id.toString()

        val fragment = instantiateFragment(context, manager, getDestinationClassName(destination), args)
        fragment.arguments = args

        val currentFragment = manager.primaryNavigationFragment
        if (currentFragment != null) {
            if (isKeepAliveFragment(currentFragment)) {
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

    override fun getValidateHostClassName(): String = (HybridHostFragment::class.java).name
}