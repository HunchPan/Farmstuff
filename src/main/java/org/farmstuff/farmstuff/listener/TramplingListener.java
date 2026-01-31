package org.farmstuff.farmstuff.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.farmstuff.farmstuff.config.TramplingConfigManager;

public class TramplingListener implements Listener {
    private final TramplingConfigManager configManager;

    public TramplingListener(TramplingConfigManager configManager){
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerTrample(PlayerInteractEvent e){

        if (e.getAction() != Action.PHYSICAL) return;

        Block block = e.getClickedBlock();

        if (block == null || block.getType() != Material.FARMLAND) return;

        Player player = e.getPlayer();

        if(!configManager.isGloballyEnabled() || !configManager.isTramplingEnabled(player.getUniqueId())){
            e.setCancelled(true);
        }
    }

}
