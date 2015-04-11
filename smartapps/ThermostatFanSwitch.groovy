/**
 *  Installation
 *  1) Copy this code and paste into a new SmartApp
 *  2) Create a New Device and choose Simulated Switch as the Device Type (or just use an existing switch to trigger it)
 *  3) configure the SmartApp and pick the simulated Switch and the Thermostat to control the Fan on
 *
 *  Thermostat Fan Switch
 *
 *  Author: SmartThings
 *
 *  Date: Jan 17 2015
 */
definition(
	name: "Thermostat Fan Switch",
	namespace: "rhworkshop",
	author: "Andy Rawson",
	description: "Turns on and off a Thermostat Fan based on the state of a specific switch.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan%402x.png"
)

preferences {
	section("When this switch is turned on or off") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Turn on or off all of these thermostat fans as well") {
		input "thermostats", "capability.thermostat", multiple: true, required: true
	}
}

def installed()
{
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
}

def updated()
{
	unsubscribe()
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
}

def onHandler(evt) {
	log.debug evt.value
	log.debug "Turning on Thermostat Fans"
	thermostats.fanOn()
}

def offHandler(evt) {
	log.debug evt.value
	log.debug "Turning off Thermostat Fans"
	thermostats.fanAuto()
}
