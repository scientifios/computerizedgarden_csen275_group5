# Smart Garden Simulation Manual

## Introduction

The Smart Garden Simulation is a desktop application developed for CSEN 275. It models a computerized garden in which users plant crops and flowers on a visual grid, then observe how automated subsystems manage watering, temperature, pests, and weather over simulated time.

The current version uses a JavaFX graphical interface, a controller-driven application structure, and a simulation engine that updates the garden continuously while presenting live status information to the user.

## Key Features

- Interactive `8 x 14` garden grid for planting and observing plants
- Nine selectable plant types:
  - Fruits: Strawberry, Cherry, Apple Sapling
  - Vegetables: Cabbage, Tomato, Scallion
  - Flowers: Daisy, Lily, Peony
- Simulation lifecycle controls: `Start`, `Pause`, `Resume`, and `Stop`
- Adjustable simulation speed: `1x`, `2x`, `5x`, and `10x`
- Automated watering system with weather-aware behavior
- Automated heating and cooling systems for garden temperature management
- Automated pest control system with live pest tracking
- Real-time weather updates shown in the GUI
- Left-side system dashboard for water, temperature, and pest status
- Right-side garden panel with per-plant detail cards
- Event log panel for recent system actions and simulation events
- Animated visual feedback for planting, watering, pests, and background effects

## Runtime Requirements

To build and run the system successfully, the following environment is required:

- Operating system: Windows, macOS, or Linux
- Java Development Kit: JDK 21
- Build tool: Maven 3.9+ or the included Maven Wrapper (`mvnw.cmd` / `mvnw`)
- GUI support: Desktop environment capable of running JavaFX applications
- Recommended memory: at least 8 GB RAM

Project-level technical requirements from the current codebase:

- Java source level: `21`
- JavaFX version: `23.0.1`
- Test framework: JUnit `5.10.2`

Before deployment, verify:

```bash
java -version
mvn -version
```

If Maven is not installed globally, use the Maven Wrapper included in the project.

## Deployment Procedure

The system can be deployed and started directly from the project directory.

### Option 1: Use the provided Windows scripts

1. Open a terminal in the project root.
2. Build the project:

```bat
build.bat
```

3. Build and run the GUI application:

```bat
run.bat
```

`run.bat` checks for Java, builds the project with the Maven Wrapper, and launches the JavaFX interface.

### Option 2: Use Maven manually

1. Open a terminal in the project root.
2. Build the project:

```bash
./mvnw clean install
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd clean install
```

3. Launch the GUI:

```bash
./mvnw javafx:run
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd javafx:run
```

### Deployment Notes

- The main GUI entry point is `CSEN275Garden.SmartGardenApplication`.
- The Maven configuration already includes the JavaFX plugin and main class settings.
- Runtime resources such as CSS, images, and JSON configuration files are loaded from `src/main/resources`.
- Log files are generated under the `logs` directory while the system is running.

## How to Use the GUI

### 1. Start the application

After launch, the main window opens automatically. The interface contains:

- a top control toolbar,
- a center garden grid,
- a left system information panel,
- a right garden information panel,
- and an event log panel above the garden view.

### 2. Plant seeds in the garden

1. Use the plant selector above the grid.
2. Choose one plant type from the dropdown list.
3. Click an empty grass tile in the garden grid.
4. The selected plant will be placed in that tile immediately.

If a tile already contains a plant, clicking it opens plant details instead of planting a new one.

### 3. Control the simulation

Use the top toolbar:

- `Start`: begins the simulation
- `Pause`: pauses the running simulation
- `Resume`: continues the simulation after pause
- `Stop`: stops the simulation
- `Speed`: changes the simulation rate to `1x`, `2x`, `5x`, or `10x`

Important behavior:

- The simulation cannot start unless at least one plant has been added.
- The status label in the toolbar shows whether the system is `STOPPED`, `RUNNING`, or `PAUSED`.

### 4. Monitor the left system panel

The left panel provides real-time system status in four areas:

- Weather
  - current weather
  - ambient temperature
- Watering System
  - plants needing water
  - sprinkler activations
  - sensor status
  - remaining water stock
- Temperature System
  - heating status
  - cooling status
  - garden temperature
  - temperature trend
- Pesting System
  - current pest count
  - critical threat tiles
  - pesticide stock

This panel is used to understand how the automated subsystems are reacting during the simulation.

### 5. Inspect the right garden panel

The right panel shows garden-level and plant-level information:

- current day
- current simulation time
- plant inventory summary
- a scrollable list of planted items

Each plant card includes:

- plant name
- tile position
- growth stage
- water level
- days alive
- total pest attacks
- active pests
- current health status

Clicking a plant card highlights the corresponding tile in the grid.

### 6. Inspect plant details directly from the grid

Click any planted tile in the garden grid to open a detail dialog. The popup shows information such as:

- growth stage
- health percentage
- water level and water requirement
- days alive and lifespan
- total pest attacks
- active pest count
- current status

### 7. Use the event log

The log panel shows recent simulation activity, including system messages, watering actions, pest events, and simulation state changes.

Available controls:

- `Clear`: clears the visible log list
- `Pause Auto-Scroll`: freezes automatic scrolling so older entries can be reviewed
- `Resume Auto-Scroll`: re-enables automatic scrolling

### 8. Clear the garden

Use the `Clear All` button next to the plant selector to remove every planted item from the grid and return the garden to its empty state.

## Summary

This system provides an interactive way to observe how a computerized garden behaves under automated management. Users can plant seeds, run the simulation, monitor subsystem activity, inspect plant health, and review system events from a single JavaFX dashboard.
