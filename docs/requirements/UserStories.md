# User Stories and Scenarios

## Epic 1: Garden Setup and Management

### US-1.1: Initialize Garden
**As a** garden manager  
**I want to** set up a new garden with a grid layout  
**So that** I can begin planting and managing my garden

**Acceptance Criteria:**
- Garden grid is displayed with empty cells
- Grid size is configurable (default 9x9)
- All cells are initially empty and available for planting

**Scenario 1.1.1: First-time Setup**
```
GIVEN the application is launched for the first time
WHEN the main window appears
THEN I should see an empty 9x9 garden grid
AND simulation controls should be available
AND all systems should be in idle state
```

### US-1.2: Plant Different Crops
**As a** garden manager  
**I want to** plant different types of plants in specific locations  
**So that** I can create a diverse garden

**Acceptance Criteria:**
- Multiple plant types are available (9 types: Strawberry, Grapevine, Apple, Carrot, Tomato, Onion, Sunflower, Tulip, Rose)
- Each plant has unique visual representation
- Plants can be placed in any empty cell
- Plant information is displayed on hover

**Scenario 1.2.1: Planting Multiple Types**
```
GIVEN I have an empty garden
WHEN I select "Tomato" from the plant menu
AND I click on cell (2,3)
THEN a tomato plant should appear at that location
AND the cell should show as occupied
AND plant status should be "Seed" stage
```

### US-1.3: Monitor Plant Health
**As a** garden manager  
**I want to** see the health status of all plants at a glance  
**So that** I can identify plants that need attention

**Acceptance Criteria:**
- Plants are color-coded by health (Green/Yellow/Red)
- Clicking a plant shows detailed status
- Status includes growth stage, water level, health percentage

**Scenario 1.3.1: Identify Stressed Plants**
```
GIVEN I have a garden with 10 plants
WHEN one plant's water level drops below 30%
THEN that plant should turn yellow
AND hover tooltip should show "Needs Water"
```

## Epic 2: Automated Watering

### US-2.1: Automatic Watering
**As a** garden manager  
**I want** the system to automatically water plants  
**So that** I don't need to manually check and water each plant

**Acceptance Criteria:**
- System checks soil moisture every simulation tick
- Watering triggers when moisture < threshold
- Watering is logged with timestamp
- Visual indicator shows watering in progress

**Scenario 2.1.1: Automated Watering Trigger**
```
GIVEN the simulation is running
WHEN zone 1's moisture drops below 40%
AND plants in zone 1 need water
THEN the sprinkler for zone 1 should activate
AND moisture level should increase
AND plants should show improved health
```

## Epic 3: Temperature Control

### US-3.1: Automatic Heating
**As a** garden manager  
**I want** the system to maintain optimal temperature  
**So that** plants don't suffer from cold stress

**Acceptance Criteria:**
- Temperature is monitored continuously
- Heating activates when temp < minimum threshold
- Heating deactivates when temp > maximum threshold
- Temperature affects plant growth rate

**Scenario 3.1.1: Cold Night Protection**
```
GIVEN it's nighttime in the simulation
WHEN temperature drops to 10°C
AND threshold is set to 15°C
THEN heating system should activate
AND temperature should increase by 5°C per minute
AND plants should show "Warming" status
```

## Epic 4: Pest Management

### US-4.1: Pest Detection
**As a** garden manager  
**I want** the system to detect pest infestations early  
**So that** I can prevent plant damage

**Acceptance Criteria:**
- Pests appear randomly in garden
- Infestation level is tracked per zone
- Visual indicators show pest presence
- Alerts trigger at medium/high infestation

**Scenario 4.1.1: Early Detection**
```
GIVEN the simulation has been running for 2 hours
WHEN pests appear in zone 5
THEN zone 5 should show a pest indicator
AND affected plants should show orange color
AND log should record "Pest detected in Zone 5"
```

### US-4.2: Automatic Pest Control
**As a** garden manager  
**I want** automatic pesticide application when infestations are severe  
**So that** plants are protected without my constant monitoring

**Acceptance Criteria:**
- Treatment activates at high infestation threshold
- Treatment reduces pest count effectively
- Treatment is logged with details

**Scenario 4.2.1: Treatment Application**
```
GIVEN zone 3 has high pest infestation (>70%)
WHEN the pest control system checks
THEN pesticide should be applied to zone 3
AND pest count should decrease by 50%
AND log should show treatment details
```

## Epic 5: Simulation Control

### US-5.1: Speed Control
**As a** user  
**I want to** adjust simulation speed  
**So that** I can observe changes quickly or in detail

**Acceptance Criteria:**
- Speed options: 1x, 5x, 10x available
- Speed change takes effect immediately
- UI updates remain smooth at all speeds
- Elapsed time display reflects current speed

**Scenario 5.1.1: Fast-Forward Simulation**
```
GIVEN simulation is running at 1x speed
WHEN I select "10x" from speed dropdown
THEN time should progress 10 times faster
AND plants should grow 10x faster
AND UI should remain responsive
```

### US-5.2: Pause and Resume
**As a** user  
**I want to** pause the simulation  
**So that** I can examine the current state or take a break

**Acceptance Criteria:**
- Pause button stops all simulation updates
- Garden state is frozen
- Resume button continues from exact state
- Pause/Resume is logged

**Scenario 5.2.1: Examination Pause**
```
GIVEN simulation is running
WHEN I click "Pause" button
THEN all plant growth should stop
AND system updates should stop
AND I can examine plant details
WHEN I click "Resume"
THEN simulation continues normally
```

## Epic 6: Logging and Monitoring

### US-6.1: Event Log Viewing
**As a** garden manager  
**I want to** view all system events in real-time  
**So that** I can understand what's happening in my garden

**Acceptance Criteria:**
- Log viewer shows recent events
- Events are timestamped
- Events are categorized (INFO, WARN, ERROR)
- Log auto-scrolls to latest entry

**Scenario 6.1.1: Monitor Events**
```
GIVEN simulation is running
WHEN a plant grows to next stage
THEN log should show "[HH:MM:SS] INFO: Tomato at (2,3) advanced to Mature stage"
AND log entry should appear in UI immediately
```

### US-6.2: Filter Logs by Category
**As a** garden manager  
**I want to** filter logs by event type  
**So that** I can focus on specific activities

**Acceptance Criteria:**
- Filter options: All, Watering, Heating, Pests, Plant Growth
- Filtering updates log view immediately
- Filtered events remain timestamped correctly

**Scenario 6.2.1: View Only Watering Events**
```
GIVEN log contains mixed event types
WHEN I select "Watering" filter
THEN only watering-related events should display
AND total count should show filtered count
```

## Epic 7: System Health

### US-7.1: System Status Dashboard
**As a** garden manager  
**I want to** see overall system health at a glance  
**So that** I know everything is functioning properly

**Acceptance Criteria:**
- Dashboard shows status of all subsystems
- Status indicators: Active, Idle, Error
- Resource levels displayed (water, energy)
- Uptime is tracked and displayed

**Scenario 7.1.1: Healthy System Check**
```
GIVEN all systems are functioning normally
WHEN I view the system dashboard
THEN I should see green indicators for:
  - Watering System: Active
  - Heating System: Idle
  - Pest Control: Active
AND resource meters should show adequate levels
```

### US-7.2: Handle System Errors Gracefully
**As a** garden manager  
**I want** the system to recover from errors automatically  
**So that** my garden simulation doesn't crash

**Acceptance Criteria:**
- Exceptions are caught and logged
- System continues running after errors
- Error notification shows in UI
- Recovery attempt is made automatically

**Scenario 7.2.1: Sensor Malfunction**
```
GIVEN a moisture sensor throws an exception
WHEN the error occurs
THEN error should be logged with details
AND system should use last known value
AND simulation should continue running
AND user should see warning notification
```

## Epic 8: Weather System

### US-8.1: Dynamic Weather
**As a** garden manager  
**I want** realistic weather changes  
**So that** the simulation feels authentic

**Acceptance Criteria:**
- Weather changes every 30-60 simulation minutes
- Weather types: Sunny, Rainy, Cloudy, Windy
- Weather affects plant growth and moisture
- Current weather displayed with icon

**Scenario 8.1.1: Rainy Day Benefits**
```
GIVEN weather changes to "Rainy"
WHEN rain continues for 10 minutes
THEN soil moisture should increase naturally
AND plants should show improved growth
AND watering system should reduce frequency
```

