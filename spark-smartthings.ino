typedef struct {
  char name[12]; //name of the Actuator
  int pin; // the pin on the Spark that the device is connected to
  int timeout; // 
  int type; // 1 = Motion, 2 = door/window contact, 3 = other
  int state; // the state the device is in
  volatile int timer; //timer for motions and debouncing things volatile since it could be used in an interrupt
} stActuator; // SmartThings Actuator device

typedef struct {
  char name[12]; //name of the Sensor - limit of 12 char for Spark Variable Names
  int pin; // the pin on the Spark that the device is connected to
  int polling; // how often to update the web variables in seconds
  int type; // 1 = Motion, 2 = door/window contact, 3 = other
  int data; // data to send to SmartThings
  int pollTimer; //timer for motions and debouncing things volatile since it could be used in an interrupt
} stSensor; //SmartThings Sensor device

// --------------------------------------------------------------------------------------
const int actuatorCount = 3; // ** change this to the number of actuators you have configured
//** change the following to suit what you have connected
//                      name     pin   timeout  type     state   timer
stActuator actuator0 {"Motion1",  D0,   120,      1,       0,      0}; 
stActuator actuator1 {"Contact1", D1,   5,        2,       0,      0}; 
stActuator actuator2 {"switch1",  D2,   1,        3,       0,      0};

// --------------------------------------------------------------------------------------
//Sensors
// change the following to suit what you have connected
const int sensorCount = 4; // ** change this to the number of sensors you have configured below
//                      name     pin   timeout  type     state   timer
stSensor sensor0 {"rssi", 100, 300, 1, 0};
stSensor sensor1 {"motion1", D0, 300, 1, 0};
stSensor sensor2 {"contact1", D1, 300, 1, 0};
stSensor sensor3 {"switch1", D2, 300, 1, 0};
// --------------------------------------------------------------------------------------

//Other stuff
const int loopDelay = 1000; //time to wait between loop() runs in ms (1000ms = 1 second)
const int led2 = D7; // LED to the side of the USB jack
stSensor sensors[sensorCount];
stActuator actuators[actuatorCount];

//Spark function def
int setOn(String command);
int setOff(String command);
int setValue(String command);
int setToggle(String command);

void setup() {
  actuators[0] = actuator0;
  actuators[1] = actuator1;
  actuators[2] = actuator2;
  
  pinMode(actuator0.pin, INPUT_PULLDOWN);
  attachInterrupt(actuator0.pin, D0_Inter, RISING);
  
  pinMode(actuator1.pin, INPUT_PULLDOWN);
  attachInterrupt(actuator1.pin, D1_Inter, RISING);
  
  pinMode(actuator2.pin, INPUT_PULLDOWN);
  attachInterrupt(actuator2.pin, D2_Inter, CHANGE);
  
  pinMode(led2, OUTPUT);
  
  // setup the spark variables for SmartThings to Poll
  Spark.variable(sensor0.name, &sensor0.data, INT);
  Spark.variable(sensor1.name, &sensor1.data, INT);
  
  // setup the spark functions for commands from SmartThings
  Spark.function("setOn", setOn);
  Spark.function("setOff", setOff);
  Spark.function("setValue", setValue);
  Spark.function("setToggle", setToggle);
  
  // catch any response from SmartThings for the webhook
  Spark.subscribe("hook-response/hook", gotResponse, MY_DEVICES);
  
  Serial.begin(9600);
}

void checkSensors() {
        if (actuator0.state) {
            actuator0.timer -= loopDelay;
        if (actuator0.timer < 0) {
            actuator0.state = 0;
            Spark.publish("hook", "{ \"pin\": \"D0\", \"state\": \"off\" }", 60, PRIVATE);
            //Spark.publish("device0_Off");
            //Spark.publish("motion1off", "0", 60, PRIVATE); IFTTT
            Serial.print("Motion Stopped on Device0_ Sensor");
            digitalWrite(led2, LOW);
        }
    }
    else {
        if (actuator0.timer > 0) {
            actuator0.state = 1;
            Spark.publish("hook", "{ \"pin\": \"D0\", \"state\": \"on\" }", 60, PRIVATE);
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
    if (sensor0.pollTimer) {
        sensor0.pollTimer -= loopDelay;  
    }
    else {
        sensor0.pollTimer = sensor0.polling * 1000;
        sensor0.data = WiFi.RSSI();
        Serial.print("RSSI: ");
        Serial.println(sensor0.data);
    }
}

void gotResponse(const char *name, const char *data) {
    Serial.print(data);
}

int setOn(String command) {
    
}

int setOff(String command) {
    
}
int setValue(String command) {
    
}
int setToggle(String command) {
    
}

int lightOn(String args) {
    digitalWrite(led2, HIGH);
    Serial.println("Switch On");
    return 1;
}

int lightOff(String args) {
    digitalWrite(led2, LOW);
    Serial.println("Switch Off");
    return 1;
}

// Interrupts
void D0_Inter()
{
   actuator0.timer = actuator0.timeout * 1000; 
}

void D1_Inter()
{
   actuator1.timer = actuator1.timeout * 1000; 
}
void D2_Inter()
{

}
