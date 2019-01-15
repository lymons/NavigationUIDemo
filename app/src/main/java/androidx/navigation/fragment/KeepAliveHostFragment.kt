package androidx.navigation.fragment

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigator

class KeepAliveHostFragment: NavHostFragment() {

    @Navigator.Name("fragment")
    private class InternalKeepAliveNavigator constructor(context: Context,
                                                         manager: FragmentManager,
                                                         containerId: Int
    ) : KeepAliveNavigator(context, manager, containerId) {
        override fun getValidateHostClassName(): String = (KeepAliveHostFragment::class.java).name
    }

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return InternalKeepAliveNavigator(requireContext(), childFragmentManager, id)
    }
}
