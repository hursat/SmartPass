var firebase = require('firebase');
var controlLED = require('./LED.js');

var config = {
		apiKey: "AIzaSyD2Hvw2VKbyky5QgLWZC5NMA6mMP1CpZQc",
		authDomain: "smartpass2-e1af8.firebaseapp.com",
		databaseURL: "https://smartpass2-e1af8.firebaseio.com",
};

firebase.initializeApp(config);

function checkUserForPermission(uid, eventID){
	
	var database = firebase.database(); 
	var userTicketRelationRef = database.ref('Tickets/10001/participants');
	var result = false;

	userTicketRelationRef.once('value').then(function(snapshot){
	
		snapshot.forEach(function(childSnapshot){
			
			if(childSnapshot.key == uid){
				
				console.log('GRANT ACCESS');
				controlLED.greenLedControl();
				result = true;
				return;
				
			}
		});
		
		if(result == false){
			console.log('NO ACCESS');
			controlLED.redLedControl();
		}
	});
};

module.exports.checkUserForPermission = checkUserForPermission;
