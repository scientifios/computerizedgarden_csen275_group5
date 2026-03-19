# GUI Design Overview

## 1. Introduction

This document introduces the GUI design of the Smart Garden Simulation system. It focuses on the main interface modules, supported features, and the basic operating flow for users.

The current GUI is implemented with JavaFX and organized around a central garden simulation view. The interface combines simulation controls, live garden visualization, system monitoring panels, and an event log into one integrated screen.

## 2. Overall GUI Structure

The GUI is mainly assembled in `SmartGardenApplication` and can be divided into the following major areas:

1. **Top Control Toolbar**
2. **Center Garden Simulation Area**
3. **Left System Information Panel**
4. **Right Garden Information Panel**
5. **Event Log Panel**
6. **Animated Background and Visual Effects Layer**

The layout follows a dashboard-style design:

- the **center** is used for the garden grid and the main simulation view,
- the **left side** is used for system monitoring,
- the **right side** is used for garden and plant information,
- the **top** provides simulation controls,
- the **log panel** shows recent runtime events.

## 3. GUI Modules

### 3.1 `SmartGardenApplication`

This is the main GUI entry point.

Its responsibilities include:

- creating the JavaFX scene,
- initializing the controller and all visible panels,
- connecting event handlers,
- starting the periodic UI refresh,
- coordinating interactions between UI and simulation logic.

It is the central class that combines all other visual modules into one application window.

### 3.2 `ModernToolbar`

This module provides the simulation control bar.

Main elements:

- **Start button**
- **Pause / Resume button**
- **Stop button**
- **Speed selector**
- **Status label**

Main functions:

- start the simulation,
- pause and resume the simulation,
- stop the simulation,
- change simulation speed,
- display the current simulation state such as `RUNNING`, `PAUSED`, or `STOPPED`.

This toolbar is the main control entry for simulation lifecycle operations.

### 3.3 `GardenGridPanel`

This is the core visual area of the GUI.

It displays the entire garden as a clickable grid. In the current implementation, the garden uses:

- **8 rows**
- **14 columns**

Main responsibilities:

- rendering all garden tiles,
- showing plants on planted tiles,
- showing grass tiles for empty cells,
- allowing users to click empty cells to plant,
- allowing users to click existing plants to inspect them,
- refreshing tile visuals as the simulation runs,
- displaying pest-related visual effects.

This module is the most interactive part of the user interface.

### 3.4 `InfoPanel` (Left System Panel)

The left `InfoPanel` is used in `LEFT_SYSTEMS` mode and summarizes the system-level runtime status.

It includes the following groups:

- **Weather**
  - current weather
  - ambient temperature
- **Watering System**
  - number of plants needing water
  - sprinkler activation summary
  - sensor status
  - remaining water stock
- **Temperature System**
  - heating system status
  - cooling system status
  - garden temperature
  - temperature trend
- **Pesting System**
  - current pest count
  - critical threat tiles
  - pesticide stock

This panel helps users monitor how the automated subsystems are behaving during simulation.

### 3.5 `InfoPanel` (Right Garden Panel)

The right `InfoPanel` is used in `RIGHT_GARDEN` mode and focuses on garden contents and plant-level information.

It includes:

- current day,
- formatted simulation time,
- plant inventory summary,
- a scrollable plant list,
- per-plant cards with status data.

Each plant card shows:

- plant name,
- tile position,
- growth stage,
- water level,
- days alive,
- total pest attacks,
- active pests,
- current health status.

The right panel also supports plant selection. Clicking a plant card highlights the corresponding tile in the garden grid.

### 3.6 Event Log Panel

The log panel is placed above the garden view and displays recent events from the system.

Main elements:

- log title,
- **Clear** button,
- **Pause Auto-Scroll / Resume Auto-Scroll** button,
- scrolling log list.

Main functions:

- show recent simulation events,
- display warnings and system actions,
- help users understand what the simulation is doing in real time,
- support log inspection without forced auto-scrolling.

Typical log content includes:

- simulation start and stop events,
- watering activity,
- pest-related events,
- resource refill activity,
- system warnings.

### 3.7 `WeatherDisplay`

This module is used to display weather information visually.

It shows:

- a weather icon,
- the current weather name.

It provides a compact visual representation of the current environment state.

### 3.8 Visual Effects Modules

The GUI also includes several visual-effect components to improve feedback and presentation:

- `AnimatedBackgroundPane`
- `ParticleSystem`
- `AnimatedTile`
- `GrassTile`
- `PestEventBridge`

Their roles include:

- rendering animated background effects,
- showing particle effects,
- animating plant tiles,
- displaying empty grass cells,
- bridging pest system events to UI animation updates.

These modules improve clarity and make simulation changes easier to observe.

## 4. Main GUI Features

The current GUI supports the following major features.

### 4.1 Plant Selection and Planting

- A plant selector is available above the garden grid.
- Users choose a plant type from the dropdown menu.
- Users click an empty grid cell to plant at that position.
- The grid updates immediately after successful planting.

### 4.2 Plant Inspection

- Users can click an existing plant tile to open a detailed information dialog.
- Users can also inspect plants in the right information panel.
- Selecting a plant in the right panel synchronizes the highlight in the grid.

### 4.3 Simulation Lifecycle Control

- Start simulation
- Pause simulation
- Resume simulation
- Stop simulation
- Change speed through the speed selector

These features are provided through the top toolbar.

### 4.4 Real-Time Garden Visualization

- All planted tiles are refreshed periodically.
- Plant state changes are reflected visually during simulation.
- Pest events are shown in the grid.
- Pesticide application effects can also be reflected on relevant tiles.

### 4.5 System Monitoring

The GUI continuously shows system-level runtime data, including:

- weather,
- temperature,
- plant counts,
- water stock,
- pesticide stock,
- pest count,
- threat positions,
- sensor summaries.

### 4.6 Event Tracking

- Recent runtime logs are visible inside the GUI.
- Users can clear the log view.
- Users can pause auto-scroll to inspect earlier entries.

### 4.7 Garden Clearing

- A **Clear All** button is available near the plant selector.
- It removes all plants from the garden grid.
- The whole grid is refreshed back to empty tiles.

## 5. How to Operate the GUI

This section describes the basic user workflow.

### 5.1 Starting the Application

1. Launch the JavaFX application.
2. The main window opens with an empty garden grid.
3. The toolbar, side panels, and log panel are shown automatically.

### 5.2 Planting Plants

1. Choose a plant type from the dropdown selector above the grid.
2. Click an empty cell in the garden grid.
3. If the position is valid and empty, the plant is added.
4. The corresponding tile changes from grass to a plant tile.

### 5.3 Viewing Plant Details

There are two main ways to inspect a plant:

1. Click a plant tile in the garden grid.
2. Click a plant card in the right garden information panel.

After selection:

- the plant tile can be highlighted,
- the right panel reflects the selected item,
- a detailed popup may show additional plant status information.

### 5.4 Running the Simulation

1. Plant at least one plant first.
2. Click **Start**.
3. The system enters the running state.
4. The toolbar status changes to show the simulation state.
5. The garden begins updating automatically.

### 5.5 Pausing and Resuming

1. Click **Pause** while the simulation is running.
2. The button text changes to **Resume**.
3. Click **Resume** to continue the simulation.

### 5.6 Stopping the Simulation

1. Click **Stop**.
2. The simulation stops.
3. Toolbar button states are reset.
4. Final statistics are logged by the backend.

### 5.7 Changing Simulation Speed

1. Use the speed combo box in the toolbar.
2. Select one of the supported values:
   - `1x`
   - `2x`
   - `5x`
   - `10x`
3. The controller forwards the selected multiplier to the simulation engine.

### 5.8 Clearing the Garden

1. Click **Clear All** above the garden grid.
2. All currently planted cells are removed.
3. The grid returns to empty grass tiles.

### 5.9 Reading Runtime Events

1. Watch the log panel during simulation.
2. Use **Pause Auto-Scroll** if you want to inspect old messages.
3. Use **Clear** to clear the visible log list.

## 6. Interaction Between GUI and Backend

The GUI does not directly implement garden logic. Instead, it communicates with backend logic through the controller and simulation engine.

Main interaction chain:

`User -> GUI Component -> GardenController -> GardenPlot / SimulationEngine -> GUI Refresh`

Examples:

- planting a plant:
  `GardenGridPanel -> GardenController.plantSeed(...) -> GardenPlot.addPlant(...)`
- starting simulation:
  `ModernToolbar -> GardenController.startSimulation() -> SimulationEngine.start()`
- pest visualization:
  `PestControlSystem -> PestEventBridge -> GardenGridPanel`

This design keeps the GUI focused on presentation and interaction, while the business logic remains in the controller and domain/simulation layers.

## 7. Design Characteristics

The current GUI design has several notable characteristics.

### 7.1 Modular Structure

The interface is divided into reusable modules such as the toolbar, garden panel, side information panels, and log panel. This improves maintainability and readability.

### 7.2 Clear Responsibility Separation

- `SmartGardenApplication` assembles the interface,
- `GardenGridPanel` handles garden interaction,
- `ModernToolbar` handles lifecycle controls,
- `InfoPanel` handles information display,
- backend logic stays in the controller and simulation engine.

### 7.3 Real-Time Feedback

The GUI refreshes periodically and reflects changing simulation state in real time. This makes the system easier to observe and demonstrates the behavior of the automated subsystems clearly.

### 7.4 Synchronized Views

The grid view, system panel, garden panel, and event log all work together. A change in simulation state is reflected across multiple interface areas.

### 7.5 Visual Emphasis

The use of animations, weather display, tile graphics, background effects, and pest events improves clarity and user engagement.

## 8. Summary

The Smart Garden GUI is designed as an interactive simulation dashboard. It provides:

- a central garden visualization,
- simulation lifecycle controls,
- real-time system monitoring,
- plant-level inspection,
- event logging,
- animated visual feedback.

Together, these modules allow users to plant, run, observe, and manage the simulated garden through a single integrated graphical interface.
