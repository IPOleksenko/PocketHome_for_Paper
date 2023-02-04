package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
							       
							I'm a PocketHome. \n
							You can teleport from home/to the house by right-clicking on the chest while crouching.
							       
							You can open my menu with the «/home» command.
							""", player.getName()));
		}
	}
}