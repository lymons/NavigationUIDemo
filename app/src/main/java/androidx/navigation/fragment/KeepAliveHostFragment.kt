package androidx.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigator

class KeepAliveHostFragment: NavHostFragment() {

    @Navigator.Name("fragment")
    private class InternalKeepAliveNavigator constructor(host: Fragment,
                                                         manager: FragmentManager,
                                                         containerId: Int
    ) : KeepAliveNavigator(host, manager, containerId) {
        override fun getValidateHostClassName(): String = (KeepAliveHostFragment::class.java).name
    }

    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return InternalKeepAliveNavigator(this, childFragmentManager, id)
    }
}
