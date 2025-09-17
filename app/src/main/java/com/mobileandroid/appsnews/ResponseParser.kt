//package com.mobileandroid.appsnews
//
//import android.util.Log
//import com.mobileandroid.appsnews.repository.NewsRepository
//import org.jsoup.Jsoup
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//import org.jsoup.parser.Parser
//
//class ResponseParser(var repository: NewsRepository) {
//
//    var sourceLink: String? = null
//    var rssResponse: String? = null
//    var item: Int = 0
//
//    fun parse(response: String): ArrayList<Map<String, String>>? {
//        rssResponse = response
//
//        return try {
//            val document: Document = Jsoup.parse(response, Parser.xmlParser())
//            val channel = document.select("channel").first()
//
//            val link = document.select("channel > link").first()?.text() ?: ""
//
//            val channelInfo = parseRssProperties(channel)
//            Log.d("AFN CHANNEL LINK", channelInfo.first["link"].orEmpty())
//
//            sourceLink = channelInfo.first["link"].orEmpty()
//
//            val items: ArrayList<Map<String, String>> = parseRssItems(channel?.select("item"), link)
//            items
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun parseRssProperties(element: Element?): Pair<Map<String, String>, String?> {
//        val channelInfo = element?.children()?.associate { it.tagName() to it.text() } ?: emptyMap()
//        val link = element?.select("link")?.first()?.text() ?: ""
//        return Pair(channelInfo, link)
//    }
//
//    private fun parseRssItems(items: List<Element>?, link: String?): ArrayList<Map<String, String>> {
//        val itemList = arrayListOf<Map<String, String>>()
//        val sourceLink = getSourceLink(link)
//
//        // Agar sourceLink "UNKNOWN SOURCE" hai, to skip karo
//        if (sourceLink == "UNKNOWN SOURCE") {
//            Log.d("ResponseParser", "Skipping items from UNKNOWN SOURCE for link: $link")
//            return itemList // Empty list return karo
//        }
//
//        items?.forEach { itemElement ->
//            val itemMap = mutableMapOf<String, String>()
//
//            itemElement.children().forEach { childElement ->
//                val tagName = childElement.tagName()
//                var textContent = childElement.text()
//                val cdataContent = childElement.ownText()
//
//                if (textContent.contains("<![CDATA[")) {
//                    textContent = textContent.replace("<![CDATA[", "")
//                    textContent = textContent.replace("]]>", "")
//                }
//
//                when (tagName) {
//                    "title", "link", "pubDate" -> {
//                        val value =
//                            if (childElement.hasText()) childElement.text() else childElement.wholeText()
//                        itemMap[tagName] = value
//                        itemMap["sourceLink"] = sourceLink
//                    }
//
//                    "enclosure" -> {
//                        val imageUrl = childElement.attr("url")
//                        if (imageUrl.isNotEmpty()) {
//                            itemMap["enclosure"] = imageUrl
//                            itemMap["imageLink"] = imageUrl
//                        }
//                    }
//
//                    "media:content" -> {
//                        val imageUrl = childElement.attr("url")
//                        if (imageUrl.isNotEmpty()) {
//                            itemMap["enclosure"] = imageUrl
//                            itemMap["imageLink"] = imageUrl
//                        }
//                    }
//
//                    "description" -> {
//                        val doc = Jsoup.parseBodyFragment(cdataContent)
//                        val img = doc.select("img")
//                        if (img.isNotEmpty()) {
//                            val src = img.attr("src")
//                            if (src.isNotEmpty()) {
//                                itemMap["imageLink"] = src
//                            }
//                            itemMap["description"] = doc.text()
//                        } else {
//                            itemMap["description"] = childElement.text()
//                        }
//                    }
//                }
//            }
//            itemList.add(itemMap)
//        }
//       // repository.mutableLiveData.postValue(itemList)
//        return itemList
//    }
//
//    private fun getSourceLink(link: String?): String {
//        if (link.isNullOrEmpty()) return "UNKNOWN SOURCE"
//
//        return when {
//            link.contains("https://www.suchtv.pk") -> "SUCHTV.PK"
//            link.contains("https://dailypakistan.com.pk/") -> "DAILYPAKISTAN.COM.PK"
//            link.contains("https://www.news.com.au") -> "NEWS.COM.AU"
//            link.contains("https://www.independent.co.uk/") -> "INDEPENDENT.CO.UK"
//            link.contains("https://www.smh.com.au/") -> "SMH.COM.AU"
//            link.contains("https://thewest.com.au") -> "WEST.COM.AU"
//            link.contains("https://www.perthnow.com.au/") -> "PERTHNOW.COM.AU"
//            link.contains("https://www.theage.com.au/") -> "THEAGE.COM.AU"
//            link.contains("https://www.canberratimes.com.au/") -> "CANBERRATIMES.COM.AU"
//            link.contains("https://www.cbc.ca/") -> "CBC.CA"
//            link.contains("https://feeds.thelocal.com/") -> "THELOCAL.COM"
//            link.contains("https://www.france24.com/") -> "FRANCE24.COM"
//            link.contains("https://www.hongkongfp.com/") -> "HONGKONGFP.COM"
//            link.contains("https://www.republika.co.id/") -> "REPUBLIKA.CO.ID"
//            link.contains("https://feeds.breakingnews.ie/") -> "BREAKINGNEWS.IE"
//            link.contains("https://www.news18.com/") -> "NEWS18.COM"
//            link.contains("https://www.indiatoday.in/") -> "INDIATODAY.IN"
//            link.contains("https://www.newsweek.pl/") -> "NEWSWEEK.PL"
//            link.contains("https://www.rt.com/") -> "RT.COM"
//            link.contains("https://rss.unian.net/") -> "UNIAN.NET"
//            link.contains("https://rss.nytimes.com/") -> "NEWYORKTIMES.COM"
//            link.contains("https://www.sowetanlive.co.za/") -> "SOWETANLIVE.CO.ZA"
//            link.contains("https://www.space.com/") -> "SPACE.COM"
//            else -> "UNKNOWN SOURCE"
//        }
//    }
//}
//


package com.mobileandroid.appsnews

import android.util.Log
import com.mobileandroid.appsnews.repository.NewsRepository
import com.mobileandroid.appsnews.utils.SourceResolver
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

class ResponseParser(var repository: NewsRepository) {

    var sourceLink: String? = null
    var rssResponse: String? = null

    fun parse(response: String): ArrayList<Map<String, String>>? {
        rssResponse = response
        return try {
            val document: Document = Jsoup.parse(response, Parser.xmlParser())
            val channel = document.selectFirst("channel")

            // Channel link (may be RSS host)
            val channelLink = channel?.selectFirst("link")?.text().orEmpty()

            // (optional) just for logging
            Log.d("AFN CHANNEL LINK", channelLink)

            // Items
            val items = parseRssItems(channel?.select("item"), channelLink)
            items
        } catch (e: Exception) {
            Log.e("ResponseParser", "parse error: ${e.message}", e)
            null
        }
    }

    private fun parseRssItems(items: List<Element>?, channelLink: String?): ArrayList<Map<String, String>> {
        val list = arrayListOf<Map<String, String>>()

        items?.forEach { itemEl ->
            val map = mutableMapOf<String, String>()

            // --- title ---
            val title = itemEl.selectFirst("title")?.text().orEmpty()
            if (title.isNotBlank()) map["title"] = title

            // --- link (try multiple fallbacks) ---
            val linkTag = itemEl.selectFirst("link")?.text().orEmpty()
            val guidTag = itemEl.selectFirst("guid")?.text().orEmpty()
            val link = when {
                linkTag.isNotBlank() -> linkTag
                guidTag.startsWith("http", true) -> guidTag
                else -> ""
            }
            if (link.isBlank()) {
                Log.d("ResponseParser", "Skipping invalid/blank link")
                return@forEach
            }
            map["link"] = link

            // --- pubDate ---
            val pubDate = itemEl.selectFirst("pubDate")?.text().orEmpty()
            if (pubDate.isNotBlank()) map["pubDate"] = pubDate

            // --- image: enclosure / media:content / media:thumbnail ---
            val enclosureUrl = itemEl.selectFirst("enclosure")?.attr("url").orEmpty()
            val mediaContentUrl = itemEl.selectFirst("media|content")?.attr("url").orEmpty()
            val mediaThumbUrl = itemEl.selectFirst("media|thumbnail")?.attr("url").orEmpty()
            val imageUrl = listOf(enclosureUrl, mediaContentUrl, mediaThumbUrl).firstOrNull { it.isNotBlank() }.orEmpty()
            if (imageUrl.isNotBlank()) {
                map["enclosure"] = imageUrl
                map["imageLink"] = imageUrl
            }

            // --- description (strip images from html if present) ---
            val descEl = itemEl.selectFirst("description")
            if (descEl != null) {
                val raw = descEl.wholeText() // CDATA safe
                val doc = Jsoup.parseBodyFragment(raw)
                // if <img> exists, prefer text
                val img = doc.selectFirst("img")
                if (img != null) {
                    // keep already-set image if any, otherwise take this
                    if (imageUrl.isBlank()) {
                        val src = img.attr("src")
                        if (src.isNotBlank()) {
                            map["imageLink"] = src
                            map["enclosure"] = src
                        }
                    }
                    map["description"] = doc.text()
                } else {
                    map["description"] = doc.text().ifBlank { descEl.text() }
                }
            }

            // --- source from item link; if unknown, fallback to channel link ---
            val itemSource = SourceResolver.fromUrl(link)
            val source = if (itemSource != "UNKNOWN") itemSource else SourceResolver.fromUrl(channelLink)
            map["sourceLink"] = source
            map["source"] = source // (optional) new key for future

            list.add(map)
        }

        return list
    }
}
