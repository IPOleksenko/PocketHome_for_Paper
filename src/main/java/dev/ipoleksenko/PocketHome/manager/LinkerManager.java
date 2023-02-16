package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.LinkerChunkGenerator;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LinkerManager extends PluginWorldManager {

	private final LinkerChunkGenerator linkerGenerator;

	public LinkerManager() {
		super();
		linkerInstance = this;

		this.linkerGenerator = new LinkerChunkGenerator();
	}


	private @NotNull String getLinkedPath(String linkerName) {
		return super.getWorldPath(linkerName);
	}

	private ChunkGenerator getLinkerGenerator() {
		return this.linkerGenerator;
	}

	/**
	 * Get WorldCreator for Linker worlds
	 *
	 * @return WorldCreator for a Linker worlds object
	 */
	private @NotNull WorldCreator getLinkerCreator(String linkerName) {
		return this.getWorldCreator(this.getLinkedPath(linkerName)).generator(this.getLinkerGenerator());
	}


	/**
	 * Check if Pocket is linked
	 *
	 * @param player Player object
	 * @return true if linked
	 */
	public boolean isLinked(@NotNull Player player) {
		final World pocket = pocketInstance.getPocket(player);
		final World linker = this.getLinker(player);
		if (linker == null || pocket == null) return false;

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();

		final List<String> linkerPocketsName = linkerContainer.getOrDefault(linkedPocketKey, DataType.STRING_LIST, new LinkedList<String>());
		final String pocketName = playerContainer.getOrDefault(pocketKey, PersistentDataType.STRING, "");
		if (!linkerPocketsName.contains(pocketName)) {
			playerContainer.remove(linkedPocketKey);
			pocketContainer.remove(linkedPocketKey);
		}

		return linkerPocketsName.contains(pocketName);
	}

	/**
	 * Teleport Player to Linker
	 *
	 * @param player target Player object
	 * @return true, if the teleport was successful
	 */
	protected boolean teleportToLinker(@NotNull Player player) {
		return this.teleportToLinker(player, player);
	}

	/**
	 * Teleport Player to other's Player Linker
	 *
	 * @param player      target Player object
	 * @param otherPlayer destination Player object
	 * @return true, if the teleport was successful
	 */
	protected boolean teleportToLinker(@NotNull Player player, @NotNull Player otherPlayer) {
		final World linker = this.getLinker(otherPlayer);
		if (linker == null) return false;

		return super.teleportTo(player, linker);
	}

	/**
	 * Link Pockets.
	 * Links a Player Pocket to Player Linker.
	 * If none of the players has Linker, it will be created.
	 *
	 * @param player      requester Player object
	 * @param otherPlayer other Player object
	 * @return true, if success. false, if Players haven't Pockets yet
	 */
	public boolean linkPockets(@NotNull Player player, @NotNull Player otherPlayer) {
		if (this.isLinked(player) && this.isLinked(otherPlayer)) return false;

		World pocket = pocketInstance.getPocket(player);
		World otherPocket = pocketInstance.getPocket(otherPlayer);
		if (pocket == null || otherPocket == null) return false;

		pocketInstance.unloadPocket(pocket);
		pocketInstance.unloadPocket(otherPocket);

		World linker = null;
		if (this.isLinked(player)) linker = this.getLinker(player);
		else if (this.isLinked(otherPlayer)) linker = this.getLinker(otherPlayer);

		if (linker == null) linker = this.createLinker(player, otherPlayer);
		else {
			this.syncChunks(linker, true);
			linker = this.createLinker(player, otherPlayer, linker.getName().split("/")[1]);
		}

		this.syncChunks(linker, false);

		return true;
	}

	/**
	 * Unlinks OfflinePlayer Pocket from Linker
	 *
	 * @param player      Player object to get Linker
	 * @param otherPlayer target OfflinePlayer object
	 * @return true, if unlink successful
	 */
	public boolean unlinkPockets(@NotNull Player player, @NotNull OfflinePlayer otherPlayer) {
		if (!this.isLinked(player)) return false;

		final World linker = this.getLinker(player);
		final List<OfflinePlayer> linkedPlayers = this.getLinkedPlayers(player).stream().filter(offlinePlayer -> offlinePlayer == otherPlayer).toList();
		if (linker == player.getWorld()) this.teleportFrom(player);

		if (linkedPlayers.isEmpty()) return false;

		this.syncChunks(linker, true);
		this.deleteLinker(linker, otherPlayer);

		return true;
	}


	/**
	 * Unlinks Player Pocket from Linker
	 *
	 * @param player target Player object
	 * @return true, if Player Pocket is linked
	 */
	public boolean unlinkPockets(@NotNull Player player) {
		if (!this.isLinked(player)) return false;

		final World linker = this.getLinker(player);
		if (linker == null) return false;
		for (Player linkerPlayer : linker.getPlayers())
			this.teleportFrom(linkerPlayer);

		this.syncChunks(linker, true);
		this.deleteLinker(player);
		this.syncChunks(linker, false);

		return true;
	}

	public List<OfflinePlayer> getLinkedPlayers(@NotNull Player player) {
		final World linker = this.getLinker(player);
		if (linker == null) return null;

		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();
		final List<String> linkerPocketsName = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkerPocketsName == null) return new LinkedList<>();

		return linkerPocketsName.stream()
						.map(pocketInstance::loadPocket)
						.map(pocketInstance::getPocketOwner)
						.toList();
	}


	private @NotNull Integer getChunksAtLevel(Integer level) {
		if (level == 0) return 0;
		return 4 * level + this.getChunksAtLevel(level - 1);
	}

	private @NotNull Integer getLevelAtChunks(Integer chunks) {
		int level;
		for (level = 0; level < chunks; ++level)
			if (this.getChunksAtLevel(level) >= chunks) break;

		return level;
	}

	protected @Nullable World getLinker(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final String linkerName = playerContainer.get(linkedPocketKey, PersistentDataType.STRING);
		if (linkerName == null) return null;

		World linker = Bukkit.getWorld(getWorldPath(linkerName));
		if (linker == null) linker = this.createLinker(player, player, linkerName);

		this.syncChunks(linker, true);

		return linker;
	}

	private void syncChunks(@NotNull World linker, boolean savePockets) {
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();
		List<String> linkerPocketNames = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkerPocketNames == null) linkerPocketNames = new LinkedList<>();

		final int radius = PocketHomePlugin.getPocketRadius();
		final int linkerLevels = this.getLevelAtChunks(linkerPocketNames.size());

		final Iterator<World> pocketWorlds = linkerPocketNames.stream().map(pocketInstance::loadPocket).iterator();
		for (int level = 1; level <= linkerLevels; ++level) {
			final int max = level * radius * 2;
			for (int linkerChunkX = -max; linkerChunkX <= max; linkerChunkX += 4 * radius)
				for (int linkerChunkZ = -max; linkerChunkZ <= max; linkerChunkZ += 4 * radius) {
					if (Math.abs(linkerChunkX) != max && Math.abs(linkerChunkZ) != max) continue;

					if (!pocketWorlds.hasNext()) break;
					final World pocket = pocketWorlds.next();
					for (int pocketChunkX = -radius; pocketChunkX < radius; ++pocketChunkX)
						for (int pocketChunkZ = -radius; pocketChunkZ < radius; ++pocketChunkZ) {
							final int toChunkX = linkerChunkX + pocketChunkX;
							final int toChunkZ = linkerChunkZ + pocketChunkZ;
							if (savePockets) {
								final Chunk pocketChunk = pocket.getChunkAt(pocketChunkX, pocketChunkZ);
								final ChunkSnapshot linkerChunkSnapshot = linker.getChunkAt(toChunkX, toChunkZ).getChunkSnapshot();
								Bukkit.getScheduler().runTask(PocketHomePlugin.getInstance(), () -> this.copyChunk(linkerChunkSnapshot, pocketChunk));
							} else {
								final ChunkSnapshot pocketChunkSnapshot = pocket.getChunkAt(pocketChunkX, pocketChunkZ).getChunkSnapshot();
								final Chunk linkerChunk = linker.getChunkAt(toChunkX, toChunkZ);
								Bukkit.getScheduler().runTask(PocketHomePlugin.getInstance(), () -> this.copyChunk(pocketChunkSnapshot, linkerChunk));
							}
						}
				}
		}
	}

	private void copyChunk(ChunkSnapshot from, Chunk to) {
		for (int x = 0; x < 16; ++x)
			for (int y = -64; y < 320; ++y)
				for (int z = 0; z < 16; ++z) {
					final Block toBlock = to.getBlock(x, y, z);
					final Material fromBlockType = from.getBlockType(x, y, z);
					final BlockData fromBlockData = from.getBlockData(x, y, z);

					if (toBlock.getType() == fromBlockType) continue;
					toBlock.setType(fromBlockType);
					toBlock.setBlockData(fromBlockData);
				}
	}

	private @NotNull World createLinker(Player player, Player otherPlayer) {
		return this.createLinker(player, otherPlayer, this.getUniqueId("__lnk"));
	}

	private @NotNull World createLinker(@NotNull Player player, @NotNull Player otherPlayer, String linkerName) {
		final World linker = this.getLinkerCreator(linkerName).createWorld();

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PersistentDataContainer otherPlayerContainer = otherPlayer.getPersistentDataContainer();
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();

		playerContainer.set(linkedPocketKey, PersistentDataType.STRING, linker.getName().split("/")[1]);
		otherPlayerContainer.set(linkedPocketKey, PersistentDataType.STRING, linker.getName().split("/")[1]);

		List<String> linkedPockets = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkedPockets == null) linkedPockets = new LinkedList<>();

		String pocketName = playerContainer.get(pocketKey, PersistentDataType.STRING);
		if (!linkedPockets.contains(pocketName)) linkedPockets.add(pocketName);
		pocketName = otherPlayerContainer.get(pocketKey, PersistentDataType.STRING);
		if (!linkedPockets.contains(pocketName)) linkedPockets.add(pocketName);

		linkerContainer.set(linkedPocketKey, DataType.STRING_LIST, linkedPockets);

		linker.setSpawnLocation(8, 1, 8);

		return linker;
	}

	private void deleteLinker(@NotNull Player player) {
		final World linker = this.getLinker(player);
		if (linker == null) return;

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();

		playerContainer.remove(linkedPocketKey);

		List<String> linkedPockets = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkedPockets == null) linkedPockets = new LinkedList<>();

		final String pocketName = playerContainer.get(pocketKey, PersistentDataType.STRING);
		linkedPockets.remove(pocketName);
		linkerContainer.set(linkedPocketKey, DataType.STRING_LIST, linkedPockets);
	}

	private void deleteLinker(@NotNull World linker, @NotNull OfflinePlayer player) {
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();

		List<String> linkedPockets = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkedPockets == null) linkedPockets = new LinkedList<>();

		linkedPockets.remove(player.getName());
		linkerContainer.set(linkedPocketKey, DataType.STRING_LIST, linkedPockets);
	}
}
