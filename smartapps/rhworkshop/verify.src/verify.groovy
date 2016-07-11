/**
 *  Verify
 *
 *  Copyright 2015 Andy Rawson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Verify",
    namespace: "rhworkshop",
    author: "Andy Rawson",
    description: "Verify that a command sent to a device worked and the device is now in the desired state.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select devices to verify") {
		input "switches", "capability.switch", multiple: true
        //input "cow", "text"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(switches, "switch", switchesHandler)
    subscribe(location, "alarmSystemStatus", alarmHandler)
    subscribe(location, "stream", alarmHandler)
    subscribe(location, "activity", alarmHandler)
    subscribe(location, "battery", batteryHandler)
    log.debug "alarm state: ${location.currentState("alarmSystemStatus")?.value}"
    //def loc = "location"
    //location.smartApps.each {
	//	loc += "$it"}
    //log.debug "Location: ${loc}"
    //subscribe(security, "intrusion", alarmHandler)
    //this.each {
	 //location.hubs.getProperties()
    def s = "${location.hubs[0].hub.getProperties()}"
    def l = s.toList().collate( 350 )*.join()
    l.each {log.debug "Verify: ${it}"}
    log.debug "verify info: ${switches.device.groupId}"
    //}
    //sendLocationEvent(name: "alarmSystemStatus", value: "off")
    
        //def incident = location.incidents[0]
        //incident.close
		//log.debug "${incident.getProperties()}"
	/*
    // try to find another app and change it's settings
    location.helloHome.childApps.each {
    	if (it.name == "Hub Offline Notification") {
        	log.debug "Found the Hub Offline Notification app id ${it.id}"
            log.debug "Settings: ${it.appSettings}"
        }
        if (it.name == "Low Battery Notification") {
        	log.debug "Found the Low Battery Notification app id ${it.id}"
            log.debug "Settings: ${it.appSettings}"
        }
    }
    */
    
}

def doReflection(obj)  
{    
   def methods = obj.getProperties()  
   def methodsNames = new StringBuilder()
   methodsNames << "Reflection:"  
   //methodsNames << "\tClass Name: ${obj.class.name}"
   methods.each  
   {  
      methodsNames << "${it} " 
   }  

   methodsNames
}  

def batteryHandler(evt) {
    log.debug "Battery Event device: ${evt.device}, Battery Level: ${evt.value}"
}

def alarmHandler(evt) {
	log.debug "Verify Event name: ${evt.name}"
	log.debug "Verify Event value: ${evt.value}"
}

def switchesHandler(evt) {
	def d = evt.device
    def val = evt.value
    if (evt.value == "on") {
        log.debug "${evt.device.hub.id} switch turned on."
    } else if (evt.value == "off") {
        log.debug "${d} switch turned off."
    }
    
    if (d.hasCommand("refresh")) {
    	d.refresh()
        log.debug "${d} switch refresh"
        if (d.currentSwitch != val) {
        	log.debug "${d} switch was not turned ${val}!"
        } else {
        	log.debug "${d} switch was verified ${val}"
        }
    } else if (d.hasCommand("poll")) {
    	d.poll()
        log.debug "${d} switch poll"
    } else {log.debug "${d} doesn't have polling or refresh commands"}
}

def checkSwitch() {

}