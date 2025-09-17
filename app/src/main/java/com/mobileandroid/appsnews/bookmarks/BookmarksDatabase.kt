package com.mobileandroid.appsnews.bookmarks

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Bookmark::class],
    version = 2,
    exportSchema = false
)
abstract class BookmarksDatabase : RoomDatabase() {

    //  Fully-qualified return type (no ambiguity)
    abstract fun getBookmarksDao(): com.mobileandroid.appsnews.bookmarks.BookmarkDao

    companion object {
        @Volatile
        private var instance: BookmarksDatabase? = null

        fun getInstance(context: Context): BookmarksDatabase {
            return instance ?: synchronized(this) {
                val tempInstance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarksDatabase::class.java,
                    "mylist.db"
                )
                    .addMigrations(BOOKMARKS_MIGRATION_1_2)
                    // ❌ remove allowMainThreadQueries() – ANR/crash risk
                    //.fallbackToDestructiveMigration() // (optional) dev build me on kar sakte ho
                    .build()
                instance = tempInstance
                tempInstance
            }
        }

        private val BOOKMARKS_MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                try {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS converted_mylist_data (
                            ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            ITEM1 TEXT,
                            URL TEXT
                        )
                        """.trimIndent()
                    )
                    database.execSQL("INSERT INTO converted_mylist_data SELECT * FROM mylist_data")
                    database.execSQL("ALTER TABLE mylist_data RENAME TO oldmylist_data")
                    database.execSQL("ALTER TABLE main.converted_mylist_data RENAME TO my_bookmarks")
                    database.execSQL("CREATE UNIQUE INDEX index_my_bookmarks_URL ON my_bookmarks(URL)")
                    database.execSQL("ALTER TABLE my_bookmarks ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("DROP TABLE IF EXISTS oldmylist_data")
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
            }
        }
    }
}
