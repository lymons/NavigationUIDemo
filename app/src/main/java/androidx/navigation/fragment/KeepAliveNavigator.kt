package androidx.navigation.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigator

@Navigator.Name("keep_alive_fragment") // `keep_alive_fragment` is used in navigation xml
open class KeepAliveNavigator(
        val context: Context,
        val manager: FragmentManager, // Should pass childFragmentManager.
        private val containerId: Int
) : BaseNavigator(context, manager, containerId) {

    override fun getValidateHostClassName(): String = (HybridHostFragment::class.java).name

    override fun handleFragment(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment {
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
            fragment = instantiateFragment(context, manager, getDestinationClassName(destination), args)
            fragment.arguments = args
            setKeepAliveFlag(fragment)

            transaction.add(containerId, fragment, tag)
        } else {
            transaction.show(fragment)
        }

        return fragment
    }

    override fun onRestoreState(savedState: Bundle?) {
        super.onRestoreState(savedState)

        savedState?.let {
            val transaction = manager.beginTransaction()
            mBackStack.forEach { tag ->
                if (tag != mBackStack.peek()) {
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