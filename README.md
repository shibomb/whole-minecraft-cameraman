# Cameraman Plugin

A Minecraft Paper plugin that allows a designated player to act as a "Cameraman", automatically spectating players with various modes.

## Features

- **Cameraman Mode**: Designate a player to be the cameraman. They will be put into Spectator mode.
- **Newcomer Mode**: Automatically spectate players when they join the server.
- **Rotation Mode**: Automatically cycle through online players at a configurable interval.
- **Persistence**: The cameraman role is saved and restored even after server restarts or player reconnections.
- **Smart Restoration**: If the cameraman disconnects and reconnects, their state and active modes are automatically restored.

## Commands

- `/cameraman set <player>`: Set the specified player as the cameraman.
- `/cameraman target <player>`: Manually make the cameraman spectate a specific player.
- `/cameraman newcomer <true|false>`: Enable/disable Newcomer Mode.
- `/cameraman rotation <true|false> [interval]`: Enable/disable Rotation Mode. Optional interval in seconds (default: 10s).

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

The `config.yml` stores the UUID of the current cameraman to persist their role across restarts.

```yaml
cameraman: <UUID>
```
