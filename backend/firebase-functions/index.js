// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//


var message;
var houseId;

exports.sendNotification = functions.database.ref('/house-messages/{houseId}/{messageId}').onCreate((snapshot, context) => {
	
	// Grab the current value of what was written to the Realtime Database.
	message = snapshot._data;
	houseId = context.params.houseId;
	var userId = message.fromId;
	
	// Get user name from database
	var databaseRef = admin.database().ref(`/users/${userId}`);
	databaseRef.once('value').then(function(snapshot) {
		var username = snapshot.val().username;
		// Create payload for notification
		const payload = {
		      notification: {
		        title: `${username}`,
		        body: `${message.text}`,
				tag: `${message.fromId}`,
		      }
		    };
		// console.log("Payload: " + payload);
		// Send notification to topic
		admin.messaging().sendToTopic(houseId, payload);
	});
	
	

});



