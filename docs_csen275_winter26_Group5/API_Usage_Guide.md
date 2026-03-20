# Smart Garden API Usage Guide

This document explains how to use `GardenSimulationAPI` for headless simulation, where the instructor should place their own `GardenSimulator`, and where the runtime logs are stored.

## 1. API Source Locations

- API main class: `src/main/java/CSEN275Garden/api/GardenSimulationAPI.java`
- Example driver class: `src/main/java/CSEN275Garden/api/GardenSimulator.java`

`GardenSimulationAPI` is already implemented. The instructor only needs to write a `main` method that calls it.

## 2. Where the Instructor's GardenSimulator Should Be Placed

The recommended folder is:

`src/main/java/CSEN275Garden/api/`

Reason:

- The existing example `GardenSimulator.java` is already in this package.
- This directory matches the package `CSEN275Garden.api`.
- Keeping the instructor's simulator in the same package makes the project structure clear and easy to run.

If the instructor creates their own simulator file, the recommended setup is:

- File name: for example `GardenSimulator.java` or `InstructorGardenSimulator.java`
- Package declaration:

```java
package CSEN275Garden.api;
```

A recommended full path is:

`src/main/java/CSEN275Garden/api/InstructorGardenSimulator.java`

## 3. Minimal Example

```java
package CSEN275Garden.api;

import java.util.Map;

public class InstructorGardenSimulator {
    public static void main(String[] args) {
        GardenSimulationAPI api = new GardenSimulationAPI();

        api.initializeGarden();

        Map<String, Object> plants = api.getPlants();

        api.rain(25);
        api.temperature(90);
        api.parasite("Green Leaf Worm");
        api.getState();

        api.stopHeadlessSimulation();
        GardenSimulationAPI.closeApiLog();
    }
}
```

## 4. API Interface Explanation

The following descriptions are based on the current implementation of `GardenSimulationAPI`.

### `new GardenSimulationAPI()`

Purpose:

- Creates an API instance
- Uses the default `GardenController`
- Creates a default `8 x 14` garden

Recommended for:

- instructor-written test scripts
- simple usage without custom controller setup

### `new GardenSimulationAPI(GardenController controller)`

Purpose:

- Uses a caller-provided `GardenController`
- Useful when a custom garden size or customized controller is needed

Note:

- `controller` cannot be `null`, otherwise an `IllegalArgumentException` is thrown

### `initializeGarden()`

Purpose:

- Initializes the garden
- Enables API log mirroring
- Attempts to load initial plants from `src/main/resources/garden-config.json`
- Falls back to default plants if the config file is missing or fails to load
- Starts the headless simulation

This should usually be called before any other API actions.

### `getPlants()`

Return type:

`Map<String, Object>`

The returned map contains 3 keys:

- `"plants"`: list of currently living plant names
- `"waterRequirement"`: list of water requirements for each plant
- `"parasites"`: list of possible pests for each plant

Typical usage:

- external test scripts
- grading or automated checks

### `rain(int amount)`

Purpose:

- Triggers a rainfall event
- Adds water to each living plant
- Sets the weather to `RAINY`
- Triggers system updates such as watering, temperature control, and pest handling
- Increments the internal API `dayCount` by 1

Parameter:

- `amount`: water amount added by this rain event

### `temperature(int temp)`

Purpose:

- Triggers a temperature change event
- The parameter is in Fahrenheit
- If the input is outside the range `40` to `120`, it is clamped automatically
- Updates the heating and cooling systems
- Applies temperature effects to plants
- Increments the internal API `dayCount` by 1

Parameter:

- `temp`: temperature in Fahrenheit, for example `90`

### `parasite(String parasiteType)`

Purpose:

- Triggers a pest infestation event
- Applies damage to plants that are vulnerable to the given pest
- Registers the pest with `PestControlSystem` so the system can continue automatic treatment
- Increments the internal API `dayCount` by 1

Parameter:

- `parasiteType`: pest name, case-insensitive

Examples:

```java
api.parasite("Green Leaf Worm");
api.parasite("Red Mite");
```

### `getState()`

Purpose:

- Does not return an object
- Writes a snapshot of the current garden state to the log

The logged state includes:

- current API day
- living plant count
- dead plant count
- total plant count
- zone count
- each plant's position, alive/dead status, health, and water

This is especially useful at the end of an instructor test run.

### `getController()`

Purpose:

- Returns the underlying `GardenController`
- Useful for advanced testing

### `getGarden()`

Purpose:

- Returns the underlying `GardenPlot`
- Useful when the caller wants to inspect the garden state directly

### `getDayCount()`

Purpose:

- Returns the API event-based day counter
- This value increases after each call to `rain(...)`, `temperature(...)`, or `parasite(...)`

### `startHeadlessSimulation()`

Purpose:

- Starts the headless simulation manually
- Logs a warning if it is already running
- Does not start if there are no living plants

Note:

- `initializeGarden()` already calls this internally
- In normal usage, an extra manual call is usually unnecessary

### `stopHeadlessSimulation()`

Purpose:

- Stops the headless simulation

Recommendation:

- Call this before program exit for clean shutdown

### `isHeadlessSimulationRunning()`

Purpose:

- Returns whether the headless simulation is currently running

### `getHeadlessDayCount()`

Purpose:

- Returns the headless engine's own day counter
- This is different from `getDayCount()`

Difference:

- `getDayCount()` tracks API-triggered event count
- `getHeadlessDayCount()` tracks day progression inside the background simulation engine

### `GardenSimulationAPI.closeApiLog()`

Purpose:

- Closes the API mirror log file

Recommendation:

- Call it once before program exit
- The shutdown hook also handles cleanup, but explicit shutdown is safer

## 5. Configuration Files Used During Initialization

The API reads these resource files:

- `src/main/resources/garden-config.json`
- `src/main/resources/parasites.json`

Meaning:

- `garden-config.json`: initial plants and positions loaded during startup
- `parasites.json`: mapping between plant types and pest types

If these resource files are missing, the API still runs using internal default values.

## 6. Where the Log Files Are Stored

The API produces two kinds of logs.

### 6.1 Session Log

Directory:

`logs/`

This is under the project root:

`<project-root>/logs/`

File naming format:

`garden_yyyyMMdd_HHmmss.log`

Example:

`logs/garden_20260319_213000.log`

Characteristics:

- This is the main system log
- `Logger` automatically creates the `logs/` folder if it does not exist
- Each run creates a new session log file

### 6.2 API Mirror Log

File location:

`log.txt`

This file is under the project root, not inside `logs/`.

That means:

`<project-root>/log.txt`

Characteristics:

- `initializeGarden()` internally calls `Logger.enableApiLogging(Paths.get("log.txt"))`
- API-driven log entries are mirrored into this file
- Multiple runs append to the same file instead of creating a new file name each time

## 7. How the Instructor Can Run Their Own GardenSimulator

If the instructor's class is placed at:

`src/main/java/CSEN275Garden/api/InstructorGardenSimulator.java`

and the package is:

```java
package CSEN275Garden.api;
```

then the project can be compiled first, and the class can be run afterward.

### Windows PowerShell Example

```powershell
.\mvnw.cmd clean compile -DskipTests
java -cp target/classes CSEN275Garden.api.InstructorGardenSimulator
```

If the instructor wants to run the example class already included in the project:

```powershell
java -cp target/classes CSEN275Garden.api.GardenSimulator
```

## 8. Most Important Conclusions for the Instructor

- The instructor's own `GardenSimulator` should be placed in `src/main/java/CSEN275Garden/api/`
- The recommended package name is `CSEN275Garden.api`
- The typical call order is:
  1. `initializeGarden()`
  2. `getPlants()` or other API queries
  3. `rain(...)` / `temperature(...)` / `parasite(...)`
  4. `getState()`
  5. `stopHeadlessSimulation()`
  6. `GardenSimulationAPI.closeApiLog()`
- The main runtime logs are written under the project-root `logs/` folder
- The API mirror log is written to project-root `log.txt`
