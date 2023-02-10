package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.LinkerChunkGenerator;
import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Manager for pocket worlds
 * <p/>
 * Keys:<br/>
 * pockethome:pocket — Name of Pocket inside Player<br/>
 * pockethome:pocket_owner — UUID of Player inside Pocket<br/>
 * pockethome:pocket_guests — List<UUID> of Pockets inside Player, of Players inside Pocket<br/>
 * pockethome:teleport_location — Location of Player inside Player<br/>
 * pockethome:linked_pocket — UUID of Linker inside Player and Pocket, List<UUID> of Pockets inside Linker
 */
public class PocketManager {

	private final NamespacedKey pocketKey;
	private final NamespacedKey pocketOwnerKey;
	private final NamespacedKey pocketGuestsKey;
	private final NamespacedKey teleportLocationKey;
	private final NamespacedKey linkedPocketKey;

	private final PocketChunkGenerator pocketGenerator;
	private final LinkerChunkGenerator linkerGenerator;


	public PocketManager() {
		this.pocketKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.pocketOwnerKey = NamespacedKey.fromString("pocket_owner", PocketHomePlugin.getInstance());
		this.pocketGuestsKey = NamespacedKey.fromString("pocket_guests", PocketHomePlugin.getInstance());
		this.teleportLocationKey = NamespacedKey.fromString("teleport_location", PocketHomePlugin.getInstance());
		this.linkedPocketKey = NamespacedKey.fromString("linked_pocket", PocketHomePlugin.getInstance());

		this.pocketGenerator = new PocketChunkGenerator();
		this.linkerGenerator = new LinkerChunkGenerator();
	}


	private @NotNull String getUniqueId() {
		final Base64.Encoder encoder = Base64.getUrlEncoder();
		final ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
		final UUID uuid = UUID.randomUUID();

		bb.putLong(uuid.getLeastSignificantBits());
		final byte[] bytes = bb.array();

		return "__lnk" + encoder.encodeToString(bytes).replace("=", "").toLowerCase();
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

	/**
	 * Used for saving Pocket in separate folder
	 *
	 * @param pocketName name of a Pocket
	 * @return <i>pocketsDir</i>/<i>pocketName</i>
	 */
	@Contract(pure = true)
	private @NotNull String getPocketPath(String pocketName) {
		return PocketHomePlugin.getPocketsDir() + pocketName;
	}

	private @NotNull String getLinkedPath(String linkerName) {
		return this.getPocketPath(linkerName);
	}

	private ChunkGenerator getLinkerGenerator() {
		return this.linkerGenerator;
	}

	private ChunkGenerator getPocketGenerator() {
		return this.pocketGenerator;
	}

	private @NotNull WorldCreator getWorldCreator(String name) {
		return new WorldCreator(name).environment(World.Environment.NORMAL);
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
	 * Get WorldCreator for Pocket worlds
	 *
	 * @param pocketName name of a Pocket
	 * @return WorldCreator for a Pocket worlds object
	 */
	private @NotNull WorldCreator getPocketCreator(String pocketName) {
		return this.getWorldCreator(this.getPocketPath(pocketName)).generator(this.getPocketGenerator());
	}


	/**
	 * Checks if a player is in a Pocket
	 *
	 * @param player Player object
	 * @return true, if in a pocket, false otherwise
	 */
	public boolean isInPocket(@NotNull Player player) {
		return player.getWorld().getName().contains(PocketHomePlugin.getPocketsDir());
	}

	public boolean isLinked(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		return playerContainer.has(linkedPocketKey);
	}

	public void loadPockets(Player @NotNull ... players) {
		for (Player player : players)
			this.createPocket(player);
	}

	/**
	 * Teleports player to his Pocket
	 *
	 * @param player target Player object
	 * @return true on successful teleport
	 */
	public boolean teleportToPocket(Player player) {
		return this.teleportToPocket(player, player);
	}

	/**
	 * Teleports player to a specified Player Pocket
	 *
	 * @param player      target Player object
	 * @param otherPlayer destination Player object
	 * @return true on successful teleport, false if pocket does not exist
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull Player otherPlayer) {
		final World pocket = this.isLinked(player) ? this.getLinker(player) : this.getPocket(otherPlayer, player == otherPlayer);
		if (pocket == null) return false;

		return this.teleportToPocket(player, pocket);
	}

	/**
	 * Teleports player to a specified Pocket
	 *
	 * @param player target Player object
	 * @param pocket destination Pocket object
	 * @return always true
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull World pocket) {
		if (!this.isInPocket(player)) {
			final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
			playerContainer.set(teleportLocationKey, DataType.LOCATION, player.getLocation());
		}

		player.teleport(pocket.getSpawnLocation());
		player.setInvulnerable(true);
		return true;
	}

	/**
	 * Teleports Player to overworld.
	 * Trying to teleport to the latest location.
	 * Teleports to bed spawn location if the latest location does not exist.
	 * Otherwise, teleports to the overworld spawn
	 *
	 * @param player target Player object
	 * @return always true
	 */
	public boolean teleportFromPocket(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		Location location = playerContainer.get(teleportLocationKey, DataType.LOCATION);

		if (location == null) location = player.getBedSpawnLocation();
		if (location == null) location = Bukkit.getWorlds().get(0).getSpawnLocation();

		player.teleport(location);
		player.setInvulnerable(false);
		return true;
	}

	/**
	 * Adds to Player Pocket UUID. That Player will be able to visit pocket
	 *
	 * @param owner Player object, Pocket owner
	 * @param guest Player object, guest that will be able to visit Pocket
	 * @return true on success, false if pocket does not exist
	 */
	public boolean addGuestToPocket(@NotNull Player owner, @NotNull Player guest) {
		final World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.addGuest(pocketContainer, DataType.UUID_LIST, guest.getUniqueId());
		this.addGuest(guestContainer, DataType.STRING_LIST, pocket.getName());

		return true;
	}

	/**
	 * Removes from Player Pocket UUID. That Player will not be able to visit pocket
	 *
	 * @param owner Player object, Pocket owner
	 * @param guest Player object, guest that will not be able to visit Pocket
	 * @return true on success, false if pocket does not exist
	 */
	public boolean removeGuestFromPocket(@NotNull Player owner, @NotNull Player guest) {
		final World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.removeGuest(pocketContainer, DataType.UUID_LIST, guest.getUniqueId());
		this.removeGuest(guestContainer, DataType.STRING_LIST, pocket.getName());

		final World guestWorld = guest.getWorld();
		if (guestWorld.getUID() == pocket.getUID()) this.teleportFromPocket(guest);

		return true;
	}

	/**
	 * Get all Pockets that can be visited by Player
	 *
	 * @param player target Player object
	 * @return List of available to visit Pockets
	 */
	public List<World> getGuestPockets(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final List<String> guestPockets = playerContainer.get(pocketGuestsKey, DataType.STRING_LIST);
		if (guestPockets == null) return new LinkedList<>();

		return guestPockets.stream().map(Bukkit::getWorld).toList();
	}

	/**
	 * Get guests of a Player Pocket
	 *
	 * @param player Player object, pocket owner
	 * @return List of guests, null if player hasn't pocket
	 */
	public List<OfflinePlayer> getPocketGuests(@NotNull Player player) {
		final World pocket = this.getPocket(player, false);
		return (pocket != null) ? this.getPocketGuests(pocket) : null;
	}

	/**
	 * Get guests of a Pocket
	 *
	 * @param pocket Pocket object
	 * @return List of guests
	 */
	public List<OfflinePlayer> getPocketGuests(@NotNull World pocket) {
		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final List<UUID> pocketGuestsUID = pocketContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		if (pocketGuestsUID == null) return new LinkedList<>();

		return pocketGuestsUID.stream().map(Bukkit::getOfflinePlayer).toList();
	}

	/**
	 * Get Player that owns Pocket
	 *
	 * @param pocket Pocket object
	 * @return OfflinePlayer object
	 */
	public OfflinePlayer getPocketOwner(@NotNull World pocket) {
		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final UUID ownerUID = pocketContainer.get(pocketOwnerKey, DataType.UUID);

		return Bukkit.getOfflinePlayer(ownerUID);
	}

	public boolean linkPockets(@NotNull Player player, @NotNull Player otherPlayer) {
		if (this.isLinked(player) && this.isLinked(otherPlayer)) return false;

		final World pocket = this.getPocket(player, false);
		final World otherPocket = this.getPocket(otherPlayer, false);
		if (pocket == null || otherPocket == null) return false;

		this.reloadPockets(pocket, otherPocket);

		World linker = this.getLinker(player);
		if (linker == null) linker = this.getLinker(otherPlayer);
		if (linker == null) linker = this.createLinker(player, otherPlayer);

		this.syncChunks(linker, false);

		return true;
	}

	public boolean unlinkPockets(@NotNull Player player) {
		if (!this.isLinked(player)) return false;
		if (this.getLinker(player) == player.getWorld()) this.teleportFromPocket(player);
		this.deleteLinker(player);
		return true;
	}

	private <T, Z> void addGuest(@NotNull PersistentDataContainer container, PersistentDataType<Z, List> dataType, T data) {
		List<T> guests = container.get(pocketGuestsKey, dataType);
		if (guests == null) guests = new LinkedList<>();
		if (guests.contains(data)) return;

		guests.add(data);
		container.set(pocketGuestsKey, dataType, guests);
	}

	private <T, Z> void removeGuest(@NotNull PersistentDataContainer container, PersistentDataType<Z, List> type, T data) {
		List<T> guests = container.get(pocketGuestsKey, type);
		if (guests == null) guests = new LinkedList<>();

		guests.remove(data);
		container.set(pocketGuestsKey, type, guests);
	}

	@Nullable
	private World getPocket(@NotNull Player player, boolean createIfMissing) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final String pocketName = playerContainer.get(pocketKey, PersistentDataType.STRING);

		if (pocketName == null) return createIfMissing ? this.createPocket(player) : null;

		return Bukkit.getWorld(this.getPocketPath(pocketName));
	}

	private @NotNull World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		final World pocket = this.getPocketCreator(pocketName).createWorld();

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		playerContainer.set(pocketKey, PersistentDataType.STRING, pocket.getName().split("/")[1]);

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		pocketContainer.set(pocketOwnerKey, DataType.UUID, player.getUniqueId());

		this.generateMisc(pocket);
		pocket.setSpawnLocation(1, 1, -1);

		return pocket;
	}

	private void unloadPockets(World @NotNull ... pockets) {
		for (World pocket : pockets) {
			for (Player player : pocket.getPlayers())
				this.teleportFromPocket(player);
			Bukkit.unloadWorld(pocket, true);
		}
	}

	private void reloadPockets(World @NotNull ... pockets) {
		this.unloadPockets(pockets);
		final Player[] players = Arrays.stream(pockets).map(this::getPocketOwner).filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toArray(Player[]::new);
		this.loadPockets(players);
	}

	private @Nullable World getLinker(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final String linkerName = playerContainer.get(linkedPocketKey, PersistentDataType.STRING);
		if (linkerName == null) return null;

		World linker = Bukkit.getWorld(getPocketPath(linkerName));
		if (linker == null) linker = this.createLinker(player, player, linkerName);

		this.syncChunks(linker, true);

		return linker;
	}

	private void syncChunks(@NotNull World linker, boolean savePockets) {
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();
		List<String> linkerPockets = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkerPockets == null) linkerPockets = new LinkedList<>();

		final int radius = PocketHomePlugin.getPocketRadius();
		final int linkerLevels = this.getLevelAtChunks(linkerPockets.size());

		final Iterator<World> pocketWorlds = linkerPockets.stream().map(this::getLinkedPath).map(Bukkit::getWorld).iterator();
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
					to.getBlock(x, y, z).setType(from.getBlockType(x, y, z));
					to.getBlock(x, y, z).setBlockData(from.getBlockData(x, y, z));
				}
	}

	private @NotNull World createLinker(Player player, Player otherPlayer) {
		return this.createLinker(player, otherPlayer, this.getUniqueId());
	}

	private @NotNull World createLinker(@NotNull Player player, @NotNull Player otherPlayer, String linkerName) {
		final World linker = this.getLinkerCreator(linkerName).createWorld();
		PocketHomePlugin.getInstance().getLogger().info("Created linker: " + linker.getName());

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

		linker.getBlockAt(7, 1, 9).setType(Material.ENDER_CHEST);
		linker.setSpawnLocation(8, 1, 8);

		return linker;
	}

	private void deleteLinker(@NotNull Player player) {
		final World linker = this.getLinker(player);

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PersistentDataContainer linkerContainer = linker.getPersistentDataContainer();

		playerContainer.remove(linkedPocketKey);

		List<String> linkedPockets = linkerContainer.get(linkedPocketKey, DataType.STRING_LIST);
		if (linkedPockets == null) linkedPockets = new LinkedList<>();

		String pocketName = playerContainer.get(pocketKey, PersistentDataType.STRING);
		linkedPockets.remove(pocketName);
		linkerContainer.set(linkedPocketKey, DataType.STRING_LIST, linkedPockets);
	}

	private void generateMisc(@NotNull World pocket) {
		pocket.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		pocket.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);

		WorldBorder border = pocket.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(2 * PocketHomePlugin.getPocketRadius() * 16);
		border.setWarningDistance(0);
	}
}
