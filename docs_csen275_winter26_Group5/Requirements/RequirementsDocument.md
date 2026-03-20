# Smart Garden Simulation - Requirements Document

## 1. Purpose
This document defines the requirements for the Smart Garden Simulation System, covering functional requirements (FRs), non-functional requirements (NFRs), assumptions, constraints, and key implementation-aligned notes.

## 2. System Overview
The system simulates an automated computerized garden containing:
1. A bounded grid of cells where plants can be placed.
2. A set of logical management zones used by automation systems.
3. Automated subsystems:
   - Watering System (moisture-driven sprinklers)
   - Heating System (temperature-driven warm-up)
   - Cooling System (temperature-driven cool-down)
   - Pest Control System (harmful pest spawn, damage, and treatment)
4. A Weather System (stochastic weather state with environmental effects).
5. A Simulation Engine that advances time in discrete ticks and updates all subsystems.
6. A JavaFX UI that visualizes the simulation in real time and displays system status and an event log.
7. An optional API mode for programmatic control and headless simulation.

## 3. Stakeholders
- Home gardeners: want simplified, automated “what-if” garden management (educational context).
- Course staff / evaluators: need a system that demonstrates modular OOP design, simulation logic, and documentation.
- Students / developers: extend plant types, systems, and behaviors.
- “External scripts” (API users): drive the simulation via programmatic events (rain, temperature, pests).

## 4. Scope
### In Scope
- Grid-based garden layout with configurable dimensions.
- Plant placement and removal in empty cells.
- Simulation of plant lifecycle and state changes driven by time, resources, pests, and weather.
- Automated watering by zones with moisture-driven activation and rain-aware suppression.
- Heating and cooling driven by sensor readings and configurable thresholds/plant-safe limits.
- Harmful pest infestation simulation and treatment when threat levels are high.
- Weather simulation with stochastic transitions and plant/zone environmental effects.
- Real-time UI visualization, simulation controls, and event log display.
- Event logging with timestamps and categories, written to per-session log files.
- Optional API-driven control and headless simulation loop.

### Out of Scope
- Real physical hardware integration (no actual sensors/actuators).
- Machine learning or advanced AI optimization.
- Database-backed persistence of garden state across runs.
- Multi-user accounts, permissions, or concurrent multi-garden sessions.
- Cloud synchronization or external data services.

## 5. Definitions
- Tick: discrete simulation step; for the JavaFX `SimulationEngine`, 1 tick corresponds to 1 minute of simulation time (real-time interval is configured by the engine’s base tick duration and speed multiplier).
- Day: simulation day boundary used to advance plant lifecycle stages and day counters.
- Zone: logical subdivision grouping grid cells for localized sensor/control logic (automation systems operate per zone).
- Plant State: includes health (0–100), water level (0–100), growth stage, days alive, and dead/alive status.

## 6. Assumptions
This section consolidates the project assumptions.
- Users have Java 21+ available to run JavaFX applications.
- Users have basic GUI literacy (clicking/selecting/scrolling).
- Gardening concepts (water/temperature/pests) are understood at a basic educational level.
- Garden state is in-memory only; no save/load persistence is required.
- Probability-based events (weather, pests) will appear plausible over longer runs.
- The simulation model is simplified (not botanically accurate).

## 7. Constraints
### Technical Constraints
- Java 21 for implementation.
- JavaFX 23.0.1 for UI.
- Maven as the build system.
- No external database systems.

### Performance / Operational Constraints
- Target smooth operation up to 10x speed.
- UI update responsiveness should be within 100ms of relevant state changes (best-effort via UI polling/timers).
- Must operate reliably for 24+ hours without crashing (exceptions caught and logged).

## 8. Functional Requirements

### 8.1 Garden Management (FR-G)
- FR-G1: The system shall support a grid-based garden layout with configurable positive row and column dimensions.
- FR-G2: The system shall allow placing a plant into any empty cell.
- FR-G3: The system shall ensure at most one plant per grid cell.
- FR-G4: The system shall partition the garden into zones for localized automation. Implementation note: zones are currently fixed to a 3x3 zone arrangement (9 zones) derived from grid dimensions by integer division; remainder cells are absorbed by the last zone in each zone-row/zone-col.

### 8.2 Plant Lifecycle & State (FR-P)
- FR-P1: The system shall support 9 selectable plant types.
  - 3 Fruits: Strawberry, Cherry, Apple Sapling
  - 3 Vegetables: Cabbage, Tomato, Scallion
  - 3 Flowers: Daisy, Lily, Peony
- FR-P2: Each plant shall maintain observable state including:
  - Growth stage
  - Health level (0–100%)
  - Water level (0–100%)
  - Days alive
  - Alive/dead status
- FR-P3: Each plant shall progress through lifecycle stages in order:
  - Seed -> Seedling -> Mature -> Flowering -> Fruiting
- FR-P4: Plants shall advance growth stages at day boundaries.
- FR-P5: Plants shall die when health reaches 0 or when days alive reaches the plant’s maximum lifespan.
- FR-P6: Plant health shall be affected by:
  - Water stress/dehydration (water level depletion over ticks)
  - Temperature stress (outside plant min/max temperature)
  - Pest attacks (pest-driven damage and post-treatment reduction of attack intensity)
  - Weather effects (via weather-driven bonuses/penalties)

### 8.3 Watering System (FR-W)
- FR-W1: The system shall provide a watering automation module operating per zone.
- FR-W2: The system shall instantiate one sprinkler and one moisture sensor per zone.
- FR-W3: On each simulation tick, the watering system shall check whether watering should occur:
  - It shall skip watering when the current weather is Rainy.
  - It shall trigger watering when zone plants require water (plant water level is below its water requirement), subject to available water supply and sensor status not being ERROR.
- FR-W4: The system shall support manual watering override for a specified zone.
- FR-W5: The system shall track water consumption by decreasing the system water supply by the actual distributed amount.
- FR-W6: The system shall stop all active sprinklers when rain begins (when WeatherSystem transitions into Rainy).

### 8.4 Heating System (FR-H)
- FR-H1: The system shall monitor ambient temperature and compute the current garden temperature (implementation note: average across zone sensors).
- FR-H2: The system shall activate heating when computed temperature falls below a configured minimum threshold.
- FR-H3: The system shall deactivate heating when computed temperature exceeds a configured maximum threshold (including hysteresis to avoid rapid cycling).
- FR-H4: Temperature changes shall affect plant growth/health via plant temperature effect logic.
- FR-H5: Users (and API mode) shall be able to set ambient temperature for weather simulation by using system methods (internally applied as zone temperatures).

### 8.5 Cooling System (FR-C)
- FR-C1: The system shall activate cooling when computed temperature exceeds the highest `maxTemperature` threshold among living plants.
- FR-C2: Cooling shall use a hysteresis rule to deactivate when temperature returns sufficiently below the threshold.
- FR-C3: Cooling shall decrease zone temperatures according to cooling modes (LOW/MEDIUM/HIGH) and apply temperature effects to plants.
- FR-C4: The system shall track cooling energy consumption by accumulated mode-driven decreases.

### 8.6 Pest Control System (FR-Pest)
- FR-Pest1: The system shall simulate harmful pest infestations.
- FR-Pest2: In non-API mode, pests shall spawn stochastically with a configured probability per update tick, choosing a random living plant position.
- FR-Pest3: In API mode, random spawning shall be disabled; pests shall only be introduced by external calls that register pests.
- FR-Pest4: The system shall apply pest damage to target plants each tick while pests are alive and the target plant exists.
- FR-Pest5: The system shall maintain zone infestation levels derived from active harmful pest counts and zone plant counts.
- FR-Pest6: The system shall determine threat level per zone and trigger treatment when the threat is HIGH or CRITICAL.
  - Implementation note: treatment is delayed briefly (about 3 seconds) to allow UI pest activity visibility; headless falls back to scheduling without JavaFX.
- FR-Pest7: The system shall eliminate pests in the treated zone, reduce ongoing pest attack intensity for affected plants, and decrement pesticide stock by a fixed cost per treatment.
- FR-Pest8: The system shall support manual pest treatment override for a specified zone.

### 8.7 Simulation Engine (FR-Sim)
- FR-Sim1: The system shall advance the simulation using discrete time steps (tick-based).
- FR-Sim2: The tick loop shall:
  - Update plant states
  - Perform watering, heating, cooling, pest control, and weather updates
  - Trigger automatic supply refills to avoid stalling during long runs
- FR-Sim3: The system shall support start, pause, resume, and stop controls.
- FR-Sim4: The system shall support variable simulation speed multipliers (1x to 10x).
- FR-Sim5: The system shall expose simulation state and time counters for UI display.
- FR-Sim6: Implementation note (consistency): UI `SimulationEngine` advances days using 1440 ticks/day, while `HeadlessSimulationEngine` currently uses a different ticks/day constant (60). The functional intent is “minute-based ticks” with consistent day boundaries; tests should verify the intended behavior.

### 8.8 Weather System (FR-Wx)
- FR-Wx1: The system shall support 5 weather conditions: Sunny, Rainy, Cloudy, Windy, Snowy.
- FR-Wx2: In non-API mode, the weather shall change stochastically based on probabilities and remain active for a random duration within configured bounds.
- FR-Wx3: Weather shall affect:
  - Plant health and water through plant-specific weather effect logic
  - Zone moisture through evaporation (+/-) behavior
  - Heating/cooling ambient temperature by setting targets for certain weather conditions (Sunny/Rainy/Snowy).
- FR-Wx4: The system shall support an API override mode that disables automatic weather changes (weather effects still apply).
- FR-Wx5: The system shall support optional real-time rotation mode (Sunny -> Rainy -> Snowy) for demo/testing.

### 8.9 User Interface (FR-UI)
- FR-UI1: The UI shall display the garden grid and real-time plant visualizations.
- FR-UI2: The UI shall provide simulation control buttons: Start, Pause/Resume, Stop.
- FR-UI3: The UI shall support speed selection within supported speed multipliers.
- FR-UI4: The UI shall display current system status for:
  - Weather
  - Water supply and irrigation-related indicators
  - Heating and cooling status (mode and current temperature)
  - Pest threat indicators (including critical threat positions)
  - Pesticide stock
- FR-UI5: The UI shall display elapsed simulation time (day counter and formatted timestamp).
- FR-UI6: The UI shall provide interactive plant selection (click/hover) to display plant information.
- FR-UI7: The UI shall display an event log panel showing recent log entries.

### 8.10 Logging System (FR-Log)
- FR-Log1: The system shall log significant events with timestamps.
- FR-Log2: Log entries shall include a category (e.g., Watering, Heating, PestControl, Simulation) and a log level (DEBUG/INFO/WARNING/ERROR).
- FR-Log3: The system shall write log entries to a per-session file under the `logs/` directory.
- FR-Log4: The UI shall display a list of recent log entries in a scrollable panel.
- FR-Log5: The system shall support in-memory retrieval and filtering by category (UI may selectively implement filtering).
- FR-Log6: When API logging is enabled, the system shall mirror API-driven events to a separate API log file.

### 8.11 API (FR-API)
- FR-API1: The system shall provide a programmatic API (`GardenSimulationAPI`) to initialize a garden and run a headless simulation loop.
- FR-API2: The API shall provide script-friendly methods:
  - `rain(int amount)`
  - `temperature(int tempFahrenheit)` (with clamping to valid range)
  - `parasite(String parasiteType)` (case-insensitive)
  - `getPlants()` (plant metadata and associated pest vulnerabilities)
  - `getState()` (snapshot report written to logs)
- FR-API3: API calls shall trigger relevant subsystem updates (heating/cooling/watering/pest control) to reflect state changes.
- FR-API4: The API shall support loading initial plants from `garden-config.json` when available.
- FR-API5: The API shall load pest vulnerability mapping from `parasites.json` when available.

## 9. Non-Functional Requirements
### NFR-1 Performance
- NFR-1.1: The simulation shall run smoothly at speeds up to 10x without noticeable stuttering/lag.
- NFR-1.2: UI updates should reflect state changes quickly (target best-effort within 100ms).
- NFR-1.3: Memory usage shall remain stable during 24+ hour runs.

### NFR-2 Reliability & Robustness
- NFR-2.1: The system shall run continuously for at least 24 hours without crashes under normal operation.
- NFR-2.2: Exceptions thrown in subsystems shall be caught and logged without terminating the application.
- NFR-2.3: Invalid user/API inputs shall be handled gracefully via validation and clamping where applicable.

### NFR-3 Maintainability
- NFR-3.1: Code shall follow object-oriented design principles with clear responsibilities.
- NFR-3.2: Public classes and methods shall include JavaDoc-style documentation.
- NFR-3.3: Package structure shall separate concerns by layer/module.

### NFR-4 Usability
- NFR-4.1: Basic UI operations shall be understandable within 5 minutes for first-time users.
- NFR-4.2: UI controls shall be labeled clearly and provide immediate visual feedback.
- NFR-4.3: Error messages shall be clear and actionable.

### NFR-5 Portability
- NFR-5.1: The application shall run on Windows, macOS, and Linux with supported JavaFX setup.
- NFR-5.2: File paths and resources shall use cross-platform conventions (where applicable).

## 10. Implementation-Aligned Notes (Open Items)
The following items are included to prevent requirement/code misunderstandings:
- Grid defaults: UI defaults to an 8x14 grid, while documentation templates may reference 9x9. The domain model supports configurable rows/columns.
- Zone layout: zone partition is currently fixed to a 3x3 arrangement (9 zones), derived from rows/columns via integer division.
- Growth stages: implementation includes an additional terminal stage “Fruiting” beyond a Flowering-only lifecycle template.
- Headless day boundary: `HeadlessSimulationEngine` currently uses a ticks/day constant that differs from `SimulationEngine`. Verify intended behavior for grading/tests.

## 11. Acceptance Criteria (System-Level)
The system is considered meeting this requirements document when:
1. Users can place plants, start/pause/resume/stop the simulation, and observe real-time plant/system updates.
2. Automated watering/heating/cooling/pest control respond correctly to weather and resource thresholds.
3. Weather transitions are stochastic (non-API mode) and affect plants and zone moisture as specified.
4. Event logging produces timestamped categorized entries in UI and per-session log files.
5. API mode can initialize a garden, run headless simulation, and apply rain/temperature/pests while producing traceable logs.

