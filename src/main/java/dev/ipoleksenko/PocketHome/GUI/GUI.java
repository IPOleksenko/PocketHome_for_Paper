package dev.ipoleksenko.PocketHome.GUI;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GUI {
	public void initializeEmptyItems(@NotNull Inventory inventory) {
		ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, (byte) 15);
		ItemMeta emptyMeta = empty.getItemMeta();
		emptyMeta.setDisplayName("PocketHome");
		empty.setItemMeta(emptyMeta);
		while (inventory.firstEmpty() != -1)
			inventory.setItem(inventory.firstEmpty(), empty);
	}

	// Nice little method to create a gui item with a custom name, and description
	protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		// Set the name of the item
		meta.setDisplayName(name);

		// Set the lore of the item
		meta.setLore(Arrays.asList(lore));

		item.setItemMeta(meta);

		return item;
	}
}