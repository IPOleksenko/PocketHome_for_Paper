package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
public class PocketManager extends PluginWorldManager {

	private final PocketChunkGenerator pocketGenerator;

	public PocketManager() {
		super();
		pocketInstance = this;

		this.pocketGenerator = new PocketChunkGenerator();
	}


	private ChunkGenerator getPocketGenerator() {
		return this.pocketGenerator;
	}

	/**
	 * Get WorldCreator for Pocket worlds
	 *
	 * @param pocketName name of a Pocket
	 * @return WorldCreator for a Pocket worlds object
	 */
	private @NotNull WorldCreator getPocketCreator(String pocketName) {
		return this.getWorldCreator(this.getWorldPath(pocketName)).generator(this.getPocketGenerator());
	}

	/**
	 * Teleports player to a specified Player Pocket
	 *
	 * @param player      target Player object
	 * @param otherPlayer destination Player object
	 * @return true on successful teleport, false if pocket doesn't exist
	 */
	protected boolean teleportToPocket(@NotNull Player player, @NotNull Player otherPlayer) {
		if (player != otherPlayer && !this.isGuest(player, otherPlayer)) return false;
		final World pocket = this.getPocket(otherPlayer);
		return super.teleportTo(player, pocket);
	}

	/**
	 * Checks if Player has other Player as guest
	 *
	 * @param player      Player object, Pocket owner
	 * @param otherPlayer Player object, Pocket guest
	 * @return true, if other Player is a guest, false otherwise
	 */
	public boolean isGuest(@NotNull Player player, @NotNull Player otherPlayer) {
		return this.getPocketGuests(player).contains(otherPlayer);
	}

	/**
	 * Adds to Player Pocket UUID. That Player will be able to visit pocket
	 *
	 * @param owner Player object, Pocket owner
	 * @param guest Player object, guest that will be able to visit Pocket
	 * @return true on success, false if pocket doesn't exist
	 */
	public boolean addGuestToPocket(@NotNull Player owner, @NotNull Player guest) {
		final World pocket = this.getPocket(owner);

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.addGuest(pocketContainer, DataType.UUID_LIST, guest.getUniqueId());
		this.addGuest(guestContainer, DataType.STRING_LIST, pocket.getName());

		return true;
	}

	/**
	 * Removes OfflinePlayer UUID from Pocket. That Player will not be able to visit pocket
	 *
	 * @param owner Player object, Pocket owner
	 * @param guest OfflinePlayer object, guest that will not be able to visit Pocket
	 * @return true on success, false if pocket doesn't exist
	 */
	public boolean removeGuestFromPocket(@NotNull Player owner, @NotNull OfflinePlayer guest) {
		final Player guestOnline = guest.getPlayer();
		if (guestOnline != null) return this.removeGuestFromPocket(owner, guestOnline);
		final World pocket = this.getPocket(owner);

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		this.removeGuest(pocketContainer, DataType.UUID_LIST, guest.getUniqueId());

		return true;
	}

	/**
	 * Removes from Player Pocket UUID. That Player will not be able to visit pocket
	 *
	 * @param owner Player object, Pocket owner
	 * @param guest Player object, guest that will not be able to visit Pocket
	 * @return true on success, false if pocket doesn't exist
	 */
	public boolean removeGuestFromPocket(@NotNull Player owner, @NotNull Player guest) {
		if (!this.isGuest(owner, guest)) return false;
		final World pocket = this.getPocket(owner);

		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final PersistentDataContainer guestContainer = guest.getPersistentDataContainer();

		this.removeGuest(pocketContainer, DataType.UUID_LIST, guest.getUniqueId());
		this.removeGuest(guestContainer, DataType.STRING_LIST, pocket.getName());

		final World guestWorld = guest.getWorld();
		if (guestWorld.getUID() == pocket.getUID()) super.teleportFrom(guest);

		return true;
	}

	/**
	 * Get all Pocket owners that can be visited by Player
	 *
	 * @param player target Player object
	 * @return List of available to visit Pockets
	 */
	public List<OfflinePlayer> getGuestPockets(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final List<String> guestPockets = playerContainer.get(pocketGuestsKey, DataType.STRING_LIST);
		if (guestPockets == null) return new LinkedList<>();

		return guestPockets.stream().map(Bukkit::getWorld).filter(Objects::nonNull).map(this::getPocketOwner).toList();
	}

	/**
	 * Get guests of a Player Pocket
	 *
	 * @param player Player object, pocket owner
	 * @return List of guests, null if player hasn't pocket
	 */
	public List<OfflinePlayer> getPocketGuests(@NotNull Player player) {
		final World pocket = this.getPocket(player);
		return this.getPocketGuests(pocket);
	}

	/**
	 * Get guests of a Pocket
	 *
	 * @param pocket Pocket object
	 * @return List of guests
	 */
	private List<OfflinePlayer> getPocketGuests(@NotNull World pocket) {
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
	protected @NotNull OfflinePlayer getPocketOwner(@NotNull World pocket) {
		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		final @NotNull UUID ownerUID = Objects.requireNonNull(pocketContainer.get(pocketOwnerKey, DataType.UUID));

		return Bukkit.getOfflinePlayer(ownerUID);
	}

	private <T, Z> void addGuest(@NotNull PersistentDataContainer container, PersistentDataType<Z, List<T>> dataType, T data) {
		final List<T> guests = container.getOrDefault(pocketGuestsKey, dataType, new LinkedList<>());
		if (guests.contains(data)) return;

		guests.add(data);
		container.set(pocketGuestsKey, dataType, guests);
	}

	private <T, Z> void removeGuest(@NotNull PersistentDataContainer container, PersistentDataType<Z, List<T>> type, T data) {
		final List<T> guests = container.getOrDefault(pocketGuestsKey, type, new LinkedList<>());
		if (guests.size() == 0) return;

		guests.remove(data);
		container.set(pocketGuestsKey, type, guests);
	}

	// meh
	protected @NotNull World getPocket(@NotNull Player player) {
		return this.createPocket(player);
	}

	private @NotNull World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		final @NotNull World pocket = Objects.requireNonNull(this.getPocketCreator(pocketName).createWorld());

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PersistentDataContainer pocketContainer = pocket.getPersistentDataContainer();
		if (playerContainer.has(pocketKey) && pocketContainer.has(pocketOwnerKey)) return pocket;

		playerContainer.set(pocketKey, PersistentDataType.STRING, pocket.getName().split("/")[1]);
		pocketContainer.set(pocketOwnerKey, DataType.UUID, player.getUniqueId());

		this.generateMisc(pocket);

		return pocket;
	}

	protected World loadPocket(@NotNull String pocketName) {
		return this.getPocketCreator(pocketName).createWorld();
	}

	protected void unloadPocket(@NotNull World pocket) {
		for (Player player : pocket.getPlayers())
			this.teleportFrom(player);
		Bukkit.unloadWorld(pocket, true);
	}

	protected void generateMisc(@NotNull World pocket) {
		super.generateMisc(pocket);

		final WorldBorder border = pocket.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(2 * PocketHomePlugin.getPocketRadius() * 16);
		border.setWarningDistance(0);
	}
}
