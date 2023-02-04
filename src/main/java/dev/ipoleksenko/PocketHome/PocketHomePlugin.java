package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class PocketHomePlugin extends JavaPlugin {

	private static final String pocketsDir = "pockets";
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
		return pocketsDir + '/';
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
		pocketManager = new PocketManager(new PocketChunkGenerator());

		this.loadPocketWorlds();
	}

	@Override
	public void onDisable() {
	}


	/**
	 * Preload (re-create) pocket worlds to be able to teleport into it
	 */
	private void loadPocketWorlds() {
		File dir = new File(getPocketsDir());
		if (!dir.exists()) return;

		File[] pocketDirs = dir.listFiles();
		if (pocketDirs == null) return;

		for (File pocketDir : pocketDirs)
			if (pocketDir.isDirectory()) {
				String[] pocketFiles = pocketDir.list();
				if (pocketFiles != null && Arrays.asList(pocketFiles).contains("level.dat"))
					getPocketManager().getPocketCreator(getPocketsDir() + pocketDir.getName()).createWorld();
			}
	}
}
