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

public class PocketManager {

	private final NamespacedKey KEY;
	private final PocketChunkGenerator generator;


	public PocketManager(PocketChunkGenerator generator) {
		this.generator = generator;
		KEY = NamespacedKey.fromString("pocket", PocketHomePlugin.getInstance());
	}


	@Contract(pure = true)
	@NotNull
	private String getPocketPath(String pocketName) {
		return "pockets/" + pocketName;
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
		return player.getWorld().getName().contains("pockets/");
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
		player.sendMessage("Teleport from HOME...");
		World world = Bukkit.getWorlds().get(0);

		player.teleport(world.getSpawnLocation());
		player.setInvulnerable(false);
		player.setAllowFlight(false);
		player.setFlying(false);
	}


	@Nullable
	private World getPocket(@NotNull Player player, boolean createIfMissing) {
		PersistentDataContainer container = player.getPersistentDataContainer();

		World pocket = null;
		if (container.has(KEY)) {
			final UUID uuid = container.get(KEY, new UUIDDataType());
			pocket = Bukkit.getWorld(uuid);
		} else if (createIfMissing)
			pocket = this.createPocket(player);

		return pocket;
	}

	@Nullable
	private World createPocket(@NotNull Player player) {
		final String pocketName = player.getName();
		World pocket = this.getPocketCreator(this.getPocketPath(pocketName)).createWorld();
		if (pocket == null) return null;

		PersistentDataContainer container = player.getPersistentDataContainer();
		container.set(KEY, new UUIDDataType(), pocket.getUID());

		generateIsland(pocket);

		return pocket;
	}

	private void generateIsland(World pocket) {
		for (int x = -32; x < 32; ++x)
			for (int z = -32; z < 32; ++z) {
				pocket.getBlockAt(x, -64, z).setType(Material.BEDROCK);
				pocket.getBlockAt(x, 0, z).setType(Material.GRASS_BLOCK);
				for (int y = -63; y < -3; ++y){
					pocket.getBlockAt(x, y, z).setType(Material.STONE);
				}
				for (int y = -3; y < 0; ++y){
					pocket.getBlockAt(x, y, z).setType(Material.DIRT);
				}
			}

		pocket.getBlockAt(0, 1, 0).setType(Material.ENDER_CHEST);

		WorldBorder border = pocket.getWorldBorder();
		border.setCenter(0., 0.);
		border.setSize(64);
		border.setWarningDistance(0);
	}
}
