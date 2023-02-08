package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PocketHomePlugin extends JavaPlugin {

	private static final String POCKETS_DIR = "pockets";
	private static final Integer POCKET_RADIUS = 2; // in chunks; generate (2 * n)^2 chunks
	private static PocketHomePlugin instance;
	private PocketManager pocketManager;

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
	 */
	public PocketManager getPocketManager() {
		return this.pocketManager;
	}

	@Override
	public void onEnable() {
		instance = this;
		pocketManager = new PocketManager();
	}

	@Override
	public void onDisable() {
	}
}