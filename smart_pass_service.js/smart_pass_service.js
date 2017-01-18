var bleno = require('bleno');
var util = require('util');
var GetUIDCharacterisctic = require('./get_uid_characteristic');

function SmartPassService(){
	
	bleno.PrimaryService.call(this, {
		uuid: '0019da7b-0929-4cbe-b93c-322a11cfadec',
		characteristics: [
			new GetUIDCharacterisctic()
		]
	});
};

util.inherits(SmartPassService, bleno.PrimaryService);
module.exports = SmartPassService;
