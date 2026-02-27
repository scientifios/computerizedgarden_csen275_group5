# Smart Garden Simulation - Developer Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Extending the System](#extending-the-system)
5. [Building and Running](#building-and-running)
6. [Testing](#testing)
7. [Debugging](#debugging)
8. [Code Conventions](#code-conventions)
9. [API Reference](#api-reference)

---

## Architecture Overview

### Design Principles

The Smart Garden Simulation follows these key principles:

1. **Layered Architecture**: Clear separation between presentation, application, domain, and infrastructure layers
2. **SOLID Principles**: Especially Single Responsibility and Open/Closed
3. **Design Patterns**: MVC, Observer, Strategy, Factory, Singleton
4. **JavaFX Properties**: Reactive UI updates through property binding

### Architectural Layers

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)         │
│  - JavaFX Application               │
│  - View Controllers                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Application Layer                │
│  - GardenController                  │
│  - Simulation Orchestration          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Domain Layer                     │
│  - Garden, Plant, Zone               │
│  - Business Logic                    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     System Layer                     │
│  - Watering, Heating, Pest Control   │
│  - Weather, Sensors                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Infrastructure Layer             │
│  - Logger, Configuration             │
│  - File I/O                          │
└─────────────────────────────────────┘
```

---

## Project Structure

```
smartGarden/
├── pom.xml                           # Maven configuration
├── README.md                         # Project overview
├── .gitignore                        # Git ignore rules
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/scu/csen275/smartgarden/
│   │   │       ├── SmartGardenApplication.java    # Main entry point
│   │   │       │
│   │   │       ├── controller/                     # Application logic
│   │   │       │   └── GardenController.java
│   │   │       │
│   │   │       ├── model/                          # Domain entities
│   │   │       │   ├── Garden.java
│   │   │       │   ├── Zone.java
│   │   │       │   ├── Position.java
│   │   │       │   ├── GrowthStage.java
│   │   │       │   ├── Plant.java                 # Abstract base
│   │   │       │   ├── Flower.java
│   │   │       │   ├── Vegetable.java
│   │   │       │   ├── Tree.java
│   │   │       │   ├── Grass.java
│   │   │       │   └── Herb.java
│   │   │       │
│   │   │       ├── system/                         # Automation systems
│   │   │       │   ├── Sensor.java                # Abstract base
│   │   │       │   ├── MoistureSensor.java
│   │   │       │   ├── TemperatureSensor.java
│   │   │       │   ├── Sprinkler.java
│   │   │       │   ├── WateringSystem.java
│   │   │       │   ├── HeatingSystem.java
│   │   │       │   ├── Pest.java                  # Abstract base
│   │   │       │   ├── HarmfulPest.java
│   │   │       │   └── PestControlSystem.java
│   │   │       │
│   │   │       ├── simulation/                     # Simulation engine
│   │   │       │   ├── SimulationEngine.java
│   │   │       │   └── WeatherSystem.java
│   │   │       │
│   │   │       └── util/                           # Infrastructure
│   │   │           └── Logger.java
│   │   │
│   │   └── resources/                              # Resources (if needed)
│   │       └── (images, CSS, FXML)
│   │
│   └── test/                                       # Unit tests
│       └── java/
│           └── (test classes)
│
├── docs/                                           # Documentation
│   ├── requirements/                               # Requirements analysis
│   │   ├── ProblemStatement.md
│   │   ├── FeatureList.md
│   │   ├── RequirementsList.md
│   │   ├── UserStories.md
│   │   ├── UseCases.md
│   │   ├── DomainModel.md
│   │   ├── Constraints.md
│   │   └── Assumptions.md
│   │
│   ├── design/                                     # Design documentation
│   │   ├── DesignOverview.md
│   │   ├── ClassDiagram.puml
│   │   ├── SequenceDiagram_StartSimulation.puml
│   │   ├── SequenceDiagram_AutomaticWatering.puml
│   │   ├── SequenceDiagram_PestControl.puml
│   │   ├── StateDiagram_PlantLifecycle.puml
│   │   ├── StateDiagram_WateringSystem.puml
│   │   ├── StateDiagram_Simulation.puml
│   │   ├── ActivityDiagram_SimulationTick.puml
│   │   ├── ActivityDiagram_PlantOperation.puml
│   │   └── ComponentDiagram.puml
│   │
│   └── manual/                                     # User documentation
│       ├── UserManual.md
│       └── DeveloperGuide.md
│
├── logs/                                           # Generated log files
│   └── garden_YYYYMMDD_HHMMSS.log
│
└── target/                                         # Maven build output
    └── (compiled classes, JAR files)
```

---

## Core Components

### 1. Garden (Domain Core)

**File**: `model/Garden.java`

**Purpose**: Central domain entity representing the garden

**Key Responsibilities**:
- Maintain plant collection (Map<Position, Plant>)
- Manage zones
- Validate positions
- Track statistics

**Important Methods**:
```java
boolean addPlant(Plant plant)
boolean removePlant(Position position)
Plant getPlant(Position position)
List<Plant> getAllPlants()
Zone getZoneForPosition(Position position)
```

**Usage Example**:
```java
Garden garden = new Garden(9, 9);
Plant tomato = new Vegetable(new Position(4, 4), "Tomato");
garden.addPlant(tomato);
```

### 2. Plant (Abstract Base Class)

**File**: `model/Plant.java`

**Purpose**: Base class for all plant types

**Key Properties** (JavaFX Observable):
- `healthLevel`: 0-100%
- `waterLevel`: 0-100%
- `growthStage`: Enum
- `daysAlive`: Integer
- `isDead`: Boolean

**Abstract Methods**:
```java
public abstract int getGrowthDuration();
```

**Update Cycle**:
```java
public void update()          // Called every tick
public void advanceDay()      // Called every simulation day
public void water(int amount) // Adds water
public void takeDamage(int amount) // Reduces health
```

**Extending Plant**:
```java
public class MyPlant extends Plant {
    public MyPlant(Position position) {
        super("MyPlant", position, 
              maxLifespan, waterReq, sunlightReq,
              minTemp, maxTemp, pestResistance);
    }
    
    @Override
    public int getGrowthDuration() {
        return 8; // days per growth stage
    }
}
```

### 3. SimulationEngine

**File**: `simulation/SimulationEngine.java`

**Purpose**: Coordinates all systems and manages time progression

**Key Components**:
- JavaFX Timeline for discrete time steps
- Speed multiplier (1x to 10x)
- State management (STOPPED, RUNNING, PAUSED)
- System update coordination

**Tick Processing**:
```java
private void tick() {
    // 1. Increment time
    elapsedTicks++;
    simulationTime.plusMinutes(1);
    
    // 2. Update plants
    for (Plant plant : garden.getAllPlants()) {
        plant.update();
    }
    
    // 3. Update systems
    wateringSystem.checkAndWater();
    heatingSystem.update();
    pestControlSystem.update();
    weatherSystem.update();
    
    // 4. Check for new day
    if (ticksPerDay >= TICKS_PER_DAY) {
        advanceDay();
    }
}
```

### 4. WateringSystem

**File**: `system/WateringSystem.java`

**Purpose**: Automated irrigation management

**Architecture**:
- One sprinkler per zone
- One moisture sensor per zone
- Central water supply
- Configurable threshold

**Watering Logic**:
```java
public void checkAndWater() {
    for (Zone zone : garden.getZones()) {
        int moisture = sensor.readValue();
        if (moisture < threshold && zone.hasPlants()) {
            waterZone(zone.getId(), WATER_AMOUNT);
        }
    }
}
```

### 5. Logger

**File**: `util/Logger.java`

**Purpose**: Centralized logging system

**Features**:
- Singleton pattern
- Thread-safe (ConcurrentQueue)
- File and in-memory logging
- Multiple log levels (DEBUG, INFO, WARNING, ERROR)
- Automatic log rotation

**Usage**:
```java
Logger logger = Logger.getInstance();
logger.info("Category", "Message");
logger.warning("Category", "Warning message");
logger.error("Category", "Error message");
logger.logException("Category", "Error context", exception);
```

---

## Extending the System

### Adding a New Plant Type

1. **Create the class**:
```java
package edu.scu.csen275.smartgarden.model;

public class Cactus extends Plant {
    private static final int DEFAULT_LIFESPAN = 300;
    private static final int WATER_REQ = 10;  // Very low
    private static final int SUNLIGHT_REQ = 90; // High
    private static final int MIN_TEMP = 5;
    private static final int MAX_TEMP = 45;   // Tolerant
    private static final int PEST_RESISTANCE = 8; // High
    private static final int GROWTH_DURATION = 20; // Slow
    
    public Cactus(Position position) {
        super("Cactus", position, DEFAULT_LIFESPAN, WATER_REQ, 
              SUNLIGHT_REQ, MIN_TEMP, MAX_TEMP, PEST_RESISTANCE);
    }
    
    @Override
    public int getGrowthDuration() {
        return GROWTH_DURATION;
    }
    
    // Optional: Override methods for custom behavior
    @Override
    public void water(int amount) {
        // Cacti don't benefit much from excess water
        super.water(amount / 2);
    }
}
```

2. **Update GardenController factory**:
```java
private Plant createPlant(String plantType, Position position) {
    return switch (plantType.toLowerCase()) {
        // ... existing cases ...
        case "cactus" -> new Cactus(position);
        default -> null;
    };
}
```

3. **Update UI plant selector**:
```java
plantBox.getItems().addAll("Flower", "Tomato", "Tree", 
                           "Grass", "Basil", "Cactus");
```

### Adding a New Sensor Type

1. **Extend Sensor base class**:
```java
package edu.scu.csen275.smartgarden.system;

public class LightSensor extends Sensor {
    public LightSensor(Zone zone) {
        super("LIGHT-" + zone.getZoneId(), zone);
    }
    
    @Override
    public int readValue() {
        updateReadingTime();
        try {
            // Implement measurement logic
            return calculateLightLevel();
        } catch (Exception e) {
            status = SensorStatus.ERROR;
            return -1;
        }
    }
    
    private int calculateLightLevel() {
        // Custom logic here
        return 75; // 0-100 scale
    }
}
```

2. **Integrate into a system** (e.g., new LightingSystem)

### Adding a New Automation System

1. **Create system class**:
```java
package edu.scu.csen275.smartgarden.system;

public class FertilizerSystem {
    private final Garden garden;
    private final IntegerProperty fertilizerStock;
    private static final Logger logger = Logger.getInstance();
    
    public FertilizerSystem(Garden garden) {
        this.garden = garden;
        this.fertilizerStock = new SimpleIntegerProperty(100);
        logger.info("Fertilizer", "System initialized");
    }
    
    public void update() {
        // Check which plants need fertilizer
        for (Plant plant : garden.getLivingPlants()) {
            if (needsFertilizer(plant)) {
                applyFertilizer(plant);
            }
        }
    }
    
    private boolean needsFertilizer(Plant plant) {
        // Implement logic
        return plant.getDaysAlive() % 10 == 0;
    }
    
    private void applyFertilizer(Plant plant) {
        if (fertilizerStock.get() > 0) {
            plant.heal(10);
            fertilizerStock.set(fertilizerStock.get() - 1);
            logger.info("Fertilizer", "Applied to " + plant.getPlantType());
        }
    }
}
```

2. **Add to SimulationEngine**:
```java
private final FertilizerSystem fertilizerSystem;

// In constructor
this.fertilizerSystem = new FertilizerSystem(garden);

// In tick() method
fertilizerSystem.update();
```

3. **Add UI controls** in SmartGardenApplication

### Adding Custom Weather Effects

1. **Add weather type to enum**:
```java
public enum Weather {
    SUNNY, CLOUDY, RAINY, WINDY, SNOWY,
    FOGGY("🌫", "Foggy");  // New weather
    // ...
}
```

2. **Update applyWeatherEffect in Plant**:
```java
public void applyWeatherEffect(String weather) {
    switch (weather) {
        // ... existing cases ...
        case "FOGGY" -> {
            // Reduced sunlight, slight cooling
            takeDamage(1);
        }
    }
}
```

3. **Update weather transitions** in WeatherSystem:
```java
private Weather generateNextWeather(Weather current) {
    // Add FOGGY to transition logic
}
```

---

## Building and Running

### Maven Commands

**Clean Build**:
```bash
mvn clean install
```

**Run Application**:
```bash
mvn javafx:run
```

**Compile Only**:
```bash
mvn compile
```

**Run Tests**:
```bash
mvn test
```

**Package JAR**:
```bash
mvn package
```

**Generate Javadoc**:
```bash
mvn javadoc:javadoc
```

### IDE Setup

#### IntelliJ IDEA

1. Import as Maven project
2. Set SDK to Java 21
3. Enable annotation processing
4. Run configuration:
   - Main class: `edu.scu.csen275.smartgarden.SmartGardenApplication`
   - VM options: `--module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml`

#### Eclipse

1. Import > Existing Maven Project
2. Set Java compiler to 21
3. Install JavaFX plugin
4. Run as Java Application

#### VS Code

1. Install Java Extension Pack
2. Install Maven extension
3. Open project folder
4. Run from Maven sidebar

---

## Testing

### Unit Test Structure

```java
package edu.scu.csen275.smartgarden.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class PlantTest {
    private Plant testPlant;
    
    @BeforeEach
    void setUp() {
        testPlant = new Flower(new Position(0, 0));
    }
    
    @Test
    void testWateringIncreasesWaterLevel() {
        int initialWater = testPlant.getWaterLevel();
        testPlant.water(20);
        assertTrue(testPlant.getWaterLevel() > initialWater);
    }
    
    @Test
    void testDamageReducesHealth() {
        int initialHealth = testPlant.getHealthLevel();
        testPlant.takeDamage(10);
        assertEquals(initialHealth - 10, testPlant.getHealthLevel());
    }
    
    @Test
    void testPlantDiesAtZeroHealth() {
        testPlant.takeDamage(150); // Exceeds health
        assertTrue(testPlant.isDead());
    }
}
```

### Integration Tests

```java
@Test
void testWateringSystemIntegration() {
    Garden garden = new Garden(9, 9);
    garden.addPlant(new Flower(new Position(0, 0)));
    
    WateringSystem waterSys = new WateringSystem(garden);
    Zone zone = garden.getZone(1);
    
    // Reduce moisture below threshold
    zone.updateMoisture(-60);
    
    // Trigger watering
    waterSys.checkAndWater();
    
    // Verify moisture increased
    assertTrue(zone.getMoistureLevel() > 30);
}
```

### Running Tests

```bash
mvn test
```

Or in IDE: Right-click test class > Run Tests

---

## Debugging

### Enabling Debug Logging

```java
Logger logger = Logger.getInstance();
logger.setMinLogLevel(Logger.LogLevel.DEBUG);
```

### Common Debug Scenarios

#### 1. Plant Not Growing

**Check**:
- Is simulation running?
- Has enough time passed? (check `daysAlive`)
- Is plant dead?
- Check `growthStage` and `daysSinceGrowth`

**Debug Code**:
```java
System.out.println("Plant: " + plant);
System.out.println("Days alive: " + plant.getDaysAlive());
System.out.println("Growth stage: " + plant.getGrowthStage());
System.out.println("Health: " + plant.getHealthLevel());
```

#### 2. Watering Not Triggering

**Check**:
- Zone moisture level: `zone.getMoistureLevel()`
- Threshold setting: `wateringSystem.getMoistureThreshold()`
- Water supply: `wateringSystem.getWaterSupply()`
- Plants in zone: `zone.getLivingPlantCount()`

**Debug Code**:
```java
for (Zone zone : garden.getZones()) {
    System.out.printf("Zone %d: Moisture=%d, Plants=%d%n",
        zone.getZoneId(), zone.getMoistureLevel(), 
        zone.getLivingPlantCount());
}
```

#### 3. UI Not Updating

**Check**:
- UI update timer running?
- JavaFX thread issues (use `Platform.runLater()`)
- Property bindings correct?

**Debug Code**:
```java
Platform.runLater(() -> {
    System.out.println("UI update on FX thread");
});
```

### Logging Best Practices

1. **Use appropriate levels**:
   - DEBUG: Detailed flow information
   - INFO: General events
   - WARNING: Potential issues
   - ERROR: Failures

2. **Include context**:
```java
logger.info("Watering", "Zone " + zoneId + " watered. " +
           "Moisture: " + moisture + "%, Supply: " + supply);
```

3. **Log exceptions properly**:
```java
try {
    // risky operation
} catch (Exception e) {
    logger.logException("Component", "Operation failed", e);
}
```

---

## Code Conventions

### Naming

- **Classes**: PascalCase (`WateringSystem`)
- **Methods**: camelCase (`waterZone()`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_THRESHOLD`)
- **Variables**: camelCase (`plantCount`)
- **Packages**: lowercase (`edu.scu.csen275.smartgarden`)

### Documentation

**Class Documentation**:
```java
/**
 * Represents a zone in the garden.
 * A zone is a logical grouping of grid cells for management purposes.
 * 
 * @author Smart Garden Team
 * @version 1.0
 * @since 1.0
 */
public class Zone {
    // ...
}
```

**Method Documentation**:
```java
/**
 * Waters a specific zone with the given amount.
 * 
 * @param zoneId the ID of the zone to water (1-9)
 * @param amount the amount of water in liters
 * @throws IllegalArgumentException if zoneId is invalid
 */
public void waterZone(int zoneId, int amount) {
    // ...
}
```

### Exception Handling

```java
public void someMethod() {
    try {
        // risky operation
    } catch (SpecificException e) {
        logger.error("Component", "Specific error: " + e.getMessage());
        // Handle specifically
    } catch (Exception e) {
        logger.logException("Component", "Unexpected error", e);
        // Fallback handling
    }
}
```

### JavaFX Properties

**Creating Properties**:
```java
private final IntegerProperty healthLevel = new SimpleIntegerProperty(100);
```

**Getters (three forms)**:
```java
// For binding
public IntegerProperty healthLevelProperty() { return healthLevel; }

// For value access
public int getHealthLevel() { return healthLevel.get(); }

// For setter (if needed)
public void setHealthLevel(int value) { healthLevel.set(value); }
```

---

## API Reference

### Core Model Classes

#### Garden
```java
Garden(int rows, int columns)
boolean addPlant(Plant plant)
boolean removePlant(Position position)
Plant getPlant(Position position)
List<Plant> getAllPlants()
List<Plant> getLivingPlants()
Zone getZoneForPosition(Position position)
```

#### Plant (abstract)
```java
void update()
void advanceDay()
void water(int amount)
void takeDamage(int amount)
void heal(int amount)
void applyTemperatureEffect(int temp)
void applyWeatherEffect(String weather)
int getHealthLevel()
boolean isDead()
```

#### Position
```java
Position(int row, int column)
int row()
int column()
boolean isAdjacentTo(Position other)
int distanceTo(Position other)
```

### System Classes

#### WateringSystem
```java
void checkAndWater()
void waterZone(int zoneId, int amount)
void manualWater(int zoneId)
void setMoistureThreshold(int threshold)
int getWaterSupply()
```

#### HeatingSystem
```java
void update()
void setAmbientTemperature(int temp)
void setTargetRange(int min, int max)
int getCurrentTemperature()
```

#### PestControlSystem
```java
void update()
void manualTreat(int zoneId)
int getHarmfulPestCount()
```

### Simulation Classes

#### SimulationEngine
```java
void start()
void pause()
void resume()
void stop()
void setSpeed(int multiplier)
SimulationState getState()
long getElapsedTicks()
```

#### WeatherSystem
```java
void update()
Weather getCurrentWeather()
Weather getForecast()
```

### Utility Classes

#### Logger
```java
static Logger getInstance()
void log(LogLevel level, String category, String message)
void info(String category, String message)
void warning(String category, String message)
void error(String category, String message)
void logException(String category, String message, Exception e)
void flush()
```

---

## Performance Optimization

### Tips for Efficient Simulation

1. **Limit Plant Count**: Max 81 (full grid), but 30-50 is optimal
2. **Batch Operations**: Update plants in groups, not individually
3. **Lazy Evaluation**: Don't recalculate unless changed
4. **Property Binding**: Use for UI, but sparingly in logic
5. **Log Wisely**: DEBUG level can be expensive at high speeds

### Profiling

Use Java profiler to identify bottlenecks:
```bash
java -agentlib:hprof=cpu=samples -jar smartGarden.jar
```

---

## Contributing

### Code Review Checklist

- [ ] Code follows naming conventions
- [ ] All public methods have Javadoc
- [ ] Exception handling in place
- [ ] No hardcoded values (use constants)
- [ ] Logging added for significant events
- [ ] UI updates use JavaFX properties
- [ ] Tests written (if applicable)
- [ ] No warnings or errors in build

### Git Workflow

1. Create feature branch: `git checkout -b feature/new-plant-type`
2. Make changes
3. Commit with clear messages: `git commit -m "Add Cactus plant type"`
4. Push branch: `git push origin feature/new-plant-type`
5. Create pull request

---

## FAQ

**Q: Can I use Java 17 instead of 21?**  
A: No, the project uses Java 21 features (pattern matching in switch). Downgrading requires code changes.

**Q: How do I change the grid size?**  
A: Modify `GRID_SIZE` constant in `SmartGardenApplication.java` and update zone initialization in `Garden.java`.

**Q: Can I add database persistence?**  
A: Yes, but requires significant architecture changes. Start by creating a persistence layer and serializing Garden state.

**Q: How do I create a standalone JAR?**  
A: Use `mvn package` then `jpackage` tool to create platform-specific installers.

**Q: Can I run multiple simulations simultaneously?**  
A: Not in current design. Each application instance runs one simulation. You could refactor to support multiple Garden instances.

---

## Conclusion

This guide provides the foundation for understanding and extending the Smart Garden Simulation system. For additional information, refer to:

- Design documentation in `docs/design/`
- UML diagrams (PlantUML format)
- Source code (fully commented)
- User manual for end-user perspective

Happy coding! 🌱

---

**Version**: 1.0.0  
**Last Updated**: December 2024  
**Course**: CSEN 275 - Object-Oriented Analysis & Design

## Recent Changes (v1.0.0)

- **Plant Types**: Expanded to 9 types (Strawberry, Grapevine, Apple, Carrot, Tomato, Onion, Sunflower, Tulip, Rose)
- **Weather Integration**: Sprinkler system now responds to weather changes (stops during rain)
- **Image Resources**: All plant images use local PNG files in `src/main/resources/images/`
- **Pest System**: Simplified to harmful pests only (BeneficialInsect class removed)
- **Animation Engine**: Improved SprinklerAnimationEngine and WaterAnimationEngine with better null safety

