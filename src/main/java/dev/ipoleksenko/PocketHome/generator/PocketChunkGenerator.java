package dev.ipoleksenko.PocketHome.generator;

import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PocketChunkGenerator extends ChunkGenerator {

	@Override
	public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
		return Collections.emptyList();
	}

	@Override
	public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
		return new PocketBiomeProvider();
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
