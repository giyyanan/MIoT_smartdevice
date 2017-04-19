var firebase = require('firebase');
var database = firebase.database();
//fetch reference to sensor values
var actuator_ref = database.ref('actuators');
var android_actuator_types={
	'splash':111,
	'speak':222,
	'flash':333,
	'shake':444,

};

//var sensors = database.ref("sensors");
var available_actuator_types = {};
var actuator_data;
//var sensors = database.ref("sensors");
actuator_ref.on('child_added',function(childSnapshot, prevChildKey){
	if (!available_actuator_types.hasOwnProperty(childSnapshot.key))
	{
		console.log("actuator added to available list %s",childSnapshot.key);
		
	}

});
actuator_ref.on('child_removed',function(childSnapstruehot){
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
      available_actuator_types[key] = childSnapshot.child("available").val();
  });
  	res.send(actuator_data);
  	console.log(available_actuator_types)
  });
  next();
});



router.route('/:actuator').get(function (req, res, next) {
	var actuator = req.params.actuator;

	var number_patt = /^\d+$/;

	actuator_ref.once("value").then(function(snapshot){
		actuator_data = snapshot;
		snapshot.forEach(function(childSnapshot) {
      // key will be "ada" the first time and "alan" the second time
      var key = childSnapshot.key;
      // childData will be the actual contents of the child
      //var childData = childSnapshot.val();
      available_actuator_types[key] = childSnapshot.child("available").val();
  });
		console.log(available_actuator_types);

		if(android_actuator_types.hasOwnProperty(actuator) ){
			var actuator_type = android_actuator_types[actuator];

			console.log(actuator_type);
			

			if(available_actuator_types.hasOwnProperty(actuator_type)){
				console.log(req)
				database.ref("actuators"+"/"+actuator_type).once("value")
				.then(function(snapshot) {
					var data = snapshot.val();
					res.send(data);
				});

				console.log("actuator found and available");
			}
			else{
				res.send("actuator not available");
			}
		}
		else if (available_actuator_types.hasOwnProperty(actuator) && number_patt.test(actuator)){
			
			actuator_type = actuator;
			database.ref("actuators"+"/"+actuator_type).once("value")
			.then(function(snapshot) {
					var data = snapshot.val();
					res.send(data);
				});

		}
		else{
			res.send("actuator not found")
		}
	});

next();
	
});

router.route('/:actuator/:value').get(function (req, res, next) {
	var actuator = req.params.actuator;
	var actuator_value = req.params.value;
	var number_patt = /^\d+$/;

	actuator_ref.once("value").then(function(snapshot){
		actuator_data = snapshot;
		snapshot.forEach(function(childSnapshot) {
      // key will be "ada" the first time and "alan" the second time
      var key = childSnapshot.key;
      // childData will be the actual contents of the child
      //var childData = childSnapshot.val();
      available_actuator_types[key] = childSnapshot.child("available").val();
  });
		console.log(available_actuator_types);

		if(android_actuator_types.hasOwnProperty(actuator) ){
			var actuator_type = android_actuator_types[actuator];

			console.log(actuator_type);
			

			if(available_actuator_types.hasOwnProperty(actuator_type)){
				console.log(req)
				database.ref("actuators"+"/"+actuator_type)
				.set({"val":req.params.value,"available":true})
				.then(function() {
					database.ref("actuators"+"/"+actuator_type).once("value")
					.then(function(snapshot) {
					var data = snapshot;
					res.send(data);
				});
				});
				

				console.log("actuator found and available");
			}
			else{
				res.send("actuator not available");
			}
		}
		else if (available_actuator_types.hasOwnProperty(actuator) && number_patt.test(actuator)){
			
			actuator_type = actuator;
			database.ref("actuators"+"/"+actuator_type)
				.set({"val":req.params.value,"available":true})
				.then(function() {
					return database.ref("actuators"+"/"+actuator_type).once("value");
				})
				.then(function(snapshot) {
					var data = snapshot;
					res.send(data);
				});

		}
		else{
			res.send("actuator not found")
		}
	});

next();
	
});

module.exports = router;
