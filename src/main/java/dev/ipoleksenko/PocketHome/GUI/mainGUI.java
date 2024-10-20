package dev.ipoleksenko.PocketHome.GUI;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class mainGUI extends GUI {
	public final Inventory main = Bukkit.createInventory(null, 9, "PocketHome");

	public void initializeItems(Player player) {
		main.setItem(0, createGuiItem(Material.RED_BED,
						"Teleport",
						(!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player) ? "Teleport on home" : "Teleport from home")));
		main.setItem(8, createGuiItem(Material.CHEST, "Exit from chest", "You will close the chest"));
	}

	public void OpenGUI(final Player player) {
		initializeItems(player);
		initializeEmptyItems(main);
		player.openInventory(main);
	}
}