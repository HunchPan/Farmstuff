package org.farmstuff.farmstuff.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.farmstuff.farmstuff.config.TramplingConfigManager;

public class TramplingMenu {

    private final TramplingConfigManager configManager;

    public TramplingMenu(TramplingConfigManager configManager) {
        this.configManager = configManager;
    }

    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "Trampling Settings");

        // Global toggle button
        ItemStack globalButton = createButton(
                Material.EMERALD_BLOCK,
                "§aGlobal Trampling",
                configManager.isGloballyEnabled() ? "§aEnabled" : "§cDisabled"
        );
        menu.setItem(13, globalButton);


        player.openInventory(menu);
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
}
