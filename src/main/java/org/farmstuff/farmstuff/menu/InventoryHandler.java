package org.farmstuff.farmstuff.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public interface InventoryHandler {
    void onClick(InventoryClickEvent event);
    void onOpen(InventoryOpenEvent event);
    void onClose(InventoryCloseEvent event);
    boolean canAccessSlot(Player player, int slot);
    Inventory getInventory();
}