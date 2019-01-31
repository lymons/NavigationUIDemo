package androidx.navigation.fragment

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigator
import androidx.navigation.onBackStackChanged
import androidx.navigation.plusAssign

class HybridHostFragment: NavHostFragment() {

    private var mIsPendingBackStackOperation = false
    @IdRes
    private var mNewGraphId: Int? = null

    private lateinit var mContainer: HybridNavigator
    private val mBackStackListener = FragmentManager.OnBackStackChangedListener {
        mIsPendingBackStackOperation = navController.onBackStackChanged(childFragmentManager, mIsPendingBackStackOperation)
    }

    fun setPendingOperation(pending: Boolean) {
        mIsPendingBackStackOperation = pending
    }

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        mContainer = HybridNavigator(this, childFragmentManager, id)
        return mContainer
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        NavHostFragment::class.java.getDeclaredField("mGraphId").let {
            it.isAccessible = true
            val id = it.getInt(this)
            if (id != 0) {
                mNewGraphId = id
                it.setInt(this, 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNewGraphId?.let {
            val navigator = KeepAliveNavigator(this, childFragmentManager, id)
            navController.navigatorProvider += navigator
            navController.setGraph(it)
            navigator.removeBackStackListener()
        }

        /**
         * replace backStack changed listener after addNavigator
         */
        mContainer.removeBackStackListener()
        childFragmentManager.addOnBackStackChangedListener(mBackStackListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        childFragmentManager.removeOnBackStackChangedListener(mBackStackListener)
    }
}