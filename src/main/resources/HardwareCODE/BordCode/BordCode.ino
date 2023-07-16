// ---> include libraries
#include <SingleLinkedList.hpp>
// ---> defines and constants

/*
Normal example Input: <*STI:999#><*LED:1#*TMD:5000#> <*LED:0#*TMD:1000#> <*LED:1#*TMD:4500#><*EST:0#>?
Min Input: <*EST:1#> (9 for starting the engine as an example)
*/

// ---> global variables
bool constructionsEnd = false;
SingleLinkedList<byte> missionNames;
SingleLinkedList<short> missionParams;
short timeDelay = 100;
bool runMission = false;
static long missionStamp;
static byte counter = 0;
short sensorInterval = 499;

const byte LED = 1; //(LED-Lights: ON)
const byte RPM = 2; //(Rotations-Per-Minute: 3600)
const byte TMD = 3; //(Time-Duration: 5000)
const byte EST = 4; //(Engine-Status-Type: ON)
const byte EGS = 5; //(Engine-Gear-Shift: 8)
const byte TSP = 6; //(Time-Stamp: 302000)
const byte STI = 7; //(Sensor-Time-Interval: 999)

// ---> user defined functions
void applyMission() { // Helper function to apply the gathered mission.
counter += 1;
  for (int i=0; i<missionNames.getSize(); i++) { //TODO this is an test code and needs to be adjusted!
    if (missionNames.getValue(i) == LED) { // Light task
      if (missionParams.getValue(i) == 1) {
        digitalWrite(13, HIGH);
      } else {
        digitalWrite(13, LOW);
      }
      Serial.print(counter + String("-Mission: LED"));
    } else if (missionNames.getValue(i) == TMD) {
      timeDelay = missionParams.getValue(i);
      Serial.print(counter + String("-Mission: TMD"));
    } else if (missionNames.getValue(i) == STI) {
      sensorInterval = missionParams.getValue(i);
      Serial.print(counter + String("-Mission: STI"));
    } else if (missionNames.getValue(i) == EST) {
      if (missionParams.getValue(i) == 1) {
        //TODO start engine.
      } else {
        //TODO stop engine.
      }
    }
    int currentParam = missionParams.getValue(i);
    Serial.println(String(":") + currentParam);
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
  if (constructionsEnd != true) { // If there are still constructions to fulfill.
  // INPUT: Constructions initializing.
    if (runMission != true) { // If there is an Mission running, with reference to the time, new missions shouldn't be read.
      if (Serial.available() >= 1) { // Safety measure, for the case that the serial communication is slower than the setup() function.
        char controller = Serial.read();
        Serial.println(String("Each controller char: ") + controller);
        if (controller == '<') {
        } else if (controller == '*') {
          String name = Serial.readStringUntil(':');
          Serial.println(String("name: ") + name);
          if (name.equals(String("LED"))) {
            missionNames.add(1);
          } else if (name.equals(String("RPM"))) {
            missionNames.add(2);
          } else if (name.equals(String("TMD"))) {
            missionNames.add(3);
          } else if (name.equals(String("EST"))) {
            missionNames.add(4);
          } else if (name.equals(String("EGS"))) {
            missionNames.add(5);
          } else if (name.equals(String("TSP"))) {
            missionNames.add(6);
          } else if (name.equals(String("STI"))) {
            missionNames.add(7);
          }
          String param = Serial.readStringUntil('#');
          Serial.println(String("param: ") + param);
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


// OUTPUT: Sensor data gathering, in parallel.
    static unsigned long sensorStamp = 0; // Static, because it should stay throughout the loop / unsigned long, because if it is to big it should restart at 0.
    if (millis() - sensorStamp > sensorInterval) {
      sensorStamp = millis();
      int reading = analogRead(0);
      float temp = reading * 0.0048828125 * 100;
      if (constructionsEnd == true) { // Signal the end of the last sensor data with a ?.
        Serial.println(String("TSP:") + sensorStamp + String("*TMP:") + temp + String("*?")); // Example output at the end: "TSP:27.34*TMP:654000*?".
      } else {
        Serial.println(String("TSP:") + sensorStamp + String("*TMP:") + temp); //Example output: "TSP:27.34*TMP:654000".
      }
    }
  }
 delay(10);
}
