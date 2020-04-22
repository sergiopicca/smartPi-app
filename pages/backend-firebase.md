---
layout: default
title: Firebase
nav_order: 4
parent: Backend
nav_order: 1
---
# Realtime database
![](images/firebase.png)
We decided to use Firebase in order to store the relevant data of our application. In particular, we decided to use the Realtime database that is a **NoSQL** database, where the data is stored as JSON, very easy to maintain and to update. In general, we found the Firebase service very flexible, supporting any kind of service we needed and one nice think was the use of the **functions** to perform some action when data changes, such as sending notification to our clients, but let's examine the structure of the data first.

## Organization of the data
