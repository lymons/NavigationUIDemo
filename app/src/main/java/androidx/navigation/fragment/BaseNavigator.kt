package androidx.navigation.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

abstract class BaseNavigator(
        private val context: Context,
        private val manager: FragmentManager,
        containerId: Int
) : FragmentNavigator(context, manager, containerId) {

    private val keepAliveSet = HashSet<String>()

    init {
        if (context is FragmentActivity) {
            val fragment = context.supportFragmentManager.findFragmentById(containerId)!!
            @Suppress("LeakingThis")
            val validateName = getValidateHostClassName()
            assert(validateName == fragment.javaClass.name) {
                val className = javaClass.simpleName
                Log.e(className, "$className must be use with $validateName.")
            }
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

        val fragment = handleFragment(transaction, destination, args)
        transaction.setPrimaryNavigationFragment(fragment)
        transaction.setReorderingAllowed(true)

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
        if (navigatorExtras is Extras) {
            for ((key, value) in navigatorExtras.sharedElements) {
                transaction.addSharedElement(key, value)
            }
        }

        transaction.commit()

        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId)
            return destination
        }
        return null
    }

    fun getDestinationClassName(destination: Destination): String {
        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }

        return className
    }

    /**
     * Judge this fragment being keep alive in current Navigator.
     *  *** Attention: this fragment may be not a keep alive mode in other Navigator.
     */
    public fun isKeepAliveFragment(fragment: Fragment): Boolean = keepAliveSet.contains(fragment.javaClass.name)

    fun setKeepAliveFlag(fragment: Fragment) = keepAliveSet.add(fragment.javaClass.name)

    protected abstract fun handleFragment(transaction: FragmentTransaction, destination: Destination, args: Bundle?): Fragment

    protected abstract fun getValidateHostClassName(): String
}