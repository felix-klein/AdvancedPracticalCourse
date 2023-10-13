// ---> include libraries
#include <SingleLinkedList.hpp> /*Include the library for the creation of List objects */
#include "TLE9879_Group.h" /* Include the Infineon shields library to the Arduino project */

// ---> defines and constants
TLE9879_Group *shields; /* Declare shields group object */


/*
Normal example Input: <*STI:999#*TMD:4#><*RPM:4000#*TMD:2#><*RPM:100#*TMD:3#><*RPM:4000#*TMD:5#><*EST:0#*TMD:8#>?
Min Input: <*EST:1#> (9 for starting the engine as an example)
*/

// ---> global variables
bool constructionsEnd = false;
bool sensorsEnd = false;
SingleLinkedList<byte> missionNames;
SingleLinkedList<short> missionParams;
static long timeDelay;
bool runMission = false;
static long missionStamp;
static byte counter = 0;
static int sensorInterval = 499;

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
      } else if (missionNames.getValue(i) == EST) { /* --> Engine-Status-Type */
          creatingMIS = creatingMIS + String("est=");
          if (missionParams.getValue(i) == 0) {
            shields->setMotorMode(STOP_MOTOR);
          } else {
            shields->setMotorMode(START_MOTOR);
          }
      } else if (missionNames.getValue(i) == RPM) { /* --> Rotations-per-Minute */
          creatingMIS = creatingMIS + String("rpm=");
          shields->setMotorSpeed(missionParams.getValue(i));
          shields->setMotorMode(START_MOTOR);
      } else if (missionNames.getValue(i) == HDA) { /* --> Hall-Delay-Angle  */
          creatingMIS = creatingMIS + String("hda=");
          shields->setParameter(HALL_DELAY_ANGLE, missionParams.getValue(i));
      } else if (missionNames.getValue(i) == STI) { /* --> Sensor-Time-Interval  */
          creatingMIS = creatingMIS + String("sti=");
      }
      creatingMIS = creatingMIS + missionParams.getValue(i) + String("&");
  }
    Serial.println(creatingMIS + String("tsp=") + millis());
}

// ---> setup()
void setup() {
  // --> Serial communication initialization
  Serial.begin(9600); // Sets the data rate in bits per second (baud) for serial data transmission.
  // --> Infineon shields initialization
  //shields->setParameter(HALL_INPUT_A, 0); /* For the case, that the contacts on the HALL input are wrong */
  shields = new TLE9879_Group(1); // Initialize the shields group with the one shieldss in the stack
  shields->setMode(HALL); // Set the mode to HALL
  shields->setParameter(HALL_POLE_PAIRS, 4); // Set number of pole pares to 4, for 8 poles
  shields->setParameter(HALL_ANGLE_DELAY_EN, 1); // Set the possibility of changing the timing degree

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
          String param = Serial.readStringUntil('#');
          if (name.equals(String("TMD"))) {
            missionNames.add(1);
            timeDelay = param.toInt() * 1000;
          } else if (name.equals(String("EST"))) {
            missionNames.add(2);
          } else if (name.equals(String("RPM"))) {
            missionNames.add(3);
          } else if (name.equals(String("HDA"))) {
            missionNames.add(4);
          } else if (name.equals(String("STI"))) {
            missionNames.add(5);
            sensorInterval = param.toInt();
          }
          missionParams.add(param.toInt());
        } else if (controller == '>') {
          applyMission();
          missionStamp = millis();
          missionNames.clear();
          missionParams.clear();
          runMission = true;
          Serial.println(timeDelay);
        } else if (controller == '?') {
          constructionsEnd = true;
          shields->setMotorMode(STOP_MOTOR);
        }
      }
    } else {
      static long timeSinceMissionStart = millis() - missionStamp;
      if (timeSinceMissionStart >= timeDelay) {
        runMission = false;
        missionStamp = 0;
        }
    }
  }

// OUTPUT: Sensor data gathering, in parallel.
    static unsigned long sensorStamp = 0; // Static, because it should stay throughout the loop / unsigned long, because if it is to big it should restart at 0.
    if ((millis() - sensorStamp > sensorInterval) && (sensorsEnd == false)) {
      sensorStamp = millis();

      int readingMIC = analogRead(0);

      int readingTMP = analogRead(1);
      float temp = readingTMP * 0.0048828125 * 100;

      int readVIB = analogRead(2);
      float vib = readVIB * (5.0 / 1023.0);

      int readingCP1 = analogRead(3);
      float volt1 = (readingCP1 / 1024.0) * 5000;

      int readingCP2 = analogRead(4);
      float volt2 = (readingCP2 / 1024.0) * 5000;

      int readingCP3 = analogRead(5);
      float volt3 = (readingCP3 / 1024.0) * 5000;

      if (constructionsEnd == true) { // Signal the end of the last sensor data with a ?.
        // Example output at the end: "TSP:67990#TMP:21.97#VIB:0.00#MIC:238#CP1:170.90#CP2:146.48#CP3:170.90#?".
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp + String("#VIB:") + vib + String("#MIC:") + readingMIC + String("#CP1:") + volt1 + String("#CP2:") + volt2 + String("#CP3:") + volt3 + String("#?"));
        sensorsEnd = true;
      } else {
        //Example output: "TSP:48110#TMP:21.97#VIB:0.00#MIC:26#CP1:156.25#CP2:146.48#CP3:166.02".
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp + String("#VIB:") + vib + String("#MIC:") + readingMIC + String("#CP1:") + volt1 + String("#CP2:") + volt2 + String("#CP3:") + volt3);
      }
    }
 delay(10);
}
