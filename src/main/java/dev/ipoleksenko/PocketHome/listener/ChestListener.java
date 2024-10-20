package dev.ipoleksenko.PocketHome.listener;

import dev.ipoleksenko.PocketHome.GUI.mainGUI;
import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChestListener implements Listener {
	mainGUI main = new mainGUI();

	// Check for clicks on items
	@EventHandler
	public void onInventoryClick(final @NotNull InventoryClickEvent event) {
		if (!event.getView().getTitle().equalsIgnoreCase("PocketHome"))
			return;

		event.setCancelled(true);

		final ItemStack clickedItem = event.getCurrentItem();

		// verify current item is not null
		if (clickedItem == null || clickedItem.getType().isAir()) return;

		final Player player = (Player) event.getWhoClicked();

		// Using slots click is a best option for your inventory click's
		if (event.getCurrentItem().getType() == Material.RED_BED)
			if (!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player)) {
				player.sendMessage("Teleport to HOME...");
				PocketHomePlugin.getInstance().getPocketManager().teleportTo(player);
			} else {
				player.sendMessage("Teleport from HOME...");
				PocketHomePlugin.getInstance().getPocketManager().teleportFrom(player);
			}
		else if (event.getCurrentItem().getType() == Material.CHEST)
			player.closeInventory();
	}

	// Cancel dragging in our inventory
	@EventHandler
	public void onInventoryClick(final @NotNull InventoryDragEvent event) {
		if (event.getInventory().equals(main.main)) {
			event.setCancelled(true);
		}
	}
}