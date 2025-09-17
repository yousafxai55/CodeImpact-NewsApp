package com.mobileandroid.appsnews.models

data class NewsItem(
    val title: String,
    val description: String?,
    val imageLink: String?,
    val link: String,
    val sourceLink: String?,
    val pubDate: String?,
    val subTitle: String?
)
