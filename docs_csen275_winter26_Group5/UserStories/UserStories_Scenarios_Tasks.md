<!-- [MermaidChart: 6dbd52cf-93bb-40b8-a6ae-8fa44fc1d7b3] -->
# 5.2 User Stories / Scenarios / Tasks

## 5.2.1 Roles and Scope
This section describes the requirements from both a user perspective and a development delivery perspective.

Primary actors:
- `Garden Manager`: uses the JavaFX UI (select plants, click tiles, start/pause/stop the simulation, view logs).
- `API Client`: external scripts/programs that drive headless simulation via `GardenSimulationAPI`.

Note: UI-visible actions follow the current code implementation. If an override exists only in the controller/API (and not as a UI button), it will still be reflected in the development tasks.

## 5.2.2 User Stories (with Scenarios / Acceptance Criteria)

### UI-01: Select Plant Type and Plant in an Empty Cell
As a `Garden Manager`, I want to select a plant type and click an empty cell to plant it, so that the simulated plant can start growing.

Acceptance Criteria:
- The plant selector provides 9 supported plant types: Strawberry/Cherry/Apple Sapling, Cabbage/Tomato/Scallion, Daisy/Lily/Peony.
- If the clicked cell is empty, planting succeeds and the cell displays the plant; subsequent simulation ticks update the plant state.
- If the clicked cell is already occupied, no new plant is placed in that cell; instead the UI shows information for the existing plant (see UI-02).

Scenario:
```text
Given I selected a plant type
And I clicked a grid cell that currently has no plant
When the UI calls controller.plantSeed
Then the cell displays the planted object (tile becomes visible)
And the plant initializes in the initial growth stage (Seed)
And the system logs the planting event ("Plant added ...")
```

### UI-02: Hover/Click to View Plant Information
As a `Garden Manager`, I want to hover or click a plant to view its information, so that I can quickly understand the current health and resource status.

Acceptance Criteria:
- Hovering displays a tooltip containing plant type, Health, Water, Age (Days Alive), and Stage.
- Clicking a plant opens a detail dialog containing Growth Stage, Health (including health-status text), Water (including requirement), Days Alive, Total Pest Attacks, Active Pests, and Status.
- Active pest count is obtained from `PestControlSystem.getActivePestCountAtPosition`.

Scenario:
```text
Given a grid cell contains a plant
When I hover over the tile
Then the tooltip shows the plant’s key state fields
When I click the tile
Then the "Plant Information" dialog is displayed
And the dialog includes Active Pests (queried from PestControlSystem)
```

### UI-03: Clear All Plants
As a `Garden Manager`, I want to click "Clear All" to remove all planted plants, so that I can start a new simulation experiment.

Acceptance Criteria:
- After clicking "Clear All", all cells return to the plantable empty state (grass tile visible).
- The internal garden model clears plants (via `controller.removePlant` calls).

Scenario:
```text
Given the garden contains any number of plants
When I click "Clear All"
Then all cells stop displaying plants
And I can plant again by clicking an empty cell
```

### UI-04: Start, Pause, and Resume the Simulation
As a `Garden Manager`, I want to start the simulation, pause it, and resume it, so that I can observe system behavior over time.

Acceptance Criteria:
- `Start`: the simulation can start only when the garden has at least one living plant; otherwise the simulation engine blocks start (exception handling is performed in the controller/UI flow).
- `Pause`: pausing stops the timeline tick; UI state freezes (plants/resources do not change due to ticks).
- `Resume`: resumes from the exact paused time/state.
- The Pause button text toggles between "Pause" and "Resume".

Scenario:
```text
Given the garden has at least one living plant
When I click "Start"
Then simulationEngine.start() is called
And the UI begins polling updates every 0.5s
When I click "Pause"
Then timeline.pause() is called
And the simulation state becomes PAUSED (reflected in UI status)
When I click "Resume"
Then timeline.play() is called
And the simulation state becomes RUNNING
```

### UI-05: Stop the Simulation
As a `Garden Manager`, I want to stop the simulation and end the current run, so that I can finish the experiment and inspect final logs.

Acceptance Criteria:
- `Stop` calls `controller.stopSimulation()`, which stops the timeline and triggers final statistics logging.
- The UI Start button becomes available again; the Pause button becomes disabled or remains unavailable (depending on current UI behavior).
- UI no longer changes due to tick updates.

Scenario:
```text
Given the simulation is running
When I click "Stop"
Then simulationEngine.stop() is called
And final statistics are recorded to logs
And UI stops changing due to ticks
```

### UI-06: Change Simulation Speed
As a `Garden Manager`, I want to change the simulation speed multiplier during a run (e.g., 1x/2x/5x/10x), so that I can observe long-term outcomes faster.

Acceptance Criteria:
- The speed dropdown contains 1x, 2x, 5x, and 10x.
- Speed changes take effect immediately via `SimulationEngine.setSpeed` (timeline rate update).
- The UI and logs reflect the updated speed.

Scenario:
```text
Given the simulation is RUNNING
When I change speed from 1x to 10x
Then controller.setSimulationSpeed is called
And the simulation timeline rate becomes 10
And the system continues producing events and updating the UI
```

### UI-07: View Real-Time Event Logs
As a `Garden Manager`, I want to view event logs in real time, so that I can understand watering, heating/cooling, pest activity, and weather changes.

Acceptance Criteria:
- The UI log panel displays recent events by polling `Logger.getRecentLogs(20)` and updating the ListView.
- A "Clear logs" button clears the UI list view.
- "Pause Auto-Scroll" toggles auto-scrolling behavior.

Scenario:
```text
Given the simulation is started
When the system triggers an event (e.g., an Auto-watered event)
Then the log list shows the entry on the next UI polling refresh
And the list auto-scrolls to the latest entry when autoScrollLogs=true
```

## API Stories

### API-01: Initialize Garden and Enter API-Driven Mode (initializeGarden)
As an `API Client`, I want to call `initializeGarden()` so that it loads initial plants and starts the headless simulation loop.

Acceptance Criteria:
- `initializeGarden()` attempts to load `/garden-config.json`; if missing, it uses fallback plants.
- API mode enables apiModeEnabled in Heating/Cooling/Weather/PestControl to avoid conflicts with random/automatic behaviors.
- `startHeadlessSimulation()` starts the headless engine (precondition: the garden has at least one living plant).
- API-related actions write traceable logs (via `Logger.enableApiLogging`).

Scenario:
```text
Given I created a GardenSimulationAPI(api) instance
When I call api.initializeGarden()
Then the garden is initialized with several plants
And relevant subsystems enter apiModeEnabled=true
And the headless simulation loop starts running
```

### API-02: Trigger a Rain Event (rain)
As an `API Client`, I want to call `rain(amount)` so that the weather becomes Rainy and plants receive water.

Acceptance Criteria:
- `rain(amount)` sets WeatherSystem current weather to `RAINY`.
- For each living plant, it executes `plant.water(amount)` and applies Rainy weather effects (`applyWeatherEffect("RAINY")`).
- Subsystems are updated to reflect state changes.
- dayCount is incremented by 1.

Scenario:
```text
Given there are living plants in the garden
When I call api.rain(50)
Then WeatherSystem.currentWeather becomes RAINY
And plants’ waterLevel increases (and Rainy weather effects are applied)
And watering/heating/pest control-related subsystems reflect the update
And dayCount++
```

### API-03: Trigger a Temperature Event (temperature)
As an `API Client`, I want to call `temperature(tempF)` so that ambient temperature changes and affects plant health.

Acceptance Criteria:
- Input temperature is clamped to the valid range 40–120°F (out-of-range values trigger a warning and are clamped).
- Fahrenheit is converted to Celsius.
- Heating/Cooling ambient temperature is updated via `setAmbientTemperature`.
- For each living plant (not dead), `applyTemperatureEffect(tempC)` is executed.
- Subsystems are updated, and dayCount is incremented.

Scenario:
```text
Given I call api.temperature(10) (below 40°F)
When the method executes
Then temperature is clamped to 40°F
And all living plants apply temperature effects using the clamped value
And dayCount++
```

### API-04: Trigger a Parasite Infestation (parasite)
As an `API Client`, I want to call `parasite(type)` so that the garden generates the specified harmful pest type and the system handles the consequences.

Acceptance Criteria:
- Input is case-insensitive.
- The method looks up vulnerabilities mapping based on plant type (from `parasites.json`; if it fails, it falls back to defaults).
- For plants that match vulnerabilities, it creates `HarmfulPest` and registers it with PestControlSystem.
- Subsystems are updated, and dayCount is incremented.

Scenario:
```text
Given some plants are vulnerable to the given parasiteType according to the vulnerabilities mapping
When I call api.parasite("Red Mite")
Then harmful pests are created and registered at those plant positions
And PestControlSystem applies damage / updates threat levels on the next update cycle
And dayCount++
```

### API-05: Query Plant Info and Current State (getPlants / getState)
As an `API Client`, I want to query the current garden information for automated monitoring/debugging.

Acceptance Criteria:
- `getPlants()` returns metadata for current living plants, including plant types, water requirements, and the parasites/vulnerabilities structure.
- `getState()` writes a snapshot report to logs, including living/dead/total counts and per-plant health/water summaries.

Scenario:
```text
Given I have initialized and started the simulation
When I call api.getPlants()
Then the returned structure includes plants/waterRequirement/parasites
When I call api.getState()
Then the logs include "Garden State Report - Day ..." and one status line per plant
```

## 5.2.3 Tasks (Development Breakdown)
Tasks decompose the system behaviors delivered in Section 5.2. The team can assign tasks by module.

### UI / Presentation Tasks
- `T-UI-01`: Implement plant selection and planting interaction (`GardenGridPanel.setupPlantSelector` + click handling that calls `controller.plantSeed`).
- `T-UI-02`: Implement the empty-cell vs occupied-cell click behavior (occupied cells show tooltip/dialog instead of planting again).
- `T-UI-03`: Implement hover tooltip (`showHoverTooltip`) and plant details dialog on click (`showPlantTooltip`).
- `T-UI-04`: Implement the "Clear All" button (`clearGarden()` loops over cells, calls `controller.removePlant`, and refreshes tiles).
- `T-UI-05`: Implement simulation control buttons: Start / Pause (toggle) / Stop (wired in `SmartGardenApplication.setupToolbarActions` and controller methods).
- `T-UI-06`: Implement the speed dropdown (`ModernToolbar.speedComboBox` -> `controller.setSimulationSpeed`).
- `T-UI-07`: Implement the event log panel refresh and auto-scroll control (UI polls Logger recent logs and refreshes the ListView).
- `T-UI-08`: Implement PestEventBridge UI animation integration (pest spawn and pesticide application trigger tile animations via `PestEventBridge` + `GardenGridPanel.onPestSpawned / onPesticideApplied`).

### Domain / Simulation Tasks
- `T-SIM-01`: Implement the discrete tick main loop (`SimulationEngine.tick`: plant.update + watering/heating/cooling/pest/weather updates).
- `T-SIM-02`: Implement the day boundary and day counter progression (`ticksPerDay` threshold -> `advanceDay`).
- `T-SIM-03`: Implement long-run supply auto-refill policies (watering water amount and pest pesticide stock refills).
- `T-SIM-04`: Implement the headless tick loop (`HeadlessSimulationEngine` interacting with the same systems).

### Watering System Tasks
- `T-W-01`: Initialize per-zone sprinkler and moisture sensor (`WateringSystem.initializeSprinklersAndSensors`).
- `T-W-02`: Implement rain-aware watering suppression (skip watering when WeatherSystem is RAINY; stop active sprinklers on RAINY transitions).
- `T-W-03`: Implement watering distribution (respect available water supply, distribute to living plants in the zone, and update zone moisture).

### Heating / Cooling Tasks
- `T-TEMP-01`: Implement heating logic (zone temperature monitoring, hysteresis, switch LOW/MEDIUM/HIGH based on thresholds, and apply plant temperature effects via `HeatingSystem.update`).
- `T-TEMP-02`: Implement cooling logic (activate/deactivate based on living plants’ `maxTemperature` threshold; decrease temperatures and track energy via `CoolingSystem.update`).
- `T-TEMP-03`: Provide API/Weather-driven ambient temperature updates (`setAmbientTemperature` in Heating/Cooling).

### Pest Control Tasks
- `T-PEST-01`: Implement non-API mode random pest spawning (probability-triggered, choose target position from living plants, generate harmful types).
- `T-PEST-02`: Implement per-tick pest damage application (`pest -> plant.pestAttack` via `causeDamage`).
- `T-PEST-03`: Implement threat assessment and treatment trigger logic (`ThreatLevel`; enter treatment at HIGH/CRITICAL).
- `T-PEST-04`: Implement treatment actions (consume pesticide stock, eliminate pests, reduce plant `pestAttacks`, and update zone infestation).

### Weather System Tasks
- `T-WX-01`: Implement the stochastic weather state machine (Sunny/Cloudy/Windy/Rainy/Snowy) and weather duration handling.
- `T-WX-02`: Implement weather effects on plants and zone moisture (via `Plant.applyWeatherEffect` + `zone.updateMoisture/evaporate`).
- `T-WX-03`: Implement API override mode (disable automatic weather changes while keeping weather effects; API entry is `WeatherSystem.setWeather`).

### Logging / API Tasks
- `T-LOG-01`: Implement Logger level gating and per-session file persistence (`Logger.log/info/warning/error` + flush strategy).
- `T-LOG-02`: Implement API mode log mirroring (`Logger.enableApiLogging / disableApiLogging`).
- `T-API-01`: Implement `GardenSimulationAPI.initializeGarden` (load `garden-config.json` and `parasites.json`, enable api mode for subsystems, start headless simulation).
- `T-API-02`: Implement API event interfaces: `rain` / `temperature` / `parasite` (trigger subsystem updates and increment `dayCount`).
- `T-API-03`: Implement API query interfaces: `getPlants` / `getState` (return structures and/or write snapshot logs).

