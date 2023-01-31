package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketHomePlugin extends JavaPlugin {
    public static PocketHomePlugin instance;
    private PocketManager pocketManager;

    public static PocketHomePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        pocketManager = new PocketManager(new PocketChunkGenerator());
    }

    @Override
    public void onDisable() {
    }

    public PocketManager getWorldManager() {
        return this.pocketManager;
    }
}
