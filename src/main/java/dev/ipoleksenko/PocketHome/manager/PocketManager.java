package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manager for pocket worlds
 */
public class PocketManager {

	private final NamespacedKey pocketUIDKey;
	private final NamespacedKey pocketOwnerKey;
	private final NamespacedKey pocketGuestsKey;
	private final NamespacedKey teleportLocationKey;
	private final PocketChunkGenerator generator;


	public PocketManager(PocketChunkGenerator generator) {
		this.pocketUIDKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.pocketOwnerKey = NamespacedKey.fromString("pocket_owner", PocketHomePlugin.getInstance());
		this.pocketGuestsKey = NamespacedKey.fromString("pocket_guests", PocketHomePlugin.getInstance());
		this.teleportLocationKey = NamespacedKey.fromString("teleport_location", PocketHomePlugin.getInstance());
		this.generator = generator;
	}


	/**
	 * Used for saving pockets in separate folder
	 *
	 * @param pocketName Name of a pocket
	 * @return <i>pocketsDir</i>/<i>pocketName</i>
	 */
	@Contract(pure = true)
	@NotNull
	private String getPocketPath(String pocketName) {
		return PocketHomePlugin.getPocketsDir() + pocketName;
	}

	/**
	 * Get WorldCreator for pocket worlds
	 *
	 * @param pocketName name of a pocket
	 * @return WorldCreator for pocket worlds object
	 */
	@NotNull
	public WorldCreator getPocketCreator(String pocketName) {
		return new WorldCreator(pocketName).generator(this.getGenerator()).environment(World.Environment.NORMAL);
	}

	private ChunkGenerator getGenerator() {
		return this.generator;
	}


	/**
	 * Checks if a player is in a pocket
	 *
	 * @param player Player object
	 * @return true, if in a pocket, false otherwise
	 */
	public boolean isInPocket(@NotNull Player player) {
		return player.getWorld().getName().contains(PocketHomePlugin.getPocketsDir());
	}

	/**
	 * Teleports player to his pocket
	 *
	 * @param player target Player object
	 * @return true on successful teleport, false if pocket does not exist
	 */
	public boolean teleportToPocket(Player player) {
		return this.teleportToPocket(player, player);
	}

	/**
	 * Teleports player to a specified player pocket
	 *
	 * @param player      target Player object
	 * @param otherPlayer destination Player object
	 * @return true on successful teleport, false if pocket does not exist
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull Player otherPlayer) {
		World pocket = this.getPocket(otherPlayer, player == otherPlayer);
		if (pocket == null) return false;

		return this.teleportToPocket(player, pocket);
	}

	/**
	 * Teleports player to a specified pocket
	 *
	 * @param player target Player object
	 * @param pocket destination pocket object
	 * @return always true
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull World pocket) {
		if (!this.isInPocket(player)) {
			PersistentDataContainer playerContainer = player.getPersistentDataContainer();
			playerContainer.set(teleportLocationKey, DataType.LOCATION, player.getLocation());
		}

		player.teleport(pocket.getSpawnLocation());
		player.setInvulnerable(true);
		return true;
	}

	/**
	 * Teleports player to overworld.
	 * Trying to teleport to the latest location.
	 * Teleports to bed spawn location if the latest location does not exist.
	 * Otherwise, teleports to the overworld spawn
	 *
	 * @param player target Player object
	 * @return always true
	 */
	public boolean teleportFromPocket(@NotNull Player player) {
		PersistentDataContainer playerContainer = player.getPersistentDataContainer();

		Location location = playerContainer.get(teleportLocationKey, DataType.LOCATION);

		if (location == null) location = player.getBedSpawnLocation();
		if (location == null) location = Bukkit.getWorlds().get(0).getSpawnLocation();

		player.teleport(location);
		player.setInvulnerable(false);

		return true;
	}

	/**
	 * Adds to Player pocket UUID. That Player will be able to visit pocket
	 *
	 * @param owner Player object, pocket owner
	 * @param guest Player object, guest that will be able to visit pocket
	 * @return always true
	 */
	public boolean addGuestToPocket(@NotNull Player owner, @NotNull Player guest) {
		PersistentDataContainer ownerContainer = owner.getPersistentDataContainer();
		UUID pocketUID = ownerContainer.get(pocketUIDKey, DataType.UUID);

		World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		List<UUID> pocketGuests = pocketContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		List<UUID> guestPockets = guestContainer.get(pocketGuestsKey, DataType.UUID_LIST);

		if (pocketGuests == null) pocketGuests = new ArrayList<UUID>();
		if (guestPockets == null) guestPockets = new ArrayList<UUID>();

		pocketGuests.add(guest.getUniqueId());
		guestPockets.add(pocketUID);

		pocketContainer.set(pocketGuestsKey, DataType.UUID_LIST, pocketGuests);
		guestContainer.set(pocketGuestsKey, DataType.UUID_LIST, guestPockets);

		return true;
	}

	/**
	 * Removes from Player pocket UUID. That Player will not be able to visit pocket
	 *
	 * @param owner Player object, pocket owner
	 * @param guest Player object, guest that will not be able to visit pocket
	 * @return always true
	 */
	public boolean removeGuestFromPocket(@NotNull Player owner, @NotNull Player guest) {
		PersistentDataContainer ownerContainer = owner.getPersistentDataContainer();
		UUID pocketUID = ownerContainer.get(pocketUIDKey, DataType.UUID);

		World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		List<UUID> pocketGuests = pocketContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		List<UUID> guestPockets = guestContainer.get(pocketGuestsKey, DataType.UUID_LIST);

		if (pocketGuests == null) pocketGuests = new ArrayList<UUID>();
		if (guestPockets == null) guestPockets = new ArrayList<UUID>();

		pocketGuests.remove(guest.getUniqueId());
		guestPockets.remove(pocketUID);

		pocketContainer.set(pocketGuestsKey, DataType.UUID_LIST, pocketGuests);
		guestContainer.set(pocketGuestsKey, DataType.UUID_LIST, guestPockets);

		if (this.isInPocket(guest)) this.teleportFromPocket(guest);

		return true;
	}

	/**
	 * Get all pockets that can be visited by Player
	 *
	 * @param player target Player object
	 * @return List of available to visit pockets
	 */
	public List<World> getGuestPockets(@NotNull Player player) {
		PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		List<UUID> playerGuestPocketsUID = playerContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		if (playerGuestPocketsUID == null) return new ArrayList<World>();

		return playerGuestPocketsUID.stream().map(Bukkit::getWorld).toList();
	}

	/**
	 * Get Player that owns pocket
	 *
	 * @param pocket Pocket object
	 * @return OfflinePlayer object
	 * Note: If player is online, it can easily be cast to Player with OfflinePlayer::getPlayer()
	 */
	public OfflinePlayer getPocketOwner(@NotNull World pocket) {
		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		UUID ownerUID = pocketContainer.get(pocketOwnerKey, DataType.UUID);

		return Bukkit.getOfflinePlayer(ownerUID);
	}

	@Nullable
	private World getPocket(@NotNull Player player, boolean createIfMissing) {
		PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final UUID pocketUID = playerContainer.get(pocketUIDKey, DataType.UUID);

		if (pocketUID == null && createIfMissing) return this.createPocket(player);
		if (pocketUID == null) return null;

		return Bukkit.getWorld(pocketUID);
	}

	@Nullable
	private World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		World pocket = this.getPocketCreator(this.getPocketPath(pocketName)).createWorld();
		if (pocket == null) return null;

		PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		playerContainer.set(pocketUIDKey, DataType.UUID, pocket.getUID());

		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		pocketContainer.set(pocketOwnerKey, DataType.UUID, player.getUniqueId());

		generateIsland(pocket);

		pocket.setSpawnLocation(1, 0, -1);

		return pocket;
	}

	private void generateIsland(World pocket) {
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
	}
}
