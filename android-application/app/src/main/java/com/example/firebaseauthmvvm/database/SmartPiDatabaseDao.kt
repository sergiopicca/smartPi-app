package com.example.firebaseauthmvvm.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// Data Access Object, we can manipulate our data
// The Dao manages the CRUD operations in the Room db
@Dao
interface SmartPiDatabaseDao {
    @Insert
    fun insertUser(user:RoomUser)

    @Update
    fun updateUser(user: RoomUser)

    @Query("SELECT * from users")
    fun getUser():RoomUser?

    @Query("SELECT owner from users")
    fun getUserOwnerHouses():String?

    @Query("SELECT guest from users")
    fun getUserGuestHouses():String?

    @Query("DELETE FROM users WHERE uid = :key")
    fun clear(key: String)

    @Insert
    fun insertDevice(device: RoomDevice)

    @Update
    fun updateDevice(device: RoomDevice)

    @Query("SELECT * FROM device")
    fun getDevices():RoomDevice?

    @Query("SELECT * FROM device WHERE name = :name")
    fun getDevice(name: String):RoomDevice?

    @Query("SELECT token from device WHERE name = :name")
    fun getToken(name: String): String?
}