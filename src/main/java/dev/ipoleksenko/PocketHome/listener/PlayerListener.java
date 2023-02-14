package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class PlayerListener implements Listener {
	@EventHandler
	public void join(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PocketHomePlugin.getInstance().getPocketManager().teleportToPocket(player);
		if (!player.hasPlayedBefore()) {
			player.sendMessage(format("""
							Hello, %s.    
							I'm a PocketHome.
							You can open my menu by clicking on the ender chest while sneaking.
							""", player.getName()));
		}
	}

	@EventHandler
	public void onPlayerDisconnect(@NotNull PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		PocketHomePlugin.getInstance().getPocketManager().teleportFrom(player);
	}
}