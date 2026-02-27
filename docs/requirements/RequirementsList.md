# Requirements List

## Functional Requirements

### FR-1: Garden Management
- **FR-1.1**: The system shall support a grid-based garden layout with configurable dimensions (minimum 9x9)
- **FR-1.2**: The system shall allow users to place plants in any empty grid cell
- **FR-1.3**: The system shall support 9 different plant types (3 Fruits, 3 Vegetables, 3 Flowers) with unique characteristics
- **FR-1.4**: The system shall track individual plant states including health, growth stage, and resource levels
- **FR-1.5**: The system shall remove dead plants from the simulation after a configurable decay period

### FR-2: Plant Lifecycle
- **FR-2.1**: Each plant shall progress through defined growth stages (Seed → Seedling → Mature → Flowering)
- **FR-2.2**: Plants shall have individual water requirements that vary by type
- **FR-2.3**: Plants shall have individual sunlight requirements that vary by type
- **FR-2.4**: Plants shall have configurable lifespan limits
- **FR-2.5**: Plant health shall be affected by water levels, temperature, pests, and weather

### FR-3: Watering System
- **FR-3.1**: The system shall automatically water plants when soil moisture drops below threshold
- **FR-3.2**: The system shall divide the garden into zones with independent watering schedules
- **FR-3.3**: Each zone shall have moisture sensors that report current levels
- **FR-3.4**: The system shall automatically stop sprinklers when rain is detected
- **FR-3.5**: Users shall be able to manually trigger watering for specific zones
- **FR-3.6**: The system shall track total water consumption
- **FR-3.7**: The system shall prevent over-watering by checking current moisture levels

### FR-4: Heating System
- **FR-4.1**: The system shall monitor ambient temperature continuously
- **FR-4.2**: The system shall activate heating when temperature falls below configured threshold
- **FR-4.3**: The system shall deactivate heating when temperature exceeds configured threshold
- **FR-4.4**: Temperature changes shall affect plant growth rates
- **FR-4.5**: Users shall be able to manually override heating system

### FR-5: Pest Control System
- **FR-5.1**: The system shall simulate random pest infestations (harmful pests only)
- **FR-5.2**: The system shall detect pest presence in affected zones
- **FR-5.3**: The system shall automatically apply treatment when infestation level exceeds threshold
- **FR-5.4**: The system shall track pest damage to individual plants
- **FR-5.5**: The system shall effectively reduce pest populations through treatment

### FR-6: Simulation Engine
- **FR-6.1**: The simulation shall use discrete time steps (1 tick = 1 minute simulation time)
- **FR-6.2**: The simulation shall support variable speed (1x, 5x, 10x, etc.)
- **FR-6.3**: Users shall be able to start, pause, and stop the simulation
- **FR-6.4**: The simulation shall trigger system updates on each tick
- **FR-6.5**: Random events shall be triggered based on probability distributions

### FR-7: Weather System
- **FR-7.1**: The system shall simulate 5 weather conditions (Sunny, Rainy, Cloudy, Windy, Snowy)
- **FR-7.2**: Weather shall change randomly based on realistic probabilities
- **FR-7.3**: Weather conditions shall affect plant health and growth
- **FR-7.4**: Rainy weather shall contribute to soil moisture levels and automatically stop sprinklers
- **FR-7.5**: The UI shall display current weather with visual indicators

### FR-8: User Interface
- **FR-8.1**: The UI shall display the garden grid with real-time plant visualization
- **FR-8.2**: Each plant shall be color-coded by health status (Green=Healthy, Yellow=Stressed, Red=Critical)
- **FR-8.3**: The UI shall provide simulation control buttons (Start, Pause, Stop)
- **FR-8.4**: The UI shall display current system status for all modules
- **FR-8.5**: The UI shall show elapsed simulation time
- **FR-8.6**: Users shall be able to hover over plants to see detailed information

### FR-9: Logging System
- **FR-9.1**: All system events shall be logged with timestamps
- **FR-9.2**: Log entries shall be categorized by event type
- **FR-9.3**: Logs shall be written to file in real-time
- **FR-9.4**: The UI shall display recent log entries in a scrollable list
- **FR-9.5**: Users shall be able to filter logs by category
- **FR-9.6**: Log files shall be created per simulation session with unique names

## Non-Functional Requirements

### NFR-1: Performance
- **NFR-1.1**: The simulation shall run smoothly at speeds up to 10x without lag
- **NFR-1.2**: UI updates shall occur within 100ms of state changes
- **NFR-1.3**: Memory usage shall remain stable during 24+ hour runs
- **NFR-1.4**: System shall handle gardens with up to 81 plants (9x9) efficiently

### NFR-2: Reliability
- **NFR-2.1**: The system shall run continuously for at least 24 hours without crashes
- **NFR-2.2**: All exceptions shall be caught and logged without terminating the application
- **NFR-2.3**: System shall gracefully handle invalid user inputs
- **NFR-2.4**: Data consistency shall be maintained across all components

### NFR-3: Maintainability
- **NFR-3.1**: Code shall follow object-oriented design principles
- **NFR-3.2**: Classes shall have single, well-defined responsibilities
- **NFR-3.3**: System shall use interfaces and abstract classes for extensibility
- **NFR-3.4**: Code shall be documented with JavaDoc comments
- **NFR-3.5**: Package structure shall logically separate concerns

### NFR-4: Usability
- **NFR-4.1**: Users shall understand basic operations within 5 minutes
- **NFR-4.2**: UI controls shall be clearly labeled and intuitive
- **NFR-4.3**: System shall provide visual feedback for all actions
- **NFR-4.4**: Error messages shall be clear and actionable

### NFR-5: Scalability
- **NFR-5.1**: System architecture shall support adding new plant types without modifying existing code
- **NFR-5.2**: New automation modules shall be addable through defined interfaces
- **NFR-5.3**: Garden grid size shall be configurable without code changes

### NFR-6: Portability
- **NFR-6.1**: System shall run on Windows, macOS, and Linux
- **NFR-6.2**: System shall use cross-platform JavaFX for UI
- **NFR-6.3**: File paths shall use platform-independent conventions

## Constraints

### C-1: Technology Constraints
- **C-1.1**: System shall be implemented in Java 21
- **C-1.2**: UI shall use JavaFX 23.0.1
- **C-1.3**: Build system shall use Maven
- **C-1.4**: No external database systems (in-memory data only)

### C-2: Development Constraints
- **C-2.1**: Project shall follow CSEN 275 OOD requirements
- **C-2.2**: Code shall demonstrate proper OOP principles
- **C-2.3**: System shall include comprehensive documentation

### C-3: Resource Constraints
- **C-3.1**: System shall run on standard development machines (8GB RAM minimum)
- **C-3.2**: Log files shall not exceed 100MB per session

