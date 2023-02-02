package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nullable;

public class PocketManager {

	private final PocketChunkGenerator generator;

	public PocketManager(PocketChunkGenerator generator) {
		this.generator = generator;
	}

	@Nullable
	public World getPocket(String worldName) {
		World world = Bukkit.getWorld(worldName);
		if (world == null)
			world = this.createPocket(worldName);

		return world;
	}

	@Nullable
	private World createPocket(String worldName) {
		World world = Bukkit.createWorld(this.getWorldCreator(worldName));
		if (world == null) return null;

		for (int x = -16; x < 16; ++x)
			for (int z = -16; z < 16; ++z) {
				world.getBlockAt(x, 0, z).setType(Material.BEDROCK);
				world.getBlockAt(x, 1, z).setType(Material.DIRT);
				world.getBlockAt(x, 2, z).setType(Material.DIRT);
				world.getBlockAt(x, 3, z).setType(Material.GRASS_BLOCK);
			}

		world.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);

		WorldBorder border = world.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(32);
		border.setWarningDistance(0);

		return world;
	}

	private WorldCreator getWorldCreator(String worldName) {
		return new WorldCreator(worldName)
						.generator(getGenerator())
						.environment(World.Environment.NORMAL);
	}

	private ChunkGenerator getGenerator() {
		return this.generator;
	}
}
