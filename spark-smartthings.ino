const int loopDelay = 1000; //time to wait between loop() runs in ms (1000ms = 1 second)

const String device0_Name = "Device 0";
const int device0_Pin = D0; // device0_ device pin 
const int device0_Timeout = 120; // device1_ device timeout in seconds
const int device0_DeviceType = 1; // 1 = Motion, 2 = door/window contact, 3 = other


const int device1_Pin = D1;
const int device1_Timeout = 120;
const int device1_DeviceType = 1;
  

const int device2_Pin = D2;
const int device2_Timeout = 1;
const int device2_DeviceType = 1;

int stVar1 = 0;
int stVar2 = 0;
int sparkVarTimeout = 300; // how often to update the web variables in seconds
int sparkVarTimer = 0;

const int led2 = D7; // LED to the side of the USB jack

typedef struct {
  String name;
  int pin;
  int timeout; 
  int type;
  int state;
  volatile int timer;
  int pinstate;
} digitalDevice;



int device0_On = 0; // Initial state for device0
volatile int device0_Timer = 0;  // timer for device0
void device0_Inter(void);  //Interrupt for device0

int device1_On = 0;
volatile int device1_Timer = 0;
void device1_Inter(void);

int device2_On = 0;
volatile int device2_Timer = 0;
void device2_Inter(void);


void setup() {

  pinMode(device0_Pin, INPUT_PULLDOWN);
  attachInterrupt(device0_Pin, device0_Inter, RISING);
  
  pinMode(device1_Pin, INPUT_PULLDOWN);
  attachInterrupt(device1_Pin, device1_Inter, RISING);
  
  pinMode(device2_Pin, INPUT_PULLDOWN);
  attachInterrupt(device2_Pin, device2_Inter, CHANGE);
  
  pinMode(led2, OUTPUT);
  
  Spark.variable("stVar1", &stVar1, INT);
  Spark.variable("stVar2", &stVar2, INT);
  
  Spark.subscribe("hook-response/ST_device", gotResponse, MY_DEVICES);
  
  Serial.begin(9600);
}

// This routine gets called repeatedly, like once every 5-15 milliseconds.
// Spark firmware interleaves background CPU activity associated with WiFi + Cloud activity with your code.
// Make sure none of your code delays or blocks for too long (like more than 5 seconds), or weird things can happen.
void loop() {
  String myTime = Time.timeStr();


if (device0_On) {
    device0_Timer = device0_Timer - loopDelay;
    if (device0_Timer < 0) {
        device0_On = 0;
        Spark.publish("ST_device");
        //Spark.publish("device0_Off");
        //Spark.publish("motion1off", "0", 60, PRIVATE); IFTTT
        Serial.print("Motion Stopped on Device0_ Sensor" + myTime);
        digitalWrite(led2, LOW);
    }
}
else {
    if (device0_Timer > 0) {
        device0_On = 1;
        Spark.publish("ST_device");
        //Spark.publish("device0_On");
        //Spark.publish("motion1on", "1", 60, PRIVATE); //IFTTT
        Serial.print("Motion Detected on Device0_ Sensor" + myTime);
        digitalWrite(led2, HIGH);
        
    }
}

  //Serial.println("OFF");
  //Serial.print("RSSI at ");
  //Serial.print(myTime);
  //Serial.print(" is: ");
  //Serial.println(WiFi.RSSI());
  updateVariables();
  delay(loopDelay);               // Wait for 1 second 

}


void updateVariables() {
    if (sparkVarTimer) {
        sparkVarTimer -= loopDelay;  
    }
    else {
        sparkVarTimer = sparkVarTimeout * 1000;
        stVar1 = WiFi.RSSI();
        Serial.print("RSSI: ");
        Serial.println(stVar1);
    }
}

void gotResponse(const char *name, const char *data) {
    Serial.print(data);
}

// Interrupts

void device2_Inter()
{
  if (digitalRead(device2_Pin)) {
      Serial.print("Switch On");
      digitalWrite(led2, HIGH);
  }
  else
  {
      Serial.print("Switch Off");
      digitalWrite(led2, LOW);
  }
}

void device0_Inter()
{
   device0_Timer = device0_Timeout * 1000; 
}

void device1_Inter()
{
   device1_Timer = device1_Timeout * 1000; 
}
