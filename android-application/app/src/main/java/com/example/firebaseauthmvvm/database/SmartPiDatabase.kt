package com.example.firebaseauthmvvm.database

import android.content.Context
import androidx.room.*

// The database instance, it should be singleton, defined once
@Database(entities = [RoomUser::class, RoomDevice::class], version = 4, exportSchema = false)
abstract class SmartPiDatabase: RoomDatabase() {
    abstract val smartPiDatabaseDao: SmartPiDatabaseDao

    // Creating the database
    companion object{
        // Volatile, meaning that it works in main memory and it will not be cached
        @Volatile
        private var INSTANCE: SmartPiDatabase? = null
        fun getInstance(context: Context): SmartPiDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SmartPiDatabase::class.java,
                            "smart_pi_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}