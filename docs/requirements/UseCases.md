# Use Cases

## UC-01: Start Garden Simulation

**Actor:** Garden Manager  
**Preconditions:**
- Application is installed and launched
- Garden has at least one plant placed

**Main Flow:**
1. User clicks "Start Simulation" button
2. System initializes all subsystems (Watering, Heating, Pest Control)
3. System starts simulation clock at 1x speed
4. System begins monitoring all sensors
5. System starts periodic updates (every tick)
6. UI updates to show "Running" status
7. Log records "Simulation started at [timestamp]"

**Postconditions:**
- Simulation is running
- All systems are active
- Time is progressing
- Plants begin growing

**Alternative Flows:**

**AF-01a: No Plants Present**
1. System displays warning: "Garden is empty. Add plants before starting."
2. Simulation does not start
3. User can add plants and retry

**AF-01b: System Already Running**
1. Button is disabled (grayed out)
2. "Stop" and "Pause" buttons are active instead

---

## UC-02: Place Plant in Garden

**Actor:** Garden Manager  
**Preconditions:**
- Garden grid is displayed
- Selected cell is empty

**Main Flow:**
1. User selects plant type from plant selector menu
2. User clicks on an empty grid cell
3. System validates cell is empty
4. System creates new plant instance with default properties
5. System places plant at selected position
6. System registers plant with all relevant systems (watering, heating)
7. UI displays plant icon in the cell
8. System logs "Planted [PlantType] at position (row, col)"

**Postconditions:**
- Plant exists in the garden
- Plant is tracked by simulation
- Cell is marked as occupied

**Alternative Flows:**

**AF-02a: Cell Already Occupied**
1. System displays error: "Cell already contains a plant"
2. Plant is not placed
3. User can select different cell

**AF-02b: Invalid Plant Type**
1. System displays error: "Invalid plant selection"
2. User must select valid plant type

---

## UC-03: Automatic Watering Cycle

**Actor:** Watering System (Automated)  
**Preconditions:**
- Simulation is running
- Plants exist in garden
- Water supply is available

**Main Flow:**
1. System timer triggers moisture check (every 5 minutes simulation time)
2. For each zone:
   a. Moisture sensor reads current level
   b. System compares to threshold (default 40%)
   c. If below threshold and plants present:
      - Calculate water needed
      - Activate sprinkler for zone
      - Animate watering visual
      - Distribute water to all plants in zone
      - Update moisture level
      - Log watering event
3. System updates UI to show new moisture levels
4. System deactivates sprinklers

**Postconditions:**
- Soil moisture is adequate
- Plants received water
- Water consumption is recorded

**Alternative Flows:**

**AF-03a: No Plants in Zone**
1. System skips watering for that zone
2. Logs "Zone [N] skipped - no plants present"

**AF-03b: Water Supply Low**
1. System displays warning: "Water supply below 10%"
2. Watering continues but at reduced rate
3. User notified to check water system

**AF-03c: Sensor Malfunction**
1. System detects sensor error
2. Uses last known good reading
3. Logs error with timestamp
4. Continues operation with backup logic

---

## UC-04: Detect and Control Pests

**Actor:** Pest Control System (Automated)  
**Preconditions:**
- Simulation is running
- Plants exist in garden

**Main Flow:**
1. System randomly generates pest appearance (based on probability)
2. Pest spawns in specific zone
3. System increments pest count for affected plants
4. System evaluates infestation level:
   - Low: 1-3 pests per plant → Monitor only
   - Medium: 4-6 pests → Visual warning
   - High: 7+ pests → Trigger treatment
5. If High threshold reached:
   a. System activates pest control module
   b. Applies pesticide to zone
   c. Reduces pest count by 50-70%
   d. Updates plant health status
   e. Logs treatment action
6. System updates UI with pest indicators

**Postconditions:**
- Pest levels are controlled
- Plant damage is minimized
- Treatment is recorded

**Alternative Flows:**

**AF-04a: Pesticide Stock Depleted**
1. System checks pesticide inventory
2. If empty, displays alert: "Pesticide stock empty"
3. Manual intervention required
4. Pests continue to damage plants until restocked

---

## UC-05: Adjust Simulation Speed

**Actor:** Garden Manager  
**Preconditions:**
- Simulation is running

**Main Flow:**
1. User selects new speed from dropdown (1x, 5x, 10x)
2. System receives speed change request
3. System updates tick duration:
   - 1x: 1 real second = 1 sim minute
   - 5x: 1 real second = 5 sim minutes
   - 10x: 1 real second = 10 sim minutes
4. System adjusts timer intervals accordingly
5. UI updates to show new speed indicator
6. All processes continue at new speed
7. System logs "Simulation speed changed to [N]x"

**Postconditions:**
- Simulation runs at new speed
- All systems operate proportionally faster
- Time display reflects new speed

**Alternative Flows:**

**AF-05a: Performance Limitation**
1. System detects performance degradation at 10x
2. Automatically reduces to stable speed (5x)
3. Notifies user: "Speed reduced to maintain performance"

---

## UC-06: Monitor Plant Growth

**Actor:** Simulation Engine (Automated)  
**Preconditions:**
- Simulation is running
- Plants exist and are alive

**Main Flow:**
1. On each simulation tick:
2. For each plant in garden:
   a. Check current growth stage
   b. Evaluate growth conditions:
      - Water level adequate?
      - Temperature in range?
      - Pest damage level?
      - Current weather bonus/penalty?
   c. If conditions favorable:
      - Increment growth progress
      - Check if stage advancement threshold met
      - If yes: Advance to next stage
      - Update visual representation
      - Log growth event
   d. If conditions poor:
      - Decrement health
      - If health critical: Change color to red
      - Log warning
3. System updates UI for all changed plants

**Postconditions:**
- Plant states are updated
- Growth progression is accurate
- UI reflects current status

**Alternative Flows:**

**AF-06a: Plant Dies**
1. Plant health reaches 0
2. System marks plant as dead
3. Visual changes to withered appearance
4. Plant stops participating in active simulation
5. Log records death with cause
6. After decay period, plant is removed from grid

---

## UC-07: Handle Temperature Extremes

**Actor:** Heating System (Automated)  
**Preconditions:**
- Simulation is running
- Temperature sensor is functional

**Main Flow:**
1. System monitors ambient temperature every tick
2. Temperature sensor reports current value
3. System compares to configured range (e.g., 15°C - 30°C)
4. If temperature < minimum threshold:
   a. Activate heating system
   b. Increment temperature by 5°C per minute
   c. Apply warmth bonus to plant growth
   d. Display heater icon on UI
   e. Log "Heating activated - Temperature: [X]°C"
5. If temperature > maximum threshold:
   a. Activate cooling (optional feature)
   b. Decrement temperature by 3°C per minute
   c. Log cooling action
6. When temperature returns to optimal range:
   a. Deactivate heating/cooling
   b. Log system status change

**Postconditions:**
- Temperature is within acceptable range
- Plants are protected from thermal stress
- Energy usage is recorded

**Alternative Flows:**

**AF-07a: Equipment Failure**
1. Heater fails to activate
2. System logs error
3. Alert shown to user
4. Manual intervention required or emergency protocol engaged

---

## UC-08: View and Filter Event Logs

**Actor:** Garden Manager  
**Preconditions:**
- Application is running
- Events have been logged

**Main Flow:**
1. User clicks "View Logs" button
2. System opens log viewer panel
3. System displays recent events (last 100 by default)
4. Each entry shows: [Timestamp] [Level] [Message]
5. User selects filter category (e.g., "Watering")
6. System filters log entries by category
7. Matching entries are displayed
8. User can scroll through filtered results
9. User can export logs to file

**Postconditions:**
- User has viewed relevant log information
- Log file remains intact

**Alternative Flows:**

**AF-08a: No Logs Available**
1. System displays "No events logged yet"
2. Message suggests starting simulation

**AF-08b: Export Logs**
1. User clicks "Export" button
2. System prompts for save location
3. System writes log to text file
4. Confirmation message shown

---

## UC-09: Pause and Resume Simulation

**Actor:** Garden Manager  
**Preconditions:**
- Simulation is currently running

**Main Flow:**
1. User clicks "Pause" button
2. System stops simulation timer
3. All automated processes halt
4. Garden state is frozen
5. UI button changes to "Resume"
6. System logs "Simulation paused at [timestamp]"
7. User examines garden state, views logs, etc.
8. User clicks "Resume" button
9. System restarts simulation timer
10. Automated processes continue
11. System logs "Simulation resumed at [timestamp]"

**Postconditions:**
- Simulation continues from exact paused state
- No data loss or inconsistency

**Alternative Flows:**

**AF-09a: Stop Simulation**
1. User clicks "Stop" instead of "Pause"
2. Simulation terminates completely
3. Final statistics are calculated
4. Session log is closed
5. Garden resets to initial state

---

## UC-10: Handle System Errors

**Actor:** System (Automated)  
**Preconditions:**
- Application is running
- An exception occurs

**Main Flow:**
1. Exception is thrown in any component
2. Exception handler catches error
3. System logs error details:
   - Timestamp
   - Error type
   - Stack trace
   - Affected component
4. System assesses severity:
   - Critical: Affects core functionality
   - Warning: Affects single feature
   - Minor: Logging or display issue
5. System attempts recovery:
   - Reset affected component
   - Use fallback behavior
   - Continue with reduced functionality
6. UI displays error notification to user
7. If recovery successful:
   - Log "Recovery successful"
   - Continue operation
8. If recovery fails:
   - Log "Critical failure"
   - Gracefully shutdown affected module
   - Notify user with details

**Postconditions:**
- Error is logged and documented
- System continues operating if possible
- User is informed of any issues

**Alternative Flows:**

**AF-10a: Unrecoverable Error**
1. System cannot recover
2. Save current state if possible
3. Display detailed error message
4. Offer user option to restart
5. Close application gracefully

