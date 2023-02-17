package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.*;

public abstract class PluginWorldManager {

	protected static PocketManager pocketInstance;
	protected static LinkerManager linkerInstance;
	protected final NamespacedKey pocketKey;
	protected final NamespacedKey pocketOwnerKey;
	protected final NamespacedKey pocketGuestsKey;
	protected final NamespacedKey teleportLocationKey;
	protected final NamespacedKey linkedPocketKey;
	private final NamespacedKey leashedEntityKey;


	public PluginWorldManager() {
		this.pocketKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.pocketOwnerKey = NamespacedKey.fromString("pocket_owner", PocketHomePlugin.getInstance());
		this.pocketGuestsKey = NamespacedKey.fromString("pocket_guests", PocketHomePlugin.getInstance());
		this.teleportLocationKey = NamespacedKey.fromString("teleport_location", PocketHomePlugin.getInstance());
		this.linkedPocketKey = NamespacedKey.fromString("linked_pocket", PocketHomePlugin.getInstance());
		this.leashedEntityKey = NamespacedKey.fromString("leashed_entity", PocketHomePlugin.getInstance());
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
		if (this.isInPocket(player)) return false;
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
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final Location spawnLocation = world.getSpawnLocation();
		if (!this.isInPocket(player)) playerContainer.set(teleportLocationKey, DataType.LOCATION, player.getLocation());

		player.setInvulnerable(true);
		return this.teleportPlayer(player, spawnLocation);
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
		Location spawnLocation = playerContainer.get(teleportLocationKey, DataType.LOCATION);

		if (spawnLocation == null) spawnLocation = player.getBedSpawnLocation();
		if (spawnLocation == null) spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();

		player.setInvulnerable(false);
		return this.teleportPlayer(player, spawnLocation);
	}

	/**
	 * Adds n-ticks to current Pocket/Linker time
	 *
	 * @param player target Player object for getting a Pocket/Linker world
	 * @param time   ticks to add
	 */
	public void addTime(@NotNull Player player, Long time) {
		if (!this.isInPocket(player)) return;
		if (time < 1) return;

		final World pocket = player.getWorld();
		final Long currentTime = pocket.getTime();
		this.setTime(player, currentTime + time);
	}

	/**
	 * Sets n-ticks for Pocket/Linker time
	 *
	 * @param player target Player object for getting a Pocket/Linker world
	 * @param time   ticks to set
	 */
	public void setTime(@NotNull Player player, Long time) {
		if (!this.isInPocket(player)) return;
		if (time < 1) return;

		final World pocket = player.getWorld();
		pocket.setTime(time);
	}

	private boolean teleportPlayer(@NotNull Player player, Location spawnLocation) {
		player.teleport(spawnLocation, TeleportCause.PLUGIN);

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		this.getLeashedEntities(playerContainer, false).stream()
						.map(Bukkit::getEntity)
						.filter(Objects::nonNull)
						.filter(LivingEntity.class::isInstance)
						.map(LivingEntity.class::cast)
						.forEach(livingEntity -> {
							livingEntity.setLeashHolder(null);
							livingEntity.teleport(spawnLocation, TeleportCause.PLUGIN);
							livingEntity.setLeashHolder(player);
						});

		return true;
	}

	public void addLeashed(@NotNull PersistentDataContainer playerContainer, @NotNull Entity entity) {
		final List<UUID> leashedEntities = this.getLeashedEntities(playerContainer, true);
		final UUID entityUID = entity.getUniqueId();
		if (!leashedEntities.contains(entityUID)) leashedEntities.add(entityUID);

		playerContainer.set(leashedEntityKey, DataType.UUID_LIST, leashedEntities);
	}

	public void removeLeashed(@NotNull PersistentDataContainer playerContainer, @NotNull Entity entity) {
		final List<UUID> leashedEntities = this.getLeashedEntities(playerContainer, true);
		final UUID entityUID = entity.getUniqueId();
		leashedEntities.remove(entityUID);

		playerContainer.set(leashedEntityKey, DataType.UUID_LIST, leashedEntities);
	}

	public void clearLeashedEntity(@NotNull PersistentDataContainer playerContainer) {
		playerContainer.set(leashedEntityKey, DataType.UUID_LIST, new LinkedList<UUID>());
	}

	private @NotNull List<UUID> getLeashedEntities(@NotNull PersistentDataContainer playerContainer, boolean clearList) {
		List<UUID> leashedEntities = playerContainer.get(leashedEntityKey, DataType.UUID_LIST);
		if (leashedEntities == null) leashedEntities = new LinkedList<>();
		if (clearList) this.clearLeashedEntity(playerContainer);
		return leashedEntities;
	}

	protected void generateMisc(@NotNull World world) {
		world.setGameRule(GameRule.DISABLE_RAIDS, true);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DO_FIRE_TICK, false);
		world.setGameRule(GameRule.DO_INSOMNIA, false);
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
		world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
		world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.DROWNING_DAMAGE, false);
		world.setGameRule(GameRule.FALL_DAMAGE, false);
		world.setGameRule(GameRule.FIRE_DAMAGE, false);
		world.setGameRule(GameRule.FREEZE_DAMAGE, false);
		world.setGameRule(GameRule.KEEP_INVENTORY, true);
		world.setGameRule(GameRule.MOB_GRIEFING, false);

		world.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);
		world.setSpawnLocation(1, 1, -1);
	}
}