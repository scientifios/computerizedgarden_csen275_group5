# Smart Garden Simulation - User Manual

## Table of Contents
1. [Introduction](#introduction)
2. [System Requirements](#system-requirements)
3. [Installation](#installation)
4. [Getting Started](#getting-started)
5. [User Interface Overview](#user-interface-overview)
6. [Planting and Managing Plants](#planting-and-managing-plants)
7. [Running the Simulation](#running-the-simulation)
8. [Monitoring Systems](#monitoring-systems)
9. [Manual Controls](#manual-controls)
10. [Understanding Plant Health](#understanding-plant-health)
11. [Troubleshooting](#troubleshooting)

---

## Introduction

Welcome to the Smart Garden Simulation System! This application simulates an automated garden with intelligent systems that water plants, control temperature, and manage pests. It's designed for educational purposes to demonstrate object-oriented design principles and simulation systems.

**Key Features:**
- 9x9 garden grid with 9 plant types (Fruits, Vegetables, Flowers)
- Automated watering system with 9 zones (weather-aware)
- Temperature control system
- Pest detection and control (harmful pests only)
- Dynamic weather simulation (5 weather types)
- Real-time visualization with smooth animations
- Variable simulation speed (1x to 10x)
- Smart sprinkler control (stops automatically when raining)

---

## System Requirements

### Minimum Requirements:
- **Operating System**: Windows 10+, macOS 10.14+, or modern Linux
- **Java Runtime**: Java 21 or higher
- **Memory**: 8 GB RAM
- **Display**: 1280x720 resolution or higher
- **Disk Space**: 100 MB free space (for application and logs)

### Recommended:
- Java Development Kit (JDK) 21
- 16 GB RAM for smooth operation at high speeds
- 1920x1080 resolution for optimal viewing

---

## Installation

### Method 1: Using Maven (Recommended)

1. **Install Java 21 (if not already installed)**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **Install Maven (if not already installed)**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

3. **Navigate to Project Directory**
   ```bash
   cd smartGarden
   ```

4. **Build the Project**
   ```bash
   mvn clean install
   ```

5. **Run the Application**
   ```bash
   mvn javafx:run
   ```

### Method 2: Direct Java Execution

If you have a pre-built JAR file:
```bash
java -jar smartGarden-1.0.0.jar
```

---

## Getting Started

### First Launch

1. **Start the Application**
   - Execute the run command from Installation section
   - The application window will appear with an empty 9x9 garden grid

2. **Plant Your First Plants**
   - Select a plant type from the dropdown menu (Flower, Tomato, Tree, Grass, or Basil)
   - Click on empty grid cells to plant
   - Try planting at least 5-10 plants for a good simulation

3. **Start the Simulation**
   - Click the "Start" button in the top toolbar
   - Watch as your garden comes to life!

4. **Observe and Learn**
   - Watch plants grow through different stages
   - See the watering system activate automatically
   - Observe weather changes and their effects
   - Monitor system logs at the bottom of the screen

---

## User Interface Overview

### Top Toolbar
- **Title**: Application name and icon
- **Start Button**: Begins the simulation
- **Pause Button**: Pauses/resumes the simulation
- **Stop Button**: Stops the simulation completely
- **Speed Selector**: Choose simulation speed (1x, 2x, 5x, 10x)
- **Status Label**: Shows current simulation state

### Center: Garden Grid
- **9x9 Grid**: Each cell can contain one plant
- **Color-Coded Cells**:
  - 🟢 **Green**: Healthy plant
  - 🟡 **Yellow**: Plant needs attention
  - 🟠 **Orange**: Plant is stressed
  - 🔴 **Red**: Plant is dying
  - ⚫ **Dark**: Dead plant
  - 🟤 **Brown**: Empty soil

### Right Panel: Information Display
- **Simulation Time**: Current day and time in simulation
- **Weather Display**: Current weather with icon
- **Plant Statistics**: Total and living plant counts
- **Resource Meters**:
  - Water Supply (refills available)
  - Temperature (automatic control)
  - Pesticide Stock (refills available)
- **Manual Control Buttons**: Override automatic systems

### Bottom Panel: Event Log
- **Recent Events**: Last 10 logged events
- Timestamped entries for all actions
- Categorized by event type (Watering, Heating, Pests, etc.)

---

## Planting and Managing Plants

### Available Plant Types

#### Fruits
1. **Strawberry** 🍓
   - **Growth Rate**: Moderate
   - **Water Needs**: Medium
   - **Special Traits**: Sweet fruit producer, moderate maintenance
   
2. **Grapevine** 🍇
   - **Growth Rate**: Moderate to Fast
   - **Water Needs**: Medium to High
   - **Special Traits**: Vine growth, fruit production
   
3. **Apple Sapling** 🍎
   - **Growth Rate**: Slow (tree growth)
   - **Water Needs**: Medium
   - **Special Traits**: Long-term investment, tree structure

#### Vegetables
4. **Carrot** 🥕
   - **Growth Rate**: Moderate
   - **Water Needs**: Medium
   - **Special Traits**: Root vegetable, moderate maintenance
   
5. **Tomato** 🍅
   - **Growth Rate**: Fast
   - **Water Needs**: High
   - **Special Traits**: Quick growth, high yield, pest-prone
   
6. **Onion** 🧅
   - **Growth Rate**: Moderate
   - **Water Needs**: Medium
   - **Special Traits**: Bulb vegetable, moderate maintenance

#### Flowers
7. **Sunflower** 🌻
   - **Growth Rate**: Fast
   - **Water Needs**: Medium to High
   - **Special Traits**: Large blooms, decorative
   
8. **Tulip** 🌸
   - **Growth Rate**: Moderate
   - **Water Needs**: Medium
   - **Special Traits**: Beautiful blooms, decorative
   
9. **Rose** 🌹
   - **Growth Rate**: Moderate
   - **Water Needs**: Medium
   - **Special Traits**: Classic flower, decorative

### How to Plant

1. **Select Plant Type**
   - Use the dropdown menu above the garden grid
   - Choose from: Strawberry, Grapevine, Apple, Carrot, Tomato, Onion, Sunflower, Tulip, or Rose

2. **Click to Plant**
   - Click any empty (brown) cell
   - The plant icon will appear immediately
   - Initial color is green (healthy)

3. **View Plant Details**
   - Click on an existing plant to see detailed information
   - Info dialog shows:
     - Growth stage
     - Health percentage
     - Water level
     - Days alive
     - Current status

4. **Remove Plants** (if needed)
   - Use the "Clear All" button to remove all plants
   - Individual removal: stop simulation first, then replant area

### Plant Growth Stages

All plants progress through these stages:
1. **Seed** → Just planted
2. **Seedling** → Early growth
3. **Mature** → Full size
4. **Flowering** → Producing blooms/fruit
5. **Fruiting** → Peak productivity (some plants)

---

## Running the Simulation

### Starting a Simulation

1. **Plant at least one plant** (required)
2. **Click "Start" button**
3. Simulation begins at 1x speed by default
4. All systems activate automatically

### Controlling Speed

- **1x Speed**: Real-time (1 second = 1 minute simulation time)
- **2x Speed**: Twice as fast
- **5x Speed**: Fast-forward mode
- **10x Speed**: Ultra-fast (for long-term observation)

**Tip**: Start at 1x to understand the system, then increase speed for longer runs.

### Pausing and Resuming

- **Pause**: Click "Pause" button
  - Simulation freezes
  - You can examine plants and logs
  - No changes occur while paused
  
- **Resume**: Click "Resume" button (appears after pause)
  - Simulation continues from exact state
  - No data loss

### Stopping the Simulation

- **Click "Stop" button**
- Simulation ends and resets
- Final statistics are logged
- Garden remains but is inactive

**Warning**: Stopping clears the simulation state. If you want to examine the garden later, use Pause instead.

---

## Monitoring Systems

### Watering System

**How It Works:**
- Garden divided into 9 zones (3x3 arrangement)
- Each zone has a moisture sensor
- Sprinklers activate when moisture drops below 40%
- **Smart Feature**: Sprinklers automatically stop when it starts raining
- Automatic watering provides 30 units per cycle
- Watering occurs every 5 simulation minutes (check cycle)

**What to Watch:**
- Water Supply meter (right panel)
- Zone moisture levels (affects plant health)
- Watering events in the log
- Green cells indicate well-watered plants

**Indicators:**
- Yellow/orange plants may need more water
- If water supply is low, refill immediately
- Watch for "Water supply low" warnings in logs

### Heating System

**How It Works:**
- Monitors ambient temperature continuously
- Target range: 15°C - 28°C (configurable)
- Activates heating when temperature drops below 15°C
- Three heating modes: Low, Medium, High
- Natural cooling when heating is off

**What to Watch:**
- Temperature meter (right panel)
- Heating mode displayed in logs
- Plant health affected by temperature extremes

**Indicators:**
- Cold temperatures damage plants
- Heating system uses energy (tracked in logs)
- Optimal temperature = green, healthy plants

### Pest Control System

**How It Works:**
- Harmful pests appear randomly throughout simulation
- Pest types include: Aphids, Caterpillars, Beetles, and more
- Infestation level tracked per zone
- Automatic treatment at 60% infestation
- Pesticide application reduces pest count by 50%

**What to Watch:**
- Pesticide Stock meter (right panel)
- Orange/red cells may indicate pest damage
- Pest-related warnings in logs
- Plant health declining rapidly

**Indicators:**
- Pests attack plants gradually
- Low pesticide stock requires refill
- Treatment is effective but requires monitoring

### Weather System

**Weather Types:**
- **Sunny** ☀: Optimal for growth, increases health, evaporates moisture
- **Cloudy** ☁: Neutral effect, moderate conditions
- **Rainy** 🌧: Adds moisture naturally, **automatically stops sprinklers** to conserve water
- **Windy** 💨: Stresses plants slightly, increases evaporation
- **Snowy** ❄: Damages plants, requires heating

**Weather Changes:**
- Occurs every 30-120 simulation minutes
- Realistic transitions (sunny → cloudy → rainy)
- Current weather shown in right panel
- Weather affects growth rates and water needs

---

## Manual Controls

### When to Use Manual Overrides

Use manual controls when:
- Testing the system
- Emergency situations (e.g., plants dying quickly)
- Demonstrating specific features
- Supplementing automatic systems

### Available Manual Controls

#### 1. **Water All Zones**
- **Location**: Right panel
- **Action**: Immediately waters all 9 zones
- **Use When**: Plants are very dry, testing watering system
- **Note**: Consumes water supply

#### 2. **Refill Water**
- **Location**: Right panel
- **Action**: Adds 5000L to water supply
- **Use When**: Water meter is low or empty
- **Note**: Can refill anytime, no limit

#### 3. **Refill Pesticide**
- **Location**: Right panel
- **Action**: Adds 25 applications to pesticide stock
- **Use When**: Pesticide meter is low, pests increasing
- **Note**: Prevents pest damage accumulation

#### 4. **Adjust Simulation Speed**
- **Location**: Top toolbar
- **Action**: Changes how fast time progresses
- **Use When**: Want to observe quickly or in detail
- **Note**: No effect on game mechanics, just time scale

---

## Understanding Plant Health

### Health Indicators

#### Color Coding
- **Green (80-100% health)**: Excellent condition
- **Yellow (50-79% health)**: Fair, needs attention
- **Orange (20-49% health)**: Poor, at risk
- **Red (0-19% health)**: Critical, dying soon

#### Factors Affecting Health

**Positive Factors:**
✅ Adequate water (above requirement)  
✅ Optimal temperature (15-28°C)  
✅ Favorable weather (Sunny, Rainy)  
✅ No pest attacks  
✅ Appropriate growth stage  

**Negative Factors:**
❌ Insufficient water (below requirement)  
❌ Extreme temperatures (too hot/cold)  
❌ Adverse weather (Windy, Snowy)  
❌ Pest infestations  
❌ Reaching end of lifespan  

### Common Health Issues

#### Issue: Plant Turning Yellow
**Likely Cause**: Needs water  
**Solution**: 
- Wait for automatic watering (if moisture low)
- Use manual water if urgent
- Check water supply isn't empty

#### Issue: Plant Turning Orange/Red
**Likely Causes**: 
- Severe dehydration
- Pest attacks
- Temperature stress

**Solution**:
- Immediately water the zone
- Check for pest warnings, apply treatment if needed
- Verify temperature is in range

#### Issue: Plant Died (💀)
**Possible Causes**:
- Health reached 0%
- Exceeded maximum lifespan
- Accumulated too much damage

**Prevention**:
- Monitor health regularly
- Keep resources stocked (water, pesticide)
- Use appropriate plant types for your management style

### Optimal Care Tips

1. **Start Slow**: Begin with hardy plants (Grass, Herbs)
2. **Maintain Resources**: Keep water and pesticide above 50%
3. **Watch the Weather**: Rainy weather helps reduce watering needs
4. **Speed Wisely**: Don't run too fast if you're still learning
5. **Read the Logs**: They tell you exactly what's happening

---

## Troubleshooting

### Simulation Won't Start

**Problem**: "Start" button doesn't work or shows error  
**Solutions**:
- ✓ Plant at least one plant first
- ✓ Check console for error messages
- ✓ Restart the application
- ✓ Verify Java version (must be 21+)

### Plants Dying Quickly

**Problem**: Many plants die within a few simulation days  
**Solutions**:
- ✓ Reduce simulation speed to observe issues
- ✓ Check water supply meter - refill if low
- ✓ Look for pest warnings in logs
- ✓ Ensure temperature is stable
- ✓ Try hardier plant types (Tree, Grass)

### UI Not Updating

**Problem**: Garden grid or stats not changing  
**Solutions**:
- ✓ Verify simulation is running (not paused/stopped)
- ✓ Check if simulation speed is very slow
- ✓ Restart the application
- ✓ Check system resources (CPU/memory)

### Performance Issues

**Problem**: Application is slow or laggy  
**Solutions**:
- ✓ Reduce simulation speed
- ✓ Plant fewer plants (max 81)
- ✓ Close other applications
- ✓ Ensure 8GB+ RAM available
- ✓ Update to latest Java version

### Log File Errors

**Problem**: Cannot create or write log files  
**Solutions**:
- ✓ Check file permissions in project directory
- ✓ Ensure `logs/` directory exists
- ✓ Free up disk space
- ✓ Run application with proper permissions

### Application Crashes

**Problem**: Application closes unexpectedly  
**Solutions**:
- ✓ Check log files in `logs/` directory for error details
- ✓ Verify Java installation is correct
- ✓ Ensure all Maven dependencies are installed
- ✓ Try running at lower simulation speed
- ✓ Report bug with log file contents

---

## Keyboard Shortcuts

Currently, the application uses mouse/button controls only. Keyboard shortcuts may be added in future versions.

---

## Advanced Usage

### Long-Running Simulations

To run the simulation for 24+ hours:

1. **Optimize Settings**:
   - Use 5x or 10x speed
   - Plant a moderate number of plants (30-50)
   - Keep resource meters above 50% before starting

2. **Monitor Periodically**:
   - Check every few hours
   - Refill resources as needed
   - Pause to examine state if needed

3. **Expected Results**:
   - Plants will go through full lifecycles
   - Some plants will die naturally (reaching lifespan)
   - Weather will cycle through all types
   - Comprehensive logs will be generated

### Testing Specific Scenarios

**Drought Scenario**:
- Don't refill water
- Observe plant stress and system response

**Pest Outbreak**:
- Don't refill pesticide
- Watch infestation spread

**Temperature Extremes**:
- Manually set snowy weather (if exposed in code)
- Observe heating system activation

---

## Best Practices

1. **Start Small**: Begin with 10-15 plants to learn the system
2. **Mix Plant Types**: Use variety for interesting dynamics
3. **Monitor Logs**: They provide valuable insights
4. **Experiment**: Try different speeds and plant combinations
5. **Be Patient**: Plants take time to grow (even at 10x speed)
6. **Plan Ahead**: Stock resources before long runs
7. **Save Observations**: Take screenshots or notes for analysis

---

## Getting Help

### Resources

- **Documentation**: See `docs/` directory for detailed design documents
- **Log Files**: Located in `logs/` directory, named by session timestamp
- **Source Code**: Fully commented, available in `src/` directory

### Support

For issues or questions:
1. Check this manual first
2. Review log files for error details
3. Consult design documentation
4. Contact course instructor or TA

---

## Conclusion

The Smart Garden Simulation is a powerful educational tool demonstrating:
- Object-oriented design principles
- Multi-threaded simulation
- Automated control systems
- Real-time visualization

Enjoy exploring the world of automated gardening! 🌱

---

**Version**: 1.0.0  
**Last Updated**: December 2024  
**Course**: CSEN 275 - Object-Oriented Analysis & Design

## Recent Updates

- **Version 1.0.0** (December 2024):
  - Added 9 plant types (3 Fruits, 3 Vegetables, 3 Flowers)
  - Implemented weather-aware sprinkler control (stops automatically when raining)
  - All plant images now use local PNG files (no web dependencies)
  - Improved animation performance and stability
  - Enhanced pest control system (harmful pests only)

