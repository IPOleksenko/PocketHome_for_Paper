package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.listener.ChestListener;
import dev.ipoleksenko.PocketHome.listener.DamageListener;
import dev.ipoleksenko.PocketHome.listener.EnderChestListener;
import dev.ipoleksenko.PocketHome.listener.PlayerListener;
import dev.ipoleksenko.PocketHome.manager.LinkerManager;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
	@Contract(pure = true)
	public static @NotNull String getPocketsDir() {
		return POCKETS_DIR + '/';
	}

	public static Integer getPocketRadius() {
		return POCKET_RADIUS;
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