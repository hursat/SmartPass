var bleno = require('bleno');
var util = require('util');
var fbComm= require('./firebase_communication.js');

var BlenoCharacteristic = bleno.Characteristic;
var GetUIDCharacterisctic = function () {

	GetUIDCharacterisctic.super_.call(this, {
		uuid: 'e153486e-fce4-41d4-8b74-21323b3cf222',
		properties: ['write'],
		value: null
	});
	
	this._value = new Buffer(0);
};

util.inherits(GetUIDCharacterisctic, BlenoCharacteristic);

GetUIDCharacterisctic.prototype.onWriteRequest = function(data, offset, withoutResponse, callback){

	this._value = data;
	
	console.log('GetUIDCharacteristic::onWriteRequest Received Value: ' + this._value);
	
	fbComm.checkUserForPermission(this._value, '10001');
	
	callback(this.RESULT_SUCCESS, this._value);
};

module.exports = GetUIDCharacterisctic
