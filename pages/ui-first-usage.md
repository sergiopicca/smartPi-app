---
layout: default
title: First usage
nav_order: 3
parent: User interface
nav_order: 2
---
#  First usage
In the [Login and Registration](https://sergiopicca.github.io/smartPi-app/pages/ui-auth.html) page we give an introduction about all the possible choices to authenticate a user.
In this section we are going to explain more in detail what happens when a new user is authenticated for the first time and he would like to join or create a house where he lives.

(IMAGE ABOUT FIRST USAGE)

So as we can see we have two buttons where the user can tap according to his necessity: the first one is "Add new house", where he is going to create a new house and the second one is "Add an existing house". If we choose the first option, we will see the following activity

(IMAGE ABOUT NEW HOUSE)

First of all, we have to clarify that all these functionality are offered by Google through [Google Maps API](https://developers.google.com/maps/documentation) and we used a little tutorial as a "guide-line" from [here](https://www.raywenderlich.com/230-introduction-to-google-maps-api-for-android-with-kotlin).

In detail, the search bar on the top is an ```AutocompleteFragment``` which is provided by Google. This widget gives the opportunity to search each address on the world (or if you want a specific country, you can set it easily) and once the user has chosen one of them, a ```Place``` is obtained by the callback and a ```Marker``` is pointed out on the map, which is a ```SupportMapFragment```.

The ```Place``` has some features and in our case, we decided to use these one: ```name```, ```id```, ```latLng.latitude``` and ```address```. The feature captured are used in two functions:

```java
class RoomAdapter(val clickListener: RoomListener): ListAdapter<Room, RoomAdapter.ViewHolder>(RoomNightDiffCallback()) {
    private var listener: ((item:Room) -> Unit)? = null

    fun deleteRoom(listener: (item: Room) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, listener)
    }
```

```java
class RoomAdapter(val clickListener: RoomListener): ListAdapter<Room, RoomAdapter.ViewHolder>(RoomNightDiffCallback()) {
    private var listener: ((item:Room) -> Unit)? = null

    fun deleteRoom(listener: (item: Room) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, listener)
    }
```
