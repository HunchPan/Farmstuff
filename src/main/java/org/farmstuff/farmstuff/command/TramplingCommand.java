package org.farmstuff.farmstuff.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.farmstuff.farmstuff.config.TramplingConfigManager;
import org.farmstuff.farmstuff.menu.TramplingMenu;

import java.util.UUID;

public class TramplingCommand implements CommandExecutor {

    private final TramplingConfigManager configManager;

    public TramplingCommand(TramplingConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 1) {
            String action = args[0].toLowerCase();
            if (action.equals("menu")) {
                return handleMenuCommand(sender);
            } else if (action.equals("on") || action.equals("off")) {
                return handleGlobalToggle(sender, action);
            }
        } else if (args.length == 2) {
            return handlePlayerToggle(sender, args[0], args[1]);
        }

        sender.sendMessage("Usage: /trampling <on/off> or /trampling <player> <on/off>");
        return true;
    }

    private boolean handleGlobalToggle(CommandSender sender, String action) {
        if (!sender.hasPermission("farmstuff.trampling.global")) {
            sender.sendMessage("§cYou don't have permission to change global trampling settings");
            return true;
        }

        boolean enable = action.equals("on");
        configManager.setGloballyEnabled(enable);
        sender.sendMessage("Trampling globally " + (enable ? "enabled" : "disabled"));
        return true;
    }

    private boolean handlePlayerToggle(CommandSender sender, String playerName, String playerAction) {
        String action = playerAction.toLowerCase();
        if (!action.equals("on") && !action.equals("off")) {
            return true;
        }

        // check if setting own or other's trampling
        boolean isSelf = sender instanceof Player && sender.getName().equalsIgnoreCase(playerName);

        if (isSelf) {
            if (!sender.hasPermission("farmstuff.trampling.self")) {
                sender.sendMessage("§cYou don't have permission to change trampling settings");
                return true;
            }
        } else {
            if (!sender.hasPermission("farmstuff.trampling.others")) {
                sender.sendMessage("§cYou don't have permission to change other players' trampling settings");
                return true;
            }
        }

        boolean enable = action.equals("on");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            sender.sendMessage("Player " + playerName + " not found");
            return true;
        }

        UUID uuid = offlinePlayer.getUniqueId();
        configManager.setTramplingEnabled(uuid, enable);
        sender.sendMessage("Trampling " + (enable ? "enabled" : "disabled") + " for " + playerName);
        return true;
    }

    private boolean handleMenuCommand(CommandSender sender) {
        if (sender instanceof Player) {
            new TramplingMenu(configManager).openMenu((Player) sender);
            return true;
        } else {
            sender.sendMessage("Only players can open menus");
            return true;
        }
    }
}
