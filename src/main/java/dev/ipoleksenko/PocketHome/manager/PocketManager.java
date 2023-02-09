package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manager for pocket worlds
 * <p/>
 * Keys:<br/>
 * pockethome:pocket — Name of Pocket inside Player<br/>
 * pockethome:pocket_owner — UUID of Player inside Pocket<br/>
 * pockethome:pocket_guests — List<UUID> of Pockets inside Player, of Players inside Pocket<br/>
 * pockethome:teleport_location — Location of Player inside Player<br/>
 */
public class PocketManager {

	private final NamespacedKey pocketKey;
	private final NamespacedKey pocketOwnerKey;
	private final NamespacedKey pocketGuestsKey;
	private final NamespacedKey teleportLocationKey;

	private final PocketChunkGenerator pocketGenerator;


	public PocketManager() {
		this.pocketKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.pocketOwnerKey = NamespacedKey.fromString("pocket_owner", PocketHomePlugin.getInstance());
		this.pocketGuestsKey = NamespacedKey.fromString("pocket_guests", PocketHomePlugin.getInstance());
		this.teleportLocationKey = NamespacedKey.fromString("teleport_location", PocketHomePlugin.getInstance());

		this.pocketGenerator = new PocketChunkGenerator();
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

	private ChunkGenerator getPocketGenerator() {
		return this.pocketGenerator;
	}

	private @NotNull WorldCreator getWorldCreator(String name) {
		return new WorldCreator(name).environment(World.Environment.NORMAL);
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
		final World pocket = this.getPocket(otherPlayer, player == otherPlayer);
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
		final World ownerPocket = this.getPocket(owner, false);
		if (ownerPocket == null) return false;

		final PersistentDataContainer pocketContainer = ownerPocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.addGuest(pocketContainer, guest.getUniqueId());
		this.addGuest(guestContainer, ownerPocket.getUID());

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
		final World ownerPocket = this.getPocket(owner, false);
		if (ownerPocket == null) return false;

		final PersistentDataContainer pocketContainer = ownerPocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.removeGuest(pocketContainer, guest.getUniqueId());
		this.removeGuest(guestContainer, ownerPocket.getUID());

		final World guestWorld = guest.getWorld();
		if (guestWorld.getUID() == ownerPocket.getUID()) this.teleportFromPocket(guest);

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
		final List<UUID> guestPocketsUID = playerContainer.get(pocketGuestsKey, DataType.UUID_LIST);
		if (guestPocketsUID == null) return new ArrayList<>();

		return guestPocketsUID.stream().map(Bukkit::getWorld).toList();
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
		if (pocketGuestsUID == null) return new ArrayList<>();

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

	private void addGuest(@NotNull PersistentDataContainer container, UUID uuid) {
		List<UUID> guests = container.get(pocketGuestsKey, DataType.UUID_LIST);
		if (guests == null) guests = new ArrayList<>();
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
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final String pocketName = playerContainer.get(pocketKey, PersistentDataType.STRING);

		if (pocketName == null) return createIfMissing ? this.createPocket(player) : null;

		return Bukkit.getWorld(pocketName);
	}

	private @NotNull World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		final World pocket = this.getPocketCreator(pocketName).createWorld();

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		playerContainer.set(pocketKey, PersistentDataType.STRING, pocket.getName());

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		pocketContainer.set(pocketOwnerKey, DataType.UUID, player.getUniqueId());

		this.generateMisc(pocket);
		pocket.setSpawnLocation(1, 1, -1);

		return pocket;
	}

	private void generateMisc(@NotNull World pocket) {
		pocket.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		pocket.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);

		WorldBorder border = pocket.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(2 * (PocketHomePlugin.getPocketRadius() + 1) * 16);
		border.setWarningDistance(0);
	}
}
