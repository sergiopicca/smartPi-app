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
First of all, we have to clarify that all these functionality are offered by Google through [Google Maps API](https://developers.google.com/maps/documentation) and we used a small tutorial as a "guide-line" from [here](https://www.raywenderlich.com/230-introduction-to-google-maps-api-for-android-with-kotlin).
In detail, the search bar is an ```AutocompleteFragment``` which is provided by Google
