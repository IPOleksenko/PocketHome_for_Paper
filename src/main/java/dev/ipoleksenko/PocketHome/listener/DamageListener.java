package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageListener implements Listener {

	@EventHandler
	public void onDamageVoid(@NotNull EntityDamageEvent event) {
		if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
			Player player = (Player) event.getEntity();
			if (PocketHomePlugin.getInstance().getPocketManager().isInPocket(player)) {
				player.setFallDistance(0);
				PocketHomePlugin.getInstance().getPocketManager().teleportFromPocket(player);
				event.setCancelled(true);
			}
		}
	}
}
