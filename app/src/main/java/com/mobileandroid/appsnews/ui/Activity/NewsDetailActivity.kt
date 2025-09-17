package com.mobileandroid.appsnews.ui.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.bookmarks.Bookmark
import com.mobileandroid.appsnews.databinding.ActivityNewsDetailBinding
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewsDetailBinding
    private var descriptionModified: String? = null
    private var isBookmarked: Boolean = false
    private lateinit var databaseViewModel: DatabaseViewModel

    private var title: String? = null
    private var imageUrl: String? = null
    private var source: String? = null
    private var link: String? = null

    private fun getSourceLink(link: String?): String {
        return when {
            link == null -> "UNKNOWN SOURCE"
            link.contains("https://www.suchtv.pk") -> "SUCHTV.PK"
            link.contains("https://dailypakistan.com.pk/") -> "DAILYPAKISTAN.COM.PK"
            link.contains("https://www.news.com.au") -> "NEWS.COM.AU"
            link.contains("https://www.independent.co.uk/") -> "INDEPENDENT.CO.UK"
            link.contains("https://www.smh.com.au/") -> "SMH.COM.AU"
            link.contains("https://thewest.com.au") -> "WEST.COM.AU"
            link.contains("https://www.perthnow.com.au/") -> "PERTHNOW.COM.AU"
            link.contains("https://www.theage.com.au/") -> "THEAGE.COM.AU"
            link.contains("https://www.canberratimes.com.au/") -> "CANBERRATIMES.COM.AU"
            link.contains("https://www.cbc.ca/") -> "CBC.CA"
            link.contains("https://feeds.thelocal.com/") -> "THELOCAL.COM"
            link.contains("https://www.france24.com/") -> "FRANCE24.COM"
            link.contains("https://www.hongkongfp.com/") -> "HONGKONGFP.COM"
            link.contains("https://www.republika.co.id/") -> "REPUBLIKA.CO.ID"
            link.contains("https://feeds.breakingnews.ie/") -> "BREAKINGNEWS.IE"
            link.contains("https://www.news18.com/") -> "NEWS18.COM"
            link.contains("https://www.indiatoday.in/") -> "INDIATODAY.IN"
            link.contains("https://www.newsweek.pl/") -> "NEWSWEEK.PL"
            link.contains("https://www.rt.com/") -> "RT.COM"
            link.contains("https://rss.unian.net/") -> "UNIAN.NET"
            link.contains("https://rss.nytimes.com/") -> "NEWYORKTIMES.COM"
            link.contains("https://www.sowetanlive.co.za/") -> "SOWETANLIVE.CO.ZA"
            link.contains("https://www.space.com/") -> "SPACE.COM"
            else -> "UNKNOWN SOURCE"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]

        try {
            title = intent.getStringExtra("title")
            imageUrl = intent.getStringExtra("imageLink")
            val description = intent.getStringExtra("description")
            val subTitle = intent.getStringExtra("subTitle")
            link = intent.getStringExtra("link")
            source = getSourceLink(link)

            if (description != null) {
                val document = Jsoup.parse(description)
                descriptionModified = document.text()
            }

            //  Setup Toolbar with Back Arrow + Trimmed Title
            setSupportActionBar(binding.toolbarNewsDetail)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow_icon)
            val trimmedTitle = getTrimmedTitle(title)
            supportActionBar?.title = trimmedTitle

            val shortTitle = getTrimmedTitle(title, 2)
            supportActionBar?.title = shortTitle
            Log.d("ToolbarTitle", "Full Title: $title")
            Log.d("ToolbarTitle", "Trimmed Title: $trimmedTitle")

            // 🔹 Load image with Glide

            // 🔹 Load image with Glide + Caching
            if (imageUrl != null) {
                val startTime = System.currentTimeMillis()
                Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //  Cache both original & resized
//                    .placeholder(R.drawable.placeholder_image) // optional placeholder
//                    .error(R.drawable.error_image) // optional error image
                    .into(binding.newsImageViewInDetail)

                val endTime = System.currentTimeMillis()
                val timeTaken = endTime - startTime
                Log.d("ImageLoadTime", "Detail image loaded (Glide) in $timeTaken ms, URL: $imageUrl")
            }

//            if (imageUrl != null) {
//                val startTime = System.currentTimeMillis()
//                Picasso.get()
//                    .load(imageUrl)
//                    .into(binding.newsImageViewInDetail, object : Callback {
//                        override fun onSuccess() {
//                            val endTime = System.currentTimeMillis()
//                            val timeTaken = endTime - startTime
//                            Log.d("ImageLoadTime", "Detail image loaded in $timeTaken ms, URL: $imageUrl")
//                        }
//
//                        override fun onError(e: Exception?) {
//                            val endTime = System.currentTimeMillis()
//                            val timeTaken = endTime - startTime
//                            Log.e("ImageLoadTime", "Detail image failed in $timeTaken ms, URL: $imageUrl, Error: ${e?.message}")
//                        }
//                    })
//            }

            // 🔹 Set other views
            if (title != null) binding.newsTitleInDetail.text = title
            if (!source.isNullOrEmpty()) binding.newsSourceDetail.text = source
            if (!descriptionModified.isNullOrEmpty()) binding.newsContentDetail.text = descriptionModified
            if (!subTitle.isNullOrEmpty()) binding.newsTitleInDetail.text = subTitle

        } catch (e: Exception) {
            Log.e("NewsDetailActivity", "Error in onCreate: ${e.message}", e)
        }

        binding.toolbarNewsDetail.setNavigationOnClickListener { finish() }

        binding.readFullArticleBtn.setOnClickListener {
            Log.d("newsLink", "Opening CompleteNewsActivity with: $link")
            startActivity(Intent(this, CompleteNewsActivity::class.java).putExtra("newsLink", link))
        }

        binding.detailBookmark.setOnClickListener { handleBookmark() }
        binding.detailShare.setOnClickListener { handleShare() }

        checkBookmarkStatus()
    }

    /** 🔹 Trim title for toolbar */
    private fun getTrimmedTitle(fullTitle: String?, wordLimit: Int = 6): String {
        if (fullTitle.isNullOrEmpty()) return ""
        val cleanTitle = fullTitle.replace("'", "").replace(",", "")
        val words = cleanTitle.split(" ")
        return if (words.size <= wordLimit) fullTitle
        else words.take(wordLimit).joinToString(" ") + ""
    }

    private fun checkBookmarkStatus() {
        link?.let { url ->
            databaseViewModel.isNewsBookmarked(url) { bookmarked ->
                isBookmarked = bookmarked
                updateBookmarkIcon()
            }
        }
    }

//    private fun handleBookmark() {
//        val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
//
//        link?.let { url ->
//            val bookmark = Bookmark(title, link, imageUrl, source, currentDate, true)
//            if (isBookmarked) {
//                databaseViewModel.deleteBookmarkByUrl(url)
//                Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show()
//            } else {
//                databaseViewModel.addBookmark(bookmark)
//                Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show()
//            }
//            isBookmarked = !isBookmarked
//            updateBookmarkIcon()
//        }
//    }


    private fun handleBookmark() {
        val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())

        link?.let { url ->
            val bookmark = Bookmark(
                postTitle = title,
                postUrl = url,
                postImage = imageUrl,
                postSource = source,
                pubDate = currentDate,
                isBookmarked = true
            )

            if (isBookmarked) {
                databaseViewModel.deleteBookmarkByUrl(url)
                Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show()
            } else {
                databaseViewModel.addBookmark(bookmark)
                Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show()
            }

            isBookmarked = !isBookmarked
            updateBookmarkIcon()
        }
    }


    private fun updateBookmarkIcon() {
        if (isBookmarked) {
            binding.bookmarkIcon.setImageResource(R.drawable.menu_ic_remove_bookmark)
        } else {
            binding.bookmarkIcon.setImageResource(R.drawable.menu_ic_add_bookmark)
        }
    }

    private fun handleShare() {
        val shareText = "${binding.newsTitleInDetail.text}\nRead more: $link"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}
