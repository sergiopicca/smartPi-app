package com.example.firebaseauthmvvm.models

class ChatMessage(val id: String, val text: String, val fromId: String,
                  val timestamp: String) {
    constructor(): this("", "", "", "")
    override fun toString(): String {
        return "ChatMessage(id:'$id', text:'$text', fromId:'$fromId', ts:'$timestamp')"
    }
}