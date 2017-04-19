var firebase = require('firebase');
var database = firebase.database();
//fetch reference to sensor values
var actuator_ref = database.ref('actuators');
var android_actuator_types={
	'speaker':222,
	'screen':111,
	'flash':333,
	'vibrate':444,

};

//var sensors = database.ref("sensors");
var available_actuator_types = {};
var actuator_data;
//var sensors = database.ref("sensors");
actuator_ref.on('child_added',function(childSnapshot, prevChildKey){
	if (!available_actuator_types.hasOwnProperty(childSnapshot.key))
	{
		available_actuator_types[childSnapshot.key] = true;
		console.log("actuator added to available list %s",childSnapshot.key);
	}

});
actuator_ref.on('child_removed',function(childSnapshot){
	if (available_actuator_types.hasOwnProperty(childSnapshot.key))
	{
		delete available_actuator_types[childSnapshot];
		//console.log("sensor removed from available list %s",childSnapshot.key);
	}

});
var express = require('express'),  router = express.Router();//,  resources = require('./../resources/model');

router.route('/').get(function (req, res, next) {
  //req.result = resources.pi.sensors; //#A
  //next(); //#B
  //res.send('sensor')
  actuator_ref.once("value").then(function(snapshot){
  	actuator_data = snapshot;
  	snapshot.forEach(function(childSnapshot) {
      // key will be "ada" the first time and "alan" the second time
      var key = childSnapshot.key;
      // childData will be the actual contents of the child
      //var childData = childSnapshot.val();
      available_actuator_types[key] = true;
  });
  	res.send(actuator_data);
  });
  
});
router.route

module.exports = router;
