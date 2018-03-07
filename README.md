# State Calculator
It is assumed that `dependency_of` field can be trusted
 - `Scopt` used for command line parameters parsing
 - `json4s` used for json serialization and deserialization

The main logic is
 - Prepare a map of components by their ids
 - Set derived/own states of the components
 - Sort events by timestamp and update the states by the events
 - Breath First Search for alert components so that we wont miss components because of early visiting of no_data/clear/warning components
 - Breath First Search for alert components so that we wont miss components because of early visiting of no_data/clear components
  
TODOs
 - Don't show when list/set/map are empty in json 
 - Try reducing the mutable collections using copy() or etc
 - More testing could be added
 - Metrics could be added
 - Benchmark test could be added

## Prerequisites 
 - Install scala 2.12
 - Install sbt 1.1.1 or newer
## Test with Coverage and Report
```bash
sbt clean coverage test coverageReport
```
## Build
```bash
sbt assembly
```
### Docker Build 
```bash
sbt docker:publishLocal
```
## Run
```bash
java -jar target/scala-2.12/state-calculator.jar -g graph.json -e events.json 
```
or
```bash
./run.sh src/test/resources/graph.json src/test/resources/events.json
```
### Docker Run
```bash
docker run -v ${localPath}:/opt/files -it state-calculator:0.1 -g /opt/files/graph.json -e /opt/files/events.json
```