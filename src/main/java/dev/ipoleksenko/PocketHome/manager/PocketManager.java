package dev.ipoleksenko.PocketHome.manager;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.generator.PocketChunkGenerator;
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
	private final PocketChunkGenerator generator;


	public PocketManager(PocketChunkGenerator generator) {
		this.pocketUIDKey = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
		this.generator = generator;
	}


	/**
	 * Used for saving pockets in separate folder
	 *
	 * @param pocketName name of a pocket
	 * @return pockets/`pocketName`
	 */
	@Contract(pure = true)
	@NotNull
	private String getPocketPath(String pocketName) {
		return PocketHomePlugin.getPocketsDir() + pocketName;
	}

	@NotNull
	private WorldCreator getPocketCreator(String pocketName) {
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
	 * @param player Player instance
	 * @return true if success, false otherwise
	 */
	public boolean teleportToPocket(Player player) {
		return this.teleportToPocket(player, player);
	}

	/**
	 * Teleports player to other's player pocket
	 *
	 * @param player      Player instance
	 * @param otherPlayer destination Player instance
	 * @return true if success, false otherwise
	 */
	public boolean teleportToPocket(@NotNull Player player, @NotNull Player otherPlayer) {
		boolean playerEquals = player.getName().equals(otherPlayer.getName());
		World pocket = this.getPocket(otherPlayer, playerEquals);
		if (pocket == null) return false;

		player.teleport(pocket.getSpawnLocation());
		player.setInvulnerable(true);
		player.setAllowFlight(true);
		return true;
	}

	/**
	 * Teleports player to overworld
	 *
	 * @param player player instance
	 */
	public void teleportFromPocket(@NotNull Player player) {
		World world = Bukkit.getWorlds().get(0);

		player.teleport(world.getSpawnLocation());
		player.setInvulnerable(false);
		player.setAllowFlight(false);
		player.setFlying(false);
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
