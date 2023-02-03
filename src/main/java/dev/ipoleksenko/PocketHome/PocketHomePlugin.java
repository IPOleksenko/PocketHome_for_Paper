package dev.ipoleksenko.PocketHome;

import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

public class PocketHomePlugin extends JavaPlugin {
	public static PocketHomePlugin instance;
	private PocketManager pocketManager;

	public static PocketHomePlugin getInstance() {
		return instance;
	}

	public PocketManager getPocketManager() {
		return this.pocketManager;
	}

	@Override
	public void onEnable() {
		instance = this;
		pocketManager = new PocketManager(new PocketChunkGenerator());

		loadPocketWorlds();
	}

	private void loadPocketWorlds() {
		File dir = new File("./pockets/");
		if (!dir.exists()) return;

		File[] dirPockets = dir.listFiles();
		if (dirPockets == null) return;

		for (File file : dirPockets)
			if (file.isDirectory()) {
				String[] pocketFiles = file.list();
				if (pocketFiles != null && Arrays.asList(pocketFiles).contains("level.dat"))
					WorldCreator.name("pockets/" + file.getName()).createWorld();
			}
	}

	@Override
	public void onDisable() {
	}
}
