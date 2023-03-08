package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.util.DataType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class PluginWorldManager {

	private static final Map<GameRule<Boolean>, Boolean> DEFAULT_GAMERULES = Map.ofEntries(
					Map.entry(GameRule.DISABLE_RAIDS, true),
					Map.entry(GameRule.DO_DAYLIGHT_CYCLE, false),
					Map.entry(GameRule.DO_FIRE_TICK, false),
					Map.entry(GameRule.DO_INSOMNIA, false),
					Map.entry(GameRule.DO_MOB_SPAWNING, false),
					Map.entry(GameRule.DO_PATROL_SPAWNING, false),
					Map.entry(GameRule.DO_TRADER_SPAWNING, false),
					Map.entry(GameRule.DO_WARDEN_SPAWNING, false),
					Map.entry(GameRule.DO_WEATHER_CYCLE, false),
					Map.entry(GameRule.DROWNING_DAMAGE, false),
					Map.entry(GameRule.FALL_DAMAGE, false),
					Map.entry(GameRule.FIRE_DAMAGE, false),
					Map.entry(GameRule.FREEZE_DAMAGE, false),
					Map.entry(GameRule.KEEP_INVENTORY, true),
					Map.entry(GameRule.MOB_GRIEFING, false)
	);
	protected static PocketManager pocketInstance;
	protected static LinkerManager linkerInstance;
	protected final NamespacedKey pocketKey = PocketHomePlugin.getInstance().getNamespacedKey("pocket");
	protected final NamespacedKey pocketOwnerKey = PocketHomePlugin.getInstance().getNamespacedKey("pocket_owner");
	protected final NamespacedKey pocketGuestsKey = PocketHomePlugin.getInstance().getNamespacedKey("pocket_guests");
	protected final NamespacedKey teleportLocationKey = PocketHomePlugin.getInstance()
					.getNamespacedKey("teleport_location");
	protected final NamespacedKey linkedPocketKey = PocketHomePlugin.getInstance().getNamespacedKey("linked_pocket");
	private final NamespacedKey leashedEntitiesKey = PocketHomePlugin.getInstance().getNamespacedKey("leashed_entities");

	/**
	 * Used for saving Pocket in separate folder
	 *
	 * @param worldName name of a World
	 * @return <i>pocketsDir</i>/<i>worldName</i>
	 */
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
		return linkerInstance.isLinked(otherPlayer) ? linkerInstance.teleportToLinker(player, otherPlayer)
		                                            : pocketInstance.teleportToPocket(player, otherPlayer);
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
		final Location defaultSpawn = player.getBedSpawnLocation() != null ? player.getBedSpawnLocation()
		                                                                   : Bukkit.getWorlds().get(0).getSpawnLocation();
		final Location spawnLocation = playerContainer.getOrDefault(teleportLocationKey, DataType.LOCATION, defaultSpawn);

		player.setInvulnerable(false);
		return this.teleportPlayer(player, spawnLocation);
	}

	private boolean teleportPlayer(@NotNull Player player, Location spawnLocation) {
		final boolean result = player.teleport(spawnLocation, TeleportCause.PLUGIN);
		if (!result) return false;

		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		this.getLeashedEntities(playerContainer, false)
						.stream()
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

		playerContainer.set(leashedEntitiesKey, DataType.UUID_LIST, leashedEntities);
	}

	public void removeLeashed(@NotNull PersistentDataContainer playerContainer, @NotNull Entity entity) {
		final List<UUID> leashedEntities = this.getLeashedEntities(playerContainer, true);
		final UUID entityUID = entity.getUniqueId();
		leashedEntities.remove(entityUID);

		playerContainer.set(leashedEntitiesKey, DataType.UUID_LIST, leashedEntities);
	}

	public void clearLeashedEntity(@NotNull PersistentDataContainer playerContainer) {
		playerContainer.set(leashedEntitiesKey, DataType.UUID_LIST, new LinkedList<>());
	}

	private @NotNull List<UUID> getLeashedEntities(@NotNull PersistentDataContainer playerContainer, boolean clearList) {
		final List<UUID> leashedEntities = playerContainer.getOrDefault(leashedEntitiesKey, DataType.UUID_LIST, new LinkedList<>());

		if (clearList) this.clearLeashedEntity(playerContainer);
		return leashedEntities;
	}

	protected void generateMisc(@NotNull World world) {
		DEFAULT_GAMERULES.forEach(world::setGameRule);

		world.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);
		world.setSpawnLocation(1, 1, -1);
	}
}