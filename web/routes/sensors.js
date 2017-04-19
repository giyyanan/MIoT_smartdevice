//fetch reference to the default database in the app
var firebase = require('firebase');
var database = firebase.database();
//fetch reference to sensor values
var sensor_ref = database.ref('sensors');
var android_sensor_types = {
	'accelerometer' : 1,	'magnetometer':2,	'orientation':3,	'gyroscope':4,	' light_sensor':5,
		'pressure':6,		'Orientation':7,	'proximity':8,		'gravity':9,	'acceleration':10,
			'rotation':11,	'relative_humidity':12,	'ambient_temperature':13,	'uncalibrated_magnetometer':14,	'game_rotation':15,
				'uncalibrated_gyroscaope':16,	'significant_motion':17,	'step_detection':18,	'step_count':19, 	'geomgnetic_rotation':20,
						'heart_rate':21, 'tilt':22,	'pickup_gesture':25,	'pose_6dof':28,'stationary_detect':29,
						'device_orientation':27,'motion_detect':30,
						'heart_beat':31	,'off_body_detect':34,
					'temperature':65536,	'sensors_sync':65537,	'double_twist':65538,	'double_tap':65539,
};
var available_sensor_types = {};
var sensor_data;
//var sensors = database.ref("sensors");
sensor_ref.on('child_added',function(childSnapshot, prevChildKey){
	if (!available_sensor_types.hasOwnProperty(childSnapshot.key))
	{
		available_sensor_types[childSnapshot.key] = true;
		console.log("sensor added to available list %s",childSnapshot.key);
	}

});
sensor_ref.on('child_removed',function(childSnapshot){
	if (available_sensor_types.hasOwnProperty(childSnapshot.key))
	{
		delete available_sensor_types[childSnapshot];
		//console.log("sensor removed from available list %s",childSnapshot.key);
	}

});

var express = require('express'),  router = express.Router();//,  resources = require('./../resources/model');

router.route('/').get(function (req, res, next) {
  //req.result = resources.pi.sensors; //#A
  //next(); //#B
  //res.send('sensor')
  
  sensor_ref.once("value").then(function(snapshot){
  	sensor_data = snapshot;
  	snapshot.forEach(function(childSnapshot) {
      // key will be "ada" the first time and "alan" the second time
      var key = childSnapshot.key;
      // childData will be the actual contents of the child
      //var childData = childSnapshot.val();
      available_sensor_types[key] = true;
  });
  	console.log(available_sensor_types);
  	res.send(sensor_data);

  });
  next();
  
});

router.route('/:id').get(function (req, res, next) {
  var id = req.params.id;
  var number_patt = /^\d+$/;
  if(number_patt.test(id) && available_sensor_types.hasOwnProperty(id))  {
  	console.log('number parameter available')
  	sensor_ref.child(id).once("value").then(function(snapshot){
  	res.send(snapshot);
  });

  }
  else if (android_sensor_types.hasOwnProperty(id) && available_sensor_types.hasOwnProperty(android_sensor_types[id])) {
  	console.log('string parameter available')
  	sensor_ref.child(android_sensor_types[id]).once('value').then(function(childSnapshot, prevChildKey) {
  	//res.write(childSnapshot.key+new Date());
  	//setTimeout(function(){ res.redirect(req.originalUrl) }(req,res), 2000);
  	res.send(childSnapshot)
  	console.log('value changed');
  });
  }
  else{
  	res.send("Goolge hasn't still figured out this sensor");
  }
  
  next();
});

module.exports = router;
