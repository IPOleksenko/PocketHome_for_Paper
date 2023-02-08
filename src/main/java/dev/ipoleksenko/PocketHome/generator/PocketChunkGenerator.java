package dev.ipoleksenko.PocketHome.generator;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Generates a world with an island at a center
 */
public class PocketChunkGenerator extends VoidChunkGenerator {

	@Override
	public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
		return Collections.singletonList(new PocketBlockPopulator());
	}
}
