
package com.mobileandroid.appsnews.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobileandroid.appsnews.utils.CATEGORIES_TABLE_NAME

@Entity(tableName = CATEGORIES_TABLE_NAME)
data class TopicsCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val imageUrl:String
)


