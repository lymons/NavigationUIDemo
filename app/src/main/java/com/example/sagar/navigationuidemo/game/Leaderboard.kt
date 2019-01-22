/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sagar.navigationuidemo.game

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.example.sagar.navigationuidemo.R
import com.example.sagar.navigationuidemo.VerboseFragment
import kotlinx.android.synthetic.main.fragment_leaderboard.*

/**
 * Shows a static leaderboard with three users.
 */
class Leaderboard : VerboseFragment() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewAdapter = MyAdapter(arrayOf("Flo", "Ly", "Jo"))

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        leaderboard_list.setHasFixedSize(true)

        // specify an viewAdapter (see also next example)
        leaderboard_list.adapter = viewAdapter
    }
}

class MyAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val item: View) : RecyclerView.ViewHolder(item)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_view_item, parent, false)


        return ViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.item.findViewById<TextView>(R.id.user_name_text).text = myDataset[position]

        holder.item.findViewById<ImageView>(R.id.user_avatar_image)
                .setImageResource(listOfAvatars[position])

        holder.item.setOnClickListener {
            val bundle = bundleOf("userName" to myDataset[position])

            Navigation.findNavController(holder.item).navigate(
                    R.id.action_leaderboard_to_userProfile,
                bundle)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

private val listOfAvatars = listOf(
        R.drawable.avatar_1_raster, R.drawable.avatar_2_raster, R.drawable.avatar_3_raster)
