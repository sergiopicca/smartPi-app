package com.example.firebaseauthmvvm.models

class Room (
    var roomId: String,
    var name: String) {

    override fun toString(): String {
        return "Room: id:$roomId, name: $name"
    }

    override fun equals(other: Any?)
            = (other is Room)
            && roomId == other.roomId
            && name == other.name
}
