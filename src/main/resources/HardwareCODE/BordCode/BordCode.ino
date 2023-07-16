// ---> include libraries
#include <SingleLinkedList.hpp>
// ---> defines and constants

/*
Normal example Input: <*RPM:3600#*TMD:5000#>?
Min Input: <*EST:1#> (9 for starting the engine as an example)
Examples:
  RPM:3600 (Rotations-Per-Minute)*
  TMD:5000 (Time-Duration)*
  EST:1 (Engine-Status-Type: ON)*
  EGS:8 (Engine-Gear-Shift)*
  LED:1 (LED-Lights: ON)
  TSP:302000 (Time-Stamp)
  STI:999 (Sensor-Time-Interval)
*/

// ---> global variables
bool constructionsEnd = false;
SingleLinkedList<String> missionNames;
SingleLinkedList<short> missionParams;
short timeDelay = 10000;
bool runMission = false;
static long missionStamp = 0;
static byte counter = 0;
short sensorInterval = 499;

// ---> user defined functions
void applyMission() { // Helper function to apply the gathered mission.
counter += 1;
  for (int i=0; i<missionNames.getSize(); i++) { //TODO this is an test code and needs to be adjusted!
    if (missionNames.getValue(i).equals(String("LED")) { // Light task
      if (missionParams.getValue(i) == 1) {
        digitalWrite(13, HIGH);
      } else {
        digitalWrite(13, LOW);
      }
    } else if (missionNames.getValue(i).equals(String("TMD")) {
      timeDelay = missionParams.getValue(i);
    } else if (missionNames.getValue(i).equals(String("STI")) {
      sensorInterval = missionParams.getValue(i);
    }
    Serial.println(String(counter + F("-Mission: " ) + missionNames.getValue(i) + ":" + missionParams.getValue(i)));
  }
}

// ---> setup()
void setup() {
  Serial.begin(115200); // Sets the data rate in bits per second (baud) for serial data transmission.
  while (Serial.available() < 9) {}; // Get the number of bytes (characters) available for reading from the serial port. We wait till we get something.

  pinMode(13, OUTPUT);
}

// ---> loop()
void loop() {
  Serial.println(String(F(--S----LOOP begin-------) + millis())); //TODO Testing

  
  if (constructionsEnd != true) { // If there are still constructions to fulfill.
  // INPUT: Constructions initializing.
    if (runMission != true) { // If there is an Mission running, with reference to the time, new missions shouldn't be read.
      if (Serial.available() >= 1) { // Safety measure, for the case that the serial communication is slower than the setup() function.
        char controller = Serial.read();

        if (controller == '<') {
          Serial.println(String(F("Group-Mission: End-Mission  /. " + "TSP: " + millis())));
        } else if (controller == '*') {
          String name = readStringUntil(':');
          String param = readStringUntil('#');
          missionNames.add(name);
          missionParams.add(param);
        } else if (controller == '>') {
          applyMission();
          missionStamp = millis();
          Serial.println(String(F("Group-Mission: Start-Mission  /. " + "TSP: " + missionStamp)));
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


// OUTPUT: Sensor data gathering, in parallel.
    static unsigned long sensorStamp = 0; // Static, because it should stay throughout the loop / unsigned long, because if it is to big it should restart at 0.
    if (millis() - sensorStamp > sensorInterval) {
      sensorStamp = millis();
      int reading = analogRead(0);
      temp = reading * 0.0048828125 * 100;
      if (constructionsEnd == true) { // Signal the end of the last sensor data with a ?.
        Serial.Println(String("TSP:" + sensorStamp + '*' + "TMP:" + temp + "*?")); // Example output at the end: "TSP:27.34*TMP:654000*?".
      } else {
        Serial.Println(String(F("TSP:") + sensorStamp + '*' + F("TMP:") + temp)); //Example output: "TSP:27.34*TMP:654000".
      }
    }
  }
 Serial.println(String(F(--E----LOOP stop-------) + millis())); //TODO Testing
}
