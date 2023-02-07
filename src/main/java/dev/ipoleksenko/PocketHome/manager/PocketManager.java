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
 * <p/>
 * Keys:<br/>
 * pockethome:pocket — UUID of Pocket inside Player<br/>
 * pockethome:pocket_owner — UUID of Player inside Pocket<br/>
 * pockethome:pocket_guests — List<UUID> of Pockets inside Player, of Players inside Pocket<br/>
 * pockethome:teleport_location — Location of Player inside Player<br/>
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
	 * Used for saving Pocket in separate folder
	 *
	 * @param pocketName name of a Pocket
	 * @return <i>pocketsDir</i>/<i>pocketName</i>
	 */
	@Contract(pure = true)
	@NotNull
	private String getPocketPath(String pocketName) {
		return PocketHomePlugin.getPocketsDir() + pocketName;
	}

	/**
	 * Get WorldCreator for Pocket worlds
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
	 * Checks if a player is in a Pocket
	 *
	 * @param player Player object
	 * @return true, if in a pocket, false otherwise
	 */
	public boolean isInPocket(@NotNull Player player) {
		return player.getWorld().getName().contains(PocketHomePlugin.getPocketsDir());
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
		World pocket = this.getPocket(otherPlayer, player == otherPlayer);
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
			PersistentDataContainer playerContainer = player.getPersistentDataContainer();
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
		PersistentDataContainer playerContainer = player.getPersistentDataContainer();

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
		PersistentDataContainer ownerContainer = owner.getPersistentDataContainer();
		UUID pocketUID = ownerContainer.get(pocketUIDKey, DataType.UUID);
		UUID guestUID = guest.getUniqueId();

		World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.addGuest(pocketContainer, guestUID);
		this.addGuest(guestContainer, pocketUID);

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
		PersistentDataContainer ownerContainer = owner.getPersistentDataContainer();
		UUID pocketUID = ownerContainer.get(pocketUIDKey, DataType.UUID);
		UUID guestUID = guest.getUniqueId();

		World pocket = this.getPocket(owner, false);
		if (pocket == null) return false;

		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.removeGuest(pocketContainer, guestUID);
		this.removeGuest(guestContainer, pocketUID);

		World guestWorld = guest.getWorld();
		if (guestWorld.getUID() == pocketUID) this.teleportFromPocket(guest);

		return true;
	}

	/**
	 * Get all Pockets that can be visited by Player
	 *
	 * @param player target Player object
	 * @return List of available to visit Pockets
	 */
	public List<World> getGuestPockets(@NotNull Player player) {
		PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		List<UUID> guestPocketsUID = playerContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		if (guestPocketsUID == null) return new ArrayList<World>();

		return guestPocketsUID.stream().map(Bukkit::getWorld).toList();
	}

	/**
	 * Get guests of a Player Pocket
	 *
	 * @param player Player object, pocket owner
	 * @return List of guests, null if player hasn't pocket
	 */
	public List<OfflinePlayer> getPocketGuests(@NotNull Player player) {
		World pocket = this.getPocket(player, false);
		return (pocket != null) ? this.getPocketGuests(pocket) : null;
	}

	/**
	 * Get guests of a Pocket
	 *
	 * @param pocket Pocket object
	 * @return List of guests
	 */
	public List<OfflinePlayer> getPocketGuests(@NotNull World pocket) {
		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		List<UUID> pocketGuestsUID = pocketContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		if (pocketGuestsUID == null) return new ArrayList<OfflinePlayer>();

		return pocketGuestsUID.stream().map(Bukkit::getOfflinePlayer).toList();
	}

	/**
	 * Get Player that owns Pocket
	 *
	 * @param pocket Pocket object
	 * @return OfflinePlayer object
	 */
	public OfflinePlayer getPocketOwner(@NotNull World pocket) {
		PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		UUID ownerUID = pocketContainer.get(pocketOwnerKey, DataType.UUID);

		return Bukkit.getOfflinePlayer(ownerUID);
	}

	private void addGuest(@NotNull PersistentDataContainer container, UUID uuid) {
		List<UUID> guests = container.get(pocketGuestsKey, DataType.UUID_LIST);
		if (guests == null) guests = new ArrayList<UUID>();
		if (guests.contains(uuid)) return;

		guests.add(uuid);
		container.set(pocketGuestsKey, DataType.UUID_LIST, guests);
	}

	private void removeGuest(@NotNull PersistentDataContainer container, UUID uuid) {
		List<UUID> guests = container.get(pocketGuestsKey, DataType.UUID_LIST);
		if (guests == null) guests = new ArrayList<>();

		guests.remove(uuid);
		container.set(pocketGuestsKey, DataType.UUID_LIST, guests);
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
