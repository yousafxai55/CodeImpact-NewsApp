package com.mobileandroid.appsnews.bookmarks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_bookmarks",
    indices = [Index(value = ["URL"], unique = true)]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Int = 0,

    @ColumnInfo(name = "ITEM1")
    val postTitle: String? = null,

    @ColumnInfo(name = "URL")
    val postUrl: String,

    @ColumnInfo(name = "IMAGE_URL")
    val postImage: String? = null,

    @ColumnInfo(name = "SOURCE")
    var postSource: String? = null,

    @ColumnInfo(name = "PUB_DATE")
    var pubDate: String? = null,

    @ColumnInfo(name = "isBookmarked")
    val isBookmarked: Boolean = false
)
