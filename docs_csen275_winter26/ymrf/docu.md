# 5.6 Object Diagram (10 points)

## Overview

An Object Diagram shows object instances and their relationships at a specific moment in time—a "runtime snapshot" of the class diagram. This section presents the object layout of the Smart Garden system during **early simulation** (after the first tick).

## 5.6.1 Scenario

**Scenario**: The user has planted 3 plants (1 flower, 1 vegetable, 1 fruit), clicked Start, and the first simulation tick has completed.

**Key Objects**:
- `ctrl`: `GardenController` – controller
- `garden`: `Garden` – garden model
- `engine`: `SimulationEngine` – simulation engine
- `z1`, `z2`: `Zone` – zones (2 shown)
- `p1`, `p2`, `p3`: `Plant` (Flower, Vegetable, Fruit)
- Subsystems: `wateringSystem`, `heatingSystem`, `coolingSystem`, `pestControlSystem`, `weatherSystem`

## 5.6.2 Object Diagram – Core Domain Model

```mermaid
flowchart TB
    ctrl["ctrl : GardenController"]
    garden["garden : Garden"]
    engine["engine : SimulationEngine"]
    z1["z1 : Zone<br/>zoneId=1, moisture=50<br/>temp=20, pest=0"]
    z2["z2 : Zone<br/>zoneId=2, moisture=50<br/>temp=20, pest=0"]
    p1["p1 : Flower<br/>pos=(0,0), water=100<br/>health=100, stage=SEED"]
    p2["p2 : Vegetable<br/>pos=(2,2), water=100<br/>health=100, stage=SEED"]
    p3["p3 : Fruit<br/>pos=(4,4), water=100<br/>health=100, stage=SEED"]
    ctrl -->|contains| garden
    ctrl -->|controls| engine
    engine -->|manages| garden
    garden -->|zones.0| z1
    garden -->|zones.1| z2
    garden --> p1
    garden --> p2
    garden --> p3
    z1 --> p1
    z2 --> p2
    z2 --> p3
```

## 5.6.3 Object Diagram – System Components

```mermaid
flowchart TB
    engine["engine : SimulationEngine<br/>state=RUNNING, speed=1, ticks=1"]
    watering["WateringSystem<br/>waterSupply=10000"]
    heating["HeatingSystem<br/>mode=LOW"]
    cooling["CoolingSystem<br/>mode=OFF"]
    pest["PestControlSystem<br/>pesticide=50"]
    weather["WeatherSystem<br/>SUNNY"]
    garden["garden : Garden"]
    engine -->|manages| garden
    engine --> watering
    engine --> heating
    engine --> cooling
    engine --> pest
    engine --> weather
    watering -->|reads| garden
    heating -->|affects| garden
    cooling -->|affects| garden
    pest -->|monitors| garden
    weather -->|affects| garden
```

## 5.6.4 Object Diagram Summary

| # | Type | Description |
|---|------|-------------|
| 1 | Core Domain Object Diagram | Objects and links among Garden, Zone, Plant, Controller, and Engine at simulation start |
| 2 | System Components Object Diagram | Instance relationships between SimulationEngine and WateringSystem, HeatingSystem, CoolingSystem, PestControlSystem, WeatherSystem |

**Total: 2 object diagrams**, covering **domain objects** and **system components**.

---

# 5.7 Communication and/or Sequence Diagram (10 points)

## Overview

Sequence diagrams describe the interaction order between objects over time. This section references existing project sequence diagrams and adds two new ones: **Plant Seed** and **Simulation Tick**.

## 5.7.1 Existing Sequence Diagrams (docs/design)

| # | File | Scenario |
|---|------|----------|
| 1 | `SequenceDiagram_StartSimulation.puml` | User clicks Start → View → Controller → SimulationEngine → subsystem initialization |
| 2 | `SequenceDiagram_PestControl.puml` | Pest control within a tick: spawn → detect → assess → applyTreatment → damage |
| 3 | `SequenceDiagram_AutomaticWatering.puml` | Automatic watering within a tick: checkMoisture → Sprinkler → Plant.water() |

## 5.7.2 Plant Seed Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant View as GardenGridPanel
    participant Controller as GardenController
    participant Garden
    participant Plant
    participant Zone
    participant Log as Logger
    User->>View: Click cell + select plant type (e.g., Daisy)
    View->>Controller: plantSeed(PlantType.DAISY, Position)
    Note over Controller: createPlant → new Flower(position, Daisy)
    Controller->>Plant: new Flower(position, Daisy)
    Plant-->>Controller: plant
    Controller->>Garden: addPlant(plant)
    Garden->>Zone: findZoneContaining(position)
    Zone-->>Garden: zone
    Garden->>Zone: addPlant(plant)
    Garden->>Garden: updateLivingCount()
    Garden-->>Controller: true
    Controller->>Log: log(INFO, Plant Daisy added)
    Controller-->>View: true
    View->>View: refreshGrid()
    View-->>User: Show plant sprite at cell
```

## 5.7.3 Simulation Tick Sequence Diagram (High-Level)

```mermaid
sequenceDiagram
    participant Engine as SimulationEngine
    participant Garden
    participant Water as WateringSystem
    participant Heat as HeatingSystem
    participant Cool as CoolingSystem
    participant Pest as PestControlSystem
    participant Weather as WeatherSystem
    Engine->>Engine: tick()
    Engine->>Engine: elapsedTicks++, simulationTime.plusMinutes(1)
    Engine->>Garden: getAllPlants()
    Garden-->>Engine: plants
    loop for each plant
        Engine->>Engine: plant.update()
    end
    Engine->>Water: checkAndWater()
    Engine->>Heat: update()
    Engine->>Cool: update()
    Engine->>Pest: update()
    Engine->>Weather: update()
    Engine->>Engine: autoRefillSupplies()
    Engine->>Garden: updateLivingCount()
    Garden-->>Engine:
```

## 5.7.4 Sequence Diagram Summary

| # | Type | Description |
|---|------|-------------|
| 1 | Start Simulation | User → View → Controller → SimulationEngine → subsystem initialization (existing) |
| 2 | Pest Control | SimulationEngine → PestControlSystem → Zone/Plant/Pest (existing) |
| 3 | Automatic Watering | SimulationEngine → WateringSystem → Zone/Sensor/Sprinkler/Plant (existing) |
| 4 | Plant Seed | User → GardenGridPanel → GardenController → Garden → Zone (added in this section) |
| 5 | Simulation Tick | Order of SimulationEngine calls to subsystems per tick (added in this section) |

**Total: 5 sequence diagrams**—3 from `docs/design`, 2 added in this section.
