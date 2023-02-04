package dev.ipoleksenko.PocketHome.generator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides minecraft:the_void biomes for all pocket worlds
 */
public class PocketBiomeProvider extends BiomeProvider {

	@Override
	@NotNull
	public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
		return Biome.THE_VOID;
	}

	@Override
	@NotNull
	public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
		return List.of(Biome.THE_VOID);
	}
}
