package com.example.firebaseauthmvvm.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// The user in the room database
@Entity(tableName = "users")
data class RoomUser(
    @PrimaryKey(autoGenerate = false)
    var uid: String,

    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "username")
    var username: String,

    @ColumnInfo(name = "owner")
    var owner: String?,

    @ColumnInfo(name = "guest")
    var guest: String?,

    @ColumnInfo(name = "profileImg")
    var profileImageUrl: String?
)