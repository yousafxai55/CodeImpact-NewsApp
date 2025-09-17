package com.mobileandroid.appsnews.models

import org.simpleframework.xml.Attribute

data class MediaContent(
    @field:Attribute(name = "url")
    val url: String? = null
)