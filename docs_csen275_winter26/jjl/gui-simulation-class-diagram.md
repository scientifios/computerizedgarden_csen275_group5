# GUI Simulation Class Diagram

Scope: only the garden GUI simulation flow is kept. API-related classes such as `GardenSimulationAPI`, `GardenSimulator`, and `HeadlessSimulationEngine` are intentionally excluded.

```mermaid
classDiagram
    class SmartGardenApplication {
        -GardenController controller
        -GardenGridPanel gardenPanel
        -ModernToolbar toolbar
        -InfoPanel leftInfoPanel
        -InfoPanel rightInfoPanel
        -PestEventBridge pestEventBridge
        +start(Stage)
        +createScene() Scene
        +setupToolbarActions()
        +updateUI()
    }

    class GardenController {
        -Garden garden
        -SimulationEngine simulationEngine
        +plantSeed(PlantType, Position) boolean
        +removePlant(Position) boolean
        +startSimulation()
        +pauseSimulation()
        +resumeSimulation()
        +stopSimulation()
        +setSimulationSpeed(int)
    }

    class GardenGridPanel {
        -GardenController controller
        -AnimatedTile[][] tiles
        -GrassTile[][] grassTiles
        -Position selectedPlantPosition
        +updateTile(int, int)
        +updateAllTiles()
        +onPestSpawned(Position, String, boolean)
        +onPesticideApplied(Position)
        +setSelectedPlant(Position)
    }

    class ModernToolbar {
        -Label statusLabel
        -Button startBtn
        -Button pauseBtn
        -Button stopBtn
        -ComboBox~String~ speedBox
        +updateStatus(String)
    }

    class InfoPanel {
        <<GUI>>
        +updateLeftPanel(...)
        +updateGardenInfo(int, String, int, List~Plant~, Map~Position,Integer~)
        +setSelectedPlant(Position)
    }

    class PestEventBridge {
        +setHandler(PestAnimationHandler)
        +notifyPestSpawned(Position, String, boolean)
        +notifyPesticideApplied(Position)
    }

    class SimulationEngine {
        -Garden garden
        -WateringSystem wateringSystem
        -HeatingSystem heatingSystem
        -CoolingSystem coolingSystem
        -PestControlSystem pestControlSystem
        -WeatherSystem weatherSystem
        -SimulationState state
        -int dayCounter
        +start()
        +pause()
        +resume()
        +stop()
        +setSpeed(int)
        +getFormattedTime() String
    }

    class WeatherSystem {
        -Garden garden
        -HeatingSystem heatingSystem
        -CoolingSystem coolingSystem
        -Weather currentWeather
        -int weatherDuration
        +update()
        +setWeather(Weather)
        +enableSunnyRainyRotation()
        +getForecast() Weather
    }

    class WateringSystem {
        -Garden garden
        -Map~int,Sprinkler~ sprinklers
        -Map~int,MoistureSensor~ sensors
        -int waterSupply
        -int moistureThreshold
        -WeatherSystem weatherSystem
        +checkAndWater()
        +waterZone(int,int)
        +refillWater(int)
        +stopAllSprinklers()
    }

    class HeatingSystem {
        -Garden garden
        -Map~int,TemperatureSensor~ sensors
        -int currentTemperature
        -int targetMinTemperature
        -int targetMaxTemperature
        -HeatingMode heatingMode
        +update()
        +setAmbientTemperature(int)
        +setTargetRange(int,int)
        +getStatus() String
    }

    class CoolingSystem {
        -Garden garden
        -Map~int,TemperatureSensor~ sensors
        -int currentTemperature
        -CoolingMode coolingMode
        +update()
        +setAmbientTemperature(int)
        +getStatus() String
    }

    class PestControlSystem {
        -Garden garden
        -List~Pest~ pests
        -int pesticideStock
        -int treatmentThreshold
        -PestEventBridge pestEventBridge
        +update()
        +registerPest(Pest)
        +refillPesticide(int)
        +getHarmfulPestCount() int
        +getActivePestCountAtPosition(Position) int
        +setPestEventBridge(PestEventBridge)
    }

    class Garden {
        -int rows
        -int columns
        -Map~Position, Plant~ plantMap
        -List~Zone~ zones
        -String currentWeather
        -int totalPlants
        -int livingPlants
        +addPlant(Plant) boolean
        +removePlant(Position) boolean
        +getPlant(Position) Plant
        +getAllPlants() List~Plant~
        +getLivingPlants() List~Plant~
        +getZone(int) Zone
        +getZoneForPosition(Position) Zone
        +updateLivingCount()
        +setWeather(String)
        +getStatistics() Map~String,Integer~
    }

    class Zone {
        -int zoneId
        -List~Position~ boundaries
        -List~Plant~ plantsInZone
        -int moistureLevel
        -int temperature
        -int pestInfestationLevel
        +containsPosition(Position) boolean
        +addPlant(Plant)
        +removePlant(Plant)
        +getPlantsNeedingWater() List~Plant~
        +getLivingPlants() List~Plant~
        +updateMoisture(int)
        +setTemperature(int)
        +updatePestLevel(int)
        +evaporate(int)
    }

    class Position {
        +row() int
        +column() int
        +isAdjacentTo(Position) boolean
        +distanceTo(Position) int
    }

    class Plant {
        <<abstract>>
        -Position position
        -GrowthStage growthStage
        -int healthLevel
        -int waterLevel
        -int daysAlive
        -boolean isDead
        -int maxLifespan
        -int waterRequirement
        -int minTemperature
        -int maxTemperature
        -int pestAttacks
        -int totalPestAttacks
        +update()
        +advanceDay()
        +water(int)
        +takeDamage(int)
        +heal(int)
        +pestAttack()
        +reducePestAttacks(int)
        +applyTemperatureEffect(int)
        +applyWeatherEffect(String)
        +getGrowthDuration() int
    }

    class Flower {
        -String bloomColor
        +getGrowthDuration() int
    }

    class Fruit {
        -String fruitType
        +getGrowthDuration() int
    }

    class Vegetable {
        -String vegetableType
        +getGrowthDuration() int
    }

    class Sensor {
        <<abstract>>
        -String sensorId
        -Zone zone
        -SensorStatus status
        +readValue() int
        +calibrate()
        +reportStatus() SensorStatus
    }

    class MoistureSensor {
        +readValue() int
    }

    class TemperatureSensor {
        +readValue() int
    }

    class Sprinkler {
        -Zone zone
        -int flowRate
        -boolean isActive
        +activate()
        +deactivate()
        +distributeWater(int) int
    }

    class Pest {
        <<abstract>>
        -String pestType
        -int damageRate
        -Position position
        -boolean isAlive
        +causeDamage(Plant)
        +isBeneficial() boolean
        +eliminate()
    }

    class HarmfulPest {
        +causeDamage(Plant)
        +isBeneficial() boolean
    }

    class PlantType {
        <<enumeration>>
    }

    class GrowthStage {
        <<enumeration>>
    }

    class SimulationState {
        <<enumeration>>
    }

    SmartGardenApplication --> GardenController
    SmartGardenApplication --> GardenGridPanel
    SmartGardenApplication --> ModernToolbar
    SmartGardenApplication --> InfoPanel
    SmartGardenApplication --> PestEventBridge

    GardenGridPanel --> GardenController
    InfoPanel --> Plant
    PestEventBridge --> GardenGridPanel

    GardenController --> Garden
    GardenController --> SimulationEngine
    GardenController --> PlantType

    Garden "1" *-- "9" Zone
    Garden "1" o-- "*" Plant
    Plant --> Position
    Plant --> GrowthStage
    Flower --|> Plant
    Fruit --|> Plant
    Vegetable --|> Plant

    SimulationEngine --> Garden
    SimulationEngine --> WateringSystem
    SimulationEngine --> HeatingSystem
    SimulationEngine --> CoolingSystem
    SimulationEngine --> PestControlSystem
    SimulationEngine --> WeatherSystem
    SimulationEngine --> SimulationState

    WeatherSystem --> Garden
    WeatherSystem --> HeatingSystem
    WeatherSystem --> CoolingSystem

    WateringSystem --> Garden
    WateringSystem --> Sprinkler
    WateringSystem --> MoistureSensor
    WateringSystem --> WeatherSystem
    Sprinkler --> Zone
    Sensor --> Zone
    MoistureSensor --|> Sensor
    TemperatureSensor --|> Sensor

    HeatingSystem --> Garden
    HeatingSystem --> TemperatureSensor
    CoolingSystem --> Garden
    CoolingSystem --> TemperatureSensor

    PestControlSystem --> Garden
    PestControlSystem --> Pest
    PestControlSystem --> PestEventBridge
    Pest --> Position
    Pest --> Plant
    HarmfulPest --|> Pest
```
