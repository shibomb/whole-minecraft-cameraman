# Cameraman Plugin

A Minecraft Paper plugin that allows a designated player to act as a "Cameraman", automatically spectating players with various modes.

## Live Demo

Check out the plugin in action on our 24/7 YouTube Live stream:
[**Watch Live on YouTube**](https://youtube.com/live/MWFDWFQ1ZX0)

## Features

- **Cameraman Mode**: Designate a player to be the cameraman. They will be put into Spectator mode.
- **Newcomer Mode**: Automatically spectate players when they join the server.
- **Rotation Mode**: Automatically cycle through online players at a configurable interval.
- **Mob Target Mode**: Spectate nearby mobs (living entities) instead of players.
- **Auto Mob Target**: Automatically switch to Mob Target Mode when no players are available to spectate.
- **Smooth Teleport**: Smoothly interpolate the camera position when switching targets for a cinematic effect.
- **Persistence**: The cameraman role and all mode settings are saved and restored even after server restarts or player reconnections.
- **Smart Restoration**: If the cameraman disconnects and reconnects, their state and active modes are automatically restored.

## Commands

### Common
- `/cameraman set <player>`: Set the specified player as the cameraman.
- `/cameraman clear`: Stop spectating the current target.
- `/cameraman teleportsmooth <true|false> [duration]`: Enable/disable Smooth Teleport. Optional duration in seconds (default: 3s).
- `/cameraman showmessage <true|false>`: Enable/disable informational messages (e.g., "Moving to...", "Now spectating..."). Command feedback is still shown (default: true).

### Player
- `/cameraman target <player>`: Manually make the cameraman spectate a specific player.
- `/cameraman newcomer <true|false>`: Enable/disable Newcomer Mode.
- `/cameraman rotation <true|false> [interval]`: Enable/disable Rotation Mode. Optional interval in seconds (default: 10s).
- `/cameraman spectatemode <true|false> [perspective]`: Enable/disable Spectate Mode for players. Optional perspective: POV, BEHIND, FRONT, RANDOM (default: POV). If false, the cameraman teleports to the perspective location without attaching view.

### Mob
- `/cameraman mobtarget <true|false>`: Enable/disable Mob Target Mode.
- `/cameraman automob <true|false> [delay]`: Enable/disable Auto Mob Target. Optional delay in seconds (default: 5s).
- `/cameraman mobspectatemode <true|false> [perspective]`: Enable/disable Spectate Mode for mobs. Optional perspective: POV, BEHIND, FRONT, RANDOM (default: POV). If false, the cameraman teleports to the perspective location without attaching view.
- `/cameraman mobnightvision <true|false>`: Enable/disable Mob Night Vision. If true, the cameraman receives Night Vision effect when targeting a mob to improve visibility (default: false).
- `/cameraman nightvisionthreshold <0-15>`: Set the light level threshold for Adaptive Night Vision. Night Vision is applied if the target's light level is at or below this value (default: 7).
- `/cameraman distance <value|min-max>`: Set the distance for BEHIND/FRONT perspectives. Can be a fixed number (e.g. 3.0) or a range (e.g. 3.0-6.0) (default: 3.0).
- `/cameraman height <value|min-max>`: Set the height for BEHIND/FRONT perspectives. Can be a fixed number (e.g. 1.0) or a range (e.g. 1.0-3.0) (default: 1.0).

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
newcomerMode: false
rotationMode: false
rotationInterval: 10
mobTargetMode: false
autoMobTarget: false
autoMobTargetDelay: 5
teleportSmooth: false
teleportSmoothDuration: 3
spectateMode: true
spectatePerspective: POV
mobSpectateMode: true
mobSpectatePerspective: POV
mobNightVision: false
showMessage: true
nightVisionThreshold: 7
spectateDistance: "3.0"
spectateHeight: "1.0"
```
