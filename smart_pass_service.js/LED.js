var wpi = require('wiring-pi');

	var greenLEDPin = 7;
	var redLEDPin = 15;

	wpi.setup('wpi');
	wpi.pinMode(greenLEDPin, wpi.OUTPUT);

function openGreenLED(){

	wpi.digitalWrite(greenLEDPin, 1);
	
};

function closeGreenLED(){

	wpi.digitalWrite(greenLEDPin, 0);
	
};

function greenLedControl(){
		
		openGreenLED();
	
		setTimeout(closeGreenLED, 2000);

};

function openRedLED(){

	wpi.digitalWrite(redLEDPin, 1);
	
};

function closeRedLED(){

	wpi.digitalWrite(redLEDPin, 0);
	
};

function redLedControl(){
		
		openRedLED();
	
		setTimeout(closeRedLED, 2000);

};

module.exports.greenLedControl = greenLedControl;
module.exports.redLedControl = redLedControl;
