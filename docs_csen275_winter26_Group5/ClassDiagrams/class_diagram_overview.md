## Class Diagram

Related files:

- `class_diagram.mmd`
- `class_diagram.png`

### 1. System Architecture Overview

The class diagram describes the core structure of the smart garden GUI simulation system. Overall, the system can be understood in four layers:

1. **GUI Layer**
   This layer is responsible for user interaction, interface presentation, and animation feedback. It mainly includes `SmartGardenApplication`, `GardenGridPanel`, `ModernToolbar`, `InfoPanel`, and `PestEventBridge`.
2. **Controller Layer**
   This layer translates user actions into business operations. The central class is `GardenController`.
3. **Simulation Layer**
   This layer drives the runtime behavior of the system. The core class is `SimulationEngine`, which coordinates the watering, temperature, weather, and pest subsystems.
4. **Domain Layer**
   This layer represents the actual garden objects and states. It mainly includes `GardenPlot`, `GardenZone`, `Plant` and its subclasses, `Sensor`, `Sprinkler`, and `Pest`.

The typical call path is:

`User -> GUI -> GardenController -> GardenPlot / SimulationEngine -> Subsystems -> GUI refresh`

This structure shows a clear separation of responsibilities. The GUI layer does not directly handle complex business logic, the controller layer dispatches requests, the simulation engine performs periodic updates, and the domain objects store and manage state.

### 2. Role of Each Core Class

#### `SmartGardenApplication`

- The entry point of the system and the JavaFX application launcher.
- Creates the main scene and initializes the controller and panels.
- Organizes the toolbar, garden grid, and side information panels.

#### `GardenController`

- Acts as the bridge between the GUI and the underlying business logic.
- Handles planting, plant removal, simulation start/pause/resume/stop, and speed adjustment.
- Holds references to `GardenPlot` and `SimulationEngine`, so it is the main entry point for user-triggered actions.

#### `GardenGridPanel`

- Displays the garden grid.
- Updates the visual state of each tile, such as plants, empty ground, and pest animations.
- Builds `GridPosition` objects from user clicks and delegates actions to the controller.

#### `ModernToolbar`

- Manages the simulation control buttons and status display.
- Supports start, pause, stop, and speed selection.
- Serves as the main interface component for simulation lifecycle control.

#### `InfoPanel`

- Displays overall garden information and selected plant information.
- Examples include plant counts, weather state, pest distribution, and selected plant details.

#### `PestEventBridge`

- Transfers pest-related events from the simulation subsystem to the GUI.
- Notifies the UI when pests appear or when pesticide is applied.
- Reduces direct coupling between `PestControlSystem` and `GardenGridPanel`.

#### `SimulationEngine`

- The central scheduler of the entire simulation system.
- Manages runtime states such as `RUNNING`, `PAUSED`, and `STOPPED`.
- Calls each subsystem during time progression and triggers UI refreshes.
- Maintains global simulation information such as day count, speed, and statistics.

#### `WeatherSystem`

- Manages weather state and weather rotation logic.
- Decides when weather changes and propagates weather effects to the garden, plants, and temperature systems.
- Weather influences moisture, temperature, and plant condition.

#### `WateringSystem`

- Manages the automatic watering logic.
- Reads moisture sensor values, checks weather and water supply, and decides whether a zone should be watered.
- Uses `Sprinkler` objects to distribute water to target zones.

#### `HeatingSystem`

- Controls temperature increase when the environment becomes too cold.
- Reads temperature sensor values and decides whether heating should be enabled and at what intensity.

#### `CoolingSystem`

- Controls temperature reduction when the environment becomes too hot.
- Works similarly to the heating system, but prevents plants from being damaged by excessive heat.

#### `PestControlSystem`

- Handles pest spawning, pest damage, threat assessment, and pesticide treatment.
- Tracks harmful pests and applies treatment to high-risk zones when necessary.
- Uses `PestEventBridge` to synchronize pest-related events with the GUI.

#### `GardenPlot`

- Represents the entire garden and acts as the main data container.
- Stores the plant map, zone list, current weather, and statistical information.
- Provides core operations such as adding plants, removing plants, querying plants, and finding zones.

#### `GardenZone`

- Represents a zone within the garden.
- Maintains the plants in that zone, along with moisture, temperature, and pest infestation level.
- Serves as the local operating unit for watering, temperature control, and pest treatment.

#### `GridPosition`

- Represents a row/column location in the garden grid.
- Provides basic spatial operations such as adjacency checks and distance calculations.

#### `Plant`

- The abstract parent class for all plant types.
- Encapsulates common plant state and behavior, including growth stage, health, water level, lifespan, temperature tolerance, and pest attack counts.
- Supports updating, advancing a day, watering, taking damage, healing, and applying weather or temperature effects.

#### `Flower` / `Fruit` / `Vegetable`

- Concrete subclasses of `Plant`.
- Reuse the shared plant logic while representing different crop characteristics through their own attributes and growth duration.

#### `Sensor`

- The abstract parent class for sensors.
- Defines common interfaces such as reading values, calibration, and status reporting.

#### `MoistureSensor`

- Reads soil moisture information for a garden zone.
- Provides decision input for the watering system.

#### `TemperatureSensor`

- Reads temperature information for a garden zone.
- Provides decision input for the heating and cooling systems.

#### `Sprinkler`

- Represents a watering device.
- Can be activated or deactivated and distributes water to its assigned zone.

#### `Pest`

- The abstract parent class for pests.
- Contains pest position, damage capability, and alive/dead state.
- Can damage plants and can also be eliminated.

#### `HarmfulPest`

- A concrete subclass of `Pest` representing a harmful pest.
- Mainly participates in damaging plants and triggering pest treatment workflows.

#### Enumeration Types

- `PlantType`: plant category.
- `GrowthStage`: plant growth stage.
- `SimulationState`: runtime state of the simulation.

### 3. Design Characteristics Reflected by the Class Diagram

- **Clear layering**: the GUI, controller, simulation, and domain layers are well separated.
- **High cohesion**: each subsystem focuses on one main concern, such as watering, weather, pests, or temperature.
- **Low coupling**: for example, `PestEventBridge` decouples simulation events from UI animation behavior.
- **Good extensibility**: plants, sensors, and pests all use abstract parent classes with subclasses, making future extension easier.