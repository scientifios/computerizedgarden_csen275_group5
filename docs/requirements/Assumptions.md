# System Assumptions

## Environment Assumptions

### ENV-1: Java Runtime
**Assumption**: Users have Java 21 or higher installed on their systems  
**Justification**: Required for running JavaFX applications  
**Risk**: If false, application will not launch  
**Mitigation**: Provide clear installation instructions with JDK download links

### ENV-2: Display Resolution
**Assumption**: User displays are at least 1280x720 resolution  
**Justification**: UI is designed for this minimum size  
**Risk**: Layout may be cramped or unusable on smaller screens  
**Mitigation**: Use responsive layout where possible; document minimum requirements

### ENV-3: Operating System
**Assumption**: System runs on Windows 10+, macOS 10.14+, or modern Linux distributions  
**Justification**: JavaFX support for these platforms  
**Risk**: May not work on very old or embedded systems  
**Mitigation**: Test on target platforms; document compatibility

### ENV-4: File System Access
**Assumption**: Application has read/write access to local file system for logs  
**Justification**: Logging requires file creation in logs/ directory  
**Risk**: Permissions errors in restricted environments  
**Mitigation**: Graceful handling of file access failures; inform user

---

## User Assumptions

### USER-1: Basic Computer Literacy
**Assumption**: Users can navigate GUI applications and understand basic concepts (click, select, scroll)  
**Justification**: Standard for educational software  
**Risk**: Complete novices may struggle initially  
**Mitigation**: Provide user manual with screenshots; intuitive UI design

### USER-2: Gardening Knowledge
**Assumption**: Users have basic understanding of plant needs (water, sunlight, temperature)  
**Justification**: System simulates real-world gardening concepts  
**Risk**: Users unfamiliar with gardening may not understand plant requirements  
**Mitigation**: Provide tooltips and help text explaining concepts

### USER-3: English Language
**Assumption**: Users can read English  
**Justification**: All UI text and documentation is in English  
**Risk**: Non-English speakers may struggle  
**Mitigation**: Use clear, simple language; icons and visual indicators

### USER-4: Supervision During Long Runs
**Assumption**: Users will periodically check on 24-hour simulation runs  
**Justification**: Not a critical monitoring system  
**Risk**: Issues may go unnoticed for hours  
**Mitigation**: Logging captures all events for later review

---

## Data Assumptions

### DATA-1: Initial State Validity
**Assumption**: Garden starts in a valid, consistent state  
**Justification**: No data is loaded from external sources  
**Risk**: N/A (controlled startup)  
**Mitigation**: Validate initial configuration in code

### DATA-2: No Data Persistence
**Assumption**: Users accept that garden state is lost when application closes  
**Justification**: In-memory only, no database  
**Risk**: Users may expect save/load functionality  
**Mitigation**: Document clearly that state is not saved; consider future enhancement

### DATA-3: Reasonable Input Values
**Assumption**: Users will not intentionally provide extreme or malicious inputs  
**Justification**: Educational context, not security-critical  
**Risk**: Extreme values could cause unexpected behavior  
**Mitigation**: Input validation on key parameters; bounds checking

### DATA-4: Log File Growth
**Assumption**: Log files will not exceed available disk space during reasonable use (< 100MB per session)  
**Justification**: Event frequency and message sizes are modest  
**Risk**: Very long runs with high verbosity could create large files  
**Mitigation**: Log rotation or size limits if needed

---

## Simulation Assumptions

### SIM-1: Simplified Plant Biology
**Assumption**: Abstract plant growth model is acceptable (not botanically accurate)  
**Justification**: Educational simulation, not scientific model  
**Risk**: Unrealistic behaviors may confuse users  
**Mitigation**: Document that model is simplified; adjust parameters for plausibility

### SIM-2: Discrete Time Steps
**Assumption**: 1-minute time steps provide sufficient granularity  
**Justification**: Balances realism with performance  
**Risk**: Some events may seem abrupt or discontinuous  
**Mitigation**: Smooth visual transitions; reasonable event frequencies

### SIM-3: Uniform Zone Conditions
**Assumption**: All plants in a zone experience identical conditions (moisture, temperature)  
**Justification**: Simplifies sensor and control logic  
**Risk**: Less realistic than per-plant variability  
**Mitigation**: Acceptable simplification for scope of project

### SIM-4: Random Events Follow Distributions
**Assumption**: Probability-based events (weather, pests) will seem realistic over time  
**Justification**: Standard stochastic simulation approach  
**Risk**: Short runs may have unusual patterns (e.g., no rain for hours)  
**Mitigation**: Tune probabilities for desired outcomes; long runs will average out

### SIM-5: Linear System Responses
**Assumption**: Systems respond immediately and linearly (e.g., watering instantly adds moisture)  
**Justification**: Simplifies control algorithms  
**Risk**: Doesn't model real-world lag or non-linear effects  
**Mitigation**: Acceptable for educational purposes

---

## System Assumptions

### SYS-1: Single Threaded Safety
**Assumption**: JavaFX threading model is correctly followed (UI updates on FX thread)  
**Justification**: Required for JavaFX applications  
**Risk**: Threading bugs could cause UI freezes or corruption  
**Mitigation**: Use Platform.runLater() for all UI updates from background threads

### SYS-2: No Concurrent Gardens
**Assumption**: Only one garden simulation runs per application instance  
**Justification**: Simplifies architecture  
**Risk**: Users may want to compare multiple gardens  
**Mitigation**: Document limitation; could be future enhancement

### SYS-3: Deterministic Core Logic (Except Random Events)
**Assumption**: Same initial conditions and random seed produce same results  
**Justification**: Useful for testing and debugging  
**Risk**: Harder to reproduce issues if not using seeded random  
**Mitigation**: Option to set random seed for testing

### SYS-4: No External Dependencies (Runtime)
**Assumption**: Application runs without internet or external services  
**Justification**: Self-contained educational tool  
**Risk**: Cannot leverage cloud features or remote data  
**Mitigation**: N/A (design choice)

### SYS-5: Memory Sufficient
**Assumption**: 8GB RAM is enough for 81 plants + simulation + UI  
**Justification**: Calculation of object sizes and typical usage  
**Risk**: Memory leaks could exhaust memory over 24 hours  
**Mitigation**: Careful memory management; periodic monitoring during testing

---

## Performance Assumptions

### PERF-1: UI Refresh Rate
**Assumption**: 10-30 FPS UI updates are sufficient for smooth visualization  
**Justification**: Plant growth is slow; high frame rates not needed  
**Risk**: Jerky animations or laggy feel if lower  
**Mitigation**: Optimize rendering; use JavaFX animations

### PERF-2: Tick Processing Time
**Assumption**: Processing one simulation tick takes < 10ms  
**Justification**: Allows 10x speed (100 ticks/sec) without lag  
**Risk**: Complex calculations could slow down  
**Mitigation**: Profile performance; optimize hot paths

### PERF-3: Log Write Performance
**Assumption**: Writing logs to disk doesn't block simulation  
**Justification**: Use buffered I/O  
**Risk**: Excessive logging could cause delays  
**Mitigation**: Async logging or batch writes

---

## Integration Assumptions

### INT-1: Module Independence
**Assumption**: Watering, Heating, and Pest Control systems can operate independently  
**Justification**: Modular design principle  
**Risk**: Some coordination may be needed (e.g., don't water during freezing)  
**Mitigation**: Minimal coupling through well-defined interfaces

### INT-2: Sensor Reliability
**Assumption**: Simulated sensors always return valid values (or explicit error state)  
**Justification**: Controlled simulation environment  
**Risk**: N/A (not modeling sensor hardware failures unless by design)  
**Mitigation**: Optional feature to simulate sensor malfunctions

### INT-3: Event Ordering
**Assumption**: Order of operations within a tick doesn't critically affect outcomes  
**Justification**: Systems are loosely coupled  
**Risk**: Race conditions or unexpected interactions  
**Mitigation**: Define clear update order; test edge cases

---

## Testing Assumptions

### TEST-1: Manual Testing Primary
**Assumption**: Visual observation and manual testing are primary validation methods  
**Justification**: Educational project; automated testing is secondary  
**Risk**: May miss subtle bugs  
**Mitigation**: Run extended tests (24-hour runs); peer review

### TEST-2: No Production Data
**Assumption**: No real-world data to validate against  
**Justification**: Simulation, not scientific model  
**Risk**: Hard to assess "correctness" objectively  
**Mitigation**: Ensure behaviors are reasonable and consistent with design

### TEST-3: Development Environment Testing
**Assumption**: Testing on developer machines is representative of user environments  
**Justification**: Standard Java application  
**Risk**: Environment-specific issues may appear on other systems  
**Mitigation**: Test on multiple OS; document requirements

---

## Maintenance Assumptions

### MAINT-1: Short-Term Project
**Assumption**: System will be used for one academic term, not long-term production  
**Justification**: Educational project scope  
**Risk**: Design may not accommodate future major enhancements  
**Mitigation**: Good OO design allows for extensions if needed

### MAINT-2: Limited Support
**Assumption**: No ongoing support or updates after course completion  
**Justification**: Project timeline and resources  
**Risk**: Bugs discovered later won't be fixed  
**Mitigation**: Thorough testing before submission

### MAINT-3: Documentation Sufficiency
**Assumption**: Provided documentation is sufficient for understanding and running the system  
**Justification**: Comprehensive docs are being created  
**Risk**: Undocumented features or edge cases  
**Mitigation**: Include examples and common scenarios in manuals

---

## Business/Academic Assumptions

### BUS-1: Grading Criteria
**Assumption**: Project will be evaluated on OO design, functionality, and documentation  
**Justification**: CSEN 275 course objectives  
**Risk**: Misalignment with actual grading rubric  
**Mitigation**: Verify requirements with instructor; follow assignment guidelines

### BUS-2: No Commercial Use
**Assumption**: System is for educational demonstration only  
**Justification**: Academic context  
**Risk**: N/A  
**Mitigation**: N/A

### BUS-3: Collaboration Allowed
**Assumption**: Reference to existing project patterns is acceptable if not plagiarized  
**Justification**: Learning from examples is encouraged  
**Risk**: Crossing line into copying  
**Mitigation**: Understand and reimplement; cite sources; add improvements

