# Design Overview

## Architecture

The Smart Garden Simulation System follows a **layered architecture** with clear separation of concerns:

### 1. Presentation Layer (JavaFX UI)
- **Responsibility**: User interaction and visualization
- **Components**: FXML views, View Controllers
- **Pattern**: Model-View-Controller (MVC)

### 2. Application Layer
- **Responsibility**: Orchestration and coordination
- **Components**: Garden Controller, Simulation Controller
- **Pattern**: Facade, Mediator

### 3. Domain Layer
- **Responsibility**: Core business logic
- **Components**: Garden, Plant hierarchy, Growth logic
- **Pattern**: Domain Model, Abstract Factory

### 4. System Layer
- **Responsibility**: Automation subsystems
- **Components**: Watering (weather-aware), Heating, Pest Control, Weather
- **Pattern**: Strategy, Observer

### 5. Infrastructure Layer
- **Responsibility**: Cross-cutting concerns
- **Components**: Logger, Configuration, Event Scheduler
- **Pattern**: Singleton, Command

## Design Patterns Applied

### Creational Patterns
1. **Factory Pattern**: Plant creation based on type
2. **Singleton Pattern**: Logger, Configuration instances
3. **Builder Pattern**: Complex garden initialization

### Structural Patterns
1. **Facade Pattern**: GardenController simplifies subsystem access
2. **Composite Pattern**: Garden contains Zones contains Plants
3. **Decorator Pattern**: Weather effects modify plant behavior

### Behavioral Patterns
1. **Observer Pattern**: UI updates when simulation state changes
2. **Strategy Pattern**: Different watering/heating strategies
3. **State Pattern**: Plant lifecycle states, Simulation states
4. **Command Pattern**: User actions encapsulated as commands
5. **Template Method**: Common sensor reading logic

## Key Design Decisions

### 1. JavaFX Properties for Reactive UI
**Decision**: Use JavaFX observable properties for plant attributes  
**Rationale**: Enables automatic UI updates without manual polling  
**Trade-off**: Slightly more complex model code, but much cleaner UI code

### 2. Time-Based Simulation with Timeline
**Decision**: Use JavaFX Timeline for discrete time steps  
**Rationale**: Consistent timing, easy speed control, built-in pause/resume  
**Trade-off**: Limited to JavaFX thread, but sufficient for this application

### 3. Zone-Based Garden Organization
**Decision**: Divide garden into zones for system management  
**Rationale**: Realistic modeling of real irrigation zones, improves performance  
**Trade-off**: Less granular than per-plant control, but more maintainable

### 4. Abstract Plant Base Class
**Decision**: Plant as abstract class (not interface)  
**Rationale**: Provides common implementation, enforces contract  
**Trade-off**: Single inheritance limitation, but no need for multiple inheritance here

### 5. Centralized Logging
**Decision**: Single Logger instance used throughout  
**Rationale**: Consistent log format, easier to manage file I/O  
**Trade-off**: Potential bottleneck, but buffered writing mitigates this

### 8. Weather-Aware Sprinkler Control
**Decision**: Sprinklers automatically stop when rain is detected  
**Rationale**: Prevents unnecessary water usage, realistic behavior  
**Trade-off**: Requires coordination between WeatherSystem and WateringSystem, but improves efficiency

### 6. No Persistence Layer
**Decision**: All data in-memory, logs to file only  
**Rationale**: Simplifies architecture, meets requirements  
**Trade-off**: No save/load capability, acceptable for educational project

### 7. Event-Driven Random Events
**Decision**: Probability-based random event generation each tick  
**Rationale**: Simple implementation, realistic unpredictability  
**Trade-off**: Not schedulable, but adds to realism

## Class Organization

### Package Structure
```
edu.scu.csen275.smartgarden/
├── model/              # Domain entities
│   ├── plant/          # Plant hierarchy
│   ├── Garden.java
│   ├── Zone.java
│   └── Position.java
├── system/             # Automation systems
│   ├── watering/
│   ├── heating/
│   ├── pest/
│   └── weather/
├── simulation/         # Simulation engine
│   ├── SimulationEngine.java
│   ├── EventScheduler.java
│   └── SimulationState.java
├── controller/         # Application logic
│   ├── GardenController.java
│   └── MainViewController.java
├── util/               # Infrastructure
│   ├── Logger.java
│   └── Configuration.java
└── SmartGardenApplication.java
```

## UML Diagrams

The following UML diagrams document the system design:

1. **Class Diagram** (`ClassDiagram.puml`)  
   - Complete class structure with relationships
   - All attributes and methods
   - Inheritance and composition relationships

2. **Sequence Diagrams**  
   - `SequenceDiagram_StartSimulation.puml`: Simulation startup flow
   - `SequenceDiagram_AutomaticWatering.puml`: Watering cycle interaction
   - `SequenceDiagram_PestControl.puml`: Pest detection and treatment

3. **State Diagrams**  
   - `StateDiagram_PlantLifecycle.puml`: Plant growth stages
   - `StateDiagram_WateringSystem.puml`: Watering system states
   - `StateDiagram_Simulation.puml`: Simulation engine states

4. **Activity Diagrams**  
   - `ActivityDiagram_SimulationTick.puml`: Single tick processing
   - `ActivityDiagram_PlantOperation.puml`: Planting a new plant

5. **Component Diagram** (`ComponentDiagram.puml`)  
   - High-level system architecture
   - Component relationships and interfaces

## Concurrency Model

### Threading Strategy
- **UI Thread (JavaFX Application Thread)**: All UI updates
- **Timeline Thread**: Simulation tick processing
- **Platform.runLater()**: Used to update UI from simulation thread

### Thread Safety Considerations
1. **Immutable Objects**: Position, Configuration keys
2. **Synchronized Access**: Logger buffer writes
3. **JavaFX Properties**: Automatically thread-safe for UI binding
4. **No Shared Mutable State**: Each system manages its own state

## Error Handling Strategy

### Exception Hierarchy
```
RuntimeException
├── GardenException (base for all garden errors)
│   ├── InvalidPositionException
│   ├── PlantNotFoundException
│   └── SystemException
│       ├── WateringException
│       ├── HeatingException
│       └── PestControlException
```

### Error Handling Approach
1. **Validation**: Check preconditions, throw exceptions early
2. **Catching**: Catch at system boundaries (UI, simulation tick)
3. **Logging**: All exceptions logged with context
4. **Recovery**: Attempt to continue with degraded functionality
5. **User Notification**: Show non-intrusive alerts for non-critical errors

## Performance Considerations

### Optimization Strategies
1. **Lazy Initialization**: Create objects only when needed
2. **Object Reuse**: Reuse Position objects via caching
3. **Batch Updates**: Group UI updates to reduce overhead
4. **Efficient Collections**: Use appropriate data structures (HashMap for lookups, ArrayList for iteration)
5. **Avoid Premature Optimization**: Profile before optimizing

### Expected Performance
- **Garden Size**: Up to 81 plants (9x9 grid)
- **Update Frequency**: 1-10 ticks per second
- **UI Frame Rate**: 30-60 FPS
- **Memory**: < 500 MB typical usage
- **Startup Time**: < 5 seconds

## Extensibility Points

The system is designed for easy extension:

1. **New Plant Types**: Extend Plant abstract class
2. **New Automation Systems**: Implement IAutomationSystem interface
3. **New Sensors**: Implement ISensor interface
4. **New Weather Types**: Add to Weather enum, extend WeatherSystem logic
5. **New Events**: Add to EventType enum, extend EventScheduler

## Testing Strategy

### Unit Testing
- **Model Classes**: Test growth logic, water consumption, health calculations
- **System Classes**: Test triggering conditions, action execution
- **Utility Classes**: Test logging, configuration loading

### Integration Testing
- **System Interaction**: Test watering + heating + pest control coordination
- **UI Integration**: Test controller -> model -> UI update flow

### Long-Running Tests
- **24-Hour Test**: Run simulation at 10x speed for 2.4 hours real-time
- **Memory Leak Detection**: Monitor memory over extended run
- **Stability Test**: Verify no crashes or deadlocks

## Documentation Standards

### Code Documentation
- **JavaDoc**: All public classes and methods
- **Inline Comments**: Complex algorithms and business rules
- **README**: Setup and usage instructions

### Design Documentation
- **UML Diagrams**: Visual design reference
- **This Document**: High-level design rationale
- **User Manual**: End-user guide

## Compliance

### CSEN 275 Requirements
✅ Object-Oriented Design  
✅ Design Patterns  
✅ UML Diagrams  
✅ Requirements Documentation  
✅ Clean Code Principles  
✅ 24-Hour Operation Capability  

### Java Best Practices
✅ Naming Conventions  
✅ Package Organization  
✅ Exception Handling  
✅ Resource Management  
✅ Documentation  

