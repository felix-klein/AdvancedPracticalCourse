# Bachelor Thesis Practical

## Requirements for easy set up

For a simple setup of this project, [pom.xml](pom.xml) can be used,
the dependencies and plugins contained here are as follows:

* Dependencies:
    * javafx-controls
    * bootstrapfx-core
    * formsfx-core
    * javafx-fxml
    * camunda-bpm-assert
    * jSerialComm
    * junit-jupiter-api
    * graphviz-java
* Plugins:
    * javafx-maven-plugin
* Properties:
    * maven.compiler.source: 20
    * maven.compiler.target: 20
    * project.build.sourceEncoding: UTF-8
* <u>External IDE`s:</u>
  * Camunda Modeler (Version 5.12.0)
  * Arduino IDE (Version 2.1.0)

## Build and run the project

To get the project and all its windows running:

Make *Maven* executable (run) by use of:\
   ```View > Tool Windows > Maven > i17BA > Run Configurations > i17BA [javafx:run]```

## Code structure and layout

The code structure in the form of classes, methods and attributes can quickly become confusing in such large projects.
Therefore, the following lightweight UML-diagram reveals a structural representation of the back-end. Even though not
all methods (and even classes) are shown, it still illustrates the key connections.

![UML diagram of the project](UML.svg)

# Operation rules and syntax
Operations:\
  * Engine: engine, motor, machine, unit, main.
  * Gear: gear, shift, throttle, level, nivo.
  * Runtime: time, duration, span, period, term, run, length.
  * null: default.
Operation Values:\
  * 0: stop, end, terminate, finish, off, exit, 0.
  * 1: start, go, run, launch, begin, on, 1.
  * -101: increase, rise, grow, up, jump.
  * -90: decrease, reduce, low, les, ease.
  * 0 - 32767: 0, 1, ..., 32767.
  * default: -11111.

# Comment structure 
Comments are an important and essential part of clear and comprehensible code. Therefor, do we integrate comments
whenever there is a line of code which is not trivial.\
For further clarification and as a result of active programming we decided to use two types to comments:\
  * `//` for not completed comments and work, like a 'in construction comment'
  * `/* */` and `/** **/`for final comments and work, like a 'final/normal comment' 

## List of References

* Algorithmic-References
    *   
* Frontend-References
    * graphviz-java: [Graphviz Github](https://github.com/nidi3/graphviz-java)
* Hardware-References
    * Arduino IDE-language: [Arduino LANGUAGE](https://www.arduino.cc/reference/en/)
    * Arduino API List: [Arduino List Library  2.1.4](https://nkaaf.github.io/Arduino-List/html/index.html)

## Design Guide

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

# Cheat Zone
* Visibility:
  ![img.png](information1.png)
    
* Numbers:
  * byte (8): -128 to 127 
  * short (16): −32768 to 32767
  * int (32):−2147483648 to 2147483647
  * long (64): −9223372036854775808 to 9223372036854775807
  * float (32): -3.4e+38 to 3.4e+38 --7 digits
  * double (64): -1.7e+308 to 1.7e+308 --15 digits
  * boolean (1): true to false
  * char (16): all Unicode
* JavaFX:
  * Layers: Stage >> Scene >> Scene-Graph > Root > Branch > Leaf
  * SceneBuilder: JavaFX is functional modeled via SceneBuilder App.
  * Window Size: 649.0 x 803.0
  * Window Position: 781.0, 25.0

# Hardware Connection
* Connection:
  * Type: `Cable`
  * Technique: `Serial` (JSerialComm)
  * Port: `/dev/cu.usbmodem141201`
  * Baud Rate: `115200`
  * Terminal Input: `screen /dev/cu.usbmodem141201 115200`
  * Main information Link: [JSerial GitHub] (https://github.com/Fazecast/jSerialComm)