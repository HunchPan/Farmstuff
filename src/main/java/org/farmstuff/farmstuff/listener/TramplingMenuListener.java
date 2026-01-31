package org.farmstuff.farmstuff.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.farmstuff.farmstuff.config.TramplingConfigManager;

public class TramplingMenuListener implements Listener {

    private final TramplingConfigManager configManager;

    public TramplingMenuListener(TramplingConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("Trampling Settings")) return;

        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == 13) {
            boolean newState = !configManager.isGloballyEnabled();
            configManager.setGloballyEnabled(newState);
            player.sendMessage("Trampling globally " + (newState ? "enabled" : "disabled"));
        }

        new TramplingMenu(configManager).openMenu(player);
    }
}
