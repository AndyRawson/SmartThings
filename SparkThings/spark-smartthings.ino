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
const int actuatorCount =2; // ** change this to the number of actuators you have configured
//** change the following to suit what you have connected
// PWM analogWrite() is available on pins A0, A1, A4, A5, A6, A7, D0 and D1
// Actuator Types 1 = Switch, 2 = Alarm, 3 = other
//                      name     pin   timeout  type     state   value
stActuator actuator0 {"switch1",  A0,   1,        3,       0,      0}; 
stActuator actuator1 {"switch2",  D3,   1,        1,       0,      0}; 
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
const int sensorCount = 3; // ** change this to the number of sensors you have configured below
//                  name        pin   polling  type     data   timer
stSensor sensor0 {"rssi",       100,    300,    3,      0,      0}; // rssi is the current WiFi signal strength for the Spark
stSensor sensor1 {"motion1",    D0,      60,    1,      0,      0};
stSensor sensor2 {"contact1",   D1,     2,      2,      0,      0};
//stSensor sensor3 {"contact2",   D2,     300,    2,      0,      0};
//stSensor sensor4 {"contact3",   D3,     300,    2,      0,      0};
//stSensor sensor5 {"contact4",   D4,     300,    2,      0,      0};
//stSensor sensor6 {"contact5",   D5,     300,    2,      0,      0};
//stSensor sensor7 {"contact6",   D6,     300,    2,      0,      0};
//stSensor sensor8 {"contact7",   A1,     300,    2,      0,      0};
//stSensor sensor9 {"contact8",   A2,     300,    2,      0,      0};
// --------------------------------------------------------------------------------------

//Other stuff
const int loopDelay = 1000; //time to wait between loop() runs in ms (1000ms = 1 second)
const int led2 = D7; // LED to the side of the USB jack
stSensor sensors[sensorCount];
stActuator actuators[actuatorCount];

int setOn(String command);
int setOff(String command);
int setValue(String command);
int setToggle(String command);

void setup() {
  actuators[0] = actuator0;
  actuators[1] = actuator1;
  
  sensors[0] = sensor0;
  sensors[1] = sensor0;
  sensors[2] = sensor0;
  
  pinMode(sensor1.pin, INPUT_PULLDOWN);
  attachInterrupt(sensor1.pin, D0_Inter, RISING);
  
  pinMode(sensor2.pin, INPUT_PULLDOWN);
  attachInterrupt(sensor2.pin, D1_Inter, RISING);
  
  pinMode(actuator0.pin, OUTPUT);
  
  pinMode(led2, OUTPUT);
  
  // setup the spark variables for SmartThings to Poll
  const char * s0Name = sensor0.name.c_str();
  const char * s1Name = sensor1.name.c_str();
  const char * s2Name = sensor2.name.c_str();
  Spark.variable(s0Name, &sensor0.data, INT);
  Spark.variable(s1Name, &sensor1.data, INT);
  Spark.variable(s2Name, &sensor2.data, INT);
  
  // setup the spark functions for commands from SmartThings
  Spark.function("setOn", setOn);
  Spark.function("setOff", setOff);
  Spark.function("setValue", setValue);
  Spark.function("setToggle", setToggle);
  
  // catch any response from SmartThings for the webhook
  Spark.subscribe("hook-response/stdatahook", gotResponse, MY_DEVICES);
  
  Serial.begin(9600);
}

void checkSensors() {
        if (sensor1.data) {
            sensor1.timer -= loopDelay;
        if (sensor1.timer < 0) {
            sensor1.data = 0;
            Spark.publish("stdatahook", "{ \"pin\": \"D0\", \"data\": \"off\" }", 60, PRIVATE);
            //Spark.publish("device0_Off");
            //Spark.publish("motion1off", "0", 60, PRIVATE); IFTTT
            Serial.print("Motion Stopped on Device0_ Sensor");
            digitalWrite(led2, LOW);
        }
    }
    else {
        if (sensor1.timer > 0) {
            sensor1.data = 1;
            Spark.publish("stdatahook", "{ \"pin\": \"D0\", \"data\": \"on\" }", 60, PRIVATE);
            //Spark.publish("device0_On");
            //Spark.publish("motion1on", "1", 60, PRIVATE); //IFTTT
            Serial.print("Motion Detected on Device0_ Sensor");
            digitalWrite(led2, HIGH);
            
        }
    }
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
    if (sensor0.timer) {
        sensor0.timer -= loopDelay;  
    }
    else {
        sensor0.timer = sensor0.polling * 1000;
        sensor0.data = WiFi.RSSI();
        Serial.print("RSSI: ");
        Serial.println(sensor0.data);
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
    
    for (int i = 0; i < arraySize(actuators); i++) {
        if (actuators[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to On");
            actuators[i].state = 1;
            digitalWrite(actuators[i].pin, HIGH);
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
    
    for (int i = 0; i < arraySize(actuators); i++) {
        if (actuators[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            Serial.println(" to Off");
            actuators[i].state = 0;
            digitalWrite(actuators[i].pin, LOW);
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
    
    for (int i = 0; i < arraySize(actuators); i++) {
        if (actuators[i].name.equals(String(deviceToSet))) {
            Serial.print("Changing Device: ");
            Serial.print(deviceToSet);
            Serial.print(" Value: ");
            Serial.println(valueToSet);
            actuators[i].value = valueToSet.toInt();
            actuators[i].state = 1;
            analogWrite(actuators[i].pin, actuators[i].value);
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
    
    for (int i = 0; i < arraySize(actuators); i++) {
        if (actuators[i].name.equals(command)) {
            Serial.print("Changing Device: ");
            Serial.print(command);
            if (actuators[i].state) {
                Serial.println(" to Off");
                actuators[i].state = 0;
                digitalWrite(actuators[i].pin, LOW);
            }
            else {
                Serial.println(" to On");
                actuators[i].state = 1;
                digitalWrite(actuators[i].pin, HIGH);
            }

            r = 1;
        }
    }
    return r;       
    
}


// Interrupts
void D0_Inter()
{
   sensor1.timer = sensor1.polling * 1000; 
}

void D1_Inter()
{
   sensor2.timer = sensor2.polling * 1000; 
}
void D2_Inter()
{

}
