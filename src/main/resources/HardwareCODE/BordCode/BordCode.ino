// ---> include libraries
#include <SingleLinkedList.hpp> /*Include the library for the creation of List objects */
#include "TLE9879_Group.h" /* Include the Infineon Shield library to the Arduino project */

// ---> defines and constants
TLE9879_Group *shield; /* Declare Shield group object */

/*
Normal example Input: <*STI:999#><*LED:1#*TMD:5000#> <*LED:0#*TMD:1000#> <*LED:1#*TMD:4500#><*EST:0#>?
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
byte engineGear = 0;

const byte EGS = 1; //Engine-Gear-Shift     (0-6  [-1: down-shift, -99: up-shift])
const byte TMD = 2; //Time-Duration         (0-int)
const byte EST = 3; //Engine-Status-Type    (0/1)
const byte RPM = 4; //Rotations-per-Minute  (0-4000)
const byte LED = 5; //LED-Light             (0/1)
const byte HDA = 6; //Hall-Delay-Angle      (0-59)
const byte STI = 7; //Sensor-Time-Interval  (->sensorInterval)

// ---> user defined functions
void applyMission() { /* Helper function to apply the gathered mission. */
    counter += 1;
    String creatingMIS = String("MIS:");

  for (int i=0; i<missionNames.getSize(); i++) {
      if(missionNames.getValue(i) == EGS) { /* --> Engine-Gear-Shift */
          creatingMIS = creatingMIS + String("egs=");
          /* Define the gear. */
          if (missionParams.getValue(i) == -1 && engineGear > 0) { /* down-shift */
              engineGear = engineGear - 1;
          } else if (missionParams.getValue(i) == -99 && engineGear < 6) { /* up-shift */
              engineGear = engineGear + 1;
          } else { /* shift 0-6 */
              engineGear = missionParams.getValue(i);
          }
          /* Define the corresponding RPM to each gear */
          int newRPM = 0;
          switch (engineGear) {
              case 0: shield->setMotorSpeed(0); break;
              case 1: shield->setMotorSpeed(820); break;
              case 2: shield->setMotorSpeed(1398); break;
              case 3: shield->setMotorSpeed(2060); break;
              case 4: shield->setMotorSpeed(2683); break;
              case 5: shield->setMotorSpeed(3306); break;
              case 6: shield->setMotorSpeed(4000); break;
          }
      } else if (missionNames.getValue(i) == TMD) { /* --> Time-Duration */
          creatingMIS = creatingMIS + String("tmd=");
          imeDelay = missionParams.getValue(i);
      } else if (missionNames.getValue(i) == EST) { /* --> Engine-Status-Type */
          creatingMIS = creatingMIS + String("est=");
          if (missionParams.getValue(i) == 0) {
              shield->setMotorMode(STOP_MOTOR)
          } else {
              shield->setMotorMode(START_MOTOR)
          }
      } else if (missionNames.getValue(i) == RPM) { /* --> Rotations-per-Minute */
          creatingMIS = creatingMIS + String("rpm=");
          shield->setMotorSpeed(missionParams.getValue(i));
      } else if (missionNames.getValue(i) == LED) { /* --> LED-Light */
          if (missionParams.getValue(i) == 1) {
              digitalWrite(13, HIGH);
          } else {
              digitalWrite(13, LOW);
          }
      } else if (missionNames.getValue(i) == HDA) { /* --> Hall-Delay-Angle  */
          creatingMIS = creatingMIS + String("hda=");
          shield->setParameter(HALL_DELAY_ANGLE, missionParams.getValue(i))
      } else if (missionNames.getValue(i) == STI) { /* --> Sensor-Time-Interval  */
          sensorInterval = missionParams.getValue(i);
      }
      creatingMIS = creatingMIS + missionParams.getValue(i) + String("&");
  }
    Serial.println(creatingMIS + String("tsp=") + millis());
}

// ---> setup()
void setup() {
  // --> Infineon shield initialization
  shield = new TLE9879_Group(1); /* Initialize the shield group with the one shields in the stack */
  shield->setMode(HALL); /* Set the mode to HALL */
  shield->setParameter(HALL_POLE_PAIRS, 4); /* Set number of pole pares to 4, for 8 poles */
  //shield->setParameter(HALL_INPUT_A, 0); /* For the case, that the contacts on the HALL input are wrong */
  shield->setParameter(HALL_ANGLE_DELAY_EN, 1); /* Set the possibility of changing the timing degree */

  // --> Serial communication initialization
  Serial.begin(115200); // Sets the data rate in bits per second (baud) for serial data transmission.
  while (Serial.available() < 9) {}; // Get the number of bytes (characters) available for reading from the serial port. We wait till we get something.

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
          if (name.equals(String("EGS"))) {
            missionNames.add(EGS);
          } else if (name.equals(String("TMD"))) {
            missionNames.add(TMD);
          } else if (name.equals(String("EST"))) {
            missionNames.add(EST);
          } else if (name.equals(String("RPM"))) {
            missionNames.add(RPM);
          } else if (name.equals(String("LED"))) {
            missionNames.add(LED);
          } else if (name.equals(String("HDA"))) {
            missionNames.add(HDA);
          } else if (name.equals(String("STI"))) {
            missionNames.add(STI);
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
      int reading = analogRead(0);
      float temp = reading * 0.0048828125 * 100;
      if (constructionsEnd == true) { // Signal the end of the last sensor data with a ?.
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp + String("#?")); // Example output at the end: "TSP:27.34#TMP:654000#?".
        sensorsEnd = true;
      } else {
        Serial.println(String("TSP:") + sensorStamp + String("#TMP:") + temp); //Example output: "TSP:27.34#TMP:654000".
      }
    }
 delay(10);
}
