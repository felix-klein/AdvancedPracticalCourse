// ---> include libraries
#include <SingleLinkedList.hpp> /*Include the library for the creation of List objects */
#include "TLE9879_Group.h" /* Include the Infineon Shield library to the Arduino project */

// ---> defines and constants
TLE9879_Group *shield; /* Declare Shield group object */

/*
Normal example Input: <*STI:999#><*ALB:1#*TMD:5000#> <*ALB:0#*TMD:1000#><*ALB:0#*TMD:4500#>?
Min Input: <*EST:1#> (9 for starting the engine as an example)
*/

// ---> global variables
bool constructionsEnd = false;
bool sensorsEnd = false;
SingleLinkedList<byte> missionNames;
SingleLinkedList<short> missionParams;
short timeDelay = 100;
bool runMission = false;
static long missionStamp;
static byte counter = 0;
short sensorInterval = 499;

const byte TMD = 1; //Time-Duration         (0-int)
const byte EST = 2; //Engine-Status-Type    (0/1)
const byte RPM = 3; //Rotations-per-Minute  (0-4000)
const byte HDA = 4; //Hall-Delay-Angle      (1-59)
const byte STI = 5; //Sensor-Time-Interval  (->sensorInterval)

// ---> user defined functions
void applyMission() { /* Helper function to apply the gathered mission. */
    counter += 1;
    String creatingMIS = String("MIS:");

  for (int i=0; i<missionNames.getSize(); i++) {
      if (missionNames.getValue(i) == TMD) { /* --> Time-Duration */
          creatingMIS = creatingMIS + String("tmd=");
          timeDelay = missionParams.getValue(i) * 1000;
      } else if (missionNames.getValue(i) == EST) { /* --> Engine-Status-Type */
          creatingMIS = creatingMIS + String("est=");
          if (missionParams.getValue(i) == 0) {
              //shield->setMotorMode(STOP_MOTOR);
              digitalWrite(13, HIGH); // TODO: Testing
          } else {
              //shield->setMotorMode(START_MOTOR);
              digitalWrite(13, LOW); // TODO: Testing
          }
      } else if (missionNames.getValue(i) == RPM) { /* --> Rotations-per-Minute */
          creatingMIS = creatingMIS + String("rpm=");
          // TODO: Testing
          if (missionParams.getValue(i) <= 2000) {
          digitalWrite(13, LOW); // TODO: Testing
          } else {
          digitalWrite(13, LOW); // TODO: Testing
          }
          //shield->setMotorSpeed(missionParams.getValue(i));
      } else if (missionNames.getValue(i) == HDA) { /* --> Hall-Delay-Angle  */
          creatingMIS = creatingMIS + String("hda=");
          shield->setParameter(HALL_DELAY_ANGLE, missionParams.getValue(i));
      } else if (missionNames.getValue(i) == STI) { /* --> Sensor-Time-Interval  */
          creatingMIS = creatingMIS + String("sti=");
          sensorInterval = missionParams.getValue(i);
      }
      creatingMIS = creatingMIS + missionParams.getValue(i) + String("&");
  }
    Serial.println(creatingMIS + String("tsp=") + millis());
}

// ---> setup()
void setup() {
  // --> Infineon shield initialization
  //shield->setParameter(HALL_INPUT_A, 0); /* For the case, that the contacts on the HALL input are wrong */
  /*
  shield = new TLE9879_Group(1); // Initialize the shield group with the one shields in the stack
  shield->setMode(HALL); // Set the mode to HALL
  shield->setParameter(HALL_POLE_PAIRS, 4); // Set number of pole pares to 4, for 8 poles
  shield->setParameter(HALL_ANGLE_DELAY_EN, 1); // Set the possibility of changing the timing degree
  */

  // --> Serial communication initialization
  Serial.begin(115200); // Sets the data rate in bits per second (baud) for serial data transmission.
  while (Serial.available() < 9) {}; // Get the number of bytes (characters) available for reading from the serial port.

  pinMode(13, OUTPUT);
}

// ---> loop()
void loop() {
  if (constructionsEnd != true) { // If there are still constructions to fulfill.
  // INPUT: Constructions initializing.
    if (runMission != true) { // If there is a Mission running, with reference to the time, new missions shouldn't be read.
      if (Serial.available() >= 1) { // Safety measure, for the case that the serial communication is slower than the setup() function.
        char controller = Serial.read();
        if (controller == '<') {
        } else if (controller == '*') {
          String name = Serial.readStringUntil(':');
          if (name.equals(String("TMD"))) {
            missionNames.add(1);
          } else if (name.equals(String("EST"))) {
            missionNames.add(2);
          } else if (name.equals(String("RPM"))) {
            missionNames.add(3);
          } else if (name.equals(String("HDA"))) {
            missionNames.add(4);
          } else if (name.equals(String("STI"))) {
            missionNames.add(5);
          }
          String param = Serial.readStringUntil('#');
          missionParams.add(param.toInt());
        } else if (controller == '>') {
          applyMission();
          missionStamp = millis();
          missionNames.clear();
          missionParams.clear();
          runMission = true;
        } else if (controller == '?') {
          constructionsEnd = true;
        }
      }
    } else {
      long timeSinceMissionStart = millis() - missionStamp;
      if (timeSinceMissionStart >= timeDelay) {
        runMission = false;
        missionStamp = 0;
        }
    }
  }

//TODO: All sensors need to be added.
// OUTPUT: Sensor data gathering, in parallel.
    static unsigned long sensorStamp = 0; // Static, because it should stay throughout the loop / unsigned long, because if it is to big it should restart at 0.
    if ((millis() - sensorStamp > sensorInterval) && (sensorsEnd == false)) {
      sensorStamp = millis();

      int readingTMP = analogRead(0);
      float temp = readingTMP * 0.0048828125 * 100;

      int readVIB = analogRead(1);
      float vib = readVIB * (5.0 / 1023.0);

      int readingMIC = analogRead(2);

      int readingCP1 = analogRead(3);
      float volt1 = (readingCP1 / 1024.0) * 5000;

      int readingCP2 = analogRead(4);
      float volt2 = (readingCP2 / 1024.0) * 5000;

      int readingCP3 = analogRead(5);
      float volt3 = (readingCP3 / 1024.0) * 5000;

      if (constructionsEnd == true) { // Signal the end of the last sensor data with a ?.
        // Example output at the end: "TSP:65400027.34#TMP:27.34#?".
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp + String("#VIB:") + vib + String("#MIC:") + readingMIC + String("#CP1:") + volt1 + String("#CP2:") + volt2 + String("#CP3:") + volt3 + String("#?"));
        sensorsEnd = true;
      } else {
        //Example output: "TSP:65400027.34#TMP:27.34".
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp + String("#VIB:") + vib + String("#MIC:") + readingMIC + String("#CP1:") + volt1 + String("#CP2:") + volt2 + String("#CP3:") + volt3);
      }
    }
 delay(10);
}
