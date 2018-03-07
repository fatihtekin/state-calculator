# State Calculator
It is assumed that `dependency_of` field can be trusted
 - `Scopt` used for command line parameters parsing
 - `json4s` used for json serialization and deserialization

TODOs 
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
### Docker Run
```bash
docker run -v ${localPath}:/opt/files -it state-calculator:0.1 -g /opt/files/graph.json -e /opt/files/events.json
```