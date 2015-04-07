/*
 *  Spark Device
 *
 *  
 *
*/
definition(
    name: "Spark Device",
    namespace: "RH Workshop",
    author: "Andy Rawson",
    description: "Allows the Spark to control virtual devices in SmartThings",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "page1", title: "Select sensor types", nextPage: "page2", uninstall: true) {
        section {
        
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
                "sleepSensor":"Sleep Sensor",
                "smokeDetector":"Smoke Detector",
                "valve":"Valve",
                "waterSensor": "Water Sensor"
                ]
        
            input("sensorTypeD0", title: "Select sensor type for Pin D0", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD1", title: "Select sensor type for Pin D1", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD2", title: "Select sensor type for Pin D2", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD3", title: "Select sensor type for Pin D3", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD4", title: "Select sensor type for Pin D4", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD5", title: "Select sensor type for Pin D5", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD6", title: "Select sensor type for Pin D6", "enum", defaultValue: "none", options: opt)
            input("sensorTypeD7", title: "Select sensor type for Pin D7", "enum", defaultValue: "none", options: opt)
            
        }
    }

    page(name: "page2", title: "Select devices", nextPage: "page3", install: false, uninstall: false)
	page(name: "page3", title: "Webhooks URL", install: true, uninstall: false)
}

def page2() {
    dynamicPage(name: "page2") {
        section {      
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
    }
}

def page3() {
	dynamicPage(name: "page3") {
		section {
            checkToken() 
            state.appURL = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/digitalPin?access_token=${state.accessToken}"
            log.debug "Spark Webhooks URL: ${state.appURL}"
			paragraph "Spark Webhooks URL: ${state.appURL}"
            input "phone", "phone", title: "Phone Number to text the URL to", required: false
		}
	}
}

def checkToken() {
	if (!state.accessToken) {
    	createAccessToken() 
    }
}

def sendTXT() {
    if (phone) {
        log.debug "Sending Webhooks URL by SMS: ${state.appURL}"
        sendSms(phone, state.appURL)
    }
}

mappings {

    path("/digitalPin") {
        action: [
            GET: "setDeviceState"
        ]
    }
    path("/analogPin/:pin/:value") {
        action: [
            GET: "setDeviceValue"
        ]
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    sendTXT()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    sendTXT()
}

void setDeviceState() {
	log.debug "Got webhook - Pin: ${params.pin} State: ${params.state}"
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
        default:
            break
    }
}

void changeDeviceState(device, sensorType) {
log.debug "Pin: ${params.pin} State: ${params.state}"
	switch(sensorType) {
    	case "alarm":
        	if (params.state) {device.both()}
            	else {device.off()}
        	break
        case "accelerationSensor":
        	if (params.state == "on") {device.active()}
            	else if (params.state == "off") {device.inactive()}
        	break
        case "button":
        	if (params.state) {device.push()}
            	else {device.off()}
        	break
        case "carbonMonoxideSensor":
        	if (params.state) {device.detected()}
            	else {device.clear()}
        	break
        case "contactSensor":
        	if (params.state) {device.open()}
            	else {device.closed()}
        	break
        case "doorControl":
        	if (params.state) {device.open()}
            	else {device.closed()}
        	break
        case "lock":
        	if (params.state) {device.locked()}
            	else {device.unlocked()}
        	break
        case "momentary":
        	break
        case "motionSensor":
        	if (params.state == "on") {device.active()}
            	else if (params.state == "off") {device.inactive()}
        	break
        case "presenceSensor":
        	if (params.state) {device.arrived()}
            	else {device.departed()}
        	break
        case "relaySwitch":
        	if (params.state) {device.on()}
            	else {device.off()}
        	break
        case "switch":
        	if (params.state) {device.on()}
            	else {device.off()}
        	break
        case "sleepSensor":
        	break
        case "smokeDetector":
        	if (params.state) {device.detected()}
            	else {device.clear()}
        	break
        case "valve":
        	if (params.state) {device.open()}
            	else {device.closed()}
        	break
        case "waterSensor":
        	if (params.state) {device.wet()}
            	else {device.dry()}
        	break
       
    }
    
}
