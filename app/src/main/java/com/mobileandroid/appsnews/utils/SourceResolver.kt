package com.mobileandroid.appsnews.utils

import java.net.URI
import java.util.Locale

object SourceResolver {
    fun fromUrl(url: String?): String {
        if (url.isNullOrBlank()) return "UNKNOWN"
        return try {
            val rawHost = URI(url).host ?: return "UNKNOWN"
            val host = rawHost.lowercase(Locale.ROOT).removePrefix("www.")
            when (host) {
                "rss.nytimes.com", "nytimes.com" -> "NEWYORKTIMES.COM"
                "cbc.ca" -> "CBC.CA"
                "feeds.thelocal.com", "thelocal.com" -> "THELOCAL.COM"
                "hongekongfp.com", "hongkongfp.com" -> "HONGKONGFP.COM"
                "france24.com" -> "FRANCE24.COM"
                "republika.co.id" -> "REPUBLIKA.CO.ID"
                "feeds.breakingnews.ie", "breakingnews.ie" -> "BREAKINGNEWS.IE"
                "news18.com" -> "NEWS18.COM"
                "indiatoday.in" -> "INDIATODAY.IN"
                "newsweek.pl" -> "NEWSWEEK.PL"
                "rt.com" -> "RT.COM"
                "unian.net", "rss.unian.net" -> "UNIAN.NET"
                "space.com" -> "SPACE.COM"
                "sowetanlive.co.za" -> "SOWETANLIVE.CO.ZA"
                "theage.com.au" -> "THEAGE.COM.AU"
                "canberratimes.com.au" -> "CANBERRATIMES.COM.AU"
                "perthnow.com.au" -> "PERTHNOW.COM.AU"
                "thewest.com.au" -> "WEST.COM.AU"
                "smh.com.au" -> "SMH.COM.AU"
                "news.com.au" -> "NEWS.COM.AU"
                else -> host.uppercase(Locale.ROOT) // fallback: host ko hi label bana do
            }
        } catch (_: Exception) {
            "UNKNOWN"
        }
    }
}