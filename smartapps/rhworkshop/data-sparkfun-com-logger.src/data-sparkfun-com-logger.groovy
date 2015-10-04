/**
 *
 *	Installation
 *
 *	1) Go to http://data.sparkfun.com and create a new data stream
 *		In the Fields box put: 		deviceid, datatype, value
 *	
 *	2) Copy this code into a new SmartApp
 * 
 *  3) Add the new app and configure it with your keys from data.sparkfun.com and select the devices to log
 *
 * Based on "SmartThings example Code for GroveStreams" by Jason Steele
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 */

definition(
		name: "data.sparkfun.com Logger",
		namespace: "rhworkshop",
		author: "Andy Rawson",
		description: "Log things to data.sparkfun.com",
		category: "My Apps",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Log these things") {
            input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
            input "humidities", "capability.relativeHumidityMeasurement", title: "Humidities", required: false, multiple: true
            input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
            input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
            input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
            input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
            input "switches", "capability.switch", title: "Switches", required: false, multiple: true
            input "waterSensors", "capability.waterSensor", title: "Water sensors", required: false, multiple: true
            input "batteries", "capability.battery", title: "Batteries", required:false, multiple: true
            input "powers", "capability.powerMeter", title: "Power Meters", required:false, multiple: true
            input "energies", "capability.energyMeter", title: "Energy Meters", required:false, multiple: true

	}

	section ("data.sparkfun.com Public Key") {
		input "publicKey", "text", title: "Public key"
	}
	section ("data.sparkfun.com Private Key") {
		input "privateKey", "text", title: "Private key"
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
        subscribe(temperatures, "temperature", handleTemperatureEvent)
        subscribe(waterSensors, "water", handleWaterEvent)
        subscribe(humidities, "humidity", handleHumidityEvent)
        subscribe(contacts, "contact", handleContactEvent)
        subscribe(accelerations, "acceleration", handleAccelerationEvent)
        subscribe(motions, "motion", handleMotionEvent)
        subscribe(presence, "presence", handlePresenceEvent)
        subscribe(switches, "switch", handleSwitchEvent)
        subscribe(batteries, "battery", handleBatteryEvent)
        subscribe(powers, "power", handlePowerEvent)
        subscribe(energies, "energy", handleEnergyEvent)
}

def handleTemperatureEvent(evt) {
        sendValue(evt) { it.toString() }
}
 
def handleWaterEvent(evt) {
        sendValue(evt) { it == "wet" ? "1" : "0" }
}
 
def handleHumidityEvent(evt) {
        sendValue(evt) { it.toString() }
}
 
def handleContactEvent(evt) {
        sendValue(evt) { it == "open" ? "1" : "0" }
}
 
def handleAccelerationEvent(evt) {
        sendValue(evt) { it == "active" ? "1" : "0" }
}
 
def handleMotionEvent(evt) {
        sendValue(evt) { it == "active" ? "1" : "0" }
}
 
def handlePresenceEvent(evt) {
        sendValue(evt) { it == "present" ? "1" : "0" }
}
 
def handleSwitchEvent(evt) {
        sendValue(evt) { it == "on" ? "1" : "0" }
}
 
def handleBatteryEvent(evt) {
        sendValue(evt) { it.toString() }
}
 
def handlePowerEvent(evt) {
        sendValue(evt) { it.toString() }
}
 
def handleEnergyEvent(evt) {
        sendValue(evt) { it.toString() }
}

private sendValue(evt, Closure convert) {
        def compId = URLEncoder.encode(evt.displayName.trim())
        def streamId = evt.name
        def value = convert(evt.value)

	log.debug "Logging to data.sparkfun.com ${compId}, ${streamId} = ${value}"
	
        def url = "https://data.sparkfun.com/input/${publicKey}?private_key=${privateKey}&deviceid=${compId}&datatype=${streamId}&value=${value}"
    //log.debug url
        
                
	httpGet(url) { response ->
                if (response.status != 200 ) {
                        log.debug "data.sparkfun.com logging failed, status = ${response.status}"
                }
	}
}