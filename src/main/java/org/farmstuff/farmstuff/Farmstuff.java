package org.farmstuff.farmstuff;

import org.bukkit.plugin.java.JavaPlugin;
import org.farmstuff.farmstuff.command.TramplingCommand;
import org.farmstuff.farmstuff.config.TramplingConfigManager;
import org.farmstuff.farmstuff.listener.GUIListener;
import org.farmstuff.farmstuff.listener.HarvestListener;
import org.farmstuff.farmstuff.listener.TramplingListener;
import org.farmstuff.farmstuff.menu.GUIManager;

public final class Farmstuff extends JavaPlugin {

    private TramplingConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new TramplingConfigManager(this);

        getServer().getPluginManager().registerEvents(new HarvestListener(), this);
        getServer().getPluginManager().registerEvents(new TramplingListener(configManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(GUIManager.getInstance()), this);

        getCommand("trampling").setExecutor(new TramplingCommand(configManager));
    }


    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.saveData();
        }
    }
}
