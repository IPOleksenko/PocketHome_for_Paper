package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class EnderChestListener implements Listener {
	@EventHandler
	public void action(@NotNull PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block blockEvent = event.getClickedBlock();
			if (blockEvent.getType() == Material.ENDER_CHEST) {
				if (player.isSneaking()) {
					if (!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player)) {
						player.sendMessage("Teleport to HOME...");
						PocketHomePlugin.getInstance().getPocketManager().teleportToPocket(player);
					} else {
						PocketHomePlugin.getInstance().getPocketManager().teleportFromPocket(player);
					}
				}
			}
		}
	}
}