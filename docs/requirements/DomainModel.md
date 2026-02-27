# Domain Model

## Core Domain Concepts

### 1. Garden
The central concept representing the physical space where plants grow.

**Attributes:**
- Grid dimensions (rows, columns)
- Collection of plants
- Collection of zones
- Weather conditions
- Timestamp of creation

**Responsibilities:**
- Maintain spatial organization of plants
- Coordinate between different subsystems
- Track overall garden state

**Relationships:**
- Contains 0..* Plants
- Divided into 1..* Zones
- Has 1 WeatherSystem
- Monitored by 1..* Sensors

---

### 2. Plant (Abstract)
Represents any living organism growing in the garden.

**Attributes:**
- Position (row, column)
- Growth stage (Seed, Seedling, Mature, Flowering)
- Health level (0-100%)
- Water level (0-100%)
- Days alive
- Maximum lifespan
- Water requirement
- Sunlight requirement
- Temperature tolerance range
- Pest resistance level

**Behaviors:**
- grow() - Progress to next growth stage
- water(amount) - Receive water
- takeDamage(amount) - Reduce health
- heal(amount) - Increase health
- updateStatus() - Recalculate health based on conditions
- die() - Transition to dead state

**Relationships:**
- IS-A: Plant (abstract)
- Located in: 1 Cell
- Belongs to: 1 Zone
- Affected by: 0..* Pests
- Affected by: 1 Weather

**Concrete Subclasses:**
1. **Flower** - Decorative, low water need, medium sunlight
2. **Vegetable** - Food crop, high water need, high sunlight
3. **Tree** - Long-lived, medium water, high sunlight
4. **Grass** - Ground cover, medium water, medium sunlight
5. **Herb** - Aromatic, low water, medium sunlight

---

### 3. Zone
A logical subdivision of the garden for management purposes.

**Attributes:**
- Zone ID
- Cell boundaries (list of coordinates)
- Moisture level (0-100%)
- Temperature
- Pest infestation level (0-100%)
- Active plants in zone

**Responsibilities:**
- Group nearby plants
- Manage localized conditions
- Coordinate subsystem actions

**Relationships:**
- Part of: 1 Garden
- Contains: 0..* Plants
- Has: 1 MoistureSensor
- Served by: 1 Sprinkler
- Monitored by: 1 PestDetector

---

### 4. WateringSystem
Manages irrigation across all zones.

**Attributes:**
- Water supply level
- Active zones (currently watering)
- Watering schedule
- Moisture threshold (trigger point)

**Behaviors:**
- checkMoistureLevels() - Scan all zones
- waterZone(zoneId) - Activate watering for specific zone
- calculateWaterNeeded(zone) - Determine amount
- updateSupply(amount) - Track consumption

**Relationships:**
- Manages: 1..* Zones
- Uses: 1..* Sprinklers
- Reads from: 1..* MoistureSensors
- Logs to: 1 Logger

---

### 5. Sprinkler
Physical device that delivers water to a zone.

**Attributes:**
- Zone assignment
- Flow rate (liters per minute)
- Active status (on/off)
- Last activation time

**Behaviors:**
- activate() - Turn on water flow
- deactivate() - Turn off water flow
- distributeWater(amount) - Apply to zone plants

**Relationships:**
- Belongs to: 1 Zone
- Controlled by: 1 WateringSystem

---

### 6. HeatingSystem
Maintains optimal temperature for plant growth.

**Attributes:**
- Current temperature
- Target temperature range (min, max)
- Heating mode (Off, Low, Medium, High)
- Energy consumption

**Behaviors:**
- monitorTemperature() - Check current conditions
- adjustHeating() - Increase/decrease heat
- applyWarmthToPlants() - Boost growth

**Relationships:**
- Monitors: 1..* TemperatureSensors
- Affects: all Plants in Garden
- Controlled by: 1 Thermostat

---

### 7. PestControlSystem
Detects and manages pest infestations.

**Attributes:**
- Pesticide stock level
- Detection sensitivity
- Treatment threshold
- Active treatments (zones being treated)

**Behaviors:**
- detectPests(zone) - Scan for infestations
- assessThreat() - Evaluate infestation level
- applyTreatment(zone) - Deploy pesticide
- reducePestCount(zone, amount) - Update after treatment

**Relationships:**
- Monitors: all Zones
- Uses: 1..* PestTraps
- Targets: 1..* Pests
- Protects: 1..* Plants

---

### 8. Pest (Interface/Abstract)
Organisms that can harm plants.

**Attributes:**
- Pest type (Aphid, Caterpillar, Beetle, etc.)
- Damage rate
- Reproduction rate
- Zone location

**Behaviors:**
- infest(plant) - Attach to plant
- causeDamage() - Reduce plant health
- reproduce() - Increase pest count

**Concrete Types:**
- **HarmfulPest** - Damages plants, should be eliminated

**Relationships:**
- Targets: 1..* Plants
- Exists in: 1 Zone
- Detected by: 1 PestControlSystem

---

### 9. Sensor (Abstract)
Monitoring devices that provide environmental data.

**Attributes:**
- Sensor ID
- Location (zone)
- Reading value
- Last reading timestamp
- Status (Active, Inactive, Error)

**Behaviors:**
- readValue() - Get current measurement
- calibrate() - Adjust for accuracy
- reportStatus() - Communication check

**Concrete Subclasses:**
- **MoistureSensor** - Measures soil water content
- **TemperatureSensor** - Measures ambient temperature
- **PestDetector** - Identifies pest presence

**Relationships:**
- Located in: 1 Zone
- Reports to: 1 AutomationSystem
- Logged by: 1 Logger

---

### 10. SimulationEngine
Controls time progression and coordinates all systems.

**Attributes:**
- Current simulation time
- Tick interval
- Speed multiplier (1x, 5x, 10x)
- Running state (Running, Paused, Stopped)
- Elapsed time

**Behaviors:**
- start() - Begin simulation
- pause() - Temporarily halt
- resume() - Continue after pause
- stop() - End simulation
- tick() - Process one time unit
- updateAllSystems() - Trigger system updates
- triggerRandomEvents() - Generate occurrences

**Relationships:**
- Controls: 1 Garden
- Manages: all AutomationSystems
- Uses: 1 EventScheduler
- Logs to: 1 Logger

---

### 11. WeatherSystem
Simulates environmental weather conditions.

**Attributes:**
- Current weather (Sunny, Rainy, Cloudy, Windy)
- Weather duration
- Next weather (forecast)
- Temperature modifier
- Moisture modifier

**Behaviors:**
- changeWeather() - Transition to new condition
- applyEffects(plants) - Modify plant growth
- generateForecast() - Predict next weather

**Relationships:**
- Part of: 1 Garden
- Affects: all Plants
- Affects: 1 WateringSystem (rain reduces need)
- Affects: 1 HeatingSystem (temp changes)

---

### 12. Logger
Records all system events for analysis.

**Attributes:**
- Log file path
- Current session ID
- Buffer (recent entries)
- Log level filter

**Behaviors:**
- log(level, message) - Write entry
- flush() - Write buffer to disk
- rotateLogs() - Start new file
- query(filter) - Search logs

**Relationships:**
- Used by: all Systems
- Writes to: LogFile
- Displayed in: LogViewer (UI)

---

### 13. GardenController (UI)
Mediates between user interface and domain logic.

**Attributes:**
- Reference to Garden
- Reference to SimulationEngine
- Reference to all AutomationSystems
- UI state

**Behaviors:**
- startSimulation() - User action handler
- pauseSimulation()
- plantSeed(type, position)
- setSpeed(multiplier)
- manualWater(zone)
- displayStatus()

**Relationships:**
- Controls: 1 SimulationEngine
- Updates: 1 GardenView (UI)
- Receives input from: User

---

## Domain Relationships Summary

```
Garden (1) ----contains----> (0..*) Plant
Garden (1) ----divided into----> (1..*) Zone
Zone (1) ----contains----> (0..*) Plant
Plant (0..*) ----affected by----> (0..*) Pest
Zone (1) ----has----> (1) MoistureSensor
Zone (1) ----served by----> (1) Sprinkler
WateringSystem (1) ----manages----> (1..*) Zone
HeatingSystem (1) ----affects----> (0..*) Plant
PestControlSystem (1) ----monitors----> (1..*) Zone
SimulationEngine (1) ----controls----> (1) Garden
SimulationEngine (1) ----manages----> (1..*) AutomationSystem
WeatherSystem (1) ----affects----> (0..*) Plant
Logger (1) ----used by----> (1..*) System
GardenController (1) ----controls----> (1) SimulationEngine
```

---

## Key Domain Rules

1. **Plant Lifecycle Rule**: Plants must progress through growth stages sequentially
2. **Water Conservation Rule**: Watering only occurs when moisture < threshold
3. **Temperature Range Rule**: Plants suffer damage outside tolerance range
4. **Pest Threshold Rule**: Treatment activates only at high infestation levels
5. **Zone Integrity Rule**: Each cell belongs to exactly one zone
6. **Time Progression Rule**: All updates occur on simulation tick
7. **Resource Depletion Rule**: Systems stop when supplies (water, pesticide) are exhausted
8. **Health Decay Rule**: Plant health decreases when conditions are suboptimal
9. **Weather Effect Rule**: Weather modifies growth rates and resource needs
10. **Logging Rule**: All significant events must be logged with timestamp

