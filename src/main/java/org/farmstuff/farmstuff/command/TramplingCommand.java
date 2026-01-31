package org.farmstuff.farmstuff.command;

import org.bukkit.Bukkit;
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
        // global
        if (args.length == 1) {
            String action = args[0].toLowerCase();
            if (action.equals("on") || action.equals("off")) {
                boolean enable = action.equals("on");
                configManager.setGloballyEnabled(enable);
                sender.sendMessage("Trampling globally " + (enable ? "enabled" : "disabled"));
                return true;
            }
            // player
        } else if (args.length == 2) {
            String playerName = args[0];
            String playerAction = args[1].toLowerCase();
            if (playerAction.equals("on") || playerAction.equals("off")) {
                boolean enable = playerAction.equals("on");
                UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                configManager.setTramplingEnabled(uuid, enable);
                sender.sendMessage("Trampling " + (enable ? "enabled" : "disabled") + " for " + playerName);
                return true;
            }
        }

        // menu
        if (args.length == 1 && args[0].equalsIgnoreCase("menu")) {
            if (sender instanceof Player) {
                new TramplingMenu(configManager).openMenu((Player) sender);
                return true;
            } else {
                sender.sendMessage("Only players can open menus");
                return false;
            }
        }



        sender.sendMessage("Usage: /trampling <on/off> or /trampling <player> <on/off>");
        return false;
    }
}
