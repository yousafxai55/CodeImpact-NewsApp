package com.mobileandroid.appsnews.models

import org.simpleframework.xml.Attribute

data class Enclosure(
    @field:Attribute(name = "url")
    val url: String? = null
)