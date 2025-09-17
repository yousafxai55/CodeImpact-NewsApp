package com.mobileandroid.appsnews.ui.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.databinding.SampleNewsItemBinding
import com.mobileandroid.appsnews.ui.Activity.NewsDetailActivity
import java.text.SimpleDateFormat
import java.util.Locale


class PostAdapter(
    private val context: Context,
    private var listItems: MutableList<Map<String, String>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CONTENT = 0
        private const val VIEW_TYPE_AD = 1
    }

    override fun getItemViewType(position: Int): Int {
        // Har 3rd item ke baad ad
        return if ((position + 1) % 4 == 0) VIEW_TYPE_AD else VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_AD) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.native_rss_ad, parent, false) // <-- yeh aapka native ad ka XML hai
            AdViewHolder(view)
        } else {
            val binding = SampleNewsItemBinding.inflate(LayoutInflater.from(context), parent, false)
            NewsVH(binding)
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
           // loadNativeAd((holder as AdViewHolder).itemView as com.google.android.gms.ads.nativead.NativeAdView)
        } else {
            val item = listItems[position]
            val newsHolder = holder as NewsVH
            newsHolder.binding.newsTitle.text = item["title"]
            newsHolder.binding.newsSource.text = item["sourceLink"]

            // Format pubDate
            val pubDate = item["pubDate"]
            if (!pubDate.isNullOrEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    val date = inputFormat.parse(pubDate)
                    newsHolder.binding.pubDate.text = outputFormat.format(date)
                } catch (e: Exception) {
                    newsHolder.binding.pubDate.text = pubDate
                }
            }


            // Image load with Glide (instead of Picasso)
//            val imageUrl = item["imageLink"]
//            Glide.with(context)
//                .load(imageUrl)
//                .placeholder(R.drawable.breaking_news_img)
//                .error(R.drawable.breaking_news_img)
//                .into(newsHolder.binding.newsIv)

            val imageUrl = item["imageLink"]
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.breaking_news_img)
                .error(R.drawable.breaking_news_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL) //  Added cache
                .centerCrop()
                .override(600, 300) // Resize before rendering
                .into(newsHolder.binding.newsIv)


            newsHolder.itemView.setOnClickListener {

                val fullDescription = item["description"] ?: ""


                context.startActivity(Intent(context, NewsDetailActivity::class.java).apply {
                    putExtra("title", item["title"])
                    putExtra("imageLink", item["imageLink"])
                    putExtra("description", item["description"])
                    putExtra("link", item["link"])
                    putExtra("subTitle", item["subTitle"])
                })
            }
        }
    }

    //  NEW: Efficient list update using DiffUtil
    fun updateList(newList: List<Map<String, String>>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = listItems.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listItems[oldItemPosition]["link"] == newList[newItemPosition]["link"]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listItems[oldItemPosition] == newList[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listItems.clear()
        listItems.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    // Normal News ViewHolder
    class NewsVH(val binding: SampleNewsItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Ad ViewHolder
    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

//    /** Load Native Ad */
//    private fun loadNativeAd(adView: com.google.android.gms.ads.nativead.NativeAdView) {
//        val builder = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110") // Test adUnitId
//        builder.forNativeAd { nativeAd ->
//            // Set headline
//            adView.findViewById<TextView>(R.id.ad_headline).text = nativeAd.headline
//            adView.headlineView = adView.findViewById(R.id.ad_headline)
//
//            // Body
//            adView.findViewById<TextView>(R.id.ad_body)?.apply {
//                text = nativeAd.body
//                visibility = if (nativeAd.body != null) View.VISIBLE else View.INVISIBLE
//            }
//            adView.bodyView = adView.findViewById(R.id.ad_body)
//
//            // Call to action
//            adView.findViewById< Button>(R.id.ad_call_to_action)?.apply {
//                text = nativeAd.callToAction
//                visibility = if (nativeAd.callToAction != null) View.VISIBLE else View.INVISIBLE
//            }
//            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
//
//            // Icon
//            adView.findViewById<ImageView>(R.id.ad_app_icon)?.apply {
//                setImageDrawable(nativeAd.icon?.drawable)
//                visibility = if (nativeAd.icon != null) View.VISIBLE else View.GONE
//            }
//            adView.iconView = adView.findViewById(R.id.ad_app_icon)
//
//            // Media
//            adView.mediaView = adView.findViewById(R.id.ad_media)
//
//            // Advertiser
////            adView.findViewById<TextView>(R.id.ad_advertiser)?.apply {
////                text = nativeAd.advertiser
////                visibility = if (nativeAd.advertiser != null) View.VISIBLE else View.INVISIBLE
////            }
////            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
//
//            adView.setNativeAd(nativeAd)
//        }
//
//        val adLoader = builder
//            .withAdListener(object : com.google.android.gms.ads.AdListener() {
//                override fun onAdFailedToLoad(p0: com.google.android.gms.ads.LoadAdError) {
//                    Log.e("AdLoader", "Ad failed: ${p0.message}")
//                }
//            })
//            .build()
//        adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
//    }
}


