package xyz.shibomb.cameraman;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CameramanCommand implements CommandExecutor {

    private final CameramanManager manager;

    public CameramanCommand(CameramanManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman set <player>");
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                manager.setCameraman(player);
                sender.sendMessage("Cameraman set to " + player.getName());
                break;

            case "unset":
                manager.removeCameraman();
                sender.sendMessage("Cameraman unset.");
                break;

            case "target":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman target <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                manager.setTarget(target);
                sender.sendMessage("Target set to " + target.getName());
                break;

            case "clear":
                manager.clearTarget();
                break;

            case "newcomer":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman newcomer <true/false>");
                    return true;
                }
                boolean newcomerMode = Boolean.parseBoolean(args[1]);
                manager.setNewcomerMode(newcomerMode);
                sender.sendMessage("Newcomer mode: " + newcomerMode);
                break;

            case "rotation":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman rotation <true/false> [interval]");
                    return true;
                }
                boolean rotationMode = Boolean.parseBoolean(args[1]);
                long interval = 10; // Default
                if (args.length >= 3) {
                    try {
                        interval = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid interval. Using default 10s.");
                    }
                }
                manager.setRotationMode(rotationMode, interval);
                sender.sendMessage("Rotation mode: " + rotationMode);
                break;

            case "mobtarget":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman mobtarget <true/false>");
                    return true;
                }
                boolean mobTargetMode = Boolean.parseBoolean(args[1]);
                manager.setMobTargetMode(mobTargetMode);
                sender.sendMessage("Mob Target Mode: " + mobTargetMode);
                break;

            case "automob":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman automob <true/false> [delay]");
                    return true;
                }
                boolean autoMobTarget = Boolean.parseBoolean(args[1]);
                long delay = 5; // Default
                if (args.length >= 3) {
                    try {
                        delay = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid delay. Using default 5s.");
                    }
                }
                manager.setAutoMobTarget(autoMobTarget, delay);
                sender.sendMessage("Auto Mob Target: " + autoMobTarget);
                break;

            case "teleportsmooth":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman teleportsmooth <true/false> [duration]");
                    return true;
                }
                boolean teleportSmooth = Boolean.parseBoolean(args[1]);
                long duration = 3; // Default
                if (args.length >= 3) {
                    try {
                        duration = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid duration. Using default 3s.");
                    }
                }
                manager.setTeleportSmooth(teleportSmooth, duration);
                sender.sendMessage("Smooth Teleport: " + teleportSmooth);
                break;

            case "spectatemode":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman spectatemode <true/false> [perspective]");
                    return true;
                }
                boolean spectateMode = Boolean.parseBoolean(args[1]);
                manager.setSpectateMode(spectateMode);

                if (args.length >= 3) {
                    try {
                        SpectatePerspective perspective = SpectatePerspective.valueOf(args[2].toUpperCase());
                        manager.setSpectatePerspective(perspective);
                        sender.sendMessage(
                                "Spectate Mode (Player): " + spectateMode + " (Perspective: " + perspective + ")");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(
                                "Invalid perspective. Available: POV, BEHIND, FRONT, RANDOM, ORBIT, DYNAMIC, FLYBY, CRANE");
                        sender.sendMessage("Spectate Mode (Player): " + spectateMode);
                    }
                } else {
                    sender.sendMessage("Spectate Mode (Player): " + spectateMode);
                }
                break;

            case "mobspectatemode":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman mobspectatemode <true/false> [perspective]");
                    return true;
                }
                boolean mobSpectateMode = Boolean.parseBoolean(args[1]);
                manager.setMobSpectateMode(mobSpectateMode);

                if (args.length >= 3) {
                    try {
                        SpectatePerspective perspective = SpectatePerspective.valueOf(args[2].toUpperCase());
                        manager.setMobSpectatePerspective(perspective);
                        sender.sendMessage(
                                "Spectate Mode (Mob): " + mobSpectateMode + " (Perspective: " + perspective + ")");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(
                                "Invalid perspective. Available: POV, BEHIND, FRONT, RANDOM, ORBIT, DYNAMIC, FLYBY, CRANE");
                        sender.sendMessage("Spectate Mode (Mob): " + mobSpectateMode);
                    }
                } else {
                    sender.sendMessage("Spectate Mode (Mob): " + mobSpectateMode);
                }
                break;

            case "mobnightvision":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman mobnightvision <true/false>");
                    return true;
                }
                boolean mobNightVision = Boolean.parseBoolean(args[1]);
                manager.setMobNightVision(mobNightVision);
                sender.sendMessage("Mob Night Vision: " + mobNightVision);
                break;
            case "showmessage":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman showmessage <true/false>");
                    return true;
                }
                boolean showMessage = Boolean.parseBoolean(args[1]);
                manager.setShowMessage(showMessage);
                sender.sendMessage("Show Message: " + showMessage);
                break;
            case "nightvisionthreshold":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman nightvisionthreshold <0-15>");
                    return true;
                }
                try {
                    int threshold = Integer.parseInt(args[1]);
                    if (threshold < 0 || threshold > 15) {
                        sender.sendMessage("Threshold must be between 0 and 15.");
                        return true;
                    }
                    manager.setNightVisionThreshold(threshold);
                    sender.sendMessage("Night Vision Threshold: " + threshold);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number format.");
                }
                break;

            case "distance":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman distance <value|min-max>");
                    return true;
                }
                String distance = args[1];
                // Optional: Validate format here, but manager handles parsing safely
                try {
                    // Check if it's a number or a range
                    if (!distance.contains("-")) {
                        Double.parseDouble(distance);
                    } else {
                        String[] parts = distance.split("-");
                        if (parts.length != 2)
                            throw new NumberFormatException();
                        Double.parseDouble(parts[0].trim());
                        Double.parseDouble(parts[1].trim());
                    }
                    manager.setSpectateDistance(distance);
                    sender.sendMessage("Spectate Distance: " + distance);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid format. Use a number (e.g. 3.0) or range (e.g. 3.0-6.0).");
                }
                break;

            case "height":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman height <value|min-max>");
                    return true;
                }
                String height = args[1];
                try {
                    if (!height.contains("-")) {
                        Double.parseDouble(height);
                    } else {
                        String[] parts = height.split("-");
                        if (parts.length != 2)
                            throw new NumberFormatException();
                        Double.parseDouble(parts[0].trim());
                        Double.parseDouble(parts[1].trim());
                    }
                    manager.setSpectateHeight(height);
                    sender.sendMessage("Spectate Height: " + height);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid format. Use a number (e.g. 1.0) or range (e.g. 1.0-3.0).");
                }
                break;

            case "orbitspeed":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman orbitspeed <value|min-max>");
                    return true;
                }
                String speed = args[1];
                try {
                    if (!speed.contains("-")) {
                        Double.parseDouble(speed);
                    } else {
                        String[] parts = speed.split("-");
                        if (parts.length != 2)
                            throw new NumberFormatException();
                        Double.parseDouble(parts[0].trim());
                        Double.parseDouble(parts[1].trim());
                    }
                    manager.setOrbitSpeed(speed);
                    sender.sendMessage("Orbit Speed set to: " + speed);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid format. Use a number (e.g. 1.0) or range (e.g. 0.5-2.0).");
                }
                break;

            case "orbitdirection":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman orbitdirection <LEFT|RIGHT|RANDOM>");
                    return true;
                }
                String direction = args[1].toUpperCase();
                if (direction.equals("LEFT") || direction.equals("RIGHT") || direction.equals("RANDOM")) {
                    manager.setOrbitDirection(direction);
                    sender.sendMessage("Orbit Direction set to: " + direction);
                } else {
                    sender.sendMessage("Invalid direction. Use LEFT, RIGHT, or RANDOM.");
                }
                break;

            case "smoothness":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman smoothness <value>");
                    return true;
                }
                manager.setDynamicSmoothness(args[1]);
                break;

            case "flybyduration":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman flybyduration <value> (seconds)");
                    return true;
                }
                manager.setFlybyDuration(args[1]);
                break;

            case "craneduration":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman craneduration <value> (seconds)");
                    return true;
                }
                manager.setCraneDuration(args[1]);
                break;

            case "random":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /cameraman random <player|mob> <mode1,mode2...>");
                    return true;
                }
                String type = args[1].toLowerCase();
                if (!type.equals("player") && !type.equals("mob") && !type.equals("scenic")) {
                    sender.sendMessage("Invalid type. usage: /cameraman random <player|mob|scenic> ...");
                    return true;
                }

                String[] modes = args[2].toUpperCase().split(",");
                List<String> validModes = new ArrayList<>();
                for (String mode : modes) {
                    try {
                        SpectatePerspective p = SpectatePerspective.valueOf(mode);
                        if (p != SpectatePerspective.RANDOM) {
                            validModes.add(p.name());
                        }
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("Warning: skipping invalid mode '" + mode + "'");
                    }
                }

                if (validModes.isEmpty()) {
                    sender.sendMessage("No valid modes provided. List unchanged.");
                    return true;
                }
                manager.setRandomPerspectives(type, validModes);
                break;

            case "scenic":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman scenic <true|false> [perspective]");
                    return true;
                }
                boolean scenicEnable = Boolean.parseBoolean(args[1]);
                if (scenicEnable) {
                    SpectatePerspective perspective = SpectatePerspective.POV;
                    if (args.length >= 3) {
                        try {
                            perspective = SpectatePerspective.valueOf(args[2].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("Invalid perspective.");
                            return true;
                        }
                    }
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        manager.startScenicTask(p.getLocation(), perspective);
                        sender.sendMessage("Scenic Mode enabled: " + perspective + " at your location.");
                    } else {
                        // If console, use the cameraman's current location
                        Player cameraman = manager.getCameraman();
                        if (cameraman != null) {
                            manager.startScenicTask(cameraman.getLocation(), perspective);
                            sender.sendMessage("Scenic Mode enabled: " + perspective + " at cameraman's location.");
                        } else {
                            sender.sendMessage(
                                    "No cameraman set. Cannot start Scenic Mode from console without a cameraman.");
                        }
                    }
                } else {
                    manager.stopFollowing();
                    sender.sendMessage("Scenic Mode disabled.");
                }
                break;

            case "autoscenic":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman autoscenic <true|false> [perspective]");
                    return true;
                }
                boolean autoScenic = Boolean.parseBoolean(args[1]);
                SpectatePerspective autoScenicPerspective = null;
                if (args.length >= 3) {
                    try {
                        autoScenicPerspective = SpectatePerspective.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("Invalid perspective.");
                        return true;
                    }
                }
                manager.setAutoScenic(autoScenic, autoScenicPerspective);
                break;

            case "movespeed":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cameraman movespeed <value|min-max>");
                    return true;
                }
                manager.setMoveSpeed(args[1]);
                break;

            case "movedirection":
                if (args.length < 4) {
                    sender.sendMessage("Usage: /cameraman movedirection <x> <y> <z> (supports ranges e.g. -1-1)");
                    return true;
                }
                manager.setMoveDirection(args[1], args[2], args[3]);
                break;

            default:
                sender.sendMessage("Unknown subcommand.");
                break;
        }

        return true;
    }
}
