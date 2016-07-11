metadata {
	// Automatically generated. Make future change here.
	definition (name: "Motion Detector", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0009,0500"
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}

		main "motion"
		details "motion"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def name = null
	def value = description
	def descriptionText = null
	if (zigbee.isZoneType19(description)) {
		name = "motion"
		def isActive = zigbee.translateStatusZoneType19(description)
		value = isActive ? "active" : "inactive"
		descriptionText = isActive ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
	}

	def result = createEvent(
		name: name,
		value: value,
		descriptionText: descriptionText
	)

	log.debug "Parse returned ${result?.descriptionText}"
	return result
}
