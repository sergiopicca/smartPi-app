package com.example.firebaseauthmvvm.models


class Message(val id: String, val text: String, val sender: String, val senderImg: String, val sendByMe:Boolean, val ts: String) {

    override fun toString(): String {
        return "Message(id:'$id', text:'$text', sender:'$sender', senderImg:'$senderImg', sendByMe: '$sendByMe', ts:'$ts')"
    }
}