package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public abstract class PluginWorldManager {

	protected static PocketManager pocketInstance;
	protected static LinkerManager linkerInstance;
	protected final NamespacedKey pocketKey;
	protected final NamespacedKey pocketOwnerKey;
	protected final NamespacedKey pocketGuestsKey;
	protected final NamespacedKey teleportLocationKey;
	protected final NamespacedKey linkedPocketKey;


	public PluginWorldManager() {
		this.pocketKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.pocketOwnerKey = NamespacedKey.fromString("pocket_owner", PocketHomePlugin.getInstance());
		this.pocketGuestsKey = NamespacedKey.fromString("pocket_guests", PocketHomePlugin.getInstance());
		this.teleportLocationKey = NamespacedKey.fromString("teleport_location", PocketHomePlugin.getInstance());
		this.linkedPocketKey = NamespacedKey.fromString("linked_pocket", PocketHomePlugin.getInstance());
	}

	protected @NotNull String getUniqueId(String prefix) {
		final Base64.Encoder encoder = Base64.getUrlEncoder();
		final ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
		final UUID uuid = UUID.randomUUID();

		bb.putLong(uuid.getLeastSignificantBits());
		final byte[] bytes = bb.array();

		return prefix + encoder.encodeToString(bytes).replace("=", "").toLowerCase();
	}

	/**
	 * Used for saving Pocket in separate folder
	 *
	 * @param worldName name of a World
	 * @return <i>pocketsDir</i>/<i>worldName</i>
	 */
	@Contract(pure = true)
	protected @NotNull String getWorldPath(String worldName) {
		return PocketHomePlugin.getPocketsDir() + worldName;
	}

	protected @NotNull WorldCreator getWorldCreator(String name) {
		return new WorldCreator(name).environment(World.Environment.NORMAL);
	}


	/**
	 * Checks if a player is in a Pocket or Linker
	 *
	 * @param player Player object
	 * @return true, if in a pocket, false otherwise
	 */
	public boolean isInPocket(@NotNull Player player) {
		return player.getWorld().getName().contains(PocketHomePlugin.getPocketsDir());
	}

	/**
	 * Teleport Player to a Linker if linked or to a Pocket otherwise
	 *
	 * @param player target Player object
	 * @return true, if the teleport was successful
	 */
	public boolean teleportTo(@NotNull Player player) {
		return linkerInstance.isLinked(player) ? linkerInstance.teleportToLinker(player) : this.teleportTo(player, player);
	}

	/**
	 * Teleport Player to other's Player Pocket
	 *
	 * @param player      target Player object
	 * @param otherPlayer destination Player object
	 * @return true, if the teleport was successful
	 */
	public boolean teleportTo(@NotNull Player player, @NotNull Player otherPlayer) {
		return linkerInstance.isLinked(otherPlayer) ? linkerInstance.teleportToLinker(player, otherPlayer) : pocketInstance.teleportToPocket(player, otherPlayer);
	}

	/**
	 * Teleports player to a specified Pocket or Linker
	 *
	 * @param player target Player object
	 * @param world  destination World object
	 * @return true, if the teleport was successful
	 */
	public boolean teleportTo(@NotNull Player player, @NotNull World world) {
		if (this.isInPocket(player)) {
			final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
			playerContainer.set(teleportLocationKey, DataType.LOCATION, player.getLocation());
		}

		player.setInvulnerable(true);
		return player.teleport(world.getSpawnLocation());
	}

	/**
	 * Teleports Player to overworld.
	 * Trying to teleport to the latest location.
	 * Teleports to bed spawn location if the latest location does not exist.
	 * Otherwise, teleports to the overworld spawn
	 *
	 * @param player target Player object
	 * @return true, if the teleport was successful
	 */
	public boolean teleportFrom(@NotNull Player player) {
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		Location location = playerContainer.get(teleportLocationKey, DataType.LOCATION);

		if (location == null) location = player.getBedSpawnLocation();
		if (location == null) location = Bukkit.getWorlds().get(0).getSpawnLocation();

		player.setInvulnerable(false);
		return player.teleport(location);
	}

}
