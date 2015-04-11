/**
 *  TED5000 Lan Device Type for SmartThings
 *
 *	Based on:
 *  [TED5000 Device Type for SmartThings				]
 *  [Author: badgermanus@gmail.com						]
 *  [Code: https://github.com/jwsf/device-type.ted5000	]
 *
 * INSTALLATION
 * ========================================
 *
 *
 * 1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *	     Copy this code into it and create/save/publish it
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: TED5000 Local (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Select your Hub on the LAN with the TED5000
 *
 * 3) Update device preferences
 *     Click on the new device in the mobile app to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your device access information - username, password & URL
 *
 * Copyright (C) 2014 Jonathan Wilson  <badgermanus@gmail.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
preferences {
    input("ip", "string", title: "IP Address", description: "10.0.1.14", required: true, displayDuringSetup: true)
    input("port", "string", title: "Port", description: "80", defaultValue: 80, required: true, displayDuringSetup: true)
    input("username", "string", title: "Username", description: "user", required: false, displayDuringSetup: true)
    input("password", "password", title: "Password", description: "password", required: false, displayDuringSetup: true)
    input(type: "enum", name: "chartGranularity", title: "Chart Granularity", options: granularityOptions(), defaultValue: "Daily", style: "segmented")
}

metadata {
    // Automatically generated. Make future change here.
    definition(name: "TED5000 Local", author: "Andy Rawson", namespace: "rhworkshop") {
        capability "Energy Meter"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Power Meter"
    }

    // simulator metadata
    simulator {
        for (int i = 100; i <= 3000; i += 200) {
            status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
        }
        for (int i = 0; i <= 100; i += 10) {
            status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
        }
    }
    // UI tile definitions
    tiles {

        valueTile("power",
            "device.power",
            width: 2,
            height: 2,
            decoration: "flat"
        ) {
            state("power",
                label: '${currentValue} W',
                backgroundColors: [
                    [value: 200, color: "#153591"],
                    [value: 400, color: "#1e9cbb"],
                    [value: 600, color: "#90d2a7"],
                    [value: 700, color: "#44b621"],
                    [value: 1000, color: "#f1d801"],
                    [value: 1200, color: "#d04e00"],
                    [value: 1400, color: "#bc2323"]
                ]
            )
        }

        chartTile(name: "powerChart", attribute: "power")

        valueTile("energy",
            "device.energy"
        ) {
            state("energy",
                label: '${currentValue} kWh'
            )
        }
        standardTile("refresh", "device.power") {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }


        main(["power", "energy"])
        details(["powerChart", "power", "energy", "refresh"])
    }
}

def parse(String description) {
    //log.debug "Parsing '${description}'"

    def map = stringToMap(description)

    def result = []

    if (map.headers && map.body) { //got device info response
        def headerString = new String(map.headers.decodeBase64())
        if (headerString.contains("404 Not Found")) {
            log.debug "Got 404"
        }

        if (map.body) {
            def bodyString = new String(map.body.decodeBase64())
            def body = new XmlSlurper().parseText(bodyString)

            def powerNow = body.Power.Total.PowerNow
            def energy = body.Power.Total.PowerMTD
            energy = Math.round(energy.toBigDecimal() * 0.001)

            log.debug "Sending event - power: $powerNow W and month-to-date: $energy KWH"
            sendEvent(name: "power", value: powerNow, unit: "W")
            sendEvent(name: "energy", value: energy, unit: "KWH")
        }
    }

    result


}

def poll() {
    getTEDData()
}

def refresh() {
    getTEDData()
}


def getVisualizationData(attribute) {
    log.debug "getChartData for $attribute"
    def keyBase = "measure.${attribute}${getGranularity()}"
    log.debug "getChartData state = $state"

    def dateBuckets = state[keyBase]

    //convert to the right format
    def results = dateBuckets ? .sort {
        it.key
    }.collect {
        [
            date: Date.parse("yyyy-MM-dd", it.key),
            average: it.value.average,
            min: it.value.min,
            max: it.value.max
        ]
    }

    log.debug "getChartData results = $results"
    results
}


private storeData(attribute, value, dateString = getKeyFromDateDaily()) {
    log.debug "storeData initial state: $state"
    def keyBase = "measure.${attribute}"
    def numberValue = value.toBigDecimal()

    // create bucket if it doesn't exist
    if (!state[keyBase]) {
        state[keyBase] = [: ]
        log.debug "storeData - attribute not found. New state: $state"
    }

    if (!state[keyBase][dateString]) {
        //no date bucket yet, fill with initial values
        state[keyBase][dateString] = [: ]
        state[keyBase][dateString].average = numberValue
        state[keyBase][dateString].runningSum = numberValue
        state[keyBase][dateString].runningCount = 1
        state[keyBase][dateString].min = numberValue
        state[keyBase][dateString].max = numberValue

        log.debug "storeData date bucket not found. New state: $state"

        // remove old buckets
        def old = getKeyFromDateDaily(new Date() - 10)
        state[keyBase].findAll {
            it.key < old
        }.collect {
            it.key
        }.each {
            state[keyBase].remove(it)
        }
    } else {
        //re-calculate average/min/max for this bucket
        state[keyBase][dateString].runningSum = (state[keyBase][dateString].runningSum.toBigDecimal()) + numberValue
        state[keyBase][dateString].runningCount = state[keyBase][dateString].runningCount.toInteger() + 1
        state[keyBase][dateString].average = state[keyBase][dateString].runningSum.toBigDecimal() / state[keyBase][dateString].runningCount.toInteger()

        log.debug "storeData after average calculations. New state: $state"

        if (state[keyBase][dateString].min == null) {
            state[keyBase][dateString].min = numberValue
        } else if (numberValue < state[keyBase][dateString].min.toBigDecimal()) {
            state[keyBase][dateString].min = numberValue
        }
        if (state[keyBase][dateString].max == null) {
            state[keyBase][dateString].max = numberValue
        } else if (numberValue > state[keyBase][dateString].max.toBigDecimal()) {
            state[keyBase][dateString].max = numberValue
        }
    }
    log.debug "storeData after min/max calculations. New state: $state"
}


// This next method is only used from the simulator
def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    if (cmd.scale == 0) {
        [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
    } else if (cmd.scale == 1) {
        [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
    } else {
        [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
    }
}




def getGranularity() {
    chartGranularity ? : "Daily"
}

def granularityOptions() {
    ["Daily", "Hourly"]
}

private getKeyFromDateDaily(date = new Date()) {
    date.format("yyyy-MM-dd")
}

private getKeyFromDateHourly(date = new Date()) {
    date.format("yyyy-MM-dd:HH")
}


private getTEDData() {
    def uri = "/api/LiveData.xml"
    getAction(uri)

}

private getAction(uri) {
    setDeviceNetworkId(ip, port)

    def userpass = encodeCredentials(username, password)

    def headers = getHeader(userpass)

    def hubAction = new physicalgraph.device.HubAction(
            method: "GET",
            path: uri,
            headers: headers
        ) //,delayAction(1000), refresh()]
    log.debug("Executing hubAction on " + getHostAddress())

    try {

        //log.debug hubAction
        hubAction
    } catch (Exception e) {
        log.debug "Hit Exception $e on $hubAction"
    }


}


private encodeCredentials(username, password) {
    // log.debug "Encoding credentials"
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
        //log.debug "ASCII credentials are ${userpassascii}"
        //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass) {
    //log.debug "Getting headers"
    def headers = [: ]
    headers.put("HOST", getHostAddress())
    if (settings.username != null) {
        headers.put("Authorization", userpass)
    }
    //log.debug "Headers are ${headers}"
    return headers
}

private setDeviceNetworkId(ip, port) {
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
        //log.debug "Device Network Id set to ${iphex}:${porthex}"
}

private getHostAddress() {
    return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize('.').collect {
        String.format('%02x', it.toInteger())
    }.join()
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format('%04x', port.toInteger())
    return hexport
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex, 16)
}


private String convertHexToIP(hex) {
    //log.debug("Convert hex to ip: $hex") 
    [convertHexToInt(hex[0..1]), convertHexToInt(hex[2..3]), convertHexToInt(hex[4..5]), convertHexToInt(hex[6..7])].join(".")
}
