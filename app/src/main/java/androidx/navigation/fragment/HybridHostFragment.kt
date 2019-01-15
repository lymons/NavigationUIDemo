package androidx.navigation.fragment

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.IdRes
import androidx.navigation.Navigator
import androidx.navigation.plusAssign

class HybridHostFragment: NavHostFragment() {

    @IdRes
    private var mNewGraphId: Int? = null

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return HybridNavigator(requireContext(), childFragmentManager, id)
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
            val navigator = KeepAliveNavigator(requireContext(), childFragmentManager, id)
            navController.navigatorProvider += navigator
            navController.setGraph(it)
        }
    }
}