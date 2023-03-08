package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.listener.ChestListener;
import dev.ipoleksenko.PocketHome.listener.DamageListener;
import dev.ipoleksenko.PocketHome.listener.EnderChestListener;
import dev.ipoleksenko.PocketHome.listener.PlayerListener;
import dev.ipoleksenko.PocketHome.manager.LinkerManager;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PocketHomePlugin extends JavaPlugin {

	private static final String POCKETS_DIR = "pockets";
	private static final Integer POCKET_RADIUS = 2; // in chunks; generate (2 * n)^2 chunks
	private static PocketHomePlugin instance;
	private PocketManager pocketManager;
	private LinkerManager linkerManager;

	/**
	 * Get PocketHome plugin instance
	 *
	 * @return PocketHome plugin instance
	 */
	public static PocketHomePlugin getInstance() {
		return instance;
	}

	/**
	 * Get pockets directory
	 *
	 * @return Pockets directory
	 */
	public static @NotNull String getPocketsDir() {
		return POCKETS_DIR + '/';
	}

	public static Integer getPocketRadius() {
		return POCKET_RADIUS;
	}

	/**
	 * Generates unique ID
	 *
	 * @return alphanumeric id
	 */
	public static @NotNull String getUniqueId() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

	/**
	 * Generates unique ID with prefix
	 *
	 * @param prefix id prefix
	 * @return prefix + id
	 */
	public static @NotNull String getUniqueId(String prefix) {
		return prefix + getUniqueId();
	}

	/**
	 * Creates NamespacedKey for saving data in PersistentDataContainer
	 *
	 * @param key name of the key
	 * @return pockethome:key
	 */
	public NamespacedKey getNamespacedKey(String key) {
		return NamespacedKey.fromString(key, this);
	}

	/**
	 * Get PocketManager instance
	 *
	 * @return PocketManager instance
	 * @see PocketManager
	 */
	public PocketManager getPocketManager() {
		return this.pocketManager;
	}

	/**
	 * Get LinkerManager instance
	 *
	 * @return LinkerManager instance
	 * @see LinkerManager
	 */
	public LinkerManager getLinkerManager() {
		return this.linkerManager;
	}

	@Override
	public void onEnable() {
		instance = this;
		this.pocketManager = new PocketManager();
		this.linkerManager = new LinkerManager();

		getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new DamageListener(), this);
		getServer().getPluginManager().registerEvents(new ChestListener(), this);
	}

	@Override
	public void onDisable() {
	}
}