/**
 *  Lock to Contact Sensor
 *
 *  Author: SmartThings
 *
 *  Date: Jan 17 2015
 */
definition(
	name: "Lock to virtual contact sensor",
	namespace: "rhworkshop",
	author: "Andy Rawson",
	description: "Opens or closes a virtual contact switch when a lock is locked or unlocked",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan%402x.png"
)

preferences {
	section("When this lock is locked or unlocked") {
		input "master", "capability.lock", title: "Where?"
	}
	section("open or close this contact switch") {
		input "contacts", "capability.contactSensor", multiple: false, required: true
	}
}

def installed()
{
	subscribe(master, "lock.locked", lockedHandler)
	subscribe(master, "lock.unlocked", unlockedHandler)
}

def updated()
{
	unsubscribe()
	subscribe(master, "lock.locked", lockedHandler)
	subscribe(master, "lock.unlocked", unlockedHandler)
}

def lockedHandler(evt) {
	log.debug evt.value
	contacts.close()
}

def unlockedHandler(evt) {
	log.debug evt.value
	contacts.open()
}