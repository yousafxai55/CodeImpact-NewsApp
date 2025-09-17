package com.mobileandroid.appsnews.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.bookmarks.Bookmark

class BookmarksAdapter(
    private var bookmarks: List<Bookmark>,
    private val onItemClick: (Bookmark) -> Unit,
    private val onDeleteClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>(), Filterable {

    private var bookmarkListAll: List<Bookmark> = ArrayList(bookmarks)

    fun updateData(newBookmarks: List<Bookmark>) {
        bookmarks = newBookmarks
        bookmarkListAll = ArrayList(newBookmarks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.common_item_list, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = bookmarks[position]
        holder.bind(bookmark, onItemClick, onDeleteClick)
    }

    override fun getItemCount(): Int = bookmarks.size

//    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val imageView: ImageView = itemView.findViewById(R.id.BookMark_newsIv)
//        private val sourceTextView: TextView = itemView.findViewById(R.id.BookMark_newsSource)
//        private val titleTextView: TextView = itemView.findViewById(R.id.BookMark_newsTitle)
//
//        fun bind(bookmark: Bookmark, onItemClick: (Bookmark) -> Unit, onDeleteClick: (Bookmark) -> Unit) {
//            titleTextView.text = bookmark.postTitle
//            sourceTextView.text = bookmark.postUrl // Agar source alag ho toh yahan update karein
//
//            // Agar bookmark ka image URL hai, toh load karein, warna default image set karein
//            if (!bookmark.postUrl.isNullOrEmpty()) {
//                imageView.load(bookmark.postUrl) {
//                    placeholder(R.drawable.ic_launcher_foreground)
//                    error(R.drawable.ic_launcher_background)
//                }
//            } else {
//              //  imageView.setImageResource(R.drawable.placeholder_image)
//            }
//
//            itemView.setOnClickListener { onItemClick(bookmark) }
//            itemView.setOnLongClickListener {
//                onDeleteClick(bookmark)
//                true
//            }
//        }
//    }


    // ViewHolder class
    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.BookMark_newsIv)
        private val sourceTextView: TextView = itemView.findViewById(R.id.BookMark_newsSource)
        private val titleTextView: TextView = itemView.findViewById(R.id.BookMark_newsTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.BookMark_pubDate)

        fun bind(bookmark: Bookmark, onItemClick: (Bookmark) -> Unit, onDeleteClick: (Bookmark) -> Unit) {
            // Set title
            titleTextView.text = bookmark.postTitle

            // Set source
            sourceTextView.text = bookmark.postSource
            // Set date
            dateTextView.text = bookmark.pubDate ?: "No date available"

            // Load image using Coil
            if (!bookmark.postImage.isNullOrEmpty()) {
                imageView.load(bookmark.postImage) {
                    crossfade(true)
                    placeholder(R.drawable.baseline_thumb_up_24)
                    error(R.drawable.ic_launcher_background)
                }
            } else {
                imageView.setImageResource(R.drawable.menu_ic_translate)
            }

            // Click listeners
            itemView.setOnClickListener { onItemClick(bookmark) }
            itemView.setOnLongClickListener {
                onDeleteClick(bookmark)
                true
            }
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<Bookmark>()

                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(bookmarkListAll)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (bookmark in bookmarkListAll) {
                        if (bookmark.postTitle?.lowercase()?.contains(filterPattern) ?: false) {
                            filteredList.add(bookmark)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                bookmarks = results?.values as List<Bookmark>
                notifyDataSetChanged()
            }
        }
    }
}
