package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import dev.ipoleksenko.PocketHome.manager.PocketManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class PlayerListener implements Listener {

	@EventHandler
	public void join(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();
		final PocketManager pocketManager = PocketHomePlugin.getInstance().getPocketManager();

		pocketManager.teleportFrom(player);
		pocketManager.teleportTo(player);
		if (!player.hasPlayedBefore()) {
			player.sendMessage(format("""
							Hello, %s.
							       
							I'm a PocketHome. \n
							You can teleport from home/to the house by right-clicking on the chest while crouching.
							       
							You can open my menu with the «/home» command.
							""", player.getName()));
		}
	}

	@EventHandler
	public void onLeash(@NotNull PlayerLeashEntityEvent event) {
		final Player player = event.getPlayer();
		final Entity entity = event.getEntity();
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PocketManager pocketManager = PocketHomePlugin.getInstance().getPocketManager();

		pocketManager.addLeashed(playerContainer, entity);
	}

	@EventHandler
	public void onUnleash(@NotNull PlayerUnleashEntityEvent event) {
		final Player player = event.getPlayer();
		final Entity entity = event.getEntity();
		final PersistentDataContainer playerContainer = player.getPersistentDataContainer();
		final PocketManager pocketManager = PocketHomePlugin.getInstance().getPocketManager();

		pocketManager.removeLeashed(playerContainer, entity);
	}
}
