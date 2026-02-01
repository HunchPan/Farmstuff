package org.farmstuff.farmstuff.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.farmstuff.farmstuff.config.TramplingConfigManager;

public class TramplingMenu implements InventoryHandler {

    private final TramplingConfigManager configManager;
    private final Inventory inventory;

    public TramplingMenu(TramplingConfigManager configManager) {
        this.configManager = configManager;
        this.inventory = Bukkit.createInventory(null, 27, "Trampling Settings");
    }

    private void initializeItems(Player player) {
        // personal toggle button
        boolean personalEnabled = configManager.isTramplingEnabled(player.getUniqueId());
        ItemStack personalButton = createButton(
                Material.GRASS_BLOCK,
                "§aPersonal Trampling",
                personalEnabled ? "§aEnabled" : "§cDisabled"
        );
        inventory.setItem(11, personalButton);

        // global toggle button
        if (player.hasPermission("farmstuff.trampling.global")) {
            ItemStack globalButton = createButton(
                    Material.EMERALD_BLOCK,
                    "§aGlobal Trampling",
                    configManager.isGloballyEnabled() ? "§aEnabled" : "§cDisabled"
            );
            inventory.setItem(13, globalButton);
        }
    }

    private ItemStack createButton(Material material, String name, String status) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.List.of(status, "", "§7Click to toggle"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot == 11) { // personal toggle
            boolean newState = !configManager.isTramplingEnabled(player.getUniqueId());
            configManager.setTramplingEnabled(player.getUniqueId(), newState);
            player.sendMessage("§7Personal trampling " + (newState ? "§aenabled" : "§cdisabled"));

            inventory.clear();
            initializeItems(player);
        } else if (slot == 13) { // global toggle
            boolean newState = !configManager.isGloballyEnabled();
            configManager.setGloballyEnabled(newState);
            player.sendMessage("§7Trampling globally " + (newState ? "§aenabled" : "§cdisabled"));

            inventory.clear();
            initializeItems(player);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        event.getPlayer().sendMessage("§7Opened Trampling Settings.");
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        event.getPlayer().sendMessage("§7Closed Trampling Settings.");
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void openMenu(Player player) {
        inventory.clear(); 
        initializeItems(player); 
        GUIManager.getInstance().registerHandledInventory(this.inventory, this);
        player.openInventory(this.inventory);
    }


    @Override
    public boolean canAccessSlot(Player player, int slot) {
        // slot 11 personal toggle (always accessible)
        if (slot == 11) {
            return true;
        }
        // slot 13 global toggle (requires admin permission)
        if (slot == 13) {
            return player.hasPermission("farmstuff.trampling.global");
        }
        return false;
    }

}
