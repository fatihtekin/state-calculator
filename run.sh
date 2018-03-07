#!/usr/bin/env bash
sbt assembly
if [ "$#" -eq 2 ]
then
java -jar target/scala-2.12/state-calculator.jar -g $1 -e $2
else
echo "Only 2 file path arguments must be given. First graph then events json files"
fi