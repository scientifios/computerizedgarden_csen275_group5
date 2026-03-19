# GUI Simulation Class Diagram

Scope: only the garden GUI simulation flow is kept. API-related classes such as `GardenSimulationAPI`, `GardenSimulator`, and `HeadlessSimulationEngine` are intentionally excluded.

```mermaid
classDiagram
    class SmartGardenApplication {
        -GardenController controller
        -GardenGridPanel gardenPanel
        -ModernToolbar toolbar
        -InfoPanel leftSidebarPanel
        -InfoPanel rightSidebarPanel
        -PestEventBridge pestEventBridge
        +start(Stage)
        +createScene() Scene
        +setupToolbarActions()
        +updateUI()
    }

    class GardenController {
        -GardenPlot garden
        -SimulationEngine simulationEngine
        +plantSeed(PlantType, GridPosition) boolean
        +removePlant(GridPosition) boolean
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
        -GridPosition selectedPlantPosition
        +updateTile(int, int)
        +updateAllTiles()
        +onPestSpawned(GridPosition, String, boolean)
        +onPesticideApplied(GridPosition)
        +setSelectedPlant(GridPosition)
    }

    class ModernToolbar {
        -Label statusLabel
        -Button startButton
        -Button pauseButton
        -Button stopButton
        -ComboBox~String~ speedComboBox
        +updateStatus(String)
    }

    class InfoPanel {
        <<GUI>>
        +updateLeftPanel(...)
        +updateGardenInfo(int, String, int, List~Plant~, Map~GridPosition,Integer~)
        +setSelectedPlant(GridPosition)
    }

    class PestEventBridge {
        +setHandler(PestAnimationHandler)
        +notifyPestSpawned(GridPosition, String, boolean)
        +notifyPesticideApplied(GridPosition)
    }

    class SimulationEngine {
        -GardenPlot garden
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
        -GardenPlot garden
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
        -GardenPlot garden
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
        -GardenPlot garden
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
        -GardenPlot garden
        -Map~int,TemperatureSensor~ sensors
        -int currentTemperature
        -CoolingMode coolingMode
        +update()
        +setAmbientTemperature(int)
        +getStatus() String
    }

    class PestControlSystem {
        -GardenPlot garden
        -List~Pest~ pests
        -int pesticideStock
        -int treatmentThreshold
        -PestEventBridge pestEventBridge
        +update()
        +registerPest(Pest)
        +refillPesticide(int)
        +getHarmfulPestCount() int
        +getActivePestCountAtPosition(GridPosition) int
        +setPestEventBridge(PestEventBridge)
    }

    class GardenPlot {
        -int rows
        -int columns
        -Map~GridPosition, Plant~ plantMap
        -List~GardenZone~ zones
        -String currentWeather
        -int totalPlants
        -int livingPlants
        +addPlant(Plant) boolean
        +removePlant(GridPosition) boolean
        +getPlant(GridPosition) Plant
        +getAllPlants() List~Plant~
        +getLivingPlants() List~Plant~
        +getZone(int) GardenZone
        +getZoneForPosition(GridPosition) GardenZone
        +updateLivingCount()
        +setWeather(String)
        +getStatistics() Map~String,Integer~
    }

    class GardenZone {
        -int zoneId
        -List~GridPosition~ boundaries
        -List~Plant~ plantsInZone
        -int moistureLevel
        -int temperature
        -int pestInfestationLevel
        +containsPosition(GridPosition) boolean
        +addPlant(Plant)
        +removePlant(Plant)
        +getPlantsNeedingWater() List~Plant~
        +getLivingPlants() List~Plant~
        +updateMoisture(int)
        +setTemperature(int)
        +updatePestLevel(int)
        +evaporate(int)
    }

    class GridPosition {
        +row() int
        +column() int
        +isAdjacentTo(GridPosition) boolean
        +distanceTo(GridPosition) int
    }

    class Plant {
        <<abstract>>
        -GridPosition position
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
        -GardenZone zone
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
        -GardenZone zone
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
        -GridPosition position
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

    GardenController --> GardenPlot
    GardenController --> SimulationEngine
    GardenController --> PlantType

    GardenPlot "1" *-- "9" GardenZone
    GardenPlot "1" o-- "*" Plant
    Plant --> GridPosition
    Plant --> GrowthStage
    Flower --|> Plant
    Fruit --|> Plant
    Vegetable --|> Plant

    SimulationEngine --> GardenPlot
    SimulationEngine --> WateringSystem
    SimulationEngine --> HeatingSystem
    SimulationEngine --> CoolingSystem
    SimulationEngine --> PestControlSystem
    SimulationEngine --> WeatherSystem
    SimulationEngine --> SimulationState

    WeatherSystem --> GardenPlot
    WeatherSystem --> HeatingSystem
    WeatherSystem --> CoolingSystem

    WateringSystem --> GardenPlot
    WateringSystem --> Sprinkler
    WateringSystem --> MoistureSensor
    WateringSystem --> WeatherSystem
    Sprinkler --> GardenZone
    Sensor --> GardenZone
    MoistureSensor --|> Sensor
    TemperatureSensor --|> Sensor

    HeatingSystem --> GardenPlot
    HeatingSystem --> TemperatureSensor
    CoolingSystem --> GardenPlot
    CoolingSystem --> TemperatureSensor

    PestControlSystem --> GardenPlot
    PestControlSystem --> Pest
    PestControlSystem --> PestEventBridge
    Pest --> GridPosition
    Pest --> Plant
    HarmfulPest --|> Pest
```
