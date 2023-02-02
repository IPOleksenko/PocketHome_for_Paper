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


	private String getPocketPath(String pocketName) {
		return "pockets/" + pocketName;
	}

	private WorldCreator getPocketCreator(String pocketName) {
		return new WorldCreator(pocketName)
						.generator(getGenerator())
						.environment(World.Environment.NORMAL);
	}

	private ChunkGenerator getGenerator() {
		return this.generator;
	}


	@Nullable
	private World getPocket(String pocketName, boolean createIfMissing) {
		World pocket = Bukkit.getWorld(this.getPocketPath(pocketName));
		if (pocket == null && createIfMissing)
			pocket = this.createPocket(pocketName);

		return pocket;
	}

	@Nullable
	private World createPocket(String pocketName) {
		World pocket = Bukkit.createWorld(this.getPocketCreator(this.getPocketPath(pocketName)));
		if (pocket == null) return null;

		for (int x = -16; x < 16; ++x)
			for (int z = -16; z < 16; ++z) {
				pocket.getBlockAt(x, 0, z).setType(Material.BEDROCK);
				pocket.getBlockAt(x, 1, z).setType(Material.DIRT);
				pocket.getBlockAt(x, 2, z).setType(Material.DIRT);
				pocket.getBlockAt(x, 3, z).setType(Material.GRASS_BLOCK);
			}

		pocket.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);

		WorldBorder border = pocket.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(32);
		border.setWarningDistance(0);

		return pocket;
	}

}
