/*
 *  SparkThings V0.2
 *
 *  
 *  TODO: Page 2 and 3 don't load previous settings when reconfiguring
 *  TODO: Not all device types are setup and tested yet
 *  TODO: support the Particle Photon 
 *  TODO: change all the Spark references to Particle
 *  TODO: handle turning on an off pins
*/
definition(
    name: "SparkThings V0.2",
    namespace: "rhworkshop",
    author: "Andy Rawson",
    description: "Allows a Particle.io device to be used with SmartThings",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "page1", title: "Select sensor types", nextPage: "page2", uninstall: true) {
    
            def opt = [
            	[
					title : "Pin not used",
					order : 0, // the order of the group; 0-based
					image : null, // not yet supported
					values:[[key:"none",value:"Not used"]]
                ],
				[
					title : "Sensor or Actuator (could be either one)",
					order : 3, // the order of the group; 0-based
					image : null,
					values: [[key:"alarm",value:"Alarm"], [key:"doorControl",value:"Door Control"], [key:"lock",value:"Lock"], 
                    	[key:"relaySwitch",value:"Relay Switch"],[key:"switch",value:"Switch"], 
                        [key:"switchLevel",value:"Switch Level"], [key:"valve",value:"Valve"]
					]
				],
				[
					title : "Sensor only (return information)",
					order : 2, // the order of the group; 0-based
					image : null,
					values: [[key:"button",value:"Button"], 
						[key:"carbonMonoxideSensor",value:"Carbon Monoxide Sensor"], [key:"contactSensor",value:"Contact Sensor"], 
                        [key:"motionSensor",value:"Motion Sensor"], [key:"presenceSensor",value:"Presence Sensor"], [key:"sleepSensor",value:"Sleep Sensor"], 
                        [key:"smokeDetector",value:"Smoke Detector"], [key:"waterSensor",value:"Water Sensor"]
					]
				],
				[
					title : "Actuator only (do something)",
					order : 1, // the order of the group; 0-based
					image : null, // not yet supported
					values: [[key:"momentary",value:"Momentary"],[key:"tone",value:"Tone"]
					]
				]
			]
    
        section("Generate Username and Password") {
        	input "sparkUsername", "text", title: "Your Spark.io Username", required: true
            input "sparkPassword", "password", title: "Your Spark.io Password", required: true
        }
        section("Digital Pins (* can send instant status)") {
        	for ( i in 0..7 ) {
            	String isInterrupt = (i in [0,1,2,3,4]) ? " *" : ""
            	input("sensorTypeD${i}", title: "Select sensor type for Pin D${i}${isInterrupt}", "enum", defaultValue: "none", multiple: false, groupedOptions: opt)
            }

        }
        section("Analog Pins (* can send instant status)") {        
        	for ( i in 0..7 ) {
            	String isInterrupt = (i in [0,1,3,4,5,6,7]) ? " *" : ""
            	input("sensorTypeA${i}", title: "Select sensor type for Pin A${i}${isInterrupt}", "enum", defaultValue: "none", multiple: false, groupedOptions: opt)
            }
            
        }
    }
    page(name: "page2", title: "Select devices", nextPage: "page3", install: false, uninstall: false)
    page(name: "page3", title: "Sensor Settings", nextPage: "page4", install: false, uninstall: false)
	page(name: "page4", title: "Spark Device", install: true, uninstall: false)
    
}

def page2() {
    dynamicPage(name: "page2") {
   
   if (!state.sparkToken){
   def clientAuth = "particle:particle"
   clientAuth = "Basic " + clientAuth.encodeAsBase64().toString()
   def params = [
        headers: [Authorization: clientAuth],
		uri: "https://api.particle.io/oauth/token",
		body: [grant_type: "password", 
        	username: sparkUsername,
        	password: sparkPassword] 
        ]
   try {
        httpPost(params) {response -> 
        	state.sparkToken = response.data.access_token
               	}
	} catch (e) {
        log.error "error: $e"
        
    }
        log.debug "Created new Spark.io token"        
        //log.debug "Spark Token ${state.sparkToken}"
        checkToken() 

    }
    
    	def sensorTypesD = settings.findAll { it.key.startsWith("sensorTypeD") }
        sensorTypesD.eachWithIndex { sType, i -> 
			if (sType.value != "none"){
        		section("Pin D${i}"){
            		input(name: "sensorD${i}", type: "capability.${sType.value}", title: "Select the ${sType.value} device", required: true, multiple: false)
                	input(name: "actuatorD${i}", type: "bool", title: "On for Actuator, Off for Sensor", defaultValue: false)
            	}
        	}

		}
        
        def sensorTypesA = settings.findAll { it.key.startsWith("sensorTypeA") }
        sensorTypesA.eachWithIndex { sType, i -> 
			if (sType.value != "none"){
        		section("Pin A${i}"){
            		input(name: "sensorA${i}", type: "capability.${sType.value}", title: "Select the ${sType.value} device", required: true, multiple: false)
                    input(name: "actuatorA${i}", type: "bool", title: "On for Actuator, Off for Sensor", defaultValue: false)
            	}
        	}
		}        
    }
}

def page3() {
	dynamicPage(name: "page3") {
    	def sensorTypesD = settings.findAll { it.key.startsWith("sensorTypeD") }
        sensorTypesD.eachWithIndex { pConfig, i -> 
			if (settings["actuatorD${i}"]) {log.debug "sensorD${i} is an Actuator"}
            else {
            	if (settings["sensorD${i}"]) {
        		section("Sensor Type Pin D${i} settings for ${settings["sensorD${i}"]}"){
                    if (i in [0,1,2,3,4]) {
                		input(name: "instantD${i}", type: "bool", title: "Off for Polling, On for Instant update", defaultValue: true)
                	}
                	else {
                		paragraph "Polling only on this Pin"
                	}
                    input(name: "resetD${i}", type: "number", title: "Sensor reset delay (in seconds, limits fast on/off reporting on motion sensors, etc.)", defaultValue: 300)
            	}
                }
        	}
		}
        
        def sensorTypesA = settings.findAll { it.key.startsWith("sensorTypeA") }
        sensorTypesA.eachWithIndex { pConfig, i -> 
			if (settings["actuatorA${i}"]) {log.debug "sensorA${i} is an Actuator"}
            else {
            	if (settings["sensorA${i}"]) {
        		section("Sensor Type Pin A${i} settings for ${settings["sensorA${i}"]}"){
                    if (i in [0,1,3,4,5,6,7]) {
                		input(name: "instantA${i}", type: "bool", title: "Off for Polling, On for Instant update", defaultValue: true)
                	}
                	else {
                		paragraph "Polling only on this Pin"
                	}
                    input(name: "resetA${i}", type: "number", title: "Sensor reset delay (in seconds, limits fast on/off reporting on motion sensors, etc.)", defaultValue: 300)
            	}
                }
        	}
		}
        
    }
}

def page4() {
	dynamicPage(name: "page4") {
       	section("Spark Device to use"){
   			def sparkDevices = getDevices()
   	    	input(name: "sparkDevice", type: "enum", title: "Select the spark Device", required: true, multiple: false, options: sparkDevices)
       	}
        section {
            label title: "Assign a name", required: false
            
        }
	}
}

// get a list of the spark devices from the account
def getDevices() {
	def sparkDevices = [:]
	//Spark Core API Call
    def readingClosure = { response -> response.data.each { core ->
    		sparkDevices.put(core.id, core.name)  
        }
	}
    httpGet("https://api.spark.io/v1/devices?access_token=${state.sparkToken}", readingClosure)
 	return sparkDevices
}

// hey, is there an accessToken yet?
def checkToken() {
	if (!state.accessToken) {
    	createAccessToken() 
    }
}

mappings {
    path("/stdata/:webhookName/:pin/:data") {
        action: [
            GET: "setDeviceState"
        ]
    }
}

def installed() {
	state.webhookName = "dev${sparkDevice[-6..-1]}"
    state.appURL = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/stdata/${state.webhookName}/{{pin}}/{{data}}?access_token=${state.accessToken}"
	//log.debug "Installed with settings: ${settings}"
    checkWebhook() 
    //setupSensors()
}

def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
    checkWebhook()
    setupSensors()
}

def uninstalled() {
  log.debug "Uninstalling SparkThings"
  deleteWebhook()
  deleteAccessToken()
  log.debug "SparkThings Uninstalled"
}


def switchOnHandler(evt) {
	evt.getProperties().each {e -> log.debug "${e}" }
    log.debug "switch turned on!"
    def pinToSet = "D0"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setOn?access_token=${state.sparkToken}","command=${pinToSet}",) {response -> log.debug (response.data)}
}

def switchOffHandler(evt) {
    log.debug "switch turned off!"
    def pinToSet = "D0"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setOff?access_token=${state.sparkToken}","command=${pinToSet}",) {response -> log.debug (response.data)}
}

def switchValueHandler(evt) {
    log.debug "switch dimmed to ${evt.value}!"
    def pinToSet = "D0"
    def pinValue = "101"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setValue?access_token=${state.sparkToken}","command=${pinToSet}:${pinValue}",) {response -> log.debug (response.data)}
}

// got the webhook from the spark device update the ST device
def setDeviceState() {
	log.debug "Got webhook - pin: ${params.pin} data: ${params.data}"
	
	switch(params.pin) {
        case "D0":
        	changeDeviceState(sensorD0, sensorTypeD0)
            break
        case "D1":
        	changeDeviceState(sensorD1, sensorTypeD1)
            break
        case "D2":
        	changeDeviceState(sensorD2, sensorTypeD2)
            break
        case "D3":
        	changeDeviceState(sensorD3, sensorTypeD3)
            break
        case "D4":
        	changeDeviceState(sensorD4, sensorTypeD4)
            break
        case "D5":
        	changeDeviceState(sensorD5, sensorTypeD5)
            break
        case "D6":
        	changeDeviceState(sensorD6, sensorTypeD6)
            break
        case "D7":
        	changeDeviceState(sensorD7, sensorTypeD7)
            break
        case "A0":
        	changeDeviceState(sensorA0, sensorTypeA0)
            break
        case "A1":
        	changeDeviceState(sensorA1, sensorTypeA1)
            break
        case "A2":
        	changeDeviceState(sensorA2, sensorTypeA2)
            break
        case "A3":
        	changeDeviceState(sensorA3, sensorTypeA3)
            break
        case "A4":
        	changeDeviceState(sensorA4, sensorTypeA4)
            break          
        case "A5":
        	changeDeviceState(sensorA5, sensorTypeA5)
            break
        case "A6":
        	changeDeviceState(sensorA6, sensorTypeA6)
            break            
        case "A7":
        	changeDeviceState(sensorA7, sensorTypeA7)
            break
        case "config":
        	return [Respond: state.configString]
        	break
		default:
            break
    }
    
  //return [Respond: "OK"]
}

// Update the ST device based on what devicetype it is
void changeDeviceState(device, sensorType) {
log.debug "Pin: ${params.pin} State: ${params.data}"
	switch(sensorType) {
    	case "alarm":     
        	(params.data == "on") ? device.both() : device.off()
        	break
        case "button":
        	if (params.data == "on") {device.push()}
            	else {device.off()}
        	break
        case "carbonMonoxideSensor":
        	if (params.data == "on") {device.detected()}
            	else {device.clear()}
        	break
        case "contactSensor":
        	if (params.data == "on") {device.open()}
            	else {device.close()}
        	break
        case "doorControl":
        	if (params.data == "on") {device.open()}
            	else {device.close()}
        	break
        case "lock":
        	if (params.data == "on") {device.lock()}
            	else {device.unlock()}
        	break
        case "momentary":
            if (params.data == "on") {device.push()}
        	break
        case "motionSensor":
        	if (params.data == "on") {device.active()}
            	else if (params.data == "off") {device.inactive()}
        	break
        case "presenceSensor":
        	if (params.data == "on") {device.arrived()}
            	else {device.departed()}
        	break
        case "relaySwitch":
        	if (params.data == "on") {device.on()}
            	else {device.off()}
        	break
        case "switch":
        	if (params.data == "on") {device.on()}
            	else {device.off()}
        	break
        case "sleepSensor":
        	break
        case "smokeDetector":
        	if (params.data == "on") {device.detected()}
            	else {device.clear()}
        	break
        case "valve":
        	if (params.data == "on") {device.open()}
            	else {device.close()}
        	break
        case "waterSensor":
        	if (params.data == "on") {device.wet()}
            	else {device.dry()}
        	break
       
    }
    
}

// -----------------------------------------------------------
// Setup and config section
// -----------------------------------------------------------

// creates the config string to send to the spark device when requested
def setupSensors() {
	String configString = ""
    def sensorTypesD = settings.findAll { it.key.startsWith("sensorTypeD") }
    sensorTypesD.eachWithIndex { sType, i -> 
    	if (sType.value == "none"){
        	configString += "1,0,0,"
        }
        else {
        	if (settings["resetD${i}"]) {
	        	configString += settings["resetD${i}"].value
                configString += ","
            }
            else {
            	configString += "1,"
            }
            configString += sensorTypeLookup(sType) + ","
            configString += (settings["instantD${i}"]) ? "1," : "0,"
        }
    }
    def sensorTypesA = settings.findAll { it.key.startsWith("sensorTypeA") }
    sensorTypesA.eachWithIndex { sType, i -> 
    	if (sType.value == "none"){
        	configString += "1,0,0,"
        }
        else {
        	if (settings["resetA${i}"]) {
	        	configString += settings["resetA${i}"].value
                configString += ","
            }
            else {
            	configString += "1,"
            }
            configString += sensorTypeLookup(sType) + ","
            configString += (settings["instantA${i}"]) ? "1," : "0,"
        }
    }
    configString = configString[0..-2]
	state.configString = configString
	//subscribe(sensorA0, "switch.on", switchOnHandler)
    //subscribe(sensorA0, "switch.off", switchOffHandler)
    //subscribe(sensorA0, "switch.setLevel", switchValueHandler)
    subscribe(sensorA0, "momentary", switchValueHandler)
    configSpark()
}

// still working on this part...
def sensorTypeLookup(def sType) {
	log.debug sType
	return 1
}

// Check to see if we need to make the webhook or if it is there already
void checkWebhook() {
	int foundHook = 0
	httpGet(uri:"https://api.spark.io/v1/webhooks?access_token=${state.sparkToken}",
    ) {response -> response.data.each { 
    		hook ->
				//log.debug hook.event
                if (hook.event == state.webhookName) {
                	foundHook = 1
                	log.debug "Found existing webhook id: ${hook.id}"                    
                }
           }
    if (!foundHook) {
    	createWebhook()
    }
    else {
    }
 }        
}

// Create a new spark webhook for this app to use
void createWebhook() {
	log.debug "Creating a Spark webhook "
             
      httpPost(uri: "https://api.spark.io/v1/webhooks",
      			body: [access_token: state.sparkToken, 
        		event: state.webhookName,
                url: state.appURL, 
                requestType: "GET", 
                mydevices: true]
      			) {response -> log.debug (response.data)}
}

// TODO: add option to create a spark device that reports status and wifi signal strength automatically
void createSparkDevice() {
	//def sparkDevice = addChildDevice("rhworkshop", "Spark Device Status", ddni(vt), null, [name:vt, label:label, completedSetup: true])
}

// Send the spark device a config updated command so it will reboot and pull the new config
void configSpark() {
    log.debug "Updating Spark config, the Spark device will now restart"
    try {
    	httpPost("https://api.spark.io/v1/devices/${sparkDevice}/config?access_token=${state.sparkToken}","",) {response -> log.debug (response.data)}
 	} catch (all) {log.debug "Couldn't delete webhook, moving on"}
}

// -----------------------------------------------------------
// Uninstall Section
// -----------------------------------------------------------

// Cleanup on uninstall - remove the spark webhook for this app
void deleteWebhook() {
try{
httpGet(uri:"https://api.spark.io/v1/webhooks?access_token=${state.sparkToken}",
    ) {response -> response.data.each { 
    		hook ->
				//log.debug hook.event
                if (hook.event == state.webhookName) {
                	httpDelete(uri:"https://api.spark.io/v1/webhooks/${hook.id}?access_token=${state.sparkToken}")
                    log.debug "Deleted the existing webhook with the id: ${hook.id} and the event name: ${state.webhookName}"
                }
           }

 }
 } catch (all) {log.debug "Couldn't delete webhook, moving on"}
}

// Cleanup on uninstall - remove the spark access token
void deleteAccessToken() {
try{
	def authEncoded = "${sparkUsername}:${sparkPassword}".bytes.encodeBase64()
	def params = [
    	uri: "https://api.spark.io/v1/access_tokens/${state.sparkToken}",
    	headers: [
        	'Authorization': "Basic ${authEncoded}"
    	]
	]

	httpDelete(params) //uri:"https://${sparkUsername}:${sparkPassword}@api.spark.io/v1/access_tokens/${state.sparkToken}")
	log.debug "Deleted the existing Spark Access Token"
 } catch (all) {log.debug "Couldn't delete Spark Token, moving on"}
}
