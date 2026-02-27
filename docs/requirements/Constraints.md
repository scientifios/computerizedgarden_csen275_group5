# System Constraints

## Technical Constraints

### TC-1: Platform and Language
- **Constraint**: System must be implemented in Java 21
- **Rationale**: Course requirement and target platform compatibility
- **Impact**: Development team must use Java-specific patterns and libraries

### TC-2: User Interface Framework
- **Constraint**: GUI must use JavaFX 23.0.1
- **Rationale**: Modern, cross-platform UI framework with good Java integration
- **Impact**: Cannot use Swing, AWT, or web-based interfaces

### TC-3: Build System
- **Constraint**: Maven must be used for dependency management and build
- **Rationale**: Industry standard, easy setup, good IDE integration
- **Impact**: Project structure must follow Maven conventions

### TC-4: No External Database
- **Constraint**: All data must be stored in-memory (no SQL/NoSQL databases)
- **Rationale**: Simplify deployment and reduce external dependencies
- **Impact**: Data does not persist between runs; logs are file-based only

### TC-5: Single Application Instance
- **Constraint**: System runs as a standalone desktop application
- **Rationale**: Educational project scope
- **Impact**: No client-server architecture or distributed systems

---

## Performance Constraints

### PC-1: Minimum Hardware Requirements
- **Constraint**: Must run on machines with 8GB RAM, dual-core CPU
- **Rationale**: Standard student/educational computer specs
- **Impact**: Code must be memory-efficient; no resource-heavy operations

### PC-2: UI Responsiveness
- **Constraint**: UI must update within 100ms of state changes
- **Rationale**: User experience requirement for real-time simulation
- **Impact**: UI updates must be optimized; background threading required

### PC-3: Simulation Speed
- **Constraint**: Must support up to 10x speed without lag or skipping
- **Rationale**: Allow users to observe long-term growth quickly
- **Impact**: Algorithms must be efficient; proper timing mechanisms required

### PC-4: Garden Size Limit
- **Constraint**: Maximum 9x9 grid (81 cells)
- **Rationale**: Performance and UI layout considerations
- **Impact**: Scalability beyond this requires architecture changes

### PC-5: Continuous Operation
- **Constraint**: Must run for 24+ hours without memory leaks or crashes
- **Rationale**: Demonstrate robustness and proper resource management
- **Impact**: Requires careful memory management and exception handling

---

## Design Constraints

### DC-1: Object-Oriented Paradigm
- **Constraint**: System must demonstrate OOP principles (encapsulation, inheritance, polymorphism)
- **Rationale**: CSEN 275 course learning objectives
- **Impact**: Design must avoid procedural/functional patterns where OOP applies

### DC-2: Design Patterns
- **Constraint**: Must use appropriate design patterns (MVC, Observer, Strategy, Factory, etc.)
- **Rationale**: Educational requirement to demonstrate pattern knowledge
- **Impact**: May require more complex structure than simpler solutions

### DC-3: Separation of Concerns
- **Constraint**: Clear separation between Model, View, and Controller layers
- **Rationale**: Maintainability and best practices
- **Impact**: No direct coupling between UI and domain logic

### DC-4: Interface-Based Design
- **Constraint**: Automation systems must implement common interfaces
- **Rationale**: Enable extensibility and polymorphic behavior
- **Impact**: More abstract code; requires interface definitions

### DC-5: Exception Handling
- **Constraint**: All exceptions must be caught and handled gracefully
- **Rationale**: Robustness requirement; no crashes
- **Impact**: Extensive try-catch blocks; error handling logic throughout

---

## Documentation Constraints

### DOC-1: Requirements Documentation
- **Constraint**: Must include problem statement, features, user stories, use cases, domain model
- **Rationale**: Academic project requirement; demonstrate analysis skills
- **Impact**: Significant documentation effort before coding

### DOC-2: Design Documentation
- **Constraint**: Must include UML class, sequence, state, activity, and component diagrams
- **Rationale**: Demonstrate design understanding and planning
- **Impact**: Design must be documented in standard UML notation

### DOC-3: User Manual
- **Constraint**: Comprehensive user guide and help documentation required
- **Rationale**: Usability and professional standard
- **Impact**: Time allocation for writing end-user documentation

### DOC-4: Code Documentation
- **Constraint**: All public classes and methods must have JavaDoc-style comments
- **Rationale**: Code maintainability and professional practice
- **Impact**: Additional commenting effort during development

### DOC-5: Logging Requirements
- **Constraint**: All significant events must be logged with timestamps
- **Rationale**: Debugging, analysis, and demonstration purposes
- **Impact**: Logging code throughout system

---

## Functional Constraints

### FC-1: Plant Type Minimum
- **Constraint**: Must support at least 5 distinct plant types
- **Rationale**: Demonstrate polymorphism and realistic diversity
- **Impact**: Requires design of abstract plant class and multiple implementations

### FC-2: Three Automation Modules
- **Constraint**: Must implement at least 3 independent automation systems
- **Rationale**: Project requirement to show complex system integration
- **Impact**: Watering, Heating, and Pest Control are mandatory

### FC-3: Real-Time Visualization
- **Constraint**: Garden state must be visible in real-time, not just after events
- **Rationale**: User experience and demonstration requirement
- **Impact**: UI must use property binding or polling for updates

### FC-4: Manual Override
- **Constraint**: User must be able to manually trigger system actions
- **Rationale**: Control and testing flexibility
- **Impact**: UI must provide manual control buttons

### FC-5: Random Event Generation
- **Constraint**: System must include unpredictable events (weather, pests)
- **Rationale**: Realism and demonstrate handling of stochastic processes
- **Impact**: Random number generation and event scheduling logic required

---

## Time Constraints

### TIME-1: Project Completion
- **Constraint**: Project must be completed within academic term timeline
- **Rationale**: Course deadline
- **Impact**: Feature prioritization; MVP approach recommended

### TIME-2: Simulation Time Unit
- **Constraint**: 1 simulation tick = 1 minute simulation time
- **Rationale**: Balances realism with observable progress
- **Impact**: Growth rates and event frequencies must be calibrated to this scale

---

## Scope Constraints

### SC-1: Educational Focus
- **Constraint**: System is for educational demonstration, not production use
- **Rationale**: Learning objectives take precedence over commercial features
- **Impact**: Some "real-world" features (authentication, cloud sync) are out of scope

### SC-2: Single User
- **Constraint**: No multi-user support or concurrent access
- **Rationale**: Simplifies design; focuses on core functionality
- **Impact**: No user accounts, permissions, or collaboration features

### SC-3: No Physical Integration
- **Constraint**: No real hardware sensors or actuators
- **Rationale**: Software simulation only
- **Impact**: All sensor readings and actions are simulated

### SC-4: No AI/ML
- **Constraint**: No machine learning or advanced AI algorithms
- **Rationale**: Complexity and scope management
- **Impact**: Decision logic uses rule-based systems only

---

## Operational Constraints

### OP-1: Cross-Platform
- **Constraint**: Must run on Windows, macOS, and Linux
- **Rationale**: Accessibility for all students
- **Impact**: Avoid platform-specific APIs; test on multiple OS

### OP-2: No Network Requirement
- **Constraint**: System must operate fully offline
- **Rationale**: No dependency on internet or external services
- **Impact**: No cloud features, online updates, or remote monitoring

### OP-3: Self-Contained Installation
- **Constraint**: Application should run with minimal setup (Java + Maven)
- **Rationale**: Ease of deployment for grading and demonstration
- **Impact**: No complex installation scripts or system modifications

---

## Quality Constraints

### QC-1: Code Quality
- **Constraint**: Code must follow Java conventions and clean code principles
- **Rationale**: Grading criteria and maintainability
- **Impact**: Code reviews and refactoring required

### QC-2: Test Coverage
- **Constraint**: Critical components should have unit tests
- **Rationale**: Demonstrate quality assurance understanding
- **Impact**: JUnit tests for core classes

### QC-3: Error-Free Operation
- **Constraint**: System must not crash during normal operation
- **Rationale**: Reliability requirement
- **Impact**: Extensive error handling and validation

---

## Compliance Constraints

### COMP-1: Academic Integrity
- **Constraint**: Code must be original or properly attributed
- **Rationale**: University academic honesty policies
- **Impact**: Cannot directly copy reference implementation; must demonstrate understanding

### COMP-2: License Compatibility
- **Constraint**: All dependencies must have compatible open-source licenses
- **Rationale**: Legal compliance for educational use
- **Impact**: Check licenses for all Maven dependencies

