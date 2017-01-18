var bleno = require('bleno');
var SmartPassService = require('./smart_pass_service');
var spService = new SmartPassService();

bleno.on('stateChange', function(state) {
	
	console.log('State: '+ state);
	
	if(state == 'poweredOn'){
		bleno.startAdvertising('deviceAlpha', [spService.uuid]);
		
	} else {
		bleno.stopAdvertising();
	
	}
	
});

bleno.on('advertisingStart', function(error){

	console.log('Advertising Start: ' + (error ? 'error: ' + error : 'success'));
	
	if(!error){
		bleno.setServices([spService]);
	
	}
});
