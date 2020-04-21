package com.example.firebaseauthmvvm.models

class Device(
    val id:String,
    val deviceName: String,
    val type: String,
    val url: String){

    override fun equals(other: Any?)
            = (other is Device)
            && id == other.id
            && deviceName == other.deviceName
            && type == type

    override fun toString(): String {
        return "Device: id:$id, name:$deviceName, type:$type"
    }
}