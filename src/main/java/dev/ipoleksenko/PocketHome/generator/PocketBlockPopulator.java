package dev.ipoleksenko.PocketHome.generator;

import com.google.common.collect.Range;
import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PocketBlockPopulator extends BlockPopulator {

	@Override
	public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
		Integer radius = PocketHomePlugin.getPocketRadius();
		Range<Integer> chunks = Range.closedOpen(-radius, radius);
		if (chunks.contains(chunkX) && chunks.contains(chunkZ))
			for (int x = limitedRegion.getCenterBlockX(); x < limitedRegion.getCenterBlockX() + 16; ++x)
				for (int z = limitedRegion.getCenterBlockZ(); z < limitedRegion.getCenterBlockZ() + 16; ++z) {
					limitedRegion.setType(x, -64, z, Material.BEDROCK);

					for (int y = -63; y < -3; ++y)
						limitedRegion.setType(x, y, z, Material.STONE);
					for (int y = -3; y < 0; ++y)
						limitedRegion.setType(x, y, z, Material.DIRT);

					limitedRegion.setType(x, 0, z, Material.GRASS_BLOCK);
				}
	}
}
