
var httpServer = require('./servers/http'),
wsServer = require('./servers/websockets'),
resources = require('./resources/model');


// Internal Plugins
var ledsPlugin = require('./plugins/internal/ledsPlugin'), //#A
  pirPlugin = require('./plugins/internal/pirPlugin'), //#A
  dhtPlugin = require('./plugins/internal/DHT22SensorPlugin'); //#A

// External Plugins for sensors/actuators connected to the PI GPIOs
// If you test this with real sensors do not forget to set simulate to 'true'
pirPlugin.start({'simulate': true, 'frequency': 2000}); //#B
ledsPlugin.start({'simulate': true, 'frequency': 10000}); //#B
dhtPlugin.start({'simulate': true, 'frequency': 10000}); //#B

// External Plugins
var coapPlugin = require('./plugins/external/coapPlugin');
coapPlugin.start({'simulate': true, 'frequency': 10000});

// HTTP Server
var server = httpServer.listen(resources.pi.port, function () {
	console.log('HTTP server started...');

  // Websockets server
  wsServer.listen(server);

  console.info('Your WoT Pi is up and running on port %s', resources.pi.port);
});
