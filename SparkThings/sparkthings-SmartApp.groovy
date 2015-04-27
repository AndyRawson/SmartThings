/*
 *  SparkThings
 *
 *  
 *
*/
definition(
    name: "SparkThings",
    namespace: "rhworkshop",
    author: "Andy Rawson",
    description: "Allows a Spark.io device to be used with SmartThings",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "page1", title: "Select sensor types", nextPage: "page2", uninstall: true) {
            def opt = ["none":"Not used",
                "alarm":"Alarm",
                "accelerationSensor":"Acceleration Sensor",
                "button":"Button",
                "carbonMonoxideSensor":"Carbon Monoxide Sensor",
                "contactSensor":"Contact Sensor",
                "doorControl":"Door Control",
                "lock": "Lock",
                "momentary":"Momentary",
                "motionSensor":"Motion Sensor",
                "presenceSensor":"Presence Sensor",
                "relaySwitch":"Relay Switch",
                "switch": "Switch",
                "switchLevel": "Switch Level",
                "sleepSensor":"Sleep Sensor",
                "smokeDetector":"Smoke Detector",
                "valve":"Valve",
                "waterSensor": "Water Sensor"
                ]
        section("Generate Username and Password") {
        	input "sparkUsername", "text", title: "Your Spark.io Username", required: true
            input "sparkPassword", "password", title: "Your Spark.io Password", required: true
        }
        section("Digital Pins") {
        	for ( i in 0..7 ) {
            	input("sensorTypeD${i}", title: "Select sensor type for Pin D${i}", "enum", defaultValue: "none", options: opt)
            }

        }
        section("Analog Pins") {        
        	for ( i in 0..7 ) {
            	input("sensorTypeA${i}", title: "Select sensor type for Pin A${i}", "enum", defaultValue: "none", options: opt)
            }
            
        }
    }

    page(name: "page2", title: "Select devices", nextPage: "page3", install: false, uninstall: false)
	page(name: "page3", title: "Webhooks URL", install: true, uninstall: false)
}

def page2() {
    dynamicPage(name: "page2") {
    
   if (!state.sparkToken){
        httpPost(uri: "https://spark:spark@api.spark.io/oauth/token",
		body: [grant_type: "password", 
        username: sparkUsername,
        password: sparkPassword,
        expires_in: "157680000"] 
      		) {response -> state.sparkToken = response.data.access_token
               	}
        log.debug "Did it work? ${state.sparkToken}"
        checkToken() 

    }
    
        section("Digital Pin Devices"){
        if (sensorTypeD0 != "none"){
            input(name: "sensorD0", type: "capability.$sensorTypeD0", title: "Select the $sensorTypeD0 device for Pin D0", required: false, multiple: false)
            }
            
        if (sensorTypeD1 != "none"){
            input(name: "sensorD1", type: "capability.$sensorTypeD1", title: "Select the $sensorTypeD1 device for Pin D1", required: false, multiple: false)
            }
        
        if (sensorTypeD2 != "none"){
            input(name: "sensorD2", type: "capability.$sensorTypeD2", title: "Select the $sensorTypeD2 device for Pin D2", required: false, multiple: false)
            }
            
        if (sensorTypeD3 != "none"){
            input(name: "sensorD3", type: "capability.$sensorTypeD3", title: "Select the $sensorTypeD3 device for Pin D3", required: false, multiple: false)
            }
            
        if (sensorTypeD4 != "none"){
            input(name: "sensorD4", type: "capability.$sensorTypeD4", title: "Select the $sensorTypeD4 device for Pin D4", required: false, multiple: false)
            }

        if (sensorTypeD5 != "none"){
            input(name: "sensorD5", type: "capability.$sensorTypeD5", title: "Select the $sensorTypeD5 device for Pin D5", required: false, multiple: false)
            }
            
        if (sensorTypeD6 != "none"){
            input(name: "sensorD6", type: "capability.$sensorTypeD6", title: "Select the $sensorTypeD6 device for Pin D6", required: false, multiple: false)
            }
            
        if (sensorTypeD7 != "none"){
            input(name: "sensorD7", type: "capability.$sensorTypeD7", title: "Select the $sensorTypeD7 device for Pin D7", required: false, multiple: false)
            }
		}
        section("Analog Pin Devices"){
        if (sensorTypeA0 != "none"){
            input(name: "sensorA0", type: "capability.$sensorTypeA0", title: "Select the $sensorTypeA0 device for Pin A0", required: false, multiple: false)
            }
            
        if (sensorTypeA1 != "none"){
            input(name: "sensorA1", type: "capability.$sensorTypeA1", title: "Select the $sensorTypeA1 device for Pin A1", required: false, multiple: false)
            }
        
        if (sensorTypeA2 != "none"){
            input(name: "sensorA2", type: "capability.$sensorTypeA2", title: "Select the $sensorTypeA2 device for Pin A2", required: false, multiple: false)
            }
            
        if (sensorTypeA3 != "none"){
            input(name: "sensorA3", type: "capability.$sensorTypeA3", title: "Select the $sensorTypeA3 device for Pin A3", required: false, multiple: false)
            }
            
        if (sensorTypeA4 != "none"){
            input(name: "sensorA4", type: "capability.$sensorTypeA4", title: "Select the $sensorTypeA4 device for Pin A4", required: false, multiple: false)
            }

        if (sensorTypeA5 != "none"){
            input(name: "sensorA5", type: "capability.$sensorTypeA5", title: "Select the $sensorTypeA5 device for Pin A5", required: false, multiple: false)
            }
            
        if (sensorTypeA6 != "none"){
            input(name: "sensorA6", type: "capability.$sensorTypeA6", title: "Select the $sensorTypeA6 device for Pin A6", required: false, multiple: false)
            }
            
        if (sensorTypeA7 != "none"){
            input(name: "sensorA7", type: "capability.$sensorTypeA7", title: "Select the $sensorTypeA7 device for Pin A7", required: false, multiple: false)
            }
		}
        
    }
}

def page3() {
	dynamicPage(name: "page3") {
       	section("Spark Device to use"){
   			def sparkDevices = getDevices()
   	    	input(name: "sparkDevice", type: "enum", title: "Select the spark Device", required: true, multiple: false, options: sparkDevices)
       	}
        section {
            label title: "Assign a name", required: false
            
        }
	}
}


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
    //checkWebhook()
    //createSparkDevice()
}

def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
    checkWebhook()
    subscribe(sensorA0, "switch.on", switchOnHandler)
    subscribe(sensorA0, "switch.off", switchOffHandler)
    subscribe(sensorA0, "switch.setLevel", switchValueHandler)
}

def uninstalled() {
  log.debug "Uninstalling SparkThings"
  deleteWebhook()
  deleteAccessToken()
}

def switchOnHandler(evt) {
    log.debug "switch turned on!"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setOn?access_token=${state.sparkToken}","command=switch1",) {response -> log.debug (response.data)}

}

def switchOffHandler(evt) {
    log.debug "switch turned off!"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setOff?access_token=${state.sparkToken}","command=switch1",) {response -> log.debug (response.data)}

}

def switchValueHandler(evt) {
    log.debug "switch dimmed to ${evt.value}!"
    httpPost("https://api.spark.io/v1/devices/${sparkDevice}/setValue?access_token=${state.sparkToken}","command=switch1:${evt.value}",) {response -> log.debug (response.data)}

}

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

void deleteAccessToken() {
//try{
	httpDelete(uri:"https://${sparkUsername}:${sparkPassword}@api.spark.io/v1/access_tokens/${state.sparkToken}")
	log.debug "Deleted the existing Spark Access Token"
 //} catch (all) {log.debug "Couldn't delete Spark Token, moving on"}
}

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

void createSparkDevice() {
	//def sparkDevice = addChildDevice("rhworkshop", "Spark Device Status", ddni(vt), null, [name:vt, label:label, completedSetup: true])
}

void setDeviceState() {
	log.debug "Got webhook - pin: ${params.pin} data: ${params.data}"

	switch(params.pin) {
        case "contact1":
        	changeDeviceState(sensorD0, sensorTypeD0)
            break
        case "contact2":
        	changeDeviceState(sensorD1, sensorTypeD1)
            break
        case "contact3":
        	changeDeviceState(sensorD2, sensorTypeD2)
            break
        case "contact4":
        	changeDeviceState(sensorD3, sensorTypeD3)
            break
        case "contact5":
        	changeDeviceState(sensorD4, sensorTypeD4)
            break
        case "contact6":
        	changeDeviceState(sensorD5, sensorTypeD5)
            break
        case "contact7":
        	changeDeviceState(sensorD6, sensorTypeD6)
            break
        case "contact8":
        	changeDeviceState(sensorD7, sensorTypeD7)
            break
        case "HallMotion":
        	changeDeviceState(sensorA0, sensorTypeA0)
            break
        case "BasementMotion":
        	changeDeviceState(sensorA1, sensorTypeA1)
            break
        case "contact9":
        	changeDeviceState(sensorA2, sensorTypeA2)
            break
        case "contact10":
        	changeDeviceState(sensorA3, sensorTypeA3)
            break
        case "contact11":
        	changeDeviceState(sensorA4, sensorTypeA4)
            break          
        case "contact12":
        	changeDeviceState(sensorA5, sensorTypeA5)
            break
        case "contact13":
        	changeDeviceState(sensorA6, sensorTypeA6)
            break            
        case "contact14":
        	changeDeviceState(sensorA7, sensorTypeA7)
            break    
		default:
            break
    }
   
}

void changeDeviceState(device, sensorType) {
log.debug "Pin: ${params.pin} State: ${params.data}"
	switch(sensorType) {
    	case "alarm":
        	if (params.data) {device.both()}
            	else {device.off()}
        	break
        case "accelerationSensor":
        	if (params.data == "on") {device.active()}
            	else if (params.data == "off") {device.inactive()}
        	break
        case "button":
        	if (params.data) {device.push()}
            	else {device.off()}
        	break
        case "carbonMonoxideSensor":
        	if (params.data) {device.detected()}
            	else {device.clear()}
        	break
        case "contactSensor":
        	if (params.data) {device.open()}
            	else {device.closed()}
        	break
        case "doorControl":
        	if (params.data) {device.open()}
            	else {device.closed()}
        	break
        case "lock":
        	if (params.data) {device.locked()}
            	else {device.unlocked()}
        	break
        case "momentary":
        	break
        case "motionSensor":
        	if (params.data == "on") {device.active()}
            	else if (params.data == "off") {device.inactive()}
        	break
        case "presenceSensor":
        	if (params.data) {device.arrived()}
            	else {device.departed()}
        	break
        case "relaySwitch":
        	if (params.data) {device.on()}
            	else {device.off()}
        	break
        case "switch":
        	if (params.data) {device.on()}
            	else {device.off()}
        	break
        case "sleepSensor":
        	break
        case "smokeDetector":
        	if (params.data) {device.detected()}
            	else {device.clear()}
        	break
        case "valve":
        	if (params.data) {device.open()}
            	else {device.closed()}
        	break
        case "waterSensor":
        	if (params.data) {device.wet()}
            	else {device.dry()}
        	break
       
    }
    
}
