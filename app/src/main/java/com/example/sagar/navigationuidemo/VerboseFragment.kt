package com.example.sagar.navigationuidemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

open class VerboseFragment : Fragment() {

    private fun trace() {
        Log.v("LifeCycle Calling", javaClass.simpleName +
                "(${hashCode()}): " +
                Thread.currentThread().stackTrace[3].methodName)
    }

    private fun trace(suffix: String?) {
        val msg = if (suffix != null) " $suffix" else ""
        Log.v("LifeCycle Calling", javaClass.simpleName +
                "(${hashCode()}): " +
                Thread.currentThread().stackTrace[3].methodName +
                msg
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        trace()
    }

    override fun onPause() {
        super.onPause()
        trace()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trace()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        trace()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trace()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        trace()
    }

    override fun onStart() {
        super.onStart()
        trace()
    }

    override fun onResume() {
        super.onResume()
        trace()
    }

    override fun onDetach() {
        super.onDetach()
        trace()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        trace()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        trace()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        trace()
    }

    override fun onStop() {
        super.onStop()
        trace()
    }

    override fun onDestroy() {
        super.onDestroy()
        trace()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        trace("on " + if (hidden) "Hidden" else "Showed")
    }
}