package com.mobileandroid.appsnews.models

import org.simpleframework.xml.Root

@Root(name = "rss", strict=false)
data class RSSFeed(
    val channel: Map<String, String>,
    val items: ArrayList<Map<String, String>>?
)