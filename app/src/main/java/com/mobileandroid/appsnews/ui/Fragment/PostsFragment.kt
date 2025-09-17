package com.mobileandroid.appsnews.ui.Fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.api.FetchResponse
import com.mobileandroid.appsnews.databinding.FragmentPostsBinding
import com.mobileandroid.appsnews.repository.NewsRepository
import com.mobileandroid.appsnews.ui.adapters.PostAdapter
import com.mobileandroid.appsnews.utils.Utils
import com.mobileandroid.appsnews.viewmodels.NewsItemViewModel
import com.mobileandroid.appsnews.viewmodels.NewsVMFactory


class PostsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewsItemViewModel
    private lateinit var adapter: PostAdapter
    private val itemList: ArrayList<Map<String, String>> = ArrayList()

    private var searchQuery: String? = null
    private var mPage = 1
    private var mPreviousItemNum = 0
    private var mItemNum = 0
    private var isLoading = false
    private lateinit var layoutManager: LinearLayoutManager
    private val mContext: Context? = null
    private val utils = Utils()
    private val allItems = ArrayList<Map<String, String>>() // full list



    private var mPastVisibleItems = 0
    private var mVisibleItemCount = 0


    private var onDataLoaded: (() -> Unit)? = null  //  callback

    fun setOnDataLoadedListener(listener: () -> Unit) {
        onDataLoaded = listener
    }

    companion object {
        fun newInstance(query: String): PostsFragment {
            val fragment = PostsFragment()
            val args = Bundle()
            args.putString("searchQuery", query)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = arguments?.getString("searchQuery")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fetchResponse = FetchResponse()
        val repository = NewsRepository(fetchResponse)
        val factory = NewsVMFactory(repository)
        viewModel = ViewModelProvider(this, factory)[NewsItemViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        loadFirstPage()


        if (utils.isNetworkAvailable(requireContext())){
            viewModel.loadNextBatch()
        }else{
            showNoInternetDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)

        //  Agar query already pass hui hai (MainActivity se)
        if (!searchQuery.isNullOrEmpty()) {
            binding.tvLoading.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.tvNoNewsFound.visibility = View.GONE

    }
 }



    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        adapter = PostAdapter(requireContext(), itemList)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                mVisibleItemCount = layoutManager.childCount
                mPastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (mItemNum > mPreviousItemNum && itemList.isNotEmpty() && mVisibleItemCount != 0 &&
                    totalItemCount > mVisibleItemCount && !isLoading &&
                    (mVisibleItemCount + mPastVisibleItems) >= totalItemCount) {
                    loadNextPage()
                    mPreviousItemNum = mItemNum
                }
            }
        })
    }

//    private fun filterItems(query: String?) {
//        itemList.clear()
//        if (!query.isNullOrEmpty()) {
//            val filtered = allItems.filter { item ->
//                item["title"]?.contains(query, ignoreCase = true) == true ||
//                        item["description"]?.contains(query, ignoreCase = true) == true ||
//                        item["image"]?.contains(query, ignoreCase = true) == true
//            }
//            itemList.addAll(filtered)
//        } else {
//            itemList.addAll(allItems)
//        }
//        adapter.notifyDataSetChanged()
//
//        binding.tvNoNewsFound.visibility = if (itemList.isEmpty()) View.VISIBLE else View.GONE
//        binding.recyclerView.visibility = if (itemList.isNotEmpty()) View.VISIBLE else View.GONE
//    }

    private fun filterItems(query: String?) {
        itemList.clear()
        if (!query.isNullOrEmpty()) {
            val filtered = allItems.filter { item ->
                item["title"]?.contains(query, ignoreCase = true) == true ||
                        item["description"]?.contains(query, ignoreCase = true) == true ||
                        item["image"]?.contains(query, ignoreCase = true) == true
            }
            itemList.addAll(filtered)
        } else {
            itemList.addAll(allItems)
        }
        adapter.notifyDataSetChanged()
    }


    private fun loadFirstPage() {
        mPage = 1
        mPreviousItemNum = 0
        mItemNum = 0
        loadItems(false)
    }

    private fun loadNextPage() {
        mPage++
        loadItems(true)
    }

    private fun loadItems(showLoading: Boolean) {
        if (showLoading) {
            Log.d("LoadItems", "Loading items - Page: $mPage")
        }

        isLoading = true
        val start = (mPage - 1) * 10
        val limit = 10
        viewModel.loadNextBatch()
    }

//    private fun setupObservers() {
//        viewModel.newsLiveData.observe(viewLifecycleOwner) { items ->
//            binding.swipeRefreshLayout.isRefreshing = false
//            isLoading = false
//
//            binding.progressBar.visibility = View.GONE
//            binding.tvLoading.visibility = View.GONE
//
//            val filteredItems = if (!searchQuery.isNullOrEmpty()) {
//                items.filter { item ->
//                    item["title"]?.contains(searchQuery!!, ignoreCase = true) == true ||
//                            item["description"]?.contains(searchQuery!!, ignoreCase = true) == true ||
//                            item["image"]?.contains(searchQuery!!, ignoreCase = true) == true
//                }
//            } else {
//                items
//            }
//
//            if (mPage == 1) {
//                itemList.clear()
//            }
//
//            itemList.addAll(filteredItems)
//            mItemNum = itemList.size
//            adapter.notifyDataSetChanged()
//
//            binding.tvNoNewsFound.visibility = if (itemList.isEmpty()) View.VISIBLE else View.GONE
//            binding.recyclerView.visibility = if (itemList.isNotEmpty()) View.VISIBLE else View.GONE
//
//            if (mPage != 1) {
//                layoutManager.scrollToPosition(mPastVisibleItems + mVisibleItemCount - 1)
//            }
//        }
//    }


    private fun setupObservers() {
        viewModel.newsLiveData.observe(viewLifecycleOwner) { items ->
            binding.swipeRefreshLayout.isRefreshing = false
            isLoading = false
            binding.progressBar.visibility = View.GONE
            binding.tvLoading.visibility = View.GONE

            if (mPage == 1) {
                allItems.clear()
            }
            allItems.addAll(items)
            mItemNum = allItems.size

            filterItems(searchQuery)

            //  Agar filter ke baad empty ho
            if (itemList.isEmpty()) {
                binding.tvNoNewsFound.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvNoNewsFound.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }

            //  Notify SearchActivity that data is ready
            onDataLoaded?.invoke()
        }
    }


//    private fun setupObservers() {
//        viewModel.newsLiveData.observe(viewLifecycleOwner) { items ->
//            binding.swipeRefreshLayout.isRefreshing = false
//            isLoading = false
//            binding.progressBar.visibility = View.GONE
//            binding.tvLoading.visibility = View.GONE
//
//            if (mPage == 1) {
//                allItems.clear()
//            }
//
//            allItems.addAll(items)
//            mItemNum = allItems.size
//
//            filterItems(searchQuery) // apply current search
//        }
//    }

//    fun updateSearchQuery(newQuery: String) {
//        searchQuery = newQuery
//        itemList.clear()
//
//        viewModel.newsLiveData.value?.let { items ->
//            val filteredItems = items.filter { item ->
//                item["title"]?.contains(newQuery, ignoreCase = true) == true ||
//                        item["description"]?.contains(newQuery, ignoreCase = true) == true ||
//                        item["image"]?.contains(newQuery, ignoreCase = true) == true
//            }
//            itemList.addAll(filteredItems)
//        }
//
//        adapter.notifyDataSetChanged()
//        binding.tvNoNewsFound.visibility = if (itemList.isEmpty()) View.VISIBLE else View.GONE
//    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
        itemList.clear()
        adapter.notifyDataSetChanged()

        //  Show Loading while fetching
        binding.tvLoading.visibility = View.VISIBLE
        binding.tvNoNewsFound.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        // Force reload from ViewModel
        viewModel.loadNextBatch()
    }



    private fun showNoInternetDialog() {
        AlertDialog.Builder(mContext)
            .setTitle(getString(R.string.dialog_title_no_internet))
            .setMessage(getString(R.string.dialog_message_no_internet))
            .setCancelable(false)
            .setPositiveButton(
                "Retry"
            ) { dialog: DialogInterface?, which: Int -> loadFirstPage() }
            .setNeutralButton(
                "Report a problem"
            ) { dialog: DialogInterface?, which: Int ->
            }
            .setNegativeButton(
                "Exit"
            ) { dialog: DialogInterface?, which: Int -> requireActivity().finish() }
            .create()
            .show()
    }

    override fun onRefresh() {
        itemList.clear()
        adapter.notifyDataSetChanged()
        mPage = 1
        mPreviousItemNum = 0
        mItemNum = 0

        viewModel.loadNextBatch {
            loadFirstPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }
}
