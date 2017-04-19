
var express = require('express'),
  actuatorsRoutes = require('./../routes/actuators_old'),
  sensorRoutes = require('./../routes/sensors_old'),
  thingsRoutes = require('./../routes/things_old'),
  resources = require('./../resources/model'),
  converter = require('./../middleware/converter'),
  cors = require('cors'),
  bodyParser = require('body-parser');

var firebase = require("firebase");

//initializing firebase with the config information
var config = {
	apiKey: "AIzaSyBGsMEfEWNAlTxg_RKMT2hZM5nOj6wCC9g",
	authDomain: "miot-smartdevice.firebaseapp.com",
	databaseURL: "https://miot-smartdevice.firebaseio.com",
	projectId: "miot-smartdevice",
	storageBucket: "miot-smartdevice.appspot.com",
	messagingSenderId: "708415884606"
};
firebase.initializeApp(config);

module.exports = firebase;

//create new routes or url for new sensors and actuators
var smart_actuators = require('./../routes/actuators'),
	smart_sensors = require('./../routes/sensors');

var app = express();

app.use(bodyParser.json());

app.use(cors());

app.use('/pi/actuators', actuatorsRoutes);
app.use('/pi/sensors', sensorRoutes);
app.use('/things', thingsRoutes);

//link /smart/actuators(or)sensors to the new routes
app.use('/smart/actuators',smart_actuators);
app.use('/smart/sensors',smart_sensors);
app.get('/smart',function(req,res){
	res.send('Seperate implementation for my smart device')
});

app.get('/pi', function (req, res) {
  res.send('This is the WoT-Pi!')
});

// For representation design
app.use(converter());
module.exports = app;
