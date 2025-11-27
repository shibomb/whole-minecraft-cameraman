package xyz.shibomb.cameraman;

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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

            default:
                sender.sendMessage("Unknown subcommand.");
                break;
        }

        return true;
    }
}
