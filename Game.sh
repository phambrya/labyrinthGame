#!/bin/bash

javac -classpath "LabyrinthGame/lib/*" -d "./out" LabyrinthGame/src/*.java
java -classpath "LabyrinthGame/lib/*:./out/" Driver