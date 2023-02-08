package dev.ipoleksenko.PocketHome.generator;

import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Generates a total void world
 */
public abstract class VoidChunkGenerator extends ChunkGenerator {

	@Override
	public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
		return new VoidBiomeProvider();
	}

	@Override
	public boolean shouldGenerateNoise() {
		return false;
	}

	@Override
	public boolean shouldGenerateSurface() {
		return false;
	}

	@Override
	public boolean shouldGenerateCaves() {
		return false;
	}

	@Override
	public boolean shouldGenerateDecorations() {
		return false;
	}

	@Override
	public boolean shouldGenerateMobs() {
		return false;
	}

	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}
}
