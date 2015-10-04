/**
 *  Left open when mode changes
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
    name: "Left open when mode changes",
    namespace: "rhworkshop",
    author: "Andy Rawson",
    description: "Notify when a contact is left open when the mode changes.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("What to monitor") {
		input "contacts", "capability.contactSensor", title: "Contacts to monitor", required: true, multiple: true
        input "theMode", "mode", title: "The mode is changed to...", required: true, multiple: false
        }
    section("Send Notifications to") {
        input("recipients", "contact", title: "Send notifications to")
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
	subscribe(location, eventHandler) 
}

def eventHandler(evt) {
	if (location.currentMode == theMode){
    	log.debug "Mode is ${location.mode}"
        contacts.each {
        	if (it.currentContact == "open") {
            	log.debug "${it} is open when the mode changed to ${location.mode}"
                sendNotificationToContacts("${it} is open when the mode changed to ${location.mode}", recipients)
            }
        }
    }
}
