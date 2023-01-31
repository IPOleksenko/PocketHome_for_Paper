package dev.ipoleksenko.PocketHome;

import org.bukkit.plugin.java.JavaPlugin;

public class PocketHomePlugin extends JavaPlugin {
  public static PocketHomePlugin instance;

  public static PocketHomePlugin getInstance() {
    return instance;
  }
  
  @Override
  public void onEnable() {
    instance = this;
  }

  @Override
  public void onDisable() {
  }
}
