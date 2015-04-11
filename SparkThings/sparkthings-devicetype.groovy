/**
 *  Spark Device Status
 *
 * 
 * 
 *  based on code by Copyright 2014 Nicholas Wilde
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
 
preferences {
        input "deviceId", "text", title: "Device ID", required: true
        input "token", "password", title: "Access Token", required: true
        input "sparkVar", "text", title: "Spark Variable", required: true
}

metadata {
	definition (name: "Spark Device Status", namespace: "rhworkshop", author: "Andy Rawson") {
		capability "Polling"
		capability "Refresh"
		capability "Signal Strength"
        capability "Switch"
	}

	simulator {
		// TODO: define status and reply messages here
	}
	
	tiles {
	    standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Offline', icon: "st.Electronics.electronics18", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Online', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", nextState: "off"
		}
		valueTile("rssi", "device.rssi", width: 2, height: 2) {
            	state "rssi", label:'${currentValue}dBm', unit: "",
            	backgroundColors:[
            		[value: 16, color: "#5600A3"],
					[value: -31, color: "#153591"],
					[value: -44, color: "#1e9cbb"],
					[value: -59, color: "#90d2a7"],
					[value: -74, color: "#44b621"],
					[value: -84, color: "#f1d801"],
					[value: -95, color: "#d04e00"],
					[value: -96, color: "#bc2323"]
				]
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        main (["rssi", "switch"])
        details(["rssi", "switch", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	getReading()
	checkStatus()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getReading()
    checkStatus()
}

// Get the sensor reading
private getReading() {
    //Spark Core API Call
    def readingClosure = { response ->
	  	log.debug "Reading request was successful, $response.data.result"
      	sendEvent(name: "rssi", value: response.data.result)
	}

    httpGet("https://api.spark.io/v1/devices/${deviceId}/${sparkVar}?access_token=${token}", readingClosure)
}

// Check the Spark status on the polling schedule so not realtime unless refreshed
private checkStatus() {
	//Spark Core API Call
    def readingClosure = { response ->
	  	log.debug "Spark status check was successful, $response.data.connected"
      	sendEvent(name: "switch", value: response.data.connected ? "on" : "off")
	}

    httpGet("https://api.spark.io/v1/devices/${deviceId}?access_token=${token}", readingClosure)	
}
	
