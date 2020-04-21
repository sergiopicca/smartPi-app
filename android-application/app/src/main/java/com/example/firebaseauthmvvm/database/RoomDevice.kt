package com.example.firebaseauthmvvm.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "device")
data class RoomDevice (
    @PrimaryKey(autoGenerate = false)
    var name: String,

    @ColumnInfo(name = "url")
    var url: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "token")
    var token: String

)