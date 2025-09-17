package com.mobileandroid.appsnews.savedsearches

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "savedsearches", indices = [Index(value = ["topic"], unique = true)])
class SavedSearches(@field:ColumnInfo(name = "topic") val searchTopic: String) {
    @PrimaryKey
    @ColumnInfo(name = "ID")
    var id: Int? = null
}