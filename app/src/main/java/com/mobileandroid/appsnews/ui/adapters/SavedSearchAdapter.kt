package com.mobileandroid.appsnews.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.databinding.SavedSearchItemBinding
import com.mobileandroid.appsnews.savedsearches.SavedSearches


class SavedSearchAdapter(
    private var searchList: List<SavedSearches>,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (SavedSearches) -> Unit
) : RecyclerView.Adapter<SavedSearchAdapter.SearchViewHolder>() {

    fun updateData(newList: List<SavedSearches>) {
        searchList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_search_item, parent, false)
        val binding = SavedSearchItemBinding.bind(view)
        return SearchViewHolder(binding)
    }

//    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
//        val search = searchList[position]
//        holder.binding.SaveSearchTvTitle.text = search.searchTopic
//        holder.itemView.setOnClickListener { onItemClick(search.searchTopic) }
//    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val search = searchList[position]
        holder.binding.SaveSearchTvTitle.text = search.searchTopic
        holder.itemView.setOnClickListener { onItemClick(search.searchTopic) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(search) // 🛠 Long click to delete
            true
        }
    }


    override fun getItemCount(): Int = searchList.size

    class SearchViewHolder(val binding: SavedSearchItemBinding) : RecyclerView.ViewHolder(binding.root)
}
