package dev.ipoleksenko.PocketHome.GUI;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class mainGUI extends GUI {
	public final Inventory main = Bukkit.createInventory(null, 9, "PocketHome");

	public void initializeItems(Player player) {
		main.setItem(1, createGuiItem(Material.RED_BED,
						"Teleport",
						(!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player) ? "Teleport on home" : "Teleport from home")));
		main.setItem(3, createGuiItem(Material.ACACIA_DOOR, "Enter someone else's house", "Enter a house whose owner has allowed entry"));
		main.setItem(5, createGuiItem(Material.REDSTONE, "Settings", "Your home settings"));
		main.setItem(7, createGuiItem(Material.BOOK, "Help", "Help using PocketHome"));
	}

	public void OpenGUI(final Player player) {
		initializeItems(player);
		initializeEmptyItems(main);
		player.openInventory(main);
	}
}