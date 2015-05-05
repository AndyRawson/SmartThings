typedef struct {
  String name; // name of the device - limit of 12 char for Spark Variable Names
  int pin; // the pin on the Spark that the device is connected to
  int polling; // how often to update the web variables in seconds 
  int type; // 0 = disabled, 1 = Motion, 2 = door/window contact, 4 = other
  int data; // data to send to SmartThings
  volatile int timer; // timer for motions and debouncing things volatile since it could be used in an interrupt
  int isInterrupt; // Using Interrupts for instant updates 0 for polling
  int pinType; // is the pin an input (sensor) or an output (actuator)
} stDevice; // SmartThings device

// name pin polling type data timer isInterrupt
stDevice device[] = {
{"D0", D0, 300, 0, 0, 0, 1, 0},   //Interrupt
{"D1", D1, 300, 0, 0, 0, 1, 0},   //Interrupt
{"D2", D2, 300, 0, 0, 0, 1, 0},   //Interrupt
{"D3", D3, 300, 0, 0, 0, 1, 0},   //Interrupt
{"D4", D4, 300, 0, 0, 0, 1, 0},   //Interrupt
{"D5", D5, 300, 0, 0, 0, 0, 0},
{"D6", D6, 300, 0, 0, 0, 0, 0},
{"D7", D7, 300, 0, 0, 0, 0, 0},
{"A0", A0, 300, 0, 0, 0, 1, 0},   //Interrupt
{"A1", A1, 300, 0, 0, 0, 1, 0},   //Interrupt
{"A2", A2, 300, 0, 0, 0, 0, 0},
{"A3", A3, 300, 0, 0, 0, 1, 0},   //Interrupt
{"A4", A4, 300, 0, 0, 0, 1, 0},   //Interrupt
{"A5", A5, 300, 0, 0, 0, 0, 0},
{"A6", A6, 300, 0, 0, 0, 0, 0},
{"A7", A7, 300, 0, 0, 0, 0, 0},
};

const int loopDelay = 1000; //time to wait between loop() runs in ms (1000ms = 1 second)
int configured = 0;
int configDataAvailable = 0;
unsigned int configArray[48];

int rssiData = -200;
int rssiTimer = 0;
const int rssiUpdate = 300; //how often to update the rssi value in seconds

String webhookName = "";

int setOn(String command);
int setOff(String command);
int setValue(String command);
int setToggle(String command);

void setup() {
  String devID = Spark.deviceID();
  webhookName = "dev" + devID.substring(18);
  
  // catch any response from SmartThings for the webhook
  String hookResponse = "hook-response/" + webhookName;
  Spark.subscribe(hookResponse, gotResponse, MY_DEVICES);
  
  // tell SparkThings we need the Config Data
  getConfigData();
  
  Spark.variable("rssi", &rssiData, INT);
  
  // setup the spark functions for commands from SmartThings
  Spark.function("setOn", setOn);
  Spark.function("setOff", setOff);
  Spark.function("setValue", setValue);
  Spark.function("setToggle", setToggle);
 
  Serial.begin(9600);
}

void getConfigData() {
    Spark.publish(webhookName, "{ \"pin\": \"config\", \"data\": \"on\" }", 60, PRIVATE);
}

void gotResponse(const char *name, const char *data) {
    Serial.println(data);

  char* myCopy = strtok(strdup(data+16), ",");
  for (int i = 0; i < 48; i++)
  {
    configArray[i]= atoi(myCopy);
    Serial.println(configArray[i]);
    myCopy = strtok(NULL, ","); 
  }  
  configDataAvailable = 1;
    
}

void deviceSetup() {
    if (configDataAvailable) {
        Serial.println("Config Data received");
        for (int i=0; i < 16; i++){
            device[i].polling = configArray[i * 3];
            device[i].type = configArray[(i * 3) + 1];
            device[i].isInterrupt = configArray[(i * 3) + 2];
        }
        configured = 1;
        pinSetup();
    }
    else {
        Serial.println("No config data yet");
    }
}

void pinSetup() {
    for (int i=0; i < 16; i++){
        if (device[i].type) {
            pinMode(device[i].pin, getPinType(device[i].pinType));
            if (device[i].isInterrupt) {
                setInterrupt(i);
            }
            if (device[i].pinType != 3 && !device[i].isInterrupt) {
                const char * sName = device[i].name.c_str();
                Spark.variable(sName, &device[i].data, INT);
            }
        }
    }    
}

PinMode getPinType(int pinType) {
    switch (pinType) {
        case 0:
            return INPUT_PULLUP;
            break;
        case 1:
            return INPUT_PULLDOWN;
            break;
        case 2:
            return INPUT;
            break;
        case 3:
            return OUTPUT;
            break;
    }
}

void setInterrupt(int i) {
    switch (i) {
        case 0:
            attachInterrupt(D0, D0_Inter, CHANGE);
            break;
        case 1:
            attachInterrupt(D1, D1_Inter, CHANGE);
            break;
        case 2:
            attachInterrupt(D2, D2_Inter, CHANGE);
            break;
        case 3:
            attachInterrupt(D3, D3_Inter, CHANGE);
            break;
        case 4:
            attachInterrupt(D4, D4_Inter, CHANGE);
            break;
        case 8:
            attachInterrupt(A0, A0_Inter, CHANGE);
            break;
        case 9:
            attachInterrupt(A1, A1_Inter, CHANGE);
            break;
        case 11:
            attachInterrupt(A3, A3_Inter, CHANGE);
            break;
        case 12:
            attachInterrupt(A4, A4_Inter, CHANGE);
            break;
        case 13:
            attachInterrupt(A5, A5_Inter, CHANGE);
            break;
        case 14:
            attachInterrupt(A6, A6_Inter, CHANGE);
            break;
        case 15:
            attachInterrupt(A7, A7_Inter, CHANGE);
            break;
    }
}

void checkSensors() {
    for (int i=0; i < arraySize(sensor); i++) {
        if (device[i].pinType != 3) {
        Serial.print(String(device[i].timer) + " : ");
            if (device[i].data) {
                device[i].timer -= loopDelay;
                if (device[i].timer < 0) {
                    device[i].data = 0;
                    Spark.publish(webhookName, "{ \"pin\": \"" + device[i].name + "\", \"data\": \"off\" }", 60, PRIVATE);
                    //Spark.publish("device0_Off");
                    //Spark.publish("motion1off", "0", 60, PRIVATE); IFTTT
                    Serial.print("Motion Stopped on " + device[i].name);
                    //digitalWrite(led2, LOW);
                }
            }
            else {
                if (device[i].timer > 0) {
                    device[i].data = 1;
                    Spark.publish(webhookName, "{ \"pin\": \"" + device[i].name + "\", \"data\": \"on\" }", 60, PRIVATE);
                    //Spark.publish("device0_On");
                    //Spark.publish("motion1on", "1", 60, PRIVATE); //IFTTT
                    Serial.print("Motion Detected on " + device[i].name);
                    //digitalWrite(led2, HIGH);
            
                }
            }
        }
    }
    Serial.println("");
}


// This routine gets called repeatedly, like once every 5-15 milliseconds.
// Spark firmware interleaves background CPU activity associated with WiFi + Cloud activity with your code.
// Make sure none of your code delays or blocks for too long (like more than 5 seconds), or weird things can happen.
void loop() {
    
    if (!configured) {
      deviceSetup();
    }
    else {  
        checkSensors(); //see if any sensor values need to be updated
        updateRssi(); 
    }
  
  delay(loopDelay); // Wait for loopDelay ms 
  

}

void updateRssi() {
    if (rssiTimer > 0) {
        rssiTimer -= loopDelay;  
    }
    else {
        rssiTimer = rssiUpdate * 1000;
        rssiData = WiFi.RSSI();
        Serial.print("RSSI: ");
        Serial.println(rssiData);
    }

}

// setOn takes deviceName       // Example: switch1
int setOn(String command) {
    int r = 0;
    Serial.print("Function setOn: ");
    Serial.println(command);
    
    for (int i = 0; i < arraySize(actuator); i++) {
        if (device[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to On");
            device[i].data = 1;
            digitalWrite(device[i].pin, HIGH);
            r = 1;
        }
    }
    return r;   
}

// setOff takes deviceName       // Example: switch1
int setOff(String command) {
    int r = 0;
    Serial.print("Function setOff: ");
    Serial.println(command);
    
    for (int i = 0; i < arraySize(actuator); i++) {
        if (device[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to Off");
            device[i].data = 0;
            digitalWrite(device[i].pin, LOW);
            r = 1;
        }
    }
    return r;   
}

// setValue takes deviceName:value   // Example: switch1:10
int setValue(String command) {
    int r = 0;
    char copyStr[64];
    command.toCharArray(copyStr,64);
    char *p = strtok(copyStr, ":");
    
    char *deviceToSet = p; //(uint8_t)atoi(p);
    p = strtok(NULL,":");
    String valueToSet = p; //(uint8_t)atoi(p);
    
    Serial.println(command);
    
    for (int i = 0; i < arraySize(actuator); i++) {
        if (device[i].name.equals(String(deviceToSet))) {
            Serial.print("Changing Device: ");
            Serial.print(deviceToSet);
            Serial.print(" Value: ");
            Serial.println(valueToSet);
            device[i].data = valueToSet.toInt();
            device[i].data = 1;
            analogWrite(device[i].pin, device[i].data);
            r = 1;
        }
    }
    return r;
}

// setToggle takes deviceName       // Example: switch1
int setToggle(String command) {
    int r = 0;
    Serial.print("Function setToggle: ");
    Serial.println(command);
    
    for (int i = 0; i < arraySize(actuator); i++) {
        if (device[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            if (device[i].data) {
                Serial.println(" to Off");
                device[i].data = 0;
                digitalWrite(device[i].pin, LOW);
            }
            else {
                Serial.println(" to On");
                device[i].data = 1;
                digitalWrite(device[i].pin, HIGH);
            }

            r = 1;
        }
    }
    return r;       
    
}


// Interrupts
void D0_Inter()
{
   device[0].timer = device[0].polling * 1000; 
}

void D1_Inter()
{
   device[1].timer = device[1].polling * 1000; 
}
void D2_Inter()
{
   device[2].timer = device[2].polling * 1000; 
}
void D3_Inter()
{
   device[3].timer = device[3].polling * 1000; 
}
void D4_Inter()
{
   device[4].timer = device[4].polling * 1000; 
}
void A0_Inter()
{
   device[8].timer = device[8].polling * 1000; 
}
void A1_Inter()
{
   device[9].timer = device[9].polling * 1000; 
}
void A3_Inter()
{
   device[11].timer = device[11].polling * 1000; 
}
void A4_Inter()
{
   device[12].timer = device[12].polling * 1000; 
}
void A5_Inter()
{
   device[13].timer = device[13].polling * 1000; 
}
void A6_Inter()
{
   device[14].timer = device[14].polling * 1000; 
}
void A7_Inter()
{
   device[15].timer = device[15].polling * 1000; 
}