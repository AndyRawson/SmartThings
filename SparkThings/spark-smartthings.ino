typedef struct {
  String name; //name of the Actuator
  int pin; // the pin on the Spark that the device is connected to
  int timeout; // 
  int type; // 1 = Switch, 2 = Alarm, 3 = Dimmer, 4 = Other
  int state; // the state the device is in
  int value; //timer for motions and debouncing things volatile since it could be used in an interrupt
} stActuator; // SmartThings Actuator device

typedef struct {
  String name; //name of the Sensor - limit of 12 char for Spark Variable Names
  int pin; // the pin on the Spark that the device is connected to
  int polling; // how often to update the web variables in seconds
  int type; // 1 = Motion, 2 = door/window contact, 3 = rssi, 4 = other
  int data; // data to send to SmartThings
  volatile int timer; //timer for motions and debouncing things volatile since it could be used in an interrupt
} stSensor; //SmartThings Sensor device

// --------------------------------------------------------------------------------------
//** change the following to suit what you have connected
// PWM analogWrite() is available on pins A0, A1, A4, A5, A6, A7, D0 and D1
// Actuator Types 1 = Switch, 2 = Alarm, 3 = other
//                      name     pin   timeout  type     state   value
stActuator actuator[] = {};
//stActuator actuator0 {"switch1",  A0,   1,        3,       0,      0}; 
//stActuator actuator1 {"switch2",  D3,   1,        1,       0,      0}; 
//stActuator actuator2 {"switch3",  D2,   1,        1,       0,      0};
//stActuator actuator3 {"switch4",  D3,   1,        1,       0,      0};
//stActuator actuator4 {"switch5",  D4,   1,        1,       0,      0}; 
//stActuator actuator5 {"switch6",  D5,   1,        1,       0,      0}; 
//stActuator actuator6 {"switch7",  D6,   1,        1,       0,      0};
//stActuator actuator7 {"switch8",  D7,   1,        1,       0,      0};

// --------------------------------------------------------------------------------------
//Sensors
// sensors have a Spark variable that has the name provided below and are updated 
// according to the polling time in seconds. This variable can be read from SmartThings to 
// update things that are not time sensitive if they are set as Motion type then they will use
// an interrupt and the webhook to send triggers to SmartThings. The polling time will be used 
// as a way to avoid sending every motion triggered, the motion will stay active until the timeout
// counts down. The timer is reset everytime the motion triggers but it won't send SmartThings 
// a no motion trigger until the timer runs out after the last motion. 
// change the following to suit what you have connected and comment out the rest
// Sensor Types 1 = Motion, 2 = door/window contact, 3 = rssi, 4 = other
//                  name            pin   polling  type     data   timer
stSensor sensor[] = {
{"contact1",       D0,      3,       2,     0,      0},
{"contact2",       D1,      3,       2,     0,      0},
{"contact3",       D2,      3,       2,      0,      0},
{"contact4",       D3,      3,       2,      0,      0},
{"contact5",       D4,      3,       2,      0,      0},
{"HallMotion",     A0,      300,     1,      0,      0},
{"BasementMotion", A1,      300,     1,      0,      0},
{"contact6",       A3,      3,       2,      0,      0},
{"HallGlassBreak", A4,      300,     4,      0,      0},
//{"contact8",     A2,      300,     2,      0,      0},
{"rssi",        100,        300,     3,      0,      0},
};// rssi is the current WiFi signal strength for the Spark
// --------------------------------------------------------------------------------------

//Other stuff
const int loopDelay = 1000; //time to wait between loop() runs in ms (1000ms = 1 second)
const int led2 = D7; // LED to the side of the USB jack

String webhookName = "";

int setOn(String command);
int setOff(String command);
int setValue(String command);
int setToggle(String command);

void setup() {
  String devID = Spark.deviceID();
  webhookName = "dev" + devID.substring(18);

  
  pinMode(sensor[0].pin, INPUT_PULLUP);
  attachInterrupt(sensor[0].pin, D0_Inter, CHANGE);
  
  pinMode(sensor[1].pin, INPUT_PULLUP);
  attachInterrupt(sensor[1].pin, D1_Inter, CHANGE);
  
  pinMode(sensor[2].pin, INPUT_PULLUP);
  attachInterrupt(sensor[2].pin, D2_Inter, CHANGE);
  
  pinMode(sensor[3].pin, INPUT_PULLUP);
  attachInterrupt(sensor[3].pin, D3_Inter, CHANGE);
  
  pinMode(sensor[4].pin, INPUT_PULLUP);
  attachInterrupt(sensor[4].pin, D4_Inter, CHANGE);
  
  pinMode(sensor[5].pin, INPUT_PULLUP);
  attachInterrupt(sensor[5].pin, A0_Inter, CHANGE);
  
  pinMode(sensor[6].pin, INPUT_PULLUP);
  attachInterrupt(sensor[6].pin, A1_Inter, CHANGE);
  
  pinMode(sensor[7].pin, INPUT_PULLUP);
  attachInterrupt(sensor[7].pin, A3_Inter, CHANGE);
  
  pinMode(sensor[8].pin, INPUT_PULLUP);
  attachInterrupt(sensor[8].pin, A4_Inter, CHANGE);
  
  //pinMode(actuator0.pin, OUTPUT);
  
  pinMode(led2, OUTPUT);
  
  // setup the spark variables for SmartThings to Poll
  const char * s100Name = sensor[9].name.c_str();
  const char * s0Name = sensor[0].name.c_str();
  const char * s1Name = sensor[1].name.c_str();
  const char * s2Name = sensor[2].name.c_str();
  const char * s3Name = sensor[3].name.c_str();
  const char * s4Name = sensor[4].name.c_str();
  const char * s5Name = sensor[5].name.c_str();
  const char * s6Name = sensor[6].name.c_str();
  const char * s7Name = sensor[7].name.c_str();
  const char * s8Name = sensor[8].name.c_str();
  Spark.variable(s100Name, &sensor[9].data, INT);
  Spark.variable(s0Name, &sensor[0].data, INT);
  Spark.variable(s1Name, &sensor[1].data, INT);
  Spark.variable(s2Name, &sensor[2].data, INT);
  Spark.variable(s3Name, &sensor[3].data, INT);
  Spark.variable(s4Name, &sensor[4].data, INT);
  Spark.variable(s5Name, &sensor[5].data, INT);
  Spark.variable(s6Name, &sensor[6].data, INT);
  Spark.variable(s7Name, &sensor[7].data, INT);
  Spark.variable(s8Name, &sensor[8].data, INT);
  
  // setup the spark functions for commands from SmartThings
  Spark.function("setOn", setOn);
  Spark.function("setOff", setOff);
  Spark.function("setValue", setValue);
  Spark.function("setToggle", setToggle);
  
  // catch any response from SmartThings for the webhook
  String hookResponse = "hook-response/" + webhookName;
  Spark.subscribe(hookResponse, gotResponse, MY_DEVICES);
  
  Serial.begin(9600);
}

void checkSensors() {
    for (int i=0; i < arraySize(sensor); i++) {
        if (sensor[i].type != 3) {
        Serial.print(String(sensor[i].timer) + " : ");
            if (sensor[i].data) {
                sensor[i].timer -= loopDelay;
                if (sensor[i].timer < 0) {
                    sensor[i].data = 0;
                    Spark.publish(webhookName, "{ \"pin\": \"" + sensor[i].name + "\", \"data\": \"off\" }", 60, PRIVATE);
                    //Spark.publish("device0_Off");
                    //Spark.publish("motion1off", "0", 60, PRIVATE); IFTTT
                    Serial.print("Motion Stopped on " + sensor[i].name);
                    //digitalWrite(led2, LOW);
                }
            }
            else {
                if (sensor[i].timer > 0) {
                    sensor[i].data = 1;
                    Spark.publish(webhookName, "{ \"pin\": \"" + sensor[i].name + "\", \"data\": \"on\" }", 60, PRIVATE);
                    //Spark.publish("device0_On");
                    //Spark.publish("motion1on", "1", 60, PRIVATE); //IFTTT
                    Serial.print("Motion Detected on " + sensor[i].name);
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
      
  checkSensors(); //see if any sensor values need to be updated

  updateVariables(); // TODO move this to checkSensors as a Sensor type
  
  delay(loopDelay); // Wait for 1 second 
  

}

void updateVariables() {
    if (sensor[9].timer) {
        sensor[9].timer -= loopDelay;  
    }
    else {
        sensor[9].timer = sensor[9].polling * 1000;
        sensor[9].data = WiFi.RSSI();
        Serial.print("RSSI: ");
        Serial.println(sensor[9].data);
    }

}

void gotResponse(const char *name, const char *data) {
    Serial.print(data);
}

// setOn takes deviceName       // Example: switch1
int setOn(String command) {
    int r = 0;
    Serial.print("Function setOn: ");
    Serial.println(command);
    
    for (int i = 0; i < arraySize(actuator); i++) {
        if (actuator[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to On");
            actuator[i].state = 1;
            digitalWrite(actuator[i].pin, HIGH);
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
        if (actuator[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to Off");
            actuator[i].state = 0;
            digitalWrite(actuator[i].pin, LOW);
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
        if (actuator[i].name.equals(String(deviceToSet))) {
            Serial.print("Changing Device: ");
            Serial.print(deviceToSet);
            Serial.print(" Value: ");
            Serial.println(valueToSet);
            actuator[i].value = valueToSet.toInt();
            actuator[i].state = 1;
            analogWrite(actuator[i].pin, actuator[i].value);
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
        if (actuator[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            if (actuator[i].state) {
                Serial.println(" to Off");
                actuator[i].state = 0;
                digitalWrite(actuator[i].pin, LOW);
            }
            else {
                Serial.println(" to On");
                actuator[i].state = 1;
                digitalWrite(actuator[i].pin, HIGH);
            }

            r = 1;
        }
    }
    return r;       
    
}


// Interrupts
void D0_Inter()
{
   sensor[0].timer = sensor[0].polling * 1000; 
}

void D1_Inter()
{
   sensor[1].timer = sensor[1].polling * 1000; 
}
void D2_Inter()
{
   sensor[2].timer = sensor[2].polling * 1000; 
}
void D3_Inter()
{
   sensor[3].timer = sensor[3].polling * 1000; 
}
void D4_Inter()
{
   sensor[4].timer = sensor[4].polling * 1000; 
}
void A0_Inter()
{
   sensor[5].timer = sensor[5].polling * 1000; 
}
void A1_Inter()
{
   sensor[6].timer = sensor[6].polling * 1000; 
}
void A3_Inter()
{
   sensor[7].timer = sensor[7].polling * 1000; 
}
void A4_Inter()
{
   sensor[8].timer = sensor[8].polling * 1000; 
}
