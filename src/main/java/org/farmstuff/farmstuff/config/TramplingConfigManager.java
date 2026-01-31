package org.farmstuff.farmstuff.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TramplingConfigManager {

    private Map<UUID, Boolean> playerMap;
    private boolean globallyEnabled;
    private JavaPlugin plugin;
    private File tramplingFile;

    private void loadData() {
        if (!tramplingFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(tramplingFile);
        globallyEnabled = config.getBoolean("global", true);

        if (config.contains("players")) {
            for (String key : config.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                boolean enabled = config.getBoolean("players." + key);
                playerMap.put(uuid, enabled);
            }
        }
    }

    public void saveData() {
        // create directory if they dont exist
        tramplingFile.getParentFile().mkdirs();

        FileConfiguration config = new YamlConfiguration();
        config.set("global", globallyEnabled);

        // loops through every uuid and save config
        for (UUID uuid : playerMap.keySet()) {
            config.set("players." + uuid.toString(), playerMap.get(uuid));
        }

        try {
            config.save(tramplingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TramplingConfigManager(JavaPlugin plugin){
        this.plugin = plugin;
        this.tramplingFile = new File(plugin.getDataFolder(), "trampling.yml");
        this.playerMap = new HashMap<>();
        loadData();
    }

    public boolean isTramplingEnabled(UUID uuid){
        return playerMap.getOrDefault(uuid, true);
    }

    public void setTramplingEnabled(UUID uuid, boolean enabled){
        playerMap.put(uuid, enabled);
        saveData();
    }

    public boolean isGloballyEnabled() {
        return globallyEnabled;
    }

    public void setGloballyEnabled(boolean enabled) {
        this.globallyEnabled = enabled;
        saveData();
    }

}
