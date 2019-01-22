package androidx.navigation.fragment

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

abstract class BaseNavigator(
        private val hostFragment: Fragment,
        private val manager: FragmentManager,
        containerId: Int
) : FragmentNavigator(hostFragment.requireContext(), manager, containerId) {

    private val keepAliveSet = HashSet<String>()

    init {
        @Suppress("LeakingThis")
        val validateName = getValidateHostClassName()
        assert(validateName == hostFragment.javaClass.name) {
            val className = javaClass.simpleName
            Log.e(className, "$className must be use with $validateName.")
        }
    }

    /**
     * Porting from method of super.
     * The different is changing replace to add/attach of fragment.
     */
    override fun navigate(
            destination: Destination,
            args: Bundle?,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        val transaction = manager.beginTransaction()

        if (manager.isStateSaved) {
            Log.i("KeepAliveNavigator", "Ignoring navigate() call: FragmentManager has already" + " saved its state")
            return null
        }

        var enterAnim = navOptions?.enterAnim ?: -1
        var exitAnim = navOptions?.exitAnim ?: -1
        var popEnterAnim = navOptions?.popEnterAnim ?: -1
        var popExitAnim = navOptions?.popExitAnim ?: -1
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = if (enterAnim != -1) enterAnim else 0
            exitAnim = if (exitAnim != -1) exitAnim else 0
            popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
            popExitAnim = if (popExitAnim != -1) popExitAnim else 0
            transaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }

        val fragment = showDestination(transaction, destination, args)
        transaction.setPrimaryNavigationFragment(fragment)

        val isAdded = handleBackStack(transaction, destination, navOptions)

        if (navigatorExtras is Extras) {
            for ((key, value) in navigatorExtras.sharedElements) {
                transaction.addSharedElement(key, value)
            }
        }

        transaction.setReorderingAllowed(true)
        transaction.commit()

        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destination.id)
            return destination
        }
        return null
    }

    /**
     * Default is put fragment into backStack when navigation happened.
     * Return value:
     *     true if record this navigation.
     */
    protected open fun handleBackStack(transaction: FragmentTransaction, destination: Destination, navOptions: NavOptions?): Boolean {
        val isAdded: Boolean
        @IdRes val destId = destination.id
        val initialNavigation = mBackStack.isEmpty()
        // TODO Build first class singleTop behavior for fragments
        val isSingleTopReplacement = (navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId)

        when {
            initialNavigation -> isAdded = true
            isSingleTopReplacement -> {
                // Single Top means we only want one instance on the back stack
                if (mBackStack.size > 1) {
                    // If the Fragment to be replaced is on the FragmentManager's
                    // back stack, a simple replace() isn't enough so we
                    // remove it from the back stack and put our replacement
                    // on the back stack in its place
                    manager.popBackStack()
                    transaction.addToBackStack(Integer.toString(destId))
                    mIsPendingBackStackOperation = true
                }
                isAdded = false
            }
            else -> {
                transaction.addToBackStack(Integer.toString(destId))
                mIsPendingBackStackOperation = true
                isAdded = true
            }
        }

        return isAdded
    }

    protected open fun isKeepAliveNavigator() = false

    fun getDestinationClassName(destination: Destination): String {
        var className = destination.className
        if (className[0] == '.') {
            className = hostFragment.requireContext().packageName + className
        }

        return className
    }

    /**
     * Judge this fragment being keep alive in current Navigator.
     *  *** Attention: this fragment may be not a keep alive mode in other Navigator.
     */
    public fun isKeepAliveFragment(fragment: Fragment): Boolean = keepAliveSet.contains(fragment.javaClass.name)

    fun setKeepAliveFlag(fragment: Fragment) = keepAliveSet.add(fragment.javaClass.name)

    /**
     * Show specified fragment by destination.
     * Return value:
     *          True this fragment be created because of no exists.
     *          False this fragment exists.
     */
    protected abstract fun showDestination(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment

    protected abstract fun getValidateHostClassName(): String
}