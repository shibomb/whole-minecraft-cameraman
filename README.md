![Cameraman Plugin Icon](icon.png)

# Cinematic Cameraman Plugin

A Minecraft Paper plugin that allows a designated player to act as a "Cinematic Cameraman", automatically spectating players with various modes.

## Live Demo

Check out the plugin in action on our 24/7 YouTube Live stream:
[**Watch Live on YouTube**](https://www.youtube.com/live/uupyt3k9nTE)

## Features

- **Cameraman Mode**: Designate a player to be the cameraman. They will be put into Spectator mode.
- **Newcomer Mode**: Automatically spectate players when they join the server.
- **Rotation Mode**: Automatically cycle through online players at a configurable interval.
- **Mob Target Mode**: Spectate nearby mobs (living entities) instead of players.
- **Auto Mob Target**: Automatically switch to Mob Target Mode when no players are available to spectate.
- **Auto Scenic Mode**: Automatically start a scenic shot (no target) after teleporting to a location (e.g. from Auto Mob Target) if spectate mode is disabled.
- **Smooth Teleport**: Smoothly interpolate the camera position when switching targets for a cinematic effect.
- **Persistence**: The cameraman role and all mode settings are saved and restored even after server restarts or player reconnections.
- **Smart Restoration**: If the cameraman disconnects and reconnects, their state and active modes are automatically restored.

## Commands

### Management
- `/cameraman set <player>`: Set the player who will act as the cameraman.
- `/cameraman unset`: Unset the current cameraman and return them to Survival mode.
- `/cameraman target <player>`: Manually set the target for the cameraman to follow.
- `/cameraman showmessage <true|false>`: Enable/disable informational messages (e.g., "Moving to...", "Now spectating..."). Command feedback is still shown (default: true).

### Targeting
- `/cameraman newcomer <true|false>`: Enable/disable Newcomer Mode (auto-spectate newly joined players).
- `/cameraman rotation <true|false> [interval]`: Enable/disable Rotation Mode (cycle targets). Optional interval in seconds (default: 30s).
- `/cameraman mobtarget <true|false>`: Enable/disable Mob Target Mode (spectate mobs instead of players).
- `/cameraman automob <true|false> [delay]`: Enable/disable Auto Mob Target (switch to mobs if no players). Optional delay in seconds (default: 60s).

### Spectating
- `/cameraman spectatemode <true|false> [perspective]`: Enable/disable Spectate Mode for **players**. Optional perspective (default: RANDOM).
- `/cameraman mobspectatemode <true|false> [perspective]`: Enable/disable Spectate Mode for **mobs**. Optional perspective (default: RANDOM).
- `/cameraman teleportsmooth <true|false> [duration]`: Enable/disable Smooth Teleport (interpolate position when switching targets). Optional duration in seconds (default: 3s).
- `/cameraman mobnightvision <true|false>`: Enable/disable Mob Night Vision (Night Vision effect when targeting mobs).
- `/cameraman nightvisionthreshold <0-15>`: Set threshold for Adaptive Night Vision (applied if light level <= value) (default: 7).

### Scenic
- `/cameraman scenic <true|false> [perspective]`: Enable/disable "Scenic Mode". Uses current location as anchor for cinematic shots without a target entity.
- `/cameraman autoscenic <true|false> [perspective]`: Enable/disable "Auto Scenic". Automatically starts the specified scenic perspective when spectateMode is false after finding a target.
- `/cameraman random <player|mob|scenic> <mode1,mode2...>`: Configure the list of perspectives used in RANDOM mode for each target type.

### Perspectives Configuration
*Settings apply to the relevant perspectives (BEHIND, FRONT, ORBIT, DYNAMIC, FLYBY, CRANE, MOVE).*

#### General
- `/cameraman distance <value|min-max>`: Set distance (BEHIND, FRONT, ORBIT, FLYBY, CRANE) (default: 3.0-5.0).
- `/cameraman height <value|min-max>`: Set height (BEHIND, FRONT, ORBIT, DYNAMIC) (default: 0.0-1.0).

#### Orbit
- `/cameraman orbitspeed <value|min-max>`: Set speed (degrees/tick) (default: 0.1).
- `/cameraman orbitdirection <LEFT|RIGHT|RANDOM>`: Set direction (default: RANDOM).

#### Dynamic
- `/cameraman smoothness <value>`: Set smoothness (0.01 - 1.0) (default: 0.1).

#### Flyby
- `/cameraman flybyduration <seconds>`: Set loop duration (default: 30.0).

#### Crane
- `/cameraman craneduration <seconds>`: Set loop duration (default: 30.0).
- `/cameraman craneheightmin <value>`: Set minimum height (default: 1.0).
- `/cameraman craneheightmax <value>`: Set maximum height (default: 5.0).

#### Move
- `/cameraman movedirection <x,y,z>`: Set direction vector relative to the camera's starting orientation (X=Right, Y=Up, Z=Forward). Supports ranges (e.g. "0.5-1.0").
- `/cameraman movespeed <value|min-max>`: Set speed (blocks/tick) (default: 0.01).

### Scenic Mode (Targetless Cinematics)
Target a static location instead of a player/mob.
`/cameraman scenic <true|false> [perspective]`
*   `true`: Starts a scenic shot at your current location.
*   `false`: Stops the scenic shot.
*   `[perspective]`: Optional. The camera perspective to use (e.g., `ORBIT`, `FLYBY`, `MOVE`).

### Auto Scenic
If enabled in `config.yml`, Scenic Mode will automatically start after teleporting to a target if `spectateMode` is false.

## Perspectives

This plugin offers various camera modes ("Perspectives") for spectating:

- **POV**: First-person view (standard Spectator mode).
- **BEHIND**: Fixed position behind the target.
  - Configurable via `distance` and `height`.
- **FRONT**: Fixed position in front of the target, looking back at them.
  - Configurable via `distance` and `height`.
- **ORBIT**: The camera circles around the target at a fixed distance.
  - Configurable via `distance` (radius), `height`, `orbitSpeed`, and `orbitDirection`.
- **DYNAMIC**: A smooth "tracking" shot. The camera lags slightly behind the target (spring-arm effect), smoothing out sudden rotations and movements.
  - Configurable via `distance`, `height`, and `dynamicSmoothness` (0.01-1.0).
- **FLYBY**: The camera moves continuously between two points relative to the target (e.g., flying past them). Loops back and forth.
  - Configurable via `flybyDuration`.
- **CRANE**: The camera moves vertically (up and down) while maintaining a fixed horizontal distance. Great for establishing shots.
  - Configurable via `distance`, `craneDuration`, `craneHeightMin`, and `craneHeightMax`.
- **MOVE**: The camera moves linearly in a specified direction relative to the start orientation.
  - Configurable via `movedirection` (X=Right, Y=Up, Z=Forward), `movespeed`, `distance` (start offset back), and `height` (start offset up).
- **RANDOM**: Automatically selects one of the above perspectives (including cinematic ones) at random each time a new target is chosen from the rotation list.

> **Note**: If `spectatemode` or `mobspectatemode` is set to `false`, these perspectives will only teleport the cameraman to the **starting position** of the shot. Dynamic effects like orbiting or flying by require spectate mode to be enabled (true).

## Permissions

- `cameraman.use`: Allows access to all cameraman commands. Default: OP.

## Installation

1. Build the project using Maven:
   ```bash
   mvn clean package
   ```
2. Copy the generated jar file to your server's `plugins` folder.
3. Restart the server.

## Configuration

The `config.yml` stores the UUID of the current cameraman and all mode settings to persist them across restarts.

```yaml
cameraman: <UUID>
newcomerMode: true
rotationMode: true
rotationInterval: 30
mobTargetMode: false
autoMobTarget: true
autoMobTargetDelay: 60
autoScenic: true
autoScenicPerspective: RANDOM
teleportSmooth: false
teleportSmoothDuration: 3
spectateMode: true
spectatePerspective: RANDOM
mobSpectateMode: true
mobSpectatePerspective: RANDOM
mobNightVision: false
showMessage: true
nightVisionThreshold: 7
spectateDistance: "3.0-5.0"
spectateHeight: "0.0-1.0"
orbitSpeed: "0.1"
orbitDirection: "RANDOM"
dynamicSmoothness: "0.1"
flybyDuration: "30.0"
craneDuration: "30.0"
craneHeightMin: "1.0"
craneHeightMax: "5.0"
moveX: "-1-1"
moveY: "0-0.1"
moveZ: "-0.1-0.1"
moveSpeed: "0.01"
randomPlayerPerspectives:
  - POV
  - BEHIND
  - FRONT
  - ORBIT
  - FLYBY
  - MOVE
  - CRANE
randomScenicPerspectives:
  - ORBIT
  - FLYBY
  - MOVE
  - CRANE
randomMobPerspectives:
  - POV
  - BEHIND
  - FRONT
  - ORBIT
  - FLYBY
  - CRANE
```
