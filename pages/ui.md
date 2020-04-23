---
layout: default
title: User interface
nav_order: 3
has_children: true
---
#  User interface

![UI](images/overview.png)

Here we will examine the different interfaces of the application. We will go through the views of the application describing what the need for and how they are made, basically we have different activities for the sign-up and the sign-in of the application, but once the user enters in the application, so is authenticated we have one single activity and many fragment. In particular, each view has a fragment, a view model and a view model factory to create the view model, with a common convention for their names, for instance the home of the application has an **HomeFragment**, a **HomeViewModel** and a **HomeViewModelFactory**.

Here we can find a list of the views of the application, each will be described more in detail in its own section.
