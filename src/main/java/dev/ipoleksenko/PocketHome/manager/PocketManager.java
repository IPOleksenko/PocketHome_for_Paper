package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
import dev.ipoleksenko.PocketHome.util.LocationDataType;
import dev.ipoleksenko.PocketHome.util.UUIDDataType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Manager for pocket worlds
 */
public class PocketManager {

	private final NamespacedKey pocketUIDKey;
	private final NamespacedKey teleportLocationKey;
	private final PocketChunkGenerator generator;


	public PocketManager(PocketChunkGenerator generator) {
		this.pocketUIDKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
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
	 * @param pocketName Name of a pocket
	 * @return WorldCreator for pocket worlds instance
	 */
	@NotNull
	public WorldCreator getPocketCreator(String pocketName) {
		return new WorldCreator(pocketName)
						.generator(getGenerator())
						.environment(World.Environment.NORMAL);
	}

	private ChunkGenerator getGenerator() {
		return this.generator;
	}


	/**
	 * Checks if a player is in a pocket
	 *
	 * @param player Player instance
	 * @return true, if in a pocket, false otherwise
	 */
	public boolean isInPocket(@NotNull Player player) {
		return player.getWorld().getName().contains(PocketHomePlugin.getPocketsDir());
	}

	/**
	 * Teleports player to his pocket
	 *
	 * @param player Target Player instance
	 * @return true if success, false otherwise
	 */
	public boolean teleportToPocket(Player player) {
		return this.teleportToPocket(player, player);
	}

	/**
	 * Teleports player to a specified player pocket
	 *
	 * @param player      Target Player instance
	 * @param otherPlayer Destination Player instance
	 * @return True on successful teleport, false if pocket does not exist
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull Player otherPlayer) {
		World pocket = this.getPocket(otherPlayer, player == otherPlayer);
		if (pocket == null) return false;

		PersistentDataContainer container = player.getPersistentDataContainer();
		container.set(teleportLocationKey, new LocationDataType(), player.getLocation());

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
	 * @param player Target Player instance
	 */
	public void teleportFromPocket(@NotNull Player player) {
		PersistentDataContainer container = player.getPersistentDataContainer();
		Location location = container.get(teleportLocationKey, new LocationDataType());

		if (location == null)
			location = player.getBedSpawnLocation();

		if (location == null)
			location = Bukkit.getWorlds().get(0).getSpawnLocation();

		player.teleport(location);
		player.setInvulnerable(false);
	}


	@Nullable
	private World getPocket(@NotNull Player player, boolean createIfMissing) {
		PersistentDataContainer container = player.getPersistentDataContainer();
		final UUID pocketUID = container.get(pocketUIDKey, new UUIDDataType());
		World pocket = Bukkit.getWorld(pocketUID);

		if (pocket == null && createIfMissing)
			pocket = this.createPocket(player);

		return pocket;
	}

	@Nullable
	private World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		World pocket = this.getPocketCreator(this.getPocketPath(pocketName)).createWorld();
		if (pocket == null) return null;

		PersistentDataContainer container = player.getPersistentDataContainer();
		container.set(pocketUIDKey, new UUIDDataType(), pocket.getUID());

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
