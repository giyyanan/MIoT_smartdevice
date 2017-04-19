var firebase = require('firebase');
var database = firebase.database();
//fetch reference to sensor values
var actuator_ref = database.ref('actuators');

//var sensors = database.ref("sensors");

var express = require('express'),  router = express.Router();//,  resources = require('./../resources/model');

router.route('/').get(function (req, res, next) {
  //req.result = resources.pi.sensors; //#A
  //next(); //#B
  //res.send('sensor')
  var data;
  actuator_ref.once("value").then(function(snapshot){
  	data = snapshot;
  	res.send(data);
  });
  
});

module.exports = router;
