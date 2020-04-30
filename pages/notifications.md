---
layout: default
title: Notifications
nav_order: 5
---
# Notifications

We decided to give a separate section to notifications, since they are an important part of our application and they are used not only to tell the user that a new message from the family chat has arrived, but most important to **inform the user that some stranger is breaking inside the house**. Since this last functionality is very important, we though that was important for a user to receive notifications either when the application is visible and also when it is closed or is running in background. Therefore, we decided to implement a service and use **Firebase Cloud Messaging (FCM)**.

FCM topic messaging is based on the publish/subscribe model. If we ask for messages to the server after a determined amount of time in a periodic way, we waste our resources, starting from the phone battery, instead a publish/subscribe model is more useful allowing a client device to subscribe to one particular topic, in this case the house of which the user is owner or guest, and be notified when there are new messages delivered through the app. Topics are also important because allow you to send a message to multiple devices that have opted in to that particular topic, in this case suppose that the owner of the house is working and put his phone in "Do not disturb" mode, then **another member of the family**, guest of that house, **can be notified because he or she is subscribed to the same topic** (house). For clients, topics are specific data sources which the client is interested in. For the server, topics are groups of devices which have opted in to receive updates on a specific data source. Topics can be used to present categories of notifications, such as news, weather forecasts, and sports results. More information in the official [documentation](https://firebase.google.com/docs/cloud-messaging/android/topic-messaging).


## Serverless architecture

![Functions](../images/functions.jpg)
As we discussed
