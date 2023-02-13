package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerDisconnect(@NotNull PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		PocketHomePlugin.getInstance().getPocketManager().teleportFrom(player);
	}

}
