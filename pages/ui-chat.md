---
layout: default
title: Chat
nav_order: 6
parent: User interface
---
#  Yoshi, you have a new message!

One the features of our application was to integrate a little chat allowing the owner of the house and his guests, let's say his **family**, to communicate in order to leave some simple message, maybe also regarding the house, such as "Hi guys, remember that the video surveillance system is active!".

![Chat](../images/chat.jpeg)

The chat is a simple recycler view displaying the messages, but we had to define two different kinds of layout for each item, because of the different senders, basically the user should be able to distinguish his own messages, that in this case are the one in orange. We decided also to add the profile picture of each user near the message box, by using **Picasso** library, in order to recognize in a strait forward way the sender.

## Two different layouts for the items
Let's have a look to the ```MessageAdapter``` holding the messages of the recycler view, in particular to two important variables:
  ```
  class MessageAdapter: RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
     private val VIEW_TYPE_MY_MESSAGE = 1
     private val VIEW_TYPE_OTHER_MESSAGE = 2
  ```

This two variables are used to distinguish the two different types of messages that the adapter should display, according to the sender value, then two different kind of layouts will be inflated:

  override fun getItemViewType(position: Int): Int {
          val msg = data.get(position)
          val isMine = msg.sendByMe
          if(isMine){
              return VIEW_TYPE_MY_MESSAGE
          }
          else{
              return VIEW_TYPE_OTHER_MESSAGE
          }
      }

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
          if(viewType == VIEW_TYPE_MY_MESSAGE) {
              val layoutInflater = LayoutInflater.from(parent.context)
              val view = layoutInflater
                  .inflate(R.layout.chat_to_row, parent, false)
              return ViewHolder(view)
          } else {
              val layoutInflater = LayoutInflater.from(parent.context)
              val view = layoutInflater
                  .inflate(R.layout.chat_from_row, parent, false)
              return ViewHolder(view)
          }
      }
