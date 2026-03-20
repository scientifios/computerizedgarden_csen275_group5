## Activity Diagrams

The following sections describe the scenario and the general flow of each activity diagram.

### 1. `activity_other_business_planting`

Related files:

- `activity_other_business_planting.puml`
- `activity_other_business_planting.png`

#### Scenario

This diagram describes the flow for planting a plant in the GUI. It applies to the scenario where the user selects a plant type and then clicks an empty tile to place it.

#### General Flow

1. The user selects a plant type and clicks an empty tile.
2. `GardenGridPanel` reads the selected plant type and builds a `GridPosition`.
3. `GardenController` calls `plantSeed(PlantType, GridPosition)` and creates the concrete plant object.
4. `GardenPlot` validates the target position and checks whether it is already occupied.
5. If the position is invalid or occupied, the operation fails and the UI remains unchanged.
6. If the position is valid, the plant is added to `plantMap`, the plant counts are updated, and the target `GardenZone` is found.
7. `GardenZone` adds the plant to its internal list.
8. The controller returns success, and `GardenGridPanel` updates the tile so the user can see the new plant in the garden.

#### Note

This activity diagram shows a typical interaction chain of user input, controller validation, domain update, and UI feedback.

### 2. `activity_other_business_plant_removal_and_manual_actions`

Related files:

- `activity_other_business_plant_removal_and_manual_actions.puml`
- `activity_other_business_plant_removal_and_manual_actions.png`

#### Scenario

This diagram describes a manual cleanup action such as `Clear All Plants`. It applies when the user wants to remove all plants from the garden at once.

#### General Flow

1. The user clicks `Clear All`.
2. The UI iterates through all grid positions and builds a `GridPosition` for each cell.
3. `GardenController` calls `removePlant(GridPosition)` for each position.
4. `GardenPlot` removes existing plants from `plantMap`, updates the total and living counts, and finds the corresponding garden zone.
5. `GardenZone` removes the deleted plant from its zone list.
6. The GUI calls `updateAllTiles()` and restores the full grid to empty grass tiles.

#### Note

This diagram represents a batch cleanup workflow, which is essentially a repeated plant-removal operation across the entire garden.

### 3. `activity_other_business_simulation_controls`

Related files:

- `activity_other_business_simulation_controls.puml`
- `activity_other_business_simulation_controls.png`

#### Scenario

This diagram describes user control over the simulation process, including start, pause, resume, stop, and speed adjustment.

#### General Flow

- **Start**
  The user clicks Start, the controller calls `startSimulation()`, and the simulation engine checks whether there are living plants. If there are, it sets the state to `RUNNING` and starts the timeline. Otherwise, the UI shows a start error.
- **Pause**
  The user clicks Pause, the controller calls `pauseSimulation()`, and the engine enters the `PAUSED` state and pauses the timeline. The UI changes the button label to `Resume`.
- **Resume**
  The user clicks Resume, the controller calls `resumeSimulation()`, and the engine returns to the `RUNNING` state and resumes the timeline. The UI changes the button label back to `Pause`.
- **Stop**
  The user clicks Stop, the controller calls `stopSimulation()`, and the engine stops the timeline, enters the `STOPPED` state, records final statistics, and resets the toolbar state.
- **Change Speed**
  The user changes the simulation speed, the controller passes the multiplier to the engine, and the engine validates the value and updates the timeline rate. The UI shows the new speed.

#### Note

This diagram focuses on simulation lifecycle state transitions and shows how the user controls the execution pace of the system.

### 4. `activity_simulation_engine_overview`

Related files:

- `activity_simulation_engine_overview.puml`
- `activity_simulation_engine_overview.png`

#### Scenario

This is the high-level runtime overview of the simulation engine. It is the most important activity diagram for showing how the system operates from simulation start to repeated tick-based execution.

#### General Flow

1. The user clicks Start and the controller calls `startSimulation()`.
2. `SimulationEngine` validates the current state and checks whether the garden contains living plants.
3. If the simulation can start, the engine sets the state to `RUNNING` and starts the JavaFX Timeline.
4. During each simulation tick, the engine advances simulation time and triggers the following processes in parallel:
   - plant state update,
   - automatic watering,
   - heating update,
   - cooling update,
   - pest control update,
   - weather update.
5. After the subsystem updates, the engine handles automatic resource refill, updates the living plant count, and increments `dayCounter` when a simulated day is completed.
6. When a day is completed, all plants run `advanceDay()`.
7. The UI is refreshed, including the garden grid, weather display, logs, and side panels.
8. When the simulation is no longer in the `RUNNING` state, the engine stops the timeline and records simulation statistics.

#### Note

This diagram shows the tick-driven execution model of the system and highlights the role of `SimulationEngine` as the central coordinator.

### 5. `activity_simulation_pest_control_system`

Related files:

- `activity_simulation_pest_control_system.puml`
- `activity_simulation_pest_control_system.png`

#### Scenario

This diagram describes how the pest control subsystem spawns pests, applies damage, evaluates threat levels, and performs pesticide treatment during the simulation.

#### General Flow

1. `SimulationEngine` calls `pestControlSystem.update()`.
2. `PestControlSystem` first removes invalid pest references.
3. If the spawn condition is satisfied, the system selects a living plant and creates a `HarmfulPest` at that plant's `GridPosition`.
4. When a new pest is spawned, `PestEventBridge` notifies the GUI so the UI can display the pest appearance effect.
5. The system iterates through all current pests and applies damage to their target plants. Invalid pests are removed.
6. The system then iterates through all `GardenZone` objects and evaluates the threat level of each zone.
7. If a zone reaches `HIGH` or `CRITICAL` threat:
   - pesticide is applied,
   - pests in the zone are eliminated,
   - pesticide stock is reduced,
   - infestation level is updated.
8. At the same time, plants in the zone reduce accumulated pest attacks and recover health, while the UI is notified of pesticide application.
9. Finally, the system recomputes infestation levels for all zones.

#### Note

This diagram shows that pest control is a closed-loop process of discovering risk, accumulating threat, and automatically applying treatment.

### 6. `activity_simulation_temperature_systems`

Related files:

- `activity_simulation_temperature_systems.puml`
- `activity_simulation_temperature_systems.png`

#### Scenario

This diagram describes how the heating and cooling systems work together during simulation. It is used to explain how the system automatically adjusts environmental temperature based on sensor readings.

#### General Flow

1. `SimulationEngine` calls `heatingSystem.update()` and `coolingSystem.update()`.
2. The two subsystems run in parallel:
   - **HeatingSystem**
     Reads temperature sensors, calculates average temperature, and if the value is below the target minimum, selects an appropriate heating mode and increases zone temperatures. If the temperature is already stable, heating is turned off.
   - **CoolingSystem**
     Reads temperature sensors, calculates average temperature, and if the value exceeds plant tolerance, selects an appropriate cooling mode and decreases zone temperatures. Otherwise, cooling is turned off.
3. After heating or cooling actions, plant objects apply the temperature effect based on their zone conditions.

#### Note

This diagram emphasizes both automation and parallelism in temperature control, and it shows the direct role of sensor data in system decisions.

### 7. `activity_simulation_watering_system`

Related files:

- `activity_simulation_watering_system.puml`
- `activity_simulation_watering_system.png`

#### Scenario

This diagram describes the automatic watering subsystem. It explains how the system decides whether watering should happen based on weather, sensor data, plant needs, and water supply.

#### General Flow

1. `SimulationEngine` calls `wateringSystem.checkAndWater()`.
2. `WateringSystem` reads the current weather.
3. If the weather is rainy, the watering cycle is skipped and active sprinklers are stopped if needed.
4. If the water supply is below the minimum threshold, a low-water warning is logged and the watering cycle stops.
5. Otherwise, the system iterates through all `GardenZone` objects.
6. For each zone:
   - the moisture sensor is read,
   - if the sensor is in error state, the zone is skipped,
   - otherwise the system checks whether the zone contains living plants that need water.
7. If a zone needs watering, the system activates the corresponding sprinkler, while:
   - `Sprinkler` distributes water to plants,
   - `GardenZone` increases zone moisture.
8. After watering, the system reduces the total water supply, deactivates the sprinkler, and logs the watering event.

#### Note

This diagram shows that automatic watering is a decision process influenced by weather, hardware state, plant demand, and resource availability.

### 8. `activity_simulation_weather_and_pest`

Related files:

- `activity_simulation_weather_and_pest.puml`
- `activity_simulation_weather_and_pest.png`

#### Scenario

This diagram describes the interaction between the weather system and the pest control system within the same simulation cycle. It is useful for showing how environmental change and risk management happen at the same time.

#### General Flow

1. `SimulationEngine` calls `weatherSystem.update()` and `pestControlSystem.update()` in parallel.
2. In the weather branch:
   - the system checks whether weather rotation is needed,
   - if needed, it generates or switches to the next weather state and updates garden weather,
   - if weather changes, it also updates the ambient temperature baseline used by the heating and cooling systems.
3. If the current weather is rainy, zone moisture increases and rainy weather effects are applied to plants.
4. If the weather is not rainy, plants receive the current weather effect and zone moisture evaporates during sunny periods.
5. In the pest branch:
   - if the random spawn condition is satisfied, a pest is created near a living plant and the UI is notified through `PestEventBridge`,
   - pest damage is applied to plants,
   - threat levels are assessed for each zone,
   - high-risk zones receive automatic pesticide treatment.
6. After treatment, plant recovery, infestation reduction, and UI notification happen in parallel.

#### Note

This diagram highlights the parallel nature of two environment-related subsystems: weather affects growing conditions, while pests affect plant health, and together they shape the simulation outcome.


