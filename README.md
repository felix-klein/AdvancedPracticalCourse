## Advanced Practical Course

**Info text Part II - Advanced Practical Course:** \
This project is the continuation of the Bachelor: An Integrated Approach to Predictive
Maintenance and Quality Control for E-Engines through Compliance Checking. The Advanced Practical Course: 0000000917
Advanced Practical Course - Supportive Process Automation and Worker Assistance (IN2128, IN2106), offers the
possibility to extend and improve the prototype on specific features. The GitHub environment is used for this part II
of the project for reasons of overview and deployment. This is done by redeploying and embedding directly. Note that
much of the previous information, implementations and structures are retained, but their form can and must be changed.
A special tagging of the changed items is not made explicit, but can be taken from the git history.

**Info text Part I - Bachelor Thesis:** \
Welcome to the system and documentation environment for the bachelor thesis: An Integrated Approach to Predictive 
Maintenance and Quality Control for E-Engines through Compliance Checking. In addition to the written thesis, this work
also includes the practical part of the prototype construction. This environment is primarily used for editing and 
storing the software used for the prototype, but also includes the thesis itself, the literature references, the 
artefacts and a large number of images and videos that exceed the scope of the written work. In the following, you will
be referred to the further materials and receive information about the systems and environments used.

# Bachelor Thesis Material
All non-software related materials can be found in the folder [Bachelor-Thesis-Material](Bachelor-Thesis-Material).
* [Artefacts](Bachelor-Thesis-Material/Artefacts)\
  Artefacts include data sheets and implementation recommendations for the hardware, as well as software packages and 
  descriptions for the Infineon microcontroller.
* [Images](Bachelor-Thesis-Material/Images)\
  Images are divided into two sections: Images that can be found in the written elaboration and images that have been 
  added as supplements.
* [Literature References](Bachelor-Thesis-Material/Literature_References)\
  The literature references are copies of the literature used in the preparation of this thesis.
* [Written Thesis](Bachelor-Thesis-Material/Written_Thesis)\
  In the Written Thesis environment, you will find the written bachelor thesis in PDF format, as well as photographs of
  the submitted portfolio.

# Requirements for easy set up
For a simple setup of this project, [pom.xml](pom.xml) can be used,
the dependencies and plugins contained here are as follows:

* Dependencies:
    * javafx-controls
    * javafx-fxml
    * jSerialComm
* Plugins:
    * javafx-maven-plugin
* Properties:
    * maven.compiler.source: 20
    * maven.compiler.target: 20
    * project.build.sourceEncoding: UTF-8

# IDE's and external usages
* External IDE`s:
  * Camunda Modeler (Version 5.12.0)
  * Arduino IDE (Version 2.2.1)
  * Gluon — Scene Builder (Version 20.0.0)
  * IntelliJ IDEA — Ultimate Edition (Version 2023.2.1)
  * CLion (Version 2023.2.1)
  * Postman (Version 10.19.0)

* External Additions:
  * [CPEE](https://cpee.org/) - Engine: Modeller for the processes
  * Google Chrome (Version 117.0.5938.149) - As standard browser

# Build and run the project
To get the project running:
1. Install Google Chrome;
2. Make *Maven* executable (run) by use of:\
   ```View > Tool Windows > Maven > i17BA > Run Configurations > i17BA [javafx:run]```

# Code structure and layout
The code structure in the form of classes, methods and attributes can quickly become confusing in such large projects.
Therefore, the following lightweight UML-diagram reveals a structural representation of the back-end. Even though not
all methods (and even classes) are shown, it still illustrates the key connections.

![UML diagram of the project](UML.png)

# Commands and Sensor Response rules and syntax
* Commands:
  * STI (Sensor-Time-Interval): ->sensorInterval
  * EST (Engine-Status-Type): 0/1
  * HDA (Hall-Delay-Angle): 1-59
  * RPM (Rotations-per-Minute): 0-4000
  * TMD (Time-Duration): 0-int (in seconds)
  * EMS (Emergency Stop): 1 -> Description at [Emergency Stop](#Emergency-Stop)
* Sensor Response:
  * TMP is a list of all the Temperature gathered.
  * VIB is a list of all the Vibration gathered.
  * MIC is a list of all the Microphone levels of loudness gathered.
  * CP1 is a list of all the sensor values at the Current-Phase-1.
  * CP2 is a list of all the sensor values at the Current-Phase-2.
  * CP3 is a list of all the sensor values at the Current-Phase-3.
  * TSP is a list of all the respective Time-Stamps gathered.
  * MIS is a list of all the Mission-Initialisation-Stamps (not corresponding to the rest).

# Blueprint hardware command initialisation (data structure)
* Pre-Testing (Each 1 - 9):
  * Cold_Start_Fast
  * Long_Run
  * Cool_Down_Fast
  * Cold_Start_Slow&G_C_Up
  * Cool_Down_Slow&G_C_Down
* See command structure and description in
  [BlueprintCommandFlowHardware_Described](src/main/resources/adminFiles/BlueprintCommandFlowHardware_Described.txt)

# Comment structure 
Comments are an important and essential part of clear and comprehensible code. Therefor, we do integrate comments
whenever there is a line of code which is not trivial.\
For further clarification and as a result of active programming we decided to use two types to comments:\
  * `//` for not completed comments and work, like a 'in construction comment'
  * `/* */` and `/** **/`for final comments and work, like a 'final/normal comment'
This structure does not count for code which is not written in java.

# List of Programming-References
* Arduino IDE-language: [Arduino LANGUAGE](https://www.arduino.cc/reference/en/)
* Arduino API List: [Arduino List Library  2.1.4](https://nkaaf.github.io/Arduino-List/html/index.html)
* Infineon Shield Examples: [Example Sketches](Bachelor-Thesis-Material/Artefacts/TLE9879QXA40/02_example_sketches)

# Design Guide
* Main Design-Guide reference:
  * [M2-Material Design](https://m2.material.io/design/color/dark-theme.html)
* Color Scheme Dark-Theme:
  * Color Scheme: [TUM Colors](https://gist.github.com/lnksz/51e3566af2df5c7aa678cd4dfc8305f7)
  * Dark Scheme: [MATERIAL DESIGN - Dark theme](https://m2.material.io/design/color/dark-theme.html#ui-application)
    * Dark: `#121212` - Background
    * TUM Blue: `#0065bd` - Header
    * tum-blue-dark: `#072140` - 
    * tum-grey-1: `#20252A` - Box
    * Red200: `#B00020` `176,0,32` - Warning, Wrong Task
    * Green200: `#018786` `1, 135, 134`- Correct Task
    * tum-blue-light: `#5E94D4` - Label, Event Start/End
    * tum-blue-light-dark: `#9ABCE4` - Event

# Hardware Connection
* Connection:
  * Type: `Cable`
  * Technique: `Serial` (JSerialComm)
  * Port: `/dev/cu.usbmodem141101`
  * Baud Rate: `9600`
  * Terminal Input: `screen /dev/cu.usbmodem141101 115200`
  * Main information Link: [JSerial GitHub](https://github.com/Fazecast/jSerialComm)

# Emergency Stop
The Emergency Stop is a Task which can be included into a process when ever needed.
Its primary function and intended use is the testing of a process flow. The user does
not need to delete or create a new whole process just for the testing of a beginning segment.

The Emergency Stop functions as a rapid and immediate end of the test. No more commands are
transferred to the board, the motor will stop running and the sensor data will stop sending.

# Further Code Insights as a Guest
As the main Git environment for the generation of this project, the [LRZ GitLab](https://gitlab.lrz.de)
is in use. Included is an extensive documentation of Commit, Push and Merge commands, as well as 
Milestones and Labels.\
As the publication requires a public availability of the system code and additional materials, the transit
to [GitHub i17ba](https://github.com/felix-klein/i17ba) was conducted and does not include properties which are not 
regarded directly to the systems, e.g. Push history and Milestones.\
If you are interested in further insights, older releases or development stages which are documented in the GitLab
version of this project, or if you got general project question, please use the 
Discord-Server: [Server i17ba-Q&A](https://discord.gg/rDpUg3Vh) (active until one year after official project completion, 15.10.2023).
